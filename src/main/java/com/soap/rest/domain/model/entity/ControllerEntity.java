package com.soap.rest.domain.model.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
public class ControllerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long controllerId;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "endpoint_id", referencedColumnName = "id")
    @JsonBackReference
    private EndpointEntity endpointEntity;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "controllerEntity", cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @JsonManagedReference
    private List<OperationEntity> operations;

    public long getControllerId() {
        return controllerId;
    }

    public void setControllerId(long controllerId) {
        this.controllerId = controllerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EndpointEntity getEndpointEntity() {
        return endpointEntity;
    }

    public void setEndpointEntity(EndpointEntity endpointEntity) {
        this.endpointEntity = endpointEntity;
    }

    public List<OperationEntity> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationEntity> operations) {
        this.operations = operations;
    }
}
