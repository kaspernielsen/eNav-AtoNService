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
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.SecomService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.interfaces.SubscriptionInterface;
import org.grad.secom.models.SubscriptionRequestObject;
import org.grad.secom.models.SubscriptionResponseObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/secom")
@Validated
@Slf4j
public class SecomSubscriptionController implements SubscriptionInterface {

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

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
     * POST /v1/subscription : Request subscription on information, either
     * specific information according to parameters, or the information
     * accessible upon decision by the information provider.
     *
     * @param subscriptionRequest the subscription object
     * @return the subscription response object
     */
    @Override
    @Tag(name = "SECOM")
    @PostMapping(value = SUBSCRIPTION_INTERFACE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionResponseObject> subscription(@RequestBody SubscriptionRequestObject subscriptionRequest) {
        final UUID subscriptionIdentifier = Optional.ofNullable(subscriptionRequest)
                .map(this.secomService::createSubscription)
                .orElse(null);

        // Create the response
        SubscriptionResponseObject subscriptionResponse = new SubscriptionResponseObject();
        subscriptionResponse.setSubscriptionIdentifier(subscriptionIdentifier);
        subscriptionResponse.setResponseText(Optional.ofNullable(subscriptionResponse)
                .map(id -> "Subscription successfully created")
                .orElse("Information not found"));

        // Return the response
        return ResponseEntity.ok()
                .body(subscriptionResponse);
    }

}
