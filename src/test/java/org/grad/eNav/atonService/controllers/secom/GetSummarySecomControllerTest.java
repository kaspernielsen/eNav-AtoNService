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

import org.grad.eNav.atonService.TestFeignSecurityConfig;
import org.grad.eNav.atonService.TestingConfiguration;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.models.domain.s125.S125DataSetIdentification;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.secom.core.models.GetSummaryResponseObject;
import org.grad.secom.core.models.enums.ContainerTypeEnum;
import org.grad.secom.core.models.enums.InfoStatusEnum;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

import static org.grad.secom.core.interfaces.GetSummarySecomInterface.GET_SUMMARY_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class GetSummarySecomControllerTest {

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
        this.s125DataSet.setUuid(UUID.randomUUID());
        this.s125DataSet.setCreatedAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        this.s125DataSet.setLastUpdatedAt(LocalDateTime.of(2000, 1, 1, 0, 0, 1));
        this.s125DataSet.setGeometry(this.geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(-180, -90),
                new Coordinate(-180, 90),
                new Coordinate(180, 90),
                new Coordinate(180, -90),
                new Coordinate(-180, -90),
        }));

        // Also set some dataset identification information
        S125DataSetIdentification s125DataSetIdentification = new S125DataSetIdentification();
        s125DataSetIdentification.setDatasetTitle("Test S-125 Dataset Title");
        s125DataSetIdentification.setProductEdition("0.0.0test");
        s125DataSetIdentification.setDatasetFileIdentifier("test_s-125_dataset");
        s125DataSetIdentification.setDatasetAbstract("A random abstract for the test dataset");
        this.s125DataSet.setDatasetIdentificationInformation(s125DataSetIdentification);
    }

    /**
     * Test that the SECOM Get Summary interface is configured properly
     * and returns the expected Get Summary Response Object output.
     */
    @Test
    void testGetSummary() {
        doReturn(new PageImpl<>(Collections.singletonList(this.s125DataSet), Pageable.ofSize(this.queryPageSize), 1))
                .when(this.datasetService).findAll(any(), any(), any(), any(), any());
        doReturn(0L).when(this.aidsToNavigationService).findAllTotalCount(any(), any(), any(), any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_SUMMARY_INTERFACE_PATH)
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
                .expectBody(GetSummaryResponseObject.class)
                .consumeWith(response -> {
                    GetSummaryResponseObject getSummaryResponseObject = response.getResponseBody();
                    assertNotNull(getSummaryResponseObject);
                    assertNotNull(getSummaryResponseObject.getSummaryObject());
                    assertEquals(1, getSummaryResponseObject.getSummaryObject().size());
                    assertEquals(ContainerTypeEnum.S100_DataSet, getSummaryResponseObject.getSummaryObject().get(0).getContainerType());
                    assertEquals(SECOM_DataProductType.S125, getSummaryResponseObject.getSummaryObject().get(0).getDataProductType());
                    assertEquals(Boolean.FALSE, getSummaryResponseObject.getSummaryObject().get(0).getDataCompression());
                    assertEquals(Boolean.FALSE, getSummaryResponseObject.getSummaryObject().get(0).getDataProtection());
                    assertEquals(this.s125DataSet.getUuid(), getSummaryResponseObject.getSummaryObject().get(0).getDataReference());
                    assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getProductEdition(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_productVersion());
                    assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetFileIdentifier(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_identifier());
                    assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetTitle(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_name());
                    assertEquals(InfoStatusEnum.PRESENT.getValue(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_status());
                    assertEquals(this.s125DataSet.getDatasetIdentificationInformation().getDatasetAbstract(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_description());
                    assertEquals(this.s125DataSet.getLastUpdatedAt(), getSummaryResponseObject.getSummaryObject().get(0).getInfo_lastModifiedDate());
                    assertNotNull(getSummaryResponseObject.getPagination());
                    assertEquals(Integer.MAX_VALUE, getSummaryResponseObject.getPagination().getMaxItemsPerPage());
                    assertEquals(0, getSummaryResponseObject.getPagination().getTotalItems());
                });
    }

    /**
     * Test that the SECOM Get Summary interface will return an HTTP Status
     * BAD_REQUEST if one of the provided query parameters is not formatted
     * properly
     */
    @Test
    void testGetSummaryBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_SUMMARY_INTERFACE_PATH)
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
     * Test that the SECOM Get Summary interface will return an HTTP Status
     * METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testGetSummaryMethodNotAllowed() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_SUMMARY_INTERFACE_PATH)
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