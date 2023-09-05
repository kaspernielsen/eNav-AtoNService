/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.controllers.secom;

import _int.iala_aism.s125.gml._0_0.Dataset;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.JAXBException;
import org.grad.eNav.atonService.TestFeignSecurityConfig;
import org.grad.eNav.atonService.TestingConfiguration;
import org.grad.eNav.atonService.components.SecomCertificateProviderImpl;
import org.grad.eNav.atonService.components.SecomSignatureProviderImpl;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.grad.eNav.atonService.services.secom.SecomSubscriptionService;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.base.DigitalSignatureCertificate;
import org.grad.secom.core.base.SecomConstants;
import org.grad.secom.core.components.SecomSignatureFilter;
import org.grad.secom.core.exceptions.SecomValidationException;
import org.grad.secom.core.models.*;
import org.grad.secom.core.models.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.locationtech.geomesa.utils.interop.WKTUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.grad.secom.core.base.SecomConstants.SECOM_DATE_TIME_FORMAT;
import static org.grad.secom.core.base.SecomConstants.SECOM_DATE_TIME_FORMATTER;
import static org.grad.secom.core.interfaces.AcknowledgementSecomInterface.ACKNOWLEDGMENT_INTERFACE_PATH;
import static org.grad.secom.core.interfaces.CapabilitySecomInterface.CAPABILITY_INTERFACE_PATH;
import static org.grad.secom.core.interfaces.GetSecomInterface.GET_INTERFACE_PATH;
import static org.grad.secom.core.interfaces.GetSummarySecomInterface.GET_SUMMARY_INTERFACE_PATH;
import static org.grad.secom.core.interfaces.RemoveSubscriptionSecomInterface.REMOVE_SUBSCRIPTION_INTERFACE_PATH;
import static org.grad.secom.core.interfaces.SubscriptionSecomInterface.SUBSCRIPTION_INTERFACE_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@Import({TestingConfiguration.class, TestFeignSecurityConfig.class})
class SecomControllerTest {

    /**
     * The Reactive Web Test Client.
     */
    @Autowired
    WebTestClient webTestClient;

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Dataset Service mock.
     */
    @MockBean
    DatasetService datasetService;

    /**
     * The UN/LOCODE Service mock.
     */
    @MockBean
    UnLoCodeService unLoCodeService;

    /**
     * The SECOM Subscription Service mock.
     */
    @MockBean
    SecomSubscriptionService secomSubscriptionService;

    /**
     * The Secom Certificate Provider mock.
     */
    @MockBean
    SecomCertificateProviderImpl secomCertificateProvider;

    /**
     * The Secom Signature Provider mock.
     */
    @MockBean
    SecomSignatureProviderImpl secomSignatureProvider;

    /**
     * The Secom Signature Filter mock.
     *
     * This will block the actual SECOM signature verification process and will
     * allow out test messages to go through the signature filter without valid
     * signatures.
     */
    @MockBean
    SecomSignatureFilter secomSignatureFilter;

