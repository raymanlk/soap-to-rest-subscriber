package com.soap.rest.domain.repository;

import com.soap.rest.domain.model.entity.EndpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointRepository extends JpaRepository<EndpointEntity, Long> {
}