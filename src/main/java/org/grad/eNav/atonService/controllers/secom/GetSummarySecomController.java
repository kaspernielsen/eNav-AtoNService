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
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.services.AidsToNavigationService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        this.log.debug("SECOM request to get page of Dataset Summary");
        Optional.ofNullable(containerType).ifPresent(v -> this.log.debug("Container Type specified as: {}", containerType));
        Optional.ofNullable(dataProductType).ifPresent(v -> this.log.debug("Data Product Type specified as: {}", dataProductType));
        Optional.ofNullable(geometry).ifPresent(v -> this.log.debug("Geometry specified as: {}", geometry));
        Optional.ofNullable(unlocode).ifPresent(v -> this.log.debug("UNLOCODE specified as: {}", unlocode));
        Optional.ofNullable(validFrom).ifPresent(v -> this.log.debug("Valid From time specified as: {}", validFrom));
        Optional.ofNullable(validTo).ifPresent(v -> this.log.debug("Valid To time specified as: {}", validTo));

        // Init local variables
        Geometry reqGeometry = null;
        List<SummaryObject> data = null;

        // Parse the arguments
        final ContainerTypeEnum reqContainerType = Optional.ofNullable(containerType)
                .orElse(ContainerTypeEnum.S100_DataSet);
        final SECOM_DataProductType reqDataProductType = Optional.ofNullable(dataProductType)
                .orElse(SECOM_DataProductType.S125);
        if(Objects.nonNull(geometry)) {
            try {
                reqGeometry = WKTUtil.convertWKTtoGeometry(geometry);
            } catch (ParseException ex) {
                throw new ValidationException(ex.getMessage());
            }
        }
        if(Objects.nonNull(unlocode)) {
            reqGeometry = GeometryUtils.joinGeometries(reqGeometry, Optional.ofNullable(unlocode)
                    .map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)));
        }

        // Handle the input request
        final Geometry finalReqGeometry = reqGeometry;
        final Page<S125DataSet> s125DataSetPage = this.datasetService.findAll(
                null,
                reqGeometry,
                null,
                null,
                PageRequest.of(Optional.ofNullable(page).orElse(0), Optional.ofNullable(pageSize).orElse(Integer.MAX_VALUE))
        );

        // We only support S-100 Datasets here
        if(reqContainerType == ContainerTypeEnum.S100_DataSet) {
            // We only support specifically S-125 Datasets
            if (reqDataProductType == SECOM_DataProductType.S125) {
                data = s125DataSetPage.get().map(s125Dataset -> {
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

                    // Calculate the summary size
                    summaryObject.setInfo_size(this.aidsToNavigationService.findAllTotalCount(
                            null,
                            GeometryUtils.joinGeometries(s125Dataset.getGeometry(), finalReqGeometry),
                            validFrom,
                            validTo)
                    );

                    // And return the summary object
                    return summaryObject;
                })
                .collect(Collectors.toList());
            }
        }

        // Start building the response
        final GetSummaryResponseObject getSummaryResponseObject = new GetSummaryResponseObject();
        getSummaryResponseObject.setSummaryObject(data);
        getSummaryResponseObject.setPagination(new PaginationObject((int) s125DataSetPage.getTotalElements(), s125DataSetPage.getSize()));

        // And return the Get Summary Response Object
        return getSummaryResponseObject;
    }

}
