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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.UnLoCodeMapEntry;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.eNav.atonService.utils.HeaderUtil;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.atonService.utils.WKTUtil;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.interfaces.CapabilityInterface;
import org.grad.secom.interfaces.GetInterface;
import org.grad.secom.interfaces.GetSummaryInterface;
import org.grad.secom.models.*;
import org.grad.secom.models.enums.AreaNameEnum;
import org.grad.secom.models.enums.DataTypeEnum;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.Pattern;
import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secom")
@Slf4j
public class SecomController implements GetInterface, GetSummaryInterface, CapabilityInterface {

    /**
     * The AtoN Service Data Product Name.
     */
    @Value("${gla.rad.aton-service.data-product.name:S-125}")
    private String dataProductName;

    /**
     * The AtoN Service Data Product Version.
     */
    @Value("${gla.rad.aton-service.data-product.version:0.0.0}")
    private String dataProductVersion;

    /**
     * The AtoN Service Data Product Location.
     */
    @Value("${gla.rad.aton-service.data-product.location:/xsd/S125.xsd}")
    private String dataProductLocation;

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
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);

    /**
     * GET /api/secom/v1/object : The Get interface is used for pulling
     * information from a service provider. The owner of the information
     * (provider) is responsible for the authorization procedure before
     * returning information.
     *
     * @param dataReference the object data reference
     * @param dataType the object data type
     * @param productSpecification the object product specification
     * @param geometry the object geometry
     * @param areaName the object area name
     * @param unlocode the object UNLOCODE
     * @param fromTime the object from time
     * @param toTime the object to time
     * @param pageable the pageable information
     * @return the object information
     */
    @GetMapping(value = "/v1/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetResponse> get(@RequestParam(value = "dataReference", required = false) String dataReference,
                                           @RequestParam(value = "dataType", required = false) DataTypeEnum dataType,
                                           @RequestParam(value = "productSpecification", required = false) String productSpecification,
                                           @RequestParam(value = "geometry", required = false) String geometry,
                                           @RequestParam(value = "areaName", required = false) @Parameter(style = ParameterStyle.SIMPLE) @Pattern(regexp = "(\\d+(,\\d+)*)?") List<AreaNameEnum> areaName,
                                           @RequestParam(value = "unlocode", required = false) @Pattern(regexp = "[a-z]{5}") String unlocode,
                                           @RequestParam(value = "fromTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                           @RequestParam(value = "toTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
                                           Pageable pageable) {
        this.log.debug("SECOM request to get page of Dataset");
        Optional.ofNullable(dataReference).ifPresent(v -> this.log.debug("Data Reference specified as: {}", dataReference));
        Optional.ofNullable(dataType).ifPresent(v -> this.log.debug("Data Type specified as: {}", dataType));
        Optional.ofNullable(productSpecification).ifPresent(v -> this.log.debug("Product Specification specified as: {}", productSpecification));
        Optional.ofNullable(geometry).ifPresent(v -> this.log.debug("Geometry specified as: {}", geometry));
        Optional.ofNullable(areaName).ifPresent(v -> this.log.debug("Area Name specified as: {}", areaName));
        Optional.ofNullable(unlocode).ifPresent(v -> this.log.debug("UNLOCODE specified as: {}", unlocode));
        Optional.ofNullable(fromTime).ifPresent(v -> this.log.debug("From time specified as: {}", fromTime));
        Optional.ofNullable(toTime).ifPresent(v -> this.log.debug("To time specified as: {}", toTime));

        // Parse the arguments
        final S125DataSet s125DataSet = Optional.ofNullable(dataReference)
                .map(BigInteger::new)
                .map(this.datasetService::findOne)
                .orElseGet(() -> new S125DataSet());
        Geometry jtsGeometry = Optional.of(s125DataSet)
                .map(S125DataSet::getGeometry)
                .orElse(null);
        if(Objects.nonNull(geometry)) {
            try {
                jtsGeometry = WKTUtil.convertWKTtoGeometry(geometry);
            } catch (ParseException e) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createFailureAlert("dataset","geometry","Could not parse WKT geometry"))
                        .build();
            }
        }
        if(Objects.nonNull(unlocode)) {
            jtsGeometry = this.joinGeometries(jtsGeometry, Optional.ofNullable(unlocode)
                    .map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)), true);
        }
        if(Objects.nonNull(areaName)) {
            jtsGeometry = this.joinGeometries(jtsGeometry, null, true);
        }

        // Handle the input request
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(
                null,
                jtsGeometry,
                fromTime,
                toTime,
                pageable
        );

        // Start building the data response
        final DataResponse dataResponse = new DataResponse();

        // Now handle according to the data type
        switch (Optional.ofNullable(dataType).orElse(DataTypeEnum.S100_DataSet)) {
            case S100_DataSet:
            default:
                try {
                    final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(modelMapper);
                    final DataSet dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());
                    dataResponse.setPayload(S125Utils.marshalS125(dataset, Boolean.FALSE));
                } catch (JAXBException ex) {
                    this.log.error(ex.getMessage());
                }
                break;
        }

        // Now the exchange metadata
        dataResponse.setServiceExchangeMetadata(this.signPayload(dataResponse.getPayload()));

        // Populate the Get Response Object
        final GetResponse getResponseObject = new GetResponse();
        getResponseObject.setData(dataResponse);
        getResponseObject.setPagination(new Pagination((int)atonPage.getTotalElements(), atonPage.getSize()));

        // And final return the Get Response Object
        return ResponseEntity.ok()
                .body(getResponseObject);

    }

    /**
     * GET /api/secom/v1/dataset/summary : Returns the S-125 dataset entries as
     * specified by the SECOM standard.
     *
     * @param dataType the dataset data type
     * @param productSpecification the dataset product specification
     * @param geometry the dataset geometry
     * @param areaName the dataset area name
     * @param unlocode the dataset entries UNLOCODE
     * @param fromTime the dataset entries from time
     * @param toTime the dataset enries to time
     * @param pageable the pageable information
     * @return the SECOM-compliant dataset summary information
     * POLYGON ((-25.620117 43.452919, -25.620117 61.564574, 33.00293 61.564574, 33.00293 43.452919, -25.620117 43.452919))
     * 2021-05-17T00:00:00.000Z
     */
    @GetMapping(value = "/v1/dataset/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetSummaryResponse> getSummary(@RequestParam(value = "dataType", required = false) DataTypeEnum dataType,
                                                         @RequestParam(value = "productSpecification", required = false) String productSpecification,
                                                         @RequestParam(value = "geometry", required = false) String geometry,
                                                         @RequestParam(value = "areaName", required = false) @Parameter(style = ParameterStyle.SIMPLE) @Pattern(regexp = "(\\d+(,\\d+)*)?") List<AreaNameEnum> areaName,
                                                         @RequestParam(value = "unlocode", required = false) @Pattern(regexp = "[a-z]{5}") String unlocode,
                                                         @RequestParam(value = "fromTime", required = false) LocalDateTime fromTime,
                                                         @RequestParam(value = "toTime", required = false) LocalDateTime toTime,
                                                         Pageable pageable) {
        this.log.debug("SECOM request to get page of Dataset Summary");
        Optional.ofNullable(dataType).ifPresent(v -> this.log.debug("Data Type specified as: {}", dataType));
        Optional.ofNullable(productSpecification).ifPresent(v -> this.log.debug("Product Specification specified as: {}", productSpecification));
        Optional.ofNullable(geometry).ifPresent(v -> this.log.debug("Geometry specified as: {}", geometry));
        Optional.ofNullable(areaName).ifPresent(v -> this.log.debug("Area Name specified as: {}", areaName));
        Optional.ofNullable(unlocode).ifPresent(v -> this.log.debug("UNLOCODE specified as: {}", unlocode));
        Optional.ofNullable(fromTime).ifPresent(v -> this.log.debug("From time specified as: {}", fromTime));
        Optional.ofNullable(toTime).ifPresent(v -> this.log.debug("To time specified as: {}", toTime));

        // Parse the arguments
        Geometry jtsGeometry = null;
        if(Objects.nonNull(geometry)) {
            try {
                jtsGeometry = WKTUtil.convertWKTtoGeometry(geometry);
            } catch (ParseException e) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.getsummary","geometry"))
                        .build();
            }
        }
        if(Objects.nonNull(unlocode)) {
            jtsGeometry =  this.joinGeometries(jtsGeometry, Optional.of(unlocode)
                    .map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)), true);
        }
        if(Objects.nonNull(areaName)) {
            jtsGeometry = this.joinGeometries(jtsGeometry, null, true);
        }

        // Handle the input request
        final Page<S125DataSet> s125Datasets = this.datasetService.findAll(
                null,
                jtsGeometry,
                null,
                null,
                pageable
        );

        // Start building the response
        final GetSummaryResponse getSummaryResponseObject = new GetSummaryResponse();

        // Now handle according to the data type
        final Geometry finalJTSGeometry = jtsGeometry;
        switch (Optional.ofNullable(dataType).orElse(DataTypeEnum.S100_DataSet)) {
            case S100_DataSet:
            default:
                getSummaryResponseObject.setSummary(s125Datasets.stream()
                        .map(s125Dataset -> {
                            // Create and populate the summary object
                            Summary summaryObject = new Summary();
                            summaryObject.setDataReference(s125Dataset.getId().toString());
                            summaryObject.setDataType(DataTypeEnum.S100_DataSet);
                            summaryObject.setInfo_identifier(s125Dataset.getDatasetIdentificationInformation().getDatasetFileIdentifier());
                            summaryObject.setInfo_name(s125Dataset.getDatasetIdentificationInformation().getDatasetTitle());
                            summaryObject.setInfo_description(s125Dataset.getDatasetIdentificationInformation().getDatasetAbstract());
                            summaryObject.setInfo_status("present");
                            summaryObject.setInfo_lastModifiedDate(s125Dataset.getLastUpdatedAt());

                            // Calculate the summary size
                            summaryObject.setInfo_size(this.aidsToNavigationService.findAllTotalCount(
                                    null,
                                    this.joinGeometries(s125Dataset.getGeometry(), finalJTSGeometry, true),
                                    fromTime,
                                    toTime)
                            );

                            // Add the product specification information
                            S100ProductSpecification s100ProductSpecification = new S100ProductSpecification();
                            s100ProductSpecification.setName(s125Dataset.getDatasetIdentificationInformation().getProductIdentifier());
                            s100ProductSpecification.setVersion(s125Dataset.getDatasetIdentificationInformation().getProductEdition());
                            s100ProductSpecification.setDate(null);
                            s100ProductSpecification.setNumber(null);
                            summaryObject.setProductSpecification(s100ProductSpecification);

                            // And return the summary object
                            return summaryObject;
                        })
                        .collect(Collectors.toList()));
                break;
        }

        // Now handle the pagination
        getSummaryResponseObject.setPagination(new Pagination((int)s125Datasets.getTotalElements(), s125Datasets.getSize()));

        // And return the Get Message Response Object
        return ResponseEntity.ok()
                .body(getSummaryResponseObject);
    }

    /**
     * GET /api/secom/v1/capability : Returns the service instance capabilities.
     *
     * @return the SECOM-compliant service capabilities
     */
    @GetMapping(value = CAPABILITY_INTERFACE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CapabilityResponse> getCapabilities() {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        final URL payloadSchemaUrl = Optional.of(dataProductLocation)
                .map(l -> l.startsWith("http") ? l :(baseUrl + l) )
                .map(l -> { try { return new URL(l); } catch (MalformedURLException ex) { return null; } })
                .orElse(null);

        // Start building the response
        CapabilityResponse capabilityResponseObject = new CapabilityResponse();
        capabilityResponseObject.setPayloadName(this.dataProductName);
        capabilityResponseObject.setPayloadVersion(this.dataProductVersion);
        capabilityResponseObject.setPayloadSchemaUrl(payloadSchemaUrl);

        // Populate the implemented SECOM interfaces
        SecomInterfaces secomInterfaces = new SecomInterfaces();
        secomInterfaces.setGet(true);
        secomInterfaces.setGetSummary(true);
        capabilityResponseObject.setImplementedInterfaces(secomInterfaces);

        // And return the Capability Response Object
        return ResponseEntity.ok()
                .body(capabilityResponseObject);
    }

    /**
     * This helper function is to be used to implement the SECOM exchange
     * metadata population operation, by acquiring a signature for the
     * provided payload.
     *
     * @param payload the payload to be signed
     * @return the service exchange metadata with the signature information
     */
    private ServiceExchangeMetadata signPayload(String payload) {
        final ServiceExchangeMetadata serviceExchangeMetadata = new ServiceExchangeMetadata();
        serviceExchangeMetadata.setDataProtection(false);
        return serviceExchangeMetadata;
    }

    /**
     * A helper function to simplify the joining of geometries without troubling
     * ourselves for the null checking... which is a pain.
     *
     * @param a the first geometry to be joined
     * @param b the second geometry to be joined
     * @return the joined geometry
     */
    private Geometry joinGeometries(Geometry a, Geometry b, boolean intersection) {
        if(a == null && b == null) {
            return null;
        } else if(a == null || b == null) {
            return Optional.ofNullable(a).orElse(b);
        } else {
            // For intersection handle differently
            if(intersection) {
                return a.intersection(b);
            }
            // Otherwise, add up the two geometies
            return this.geometryFactory.createGeometryCollection(new Geometry[]{a, b});
        }
    }
}
