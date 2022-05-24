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
import org.grad.secom.controllers.CapabilityInterface;
import org.grad.secom.controllers.GetInterface;
import org.grad.secom.controllers.GetSummaryInterface;
import org.grad.secom.models.*;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secom/v1")
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
     * GET /api/secom/v1/dataset/summary : Returns the S-125 dataset summary as
     * specified by the SECOM standard.
     *
     * @param dataReference the dataset data reference
     * @param dataType the dataset data type
     * @param productSpecification the dataset product specification
     * @param geometry the dataset geometry
     * @param areaName the dataset area name
     * @param unlocode the dataset entries UNLOCODE
     * @param fromTime the dataset entries from time
     * @param toTime the dataset enries to time
     * @param pageable the pageable information
     * @return the SECOM-complianet dataset information
     */
    @GetMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetMessageResponse> getObject(@RequestParam("dataReference") Optional<String> dataReference,
                                                        @RequestParam("dataType") Optional<DataTypeEnum> dataType,
                                                        @RequestParam("productSpecification") Optional<String> productSpecification,
                                                        @RequestParam("geometry") Optional<String> geometry,
                                                        @RequestParam("areaName") Optional<String> areaName,
                                                        @RequestParam("unlocode") Optional<String> unlocode,
                                                        @RequestParam("fromTime") Optional<String> fromTime,
                                                        @RequestParam("toTime") Optional<String> toTime,
                                                        Pageable pageable) {
        this.log.debug("SECOM request to get page of Dataset");
        dataReference.ifPresent(v -> this.log.debug("Data Reference specified as: {}", dataReference));
        dataType.ifPresent(v -> this.log.debug("Data Type specified as: {}", dataType));
        productSpecification.ifPresent(v -> this.log.debug("Product Specification specified as: {}", productSpecification));
        geometry.ifPresent(v -> this.log.debug("Geometry specified as: {}", geometry));
        areaName.ifPresent(v -> this.log.debug("Area Name specified as: {}", areaName));
        unlocode.ifPresent(v -> this.log.debug("UNLOCODE specified as: {}", unlocode));
        fromTime.ifPresent(v -> this.log.debug("From time specified as: {}", fromTime));
        toTime.ifPresent(v -> this.log.debug("To time specified as: {}", toTime));

        // Parse the arguments
        Geometry jtsGeometry = null;
        LocalDateTime fromLocalDateTime = null;
        LocalDateTime toLocalDateTime = null;
        if(geometry.isPresent()) {
            try {
                jtsGeometry = WKTUtil.convertWKTtoGeometry(geometry.get());
            } catch (ParseException e) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createFailureAlert("dataset","geometry","Could not parse WKT geometry"))
                        .build();
            }
        }
        if(unlocode.isPresent()) {
            jtsGeometry = this.joinGeometries(jtsGeometry, unlocode.map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)));
        }
        if(areaName.isPresent()) {
            jtsGeometry = this.joinGeometries(jtsGeometry, null);
        }
        if(fromTime.isPresent()) {
            try {
                fromLocalDateTime = LocalDateTime.parse(fromTime.get(), this.dateFormat);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.get","fromTime"))
                        .build();
            }
        }
        if(toTime.isPresent()) {
            try {
                toLocalDateTime = LocalDateTime.parse(toTime.get(), this.dateFormat);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.get","toTime"))
                        .build();
            }
        }

        // Handle the input request
        final S125DataSet s125DataSet = this.datasetService.findOne(new BigInteger(dataReference.get()));
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(
                null,
                this.joinGeometries(s125DataSet.getGeometry(), jtsGeometry),
                fromLocalDateTime,
                toLocalDateTime,
                pageable
        );

        // Start building the response
        final GetMessageResponse getMessageResponseObject = new GetMessageResponse();

        // Now handle according to the data type
        switch (dataType.orElse(DataTypeEnum.S100_DataSet)) {
            case S100_DataSet:
            default:
                try {
                    final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(modelMapper);
                    final DataSet dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());
                    getMessageResponseObject.setPayload(S125Utils.marshalS125(dataset, Boolean.FALSE));
                } catch (JAXBException ex) {
                    this.log.error(ex.getMessage());
                }
                break;
        }

        // Now the exchange metadata
        getMessageResponseObject.setExchangeMetadata(this.signPayload(getMessageResponseObject.getPayload()));

        // Now handle the pagination
        getMessageResponseObject.setPagination(new PaginationObject((int)atonPage.getTotalElements(), atonPage.getSize()));

        // And return the Get Message Response Object
        return ResponseEntity.ok()
                .body(getMessageResponseObject);

    }

    /**
     * GET /api/secom/v1/dataset : Returns the S-125 dataset entries as
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
     */
    @GetMapping(value = "/dataset/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetSummaryResponse> getSummary(@RequestParam("dataType") Optional<DataTypeEnum> dataType,
                                                         @RequestParam("productSpecification") Optional<String> productSpecification,
                                                         @RequestParam("geometry") Optional<String> geometry,
                                                         @RequestParam("areaName") Optional<String> areaName,
                                                         @RequestParam("unlocode") Optional<String> unlocode,
                                                         @RequestParam("fromTime") Optional<String> fromTime,
                                                         @RequestParam("toTime") Optional<String> toTime,
                                                         Pageable pageable) {
        this.log.debug("SECOM request to get page of Dataset Summary");
        dataType.ifPresent(v -> this.log.debug("Data Type specified as: {}", dataType));
        productSpecification.ifPresent(v -> this.log.debug("Product Specification specified as: {}", productSpecification));
        geometry.ifPresent(v -> this.log.debug("Geometry specified as: {}", geometry));
        areaName.ifPresent(v -> this.log.debug("Area Name specified as: {}", areaName));
        unlocode.ifPresent(v -> this.log.debug("UNLOCODE specified as: {}", unlocode));
        fromTime.ifPresent(v -> this.log.debug("From time specified as: {}", fromTime));
        toTime.ifPresent(v -> this.log.debug("To time specified as: {}", toTime));

        // Parse the arguments
        Geometry jtsGeometry = null;
        LocalDateTime fromLocalDateTime = null;
        LocalDateTime toLocalDateTime = null;
        if(geometry.isPresent()) {
            try {
                jtsGeometry = WKTUtil.convertWKTtoGeometry(geometry.get());
            } catch (ParseException e) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.getsummary","geometry"))
                        .build();
            }
        }
        if(unlocode.isPresent()) {
            jtsGeometry = this.joinGeometries(jtsGeometry, unlocode.map(this.unLoCodeService::getUnLoCodeMapEntry)
                    .map(UnLoCodeMapEntry::getGeometry)
                    .orElseGet(() -> this.geometryFactory.createEmpty(0)));
        }
        if(areaName.isPresent()) {
            jtsGeometry = this.joinGeometries(jtsGeometry, null);
        }
        if(fromTime.isPresent()) {
            try {
                fromLocalDateTime = LocalDateTime.parse(fromTime.get(), this.dateFormat);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.getsummary","fromTime"))
                        .build();
            }
        }
        if(toTime.isPresent()) {
            try {
                toLocalDateTime = LocalDateTime.parse(toTime.get(), this.dateFormat);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest()
                        .headers(HeaderUtil.createAlert("atonservice.dataset.getsummary","toTime"))
                        .build();
            }
        }

        // Handle the input request
        final Page<S125DataSet> s125Datasets = this.datasetService.findAll(
                null,
                jtsGeometry,
                fromLocalDateTime,
                toLocalDateTime,
                pageable
        );

        // Start building the response
        final GetSummaryResponse getSummaryResponseObject = new GetSummaryResponse();

        // Now handle according to the data type
        switch (dataType.orElse(DataTypeEnum.S100_DataSet)) {
            case S100_DataSet:
            default:
                getSummaryResponseObject.setInformationSummary(s125Datasets.stream()
                        .map(s125Dataset -> {
                            InformationSummary informationSummaryObject = new InformationSummary();
                            informationSummaryObject.setMessageIdentifier(s125Dataset.getDatasetIdentificationInformation().getDatasetFileIdentifier());
                            informationSummaryObject.setIdentifier(s125Dataset.getId().toString());
                            informationSummaryObject.setName(s125Dataset.getDatasetIdentificationInformation().getDatasetTitle());
                            informationSummaryObject.setDescription(s125Dataset.getDatasetIdentificationInformation().getDatasetAbstract());
                            informationSummaryObject.setStatusEnum("present");
                            return informationSummaryObject;
                        })
                        .collect(Collectors.toList()));
                break;
        }

        // Now handle the pagination
        getSummaryResponseObject.setPagination(new PaginationObject((int)s125Datasets.getTotalElements(), s125Datasets.getSize()));

        // And return the Get Message Response Object
        return ResponseEntity.ok()
                .body(getSummaryResponseObject);
    }

    /**
     * GET /api/secom/v1/capability : Returns the service instance capabilities.
     *
     * @return the SECOM-compliant service capabilities
     */
    @GetMapping(value = "/capability", produces = MediaType.APPLICATION_JSON_VALUE)
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
     * @return the exchange metadata with the signature information
     */
    private ExchangeMetadata signPayload(String payload) {
        final ExchangeMetadata exchangeMetadata = new ExchangeMetadata();
        exchangeMetadata.setDataProtection(false);
        return exchangeMetadata;
    }

    /**
     * A helper function to simplify the joining of geometries without troubling
     * ourselves for the null checking... which is a pain.
     *
     * @param a the first geometry to be joined
     * @param b the second geometry to be joined
     * @return the joined geometry
     */
    private Geometry joinGeometries(Geometry a, Geometry b) {
        if(a == null && b == null) {
            return null;
        } else if(a == null || b == null) {
            return Optional.ofNullable(a).orElse(b);
        } else {
            return a.intersection(b);
        }
    }
}
