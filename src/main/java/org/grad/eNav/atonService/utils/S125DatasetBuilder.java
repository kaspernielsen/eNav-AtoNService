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

import _int.iala_aism.s125.gml._0_0.DataSet;
import _int.iala_aism.s125.gml._0_0.MemberType;
import _int.iho.s100.gml.base._1_0.DataSetIdentificationType;
import _int.iho.s100.gml.base._1_0.DataSetStructureInformationType;
import _int.iho.s100.gml.base._1_0.ISO6391;
import _net.opengis.gml.profiles.BoundingShapeType;
import _net.opengis.gml.profiles.EnvelopeType;
import _net.opengis.gml.profiles.Pos;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.domain.s125.S125DatasetInfo;
import org.locationtech.jts.geom.*;
import org.modelmapper.ModelMapper;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class S125DatasetBuilder {

    /**
     * The Model Mapper
     */
    private ModelMapper modelMapper;

    private AtomicInteger idIndex;
    private _int.iala_aism.s125.gml._0_0.ObjectFactory s125GMLFactory;
    private _net.opengis.gml.profiles.ObjectFactory opengisGMLFactory;

    /**
     * Class Constructor.
     */
    public S125DatasetBuilder(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.idIndex = new AtomicInteger(1);
        this.s125GMLFactory = new _int.iala_aism.s125.gml._0_0.ObjectFactory();
        this.opengisGMLFactory = new _net.opengis.gml.profiles.ObjectFactory();
    }

    /**
     * This is the main   the provided list of AtoN nodess into an S125 dataset as
     * dictated by the NIPWG S-125 data product specification.
     *
     * @param datasetInfo   The dataset information
     * @param atons         The list of S-125 AtoN nodes
     */
    public DataSet packageToDataset(S125DatasetInfo datasetInfo, List<AidsToNavigation> atons) {
        // Initialise the dataset
        DataSet s125Dataset = new DataSet();
        s125Dataset.setId(datasetInfo.getDatasetId());

        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        s125Dataset.setBoundedBy(this.generateBoundingShape(atons));

        //====================================================================//
        //                  DATASET IDENTIFICATION SECTION                    //
        //====================================================================//
        DataSetIdentificationType dataSetIdentificationType = new DataSetIdentificationType();
        dataSetIdentificationType.setEncodingSpecification(datasetInfo.getEncodingSpecification());
        dataSetIdentificationType.setEncodingSpecificationEdition(datasetInfo.getEncodingSpecificationEdition());
        dataSetIdentificationType.setProductIdentifier(datasetInfo.getProductionIdentifier());
        dataSetIdentificationType.setProductEdition(datasetInfo.getProductionEdition());
        dataSetIdentificationType.setDatasetFileIdentifier(datasetInfo.getFileIdentifier());
        dataSetIdentificationType.setDatasetTitle(datasetInfo.getTitle());
        dataSetIdentificationType.setDatasetReferenceDate(new Date());
        dataSetIdentificationType.setDatasetLanguage(ISO6391.EN);
        dataSetIdentificationType.setDatasetAbstract(datasetInfo.getAbstractText());
        s125Dataset.setDatasetIdentificationInformation(dataSetIdentificationType);

        //====================================================================//
        //              DATASET STRUCTURE INFORMATION SECTION                 //
        //====================================================================//
        DataSetStructureInformationType dataSetStructureInformationType = new DataSetStructureInformationType();
        dataSetStructureInformationType.setCoordMultFactorX(BigInteger.ONE);
        dataSetStructureInformationType.setCoordMultFactorY(BigInteger.ONE);
        dataSetStructureInformationType.setCoordMultFactorZ(BigInteger.ONE);

        //====================================================================//
        //                      DATASET MEMBERS SECTION                       //
        //====================================================================//
        Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(aton -> this.modelMapper.map(aton, S125AtonTypes.fromLocalClass(aton.getClass()).getS125Class()))
                .map(aton -> this.s125GMLFactory.createS125AidsToNavigation(aton))
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(s125Dataset.getImembersAndMembers()::add);

        // Return the dataset
        return s125Dataset;
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
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        final Envelope envelope = new Envelope();
        atonNodes.stream()
                .map(AidsToNavigation::getGeometry)
                .forEach(g -> this.enclosingEnvelopFromGeometry(envelope, g));

        Pos lowerCorner = new Pos();
        lowerCorner.getValues().add(envelope.getMinX());
        lowerCorner.getValues().add(envelope.getMaxY());
        Pos upperCorner = new Pos();
        upperCorner.getValues().add(envelope.getMaxX());
        upperCorner.getValues().add(envelope.getMaxY());

        // And create the bounding by envelope
        BoundingShapeType boundingShapeType = new BoundingShapeType();
        EnvelopeType envelopeType = new EnvelopeType();
        envelopeType.setSrsName("EPSG:4326");
        envelopeType.setLowerCorner(lowerCorner);
        envelopeType.setUpperCorner(upperCorner);
        boundingShapeType.setEnvelope(envelopeType);

        // Finally, return the result
        return boundingShapeType;
    }

    protected Envelope enclosingEnvelopFromGeometry(Envelope envelope, Geometry geometry) {
        final Geometry enclosingGeometry = geometry.getEnvelope();
        final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
        for (Coordinate c : enclosingCoordinates) {
            envelope.expandToInclude(c);
        }
        return envelope;
    }

}
