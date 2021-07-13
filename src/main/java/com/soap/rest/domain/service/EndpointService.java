package com.soap.rest.domain.service;

import com.soap.rest.domain.model.entity.EndpointEntity;

import java.util.Optional;

public interface EndpointService {
    Optional<EndpointEntity> findById(Long id);
}
