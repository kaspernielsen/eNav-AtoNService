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
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.SecomService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.interfaces.jaxrs.CapabilitySecomInterface;
import org.grad.secom.models.CapabilityObject;
import org.grad.secom.models.CapabilityResponseObject;
import org.grad.secom.models.ImplementedInterfaces;
import org.grad.secom.models.enums.ContainerTypeEnum;
import org.grad.secom.models.enums.SECOM_DataProductType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.ws.rs.Path;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

/**
 * The SECOM Capability Interface Controller.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Path("/")
@Validated
@Slf4j
public class CapabilitySecomController implements CapabilitySecomInterface {

    /**
     * The Application Version Information.
     */
    @Value("${gla.rad.aton-service.info.version:0.0.0}")
    private String appVersion;

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
    private String dataProductSchemaLocation;

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

    /**
     * The SECOM Service.
     */
    @Autowired
    SecomService secomService;

    /**
     * GET /api/secom/v1/capability : Returns the service instance capabilities.
     *
     * @return the SECOM-compliant service capabilities
     */
    @Tag(name = "SECOM")
    @Transactional
    public CapabilityResponseObject capability() {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        final URL productSchemaUrl = Optional.of(this.dataProductSchemaLocation)
                .map(l -> l.startsWith("http") ? l :(baseUrl + l) )
                .map(l -> { try { return new URL(l); } catch (MalformedURLException ex) { return null; } })
                .orElse(null);

        // Populate the implemented SECOM interfaces
        ImplementedInterfaces implementedInterfaces = new ImplementedInterfaces();
        implementedInterfaces.setGet(true);
        implementedInterfaces.setGetSummary(true);
        implementedInterfaces.setSubscription(true);

        // Start building the capability entry
        CapabilityObject capabilityObject = new CapabilityObject();
        capabilityObject.setContainerType(ContainerTypeEnum.S100_DataSet);
        capabilityObject.setDataProductType(SECOM_DataProductType.S125);
        capabilityObject.setProductSchemaUrl(productSchemaUrl);
        capabilityObject.setImplementedInterfaces(implementedInterfaces);

        // Start building the capability response
        CapabilityResponseObject capabilityResponseObject = new CapabilityResponseObject();
        capabilityResponseObject.setCapability(Collections.singletonList(capabilityObject));
        capabilityObject.setServiceVersion(this.appVersion);

        // And return the Capability Response Object
        return capabilityResponseObject;
    }

}
