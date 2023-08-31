/*
 * Copyright (c) 2023 GLA Research and Development Directorate
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

import _int.iala_aism.s125.gml._0_0.Dataset;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.aspects.LogDataset;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.exceptions.SavingFailedException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.repos.DatasetContentRepo;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The S-125 Dataset Content Service.
 *
 * Service Implementation for managing the S-125 Dataset Content objects.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class DatasetContentService {

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;

    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    /**
     * The Dataset Content Repo.
     */
    @Autowired
    DatasetContentRepo datasetContentRepo;

    /**
     * The saving operation that persists the dataset content in the database
     * using the respective repository.
     *updated
     * @param datasetContent the dataset content entity to be saved
     * @return the saved dataset content entity
     */
    @Transactional
    public DatasetContent save(@NotNull DatasetContent datasetContent) {
        log.debug("Request to save Dataset Content : {}", datasetContent);

        // Sanity Check
        Optional.of(datasetContent)
                .map(DatasetContent::getDataset)
                .map(S125Dataset::getUuid)
                .orElseThrow(() -> new SavingFailedException("Cannot save a dataset content entity without it being linked to an actual dataset"));

        // Save the new/updated dataset content
        final DatasetContent savedDatasetContent = this.datasetContentRepo.save(datasetContent);

        // Refresh the savedDatasetContent object to fetch the updated values
        this.entityManager.flush();
        this.entityManager.refresh(savedDatasetContent);

        // Return the updated dataset content
        return savedDatasetContent;
    }

    /**
     * Provided a valid dataset this function will build the respective
     * dataset content and populate it with all entries that match its
     * geographical boundaries. The resulting object will then be marshalled
     * into an XML string and returned.
     *
     * @param s125Dataset the dataset to generate the content for
     * @return the dataset with the newly generated dataset content object
     */
    @LogDataset
    @Async
    @Transactional
    public CompletableFuture<S125Dataset> generateDatasetContent(@NotNull S125Dataset s125Dataset) {
        log.debug("Request to generate the content for Dataset with UUID: {}", s125Dataset.getUuid());

        // Get all the matching Aids to Navigation - if we have a geometry at least
        final List<AidsToNavigation> atonList = Optional.of(s125Dataset)
                .map(S125Dataset::getGeometry)
                .map(geometry ->
                        this.aidsToNavigationService.findAll(null, geometry, null, null, Pageable.unpaged())
                )
                .orElseGet(Page::empty)
                .getContent();

        // Filter the new/updated Aids to Navigation entries
        final List<AidsToNavigation> deltaAtonList = atonList.stream()
                .filter(aton -> Objects.nonNull(aton.getLastModifiedAt()))
                .filter(aton -> aton.getLastModifiedAt().isAfter(s125Dataset.getLastUpdatedAt()))
                .toList();

        // Now try to marshal the dataset into an XML string and update the content/delta
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
        try {
            // Build the dataset contents, if any
            final Dataset dataset = s125DatasetBuilder.packageToDataset(s125Dataset, atonList);
            final Dataset delta = s125DatasetBuilder.packageToDataset(s125Dataset, deltaAtonList);

            // Marshall the contents into XML
            final String datasetXML = S125Utils.marshalS125(dataset, Boolean.FALSE);
            final String deltaXML = S125Utils.marshalS125(delta, Boolean.FALSE);

            // Populate the new content/delta
            final DatasetContent datasetContent = Optional.of(s125Dataset)
                    .map(S125Dataset::getDatasetContent)
                    .orElseThrow(() -> new DataNotFoundException(
                            String.format(
                                    "Cannot generate the dataset content if none define for dataset with UUID: %s",
                                    s125Dataset.getUuid()
                            )
                    ));

            // Increase the dataset sequence number
            datasetContent.increaseSequenceNo();

            // Populate the dataset content
            datasetContent.setDataset(s125Dataset);
            datasetContent.setContent(datasetXML);
            datasetContent.setContentLength(BigInteger.valueOf(datasetXML.length()));
            datasetContent.setDelta(deltaXML);
            datasetContent.setDeltaLength(BigInteger.valueOf(deltaXML.length()));

            // And finally perform the saving operation
            s125Dataset.setDatasetContent(this.save(datasetContent));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return CompletableFuture.failedFuture(ex);
        }

        // Now return the update dataset content
        return CompletableFuture.completedFuture(s125Dataset);
    }

}
