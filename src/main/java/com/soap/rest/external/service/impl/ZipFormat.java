package com.soap.rest.external.service.impl;


import com.soap.rest.domain.model.entity.FileEntity;
import com.soap.rest.external.service.ArchiveFormat;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component(value = "application/x-zip-compressed")
public class ZipFormat implements ArchiveFormat {

    @Value("${destination.root-path}")
    private String destinationPath;

    @Override
    public String extract(FileEntity fileEntity, long timestamp) throws IOException {
        String wsdlFileName = "";
        File destDir = new File(destinationPath + timestamp+ "/src/main/resources");
        byte[] buffer = new byte[1024];
        InputStream targetStream = new ByteArrayInputStream(fileEntity.getData());
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
                String ext = FilenameUtils.getExtension(newFile.getName());
                if (ext.equals("xml")) {
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

        return wsdlFileName;
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
