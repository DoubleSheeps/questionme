package com.questionme.service;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

public interface HttpClientService {
    public String client(String url, HttpMethod method, MultiValueMap<String, String> params);
}
