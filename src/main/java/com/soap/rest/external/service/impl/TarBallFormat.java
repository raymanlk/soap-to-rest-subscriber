package com.soap.rest.external.service.impl;


import com.soap.rest.domain.model.entity.FileEntity;
import com.soap.rest.external.service.ArchiveFormat;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component(value = "application/x-tar")
public class TarBallFormat implements ArchiveFormat {

    @Value("${destination.root-path}")
    private String destinationPath;

    @Override
    public String extract(FileEntity fileEntity, long timestamp) throws IOException {
        File destFile = new File(destinationPath + timestamp+ "/src/main/resources");
        InputStream targetStream = new ByteArrayInputStream(fileEntity.getData());

        String wsdlFileName = "";
        TarArchiveInputStream tis = new TarArchiveInputStream(targetStream);
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
