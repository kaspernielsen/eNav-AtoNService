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
import org.grad.eNav.atonService.TestFeignSecurityConfig;
import org.grad.eNav.atonService.TestingConfiguration;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.models.GetResponseObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.geomesa.utils.interop.WKTUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.xml.bind.JAXBException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.grad.secom.core.interfaces.GetSecomInterface.GET_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class GetSecomControllerTest {

    /**
     * The Reactive Web Test Client.
     */
    @Autowired
    WebTestClient webTestClient;

    /**
     * The Dataset Service mock.
     */
    @MockBean
    DatasetService datasetService;

    /**
     * The Aids to Navigation Service mock.
     */
    @MockBean
    AidsToNavigationService aidsToNavigationService;

    /**
     * The UN/LOCODE Service mock.
     */
    @MockBean
    UnLoCodeService unLoCodeService;


    // Test Variables
    private GeometryFactory geometryFactory;
    private UUID queryDataReference;
    private ContainerTypeEnum queryContainerType;
    private SECOM_DataProductType queryDataProductType;
    private String queryProductVersion;
    private String queryGeometry;
    private String queryUnlocode;
    private LocalDateTime queryValidFrom;
    private LocalDateTime queryValidTo;
    private Integer queryPage;
    private Integer queryPageSize;
    private S125DataSet s125DataSet;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Setup the query arguments
        this.geometryFactory = new GeometryFactory(new PrecisionModel(),4326);
        this.queryDataReference = UUID.randomUUID();
        this.queryContainerType = ContainerTypeEnum.S100_DataSet;
        this.queryDataProductType = SECOM_DataProductType.S125;
        this.queryProductVersion = "0.0.1";
        this.queryGeometry = WKTUtils.write(this.geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(-180, -90),
                new Coordinate(-180, 90),
                new Coordinate(180, 90),
                new Coordinate(180, -90),
                new Coordinate(-180, -90),
        }));
        this.queryUnlocode = "ADALV";
        this.queryValidFrom = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        this.queryValidTo = LocalDateTime.of(3000, 1, 1, 0, 0, 0);
        this.queryPage = 0;
        this.queryPageSize = Integer.MAX_VALUE;

        // Construct a test S-125 Dataset
        this.s125DataSet = new S125DataSet("125Dataset");
        this.s125DataSet.setUuid(this.queryDataReference);
        this.s125DataSet.setGeometry(this.geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(-180, -90),
                new Coordinate(-180, 90),
                new Coordinate(180, 90),
                new Coordinate(180, -90),
                new Coordinate(-180, -90),
        }));
    }

    /**
     * Test that the SECOM Get interface is configured properly and returns the
     * expected Get Response Object output.
     */
    @Test
    void testGetSummary() {
        doReturn(this.s125DataSet).when(this.datasetService).findOne(any());
        doReturn(new PageImpl<>(Collections.emptyList(), Pageable.ofSize(1), 0))
                .when(this.aidsToNavigationService).findAll(any(), any(), any(), any(), any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(GetResponseObject.class)
                .consumeWith(response -> {
                    GetResponseObject getResponseObject = response.getResponseBody();
                    assertNotNull(getResponseObject);
                    assertNotNull(getResponseObject.getDataResponseObject());
                    assertNotNull(1, getResponseObject.getDataResponseObject().getData());
                    assertNotNull(getResponseObject.getPagination());
                    assertNotNull(getResponseObject.getDataResponseObject().getExchangeMetadata());
                    assertEquals(Integer.MAX_VALUE, getResponseObject.getPagination().getMaxItemsPerPage());
                    assertEquals(0, getResponseObject.getPagination().getTotalItems());

                    // Try to parse the incoming data
                    String s125Xml = new String(Base64.getDecoder().decode(getResponseObject.getDataResponseObject().getData()));
                    try {
                        DataSet result = S125Utils.unmarshallS125(s125Xml);
                        assertNotNull(result);
                        assertNotNull(result.getDatasetStructureInformation());
                        assertNotNull(result.getDatasetIdentificationInformation());
                        assertNotNull(result.getId());
                        assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetTitle(), result.getDatasetIdentificationInformation().getDatasetTitle());
                        assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetFileIdentifier(), result.getDatasetIdentificationInformation().getDatasetFileIdentifier());
                        assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getProductEdition(), result.getDatasetIdentificationInformation().getProductEdition());
                        assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetTitle(), result.getDatasetIdentificationInformation().getDatasetTitle());
                    } catch (JAXBException ex) {
                        fail(ex);
                    }
                });
    }

    /**
     * Test that the SECOM Get interface will return an HTTP Status NOT_FOUND
     * if the dataset reference points to an unknown dataset.
     */
    @Test
    void testGetNotFound() {
        doThrow(DataNotFoundException.class).when(this.datasetService).findOne(any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Test that the SECOM Get interface will return an HTTP Status BAD_REQUEST
     * if one of the provided query parameters is not formatted properly
     */
    @Test
    void testGetBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", "Badly Formatted Date")
                        .queryParam("validTo", "Another Badly Formatted Date")
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Get interface will return an HTTP Status
     * METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testGetMethodNotAllowed() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ISO_DATE_TIME.format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }
}