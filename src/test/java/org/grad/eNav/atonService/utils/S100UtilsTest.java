/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

package org.grad.eNav.atonService.utils;

import _int.iho.s100.gml.base._1_0.PointProperty;
import _int.iho.s100.gml.base._1_0.PointType;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _int.iho.s125.gml._0.*;
import _net.opengis.gml.profiles.BoundingShapeType;
import _net.opengis.gml.profiles.EnvelopeType;
import _net.opengis.gml.profiles.Pos;
import org.apache.commons.io.IOUtils;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.generic.AtonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S100UtilsTest {

    // Test Variables
    private DataSet dataset;
    private String datasetXml;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException {
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        this.datasetXml = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        // Create an S125 dataset type similar to the static XML defined here
        this.dataset = new DataSet();
        this.dataset.setId("aton.uk.test_aton");

        // Create the bounding envelope
        EnvelopeType envelopeType = new EnvelopeType();
        envelopeType.setSrsName("EPSG:4326");
        Pos pos = new Pos();
        pos.getValues().addAll(Arrays.asList(new Double[]{53.61, 1.594}));
        envelopeType.setLowerCorner(pos);
        envelopeType.setUpperCorner(pos);
        BoundingShapeType boundingShapeType = new BoundingShapeType();
        boundingShapeType.setEnvelope(envelopeType);
        this.dataset.setBoundedBy(boundingShapeType);

        PointType pointType = new PointType();
        pointType.setPos(pos);
        PointProperty pointProperty = new PointProperty();
        pointProperty.setPoint(pointType);
        PointCurveSurfaceProperty pointCurveSurface = new PointCurveSurfaceProperty();
        pointCurveSurface.setPointProperty(pointProperty);

        // Create the Feature Name
        S125FeatureNameType featureNameType = new S125FeatureNameType();
        featureNameType.setName("Test AtoN");

        // Add the S125 NavAidStructure feature
        S125NavAidStructureType s125NavAidStructureType = new S125NavAidStructureType();
        s125NavAidStructureType.setFeatureName(featureNameType);
        s125NavAidStructureType.setGeometry(pointCurveSurface);
        s125NavAidStructureType.setMmsi(123456789);
        s125NavAidStructureType.setAtonType(S125AtonType.SPECIAL_MARK);
        s125NavAidStructureType.setDeploymentType(S125DeploymentType.MOBILE);
        s125NavAidStructureType.setRaimFlag(Boolean.FALSE);
        s125NavAidStructureType.setVatonFlag(Boolean.TRUE);
        JAXBElement<S125StructureFeatureType> jaxbElement = new JAXBElement<>(
                new QName("http://www.iho.int/S125/gml/0.1", "S125_NavAidStructure"),
                S125StructureFeatureType.class,
                null,
                s125NavAidStructureType
        );
        MemberType memberType = new MemberType();
        memberType.setAbstractFeature(jaxbElement);
        this.dataset.getMembersAndImembers().add(memberType);
    }

    /**
     * Test that we can create (marshall) and XML based on an S125 dataset type
     * object.
     *
     * @throws JAXBException a JAXB exception thrown during the marshalling operation
     */
    @Test
    void testMarchallS125() throws JAXBException {
        String xml = S100Utils.marshalS125(this.dataset);
        assertNotNull(xml);
        assertEquals(this.datasetXml, xml);
    }

    /**
     * Test that we can generate (unmarshall) a G1128 POJO based on a valid
     * XML G1128 specification.
     *
     * @throws IOException any IO exceptions while reading the input XML file
     * @throws JAXBException a JAXB exception thrown during the unmarshalling operation
     */
    @Test
    void testUnmarshalS125() throws IOException, JAXBException {
        // Unmarshall it to a G1128 service instance object
        DataSet result = S100Utils.unmarshallS125(this.datasetXml);

        // Assert all information is correct
        assertNotNull(result);
        assertEquals(this.dataset.getId(), result.getId());

        // Assert the dataset type envelopes are correct
        EnvelopeType datasetTypeEnvelope = this.dataset.getBoundedBy().getEnvelope();
        EnvelopeType resultEnvelope = result.getBoundedBy().getEnvelope();
        assertEquals(datasetTypeEnvelope.getSrsName(), resultEnvelope.getSrsName());
        assertEquals(datasetTypeEnvelope.getLowerCorner().getValues(), resultEnvelope.getLowerCorner().getValues());
        assertEquals(datasetTypeEnvelope.getUpperCorner().getValues(), resultEnvelope.getUpperCorner().getValues());

        // Assert the S125 NavAidStructure feature information is correct
        S125NavAidStructureType datasetTypeMember = (S125NavAidStructureType) ((MemberType) this.dataset.getMembersAndImembers().get(0)).getAbstractFeature().getValue();
        S125NavAidStructureType resultMember =  (S125NavAidStructureType) ((MemberType) result.getMembersAndImembers().get(0)).getAbstractFeature().getValue();
        assertEquals(datasetTypeMember.getMmsi(), resultMember.getMmsi());
        assertEquals(datasetTypeMember.getAtonType(), resultMember.getAtonType());
        assertEquals(datasetTypeMember.getDeploymentType(), resultMember.getDeploymentType());
        assertEquals(datasetTypeMember.isRaimFlag(), resultMember.isRaimFlag());
        assertEquals(datasetTypeMember.isVatonFlag(), resultMember.isVatonFlag());
    }

    // Define the test S125 Messages Content
    public static final String S125_NO_1_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_1\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 1</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_1.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><atonType>Special Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_2_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_2\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>1.594 53.61</gml:lowerCorner><gml:upperCorner>1.594 53.61</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 2</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_2.1\" srsName=\"EPSG:4326\"><gml:pos>1.594 53.61</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>111111111</mmsi><atonType>Cardinal Mark N</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>true</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";
    public static final String S125_NO_3_CONTENT = "<S125:DataSet xmlns:S125=\"http://www.iho.int/S125/gml/0.1\" xmlns:S100=\"http://www.iho.int/s100gml/1.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gml:id=\"aton.uk.test_aton_no_3\" xsi:schemaLocation=\"http://www.iho.int/S125/gml/1.0 S125.xsd\"><gml:boundedBy><gml:Envelope srsName=\"EPSG:4326\"><gml:lowerCorner>53.61 1.594</gml:lowerCorner><gml:upperCorner>53.61 1.594</gml:upperCorner></gml:Envelope></gml:boundedBy><member><S125:S125_NavAidStructure><featureName><displayName>true</displayName><language>eng</language><name>Test AtoN No 3</name></featureName><geometry><S100:pointProperty><S100:Point gml:id=\"G.aton.uk.test_aton_no_3.1\" srsName=\"EPSG:4326\"><gml:pos>53.61 1.594</gml:pos></S100:Point></S100:pointProperty></geometry><mmsi>123456789</mmsi><length>4</length><width>4</width><atonType>Port hand Mark</atonType><deploymentType>Mobile</deploymentType><raimFlag>false</raimFlag><vatonFlag>false</vatonFlag></S125:S125_NavAidStructure></member></S125:DataSet>";

    /**
     * Test that the AIS Message 21 will be constructed by default with
     * empty parameters.
     */
    @Test
    public void testEmptyConstructor() {
        AISMessage21 aisMessage21 = new AISMessage21();
        assertNull(aisMessage21.getMmsi());
        assertEquals(AtonType.DEFAULT, aisMessage21.getAtonType());
        assertEquals("", aisMessage21.getName());
        assertEquals(0.0, aisMessage21.getLatitude());
        assertEquals(0.0, aisMessage21.getLongitude());
        assertEquals(0, aisMessage21.getLength());
        assertEquals(0, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.FALSE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a test Virtual AtoN.
     */
    @Test
    public void testS125NodeConstructorNo1() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, S125_NO_1_CONTENT);

        // Create the GR-AIS Message 21
        AISMessage21 msgParams = S100Utils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(123456789, msgParams.getMmsi());
        assertEquals(AtonType.SPECIAL_MARK, msgParams.getAtonType());
        assertEquals("Test AtoN No 1", msgParams.getName());
        assertEquals(53.61, msgParams.getLatitude());
        assertEquals(1.594, msgParams.getLongitude());
        assertEquals(0, msgParams.getLength());
        assertEquals(0, msgParams.getWidth());
        assertEquals(Boolean.FALSE, msgParams.getRaim());
        assertEquals(Boolean.TRUE, msgParams.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a different Virtual AtoN.
     */
    @Test
    public void testS125NodeConstructorNo2() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_2", null, S125_NO_2_CONTENT);

        // Create the GR-AIS Message 21
        AISMessage21 aisMessage21 = S100Utils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(111111111, aisMessage21.getMmsi());
        assertEquals(AtonType.NORTH_CARDINAL, aisMessage21.getAtonType());
        assertEquals("Test AtoN No 2", aisMessage21.getName());
        assertEquals(1.594, aisMessage21.getLatitude());
        assertEquals(53.61, aisMessage21.getLongitude());
        assertEquals(0, aisMessage21.getLength());
        assertEquals(0, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.TRUE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, all parameters will be
     * correctly picked up, for a real AtoN.
     */
    @Test
    public void testS125NodeConstructorNo3() throws JAXBException {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_3", null, S125_NO_3_CONTENT);

        // Create the GR-AIS Message 21 Parameters
        AISMessage21 aisMessage21 = S100Utils.s125ToAisMessage21(node);

        // Assert that all variables have been initialised correctly
        assertEquals(123456789, aisMessage21.getMmsi());
        assertEquals(AtonType.PORT_HAND_MARK, aisMessage21.getAtonType());
        assertEquals("Test AtoN No 3", aisMessage21.getName());
        assertEquals(53.61, aisMessage21.getLatitude());
        assertEquals(1.594, aisMessage21.getLongitude());
        assertEquals(4, aisMessage21.getLength());
        assertEquals(4, aisMessage21.getWidth());
        assertEquals(Boolean.FALSE, aisMessage21.getRaim());
        assertEquals(Boolean.FALSE, aisMessage21.getVaton());
    }

    /**
     * Test that by using the S125Node constructor, if it fails, a JAXBException
     * will be thrown.
     */
    @Test
    public void testS125NodeConstructorFails() {
        // Create an S125Node message
        S125Node node = new S125Node("aton.uk.test_aton_no_1", null, "Erroneous Content");

        // Create the GR-AIS Message 21 Parameters and see it fail
        assertThrows(JAXBException.class, () -> S100Utils.s125ToAisMessage21(node));
    }

}