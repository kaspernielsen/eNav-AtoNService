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

package org.grad.eNav.atonService.services;

import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.secom.exceptions.SecomNotFoundException;
import org.grad.secom.exceptions.SecomValidationException;
import org.grad.secom.models.enums.ContainerTypeEnum;
import org.grad.secom.models.enums.SECOM_DataProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecomServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    SecomService secomService;

    /**
     * The SECOM Subscription Repo mock.
     */
    @Mock
    SecomSubscriptionRepo secomSubscriptionRepo;

    /**
     * The S-125 Publish Channel to listen for the publications to.
     */
    @Mock
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Publish Channel to listen for the deletion to.
     */
    @Mock
    PublishSubscribeChannel s125DeletionChannel;

    // Test Variables
    private SubscriptionRequest newSubscriptionRequest;
    private SubscriptionRequest existingSubscriptionRequest;
    private RemoveSubscription removeSubscription;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a new subscription request
        this.newSubscriptionRequest = new SubscriptionRequest();
        this.newSubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.newSubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);

        // Create a an existing subscription request with a UUID
        this.existingSubscriptionRequest = new SubscriptionRequest();
        this.existingSubscriptionRequest.setUuid(UUID.randomUUID());
        this.existingSubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.existingSubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);

        // Create a remove subscription object
        this.removeSubscription = new RemoveSubscription();
        this.removeSubscription.setSubscriptionIdentifier(this.existingSubscriptionRequest.getUuid());
    }

    /**
     * Test that the SECOM Service gets initialised correctly, and it subscribes
     * to the AtoN publish subscribe channels.
     */
    @Test
    void testInit() {
        // Perform the service call
        this.secomService.init();

        verify(this.s125PublicationChannel, times(1)).subscribe(this.secomService);
        verify(this.s125DeletionChannel, times(1)).subscribe(this.secomService);
    }

    /**
     * Test that the SECOM Service gets destroyed correctly, and it
     * un-subscribes from the S-125 publish subscribe channels.
     */
    @Test
    void testDestroy() {
        // Perform the service call
        this.secomService.destroy();

        verify(this.s125PublicationChannel, times(1)).destroy();
        verify(this.s125DeletionChannel, times(1)).destroy();
    }

    /**
     * Test that we can successfully create a new subscription request.
     */
    @Test
    void testCreateSubscription() {
        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionRepo).save(any());

        // Perform the service call
        SubscriptionRequest result = this.secomService.createSubscription(this.newSubscriptionRequest);

        // Make sure everything seems OK
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result.getUuid());
        assertEquals(this.existingSubscriptionRequest.getContainerType(), result.getContainerType());
        assertEquals(this.existingSubscriptionRequest.getDataProductType(), result.getDataProductType());
    }

    /**
     * Test that if we try to create a new subscription, providing the UUID
     * value, a validation exception will be thrown. This should then be handled
     * by the SECOM controller interface.
     */
    @Test
    void testCreateSubscriptionWithUuid() {
        // Perform the service call
        assertThrows(SecomValidationException.class, () -> this.secomService.createSubscription(this.existingSubscriptionRequest));
    }

    /**
     * Test that we can successfully delete an existing subscription request.
     */
    @Test
    void testDeleteSubscription() {
        doReturn(Optional.of(this.existingSubscriptionRequest)).when(this.secomSubscriptionRepo).findById(this.removeSubscription.getSubscriptionIdentifier());

        // Perform the service call
        UUID result = this.secomService.deleteSubscription(this.removeSubscription);

        // Make sure everything seems OK
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result);
    }

    /**
     * Test that if we try to delete a subscription that does not exist in the
     * database, a SecomNotFoundException will be thrown, which should then
     * be handled by the SECOM controller interface.
     */
    @Test
    void testDeleteSubscriptionNotFound() {
        doReturn(Optional.empty()).when(this.secomSubscriptionRepo).findById(this.removeSubscription.getSubscriptionIdentifier());

        // Perform the service call
        assertThrows(SecomNotFoundException.class, () -> this.secomService.deleteSubscription(this.removeSubscription));
    }
}