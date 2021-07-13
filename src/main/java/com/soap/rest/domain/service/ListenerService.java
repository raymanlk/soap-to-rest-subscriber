package com.soap.rest.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
