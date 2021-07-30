package com.soap.rest.domain.repository;

import com.soap.rest.domain.model.entity.EndpointEntity;
import com.soap.rest.domain.model.entity.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, Long> {
}
