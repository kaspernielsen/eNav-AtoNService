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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.UploadObject;
import org.grad.secom.core.models.enums.AckRequestEnum;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.grad.secom.core.models.enums.SubscriptionEventEnum;
import org.grad.secom.springboot3.components.SecomClient;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
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
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecomSubscriptionServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    SecomSubscriptionService secomSubscriptionService;

    /**
     * The Entity Manager Factory mock.
     */
    @Mock
    EntityManagerFactory entityManagerFactory;

    /**
     * The UN/LoCode Service mock.
     */
    @Mock
    UnLoCodeService unLoCodeService;

    /**
     * The SECOM Service mock.
     */
    @Mock
    SecomService secomService;

    /**
     * The SECOM Subscription Notification Service mock.
     */
    @Mock
    SecomSubscriptionNotificationService secomSubscriptionNotificationService;

    /**
     * The SECOM Subscription Repo mock.
     */
    @Mock
    SecomSubscriptionRepo secomSubscriptionRepo;

    /**
     * TThe S-125 Dataset Channel to publish the published data to.
     */
    @Mock
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Dataset Channel to publish the deleted data to.
     */
    @Mock
    PublishSubscribeChannel s125DeletionChannel;

    // Test Variables
    private SubscriptionRequest newSubscriptionRequest;
    private SubscriptionRequest existingSubscriptionRequest;
    private RemoveSubscription removeSubscription;
    private S125DataSet s125Dataset;
    private DatasetContent datasetContent;

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
        this.existingSubscriptionRequest.setGeometry(factory.createPoint(new Coordinate(52.98, 1.28)));
        this.existingSubscriptionRequest.setClientMrn("urn:mrn:org:test");

        // Create a remove subscription object
        this.removeSubscription = new RemoveSubscription();
        this.removeSubscription.setSubscriptionIdentifier(this.existingSubscriptionRequest.getUuid());

        // Create a new AtoN message
        this.s125Dataset = new S125DataSet("S-125 Dataset");
        this.s125Dataset.setUuid(UUID.randomUUID());
        this.s125Dataset.setGeometry(factory.createPoint(new Coordinate(52.98, 1.28)));
        this.s125Dataset.setCreatedAt(LocalDateTime.now());
        this.s125Dataset.setLastUpdatedAt(LocalDateTime.now());
        this.datasetContent = new DatasetContent();
        this.datasetContent.setId(BigInteger.ONE);
        this.datasetContent.setContent("S-125 dataset content");
        this.datasetContent.setContentLength(BigInteger.valueOf(this.datasetContent.getContent().length()));
        this.datasetContent.setGeneratedAt(LocalDateTime.now());
        this.s125Dataset.setDatasetContent(this.datasetContent);
    }

    /**
     * Test that the SECOM Subscription Service gets initialised correctly, and
     * it subscribes to the AtoN publish subscribe channels.
     */
    @Test
    void testInit()  {
        // Perform the service call
        this.secomSubscriptionService.init();

        verify(this.entityManagerFactory, times(1)).createEntityManager();
        verify(this.s125PublicationChannel, times(1)).subscribe(this.secomSubscriptionService);
        verify(this.s125DeletionChannel, times(1)).subscribe(this.secomSubscriptionService);
    }

    /**
     * Test that the SECOM Subscription Service gets destroyed correctly, and it
     * un-subscribes from the S-125 publish subscribe channels.
     */
    @Test
    void testDestroy() {
        // Setup a mock entity manager
        this.secomSubscriptionService.entityManager = mock(EntityManager.class);

        // Perform the service call
        this.secomSubscriptionService.destroy();

        verify(this.secomSubscriptionService.entityManager , times(1)).close();
        verify(this.s125PublicationChannel, times(1)).destroy();
        verify(this.s125DeletionChannel, times(1)).destroy();
    }

    /**
     * Test that the SECOM subscription service can process correctly the S-125
     * dataset publications received through the S-125 dataset publish-subscribe
     * publication channel. This operation should identify all matching
     * subscription requests and inform them of the new update available.
     */
    @Test
    void testHandleMessagePublication() {
        doReturn(Collections.singletonList(this.existingSubscriptionRequest)).when(this.secomSubscriptionService).findAll(any(), any(), any(), any(), any(), any());
        doNothing().when(this.secomSubscriptionService).sendToSubscription(any(), any());

        // Create a message to be handled
        Message<S125DataSet> message = Optional.of(this.s125Dataset).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                .map(builder -> builder.setHeader("deletion", false))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.secomSubscriptionService.handleMessage(message);

        // Verify that we look up the subscriptions in the proper way
        ArgumentCaptor<ContainerTypeEnum> containerTypeArgument = ArgumentCaptor.forClass(ContainerTypeEnum.class);
        ArgumentCaptor<SECOM_DataProductType> dataProductTypeArgument = ArgumentCaptor.forClass(SECOM_DataProductType.class);
        ArgumentCaptor<String> productVersionArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> uuidArgument = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Geometry> geometryArgument = ArgumentCaptor.forClass(Geometry.class);
        ArgumentCaptor<LocalDateTime> timestampArgument = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(this.secomSubscriptionService, times(1)).findAll(
                containerTypeArgument.capture(),
                dataProductTypeArgument.capture(),
                productVersionArgument.capture(),
                uuidArgument.capture(),
                geometryArgument.capture(),
                timestampArgument.capture());

        // Verify the arguments
        assertNotNull(containerTypeArgument.getValue());
        assertNotNull(dataProductTypeArgument.getValue());
        assertNotNull(productVersionArgument.getValue());
        assertNotNull(uuidArgument.getValue());
        assertNotNull(geometryArgument.getValue());
        assertNotNull(timestampArgument.getValue());
        assertEquals(ContainerTypeEnum.S100_DataSet, containerTypeArgument.getValue());
        assertEquals(SECOM_DataProductType.S125, dataProductTypeArgument.getValue());
        assertEquals(this.s125Dataset.getDatasetIdentificationInformation().getProductEdition(), productVersionArgument.getValue());
        assertEquals(this.s125Dataset.getUuid(), uuidArgument.getValue());
        assertEquals(this.s125Dataset.getGeometry(), geometryArgument.getValue());
        assertEquals(this.s125Dataset.getLastUpdatedAt(), timestampArgument.getValue());

        // Verify that we try to update the registered clients
        ArgumentCaptor<SubscriptionRequest> subscriptionRequestArgument = ArgumentCaptor.forClass(SubscriptionRequest.class);
        ArgumentCaptor<S125DataSet> s125DatasetArgument = ArgumentCaptor.forClass(S125DataSet.class);
        verify(this.secomSubscriptionService, times(1)).sendToSubscription(subscriptionRequestArgument.capture(), s125DatasetArgument.capture());

        // Verify the arguments
        assertNotNull(subscriptionRequestArgument.getValue());
        assertNotNull(s125DatasetArgument.getValue());
        assertEquals(this.existingSubscriptionRequest.getUuid(), subscriptionRequestArgument.getValue().getUuid());
        assertEquals(this.s125Dataset.getUuid(), s125DatasetArgument.getValue().getUuid());
    }

    /**
     * Test that the SECOM subscription service can process correctly the S-125
     * dataset deletions received through the S-125 dataset publish-subscribe
     * deletion channel. This operation should identify the affected
     * subscription requests directly linked to the deleted dataset and notify
     * them of the deletion.
     */
    @Test
    void testHandleMessageDeletion() {
        doReturn(Collections.singletonList(this.existingSubscriptionRequest)).when(this.secomSubscriptionService).findAll(any(), any(), any(), any(), any(), any());
        doReturn(this.existingSubscriptionRequest.getUuid()).when(this.secomSubscriptionService).delete(any());

        // Create a message to be handled
        Message<S125DataSet> message = Optional.of(this.s125Dataset).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                .map(builder -> builder.setHeader("deletion", true))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.secomSubscriptionService.handleMessage(message);

        // Verify that we look up the subscriptions in the proper way for the deletion
        ArgumentCaptor<ContainerTypeEnum> containerTypeArgument = ArgumentCaptor.forClass(ContainerTypeEnum.class);
        ArgumentCaptor<SECOM_DataProductType> dataProductTypeArgument = ArgumentCaptor.forClass(SECOM_DataProductType.class);
        ArgumentCaptor<String> productVersionArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> uuidArgument = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Geometry> geometryArgument = ArgumentCaptor.forClass(Geometry.class);
        ArgumentCaptor<LocalDateTime> timestampArgument = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(this.secomSubscriptionService, times(1)).findAll(
                containerTypeArgument.capture(),
                dataProductTypeArgument.capture(),
                productVersionArgument.capture(),
                uuidArgument.capture(),
                geometryArgument.capture(),
                timestampArgument.capture());

        // Verify the arguments
        assertNull(containerTypeArgument.getValue());
        assertNull(dataProductTypeArgument.getValue());
        assertNull(productVersionArgument.getValue());
        assertNotNull(uuidArgument.getValue());
        assertNull(geometryArgument.getValue());
        assertNull(timestampArgument.getValue());
        assertEquals(this.s125Dataset.getUuid(), uuidArgument.getValue());

        // Verify that we try to inform the registered clients for the deletion
        ArgumentCaptor<RemoveSubscription> removeSubscriptionArgument = ArgumentCaptor.forClass(RemoveSubscription.class);
        verify(this.secomSubscriptionService, times(1)).delete(removeSubscriptionArgument.capture());

        // Verify the arguments
        assertNotNull(removeSubscriptionArgument.getValue());
        assertEquals(this.existingSubscriptionRequest.getUuid(), removeSubscriptionArgument.getValue().getSubscriptionIdentifier());
    }

    /**
     * Test that the SECOM subscription service will not process any messages
     * that are received through the publish-subscribe channels and are not in
     * the correct format.
     */
    @Test
    void testHandleMessageWrongFormat() {
        // Create a message to be handled
        Message message = Optional.of(this.s125Dataset).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.OTHER))
                .map(builder -> builder.setHeader("deletion", false))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.secomSubscriptionService.handleMessage(message);

        // Verify that we won't call any of the subscription functions
        verify(this.secomSubscriptionService, never()).findAll(any(), any(), any(), any(), any(), any());
        verify(this.secomSubscriptionService, never()).sendToSubscription(any(), any());
        verify(this.secomSubscriptionService, never()).delete(any());
    }

    /**
     * Test that we can search for all the subscription currently present in the
     * database and matching the provided criteria.
     */
    @Test
    void testFindAll() {
        // Mock the full text query
        SearchQuery<SubscriptionRequest> mockedQuery = mock(SearchQuery.class);
        SearchResult<SubscriptionRequest> searchResult = mock(SearchResult.class);
        SearchResultTotal searchResultTotal = mock(SearchResultTotal.class);
        doReturn(searchResult).when(mockedQuery).fetchAll();
        doReturn(Collections.singletonList(this.existingSubscriptionRequest)).when(searchResult).hits();
        doReturn(mockedQuery).when(this.secomSubscriptionService).getSubscriptionRequestSearchQuery(any(), any(), any(), any(), any(), any(), any());

        // Perform the service call
        List<SubscriptionRequest> result = this.secomSubscriptionService.findAll(
                this.existingSubscriptionRequest.getContainerType(),
                this.existingSubscriptionRequest.getDataProductType(),
                this.existingSubscriptionRequest.getProductVersion(),
                this.existingSubscriptionRequest.getDataReference(),
                this.existingSubscriptionRequest.getGeometry(),
                LocalDateTime.now());

        // Test the result
        assertNotNull(result);
        assertEquals(1, result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertNotNull(result.get(i));
            assertEquals(this.existingSubscriptionRequest.getUuid(), result.get(i).getUuid());
            assertEquals(this.existingSubscriptionRequest.getContainerType(), result.get(i).getContainerType());
            assertEquals(this.existingSubscriptionRequest.getDataProductType(), result.get(i).getDataProductType());
            assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodStart(), result.get(i).getSubscriptionPeriodStart());
            assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodEnd(), result.get(i).getSubscriptionPeriodEnd());
            assertEquals(this.existingSubscriptionRequest.getGeometry(), result.get(i).getGeometry());
            assertEquals(this.existingSubscriptionRequest.getClientMrn(), result.get(i).getClientMrn());
        }
    }



    /**
     * Test that we can successfully create a new subscription request.
     */
    @Test
    void testSave() {
        // Mock the HTTP servlet request
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionRepo).save(any());
//        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionService.entityManager).merge(any());

        // Perform the service call
        SubscriptionRequest result = this.secomSubscriptionService.save("urn:mrn:org:test", this.newSubscriptionRequest);

        // Make sure everything seems OK
        verify(this.secomSubscriptionRepo, never()).delete(any());
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result.getUuid());
        assertEquals(this.existingSubscriptionRequest.getContainerType(), result.getContainerType());
        assertEquals(this.existingSubscriptionRequest.getDataProductType(), result.getDataProductType());
        assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodStart(), result.getSubscriptionPeriodStart());
        assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodEnd(), result.getSubscriptionPeriodEnd());
        assertEquals(this.existingSubscriptionRequest.getGeometry(), result.getGeometry());
        assertEquals(this.existingSubscriptionRequest.getClientMrn(), result.getClientMrn());

        // Make sure the subscription notification was also sent
        verify(this.secomSubscriptionNotificationService, times(1)).sendNotification(
                eq(this.existingSubscriptionRequest.getClientMrn()),
                eq(this.existingSubscriptionRequest.getUuid()),
                eq(SubscriptionEventEnum.SUBSCRIPTION_CREATED));
    }

    /**
     * Test that we can successfully update an existing subscription request,
     * with the old request being deleted and recreated as required.
     */
    @Test
    void testSaveWithExistingRequest() {
        // Mock the HTTP servlet request
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        doReturn(Optional.of(this.existingSubscriptionRequest)).when(this.secomSubscriptionRepo).findByClientMrn(eq(this.existingSubscriptionRequest.getClientMrn()));
        doReturn(Optional.of(this.existingSubscriptionRequest)).when(this.secomSubscriptionRepo).findById(this.existingSubscriptionRequest.getUuid());
        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionRepo).save(any());
