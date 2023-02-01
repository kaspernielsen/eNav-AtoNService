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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.UnLoCodeMapEntry;
import org.grad.eNav.atonService.services.DatasetContentService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.eNav.atonService.utils.GeometryUtils;
import org.grad.eNav.atonService.utils.WKTUtil;
import org.grad.secom.core.interfaces.GetSummarySecomInterface;
import org.grad.secom.core.models.GetSummaryResponseObject;
import org.grad.secom.core.models.PaginationObject;
import org.grad.secom.core.models.SummaryObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.InfoStatusEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The SECOM Get Summary Interface Controller.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Path("/")
@Validated
@Slf4j
public class GetSummarySecomController implements GetSummarySecomInterface {

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Dataset Content Service.
     */
    @Autowired
    DatasetContentService datasetContentService;

    /**
     * The UN/LOCODE Service.
     */
    @Autowired
    UnLoCodeService unLoCodeService;

    // Class Variables
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);

    /**
     * GET /api/secom/v1/dataset/summary : Returns the S-125 dataset summary
     * information, as specified by the SECOM standard.
     *
     * @param containerType the object data container type
     * @param dataProductType the object data product type
     * @param productVersion the object data product version
     * @param geometry the object geometry
     * @param unlocode the object UNLOCODE
     * @param validFrom the object valid from time
     * @param validTo the object valid to time
     * @param page the page number to be retrieved
     * @param pageSize the maximum page size
     * @return the S-125 dataset summary information
     */
    @Tag(name = "SECOM")
    @Transactional
    public GetSummaryResponseObject getSummary(@QueryParam("containerType") ContainerTypeEnum containerType,
                                               @QueryParam("dataProductType") SECOM_DataProductType dataProductType,
                                               @QueryParam("productVersion") String productVersion,
                                               @QueryParam("geometry") String geometry,
                                               @QueryParam("unlocode") @Pattern(regexp = "[A-Z]{5}") String unlocode,
                                               @QueryParam("validFrom") LocalDateTime validFrom,
                                               @QueryParam("validTo") LocalDateTime validTo,
                                               @QueryParam("page") @Min(0) Integer page,
                                               @QueryParam("pageSize") @Min(0) Integer pageSize) {
        log.debug("SECOM request to get page of Dataset Summary");
        Optional.ofNullable(containerType).ifPresent(v -> log.debug("Container Type specified as: {}", containerType));
        Optional.ofNullable(dataProductType).ifPresent(v -> log.debug("Data Product Type specified as: {}", dataProductType));
        Optional.ofNullable(geometry).ifPresent(v -> log.debug("Geometry specified as: {}", geometry));
        Optional.ofNullable(unlocode).ifPresent(v -> log.debug("UNLOCODE specified as: {}", unlocode));
        Optional.ofNullable(validFrom).ifPresent(v -> log.debug("Valid From time specified as: {}", validFrom));
        Optional.ofNullable(validTo).ifPresent(v -> log.debug("Valid To time specified as: {}", validTo));

        // Init local variables
        Geometry jtsGeometry = null;
        Pageable pageable = Optional.ofNullable(page)
                .map(p -> PageRequest.of(p, Optional.ofNullable(pageSize).orElse(Integer.MAX_VALUE)))
                .map(Pageable.class::cast)
                .orElse(Pageable.unpaged());

        // Parse the arguments
        final ContainerTypeEnum reqContainerType = Optional.ofNullable(containerType)
                .orElse(ContainerTypeEnum.S100_DataSet);
        final SECOM_DataProductType reqDataProductType = Optional.ofNullable(dataProductType)
                .orElse(SECOM_DataProductType.S125);
        if(Objects.nonNull(geometry)) {
            try {
                jtsGeometry = WKTUtil.convertWKTtoGeometry(geometry);
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

        // We only support S-100 Datasets here
        final List<SummaryObject> summaryObjectList = new ArrayList<>();
        if(reqContainerType == ContainerTypeEnum.S100_DataSet) {
            // We only support specifically S-125 Datasets
            if (reqDataProductType == SECOM_DataProductType.S125) {
                this.datasetService.findAll(null, jtsGeometry, validFrom, validTo, pageable)
                        .stream()
                        .map(s125Dataset -> {
                            // Create and populate the summary object
                            SummaryObject summaryObject = new SummaryObject();
                            summaryObject.setDataReference(s125Dataset.getUuid());
                            summaryObject.setDataProtection(Boolean.FALSE);
                            summaryObject.setDataCompression(Boolean.FALSE);
                            summaryObject.setContainerType(reqContainerType);
                            summaryObject.setDataProductType(reqDataProductType);
                            summaryObject.setInfo_productVersion(s125Dataset.getDatasetIdentificationInformation().getProductEdition());
                            summaryObject.setInfo_identifier(s125Dataset.getDatasetIdentificationInformation().getDatasetFileIdentifier());
                            summaryObject.setInfo_name(s125Dataset.getDatasetIdentificationInformation().getDatasetTitle());
                            summaryObject.setInfo_status(InfoStatusEnum.PRESENT.getValue());
                            summaryObject.setInfo_description(s125Dataset.getDatasetIdentificationInformation().getDatasetAbstract());
                            summaryObject.setInfo_lastModifiedDate(s125Dataset.getLastUpdatedAt());
                            summaryObject.setInfo_size(this.datasetContentService.findLatest(s125Dataset.getUuid()).getContentLength().longValue());

                            // And return the summary object
                            return summaryObject;
                        })
                        .forEach(summaryObjectList::add);
            }
        }

        // Start building the response
        final GetSummaryResponseObject getSummaryResponseObject = new GetSummaryResponseObject();
        getSummaryResponseObject.setSummaryObject(summaryObjectList);
        getSummaryResponseObject.setPagination(new PaginationObject(
                summaryObjectList.size(),
                Optional.ofNullable(pageSize).orElse(Integer.MAX_VALUE)));

        // And return the Get Summary Response Object
        return getSummaryResponseObject;
    }

}
