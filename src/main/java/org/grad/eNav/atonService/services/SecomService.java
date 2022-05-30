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
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.secom.exceptions.SecomNotFoundException;
import org.grad.secom.exceptions.SecomValidationException;
import org.grad.secom.models.SECOM_ExchangeMetadata;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
public class SecomService implements MessageHandler  {

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

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the S-125 publication channel.
     */
    @PostConstruct
    public void init() {
        log.info("SECOM Service is booting up...");
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
        String contentType = Objects.toString(message.getHeaders().get(MessageHeaders.CONTENT_TYPE));
        Boolean deletion = Objects.equals(message.getHeaders().get("deletion"), "true");

        // Handle only messages that seem valid
        if(message.getPayload() instanceof AidsToNavigation) {
            // Get the payload of the incoming message
            AidsToNavigation aidsToNavigation = (AidsToNavigation) message.getPayload();

            // A simple debug message;
            log.debug(String.format("Received Aids to Navigation publication with AtoN number: %s.", aidsToNavigation.getAtonNumber()));
        }
        // String input usually come from the S-125 deletions
        else if(deletion && message.getPayload() instanceof String) {
            // Get the header and payload of the incoming message
            String payload = (String) message.getPayload();

            // A simple debug message;
            log.debug(String.format("Received Aids to Navigation deletion for AtoN Number: %s.", payload));
        }
        else {
            log.warn("Aids to Navigation Service received a publish-subscribe message with erroneous format.");
        }
    }

    /**
     * Creates a new SECOM subscription and persists its information in the
     * database.
     *
     * @param subscriptionRequest the subscription request
     * @return the subscription request generated
     */
    public SubscriptionRequest createSubscription(SubscriptionRequest subscriptionRequest) {
        log.debug("Request to save SECOM subscription : {}", subscriptionRequest);

        // Make sure we don't have a UUID to begin with
        if(Objects.nonNull(subscriptionRequest.getUuid())) {
            throw new SecomValidationException("Cannot create a SECOM subscription if the UUID is already provided!");
        }

        // Now save for each type
        return this.secomSubscriptionRepo.save(subscriptionRequest);
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
        Optional.of(removeSubscription)
                .map(RemoveSubscription::getSubscriptionIdentifier)
                .flatMap(this.secomSubscriptionRepo::findById)
                .ifPresentOrElse(
                        this.secomSubscriptionRepo::delete,
                        () -> {
                            throw new SecomNotFoundException(removeSubscription.getSubscriptionIdentifier().toString());
                        }
        );

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
    public SECOM_ExchangeMetadata signPayload(String payload) {
        final SECOM_ExchangeMetadata serviceExchangeMetadata = new SECOM_ExchangeMetadata();
        serviceExchangeMetadata.setDataProtection(false);
        return serviceExchangeMetadata;
    }

    /**
     * A helper function to simplify the joining of geometries without troubling
     * ourselves for the null checking... which is a pain.
     *
     * @param geometries the geometries variable argument
     * @return the resulting joined geometry
     */
    public Geometry joinGeometries(Geometry... geometries) {
        Geometry result = null;
        for(Geometry geometry : geometries) {
            if(result == null && geometry == null) {
                result = null;
            } else if(result == null || geometry == null) {
                result = Optional.ofNullable(result).orElse(geometry);
            } else {
                return result.intersection(geometry);
            }
        }
        return result;
    }

}
