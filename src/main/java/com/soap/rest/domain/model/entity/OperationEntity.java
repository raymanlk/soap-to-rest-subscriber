package com.soap.rest.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
public class OperationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long operationId;

    @Column
    private String originalValue;

    @ManyToOne
    @JoinColumn(name = "controller_id", referencedColumnName = "controllerId")
    @JsonBackReference
    private ControllerEntity controllerEntity;

    public long getOperationId() {
        return operationId;
    }

    public void setOperationId(long operationId) {
        this.operationId = operationId;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public ControllerEntity getControllerEntity() {
        return controllerEntity;
    }

    public void setControllerEntity(ControllerEntity controllerEntity) {
        this.controllerEntity = controllerEntity;
    }
}
