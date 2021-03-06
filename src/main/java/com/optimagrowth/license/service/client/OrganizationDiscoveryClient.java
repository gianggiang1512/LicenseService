package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrganizationDiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationDiscoveryClient.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    public Organization getOrganization(String organizationId){
        RestTemplate restTemplate = new RestTemplate();
        List<ServiceInstance> instances = discoveryClient.getInstances("organization-service");

        if(instances.size() == 0) return null;

        String serviceUri = String.format(
                "%s/v1/organization/%s", instances.get(0).getUri().toString(), organizationId
        );

        logger.info("OrganizationDiscoveryClient - ServiceUri: " + serviceUri);

        ResponseEntity<Organization> exchange =
                restTemplate.exchange(serviceUri, HttpMethod.GET, null, Organization.class, organizationId);

        return exchange.getBody();
    }
}
