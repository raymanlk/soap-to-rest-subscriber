package com.soap.rest.domain.service;

import com.predic8.wsdl.*;
import com.soap.rest.BusinessTemplateApplication;
import com.soap.rest.application.config.AppContext;
import com.soap.rest.domain.model.entity.*;
import com.soap.rest.domain.repository.StatusRepository;
import com.soap.rest.external.service.ArchiveFormat;
import com.soap.rest.external.util.ReplacementConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CodeGenerateServiceImpl implements CodeGenerateService {

    Logger logger = LoggerFactory.getLogger(CodeGenerateServiceImpl.class);

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private AppContext context;

    @Autowired
    private StatusRepository statusRepository;

    @Value("${destination.root-path}")
    private String destinationPath;

    @Override
    public void generate(Long id) {
        logger.info("Database Id: {}", id);
        Optional<EndpointEntity> endpoint = endpointService.findById(id);
        StatusEntity statusEntity = new StatusEntity();
        statusEntity.setEndpointEntity(endpoint.get());
        statusEntity.setStatus("START");
        statusRepository.save(statusEntity);
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

    private void generateTemplate(long timestamp) {
        try {
            logger.info("Generate base template started...");
            File sourceDirectory = new File("D:/DYNAMIC-WSDL/new/subscriber-to-jenkins/src/main/resources/soap-template");
            File destinationDirectory = new File(destinationPath + timestamp);
            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
            logger.info("Generate base template completed...");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void generateCode(Optional<EndpointEntity> endpoint, long timestamp) {
        Definitions definitions = null;
        try {
            definitions = parseWsdl(endpoint, timestamp);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        HashMap<String, String> messageMap = generateMessageMap(definitions);
        HashMap<String, HashMap<String, String>> map = generateMap(definitions, messageMap);
        generateProperties(endpoint, timestamp);
        generateController(map, timestamp, endpoint);
        generateService(map, timestamp);
        logger.info("Successfully generated project");
        StatusEntity statusEntity = new StatusEntity();
        statusEntity.setEndpointEntity(endpoint.get());
        statusEntity.setStatus("COMPLETE");
        statusRepository.save(statusEntity);
    }

    private Definitions parseWsdl(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        WSDLParser parser = new WSDLParser();
        if (endpoint.get().getUrl() != null) {
            generateUrlPom(endpoint, timestamp);
            logger.info("Parse url started...");
            return parser.parse(endpoint.get().getUrl());
        } else if (endpoint.get().getFileEntity().getType().equals("text/xml")) {
            logger.info("Parse wsdl started...");
            InputStream targetStream = new ByteArrayInputStream(endpoint.get().getFileEntity().getData());
            generateFilePom(endpoint, timestamp);
            return parser.parse(targetStream);


        } else {
            logger.info("Parse file started...");
            generateFilePom(timestamp);
            FileEntity fileEntity = endpoint.get().getFileEntity();
            ArchiveFormat instance = AppContext.getBean(fileEntity.getType(), ArchiveFormat.class);
            String wsdlFileName = instance.extract(fileEntity, timestamp);
            String wsdlLocation = destinationPath + timestamp + "/src/main/resources" + "/" + wsdlFileName;
            Definitions defs = parser.parse(wsdlLocation);
            logger.info("Parse file completed...");

            return defs;

        }
    }

    private HashMap<String, String> generateMessageMap(Definitions definitions) {
        logger.info("Generate message map started...");
        HashMap<String, String> messageMap = new HashMap<>();

        for (com.predic8.wsdl.Message msg : definitions.getMessages()) {
            for (Part part : msg.getParts()) {
                if (part.getElement() != null) {
                    messageMap.put(msg.getName(), part.getElement().getName());
                }
            }
        }
        logger.info("Generate message map completed...");
        return messageMap;
    }

    private HashMap<String, HashMap<String, String>> generateMap(Definitions definitions, HashMap<String, String> messageMap) {
        logger.info("Generate map started...");
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
        logger.info("Generate map completed...");
        return map;
    }

    private void generateFilePom(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        logger.info("Generate file pom started...");
        InputStream initialStream = new ByteArrayInputStream(endpoint.get().getFileEntity().getData());
        File targetFile = new File(destinationPath + timestamp + "/src/main/resources/app.xsd");
        FileUtils.copyInputStreamToFile(initialStream, targetFile);

        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
        content = content.replace("{WSDL}", ReplacementConstants.generateFilePom);
        File mainFile = new File(destinationPath + timestamp + "/pom.xml");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
        logger.info("Generate file pom completed...");
    }

    private void generateFilePom(long timestamp) throws IOException {
        logger.info("Generate file pom started...");
        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
        content = content.replace("{WSDL}", ReplacementConstants.generateZipPom);
        File mainFile = new File(destinationPath + timestamp + "/pom.xml");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
        logger.info("Generate file pom completed...");
    }

    private void generateUrlPom(Optional<EndpointEntity> endpoint, long timestamp) throws IOException {
        logger.info("Generate url pom started...");
        String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/pom.txt"), "UTF-8");
        content = content.replace("{WSDL}", ReplacementConstants.generateUrlPomStart
                + endpoint.get().getUrl() + ReplacementConstants.generateUrlPomEnd);
        File mainFile = new File(destinationPath + timestamp + "/pom.xml");
        FileUtils.writeStringToFile(mainFile, content, "UTF-8");
        logger.info("Generate url pom completed...");
    }

    private void generateProperties(Optional<EndpointEntity> endpoint, long timestamp) {
        logger.info("Generate properties started...");
        try {
            String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/properties.txt"), "UTF-8");
            content = content.replace("{ENDPOINT}", endpoint.get().getProduction());
            content = content.replace("{CONTEXT}", endpoint.get().getContext() + "/" + endpoint.get().getVersion());
            File mainFile = new File(destinationPath + timestamp + "/src/main/resources/application.properties");
            FileUtils.writeStringToFile(mainFile, content, "UTF-8");
            logger.info("Generate properties completed...");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void generateController(HashMap<String, HashMap<String, String>> map, long timestamp, Optional<EndpointEntity> endpoint) {
        try {
            logger.info("Generate controller started...");
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
                File mainFile = new File(destinationPath + timestamp + "/src/main/java/com/soap/client/" + capitalize(controllerEntity.getName()) + "Controller.java");
                FileUtils.writeStringToFile(mainFile, content, "UTF-8");
                logger.info("Generate controller completed...");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String generateBody(HashMap<String, HashMap<String, String>> map, long timestamp, List<OperationEntity> operationEntityList, String contollerName) {
        String complete = "";
        try {
            logger.info("Generate body started...");
            for (OperationEntity operationEntity : operationEntityList) {
                String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/operation.txt"), "UTF-8");
                HashMap<String, String> tempMap = map.get(operationEntity.getOriginalValue());
                content = content.replace("{CLASSNAME}", contollerName + "Controller.class");
                for (Map.Entry a : tempMap.entrySet()) {
                    content = content.replace(a.getKey().toString(), a.getValue().toString());
                }
                complete = complete.concat(content);
                complete = complete.concat("\n");
                logger.info("Generate body completed...");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return complete;
    }

    private void generateService(HashMap<String, HashMap<String, String>> map, long timestamp) {
        try {
            for (Map.Entry m : map.entrySet()) {
                logger.info("Generate service started...");
                String content = IOUtils.toString(BusinessTemplateApplication.class.getResourceAsStream("/template_service.txt"), "UTF-8");
                HashMap<String, String> tempMap = (HashMap<String, String>) m.getValue();
                for (Map.Entry a : tempMap.entrySet()) {
                    content = content.replace(a.getKey().toString(), a.getValue().toString());
                }
                File mainFile = new File(destinationPath + timestamp + "/src/main/java/com/soap/client/" + m.getKey() + "WebService.java");
                FileUtils.writeStringToFile(mainFile, content, "UTF-8");
                logger.info("Generate service completed...");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
