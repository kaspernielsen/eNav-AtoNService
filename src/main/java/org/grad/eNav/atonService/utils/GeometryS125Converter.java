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
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.*;

import jakarta.xml.bind.JAXBElement;
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
                .map(aton -> this.geometryToS125PointCurveSurfaceToGeometry(aton.getGeometry()))
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
    protected Geometry s125PointCurveSurfaceToGeometry(PointCurveSurfaceProperty pointCurveSurface) {
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
                                .map(CurveProperty::getCurve)
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
     * Navigation objects to a generic S-125 point/curve/surface geometry
     * that can be understood and handled by the S-125 services.
     *
     * @param geometry      The JTS geometry object
     * @return the S-125 point/curve/surface geometry
     */
    protected PointCurveSurfaceProperty geometryToS125PointCurveSurfaceToGeometry(Geometry geometry) {
        // Initialise the point/curve/surface geometry
        PointCurveSurfaceProperty pointCurveSurfaceProperty = new PointCurveSurfaceProperty();

        // Populate according to the S-125 AtoN type
        populatePointCurveSurfaceToGeometry(geometry, pointCurveSurfaceProperty);

        // And return the populated property
        return pointCurveSurfaceProperty;
    }

    /**
     * A iterative helper function that examines the provided geometry and
     * dives deeper into collection to pick up the basic JTS geometry types
     * such as the points, lines and polygons.
     *
     * @param geometry                      The geometry to be examined
     * @param pointCurveSurfaceProperty     The S-125 geometry object to be populated
     */
    protected void populatePointCurveSurfaceToGeometry(Geometry geometry, PointCurveSurfaceProperty pointCurveSurfaceProperty) {
        // Create an OpenGIS GML factory
        ObjectFactory opengisGMLFactory = new ObjectFactory();

        if(geometry instanceof Puntal) {
            // Initialise the point property if not already initialised
            if(pointCurveSurfaceProperty.getPointProperty() == null) {
                pointCurveSurfaceProperty.setPointProperty(this.initPointProperty());
            }

            // And append the point
            pointCurveSurfaceProperty.getPointProperty().getPoint().setPos(
                    this.generatePointPropertyPosition(coordinatesToGmlPosList(geometry.getCoordinates()).getValues())
            );
        } else if(geometry instanceof Lineal) {
            // Initialise the curve property if not already initialised
            if(pointCurveSurfaceProperty.getCurveProperty() == null) {
                pointCurveSurfaceProperty.setCurveProperty(this.initialiseCurveProperty());
            }

            // And append the line string
            pointCurveSurfaceProperty.getCurveProperty().getCurve().getSegments().getAbstractCurveSegments().add(
                    opengisGMLFactory.createLineStringSegment(generateCurvePropertySegment(coordinatesToGmlPosList(geometry.getCoordinates()).getValues()))
            );
        } else if(geometry instanceof Polygonal) {
            // Initialise the curve property if not already initialised
            if(pointCurveSurfaceProperty.getSurfaceProperty() == null) {
                pointCurveSurfaceProperty.setSurfaceProperty(this.initialiseSurfaceProperty());
            }

            // And append the surface patch
            ((SurfaceType)pointCurveSurfaceProperty.getSurfaceProperty().getAbstractSurface().getValue()).getPatches().getAbstractSurfacePatches().add(
                    opengisGMLFactory.createPolygonPatch(generateSurfacePropertyPatch(coordinatesToGmlPosList(geometry.getCoordinates()).getValues()))
            );
        } else if(geometry instanceof GeometryCollection && geometry.getNumGeometries() > 0) {
            for(int i=0; i < geometry.getNumGeometries(); i++) {
                this.populatePointCurveSurfaceToGeometry(geometry.getGeometryN(i), pointCurveSurfaceProperty);
            }
        }
    }

    /**
     * Populates and return an S-125 surface property based on the provided
     * surface geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    protected PolygonPatchType generateSurfacePropertyPatch(Collection<Double> coords) {
        // Create an OpenGIS GML factory
        ObjectFactory opengisGMLFactory = new ObjectFactory();

        // Generate the elements
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

        // And return the output
        return polygonPatchType;
    }

    /**
     * Populates and return an S-125 curve property based on the provided line
     * segment geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    protected LineStringSegmentType generateCurvePropertySegment(Collection<Double> coords) {
        // Generate the elements
        LineStringSegmentType lineStringSegmentType = new LineStringSegmentType();
        PosList posList = new PosList();

        // Populate with the geometry data
        posList.getValues().addAll(coords);
        lineStringSegmentType.setPosList(posList);

        // And return the output
        return lineStringSegmentType;
    }

    /**
     * Populates and return an S-125 point property based on the provided point
     * geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    protected Pos generatePointPropertyPosition(Collection<Double> coords) {
        // Generate the elements
        Pos pos = new Pos();

        // Populate with the geometry data
        pos.getValues().addAll(coords);

        // And return the output
        return pos;
    }

    /**
     * Initialise the S-125 Surface Property object
     *
     * @return the initialised S-125 Surface Property object
     */
    protected SurfaceProperty initialiseSurfaceProperty() {
        // Create an OpenGIS GML factory
        ObjectFactory opengisGMLFactory = new ObjectFactory();

        // Generate the elements
        SurfaceProperty surfaceProperty = new SurfaceProperty();
        _int.iho.s100.gml.base._1_0.SurfaceType surfaceType = new _int.iho.s100.gml.base._1_0.SurfaceType();
        Patches patches = new Patches();

        // Populate the elements
        surfaceType.setPatches(patches);
        surfaceProperty.setAbstractSurface(opengisGMLFactory.createSurface(surfaceType));

        // And return the output
        return surfaceProperty;
    }

    /**
     * Initialise the S-125 Curve Property object
     *
     * @return the initialised S-125 Curve Property object
     */
    protected CurveProperty initialiseCurveProperty() {
        // Generate the elements
        CurveProperty curveProperty = new CurveProperty();
        _int.iho.s100.gml.base._1_0.CurveType curveType = new _int.iho.s100.gml.base._1_0.CurveType();
        Segments segments = new Segments();

        // Populate the elements
        curveType.setSegments(segments);
        curveProperty.setCurve(curveType);

        // And return the output
        return curveProperty;
    }

    /**
     * Initialise the S-125 Point Property object
     *
     * @return the initialised S-125 Point Property object
     */
    protected PointProperty initPointProperty() {
        // Generate the elements
        PointProperty pointProperty = new PointProperty();
        _int.iho.s100.gml.base._1_0.PointType pointType = new _int.iho.s100.gml.base._1_0.PointType();

        // Populate the elements
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
    protected PosList coordinatesToGmlPosList(Coordinate[] coordinates) {
        // Translate the coordinates to a simple list of doubles (Y, X)
        List<Double> coords = Optional.ofNullable(coordinates)
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .map(c -> Arrays.asList(c.getX(), c.getY()))
                .flatMap(List::stream).toList();

        // The create the list and return
        PosList posList = new PosList();
        posList.getValues().addAll(coords);
        return posList;
    }

}