    // Test Variables
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
    private S125Dataset s125DataSet;
    private String s125DataSetAsXml;
    private DatasetContent datasetContent;
    private SubscriptionRequest subscriptionRequest;
    private SubscriptionRequest savedSubscriptionRequest;
    private RemoveSubscriptionObject removeSubscriptionObject;
    private AcknowledgementObject acknowledgementObject;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() throws JAXBException {
        // Setup the query arguments
        // Test Variables
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.queryDataReference = UUID.randomUUID();
        this.queryContainerType = ContainerTypeEnum.S100_DataSet;
        this.queryDataProductType = SECOM_DataProductType.S125;
        this.queryProductVersion = "0.0.1";
        this.queryGeometry = WKTUtils.write(geometryFactory.createPolygon(new Coordinate[]{
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
        this.s125DataSet = new S125Dataset("125Dataset");
        this.s125DataSet.setUuid(this.queryDataReference);
        this.s125DataSet.setGeometry(geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(-180, -90),
                new Coordinate(-180, 90),
                new Coordinate(180, 90),
                new Coordinate(180, -90),
                new Coordinate(-180, -90),
        }));

        // Marshal the dataset content
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
        final Dataset dataset = s125DatasetBuilder.packageToDataset(this.s125DataSet, Collections.emptyList());
        this.s125DataSetAsXml = S125Utils.marshalS125(dataset, Boolean.FALSE);
        this.datasetContent = new DatasetContent();
        this.datasetContent.setId(BigInteger.ONE);
        this.datasetContent.setGeneratedAt(LocalDateTime.now());
        this.datasetContent.setContent(this.s125DataSetAsXml);
        this.datasetContent.setContentLength(BigInteger.valueOf(this.s125DataSetAsXml.length()));
        this.s125DataSet.setDatasetContent(this.datasetContent);

        // Setup the subscription requests and responses
        this.subscriptionRequest = new SubscriptionRequest();
        this.subscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.subscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
        this.savedSubscriptionRequest = new SubscriptionRequest();
        this.savedSubscriptionRequest.setUuid(UUID.randomUUID());
        this.savedSubscriptionRequest.setContainerType(ContainerTypeEnum.S100_DataSet);
        this.savedSubscriptionRequest.setDataProductType(SECOM_DataProductType.S125);
        this.removeSubscriptionObject = new RemoveSubscriptionObject();
        this.removeSubscriptionObject.setSubscriptionIdentifier(UUID.randomUUID());

        // Setup an acknowledgement
        this.acknowledgementObject = new AcknowledgementObject();
        EnvelopeAckObject envelopeAckObject = new EnvelopeAckObject();
        envelopeAckObject.setCreatedAt(LocalDateTime.now());
        envelopeAckObject.setTransactionIdentifier(UUID.randomUUID());
        envelopeAckObject.setAckType(AckTypeEnum.DELIVERED_ACK);
        envelopeAckObject.setEnvelopeSignatureCertificate("Signature Certificate");
        envelopeAckObject.setEnvelopeRootCertificateThumbprint("Root Certificate Thumbprint");
        envelopeAckObject.setEnvelopeSignatureTime(LocalDateTime.now());
        this.acknowledgementObject.setEnvelope(envelopeAckObject);
        this.acknowledgementObject.setEnvelopeSignature("Envelope Signature");
    }

    /**
     * Test that the SECOM Capability interface is configured properly and
     * returns the expected Capability Response Object output.
     */
    @Test
    void testCapability() {
        webTestClient.get()
                .uri("/api/secom" + CAPABILITY_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CapabilityResponseObject.class)
                .consumeWith(response -> {
                    CapabilityResponseObject capabilityResponseObject = response.getResponseBody();
                    assertNotNull(capabilityResponseObject);
                    assertNotNull(capabilityResponseObject.getCapability());
                    assertFalse(capabilityResponseObject.getCapability().isEmpty());
                    assertEquals(1, capabilityResponseObject.getCapability().size());
                    assertEquals(ContainerTypeEnum.S100_DataSet, capabilityResponseObject.getCapability().get(0).getContainerType());
                    assertEquals(SECOM_DataProductType.S125, capabilityResponseObject.getCapability().get(0).getDataProductType());
                    assertEquals("/xsd/S125.xsd", capabilityResponseObject.getCapability().get(0).getProductSchemaUrl().getPath());
                    assertEquals("0.0.0", capabilityResponseObject.getCapability().get(0).getServiceVersion());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getAccess());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getEncryptionKey());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGet());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getSubscription());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGetByLink());
                    assertTrue(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getGetSummary());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getUpload());
                    assertFalse(capabilityResponseObject.getCapability().get(0).getImplementedInterfaces().getUploadLink());
                });
    }

    /**
     * Test that the SECOM Capability interface will respond with an HTTP
     * Status METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testCapabilityMethodNotAllowed() {
        webTestClient.post()
                .uri("/api/secom" + CAPABILITY_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Test that the SECOM Get Summary interface is configured properly
     * and returns the expected Get Summary Response Object output.
     */
    @Test
    void testGetSummary() {
        doReturn(new PageImpl<>(Collections.singletonList(this.s125DataSet), Pageable.ofSize(this.queryPageSize), 1))
                .when(this.datasetService).findAll(any(), any(), any(), any(), any(), any());

         webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_SUMMARY_INTERFACE_PATH)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
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
                    assertEquals(1, getSummaryResponseObject.getPagination().getTotalItems());
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
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
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
                        .queryParam("validFrom", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Test that the SECOM Get interface is configured properly and returns the
     * expected Get Response Object output.
     */
    @Test
    void testGet() throws CertificateEncodingException, IOException {
        // Mock the SECOM library certificate and signature providers
        X509Certificate mockCertificate = mock(X509Certificate.class);
        doReturn("certificate".getBytes()).when(mockCertificate).getEncoded();
        PublicKey mockPublicKey = mock(PublicKey.class);
        doReturn(mockPublicKey).when(mockCertificate).getPublicKey();
        X509Certificate mockRootCertificate = mock(X509Certificate.class);
        doReturn("rootCertificate".getBytes()).when(mockRootCertificate).getEncoded();
        DigitalSignatureCertificate digitalSignatureCertificate = new DigitalSignatureCertificate();
        digitalSignatureCertificate.setCertificateAlias("secom");
        digitalSignatureCertificate.setCertificate(mockCertificate);
        digitalSignatureCertificate.setPublicKey(mockPublicKey);
        digitalSignatureCertificate.setRootCertificate(mockRootCertificate);
        doReturn(digitalSignatureCertificate).when(this.secomCertificateProvider).getDigitalSignatureCertificate();
        doReturn(DigitalSignatureAlgorithmEnum.ECDSA).when(this.secomSignatureProvider).getSignatureAlgorithm();
        doReturn("signature".getBytes()).when(this.secomSignatureProvider).generateSignature(any(), any(), any());

        // Mock the rest
        doReturn(new PageImpl<>(Collections.singletonList(this.s125DataSet), Pageable.ofSize(this.queryPageSize), 1))
                .when(this.datasetService).findAll(any(), any(), any(), any(), any(), any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidFrom))
                        .queryParam("validTo", DateTimeFormatter.ofPattern(SECOM_DATE_TIME_FORMAT).format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isOk()
                .expectBody(GetResponseObject.class)
                .consumeWith(response -> {
                    GetResponseObject getResponseObject = response.getResponseBody();
                    assertNotNull(getResponseObject);
                    assertNotNull(getResponseObject.getDataResponseObject());
                    assertNotNull(getResponseObject.getPagination());
                    assertEquals(1, getResponseObject.getDataResponseObject().size());
                    assertNotNull(getResponseObject.getDataResponseObject().get(0));
                    assertNotNull(getResponseObject.getDataResponseObject().get(0).getData());
                    assertNotNull(getResponseObject.getDataResponseObject().get(0).getExchangeMetadata());
                    assertEquals(Boolean.FALSE, getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDataProtection());
                    assertEquals(Boolean.FALSE, getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getCompressionFlag());
                    assertEquals(SecomConstants.SECOM_PROTECTION_SCHEME, getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getProtectionScheme());
                    assertEquals(DigitalSignatureAlgorithmEnum.ECDSA, getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDigitalSignatureReference());
                    assertNotNull(getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDigitalSignatureValue());
                    assertEquals(DatatypeConverter.printHexBinary("signature".getBytes()), getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDigitalSignatureValue().getDigitalSignature());
                    assertEquals(Base64.getEncoder().encodeToString("certificate".getBytes()), getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDigitalSignatureValue().getPublicCertificate());
                    assertEquals("a79fd87b7e6418a5085f88c21482e017eb0ef9a6", getResponseObject.getDataResponseObject().get(0).getExchangeMetadata().getDigitalSignatureValue().getPublicRootCertificateThumbprint());
                    assertEquals(Integer.MAX_VALUE, getResponseObject.getPagination().getMaxItemsPerPage());
                    assertEquals(1, getResponseObject.getPagination().getTotalItems());

                    // Try to parse the incoming data
                    String s125Xml = new String(Base64.getDecoder().decode(getResponseObject.getDataResponseObject().get(0).getData()));
                    try {
                        Dataset result = S125Utils.unmarshallS125(s125Xml);
                        assertNotNull(result);
                        assertNotNull(result.getId());
                        assertNotNull(result.getDatasetIdentificationInformation());
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
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Get interface will return an HTTP Status
     * METHOD_NOT_ALLOWED if a method other than a GET is requested.
     */
    @Test
    void testGetMethodNotAllowed() {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/secom" + GET_INTERFACE_PATH)
                        .queryParam("dataReference", this.queryDataReference)
                        .queryParam("containerType", this.queryContainerType.getValue())
                        .queryParam("dataProductType", this.queryDataProductType)
                        .queryParam("productVersion", this.queryProductVersion)
                        .queryParam("geometry", this.queryGeometry)
                        .queryParam("unlocode", this.queryUnlocode)
                        .queryParam("validFrom", SECOM_DATE_TIME_FORMATTER.format(this.queryValidFrom))
                        .queryParam("validTo", SECOM_DATE_TIME_FORMATTER.format(this.queryValidTo))
                        .queryParam("page", this.queryPage)
                        .queryParam("pageSize", this.queryPageSize)
                        .build())
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Test that the SECOM Subscription interface is configured properly and
     * returns the expected Subscription Response Object output.
     */
    @Test
    void testSubscription() {
        doReturn(savedSubscriptionRequest).when(this.secomSubscriptionService).save(any(), any());

        webTestClient.post()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(subscriptionRequest), SubscriptionRequest.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponseObject.class)
                .consumeWith(response -> {
                    SubscriptionResponseObject subscriptionResponseObject = response.getResponseBody();
                    assertNotNull(subscriptionResponseObject);
                    assertEquals(savedSubscriptionRequest.getUuid(), subscriptionResponseObject.getSubscriptionIdentifier());
                    assertEquals("Subscription successfully created", subscriptionResponseObject.getResponseText());
                });
    }

    /**
     * Test that the SECOM Subscription interface will return an HTTP Status
     * BAD_REQUEST if a validation error occurs.
     */
    @Test
    void testSubscriptionBadRequest() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any(), any());

        webTestClient.post()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(subscriptionRequest), SubscriptionRequest.class))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Subscription interface will return an HTTP Status
     * METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testSubscriptionMethodNotAllowed() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any(), any());

        webTestClient.get()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Test that the SECOM Remove Subscription interface is configured properly
     * and returns the expected Remove Subscription Response Object output.
     */
    @Test
    void testRemoveSubscription() {
        doReturn(removeSubscriptionObject.getSubscriptionIdentifier()).when(this.secomSubscriptionService).delete(any());

        webTestClient.method(HttpMethod.DELETE)
                .uri("/api/secom" + REMOVE_SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(removeSubscriptionObject), RemoveSubscriptionObject.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody(RemoveSubscriptionResponseObject.class)
                .consumeWith(response -> {
                    RemoveSubscriptionResponseObject removeSubscriptionResponseObject = response.getResponseBody();
                    assertNotNull(removeSubscriptionResponseObject);
                    assertEquals(String.format("Subscription %s removed", removeSubscriptionObject.getSubscriptionIdentifier()), removeSubscriptionResponseObject.getResponseText());
                });
    }

    /**
     * Test that the SECOM Remove Subscription interface will return an HTTP
     * Status BAD_REQUEST if a validation error occurs.
     */
    @Test
    void testRemoveSubscriptionBadRequest() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any(), any());

        webTestClient.post()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(removeSubscriptionObject), RemoveSubscriptionObject.class))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Remove Subscription interface will return an HTTP
     * Status METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testRemoveSubscriptionMethodNotAllowed() {
        doThrow(SecomValidationException.class).when(this.secomSubscriptionService).save(any(), any());

        webTestClient.get()
                .uri("/api/secom" + SUBSCRIPTION_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Test that the SECOM Acknowledgement interface is configured properly
     * and returns the expected Acknowledgement Response Object output.
     */
    @Test
    void testAcknowledgement() {
        webTestClient.method(HttpMethod.POST)
                .uri("/api/secom" + ACKNOWLEDGMENT_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(acknowledgementObject), AcknowledgementObject.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AcknowledgementResponseObject.class)
                .consumeWith(response -> {
                    AcknowledgementResponseObject acknowledgementResponseObject = response.getResponseBody();
                    assertNotNull(acknowledgementResponseObject);
                    assertEquals(String.format("Successfully received ACK for %s", acknowledgementObject.getEnvelope().getTransactionIdentifier()), acknowledgementResponseObject.getResponseText());
                });
    }

    /**
     * Test that the SECOM Acknowledgement interface will return an HTTP
     * Status BAD_REQUEST if a validation error occurs.
     */
    @Test
    void testAcknowledgementBadRequest() {
        webTestClient.post()
                .uri("/api/secom" + ACKNOWLEDGMENT_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just("Invalid acknowledgement object"), String.class))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Test that the SECOM Acknowledgement interface will return an HTTP
     * Status METHOD_NOT_ALLOWED if a method other than a get is requested.
     */
    @Test
    void testAcknowledgementNotAllowed() {
        webTestClient.get()
                .uri("/api/secom" + ACKNOWLEDGMENT_INTERFACE_PATH)
                .header(SecomRequestHeaders.MRN_HEADER, "mrn")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }
}