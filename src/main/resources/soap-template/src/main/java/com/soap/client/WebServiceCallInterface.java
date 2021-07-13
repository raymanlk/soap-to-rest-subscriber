package com.soap.client;

import feign.Headers;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestBody;

public interface WebServiceCallInterface<T,V> {
    @RequestLine("POST")
    @Headers({"Content-Type: text/xml"})
    V generate(@RequestBody T request);
}