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

package org.grad.eNav.atonService.controllers.secom;

import org.grad.eNav.atonService.TestFeignSecurityConfig;
import org.grad.eNav.atonService.TestingConfiguration;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.services.secom.SecomSubscriptionService;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.SubscriptionResponseObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.grad.secom.core.interfaces.SubscriptionSecomInterface.SUBSCRIPTION_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class SubscriptionSecomControllerTest {

    /**
     * The Reactive Web Test Client.
     */
    @Autowired
    WebTestClient webTestClient;

    /**
     * The SECOM Subscription Service mock.
     */
    @MockBean
    SecomSubscriptionService secomSubscriptionService;

    // Test Variables
    private SubscriptionRequest subscriptionRequest;
    private SubscriptionRequest savedsubscriptionRequest;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        this.subscriptionRequest = new SubscriptionRequest();
        this.subscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.subscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
        this.savedsubscriptionRequest = new SubscriptionRequest();
        this.savedsubscriptionRequest.setUuid(UUID.randomUUID());
        this.savedsubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.savedsubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
    }

    /**
     * Test that the SECOM Subscription interface is configured properly and
     * returns the expected Subscription Response Object output.
     */
    @Test
    void testSubscription() {
        doReturn(savedsubscriptionRequest).when(this.secomSubscriptionService).save(any());

        webTestClient.post()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(subscriptionRequest), SubscriptionRequest.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponseObject.class)
                .consumeWith(response -> {
                    SubscriptionResponseObject subscriptionResponseObject = response.getResponseBody();
                    assertNotNull(subscriptionResponseObject);
                    assertEquals(savedsubscriptionRequest.getUuid(), subscriptionResponseObject.getSubscriptionIdentifier());
                    assertEquals("Subscription successfully created", subscriptionResponseObject.getResponseText());
                });
    }

    /**
     * Test that the SECOM Subscription interface will return an HTTP Status
     * BAD_REQUEST if a validation error occurs.
     */
    @Test
    void testSubscriptionBadRequest() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any());

        webTestClient.post()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(subscriptionRequest), SubscriptionRequest.class))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Subscription interface will return an HTTP Status
     * METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testSubscriptionMethodNotAllowed() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any());

        webTestClient.get()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }
}