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

package org.grad.eNav.atonService.utils;

import _int.iala_aism.s125.gml._0_0.S125AidsToNavigationType;
import _int.iho.s100.gml.base._1_0.CurveProperty;
import _int.iho.s100.gml.base._1_0.PointProperty;
import _int.iho.s100.gml.base._1_0.SurfaceProperty;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _net.opengis.gml.profiles.*;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import javax.xml.bind.JAXBElement;
import java.util.*;

/**
 * The type Geometry S-125 Point/Curve/Surface Converter Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class GeometryS125Converter {

    /**
     * Convert an S-125 Aids to Navigation entry to the JTS Geometry.
     *
     * @param s125AidsToNavigationType the S-125 Aids to Navigation
     * @return the respective JTS geometry object
     */
    public Geometry convertToGeometry(S125AidsToNavigationType s125AidsToNavigationType) {
        return Optional.ofNullable(s125AidsToNavigationType)
                .map(S125Utils::geomPerS125AidsToNavigationType)
                .map(this::s125PointCurveSurfaceToGeometry)
                .orElse(null);
    }

    /**
     * Convert an Aids to Navigation entry to the S-125 Point/Curve/Surface
     * geometry.
     *
     * @param aidsToNavigation the Aids to Navigation entry
     * @return the respective Point/Curve/Surface geometry object
     */
    public PointCurveSurfaceProperty convertFromGeometry(AidsToNavigation aidsToNavigation) {
        return Optional.ofNullable(aidsToNavigation)
                .map(aton -> this.geometryToS125PointCurveSurfaceToGeometry(aton.getGeometry(), S125AtonTypes.fromLocalClass(aton.getClass())))
                .orElse(null);
    }

    /**
     * Translates the generic point/curve/surface property of the S-125
     * feature type into a JTS geometry (most likely a geometry collection)
     * that can be understood and handled by the services.
     *
     * @param pointCurveSurface the S-100 point/curve/surface property
     * @return the respective geometry
     */
    private Geometry s125PointCurveSurfaceToGeometry(PointCurveSurfaceProperty pointCurveSurface) {
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return Optional.ofNullable(pointCurveSurface)
                .map(pcs -> {
                    // Map based on the type of the populated geometry
                    if(Objects.nonNull(pcs.getPointProperty())) {
                        return Optional.of(pcs.getPointProperty())
                                .map(_int.iho.s100.gml.base._1_0.PointProperty::getPoint)
                                .map(PointType::getPos)
                                .map(pos -> new Coordinate(pos.getValues().get(0), pos.getValues().get(1)))
                                .map(geometryFactory::createPoint)
                                .map(Geometry.class::cast)
                                .orElse(geometryFactory.createEmpty(0));
                    } else if(Objects.nonNull(pcs.getCurveProperty())) {
                        return geometryFactory.createGeometryCollection(Optional.of(pcs.getCurveProperty())
                                .map(_int.iho.s100.gml.base._1_0.CurveProperty::getCurve)
                                .map(CurveType::getSegments)
                                .map(Segments::getAbstractCurveSegments)
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(JAXBElement::getValue)
                                .filter(LineStringSegmentType.class::isInstance)
                                .map(LineStringSegmentType.class::cast)
                                .map(LineStringSegmentType::getPosList)
                                .map(this::gmlPosListToCoordinates)
                                .map(coords -> coords.length == 1? geometryFactory.createPoint(coords[0]) : geometryFactory.createLineString(coords))
                                .toList()
                                .toArray(Geometry[]::new));
                    } else if(Objects.nonNull(pcs.getSurfaceProperty())) {
                        return geometryFactory.createGeometryCollection(Optional.of(pcs.getSurfaceProperty())
                                .map(_int.iho.s100.gml.base._1_0.SurfaceProperty::getAbstractSurface)
                                .map(JAXBElement::getValue)
                                .filter(SurfaceType.class::isInstance)
                                .map(SurfaceType.class::cast)
                                .map(SurfaceType::getPatches)
                                .map(Patches::getAbstractSurfacePatches)
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(JAXBElement::getValue)
                                .filter(PolygonPatchType.class::isInstance)
                                .map(PolygonPatchType.class::cast)
                                .map(PolygonPatchType::getExterior)
                                .map(AbstractRingPropertyType::getAbstractRing)
                                .map(JAXBElement::getValue)
                                .filter(LinearRingType.class::isInstance)
                                .map(LinearRingType.class::cast)
                                .map(LinearRingType::getPosList)
                                .map(this::gmlPosListToCoordinates)
                                .map(coords -> coords.length == 1? geometryFactory.createPoint(coords[0]) : geometryFactory.createPolygon(coords))
                                .toList()
                                .toArray(Geometry[]::new));
                    }
                    return null;
                })
                .orElseGet(() -> geometryFactory.createEmpty(-1));
    }

    /**
     * Translates the generic JTS geometry property of the local Aids to
     * Navigation objects tp a generic S-125 point/curve/surface geometry
     * that can be understood and handled by the S-125 services.
     *
     * @param geometry      The JTS geometry object
     * @param s125AtonTypes The S-125 AtoN type to pick up the point/curve/surface geometry type from
     * @return the S-125 point/curve/surface geometry
     */
    private PointCurveSurfaceProperty geometryToS125PointCurveSurfaceToGeometry(Geometry geometry, S125AtonTypes s125AtonTypes) {
        // Initialise the point/curve/surface geometry
        PointCurveSurfaceProperty pointCurveSurfaceProperty = new PointCurveSurfaceProperty();
        // Populate according to the S-125 AtoN type
        try {
            Class<?> s125GeometryType = s125AtonTypes.getS125GeometryType();
            if(s125GeometryType == _int.iho.s100.gml.base._1_0_Ext.PointProperty.class) {
                pointCurveSurfaceProperty.setPointProperty(this.generatePointProperty(
                        coordinatesToPosList(geometry.getCoordinates()).getValues()
                ));
            } else if (s125GeometryType == _int.iho.s100.gml.base._1_0_Ext.CurveProperty.class) {
                pointCurveSurfaceProperty.setCurveProperty(this.generateCurveProperty(
                        coordinatesToPosList(geometry.getCoordinates()).getValues()
                ));
            } else if (s125GeometryType == _int.iho.s100.gml.base._1_0_Ext.SurfaceProperty.class) {
                pointCurveSurfaceProperty.setSurfaceProperty(this.generateSurfaceProperty(
                        coordinatesToPosList(geometry.getCoordinates()).getValues()
                ));
            }
        } catch (NoSuchFieldException e) {
            // Don't bother... something went wrong, so we did nothing
        }
        // And return the populated property
        return pointCurveSurfaceProperty;
    }

    /**
     * Populates and return an S-125 surface property based on the provided
     * surface geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    private SurfaceProperty generateSurfaceProperty(Collection<Double> coords) {
        // Create an OpenGIS GML factory
        ObjectFactory opengisGMLFactory = new ObjectFactory();

        // Generate the elements
        SurfaceProperty surfaceProperty = new SurfaceProperty();
        _int.iho.s100.gml.base._1_0.SurfaceType surfaceType = new _int.iho.s100.gml.base._1_0.SurfaceType();
        Patches patches = new Patches();
        PolygonPatchType polygonPatchType = new PolygonPatchType();
        AbstractRingPropertyType abstractRingPropertyType = new AbstractRingPropertyType();
        LinearRingType linearRingType = new LinearRingType();
        PosList posList = new PosList();

        // Populate with the geometry data
        posList.getValues().addAll(coords);

        // Populate the elements
        linearRingType.setPosList(posList);
        abstractRingPropertyType.setAbstractRing(opengisGMLFactory.createLinearRing(linearRingType));
        polygonPatchType.setExterior(abstractRingPropertyType);
        patches.getAbstractSurfacePatches().add(opengisGMLFactory.createPolygonPatch(polygonPatchType));
        surfaceType.setPatches(patches);
        surfaceProperty.setAbstractSurface(opengisGMLFactory.createSurface(surfaceType));

        // And return the output
        return surfaceProperty;
    }

    /**
     * Populates and return an S-125 curve property based on the provided line
     * segment geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    private CurveProperty generateCurveProperty(Collection<Double> coords) {
        // Create an OpenGIS GML factory
        ObjectFactory opengisGMLFactory = new ObjectFactory();

        // Generate the elements
        CurveProperty curveProperty = new CurveProperty();
        _int.iho.s100.gml.base._1_0.CurveType curveType = new _int.iho.s100.gml.base._1_0.CurveType();
        Segments segments = new Segments();
        LineStringSegmentType lineStringSegmentType = new LineStringSegmentType();
        PosList posList = new PosList();

        // Populate with the geometry data
        posList.getValues().addAll(coords);

        // Populate the elements
        lineStringSegmentType.setPosList(posList);
        segments.getAbstractCurveSegments().add(opengisGMLFactory.createLineStringSegment(lineStringSegmentType));
        curveType.setSegments(segments);
        curveProperty.setCurve(curveType);

        // And return the output
        return curveProperty;
    }

    /**
     * Populates and return an S-125 point property based on the provided point
     * geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    protected PointProperty generatePointProperty(Collection<Double> coords) {
        // Generate the elements
        PointProperty pointProperty = new PointProperty();
        _int.iho.s100.gml.base._1_0.PointType pointType = new _int.iho.s100.gml.base._1_0.PointType();
        Pos pos = new Pos();

        // Populate with the geometry data
        pos.getValues().addAll(coords);

        // Populate the elements
        pointType.setPos(pos);
        pointProperty.setPoint(pointType);

        // And return the output
        return pointProperty;
    }

    /**
     * A simple utility function that splits the position list values by two
     * and generates JTS geometry coordinates by them.
     *
     * @param posList the provided position list
     * @return the respective coordinates
     */
    protected Coordinate[] gmlPosListToCoordinates(PosList posList) {
        final List<Coordinate> result = new ArrayList<>();
        final Iterator<Double> iterator = posList.getValues().iterator();
        while(iterator.hasNext()) {
            result.add(new Coordinate(iterator.next(), iterator.next()));
        }
        return result.toArray(new Coordinate[]{});
    }

    /**
     * A simple utility function that receives JTS geometry coordinates and
     * constructs a position list object.
     *
     * @param coordinates the provided coordinates
     * @return the respective position list
     */
    protected PosList coordinatesToPosList(Coordinate[] coordinates) {
        // Translate the coordinates to a simple list of doubles (Y, X)
        List<Double> coords = Optional.ofNullable(coordinates)
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .map(c -> Arrays.asList(c.getY(), c.getX()))
                .flatMap(List::stream).toList();

        // The create the list and return
        PosList posList = new PosList();
        posList.getValues().addAll(coords);
        return posList;
    }

}
