package com.soap.rest.domain.model.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;
@Entity
public class EndpointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String url;
    private String context;
    private String version;
    private String path;
    private String name;
    private String production;
    private String sandbox;
    private boolean cors;
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private FileEntity fileEntity;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "endpointEntity", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ControllerEntity> controllers;


    @OneToOne(mappedBy = "endpointEntity")
    private StatusEntity statusEntity;

    public EndpointEntity() {
    }


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public String getSandbox() {
        return sandbox;
    }

    public void setSandbox(String sandbox) {
        this.sandbox = sandbox;
    }

    public boolean isCors() {
        return cors;
    }

    public void setCors(boolean cors) {
        this.cors = cors;
    }

    public FileEntity getFileEntity() {
        return fileEntity;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public List<ControllerEntity> getControllers() {
        return controllers;
    }

    public void setControllers(List<ControllerEntity> controllers) {
        this.controllers = controllers;
    }

    public StatusEntity getStatusEntity() {
        return statusEntity;
    }

    public void setStatusEntity(StatusEntity statusEntity) {
        this.statusEntity = statusEntity;
    }
}
