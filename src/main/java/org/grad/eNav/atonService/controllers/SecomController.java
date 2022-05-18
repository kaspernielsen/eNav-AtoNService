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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.models.dtos.secom.DataTypeEnum;
import org.grad.eNav.atonService.models.dtos.secom.ExchangeMetadata;
import org.grad.eNav.atonService.models.dtos.secom.GetMessageResponseObject;
import org.grad.eNav.atonService.models.dtos.secom.PaginationObject;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.util.Optional;

@RestController
@RequestMapping("/api/secom/v1/dataset")
@Slf4j
public class SecomController {

    /**
     * The Object Mapper.
     */
    @Autowired
    ObjectMapper objectMapper;

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
     * GET /api/secom/v1/dataset : Returns the S-125 dataset entries as
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
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetMessageResponseObject> getDataset(@RequestParam("dataReference") Optional<String> dataReference,
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

        // Handle the input conditions
        final S125DataSet s125DataSet = this.datasetService.findOne(new BigInteger(dataReference.get()));
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(Optional.empty(), Optional.ofNullable(s125DataSet).map(S125DataSet::getGeometry), pageable);
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(modelMapper);
        final DataSet dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());

        // Start building the response
        final GetMessageResponseObject getMessageResponseObject = new GetMessageResponseObject();

        // Now handle according to the data type
        switch (dataType.orElse(DataTypeEnum.S100_DataSet)) {
            case S100_DataSet:
            default:
                try {
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
}
