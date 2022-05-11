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
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.repos.AidsToNavigationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The Aids to Navigation Service.
 *
 * Service Implementation for managing the S-125 Aids to Navigation objects.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class AidsToNavigationService {

    /**
     * The Generic Aids to Navigation Repo.
     */
    @Autowired
    AidsToNavigationRepo aidsToNavigationRepo;

    /**
     * A simple saving operation that persists the models in the database using
     * the correct repository based on the instance type.
     *
     * @param aidsToNavigation the Aids to Navigation entity to be saved
     * @return the saved Aids to Navigation entity
     */
    @Transactional
    public AidsToNavigation save(AidsToNavigation aidsToNavigation) {
        log.debug("Request to save Aids to Navigation : {}", aidsToNavigation);

        // Update the entity ID if the Code ID was found
        this.aidsToNavigationRepo.findByAtonNumber(aidsToNavigation.getAtonNumber())
                .ifPresent(aton -> aidsToNavigation.setId(aton.getId()));

        // Now save for each type
        return this.aidsToNavigationRepo.save(aidsToNavigation);
    }

    /**
     * Delete the Aids to Navigation by ID.
     *
     * @param id the ID of the Aids to Navigation
     */
    @Transactional
    public void delete(BigInteger id) {
        log.debug("Request to delete Aids to Navigation with ID : {}", id);

        // Make sure the station node exists
        final AidsToNavigation aidsToNavigation = this.aidsToNavigationRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(String.format("No station node found for the provided ID: %d", id)));

        // Now delete the station node
        this.aidsToNavigationRepo.delete(aidsToNavigation);
    }

    /**
     * Delete the Aids to Navigation by its AtoN number.
     *
     * @param atonNumber the AtoN number of the Aids to Navigation
     */
    public void deleteByAtonNumber(String atonNumber) throws DataNotFoundException {
        log.debug("Request to delete ids to Navigation with AtoN number : {}", atonNumber);
        BigInteger id = this.aidsToNavigationRepo.findByAtonNumber(atonNumber)
                .map(AidsToNavigation::getId)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No Aids to Navigation found for the provided AtoN number: %s", atonNumber))
                );
        this.delete(id);
    }

}