//        doReturn(this.existingSubscriptionRequest).when(this.secomSubscriptionService.entityManager).merge(any());

        // Perform the service call
        SubscriptionRequest result = this.secomSubscriptionService.save(this.existingSubscriptionRequest.getClientMrn(), this.newSubscriptionRequest);

        // Make sure everything seems OK
        verify(this.secomSubscriptionRepo, times(1)).delete(any());
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result.getUuid());
        assertEquals(this.existingSubscriptionRequest.getContainerType(), result.getContainerType());
        assertEquals(this.existingSubscriptionRequest.getDataProductType(), result.getDataProductType());
        assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodStart(), result.getSubscriptionPeriodStart());
        assertEquals(this.existingSubscriptionRequest.getSubscriptionPeriodEnd(), result.getSubscriptionPeriodEnd());
        assertEquals(this.existingSubscriptionRequest.getGeometry(), result.getGeometry());
        assertEquals(this.existingSubscriptionRequest.getClientMrn(), result.getClientMrn());

        // Make sure the subscription notifications were also sent
        verify(this.secomSubscriptionNotificationService, times(1)).sendNotification(
                eq(this.existingSubscriptionRequest.getClientMrn()),
                eq(this.existingSubscriptionRequest.getUuid()),
                eq(SubscriptionEventEnum.SUBSCRIPTION_REMOVED));
        verify(this.secomSubscriptionNotificationService, times(1)).sendNotification(
                eq(this.existingSubscriptionRequest.getClientMrn()),
                eq(this.existingSubscriptionRequest.getUuid()),
                eq(SubscriptionEventEnum.SUBSCRIPTION_CREATED));
    }

    /**
     * Test that if no MRN is provided in the HTTP request header, then the
     * subscription request will be declined with a SECOMValidationException.
     */
    @Test
    void testSaveBlankMrn() {
        // Perform the service call
        assertThrows(SecomValidationException.class, () -> this.secomSubscriptionService.save(null, this.newSubscriptionRequest));

        // Make sure no subscription notifications were sent
        verify(this.secomSubscriptionNotificationService, never()).sendNotification(any(), any(), any());
    }

    /**
     * Test that we can successfully delete an existing subscription request.
     */
    @Test
    void testDelete() {
        doReturn(Optional.of(this.existingSubscriptionRequest)).when(this.secomSubscriptionRepo).findById(this.removeSubscription.getSubscriptionIdentifier());

        // Perform the service call
        UUID result = this.secomSubscriptionService.delete(this.removeSubscription);

        // Make sure everything seems OK
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result);
        verify(this.secomSubscriptionNotificationService, times(1)).sendNotification(
                eq(this.existingSubscriptionRequest.getClientMrn()),
                eq(this.existingSubscriptionRequest.getUuid()),
                eq(SubscriptionEventEnum.SUBSCRIPTION_REMOVED));

        // Make sure the subscription notification was also sent
        verify(this.secomSubscriptionNotificationService, times(1)).sendNotification(
                eq(this.existingSubscriptionRequest.getClientMrn()),
                eq(this.existingSubscriptionRequest.getUuid()),
                eq(SubscriptionEventEnum.SUBSCRIPTION_REMOVED));
    }

    /**
     * Test that if we try to delete a subscription that does not exist in the
     * database, a SecomNotFoundException will be thrown, which should then
     * be handled by the SECOM controller interface.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Optional.empty()).when(this.secomSubscriptionRepo).findById(this.removeSubscription.getSubscriptionIdentifier());

        // Perform the service call
        assertThrows(SecomNotFoundException.class, () -> this.secomSubscriptionService.delete(this.removeSubscription));

        // Make sure no subscription notifications were sent
        verify(this.secomSubscriptionNotificationService, never()).sendNotification(any(), any(), any());
    }

    /**
     * Test that for a given subscription and a list of received Aids to
     * Navigation messages, this function will send the appropriate message to
     * the SECOM client discovered through the SECOM service.
     */
    @Test
    void testSendToSubscription() throws IOException {
        // Mock a SECOM client
        final SecomClient secomClient = mock(SecomClient.class);
        doReturn(secomClient).when(this.secomService).getClient(this.existingSubscriptionRequest.getClientMrn());

        // Perform the service call
        this.secomSubscriptionService.sendToSubscription(this.existingSubscriptionRequest, this.s125Dataset);

        // Verify that we upload the constructed SECOM upload object
        ArgumentCaptor<UploadObject> uploadArgument = ArgumentCaptor.forClass(UploadObject.class);
        verify(secomClient).upload(uploadArgument.capture());

        // Verify that the constructed object seems valid
        assertNotNull(uploadArgument.getValue());
        assertNotNull(uploadArgument.getValue().getEnvelope());
        assertTrue(uploadArgument.getValue().getEnvelope().getData().length > 0);
        assertEquals(SECOM_DataProductType.S125, uploadArgument.getValue().getEnvelope().getDataProductType());
        assertEquals(Boolean.TRUE, uploadArgument.getValue().getEnvelope().getFromSubscription());
        assertEquals(AckRequestEnum.DELIVERED_ACK_REQUESTED, uploadArgument.getValue().getEnvelope().getAckRequest());
        assertNotNull(uploadArgument.getValue().getEnvelope().getTransactionIdentifier());
    }

    /**
     * Test that the helper function that generates simple remove subscription
     * requests, will do so in an orderly fashion and include the provided
     * subscription identifier UUID.
     */
    @Test
    void testConstructRemoveSubscription() {
        // Perform the service call
        RemoveSubscription result = this.secomSubscriptionService.constructRemoveSubscription(this.existingSubscriptionRequest);

        // Make sure the remove subscription object seems OK
        assertNotNull(result);
        assertEquals(this.existingSubscriptionRequest.getUuid(), result.getSubscriptionIdentifier());
    }
}