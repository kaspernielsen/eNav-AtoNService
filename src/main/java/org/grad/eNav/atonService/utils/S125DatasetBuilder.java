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

import _int.iala_aism.s125.gml._0_0.*;
import _net.opengis.gml.profiles.BoundingShapeType;
import _net.opengis.gml.profiles.EnvelopeType;
import _net.opengis.gml.profiles.Pos;
import jakarta.xml.bind.JAXBElement;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.modelmapper.ModelMapper;

import java.util.*;

public class S125DatasetBuilder {

    /**
     * The Model Mapper
     */
    private ModelMapper modelMapper;

    // Class Variables
    private _int.iala_aism.s125.gml._0_0.ObjectFactory s125GMLFactory;

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
    public Dataset packageToDataset(S125Dataset s125Dataset, List<AidsToNavigation> atons) {
        // Initialise the dataset
        Dataset dataset = this.modelMapper.map(s125Dataset, Dataset.class);

        // Always use a UUID as an ID
        if(Objects.isNull(dataset.getId())) {
            dataset.setId(Optional.ofNullable(s125Dataset).map(S125Dataset::getUuid).orElse(UUID.randomUUID()).toString());
        }

        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        dataset.setBoundedBy(this.generateBoundingShape(atons));
        /*dataset.setPointsAndMultiPointsAndCurves(Optional.ofNullable(s125Dataset)
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
                .collect(Collectors.toList()));*/

        //====================================================================//
        //                      DATASET MEMBERS SECTION                       //
        //====================================================================//
        // Add the AtoN members
        Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(aton -> this.modelMapper.map(aton, S125AtonTypes.fromLocalClass(aton.getClass()).getS125Class()))
                //.map(aton -> this.s125GMLFactory.createS125AidsToNavigation(aton)) // Causes problems by introducing "xsi:type"
                .map(this::createJaxbElement)
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(dataset.getImembersAndMembers()::add);

        // Append the aggregations
        Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(AidsToNavigation::getAggregations)
                .flatMap(Set::stream)
                .distinct()
                .map(agg -> this.modelMapper.map(agg, AggregationType.class))
                .map(this.s125GMLFactory::createAggregation)
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(dataset.getImembersAndMembers()::add);

        // Append the associations
        Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(AidsToNavigation::getAssociations)
                .flatMap(Set::stream)
                .distinct()
                .map(ass -> this.modelMapper.map(ass, AssociationType.class))
                .map(this.s125GMLFactory::createAssociation)
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(dataset.getImembersAndMembers()::add);

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

        Pos lowerCorner = new Pos();
        lowerCorner.setValue(new Double[]{envelope.getMinX(), envelope.getMaxY()});
        Pos upperCorner = new Pos();
        upperCorner.setValue(new Double[]{envelope.getMaxX(), envelope.getMaxY()});

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

    /**
     * Creates a respective JAXB element based on the provided S125 AtoN type.
     * This generation method fixes the problem where the generated XML
     * dataset members have the type identified through the "xsi:type"
     * argument.
     *
     * @return the generated JAXElement
     */
    protected JAXBElement<? extends AidsToNavigationType> createJaxbElement(AidsToNavigationType s125AidsToNavigationType) {
        switch(S125AtonTypes.fromS125Class(s125AidsToNavigationType.getClass())) {
            case CARDINAL_BEACON:
                return this.s125GMLFactory.createBeaconCardinal((BeaconCardinalType) s125AidsToNavigationType);
            case LATERAL_BEACON:
                return this.s125GMLFactory.createBeaconLateral((BeaconLateralType) s125AidsToNavigationType);
            case ISOLATED_DANGER_BEACON:
                return this.s125GMLFactory.createBeaconIsolatedDanger((BeaconIsolatedDangerType) s125AidsToNavigationType);
            case SAFE_WATER_BEACON:
                return this.s125GMLFactory.createBeaconSafeWater((BeaconSafeWaterType) s125AidsToNavigationType);
            case SPECIAL_PURPOSE_BEACON:
                return this.s125GMLFactory.createBeaconSpecialPurposeGeneral((BeaconSpecialPurposeGeneralType) s125AidsToNavigationType);
            case CARDINAL_BUOY:
                return this.s125GMLFactory.createBuoyCardinal((BuoyCardinalType) s125AidsToNavigationType);
            case LATERAL_BUOY:
                return this.s125GMLFactory.createBuoyLateral((BuoyLateralType) s125AidsToNavigationType);
            case INSTALLATION_BUOY:
                return this.s125GMLFactory.createBuoyInstallation((BuoyInstallationType) s125AidsToNavigationType);
            case ISOLATED_DANGER_BUOY:
                return this.s125GMLFactory.createBuoyIsolatedDanger((BuoyIsolatedDangerType) s125AidsToNavigationType);
            case SAFE_WATER_BUOY:
                return this.s125GMLFactory.createBuoySafeWater((BuoySafeWaterType) s125AidsToNavigationType);
            case SPECIAL_PURPOSE_BUOY:
                return this.s125GMLFactory.createBuoySpecialPurposeGeneral((BuoySpecialPurposeGeneralType) s125AidsToNavigationType);
            case DAYMARK:
                return this.s125GMLFactory.createDaymark((DaymarkType) s125AidsToNavigationType);
            case FOG_SIGNAL:
                return this.s125GMLFactory.createFogSignal((FogSignalType) s125AidsToNavigationType);
            case LIGHT:
                return this.s125GMLFactory.createLight((LightType) s125AidsToNavigationType);
            case  LIGHT_FLOAT:
                return this.s125GMLFactory.createLightFloat((LightFloatType) s125AidsToNavigationType);
            case LANDMARK:
                return this.s125GMLFactory.createLandmark((LandmarkType) s125AidsToNavigationType);
            case LIGHTHOUSE:
                return this.s125GMLFactory.createLighthouse((LighthouseType) s125AidsToNavigationType);
            case LIGHT_VESSEL:
                return this.s125GMLFactory.createLightVessel((LightVesselType) s125AidsToNavigationType);
            case NAVIGATION_LINE:
                return this.s125GMLFactory.createNavigationLine((NavigationLineType) s125AidsToNavigationType);
            case OFFSHORE_PLATFORM:
                return this.s125GMLFactory.createOffshorePlatform((OffshorePlatformType) s125AidsToNavigationType);
            case PHYSICAL_AIS_ATON:
                return this.s125GMLFactory.createPhysicalAISAidToNavigation((PhysicalAISAidToNavigationType) s125AidsToNavigationType);
            case PILE:
                return this.s125GMLFactory.createPile((PileType) s125AidsToNavigationType);
            case RADAR_REFLECTOR:
                return this.s125GMLFactory.createRadarReflector((RadarReflectorType) s125AidsToNavigationType);
            case RADIO_STATION:
                return this.s125GMLFactory.createRadioStation((RadioStationType) s125AidsToNavigationType);
            case RECOMMENDED_TRACK:
                return this.s125GMLFactory.createRecommendedTrack((RecommendedTrackType) s125AidsToNavigationType);
            case RETRO_REFLECTOR:
                return this.s125GMLFactory.createRetroReflector((RetroReflectorType) s125AidsToNavigationType);
            case SILO_TANK:
                return this.s125GMLFactory.createSiloTank((SiloTankType) s125AidsToNavigationType);
            case SYNTHETIC_AIS_ATON:
                return this.s125GMLFactory.createSyntheticAISAidToNavigation((SyntheticAISAidToNavigationType) s125AidsToNavigationType);
            case TOPMARK:
                return this.s125GMLFactory.createTopmark((TopmarkType) s125AidsToNavigationType);
            case VIRTUAL_AIS_ATON:
                return this.s125GMLFactory.createVirtualAISAidToNavigation((VirtualAISAidToNavigationType) s125AidsToNavigationType);
            default:
                return this.s125GMLFactory.createAidsToNavigation(s125AidsToNavigationType);
        }
    }

}
