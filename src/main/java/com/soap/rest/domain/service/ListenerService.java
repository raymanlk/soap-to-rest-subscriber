package com.soap.rest.domain.service;

import com.predic8.wsdl.*;
import com.soap.rest.BusinessTemplateApplication;
import com.soap.rest.domain.model.entity.ControllerEntity;
import com.soap.rest.domain.model.entity.EndpointEntity;
import com.soap.rest.domain.model.entity.OperationEntity;
import com.soap.rest.external.util.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ListenerService implements MessageListener {
    Logger logger = LoggerFactory.getLogger(ListenerService.class);

    private CodeGenerateService codeGenerateService;

    @Autowired
    public ListenerService(CodeGenerateService codeGenerateService) {
        this.codeGenerateService = codeGenerateService;
    }

    public void onMessage(Message message) {
        try {
            codeGenerateService.generate(Long.valueOf(new String(message.getBody())));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


}
