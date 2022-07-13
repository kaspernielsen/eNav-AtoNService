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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.grad.eNav.atonService.config.GlobalConfig;
import org.grad.eNav.atonService.models.domain.Pair;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.EnvelopeUploadObject;
import org.grad.secom.core.models.SECOM_ExchangeMetadata;
import org.grad.secom.core.models.UploadObject;
import org.grad.secom.core.models.enums.AckRequestEnum;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.grad.secom.core.models.enums.SubscriptionEventEnum;
import org.grad.secom.springboot.components.SecomClient;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.search.sort.dsl.LuceneSearchSortFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * The SECOM Subscription Service Class.
 * <p/>
 * A service to handle the incoming SECOM subscription requests. Each
 * subscription is persisted in the database and is handled appropriately
 * as specified by the SECOM standard.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class SecomSubscriptionService implements MessageHandler {

    /**
     * The Entity Manager Factory.
     */
    @Autowired
    EntityManagerFactory entityManagerFactory;

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Request Context.
     */
    @Autowired
    @Lazy
    Optional<HttpServletRequest> httpServletRequest;

    /**
     * The S-125 Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The UN/LoCode Service.
     */
    @Autowired
    UnLoCodeService unLoCodeService;

    /**
     * The SECOM Service.
     */
    @Autowired
    SecomService secomService;

    /**
     * The SECOM Subscription Notification Service.
     */
    @Autowired
    SecomSubscriptionNotificationService secomSubscriptionNotificationService;

    /**
     * The SECOM Subscription Repo.
     */
    @Autowired
    SecomSubscriptionRepo secomSubscriptionRepo;

    /**
     * The S-125 Publish Channel to listen for the publications to.
     */
    @Autowired
    @Qualifier("s125PublicationChannel")
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Publish Channel to listen for the deletion to.
     */
    @Autowired
    @Qualifier("s125DeletionChannel")
    PublishSubscribeChannel s125DeletionChannel;

    // Class Variables
    EntityManager entityManager;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the S-125 publication channel.
     */
    @PostConstruct
    public void init() {
        log.info("SECOM Subscription Service is booting up...");
        this.entityManager = this.entityManagerFactory.createEntityManager();
        this.s125PublicationChannel.subscribe(this);
        this.s125DeletionChannel.subscribe(this);
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("SECOM Subscription Service is shutting down...");
        if(this.entityManager != null) {
            this.entityManager.close();
        }
        if (this.s125PublicationChannel != null) {
            this.s125PublicationChannel.destroy();
        }
        if (this.s125DeletionChannel != null) {
            this.s125DeletionChannel.destroy();
        }
    }

    /**
     * This is a simple handler for the incoming messages. This is a generic
     * handler for any type of Spring Integration messages, but it should really
     * only be used for the ones containing S-125 message payloads.
     *
     * @param message               The message to be handled
     * @throws MessagingException   The Messaging exceptions that might occur
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        // Get the headers of the incoming message
        SECOM_DataProductType contentType = message.getHeaders().get(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.class);
        Boolean deletion = message.getHeaders().get("deletion", Boolean.class);

        // Only listen to S-125 data product messages
        if(contentType != SECOM_DataProductType.S125) {
            return;
        }

        // Handle only messages that seem valid
        if(message.getPayload() instanceof AidsToNavigation) {
            // Get the payload of the incoming message
            AidsToNavigation aidsToNavigation = (AidsToNavigation) message.getPayload();

            // A simple debug message
            log.debug(String.format("SECOM Service received AtoN %s with AtoN number: %s.",
                    deletion ? "deletion" : "publication",
                    aidsToNavigation.getAtonNumber()));

            // Handle based on whether this is a deletion or not
            if(!deletion) {
                // Get the matching subscriptions and inform them
                this.findAll(aidsToNavigation.getGeometry(),
                                Optional.of(aidsToNavigation).map(AidsToNavigation::getDateStart).map(ld -> ld.atStartOfDay()).orElse(null),
                                Optional.of(aidsToNavigation).map(AidsToNavigation::getDateEnd).map(ld -> ld.atTime(LocalTime.MAX)).orElse(null))
                        .stream()
                        .forEach(subscription -> this.sendToSubscription(subscription, Collections.singletonList(aidsToNavigation)));

            }
        }
        else {
            log.warn("Aids to Navigation Service received a publish-subscribe message with erroneous format.");
        }
    }

    /**
     * Get all the Subscription Requests in a search list.
     *
     * @param geometry the query geometry
     * @param fromTime the query from time
     * @param toTime the query to time
     * @return the list of Subscription Requests
     */
    @Transactional(readOnly = true)
    public List<SubscriptionRequest> findAll(Geometry geometry,
                                             LocalDateTime fromTime,
                                             LocalDateTime toTime) {
        log.debug("Request to get Subscription Requests in a search");
        // Create the search query - always sort by name
        SearchQuery<SubscriptionRequest> searchQuery = this.getSubscriptionRequestSearchQuery(
                geometry,
                fromTime,
                toTime,
                new Sort(new SortedSetSortField("uuid", false))
        );

        // Map the results to a paged response
        return searchQuery.fetchAll().hits();
    }

    /**
     * Creates a new SECOM subscription and persists its information in the
     * database.
     *
     * @param subscriptionRequest the subscription request
     * @return the subscription request generated
     */
    public SubscriptionRequest save(SubscriptionRequest subscriptionRequest) {
        log.debug("Request to save SECOM subscription : {}", subscriptionRequest);

        // Get the subscription request MRN
        final String mrn = this.httpServletRequest
                .map(req -> req.getHeader("MRN"))
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new SecomValidationException("Cannot raise new subscription requests without a provided client MRN"));

        // See if a duplicate subscription request already exists
        this.secomSubscriptionRepo.findByClientMrn(mrn)
                .map(this::constructRemoveSubscription)
                .ifPresent(this::delete);

        // Populate the subscription dataset and geometry
        subscriptionRequest.setS125DataSet(Optional.of(subscriptionRequest)
                .map(SubscriptionRequest::getDataReference)
                .map(this.datasetService::findOne)
                .orElse(null));
        subscriptionRequest.setClientMrn(this.httpServletRequest.map(req -> req.getHeader("MRN")).orElse(null));
        subscriptionRequest.updateSubscriptionGeometry(this.unLoCodeService);

        // Now save the request
        final SubscriptionRequest savedSubscriptionRequest = this.secomSubscriptionRepo.save(subscriptionRequest);

        // Inform to the subscription client (identify through MRN) - asynchronous
        this.secomSubscriptionNotificationService.sendNotification(subscriptionRequest.getClientMrn(),
                savedSubscriptionRequest.getUuid(),
                SubscriptionEventEnum.SUBSCRIPTION_CREATED);

        // Now save for each type
        return savedSubscriptionRequest;
    }

    /**
     * Removes and existing SECOM subscription from the persisted entries in
     * the database if found and return an output message.
     *
     * @param removeSubscription the remove subscription
     * @return the subscription identifier UUID removed
     */
    public UUID delete(RemoveSubscription removeSubscription) {
        log.debug("Request to delete SECOM subscription : {}", removeSubscription);

        // Look for the subscription and delete it if found
        final SubscriptionRequest subscriptionRequest = Optional.of(removeSubscription)
                .map(RemoveSubscription::getSubscriptionIdentifier)
                .flatMap(this.secomSubscriptionRepo::findById)
                .orElseThrow(() -> new SecomNotFoundException(removeSubscription.getSubscriptionIdentifier().toString()));

        // Delete the subscription
        this.secomSubscriptionRepo.delete(subscriptionRequest);

        // Inform to the subscription client (identify through MRN) - asynchronous
        this.secomSubscriptionNotificationService.sendNotification(subscriptionRequest.getClientMrn(),
                subscriptionRequest.getUuid(),
                SubscriptionEventEnum.SUBSCRIPTION_REMOVED);

        // If all OK, then return the subscription UUID
        return removeSubscription.getSubscriptionIdentifier();
    }

    /**
     * This function handles the operation of sending the updated list of
     * the received S-125 Aids to Navigation entries to the provided
     * subscription. This involved contacting the SECOM service registry in
     * order to find our the correct endpoint and then the SECOM upload
     * interface of the discovered client (if a valid registration is returned)
     * will be utilised.
     *
     * @param subscriptionRequest the subscription request
     * @param aidsToNavigationList the list of Aids to Navigation to be pushed
     */
    protected void sendToSubscription(SubscriptionRequest subscriptionRequest, List<AidsToNavigation> aidsToNavigationList) {
        // Make sure we also have an MRN for the subscribed client
        if(Objects.isNull(subscriptionRequest.getClientMrn())) {
            log.warn("Subscription request found for S-125 dataset updates but no client MRN");
            return;
        }

        // Identify the subscription client if possible through the client MRN
        final SecomClient secomClient = this.secomService.getClient(subscriptionRequest.getClientMrn());

        // Build the upload object
        UploadObject uploadObject = new UploadObject();
        EnvelopeUploadObject envelopeUploadObject = new EnvelopeUploadObject();
        Pair<String, SECOM_ExchangeMetadata> signedData = this.secomService.signPayload(GlobalConfig.convertTos125DataSet(this.modelMapper, aidsToNavigationList));
        envelopeUploadObject.setData(signedData.getKey());
        envelopeUploadObject.setExchangeMetadata(signedData.getValue());
        envelopeUploadObject.setContainerType(ContainerTypeEnum.S100_DataSet);
        envelopeUploadObject.setDataProductType(SECOM_DataProductType.S125);
        envelopeUploadObject.setFromSubscription(true);
        envelopeUploadObject.setAckRequest(AckRequestEnum.NO_ACK_REQUESTED);
        envelopeUploadObject.setTransactionIdentifier(UUID.randomUUID());
        uploadObject.setEnvelope(envelopeUploadObject);
        uploadObject.setEnvelopeSignature("To be implemented");

        // Now upload the message to the subscription client
        secomClient.upload(uploadObject);
    }

    /**
     * A helper function that quickly constructs a RemoveSubscription request
     * based on the provided subscription Identifier.
     *
     * @param subscriptionRequest the subscription request
     * @return the remove subscription request
     */
    protected RemoveSubscription constructRemoveSubscription(SubscriptionRequest subscriptionRequest) {
        RemoveSubscription removeSubscription = new RemoveSubscription();
        removeSubscription.setSubscriptionIdentifier(subscriptionRequest.getUuid());
        return removeSubscription;
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * AtoN UID and geometry. This query will be based solely on the aton
     * messages table and will include the following fields:
     * -
     * For any more elaborate search, the getSearchMessageQueryByText function
     * can be used.
     *
     * @param geometry the geometry that the results should intersect with
     * @param fromTime the date-time the results should match from
     * @param toTime the date-time the results should match to
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<SubscriptionRequest> getSubscriptionRequestSearchQuery(Geometry geometry,
                                                                                 LocalDateTime fromTime,
                                                                                 LocalDateTime toTime,
                                                                                 Sort sort) {
        // Then build and return the hibernate-search query
        SearchSession searchSession = Search.session( this.entityManager );
        SearchScope<SubscriptionRequest> scope = searchSession.scope( SubscriptionRequest.class );
        return searchSession.search( scope )
                .where( f -> f.bool(b -> {
                            b.must(f.matchAll());
                            Optional.ofNullable(geometry).ifPresent(g-> b.must(f.extension(LuceneExtension.get())
                                    .fromLuceneQuery(createGeoSpatialQuery(g))));
                            Optional.ofNullable(fromTime).ifPresent(v -> b.must(f.range()
                                    .field("subscriptionPeriodEnd")
                                    .atLeast(fromTime)));
                            Optional.ofNullable(toTime).ifPresent(v -> b.must(f.range()
                                    .field("subscriptionPeriodStart")
                                    .atMost(toTime)));
                        })
                )
                .sort(f -> ((LuceneSearchSortFactory)f).fromLuceneSort(sort))
                .toQuery();
    }

    /**
     * Creates a Lucene geo-spatial query based on the provided geometry. The
     * query isa recursive one based on the maxLevels defined (in this case 12,
     * which result in a sub-meter precision).
     *
     * @param geometry      The geometry to generate the spatial query for
     * @return The Lucene geo-spatial query constructed
     */
    protected Query createGeoSpatialQuery(Geometry geometry) {
        // Initialise the spatial strategy
        JtsSpatialContext ctx = JtsSpatialContext.GEO;
        int maxLevels = 12; //results in sub-meter precision for geo-hash
        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);
        RecursivePrefixTreeStrategy strategy = new RecursivePrefixTreeStrategy(grid,"subscriptionGeometry");

        // Create the Lucene GeoSpatial Query
        return Optional.ofNullable(geometry)
                .map(g -> new SpatialArgs(SpatialOperation.Intersects, new JtsGeometry(g, ctx, false , true)))
                .map(strategy::makeQuery)
                .orElse(null);
    }

}
