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
import org.grad.secom.core.models.CapabilityResponseObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.grad.secom.core.interfaces.CapabilitySecomInterface.CAPABILITY_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class CapabilitySecomControllerTest {

    /**
     * The Reactive Web Test Client.
     */
    @Autowired
    WebTestClient webTestClient;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {

    }

    /**
     * Test that the SECOM Capability interface is configured properly and
     * returns the expected Capability Response Object output.
     */
    @Test
    void testCapability() {
        webTestClient.get()
                .uri("/api/secom" + CAPABILITY_INTERFACE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CapabilityResponseObject.class)
                .consumeWith(response -> {
                    CapabilityResponseObject capabilityResponseObject = response.getResponseBody();
                    assertNotNull(capabilityResponseObject);
                    assertNotNull(capabilityResponseObject.getCapability());
                    assertFalse(capabilityResponseObject.getCapability().isEmpty());
                    assertEquals(1, capabilityResponseObject.getCapability().size());
                    assertEquals(ContainerTypeEnum.S100_DataSet, capabilityResponseObject.getCapability().get(0).getContainerType());
                    assertEquals(SECOM_DataProductType.S125, capabilityResponseObject.getCapability().get(0).getDataProductType());
                    assertEquals("/xsd/S125.xsd", capabilityResponseObject.getCapability().get(0).getProductSchemaUrl().getPath());
                    assertEquals("0.0.0", capabilityResponseObject.getCapability().get(0).getServiceVersion());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getAccess());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getEncryptionKey());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGet());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getSubscription());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGetByLink());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGetSummary());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getUpload());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getUploadLink());
                });
    }

    /**
     * Test that the SECOM Capability interface will respond with an HTTP
     * Status METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testCapabilityMethodNotAllowed() {
        webTestClient.post()
                .uri("/api/secom" + CAPABILITY_INTERFACE_PATH)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }


}