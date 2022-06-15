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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.services.SecomService;
import org.grad.secom.exceptions.SecomNotFoundException;
import org.grad.secom.interfaces.jaxrs.SubscriptionSecomInterface;
import org.grad.secom.models.SubscriptionRequestObject;
import org.grad.secom.models.SubscriptionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The SECOM Subscription Interface Controller.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Path("")
@Validated
@Slf4j
public class SubscriptionSecomController implements SubscriptionSecomInterface {

    /**
     * Object Mapper from SECOM Subscription Request DTO to Domain.
     */
    @Autowired
    DomainDtoMapper<SubscriptionRequestObject, SubscriptionRequest> subscriptionRequestDomainMapper;

    /**
     * The SECOM Service.
     */
    @Autowired
    SecomService secomService;

    /**
     * POST /api/secom/v1/subscription : Request subscription on information,
     * either specific information according to parameters, or the information
     * accessible upon decision by the information provider.
     *
     * @param subscriptionRequestObject the subscription request object
     * @return the subscription response object
     */
    @Tag(name = "SECOM")
    @Transactional
    public SubscriptionResponseObject subscription(@Valid SubscriptionRequestObject subscriptionRequestObject) {
        final SubscriptionRequest subscriptionRequest = Optional.ofNullable(subscriptionRequestObject)
                .map(dto -> this.subscriptionRequestDomainMapper.convertTo(dto, SubscriptionRequest.class))
                .map(this.secomService::saveSubscription)
                .filter(req -> Objects.nonNull(req.getUuid()))
                .orElseThrow(() -> new SecomNotFoundException("UUID"));

        // Create the response
        final SubscriptionResponseObject subscriptionResponse = new SubscriptionResponseObject();
        subscriptionResponse.setSubscriptionIdentifier(subscriptionRequest.getUuid());
        subscriptionResponse.setResponseText("Subscription successfully created");

        // Return the response
        return subscriptionResponse;
    }

}
