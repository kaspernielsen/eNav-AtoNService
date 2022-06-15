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

import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.BeaconCardinal;
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
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
     * The Model Mapper mock.
     */
    @Mock
    ModelMapper modelMapper;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Asynchronous Task Executor mock.
     */
    @Mock
    TaskExecutor taskExecutor;

    /**
     * The Request Context mock.
     */
    @Mock
    Optional<HttpServletRequest> httpServletRequest;

    /**
     * The Service Registry mock.
     */
    @Mock
    Optional<WebClient> serviceRegistry;

    /**
     * The S-125 Dataset Service mock.
     */
    @Mock
    DatasetService datasetService;

    /**
     * The UN/LoCode Service mock.
     */
    @Mock
    UnLoCodeService unLoCodeService;

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
    private AidsToNavigation aidsToNavigation;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Create a new subscription request
        this.newSubscriptionRequest = new SubscriptionRequest();
        this.newSubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.newSubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
        this.newSubscriptionRequest.setSubscriptionPeriodStart(LocalDateTime.now());
        this.newSubscriptionRequest.setSubscriptionPeriodEnd(LocalDateTime.now());
        this.newSubscriptionRequest.setGeometry(factory.createPoint(new Coordinate(52.98, 28)));

        // Create a an existing subscription request with a UUID
        this.existingSubscriptionRequest = new SubscriptionRequest();
        this.existingSubscriptionRequest.setUuid(UUID.randomUUID());
        this.existingSubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.existingSubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
        this.existingSubscriptionRequest.setSubscriptionPeriodStart(LocalDateTime.now());
        this.existingSubscriptionRequest.setSubscriptionPeriodEnd(LocalDateTime.now());
        this.existingSubscriptionRequest.setGeometry(factory.createPoint(new Coordinate(52.98, 28)));

        // Create a remove subscription object
        this.removeSubscription = new RemoveSubscription();
        this.removeSubscription.setSubscriptionIdentifier(this.existingSubscriptionRequest.getUuid());

        // Create a new AtoN message
        this.aidsToNavigation = new BeaconCardinal();
        this.aidsToNavigation.setId(BigInteger.valueOf(1));
        this.aidsToNavigation.setAtonNumber("AtonNumber001");
        this.aidsToNavigation.setIdCode("ID001");
        this.aidsToNavigation.setTextualDescription("Description of AtoN No 1");
        this.aidsToNavigation.setTextualDescriptionInNationalLanguage("National Language Description of AtoN No 1" );
        this.aidsToNavigation.setGeometry(factory.createPoint(new Coordinate(52.98, 28)));
        this.aidsToNavigation.setDateStart(LocalDate.now());
        this.aidsToNavigation.setDateEnd(LocalDate.now());

        // Finally alreay set the discovery service variables
        this.secomService.discoveryServiceUrl = "http://localhost:8444/v1/searchService";
    }

    /**
     * Test that the SECOM Service gets initialised correctly, and it subscribes
     * to the AtoN publish subscribe channels.
     */
    @Test
    void testInit()  {
        // Perform the service call
        this.secomService.init();

        assertNotNull(this.secomService.discoveryService);
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

        assertNull(this.secomService.discoveryService);
        verify(this.s125PublicationChannel, times(1)).destroy();
        verify(this.s125DeletionChannel, times(1)).destroy();
    }

    /**
     * Test that the SECOM service can process correctly the Aids to Navigation
     * publications received through the S-125 publish-subscribe channel.
     */
    @Test
    void testHandleMessage() {
        doReturn(Collections.singletonList(this.existingSubscriptionRequest)).when(this.secomService).findAllSubscriptions(any(), any(), any());
        doReturn(Boolean.TRUE).when(this.serviceRegistry).isEmpty();

        // Create a message to be handled
        Message message = Optional.of(this.aidsToNavigation).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                .map(builder -> builder.setHeader("deletion", false))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.secomService.handleMessage(message);

        // Verify that we look up the subscriptions in the proper way
        ArgumentCaptor<Geometry> geometryArgument = ArgumentCaptor.forClass(Geometry.class);
        ArgumentCaptor<LocalDateTime> fromTimeArgument = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toTimeArgument = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(this.secomService, times(1)).findAllSubscriptions(geometryArgument.capture(), fromTimeArgument.capture(), toTimeArgument.capture());

        // Verify the arguments
        assertNotNull(geometryArgument.getValue());
        assertNotNull(fromTimeArgument.getValue());
        assertNotNull(toTimeArgument.getValue());
        assertEquals(this.aidsToNavigation.getGeometry(), geometryArgument.getValue());
        assertEquals(this.aidsToNavigation.getDateStart().atStartOfDay(), fromTimeArgument.getValue());
        assertEquals(this.aidsToNavigation.getDateEnd().atTime(LocalTime.MAX), toTimeArgument.getValue());

        // Verify that we try to update the registered clients
        ArgumentCaptor<SubscriptionRequest> subscriptionRequestArgument = ArgumentCaptor.forClass(SubscriptionRequest.class);
        ArgumentCaptor<List<AidsToNavigation>> atonListArgument = ArgumentCaptor.forClass(List.class);
        verify(this.secomService, times(1)).sendToSubscription(subscriptionRequestArgument.capture(), atonListArgument.capture());

        // Verify the arguments
        assertNotNull(subscriptionRequestArgument.getValue());
        assertNotNull(atonListArgument.getValue());
        assertEquals(this.existingSubscriptionRequest.getUuid(), subscriptionRequestArgument.getValue().getUuid());
        assertEquals(1, atonListArgument.getValue().size());
    }

    /**
     * Test that the SECOM service can process correctly the Aids to Navigation
     * deletions received through the S-125 publish-subscribe channel.
     */
    @Test
    void handleMessageDeletion() {
        // Create a message to be handled
        Message message = Optional.of(this.aidsToNavigation).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                .map(builder -> builder.setHeader("deletion", true))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.secomService.handleMessage(message);

        // Verify that do not attempt to serve any Subscription Requests
        verify(this.secomService, never()).findAllSubscriptions(any(), any(), any());
        verify(this.secomService, never()).sendToSubscription(any(), any());
    }

    /**
     * Test that we can successfully create a new subscription request.
     */
    @Test
    void testSaveSubscription() {
        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionRepo).save(any());

        // Perform the service call
        SubscriptionRequest result = this.secomService.saveSubscription(this.newSubscriptionRequest);

        // Make sure everything seems OK
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result.getUuid());
        assertEquals(this.existingSubscriptionRequest.getContainerType(), result.getContainerType());
        assertEquals(this.existingSubscriptionRequest.getDataProductType(), result.getDataProductType());
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