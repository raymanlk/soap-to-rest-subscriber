package com.soap.rest.domain.service;

import com.soap.rest.domain.model.entity.EndpointEntity;
import com.soap.rest.domain.repository.EndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EndpointServiceImpl implements EndpointService {
    @Autowired
    EndpointRepository endpointRepository;

    @Override
    public Optional<EndpointEntity> findById(Long id) {
        return endpointRepository.findById(id);
    }

}
