/*
 * Copyright (c) 2022 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.services.secom;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.atonService.models.domain.Pair;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.SECOM_ExchangeMetadata;
import org.grad.secom.core.models.SearchFilterObject;
import org.grad.secom.core.models.SearchObjectResult;
import org.grad.secom.core.models.SearchParameters;
import org.grad.secom.springboot.components.SecomClient;
import org.grad.secom.springboot.components.SecomConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * The SECOM Service Class.
 *
 * A service to handle the general SECOM requests that need additional
 * processing, not covered by the existing services, e.g signing.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class SecomService {

    /**
     * The Service Registry URL.
     */
    @Value("${gla.rad.aton-service.service-registry.url:}" )
    String discoveryServiceUrl;

    /**
     * The SECOM Configuration Properties.
     */
    @Autowired
    SecomConfigProperties secomConfigProperties;

    // Class Variables
    SecomClient discoveryService;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the S-125 publication channel.
     */
    @PostConstruct
    public void init() {
        log.info("SECOM Service is booting up...");
        this.discoveryService = Optional.ofNullable(this.discoveryServiceUrl)
                .filter(StringUtils::isNotBlank)
                .map(url -> {
                    try {
                        return new URL(url);
                    } catch (MalformedURLException ex) {
                        this.log.error("Invalid SECOM discovery service URL provided...", ex);
                        return null;
                    }
                })
                .map(url -> {
                    try {
                        return new SecomClient(url, secomConfigProperties);
                    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException ex) {
                        this.log.error("Unable to initialise the SSL context for the SECOM discovery service...", ex);
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("SECOM Service is shutting down...");
        this.discoveryService = null;
    }

    /**
     * Based on an MRN provided, this function will contact the SECOM discovery
     * service (in this case it's the MCP MSR) and request the client endpoint
     * URI. It will then construct a SECOM client to be returned for the URI
     * discovered.
     *
     * @param mrn the MRN to be lookup up
     * @return the SECOM client for the endpoint matching the provided URI
     */
    public SecomClient getClient(String mrn) {
        // Validate the MRN
        Optional.ofNullable(mrn)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new SecomValidationException("Cannot request a service discovery for an empty/invalid MRN"));

        // Make sure the service registry is available
        Optional.ofNullable(this.discoveryService)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new SecomValidationException("Subscription request found for S-125 dataset updates but no connection to service registry"));

        // Create the discovery service search filter object for the provided MRN
        final SearchFilterObject searchFilterObject = new SearchFilterObject();
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setInstanceId(mrn);
        searchFilterObject.setQuery(searchParameters);

        // Lookup the endpoints of the clients from the SECOM discovery service
        final Optional<SearchObjectResult[]> instances = Optional.ofNullable(discoveryService)
                .map(ds -> ds.search(searchFilterObject, 0, Integer.MAX_VALUE))
                .orElse(Optional.empty());

        // Extract the latest matching instance
        final SearchObjectResult instance = instances.map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .max(Comparator.comparing(SearchObjectResult::getVersion))
                .orElseThrow(() -> new SecomNotFoundException(mrn));

        // Now construct and return a SECOM client for the discovered URI
        try {
            return new SecomClient(new URL(instance.getEndpointUri()), secomConfigProperties);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException ex) {
            this.log.error(ex.getMessage(), ex);
            throw new SecomValidationException(ex.getMessage());
        }
    }

    /**
     * This helper function is to be used to implement the SECOM exchange
     * metadata population operation, by acquiring a signature for the
     * provided payload.
     *
     * @param payload the payload to be signed
     * @return the service exchange metadata with the signature information
     */
    public Pair<String, SECOM_ExchangeMetadata> signPayload(String payload) {
        // Sign the payload
        final String signedPayload = Base64.getEncoder().encodeToString(payload.getBytes());

        // Generate the SECOM metadata
        final SECOM_ExchangeMetadata serviceExchangeMetadata = new SECOM_ExchangeMetadata();
        serviceExchangeMetadata.setDataProtection(false);
        serviceExchangeMetadata.setCompressionFlag(false);

        // And return the information tuple
        return new Pair<>(signedPayload, serviceExchangeMetadata);
    }

}
