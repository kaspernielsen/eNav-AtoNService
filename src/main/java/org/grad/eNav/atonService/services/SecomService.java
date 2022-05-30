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
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.repos.SecomSubscriptionRepo;
import org.grad.secom.exceptions.SecomNotFoundException;
import org.grad.secom.models.SECOM_ExchangeMetadata;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
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
public class SecomService {

    /**
     * The SECOM Subscription Repo.
     */
    @Autowired
    SecomSubscriptionRepo secomSubscriptionRepo;

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
            throw new ValidationException("Cannot create a SECOM subscription if the UUID is already provided!");
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
     * @param a the first geometry to be joined
     * @param b the second geometry to be joined
     * @return the joined geometry
     */
    public Geometry joinGeometries(Geometry a, Geometry b) {
        if(a == null && b == null) {
            return null;
        } else if(a == null || b == null) {
            return Optional.ofNullable(a).orElse(b);
        } else {
            return a.intersection(b);
        }
    }

}
