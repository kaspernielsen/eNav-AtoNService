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
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * The SECOM Service Class.
 *
 * A service to handle the incoming SECOM requests that need additional
 * processing, not covered by the existing services, e.g subscriptions.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class SecomService implements MessageHandler {

    /**
     * The Service Registry URL.
     */
    @Value("${gla.rad.aton-service.service-registry.url:}" )
    String discoveryServiceUrl;

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;

    /**
     * The Asynchronous Task Executor.
     */
    @Autowired
    TaskExecutor taskExecutor;

    /**
     * The Request Context.
     */
    @Autowired
    @Lazy
    Optional<HttpServletRequest> httpServletRequest;

    /**
     * The Service Registry.
     */
    @Autowired
    @Lazy
    Optional<WebClient> serviceRegistry;

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
                        return new SecomClient(url, true);
                    } catch (SSLException ex) {
                        this.log.error("Unable to initialise the SSL context for the SECOM discovery service...", ex);
                        return null;
                    }
                })
                .orElse(null);
        this.s125PublicationChannel.subscribe(this);
        this.s125DeletionChannel.subscribe(this);
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("SECOM Service is shutting down...");
        this.discoveryService = null;
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
    @Transactional
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
            log.debug(String.format("S-125 Web Socket Service received AtoN %s with AtoN number: %s.",
                    deletion ? "deletion" : "publication",
                    aidsToNavigation.getAtonNumber()));

            // Handle based on whether this is a deletion or not
            if(!deletion) {
                // Get the matching subscriptions and inform them
                this.findAllSubscriptions(aidsToNavigation.getGeometry(),
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
    public List<SubscriptionRequest> findAllSubscriptions(Geometry geometry,
                                                          LocalDateTime fromTime,
                                                          LocalDateTime toTime) {
        log.debug("Request to get Subscription Requests in a search");
        // Create the search query - always sort by name
        SearchQuery<SubscriptionRequest> searchQuery = this.geSubscriptionRequestSearchQuery(
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
    public SubscriptionRequest saveSubscription(SubscriptionRequest subscriptionRequest) {
        log.debug("Request to save SECOM subscription : {}", subscriptionRequest);

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
        this.taskExecutor.execute(() -> {
            final SecomClient secomClient = this.getSecomClient(savedSubscriptionRequest.getClientMrn());
            SubscriptionNotificationObject subscriptionNotificationObject = new SubscriptionNotificationObject();
            subscriptionNotificationObject.setSubscriptionIdentifier(savedSubscriptionRequest.getUuid());
            subscriptionNotificationObject.setEventEnum(SubscriptionEventEnum.SUBSCRIPTION_CREATED);
            secomClient.subscriptionNotification(subscriptionNotificationObject);
        });

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
    public UUID deleteSubscription(RemoveSubscription removeSubscription) {
        log.debug("Request to delete SECOM subscription : {}", removeSubscription);

        // Look for the subscription and delete it if found
        final SubscriptionRequest subscriptionRequest = Optional.of(removeSubscription)
                .map(RemoveSubscription::getSubscriptionIdentifier)
                .flatMap(this.secomSubscriptionRepo::findById)
                .orElseThrow(() -> new SecomNotFoundException(removeSubscription.getSubscriptionIdentifier().toString()));

        // Delete the subscription
        this.secomSubscriptionRepo.delete(subscriptionRequest);

        // Inform to the subscription client (identify through MRN) - asynchronous
        this.taskExecutor.execute(() -> {
            final SecomClient secomClient = this.getSecomClient(subscriptionRequest.getClientMrn());
            SubscriptionNotificationObject subscriptionNotificationObject = new SubscriptionNotificationObject();
            subscriptionNotificationObject.setSubscriptionIdentifier(removeSubscription.getSubscriptionIdentifier());
            subscriptionNotificationObject.setEventEnum(SubscriptionEventEnum.SUBSCRIPTION_REMOVED);
            secomClient.subscriptionNotification(subscriptionNotificationObject);
        });

        // If all OK, then return the subscription UUID
        return removeSubscription.getSubscriptionIdentifier();
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
        // First make sure the service registry is available
        if(this.serviceRegistry.isEmpty()) {
            log.warn("Subscription request found for S-125 dataset updates but no connection to service registry");
            return;
        }

        // Make sure we also have an MRN for the subscribed client
        if(Objects.isNull(subscriptionRequest.getClientMrn())) {
            log.warn("Subscription request found for S-125 dataset updates but no client MRN");
            return;
        }

        // Identify the subscription client if possible through the client MRN
        final SecomClient secomClient = this.getSecomClient(subscriptionRequest.getClientMrn());

        // Build the upload object
        UploadObject uploadObject = new UploadObject();
        EnvelopeUploadObject envelopeUploadObject = new EnvelopeUploadObject();
        Pair<String, SECOM_ExchangeMetadata> signedData = signPayload(GlobalConfig.convertTos125DataSet(this.modelMapper, aidsToNavigationList));
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
     * Based on an MRN provided, this function will contact the SECOM discovery
     * service (in this case it's the MCP MSR) and request the client endpoint
     * URI. It will then construct a SECOM client to be returned for the URI
     * discovered.
     *
     * @param mrn the MRN to be lookup up
     * @return the SECOM client for the endpoint matching the provided URI
     */
    protected SecomClient getSecomClient(String mrn) {
        // Validate the MRN
        Optional.ofNullable(mrn)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new SecomValidationException("Cannot request a service discovery for an empty/invalid MRN"));

        // Create the discovery service search filter object for the provided MRN
        final SearchFilterObject searchFilterObject = new SearchFilterObject();
        searchFilterObject.setQuery(String.format("instanceId:\"%s\"", mrn));

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
            return new SecomClient(new URL(instance.getEndpointUri()), false);
        } catch (SSLException | MalformedURLException ex) {
            this.log.error(ex.getMessage(), ex);
            throw new SecomValidationException(ex.getMessage());
        }
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
    protected SearchQuery<SubscriptionRequest> geSubscriptionRequestSearchQuery(Geometry geometry,
                                                                                LocalDateTime fromTime,
                                                                                LocalDateTime toTime,
                                                                                Sort sort) {
        // Then build and return the hibernate-search query
        SearchSession searchSession = Search.session( this.entityManager );
        SearchScope<SubscriptionRequest> scope = searchSession.scope( SubscriptionRequest.class );
        return searchSession.search( scope )
                .where( f -> f.bool(b -> {
                            b.must(f.matchAll());
                            Optional.ofNullable(fromTime).ifPresent(v -> b.must(f.range()
                                    .field("subscriptionPeriodStart")
                                    .atMost(toTime)));
                            Optional.ofNullable(toTime).ifPresent(v -> b.must(f.range()
                                    .field("subscriptionPeriodEnd")
                                    .atLeast(fromTime)));
                            Optional.ofNullable(geometry).ifPresent(g-> b.must(f.extension(LuceneExtension.get())
                                    .fromLuceneQuery(createGeoSpatialQuery(g))));
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