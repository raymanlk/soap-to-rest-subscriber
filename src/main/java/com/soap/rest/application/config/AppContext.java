package com.soap.rest.application.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class AppContext implements ApplicationContextAware {
    private static ApplicationContext context;

    public static <T> T getBean(String cls, Class<T> beanClass) {
        return context.getBean(cls, beanClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setContext(applicationContext);
    }

    private void setContext(ApplicationContext applicationContext) {
        AppContext.context = applicationContext;
    }
}