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
import _int.iala_aism.s125.gml._0_0.S125AidsToNavigationType;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _net.opengis.gml.profiles.*;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.domain.AtonMessageType;
import org.grad.eNav.atonService.models.dtos.S100AbstractNode;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.util.*;

/**
 * The S-100 Utility Class.
 *
 * A static utility function class that allows easily manipulation of the S-100
 * messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class AtonMessageUtils {

    /**
     * This helper function translates the provided SNode domain object to
     * a S100AbstractNode implementing DTO. This can be used when the service
     * response to a client, rather than an internal component.
     *
     * @param snode the SNode object to be translated to a DTO
     * @return the DTO generated from the provided SNode object
     */
    public static AtonMessage toAtonMessage(S100AbstractNode snode) {
        // Sanity check
        if(Objects.isNull(snode)) {
            return null;
        }

        // Now construct the AtoN Message based on the node type
        if (snode instanceof S125Node) {
            // Construct the geometry using the S125 Member NavAid Geometry
            final Geometry geometry = Optional.ofNullable(snode)
                    .map(S100AbstractNode::getContent)
                    .map(content -> {
                        try {
                            return S125Utils.unmarshallS125(snode.getContent());
                        }
                        catch(JAXBException ex) {
                            log.error(ex.getMessage(), ex); return null;
                        }
                    })
                    .map(DataSet::getImembersAndMembers)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(MemberType.class::isInstance)
                    .map(MemberType.class::cast)
                    .map(MemberType::getAbstractFeature)
                    .map(JAXBElement::getValue)
                    .filter(S125AidsToNavigationType.class::isInstance)
                    .map(S125AidsToNavigationType.class::cast)
                    .filter(s125 -> Objects.equals(s125.getAtonNumber(), ((S125Node)snode).getAtonUID()))
                    .findFirst()
                    .map(S125Utils::geomPerS125AidsToNavigationType)
                    .map(AtonMessageUtils::s125PointCurveSurfaceToGeometry)
                    .orElse(null);

            // Construct the AtoN message
            return new AtonMessage(
                    S125Node.class.cast(snode).getAtonUID(),
                    AtonMessageType.S125,
                    geometry,
                    snode.getContent());
        }

        // Otherwise, nothing to return
        return null;
    }

    /**
     * Translates the generic point/curve/surface property of the S-125
     * feature type into a JTS geometry (most likely a geometry collection)
     * that can be understood and handled by the services.
     *
     * @param pointCurveSurface the S-100 point/curve/surface property
     * @return the respective geometry
     */
    protected static Geometry s125PointCurveSurfaceToGeometry(PointCurveSurfaceProperty pointCurveSurface) {
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
                                .map(AtonMessageUtils::gmlPosListToCoordinates)
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
                                .map(AtonMessageUtils::gmlPosListToCoordinates)
                                .map(coords -> coords.length == 1? geometryFactory.createPoint(coords[0]) : geometryFactory.createPolygon(coords))
                                .toList()
                                .toArray(Geometry[]::new));
                    }
                    return null;
                })
                .orElseGet(() -> geometryFactory.createEmpty(-1));
    }

    /**
     * A simple utility function that splits the position list values by two
     * and generates TTS geometry coordinates by them.
     *
     * @param posList the provided position list
     * @return the respective coordinates
     */
    protected static Coordinate[] gmlPosListToCoordinates(PosList posList) {
        final List<Coordinate> result = new ArrayList<>();
        final Iterator<Double> iterator = posList.getValues().iterator();
        while(iterator.hasNext()) {
            result.add(new Coordinate(iterator.next(), iterator.next()));
        }
        return result.toArray(new Coordinate[]{});
    }

}
