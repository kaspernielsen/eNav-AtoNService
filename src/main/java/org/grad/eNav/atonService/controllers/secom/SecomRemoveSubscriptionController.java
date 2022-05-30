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
import org.grad.eNav.atonService.models.domain.secom.RemoveSubscription;
import org.grad.eNav.atonService.services.SecomService;
import org.grad.secom.exceptions.SecomNotFoundException;
import org.grad.secom.interfaces.RemoveSubscriptionInterface;
import org.grad.secom.models.RemoveSubscriptionObject;
import org.grad.secom.models.RemoveSubscriptionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/secom")
@Validated
@Slf4j
public class SecomRemoveSubscriptionController implements RemoveSubscriptionInterface {

    /**
     * The SECOM Service.
     */
    @Autowired
    SecomService secomService;

    /**
     * Object Mapper from SECOM Remove Subscription DTO to Domain.
     */
    @Autowired
    DomainDtoMapper<RemoveSubscriptionObject, RemoveSubscription> removeSubscriptionDomainMapper;

    /**
     * DELETE /api/secom/v1/subscription : Subscription(s) can be removed either
     * internally by information owner, or externally by the consumer. This
     * interface shall be used by the consumer to request removal of
     * subscription.
     *
     * @param removeSubscriptionObject the remove subscription object
     * @return the remove subscription response object
     */
    @Override
    @Tag(name = "SECOM")
    @DeleteMapping(value = REMOVE_SUBSCRIPTION_INTERFACE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RemoveSubscriptionResponseObject> removeSubscription(@Valid @RequestBody RemoveSubscriptionObject removeSubscriptionObject) {
        final UUID subscriptionIdentifier = Optional.ofNullable(removeSubscriptionObject)
                .map(dto -> this.removeSubscriptionDomainMapper.convertTo(dto, RemoveSubscription.class))
                .map(this.secomService::deleteSubscription)
                .orElseThrow(() -> new SecomNotFoundException(removeSubscriptionObject.getSubscriptionIdentifier().toString()));

        // Create the response
        final RemoveSubscriptionResponseObject removeSubscriptionResponse = new RemoveSubscriptionResponseObject();
        removeSubscriptionResponse.setResponseText(String.format("Subscription %s removed", subscriptionIdentifier));

        // Return the response
        return ResponseEntity.ok()
                .body(removeSubscriptionResponse);
    }

}
