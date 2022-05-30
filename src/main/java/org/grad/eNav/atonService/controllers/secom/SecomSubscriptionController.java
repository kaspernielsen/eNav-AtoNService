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
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.interfaces.SubscriptionInterface;
import org.grad.secom.models.SubscriptionRequestObject;
import org.grad.secom.models.SubscriptionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/secom")
@Validated
@Slf4j
public class SecomSubscriptionController implements SubscriptionInterface {

    /**
     * Object Mapper from SECOM Subscription Request DTO to Domain.
     */
    @Autowired
    DomainDtoMapper<SubscriptionRequestObject, SubscriptionRequest> subscriptionRequestDomainMapper;

    /**
     * The UN/LOCODE Service.
     */
    @Autowired
    UnLoCodeService unLoCodeService;

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
    @Override
    @Tag(name = "SECOM")
    @PostMapping(value = SUBSCRIPTION_INTERFACE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionResponseObject> subscription(@Valid @RequestBody SubscriptionRequestObject subscriptionRequestObject) {
        final SubscriptionRequest subscriptionRequest = Optional.ofNullable(subscriptionRequestObject)
                .map(dto -> this.subscriptionRequestDomainMapper.convertTo(dto, SubscriptionRequest.class))
                .map(this.secomService::createSubscription)
                .orElse(null);

        // Create the response
        SubscriptionResponseObject subscriptionResponse = new SubscriptionResponseObject();
        subscriptionResponse.setSubscriptionIdentifier(Optional.ofNullable(subscriptionRequest)
                .map(SubscriptionRequest::getUuid)
                .orElse(null));
        subscriptionResponse.setResponseText(Optional.ofNullable(subscriptionRequest)
                .map(SubscriptionRequest::getUuid)
                .map(uuid -> "Subscription successfully created")
                .orElse("Information not found"));

        // Return the response
        return ResponseEntity.ok()
                .body(subscriptionResponse);
    }

}
