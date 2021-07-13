package com.soap.rest.domain.service;

import com.soap.rest.BusinessTemplateApplication;
import com.soap.rest.domain.model.entity.ControllerEntity;
import com.soap.rest.domain.model.entity.EndpointEntity;
import com.soap.rest.domain.model.entity.OperationEntity;
import com.predic8.wsdl.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
@Transactional
public class ListenerService implements MessageListener {
    Logger logger = LoggerFactory.getLogger(ListenerService.class);

    private EndpointService endpointService;

    @Autowired
    public ListenerService(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    public void onMessage(Message message) {
        try {
            main(Long.valueOf(new String(message.getBody())));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void main(Long id) throws IOException {
        logger.info("This is the long id: {}", id);
        Optional<EndpointEntity> endpoint = endpointService.findById(id);
        if (endpoint.isPresent()) {
            logger.info("Endpoint url: {}", endpoint.get().getUrl());
        } else {
            logger.info("No endpoint found with id: {}", id);
            return;
        }
        long timestamp = System.currentTimeMillis();
        generateTemplate(timestamp);
        generateCode(endpoint, timestamp);
    }

    private void generateTemplate(long timestamp) throws IOException {
        File sourceDirectory = new File("D:/DYNAMIC-WSDL/subscriber-to-jenkins/src/main/resources/soap-template");
        File destinationDirectory = new File("D:/DYNAMIC-WSDL/" + timestamp);
        FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
    }

    private void generateCode(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        Definitions definitions = parseWsdl(endpoint, timestamp);
        HashMap<String, String> messageMap = generateMessageMap(definitions);
        HashMap<String, HashMap<String, String>> map = generateMap(definitions, messageMap);
        generateProperties(endpoint, timestamp);
        generateController(map, timestamp, endpoint);
        generateService(map, timestamp);
    }

    private Definitions parseWsdl(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        WSDLParser parser = new WSDLParser();
        if (endpoint.get().getUrl() != null) {
            generateUrlPom(endpoint, timestamp);
            return parser.parse(endpoint.get().getUrl());
        } else if (endpoint.get().getFileEntity().getType().equals("text/xml")) {
            InputStream targetStream = new ByteArrayInputStream(endpoint.get().getFileEntity().getData());
            generateFilePom(endpoint, timestamp);
            return parser.parse(targetStream);

        } else if (endpoint.get().getFileEntity().getType().equals("application/x-zip-compressed")) {
            try {
                String wsdlFileName = "";
                File destDir = new File("D:/DYNAMIC-WSDL/" + timestamp+ "/src/main/resources");
                byte[] buffer = new byte[1024];
                InputStream targetStream = new ByteArrayInputStream(endpoint.get().getFileEntity().getData());
                ZipInputStream zis = new ZipInputStream(targetStream);
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        //
                        String ext = FilenameUtils.getExtension(newFile.getName());
                        if(ext.equals("xml")) {
                            wsdlFileName = zipEntry.getName();
                        }

                        // write file content
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();
                String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
                content = content.replace("{WSDL}", "<schemaLanguage>WSDL</schemaLanguage>\n" +
                        "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
                        "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
                        "\t\t\t\t\t<schemaIncludes>\n" +
                        "\t\t\t\t\t\t<include>*.xml</include>\n" +
                        "\t\t\t\t\t</schemaIncludes>");
                File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/pom.xml");
                FileUtils.writeStringToFile(mainFile, content, "UTF-8");
                String xmlStream = "D:/DYNAMIC-WSDL/" + timestamp+ "/src/main/resources" + "/" + wsdlFileName;
                Definitions defs = parser.parse(xmlStream);
                return defs;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    private HashMap<String, String> generateMessageMap(Definitions definitions) {
        HashMap<String, String> messageMap = new HashMap<>();

        for (com.predic8.wsdl.Message msg : definitions.getMessages()) {
            for (Part part : msg.getParts()) {
                if (part.getElement() != null) {
                    messageMap.put(msg.getName(), part.getElement().getName());
                }
            }
        }
        return messageMap;
    }

    private HashMap<String, HashMap<String, String>> generateMap(Definitions definitions, HashMap<String, String> messageMap) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();

        for (PortType pt : definitions.getPortTypes()) {
            for (Operation op : pt.getOperations()) {
                HashMap<String, String> wsdlMap = new HashMap<>();
                wsdlMap.put("{PATH}", pt.getName().toLowerCase());
                wsdlMap.put("{SUB_PATH}", op.getName().toLowerCase());
                wsdlMap.put("{SUB_CLASS}", op.getName());
                wsdlMap.put("{GET_REQUEST}", messageMap.get(op.getInput().getMessage().getName()).substring(0, 1).toUpperCase() + messageMap.get(op.getInput().getMessage().getName()).substring(1));
                wsdlMap.put("{GET_RESPONSE}", messageMap.get(op.getOutput().getMessage().getName()).substring(0, 1).toUpperCase() + messageMap.get(op.getOutput().getMessage().getName()).substring(1));
                map.put(op.getName(), wsdlMap);
            }
        }

        return map;
    }

    private void generateFilePom(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        InputStream initialStream = new ByteArrayInputStream(endpoint.get().getFileEntity().getData());
        File targetFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/src/main/resources/app.xsd");
        FileUtils.copyInputStreamToFile(initialStream, targetFile);

        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
        content = content.replace("{WSDL}", "<schemaLanguage>WSDL</schemaLanguage>\n" +
                "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
                "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
                "\t\t\t\t\t<schemaIncludes>\n" +
                "\t\t\t\t\t\t<include>*.xsd</include>\n" +
                "\t\t\t\t\t\t<include>*.xml</include>\n" +
                "\t\t\t\t\t</schemaIncludes>");
        File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/pom.xml");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
    }

    private void generateUrlPom(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
        content = content.replace("{WSDL}", "" +
                "\t\t\t\t\t<schemaLanguage>WSDL</schemaLanguage>\n" +
                "\t\t\t\t\t<generateDirectory>src/main/java</generateDirectory>\n" +
                "\t\t\t\t\t<generatePackage>com.soap.client.wsdl</generatePackage>\n" +
                "\t\t\t\t\t<schemas>\n" +
                "\t\t\t\t\t\t<schema>\n" +
                "\t\t\t\t\t\t\t<url>" + endpoint.get().getUrl() + "</url>\n" +
                "\t\t\t\t\t\t</schema>\n" +
                "\t\t\t\t\t</schemas>");
        File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/pom.xml");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
    }

    private void generateProperties(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/properties.txt"), "UTF-8");
        content = content.replace("{ENDPOINT}", endpoint.get().getProduction());
        content = content.replace("{CONTEXT}", endpoint.get().getContext() + "/" + endpoint.get().getVersion());
        File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/src/main/resources/application.properties");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
    }

    private void generateController(HashMap<String, HashMap<String, String>> map, long timestamp, Optional<EndpointEntity> endpoint) throws IOException {
        List<ControllerEntity> list = endpoint.get().getControllers();
        for (ControllerEntity controllerEntity : list) {
            String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/template_main.txt"), "UTF-8");
            if (endpoint.get().isCors()) {
                content = content.replace("{CORS}", "@CrossOrigin(origins = \"*\", allowedHeaders = \"*\")");
            } else {
                content = content.replace("{CORS}", "");
            }
            String operation = capitalize(controllerEntity.getName());
            content = content.replace("{OP}", operation);
            String body = generateBody(map, timestamp, controllerEntity.getOperations(), capitalize(controllerEntity.getName()));
            content = content.replace("{INSERT}", body);
            File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/src/main/java/com/soap/client/" + capitalize(controllerEntity.getName()) + "Controller.java");
            FileUtils.writeStringToFile(mainFile, content, "UTF-8");
        }
    }


    private String generateBody(HashMap<String, HashMap<String, String>> map, long timestamp, List<OperationEntity> operationEntityList, String contollerName) throws IOException {
        String complete = "";
        for (OperationEntity operationEntity : operationEntityList) {
            String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/operation.txt"), "UTF-8");
            HashMap<String, String> tempMap = map.get(operationEntity.getOriginalValue());
            content = content.replace("{CLASSNAME}", contollerName + "Controller.class");
            for (Map.Entry a : tempMap.entrySet()) {
                content = content.replace(a.getKey().toString(), a.getValue().toString());
            }
            complete = complete.concat(content);
            complete = complete.concat("\n");
        }
        return complete;
    }

    private void generateService(HashMap<String, HashMap<String, String>> map, long timestamp) throws IOException {
        for (Map.Entry m : map.entrySet()) {
            String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/template_service.txt"), "UTF-8");
            HashMap<String, String> tempMap = (HashMap<String, String>) m.getValue();
            for (Map.Entry a : tempMap.entrySet()) {
                content = content.replace(a.getKey().toString(), a.getValue().toString());
            }
            File mainFile = new File("D:/DYNAMIC-WSDL/" + timestamp + "/src/main/java/com/soap/client/" + m.getKey() + "WebService.java");
            FileUtils.writeStringToFile(mainFile, content, "UTF-8");
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


}
