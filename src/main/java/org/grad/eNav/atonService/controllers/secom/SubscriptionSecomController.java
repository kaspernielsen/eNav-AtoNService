/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.controllers.secom;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.services.secom.SecomSubscriptionService;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.interfaces.SubscriptionSecomInterface;
import org.grad.secom.core.models.SubscriptionRequestObject;
import org.grad.secom.core.models.SubscriptionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

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
    SecomSubscriptionService secomSubscriptionService;

    /**
     * The Request Context.
     */
    @Autowired
    @Lazy
    Optional<HttpServletRequest> httpServletRequest;

    /**
     * POST /api/secom/v1/subscription : Request subscription on information,
     * either specific information according to parameters, or the information
     * accessible upon decision by the information provider.
     *
     * @param subscriptionRequestObject the subscription request object
     * @return the subscription response object
     */
    @Tag(name = "SECOM")
    public SubscriptionResponseObject subscription(@Valid SubscriptionRequestObject subscriptionRequestObject) {
        // Try to access the request header if possible...
        // These should have been forwarded by the API Gateway normally and
        // contain the SSL client certificate and extra information.
        final String mrn = this.httpServletRequest
                .map(req -> req.getHeader(SecomRequestHeaders.MRN_HEADER))
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new SecomValidationException("Cannot raise new subscription requests without a provided client MRN"));
        final SubscriptionRequest subscriptionRequest = Optional.ofNullable(subscriptionRequestObject)
                .map(dto -> this.subscriptionRequestDomainMapper.convertTo(dto, SubscriptionRequest.class))
                .map(subReq -> this.secomSubscriptionService.save(mrn, subReq))
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
