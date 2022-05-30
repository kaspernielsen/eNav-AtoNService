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
import org.grad.secom.models.RemoveSubscriptionObject;
import org.grad.secom.models.SECOM_ExchangeMetadata;
import org.grad.secom.models.SubscriptionRequestObject;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

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
     * Creates a new SECOM subscription and persists its information in the
     * database.
     *
     * @param subscriptionRequestObject the subscription request object
     * @return the subscription identifier UUID generated
     */
    public UUID createSubscription(SubscriptionRequestObject subscriptionRequestObject) {
        return null;
    }

    /**
     * Removes and existing SECOM subscription from the persisted entries in
     * the database if found and return an output message.
     *
     * @param removeSubscriptionObject the remove subscription object
     * @return the subscription identifier UUID removed
     */
    public UUID deleteSubscription(RemoveSubscriptionObject removeSubscriptionObject) {
        return null;
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
