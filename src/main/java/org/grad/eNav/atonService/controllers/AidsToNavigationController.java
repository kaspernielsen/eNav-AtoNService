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
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.models.dtos.s125.AidsToNavigationDto;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.atonService.utils.HeaderUtil;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

/**
 * REST controller for managing Aids to Navigation.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@RequestMapping("/api/atons")
@Slf4j
public class AidsToNavigationController {

    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    /**
     * Object Mapper from Domain to DTO.
     */
    @Autowired
    DomainDtoMapper<AidsToNavigation, AidsToNavigationDto> aidsToNavigationToDtoMapper;

    /**
     * GET /api/atons : Returns a paged list of all current Aids to navigation.
     *
     * @param atonNumber the Aids to Navigation number
     * @param geometry the geometry for AtoN message filtering
     * @param startDate the start date for AtoN message filtering
     * @param endDate the end date for AtoN message filtering
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AidsToNavigationDto>> getAidsToNavigation(@RequestParam("atonNumber") Optional<String> atonNumber,
                                                                         @RequestParam("geometry") Optional<Geometry> geometry,
                                                                         @RequestParam("startDate") Optional<Date> startDate,
                                                                         @RequestParam("endDate") Optional<Date> endDate,
                                                                         Pageable pageable) {
        this.log.debug("REST request to get page of Aids to Navigation");
        atonNumber.ifPresent(v -> this.log.debug("Aids to Navigation number specified as: {}", atonNumber));
        geometry.ifPresent(v -> this.log.debug("Aids to Navigation geometry specified as: {}", GeometryJSONConverter.convertFromGeometry(v).toString()));
        startDate.ifPresent(v -> this.log.debug("Aids to Navigation start date specified as: {}", startDate));
        endDate.ifPresent(v -> this.log.debug("Aids to Navigation end date specified as: {}", endDate));
        Page<AidsToNavigation> nodePage = this.aidsToNavigationService.findAll(atonNumber, geometry, pageable);
        return ResponseEntity.ok()
                .body(this.aidsToNavigationToDtoMapper.convertToPage(nodePage, AidsToNavigationDto.class));
    }

    /**
     * POST /api/atons/dt : Returns a paged list of all current Aids to
     * Navigation for the datatables front-end.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<AidsToNavigationDto>> getAidsToNavigationForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        this.log.debug("REST request to get page of Aids to Navigation for datatables");
        return ResponseEntity.ok()
                .body(this.aidsToNavigationToDtoMapper.convertToDtPage(this.aidsToNavigationService.handleDatatablesPagingRequest(dtPagingRequest), dtPagingRequest, AidsToNavigationDto.class));
    }

    /**
     * DELETE /api/atons/{id} : Delete the "id" Aids to Navigation.
     *
     * @param id the ID of the Aids to Navigation to be deleted
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAidsToNavigation(@PathVariable BigInteger id) {
        this.log.debug("REST request to delete Aids to Navigation : {}", id);
        this.aidsToNavigationService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("aton", id.toString()))
                .build();
    }

}
