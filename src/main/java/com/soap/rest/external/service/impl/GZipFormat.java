package com.soap.rest.external.service.impl;


import com.soap.rest.domain.model.entity.FileEntity;
import com.soap.rest.external.service.ArchiveFormat;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.zip.GZIPInputStream;

@Component(value = "application/x-gzip")
public class GZipFormat implements ArchiveFormat {

    Logger logger = LoggerFactory.getLogger(GZipFormat.class);

    @Value("${destination.root-path}")
    private String destinationPath;

    @Override
    public String extract(FileEntity fileEntity, long timestamp) throws IOException {
        String wsdlName = "";
        String tempTar = destinationPath + timestamp +  "\\temp.tar";

        try {
            File tarFile = new File(tempTar);
            // Calling method to decompress file
            tarFile = deCompressGZipFile(fileEntity, tarFile);
            File destFile = new File(destinationPath + timestamp+ "/src/main/resources");
            if (!destFile.exists()) {
                destFile.mkdir();
            }
            // Calling method to untar file
            wsdlName = unTarFile(tarFile, destFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return wsdlName;
    }

    private File deCompressGZipFile(FileEntity fileEntity, File tarFile) throws IOException {
        InputStream targetStream = new ByteArrayInputStream(fileEntity.getData());

        GZIPInputStream gZIPInputStream = new GZIPInputStream(targetStream);

        FileOutputStream fos = new FileOutputStream(tarFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gZIPInputStream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        gZIPInputStream.close();
        return tarFile;
    }

    private String unTarFile(File tarFile, File destFile) throws IOException {
        String wsdlFileName = "";
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);
        TarArchiveEntry tarEntry = null;

        while ((tarEntry = tis.getNextTarEntry()) != null) {
            File outputFile = new File(destFile + File.separator + tarEntry.getName());
            if (tarEntry.isDirectory()) {
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
            } else {
                outputFile.getParentFile().mkdirs();
                String ext = FilenameUtils.getExtension(outputFile.getName());
                if (ext.equals("xml")) {
                    wsdlFileName = outputFile.getName();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                fos.close();
            }
        }
        tis.close();
        return wsdlFileName;
    }
}