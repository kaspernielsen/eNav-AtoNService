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
import org.grad.eNav.atonService.services.secom.SecomSubscriptionService;
import org.grad.secom.core.models.RemoveSubscriptionObject;
import org.grad.secom.core.models.RemoveSubscriptionResponseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.grad.secom.core.interfaces.RemoveSubscriptionSecomInterface.REMOVE_SUBSCRIPTION_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class RemoveSubscriptionSecomControllerTest {

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
    private RemoveSubscriptionObject removeSubscriptionObject;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        this.removeSubscriptionObject = new RemoveSubscriptionObject();
        this.removeSubscriptionObject.setSubscriptionIdentifier(UUID.randomUUID());
    }

    /**
     * Test that the SECOM Remove Subscription interface is configured properly
     * and returns the expected Remove Subscription Response Object output.
     */
    @Test
    void testRemoveSubscription() {
        doReturn(removeSubscriptionObject.getSubscriptionIdentifier()).when(this.secomSubscriptionService).delete(any());

        webTestClient.method(HttpMethod.DELETE)
                .uri("/api/secom" + REMOVE_SUBSCRIPTION_INTERFACE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(removeSubscriptionObject), RemoveSubscriptionObject.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody(RemoveSubscriptionResponseObject.class)
                .consumeWith(response -> {
                    RemoveSubscriptionResponseObject removeSubscriptionResponseObject = response.getResponseBody();
                    assertNotNull(removeSubscriptionResponseObject);
                    assertEquals(String.format("Subscription %s removed", removeSubscriptionObject.getSubscriptionIdentifier()), removeSubscriptionResponseObject.getResponseText());
                });
    }

}