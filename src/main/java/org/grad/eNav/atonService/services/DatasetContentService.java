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

import _int.iala_aism.s125.gml._0_0.AidsToNavigationType;
import _int.iala_aism.s125.gml._0_0.Dataset;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.aspects.LogDataset;
import org.grad.eNav.atonService.exceptions.DeletedAtoNsInDatasetContentGenerationException;
import org.grad.eNav.atonService.exceptions.SavingFailedException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.repos.DatasetContentRepo;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.geotools.filter.function.StaticGeometry.not;

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
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    @Lazy
    @Autowired
    DatasetService datasetService;

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
        final S125Dataset s125Dataset = Optional.of(datasetContent)
                .map(DatasetContent::getDataset)
                .orElseThrow(() -> new SavingFailedException("Cannot save a " +
                        "dataset content entity without it being linked to an " +
                        "actual dataset"));

        // Make sure the content is assign to the dataset as well
        if(Objects.isNull(s125Dataset.getDatasetContent())) {
            s125Dataset.setDatasetContent(datasetContent);
        }

        // Return the new/updated dataset content
        return this.datasetContentRepo.saveAndFlush(datasetContent);
    }

    /**
     * Provided a valid dataset this function will build the respective
     * dataset content and populate it with all entries that match its
     * geographical boundaries. The resulting object will then be marshalled
     * into an XML string and returned.
     *
     * @param uuid the dataset of the dataset to generate the content for
     * @return the dataset with the newly generated dataset content object
     */
    @LogDataset
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<S125Dataset> generateDatasetContent(@NotNull UUID uuid) {
        log.debug("Request to generate the content for Dataset with UUID: {}", uuid);

        // Make sure we have a valid dataset content entry to populate
        final S125Dataset s125Dataset = this.datasetService.findOne(uuid);
        final DatasetContent datasetContent = Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .orElseGet(DatasetContent::new);

        // Get all the previously matching Aids to Navigation - if we have the old content
        final List<AidsToNavigationType> origAtonList = Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getContent)
                .map(xml -> {
                    try { return S125Utils.getDatasetMembers(xml); }
                    catch (JAXBException ex) { return null; }
                })
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(AidsToNavigationType.class::isInstance)
                .map(AidsToNavigationType.class::cast)
                .toList();
        final Set<String> origAtonNumbers = origAtonList.stream()
                .map(AidsToNavigationType::getAtonNumber)
                .collect(Collectors.toSet());

        // Get all the new matching Aids to Navigation - if we have a geometry
        final List<AidsToNavigation> atonList = Optional.of(s125Dataset)
                .map(S125Dataset::getGeometry)
                .map(geometry ->
                        this.aidsToNavigationService.findAll(null, geometry, null, null, Pageable.unpaged())
                )
                .orElseGet(Page::empty)
                .getContent();
        final Set<String> atonNumbers = atonList.stream()
                .map(AidsToNavigation::getAtonNumber)
                .collect(Collectors.toSet());

        // ================================================================== //
        //                    IMPORTANT VALIDATION STEP                       //
        // ================================================================== //
        // In cases where any of the original AtoNs is not found in the current
        // list, this means that a new content will be invalid since there has
        // been a removal. Therefore, a ValidationException should be thrown.
        if(!atonNumbers.containsAll(origAtonNumbers)) {
            // Create a response that something went wrong
            CompletableFuture<S125Dataset> exFuture = CompletableFuture.failedFuture(new DeletedAtoNsInDatasetContentGenerationException(
                    String.format("Deleted AtoNs detected during the generation " +
                            "of the content of dataset with UUID %s. This " +
                            "dataset must be cancelled and replaced to " +
                            "continue...", s125Dataset.getUuid())
            ));
            // Stop the execution and inform the calling component on what happened
            return exFuture;
        }
        // ================================================================== //

        // Filter the new/updated Aids to Navigation entries - CAREFUL keel only
        // the unique items cause some might be included in both cases.
        final List<AidsToNavigation> newAtonList = atonList.stream()
                .filter(aton -> Objects.nonNull(aton.getAtonNumber()))
                .filter(aton -> not(origAtonNumbers.contains(aton.getAtonNumber())))
                .toList();
        final List<AidsToNavigation> updatedAtonList = atonList.stream()
                .filter(aton -> Objects.nonNull(aton.getLastModifiedAt()))
                .filter(aton -> aton.getLastModifiedAt().isAfter(Optional.of(s125Dataset)
                        .map(S125Dataset::getDatasetContent)
                        .map(DatasetContent::getGeneratedAt)
                        .orElse(LocalDateTime.MIN)))
                .toList();
        final List<AidsToNavigation> deltaAtonList = Stream
                .concat(newAtonList.stream(), updatedAtonList.stream())
                .collect(Collectors.toSet()) // To keep only the unique items
                .stream()
                .toList();

        // Now try to marshal the dataset into an XML string and update the content/delta
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
        try {
            // Build the dataset contents, if any
            final Dataset dataset = s125DatasetBuilder.packageToDataset(s125Dataset, atonList);
            final Dataset delta = s125DatasetBuilder.packageToDataset(s125Dataset, deltaAtonList);

            // Marshall the contents into XML
            final String datasetXML = S125Utils.marshalS125(dataset, Boolean.TRUE);
            // Marshall the delta into XML - but only if it's not cancelled/deleted
            final String deltaXML = S125Utils.marshalS125(delta, Boolean.TRUE);

            // Populate the dataset content/delta
            datasetContent.setDataset(this.datasetService.findOne(s125Dataset.getUuid()));
            datasetContent.setContent(datasetXML);
            datasetContent.setContentLength(Optional.ofNullable(datasetXML)
                    .map(String::length)
                    .map(BigInteger::valueOf)
                    .orElse(BigInteger.ZERO));
            datasetContent.setDelta(deltaXML);
            datasetContent.setDeltaLength(Optional.ofNullable(deltaXML)
                    .map(String::length)
                    .map(BigInteger::valueOf)
                    .orElse(BigInteger.ZERO));

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
