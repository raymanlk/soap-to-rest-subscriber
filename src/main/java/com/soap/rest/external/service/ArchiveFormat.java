package com.soap.rest.external.service;

import com.soap.rest.domain.model.entity.FileEntity;

import java.io.IOException;

public interface ArchiveFormat {
    String extract(FileEntity fileEntity, long timestamp) throws IOException;
}
