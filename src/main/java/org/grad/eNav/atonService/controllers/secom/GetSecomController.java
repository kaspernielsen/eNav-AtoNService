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

import _int.iala_aism.s125.gml._0_0.DataSet;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.UnLoCodeMapEntry;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.eNav.atonService.utils.GeometryUtils;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.atonService.utils.WKTUtil;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.exceptions.SecomNotFoundException;
import org.grad.secom.core.interfaces.GetSecomInterface;
import org.grad.secom.core.models.DataResponseObject;
import org.grad.secom.core.models.GetResponseObject;
import org.grad.secom.core.models.PaginationObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The SECOM Get Interface Controller.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Path("/")
@Validated
@Slf4j
public class GetSecomController implements GetSecomInterface {


    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    /**
     * The UN/LOCODE Service.
     */
    @Autowired
    UnLoCodeService unLoCodeService;

    // Class Variables
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);

    /**
     * GET /api/secom/v1/dataset : Returns the S-125 dataset entries as,
     * specified by the SECOM standard.
     *
     * @param dataReference the object data reference
     * @param containerType the object data container type
     * @param dataProductType the object data product type
     * @param productVersion the object data product version
     * @param geometry the object geometry
     * @param unlocode the object UNLOCODE
     * @param validFrom the object valid from time
     * @param validTo the object valid to time
     * @param page the page number to be retrieved
     * @param pageSize the maximum page size
     * @return the S-125 dataset information
     */
    @Tag(name = "SECOM")
    @Transactional
    public GetResponseObject get(@QueryParam("dataReference") UUID dataReference,
                                 @QueryParam("containerType") ContainerTypeEnum containerType,
                                 @QueryParam("dataProductType") SECOM_DataProductType dataProductType,
                                 @QueryParam("productVersion") String productVersion,
                                 @QueryParam("geometry") String geometry,
                                 @QueryParam("unlocode") @Pattern(regexp = "[A-Z]{5}") String unlocode,
                                 @QueryParam("validFrom") LocalDateTime validFrom,
                                 @QueryParam("validTo") LocalDateTime validTo,
                                 @QueryParam("page") @Min(0) Integer page,
                                 @QueryParam("pageSize") @Min(0) Integer pageSize) {
        log.debug("SECOM request to get page of Dataset");
        Optional.ofNullable(dataReference).ifPresent(v -> log.debug("Data Reference specified as: {}", dataReference));
        Optional.ofNullable(containerType).ifPresent(v -> log.debug("Coontainer Type specified as: {}", containerType));
        Optional.ofNullable(dataProductType).ifPresent(v -> log.debug("Data Product Type specified as: {}", dataProductType));
        Optional.ofNullable(geometry).ifPresent(v -> log.debug("Geometry specified as: {}", geometry));
        Optional.ofNullable(unlocode).ifPresent(v -> log.debug("UNLOCODE specified as: {}", unlocode));
        Optional.ofNullable(validFrom).ifPresent(v -> log.debug("Valid From time specified as: {}", validFrom));
        Optional.ofNullable(validTo).ifPresent(v -> log.debug("Valid To time specified as: {}", validTo));

        // Init local variables
        Geometry jtsGeometry = null;
        String data = null;
        S125DataSet s125DataSet = new S125DataSet();

        // Parse the arguments
        final ContainerTypeEnum reqContainerType = Optional.ofNullable(containerType)
                .orElse(ContainerTypeEnum.S100_DataSet);
        final SECOM_DataProductType reqDataProductType = Optional.ofNullable(dataProductType)
                .orElse(SECOM_DataProductType.S125);
        if(Objects.nonNull(dataReference)) {
            try {
                s125DataSet = this.datasetService.findOne(dataReference);
                jtsGeometry = s125DataSet.getGeometry();
            } catch (DataNotFoundException ex) {
                throw new SecomNotFoundException(dataReference.toString());
            }
        }
        if(Objects.nonNull(geometry)) {
            try {
                jtsGeometry = GeometryUtils.joinGeometries(jtsGeometry, WKTUtil.convertWKTtoGeometry(geometry));
            } catch (ParseException ex) {
                throw new ValidationException(ex.getMessage());
            }
        }
        if(Objects.nonNull(unlocode)) {
            jtsGeometry = GeometryUtils.joinGeometries(jtsGeometry, Optional.ofNullable(unlocode)
                    .map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)));
        }

        // Handle the input request
        final Geometry finalReqGeometry = jtsGeometry;
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(
                null,
                finalReqGeometry,
                validFrom,
                validTo,
                Optional.ofNullable(page)
                        .map(p -> PageRequest.of(p, Optional.ofNullable(pageSize).orElse(Integer.MAX_VALUE)))
                        .map(Pageable.class::cast)
                        .orElse(Pageable.unpaged())
        );

        // We only support S-100 Datasets here
        if(reqContainerType == ContainerTypeEnum.S100_DataSet) {
            // We only support specifically S-125 Datasets
            if(reqDataProductType == SECOM_DataProductType.S125) {
                try {
                    final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
                    final DataSet dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());
                    data = S125Utils.marshalS125(dataset, Boolean.FALSE);
                } catch (Exception ex) {
                    throw new ValidationException(ex.getMessage());
                }
            }
        }

        // Generate the Get Response Object
        final GetResponseObject getResponseObject = new GetResponseObject();
        final DataResponseObject dataResponseObject = new DataResponseObject();
        dataResponseObject.setData(data.getBytes(StandardCharsets.UTF_8));
        getResponseObject.setDataResponseObject(dataResponseObject);
        getResponseObject.setPagination(new PaginationObject(
                (int) atonPage.getTotalElements(),
                Optional.ofNullable(pageSize).orElse(Integer.MAX_VALUE)));

        // And final return the Get Response Object
        return getResponseObject;

    }

}
