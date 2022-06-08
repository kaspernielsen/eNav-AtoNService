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

package org.grad.eNav.atonService.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

/**
 * The Service Registry Config.
 *
 * This configuration allows the definition of the service registry connection
 * as a Java bean to be used by other service components.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@ConditionalOnProperty(value = "gla.rad.aton-service.service-registry.url")
public class ServiceRegistryConfig {

    /**
     * The Service Registry URL.
     */
    @Value("${gla.rad.aton-service.service-registry.url}" )
    private String serviceRegistryUrl;

    /**
     * The Service Registry Java bean.
     *
     * @return the service registry bean
     */
    @Bean
    public WebClient serviceRegistry() throws SSLException {
        final SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        final HttpClient httpConnector = HttpClient
                .create()
                .followRedirect(true)
                .secure(t -> t.sslContext(sslContext) );
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpConnector))
                .baseUrl(serviceRegistryUrl)
                //.filter(setJWT())
                .build();
    }

    /**
     * A helper function that populates the authorization header of the
     * service registry regusts.
     *
     * @return the web client exchange filter function
     */
    private ExchangeFilterFunction setJWT() {
        return ExchangeFilterFunction.ofRequestProcessor((clientRequest) -> {
            ClientRequest authorizedRequest = ClientRequest.from(clientRequest).header("AUTHORIZATION","{LOGIC TO GET THE TOKEN}").build();
            return Mono.just(authorizedRequest);
        });
    }

}
