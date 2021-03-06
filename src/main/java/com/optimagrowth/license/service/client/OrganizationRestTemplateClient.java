package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.OrganizationRedisRepository;
import com.optimagrowth.license.utils.UserContext;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationRestTemplateClient {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    @Autowired
    private KeycloakRestTemplate restTemplate;

    @Autowired
    private OrganizationRedisRepository redisRepository;

    private Organization checkRedisCache(String organizationId){
        try {
            return redisRepository.findById(organizationId).orElse(null);
        } catch (Exception ex){
            logger.error("Error encountered while trying to retrieve organization{} check Redis cache. Exception {}",
                    organizationId, ex);
            return null;
        }
    }

    private void cacheOrganizationObject(Organization organization){
        try {
            redisRepository.save(organization);
        } catch (Exception ex){
            logger.error("Unable to cache organization {} in Redis. Exception {}",
                    organization.getId(), ex);
        }
    }

    public Organization getOrganization(String organizationId){
        logger.debug("In Licensing Service.getOrganization: {}",
                UserContext.getOrganizationId());

        //Check Redis Cache
        Organization organization = checkRedisCache(organizationId);
        if(organization != null) {
            logger.debug("I have successfully retrieved an organization {} from the redis cache: {}.",
                    organizationId, organization);
            return organization;
        }

        //Unable locate organization from Redis cache
        logger.debug("Unable to locate organization {} from the redis cache.", organizationId);

        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                "http://gateway:8072/organization/v1/organization/{organizationId}",
                HttpMethod.GET, null,
                Organization.class, organizationId);

        //Save the record to Redis cache
        organization = restExchange.getBody();
        if(organization != null){
            cacheOrganizationObject(organization);
        }
        return restExchange.getBody();
    }
}
