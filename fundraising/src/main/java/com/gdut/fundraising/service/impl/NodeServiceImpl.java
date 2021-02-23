package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.service.NodeService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class NodeServiceImpl implements NodeService {
    @Override
    public void sendMessage(String message) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8088/fundraising/user/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("userPhone", "15522060993");
        map.add("userPassword", "11231230");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
        System.out.println(response.getBody());
    }

    public static void main(String[] args) {
        NodeService nodeService=new NodeServiceImpl();
        nodeService.sendMessage("143142432");
    }
}
