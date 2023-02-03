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

import _int.iala_aism.s125.gml._0_0.DataSet;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.DatasetType;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.repos.DatasetContentRepo;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Dataset Content Repo.
     */
    @Autowired
    DatasetContentRepo datasetContentRepo;

    /**
     * Find the latest dataset content by UUID.
     *
     * @param uuid the UUID of the dataset
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContent findLatest(@NotNull UUID uuid) {
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
    public DatasetContent findLatest(@NotNull UUID uuid, LocalDateTime localDateTime) {
        return this.datasetContentRepo.findLatestForUuid(
                        uuid,
                        Optional.ofNullable(localDateTime).orElseGet(LocalDateTime::now),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseGet(() ->
                    // Handling cases where the content is not yet generated
                    // We need to first generate and store this manually
                    this.save(this.generateDatasetContent(this.datasetService.findOne(uuid)))
                );
    }

    /**
     * A simple saving operation that persists the models in the database using
     * the correct repository based on the instance type.
     *
     * @param datasetContent the Dataset entity to be saved
     * @return the saved Dataset entity
     */
    @Transactional
    public DatasetContent save(@NotNull DatasetContent datasetContent) {
        log.debug("Request to save Dataset Content: {}", datasetContent);

        // Save and return the dataset
        return this.datasetContentRepo.save(datasetContent);
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
    public DatasetContent generateDatasetContent(@NotNull S125DataSet s125DataSet) {
        log.debug("Request to retrieve the content for Dataset with UUID : {}", s125DataSet.getUuid());

        // Get all the matching Aids to Navigation
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(
                null,
                s125DataSet.getGeometry(),
                null,
                null,
                Pageable.unpaged()
        );

        // If everything is OK up to now start building the dataset content
        final DatasetContent datasetContent = new DatasetContent();
        datasetContent.setDatasetType(DatasetType.S125);
        datasetContent.setUuid(s125DataSet.getUuid());
        datasetContent.setGeometry(s125DataSet.getGeometry());
        datasetContent.setCreatedAt(LocalDateTime.now());

        // Now try to marshal the dataset into an XML string
        try {
            final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
            final DataSet dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());
            datasetContent.setContent(S125Utils.marshalS125(dataset, Boolean.FALSE));
            datasetContent.setContentLength(BigInteger.valueOf(datasetContent.getContent().length()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            datasetContent.setContent("");
            datasetContent.setContentLength(BigInteger.ZERO);
        }

        // And return the dataset content
        return datasetContent;
    }

}
