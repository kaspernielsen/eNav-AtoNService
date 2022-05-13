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

import _int.iala_aism.s125.gml._0_0.DataSet;
import _int.iala_aism.s125.gml._0_0.S125AidsToNavigationType;
import _int.iho.s100.gml.base._1_0_Ext.CurveProperty;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _int.iho.s100.gml.base._1_0_Ext.PointProperty;
import _int.iho.s100.gml.base._1_0_Ext.SurfaceProperty;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.s125.S125DatasetInfo;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.models.dtos.s125.AidsToNavigationDto;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.atonService.utils.GeometryS125Converter;
import org.grad.eNav.atonService.utils.HeaderUtil;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.beans.PropertyDescriptor;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * The Application Operator Name Information.
     */
    @Value("${gla.rad.aton-service.info.operatorName:Unknown}")
    private String appOperatorName;

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
     * Setup up addition model mapper configurations.
     */
    @PostConstruct
    void init() {
        // Create the Base Aids to Navigation type map
        this.aidsToNavigationToDtoMapper.getModelMapper().createTypeMap(AidsToNavigation.class, AidsToNavigationDto.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> S125AtonTypes.fromLocalClass(((AidsToNavigation) ctx.getSource()).getClass()).getDescription())
                            .map(src -> src, AidsToNavigationDto::setAtonType);
                    mapper.using(ctx -> this.convertTos125DataSet(Collections.singletonList((AidsToNavigation) ctx.getSource())))
                            .map(src -> src, AidsToNavigationDto::setContent);
                });
        // Add the base to all Aids to Navigation Mappings
        for(S125AtonTypes atonType : S125AtonTypes.values()) {
            // Skip the unknown type, we don't need it
            if(atonType == S125AtonTypes.UNKNOWN) {
                continue;
            }
            this.aidsToNavigationToDtoMapper.getModelMapper().createTypeMap(atonType.getLocalClass(), AidsToNavigationDto.class)
                    .includeBase(AidsToNavigation.class, AidsToNavigationDto.class);
            this.aidsToNavigationToDtoMapper.getModelMapper().createTypeMap(atonType.getLocalClass(), atonType.getS125Class())
                    .addMappings(mapper -> {
                            mapper.map(AidsToNavigation::getId, S125AidsToNavigationType::setId);
                            mapper.using(ctx -> this.convertToS125Geometry((AidsToNavigation) ctx.getSource()))
                                    .map(src -> src, (dest, val) -> {
                                        try {
                                            new PropertyDescriptor("geometry", atonType.getS125Class()).getWriteMethod().invoke(dest, val);
                                        } catch (Exception ex) {
                                            this.log.error(ex.getMessage());
                                        }
                                    });
                    });
        }
    }

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

    /**
     * Converts a whole list of Aids to Navigation objects into an XML string
     * representation conforming to the S-125 data product specification.
     *
     * @param atons the list of the Aids to Navigation objects
     * @return the respective S-125 data string representation
     */
    protected String convertTos125DataSet(List<AidsToNavigation> atons) {
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.aidsToNavigationToDtoMapper.getModelMapper());
        final S125DatasetInfo datasetInfo = new S125DatasetInfo(
                "AtoN Dataset for " + atons.stream().map(AidsToNavigation::getAtonNumber).collect(Collectors.joining(" ")),
                appOperatorName.replaceAll(" ","_"),
                atons
        );
        final DataSet dataset = s125DatasetBuilder.packageToDataset(datasetInfo, atons);
        try {
            return S125Utils.marshalS125(dataset);
        } catch (JAXBException ex) {
            this.log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * Converts the geometry for an Aids to Navigation object for the local JTS
     * format used for persistence to the S-125 data product specification
     * compatible one.
     *
     * @param aidsToNavigation the Aids to Navigation to get the geometry from
     * @return the S-125 compliant geometry description
     */
    protected Object convertToS125Geometry(AidsToNavigation aidsToNavigation) {
        PointCurveSurfaceProperty pointCurveSurfaceProperty = new GeometryS125Converter().convertFromGeometry(aidsToNavigation);
        if(pointCurveSurfaceProperty.getSurfaceProperty() != null) {
            SurfaceProperty surfaceProperty = new SurfaceProperty();
            surfaceProperty.setSurfaceProperty(pointCurveSurfaceProperty.getSurfaceProperty());
            return surfaceProperty;
        } else if(pointCurveSurfaceProperty.getSurfaceProperty() != null) {
            CurveProperty curveProperty = new CurveProperty();
            curveProperty.setCurveProperty(pointCurveSurfaceProperty.getCurveProperty());
            return curveProperty;
        } else {
            PointProperty pointProperty = new PointProperty();
            pointProperty.setPointProperty(pointCurveSurfaceProperty.getPointProperty());
            return pointProperty;
        }
    }
}
