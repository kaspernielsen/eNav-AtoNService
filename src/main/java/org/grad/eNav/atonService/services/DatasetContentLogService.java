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

package org.grad.eNav.atonService.services;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.DatasetContentLog;
import org.grad.eNav.atonService.models.enums.DatasetType;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.repos.DatasetContentLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;

/**
 * The S-125 Dataset Content Service.
 * <p/>
 * Service Implementation for managing the Dataset Content objects.
 * <p/>
 * Note that there is no deletion functionality in this service since we want
 * to be able to store all the entries for auditing purposes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class DatasetContentLogService {

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Dataset Content Repo.
     */
    @Autowired
    DatasetContentLogRepo datasetContentLogRepo;

    /**
     * Find the latest dataset content by UUID.
     *
     * @param uuid the UUID of the dataset
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findLatest(@NotNull UUID uuid) {
        return this.findLatest(uuid, LocalDateTime.now());
    }

    /**
     * Find the latest by UUID before the provided reference local date-time.
     *
     * @param uuid              the UUID of the dataset
     * @param localDateTime     the reference local date-time
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findLatest(@NotNull UUID uuid, LocalDateTime localDateTime) {
        return this.datasetContentLogRepo.findLatestForUuid(
                        uuid,
                        Optional.ofNullable(localDateTime).orElseGet(LocalDateTime::now),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseGet(() ->
                    // Handling cases where the content is not yet generated
                    // We need to first generate and store this manually
                    this.save(this.generateDatasetContentLog(this.datasetService.findOne(uuid), "CREATED"))
                );
    }

    /**
     * A simple saving operation that persists the models in the database using
     * the correct repository based on the instance type.
     *
     * @param datasetContentLog the Dataset entity to be saved
     * @return the saved Dataset entity
     */
    @Transactional
    public DatasetContentLog save(@NotNull DatasetContentLog datasetContentLog) {
        log.debug("Request to save Dataset Content Log: {}", datasetContentLog);

        // Save and return the dataset
        return this.datasetContentLogRepo.save(datasetContentLog);
    }

    /**
     * Provided a valid dataset this function will build the respective
     * dataset content and populate it with all entries that match its
     * geographical boundaries. The resulting object will then be marshalled
     * into an XML string and returned.
     *
     * @param s125DataSet the UUID of the dataset
     * @return
     */
    public DatasetContentLog generateDatasetContentLog(@NotNull S125Dataset s125DataSet, @NotNull String operation) {
        log.debug("Request to retrieve the content for Dataset with UUID : {}", s125DataSet.getUuid());

        // If everything is OK up to now start building the dataset content
        final DatasetContentLog datasetContentLog = new DatasetContentLog();
        datasetContentLog.setDatasetType(DatasetType.S125);
        datasetContentLog.setUuid(s125DataSet.getUuid());
        datasetContentLog.setGeometry(s125DataSet.getGeometry());
        datasetContentLog.setOperation(Optional.of(operation)
                .filter(not(String::isBlank))
                .orElseGet(() ->
                        Objects.equals(s125DataSet.getCreatedAt(), s125DataSet.getLastUpdatedAt()) ?
                        "CREATED" : "UPDATED"));
        datasetContentLog.setGeneratedAt(Optional.of(s125DataSet)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getGeneratedAt)
                .orElse(LocalDateTime.now()));
        datasetContentLog.setContent(Optional.of(s125DataSet)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getContent)
                .orElse(null));

        // And return the dataset content
        return datasetContentLog;
    }

}
