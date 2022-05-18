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

package org.grad.eNav.atonService.controllers;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

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
    DomainDtoMapper<S125DataSet, S125DataSetDto> datasetDtoMapper;

    /**
     * Object Mapper from DTO to Domain.
     */
    @Autowired
    DomainDtoMapper<S125DataSetDto, S125DataSet> datasetDomainMapper;

    /**
     * GET /api/dataset : Returns a paged list of all current datasets.
     *
     * @param datasetTitle the S-125 Dataset Title
     * @param geometry the geometry for Dataset filtering
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<S125DataSetDto>> getDatasets(@RequestParam("datasetTitle") Optional<String> datasetTitle,
                                                            @RequestParam("geometry") Optional<Geometry> geometry,
                                                            Pageable pageable) {
        this.log.debug("REST request to get page of Dataset");
        datasetTitle.ifPresent(v -> this.log.debug("Dataset title specified as: {}", datasetTitle));
        geometry.ifPresent(v -> this.log.debug("Dataset geometry specified as: {}", GeometryJSONConverter.convertFromGeometry(v).toString()));
        Page<S125DataSet> datasetPage = this.datasetService.findAll(datasetTitle, geometry, pageable);
        return ResponseEntity.ok()
                .body(this.datasetDtoMapper.convertToPage(datasetPage, S125DataSetDto.class));
    }

    /**
     * POST /api/dataset/dt : Returns a paged list of all current datasets
     * for the datatables front-end.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<S125DataSetDto>> getDatasetsForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        this.log.debug("REST request to get page of Dataset for datatables");
        return ResponseEntity.ok()
                .body(this.datasetDtoMapper.convertToDtPage(this.datasetService.handleDatatablesPagingRequest(dtPagingRequest), dtPagingRequest, S125DataSetDto.class));
    }

    /**
     * POST /api/dataset : Create a new dataset.
     *
     * @param dataSetDto the dataset to create
     * @return the ResponseEntity with status 201 (Created) and with body the new instance, or with status 400 (Bad Request) if the instance has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> createDataset(@Valid @RequestBody S125DataSetDto dataSetDto) throws URISyntaxException {
        log.debug("REST request to save Dataset : {}", dataSetDto);
        // Check for an ID
        if (dataSetDto.getId() != null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("instance", "idexists", "A new dataset cannot already have an ID"))
                    .build();
        }
        // Save the station
        try {
            S125DataSet s125DataSet = this.datasetService.save(this.datasetDomainMapper.convertTo(dataSetDto, S125DataSet.class));
            return ResponseEntity.created(new URI(String.format("/api/dataset/%d", s125DataSet.getId())))
                    .body(this.datasetDtoMapper.convertTo(s125DataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .body(dataSetDto);
        }
    }

    /**
     * PUT /api/dataset/{id} : Updates an existing "ID" dataset.
     *
     * @param id the ID of the dataset to be updated
     * @param dataSetDto the dataset to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated instance
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S125DataSetDto> updateDataset(@PathVariable BigInteger id, @Valid @RequestBody S125DataSetDto dataSetDto) throws URISyntaxException {
        log.debug("REST request to update Dataset : {}", dataSetDto);
        // Make sure we gor the ID
        dataSetDto.setId(id);
        // Save the station
        try {
            S125DataSet s125DataSet = this.datasetService.save(this.datasetDomainMapper.convertTo(dataSetDto, S125DataSet.class));
            return ResponseEntity.ok()
                    .body(this.datasetDtoMapper.convertTo(s125DataSet, S125DataSetDto.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("dataset", e.getMessage(), e.toString()))
                    .body(dataSetDto);
        }
    }

    /**
     * DELETE /api/dataset/{id} : Delete the "ID" Dataset.
     *
     * @param id the ID of the Dataset to be deleted
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteDataset(@PathVariable BigInteger id) {
        this.log.debug("REST request to delete Dataset : {}", id);
        this.datasetService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("dataset", id.toString()))
                .build();
    }

}
