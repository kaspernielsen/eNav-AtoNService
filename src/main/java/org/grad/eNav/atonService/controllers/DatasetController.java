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

package org.grad.eNav.atonService.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.models.dtos.s125.S125DataSetDto;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.atonService.utils.HeaderUtil;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;


/**
 * REST controller for managing Datasets.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@RequestMapping("/api/dataset")
@Slf4j
public class DatasetController {

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * Object Mapper from Domain to DTO.
     */
    @Autowired
    DomainDtoMapper<S125Dataset, S125DataSetDto> datasetDtoMapper;

    /**
     * Object Mapper from DTO to Domain.
     */
    @Autowired
    DomainDtoMapper<S125DataSetDto, S125Dataset> datasetDomainMapper;

    /**
     * GET /api/dataset : Returns a paged list of all current datasets.
     *
     * @param uuid the UUID of the dataset to be retrieved
     * @param geometry the geometry for Dataset filtering
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of datasets in body
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<S125DataSetDto>> getDatasets(@RequestParam("datasetTitle") Optional<UUID> uuid,
                                                            @RequestParam("geometry") Optional<Geometry> geometry,
                                                            Pageable pageable) {
        log.debug("REST request to get page of Dataset");
        uuid.ifPresent(v -> log.debug("Dataset UUID specified as: {}", uuid.toString()));
        geometry.ifPresent(v -> log.debug("Dataset geometry specified as: {}", GeometryJSONConverter.convertFromGeometry(v).toString()));
        final Page<S125Dataset> datasetPage = this.datasetService.findAll(
                uuid.orElse(null),
                geometry.orElse(null),
                null,
                null,
                Boolean.TRUE,
                pageable
        );
        return ResponseEntity.ok()
                .body(this.datasetDtoMapper.convertToPage(datasetPage, S125DataSetDto.class));
    }

    /**
     * POST /api/dataset/dt : Returns a paged list of all current datasets
     * for the datatables front-end.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of datasets in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<S125DataSetDto>> getDatasetsForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        log.debug("REST request to get page of Dataset for datatables");
        final Page<S125Dataset> datasetPage = this.datasetService.handleDatatablesPagingRequest(dtPagingRequest);
        return ResponseEntity.ok()
                .body(this.datasetDtoMapper.convertToDtPage(datasetPage, dtPagingRequest, S125DataSetDto.class));
    }

    /**
     * POST /api/dataset : Create a new dataset.
     *
     * @param dataSetDto the dataset to create
     * @return the ResponseEntity with status 201 (Created) and with body the new instance, or with status 400 (Bad Request) if the instance has already an ID
     */
    @Transactional // <- We need this to read the updated content (lazy loading)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> createDataset(@RequestBody S125DataSetDto dataSetDto) {
        log.debug("REST request to save Dataset : {}", dataSetDto);
        // Check for an ID
        if (dataSetDto.getUuid() != null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", "idexists", "A new dataset cannot already have an ID"))
                    .build();
        }
        // Save the dataset
        try {
            final S125Dataset s125DataSet = this.datasetService.save(this.datasetDomainMapper.convertTo(dataSetDto, S125Dataset.class));
            return ResponseEntity.created(new URI(String.format("/api/dataset/%s", s125DataSet.getUuid())))
                    .body(this.datasetDtoMapper.convertTo(s125DataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .body(dataSetDto);
        }
    }

    /**
     * PUT /api/dataset/{uuid} : Updates an existing "UUID" dataset.
     *
     * @param uuid the UUID of the dataset to be updated
     * @param dataSetDto the dataset to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated instance
     */
    @Transactional // <- We need this to read the updated content (lazy loading)
    @PutMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> updateDataset(@PathVariable UUID uuid, @Valid @RequestBody S125DataSetDto dataSetDto) {
        log.debug("REST request to update Dataset : {}", dataSetDto);
        // Make sure we got the UUID
        dataSetDto.setUuid(uuid);
        // Save the dataset
        try {
            final S125Dataset s125DataSet = this.datasetService.save(this.datasetDomainMapper.convertTo(dataSetDto, S125Dataset.class));
            return ResponseEntity.ok()
                    .body(this.datasetDtoMapper.convertTo(s125DataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .body(dataSetDto);
        }
    }

    /**
     * PUT /api/dataset/{uuid}/cancel : Cancels an existing "UUID" dataset.
     *
     * @param uuid the UUID of the dataset to be cancelled
     * @return the ResponseEntity with status 200 (OK) and with body the cancelled instance
     */
    @PutMapping(value = "/{uuid}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> cancelDataset(@PathVariable UUID uuid) {
        log.debug("REST request to cancel Dataset with UUID: {}", uuid);
        // Cancel the dataset
        try {
            final S125Dataset s125DataSet = this.datasetService.cancel(uuid);
            return ResponseEntity.ok()
                    .body(this.datasetDtoMapper.convertTo(s125DataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .build();
        }
    }

    /**
     * PUT /api/dataset/{uuid}/cancel : Replaces an existing "UUID" dataset.
     *
     * @param uuid the UUID of the dataset to be replaced
     * @return the ResponseEntity with status 200 (OK) and with body the replaced instance
     */
    @PutMapping(value = "/{uuid}/replace", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> replaceDataset(@PathVariable UUID uuid) {
        log.debug("REST request to replace Dataset with UUID: {}", uuid);
        // Replace the dataset
        try {
            final S125Dataset copiedDataSet = this.datasetService.replace(uuid);
            return ResponseEntity.ok()
                    .body(this.datasetDtoMapper.convertTo(copiedDataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .build();
        }
    }

    /**
     * DELETE /api/dataset/{uuid} : Delete the "UUID" Dataset.
     *
     * @param uuid the UUID of the Dataset to be deleted
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteDataset(@PathVariable UUID uuid) {
        log.debug("REST request to delete Dataset with UUID : {}", uuid);
        final S125Dataset s125Dataset = this.datasetService.delete(uuid);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("dataset", s125Dataset.getUuid().toString()))
                .build();
    }

}
