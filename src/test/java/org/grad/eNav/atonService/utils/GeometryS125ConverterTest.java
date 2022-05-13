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

import _int.iala_aism.s125.gml._0_0.DataSet;
import _int.iala_aism.s125.gml._0_0.MemberType;
import _int.iala_aism.s125.gml._0_0.S125VirtualAISAidToNavigationType;
import _int.iho.s100.gml.base._1_0.CurveProperty;
import _int.iho.s100.gml.base._1_0.PointProperty;
import _int.iho.s100.gml.base._1_0.SurfaceProperty;
import _int.iho.s100.gml.base._1_0.SurfaceType;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _net.opengis.gml.profiles.*;
import org.apache.commons.io.IOUtils;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.domain.s125.VirtualAISAidToNavigation;
import org.grad.eNav.s125.utils.S125Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeometryS125ConverterTest {

    // Test Variables
    private GeometryS125Converter geometryS125Converter;
    private GeometryFactory factory;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException {
        // First create the converter
        geometryS125Converter = new GeometryS125Converter();

        // Create a temp geometry factory to get a test geometries
        this.factory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    /**
     * Test that we can directly convert an S-125 dataset Aids to Navigation
     * member entry to its respective JTS geometry, with the respective type
     * that is matching.
     */
    @Test
    void testConvertFromGeometry() throws IOException, JAXBException {
        // Read a valid S125 content to generate the S125Node message for.
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        String xml = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        // Unmarshall it to a G1128 service instance object
        DataSet dataset = S125Utils.unmarshallS125(xml);

        // Assert the S-125 AidsToNavigation feature information is populated
        assertNotNull(dataset.getImembersAndMembers());
        assertEquals(1, dataset.getImembersAndMembers().size());
        S125VirtualAISAidToNavigationType resultMember = (S125VirtualAISAidToNavigationType) ((MemberType) dataset.getImembersAndMembers().get(0)).getAbstractFeature().getValue();

        // Convert to JTS geometry
        Geometry result = this.geometryS125Converter.convertToGeometry(resultMember);

        // Make sure the result looks OK
        assertNotNull(result);
        assertEquals(Point.class, result.getClass());
        assertEquals(1.4233333333333333, ((Point)result).getX());
        assertEquals(51.891666666666666, ((Point)result).getY());
    }

    /**
     * Test that we can directly convert the JTS geometry from an Aids to
     * Navigation object into an S-125 PointCurveSurfaceProperty object
     * populated as required.
     */
    @Test
    void testConvertToGeometry() {
        // Create the point
        List<Double> coords = Stream.of(51.98, 1.28).toList();
        Point point = this.factory.createPoint(new Coordinate(coords.get(0), coords.get(1)));

        // Create a typical Aids to Navigation
        VirtualAISAidToNavigation virtualAISAidToNavigation = new VirtualAISAidToNavigation();
        virtualAISAidToNavigation.setGeometry(point);

        // Convert to S-125 geometry
        PointCurveSurfaceProperty result = this.geometryS125Converter.convertFromGeometry(virtualAISAidToNavigation);

        // Make sure the result looks OK
        assertNotNull(result);
        assertNotNull(result.getPointProperty());
        assertNull(result.getCurveProperty());
        assertNull(result.getSurfaceProperty());

        // Make sure the point property looks OK
        PointProperty pointProperty = result.getPointProperty();
        assertNotNull(pointProperty.getPoint());
        assertNotNull(pointProperty.getPoint().getPos());
        assertNotNull(pointProperty.getPoint().getPos().getValues());
        assertEquals(coords.size(), pointProperty.getPoint().getPos().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i), pointProperty.getPoint().getPos().getValues().get(i));
        }
    }

    /**
     * Test that we can translate correctly a geometry object to an S-125
     * point/curve/surface geometry description. This specific test looks
     * at the surface translation.
     */
    @Test
    void testGeometryToS125PointCurveSurfaceToGeometryForSurface() {
        // Create the polygon
        List<Double> coords = Stream.of(51.98, 1.28, 51.98, 2.28, 52.98, 2.28, 52.98, 1.28, 51.98, 1.28).toList();
        Polygon polygon = this.factory.createPolygon(new Coordinate[]{
                new Coordinate(coords.get(0), coords.get(1)),
                new Coordinate(coords.get(2), coords.get(3)),
                new Coordinate(coords.get(4), coords.get(5)),
                new Coordinate(coords.get(6), coords.get(7)),
                new Coordinate(coords.get(8), coords.get(9))}
        );

        // Translate to S-125 geometry
        PointCurveSurfaceProperty result = this.geometryS125Converter.geometryToS125PointCurveSurfaceToGeometry(polygon);

        // Make sure the result looks OK
        assertNotNull(result);
        assertNull(result.getPointProperty());
        assertNull(result.getCurveProperty());
        assertNotNull(result.getSurfaceProperty());

        // Make sure the surface property looks OK
        assertNotNull(result.getSurfaceProperty());
        assertNotNull(result.getSurfaceProperty().getAbstractSurface());
        assertNotNull(result.getSurfaceProperty().getAbstractSurface().getValue());
        assertEquals(SurfaceType.class, result.getSurfaceProperty().getAbstractSurface().getValue().getClass());
        assertNotNull(((SurfaceType)result.getSurfaceProperty().getAbstractSurface().getValue()).getPatches());
        assertNotNull(((SurfaceType)result.getSurfaceProperty().getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches());
        assertEquals(1, ((SurfaceType)result.getSurfaceProperty().getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches().size());
        assertEquals(PolygonPatchType.class, ((SurfaceType)result.getSurfaceProperty().getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches().get(0).getValue().getClass());

        // Make sure the polygon patches look OK
        PolygonPatchType polygonPatchType = (PolygonPatchType)((SurfaceType)result.getSurfaceProperty().getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches().get(0).getValue();
        assertNotNull(polygonPatchType);
        assertNotNull(polygonPatchType.getExterior());
        assertNotNull(polygonPatchType.getExterior().getAbstractRing());
        assertNotNull(polygonPatchType.getExterior().getAbstractRing().getValue());
        assertEquals(LinearRingType.class, polygonPatchType.getExterior().getAbstractRing().getValue().getClass());
        assertNotNull(((LinearRingType)polygonPatchType.getExterior().getAbstractRing().getValue()).getPosList());
        assertNotNull(((LinearRingType)polygonPatchType.getExterior().getAbstractRing().getValue()).getPosList().getValues());
        assertEquals(10, ((LinearRingType)polygonPatchType.getExterior().getAbstractRing().getValue()).getPosList().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i), ((LinearRingType)polygonPatchType.getExterior().getAbstractRing().getValue()).getPosList().getValues().get(i));
        }
    }

    /**
     * Test that we can translate correctly a geometry object to an S-125
     * point/curve/surface geometry description. This specific test looks
     * at the curve translation.
     */
    @Test
    void testGeometryToS125PointCurveSurfaceToGeometryForCurve() {
        // Create the line
        List<Double> coords = Stream.of(51.98, 1.28, 52.98, 2.28).toList();
        LineString lineString = this.factory.createLineString(new Coordinate[]{new Coordinate(coords.get(0), coords.get(1)), new Coordinate(coords.get(2), coords.get(3))});

        // Translate to S-125 geometry
        PointCurveSurfaceProperty result = this.geometryS125Converter.geometryToS125PointCurveSurfaceToGeometry(lineString);

        // Make sure the result looks OK
        assertNotNull(result);
        assertNull(result.getPointProperty());
        assertNotNull(result.getCurveProperty());
        assertNull(result.getSurfaceProperty());

        // Make sure the curve property looks OK
        CurveProperty curveProperty = result.getCurveProperty();
        assertNotNull(curveProperty.getCurve().getSegments());
        assertNotNull(curveProperty.getCurve().getSegments().getAbstractCurveSegments());
        assertEquals(1, curveProperty.getCurve().getSegments().getAbstractCurveSegments().size());
        assertNotNull(curveProperty.getCurve().getSegments().getAbstractCurveSegments().get(0).getValue());
        assertEquals(LineStringSegmentType.class, curveProperty.getCurve().getSegments().getAbstractCurveSegments().get(0).getValue().getClass());

        // Make sure the curve segments look OK
        LineStringSegmentType lineStringSegmentType = (LineStringSegmentType) curveProperty.getCurve().getSegments().getAbstractCurveSegments().get(0).getValue();
        assertNotNull(lineStringSegmentType.getPosList());
        assertNotNull(lineStringSegmentType.getPosList().getValues());
        assertEquals(4,lineStringSegmentType.getPosList().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i),lineStringSegmentType.getPosList().getValues().get(i));
        }
    }

    /**
     * Test that we can translate correctly a geometry object to an S-125
     * point/curve/surface geometry description. This specific test looks
     * at the point translation.
     */
    @Test
    void testGeometryToS125PointCurveSurfaceToGeometryForPoint() {
        // Create the point
        List<Double> coords = Stream.of(51.98, 1.28).toList();
        Point point = this.factory.createPoint(new Coordinate(coords.get(0), coords.get(1)));

        // Translate to S-125 geometry
        PointCurveSurfaceProperty result = this.geometryS125Converter.geometryToS125PointCurveSurfaceToGeometry(point);

        // Make sure the result looks OK
        assertNotNull(result);
        assertNotNull(result.getPointProperty());
        assertNull(result.getCurveProperty());
        assertNull(result.getSurfaceProperty());

        // Make sure the point property looks OK
        PointProperty pointProperty = result.getPointProperty();
        assertNotNull(pointProperty.getPoint());
        assertNotNull(pointProperty.getPoint().getPos());
        assertNotNull(pointProperty.getPoint().getPos().getValues());
        assertEquals(coords.size(), pointProperty.getPoint().getPos().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i), pointProperty.getPoint().getPos().getValues().get(i));
        }
    }

    /**
     * Test that we can correctly create a new S-125 GML SurfaceProperty Patch
     * based on a set of coordinates.
     */
    @Test
    void testGenerateSurfacePropertyPatch() {
        // Create the coordinates
        List<Double> coords = Stream.of(51.98, 1.28, 51.98, 2.28, 52.98, 2.28, 52.98, 1.28, 51.98, 1.28).toList();

        // Generate the curve property patch
        PolygonPatchType result = this.geometryS125Converter.generateSurfacePropertyPatch(coords);

        // Make sure the polygon patches look OK
        assertNotNull(result);
        assertNotNull(result.getExterior());
        assertNotNull(result.getExterior().getAbstractRing());
        assertNotNull(result.getExterior().getAbstractRing().getValue());
        assertEquals(LinearRingType.class, result.getExterior().getAbstractRing().getValue().getClass());
        assertNotNull(((LinearRingType)result.getExterior().getAbstractRing().getValue()).getPosList());
        assertNotNull(((LinearRingType)result.getExterior().getAbstractRing().getValue()).getPosList().getValues());
        assertEquals(coords.size(), ((LinearRingType)result.getExterior().getAbstractRing().getValue()).getPosList().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i), ((LinearRingType)result.getExterior().getAbstractRing().getValue()).getPosList().getValues().get(i));
        }
    }

    /**
     * Test that we can correctly create a new S-125 GML CurveProperty Segment
     * based on a set of coordinates.
     */
    @Test
    void testGenerateCurvePropertySegment() {
        // Create the coordinates
        List<Double> coords = Stream.of(51.98, 1.28, 52.98, 2.28).toList();

        // Generate the curve property segment
        LineStringSegmentType result = this.geometryS125Converter.generateCurvePropertySegment(coords);

        // Make sure the curve segments look OK
        assertNotNull(result);
        assertNotNull(result.getPosList());
        assertNotNull(result.getPosList().getValues());
        assertEquals(coords.size(), result.getPosList().getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i),result.getPosList().getValues().get(i));
        }
    }

    /**
     * Test that we can correctly create a new S-125 GML PointProperty Position
     * based on a set of coordinates.
     */
    @Test
    void testGeneratePointPropertyPosition() {
        // Create the coordinates
        List<Double> coords = Stream.of(51.98, 1.28).toList();

        // Generate the point property position
        Pos result = this.geometryS125Converter.generatePointPropertyPosition(coords);

        // Make sure the point property looks OK
        assertNotNull(result);
        assertEquals(coords.size(), result.getValues().size());
        for(int i=0; i< coords.size(); i++) {
            assertEquals(coords.get(i), result.getValues().get(i));
        }
    }

    /**
     * Test that we can correctly initialise a new S-125 GML SurfaceProperty.
     */
    @Test
    void testInitialiseSurfaceProperty() {
        // Generate the curve property
        SurfaceProperty result = this.geometryS125Converter.initialiseSurfaceProperty();

        // Make sure the surface property looks OK
        assertNotNull(result);
        assertNotNull(result.getAbstractSurface());
        assertNotNull(result.getAbstractSurface().getValue());
        assertEquals(SurfaceType.class, result.getAbstractSurface().getValue().getClass());
        assertNotNull(((SurfaceType)result.getAbstractSurface().getValue()).getPatches());
        assertNotNull(((SurfaceType)result.getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches());
        assertEquals(0, ((SurfaceType)result.getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches().size());
    }

    /**
     * Test that we can correctly initialise a new S-125 GML CurveProperty.
     */
    @Test
    void testInitialiseCurveProperty() {
        // Generate the curve property
        CurveProperty result = this.geometryS125Converter.initialiseCurveProperty();

        // Make sure the curve property looks OK
        assertNotNull(result);
        assertNotNull(result.getCurve());
        assertNotNull(result.getCurve().getSegments());
        assertNotNull(result.getCurve().getSegments().getAbstractCurveSegments());
        assertEquals(0, result.getCurve().getSegments().getAbstractCurveSegments().size());
    }

    /**
     * Test that we can correctly initialise a new S-125 GML PointProperty.
     */
    @Test
    void tesInitialisePointProperty() {
        // Generate the point property
        PointProperty result = this.geometryS125Converter.initPointProperty();

        // Make sure the point property looks OK
        assertNotNull(result);
        assertNotNull(result.getPoint());
    }

    /**
     * Test that we can correctly translate an S-125 GML position list into a
     * array of coordinates.
     */
    @Test
    void testCoordinatesToPosList() {
        // Initialise the position list
        PosList posList = new PosList();
        posList.getValues().addAll(Stream.of(1.0, 2.0, 3.0, 4.0).toList());

        // Translate to coordinates
        Coordinate[] result = this.geometryS125Converter.gmlPosListToCoordinates(posList);

        // Make sure the translation looks OK
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(posList.getValues().get(0), result[0].getX());
        assertEquals(posList.getValues().get(1), result[0].getY());
        assertEquals(posList.getValues().get(2), result[1].getX());
        assertEquals(posList.getValues().get(3), result[1].getY());
    }

    /**
     * Test that we can correctly translate an array of coordinates into a
     * single S-125 GML position list.
     */
    @Test
    void testCoordinatesToGmlPosList() {
        // Initialise some coordinates
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1.0, 2.0, 0), new Coordinate(3.0, 4.0, 0)};

        // Translate to a position list
        PosList result = this.geometryS125Converter.coordinatesToGmlPosList(coordinates);

        // Make sure the translation looks OK
        assertNotNull(result);
        assertNotNull(result.getValues());
        assertEquals(4, result.getValues().size());
        assertEquals(coordinates[0].getX(), result.getValues().get(0));
        assertEquals(coordinates[0].getY(), result.getValues().get(1));
        assertEquals(coordinates[1].getX(), result.getValues().get(2));
        assertEquals(coordinates[1].getY(), result.getValues().get(3));
    }
}