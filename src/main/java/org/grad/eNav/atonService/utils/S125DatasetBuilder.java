/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.utils;

import _int.iho.s100.gml.base._5_0.CurveProperty;
import _int.iho.s100.gml.base._5_0.MultiPointProperty;
import _int.iho.s100.gml.base._5_0.PointProperty;
import _int.iho.s100.gml.base._5_0.SurfaceProperty;
import _int.iho.s100.gml.profiles._5_0.BoundingShapeType;
import _int.iho.s100.gml.profiles._5_0.EnvelopeType;
import _int.iho.s100.gml.profiles._5_0.Pos;
import _int.iho.s100.gml.profiles._5_0.impl.BoundingShapeTypeImpl;
import _int.iho.s100.gml.profiles._5_0.impl.EnvelopeTypeImpl;
import _int.iho.s100.gml.profiles._5_0.impl.PosImpl;
import _int.iho.s125.gml.cs0._1.Dataset;
import _int.iho.s125.gml.cs0._1.impl.AggregationImpl;
import _int.iho.s125.gml.cs0._1.impl.AssociationImpl;
import _int.iho.s125.gml.cs0._1.impl.DatasetImpl;
import _int.iho.s125.gml.cs0._1.impl.ObjectFactory;
import jakarta.validation.constraints.NotNull;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.modelmapper.ModelMapper;

import java.util.*;

public class S125DatasetBuilder {

    /**
     * The Model Mapper
     */
    private final ModelMapper modelMapper;

    // Class Variables
    private final ObjectFactory s125GMLFactory;

    /**
     * Class Constructor.
     */
    public S125DatasetBuilder(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.s125GMLFactory = new ObjectFactory();
    }

    /**
     * This is the main   the provided list of AtoN nodes into an S125 dataset
     * as dictated by the NIPWG S-125 data product specification.
     *
     * @param s125Dataset   The S-125 local dataset object
     * @param atons         The list of S-125 local AtoN object list
     */
    public Dataset packageToDataset(@NotNull S125Dataset s125Dataset, List<AidsToNavigation> atons) {
        // Initialise the dataset
        Dataset dataset = this.modelMapper.map(s125Dataset, DatasetImpl.class);

        // Always use a UUID as an ID
        if(Objects.isNull(dataset.getId())) {
            dataset.setId(Optional.ofNullable(s125Dataset)
                    .map(S125Dataset::getUuid)
                    .orElse(UUID.randomUUID())
                    .toString());
        }

        // Add the content update sequence to the dataset identification information
        Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getSequenceNo)
                .ifPresent(dataset.getDatasetIdentificationInformation()::setUpdateNumber);
        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        dataset.setBoundedBy(this.generateBoundingShape(atons));
        dataset.getPointsAndMultiPointsAndCurves()
                .addAll(
                    Optional.of(s125Dataset)
                    .map(d -> new GeometryS125Converter().geometryToS125PointCurveSurfaceGeometry(d.getGeometry()))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(attr -> {
                        if(attr instanceof PointProperty) {
                            return ((PointProperty)attr).getPoint();
                        } else if(attr instanceof MultiPointProperty) {
                            return ((MultiPointProperty)attr).getMultiPoint();
                        } else if(attr instanceof CurveProperty) {
                            return ((CurveProperty)attr).getCurve();
                        } else if(attr instanceof SurfaceProperty) {
                            return ((SurfaceProperty)attr).getSurface();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList()
                );

        //====================================================================//
        //                      DATASET MEMBERS SECTION                       //
        //====================================================================//
        // Add the AtoN members
        S125Utils.addDatasetMembers(dataset, Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(aton -> this.modelMapper.map(aton, S125AtonTypes.fromLocalClass(aton.getClass()).getS125Class()))
                .toList());

        // Append the aggregations
        S125Utils.addDatasetMembers(dataset, Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(AidsToNavigation::getAggregations)
                .flatMap(Set::stream)
                .distinct()
                .map(agg -> this.modelMapper.map(agg, AggregationImpl.class))
                .toList());

        // Append the associations
        S125Utils.addDatasetMembers(dataset,Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(AidsToNavigation::getAssociations)
                .flatMap(Set::stream)
                .distinct()
                .map(ass -> this.modelMapper.map(ass, AssociationImpl.class))
                .toList());

        // Return the dataset
        return dataset;
    }

    /**
     * For easy generation of the bounding shapes for the dataset or individual
     * features, we are using this function.
     *
     * @param atonNodes     The AtoN nodes to generate the bounding shape from
     * @return the bounding shape
     */
    protected BoundingShapeType generateBoundingShape(Collection<AidsToNavigation> atonNodes) {
        // Calculate the bounding by envelope
        final Envelope envelope = new Envelope();
        atonNodes.stream()
                .map(AidsToNavigation::getGeometry)
                .forEach(g -> this.enclosingEnvelopeFromGeometry(envelope, g));

        Pos lowerCorner = new PosImpl();
        lowerCorner.setValue(new Double[]{envelope.getMinX(), envelope.getMaxY()});
        Pos upperCorner = new PosImpl();
        upperCorner.setValue(new Double[]{envelope.getMaxX(), envelope.getMaxY()});

        // And create the bounding by envelope
        BoundingShapeType boundingShapeType = new BoundingShapeTypeImpl();
        EnvelopeType envelopeType = new EnvelopeTypeImpl();
        envelopeType.setSrsName("EPSG:4326");
        envelopeType.setLowerCorner(lowerCorner);
        envelopeType.setUpperCorner(upperCorner);
        boundingShapeType.setEnvelope(envelopeType);

        // Finally, return the result
        return boundingShapeType;
    }

    /**
     * Adds the enclosing geometry boundaries to the provided envelop.
     *
     * @param envelope      The envelope to be updated
     * @param geometry      The geometry to update the envelope boundaries with
     * @return the updates envelope
     */
    protected Envelope enclosingEnvelopeFromGeometry(Envelope envelope, Geometry geometry) {
        final Geometry enclosingGeometry = geometry.getEnvelope();
        final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
        for (Coordinate c : enclosingCoordinates) {
            envelope.expandToInclude(c);
        }
        return envelope;
    }

}
