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
import _net.opengis.gml.profiles.AbstractFeatureMemberType;
import _net.opengis.gml.profiles.AbstractFeatureType;
import _net.opengis.gml.profiles.Pos;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.dtos.S100AbstractNode;
import org.grad.eNav.atonService.models.dtos.S124Node;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.vdes1000.ais.messages.AISMessage21;
import org.grad.vdes1000.generic.AtonType;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The S-100 Utility Class.
 *
 * A static utility function class that allows easily manipulation of the S-100
 * messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class S100Utils {

    /**
     * Using the S125 utilities we can marshall back an S125 DatasetType
     * object it's XML view.
     *
     * @param datasetType the Service Instance object
     * @return the marshalled S125 message XML representation
     */
    public static String marshalS125(DataSet datasetType) throws JAXBException {
        // Create the JAXB objects
        JAXBContext jaxbContext = JAXBContext.newInstance(DataSet.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Transform the G1128 object to an output stream
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(datasetType, xmlStream);

        // Return the XML string
        return xmlStream.toString();
    }

    /**
     * The S125Node object contains the S125 XML content of the message. We
     * can easily translate that into an S125 DatasetType object so that it
     * can be accessed more efficiently.
     *
     * @param s125 the S125 message content
     * @return The unmarshalled S125 DatasetType object
     * @throws JAXBException
     */
    public static DataSet unmarshallS125(String s125) throws JAXBException {
        // Create the JAXB objects
        JAXBContext jaxbContext = JAXBContext.newInstance(DataSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // Transform the S125 context into an input stream
        ByteArrayInputStream is = new ByteArrayInputStream(s125.getBytes());

        // And translate
        return (DataSet) JAXBIntrospector.getValue(jaxbUnmarshaller.unmarshal(is));
    }

    /**
     * This helper function translates the provided SNode domain object to
     * a S100AbstractNode implementing DTO. This can be used when the service
     * response to a client, rather than an internal component.
     *
     * @param snode the SNode object to be translated to a DTO
     * @return the DTO generated from the provided SNode object
     */
    public static S100AbstractNode toS100Dto(AtonMessage snode) {
        // Sanity check
        if(Objects.isNull(snode)) {
            return null;
        }

        // We first need to extract the bounding box of the snode message
        final AbstractFeatureType dataset;

        // Unmarshall the station node message  using the appropriate message time
        try {
            switch (snode.getType()) {
                case S125:
                    dataset = S100Utils.unmarshallS125(snode.getMessage());
                    break;
                default:
                    log.error("Unsupported S100 dataset translation operation detected...");
                    return null;
            }
        } catch (JAXBException | NumberFormatException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Find out the bounding box
        final List<Double> point = dataset.getBoundedBy().getEnvelope().getLowerCorner().getValues();
        final String crsName = dataset.getBoundedBy().getEnvelope().getSrsName();
        final Integer srid = Optional.ofNullable(crsName).map(crs -> crs.split(":")[1]).map(Integer::valueOf).orElse(null);
        final JsonNode  bbox = GeoJSONUtils.createGeoJSONPoint(point.get(1), point.get(0), srid);

        // Now construct the DTO based on the SNode type
        switch (snode.getType()) {
            case S124:
                return new S124Node(snode.getUid(), bbox, snode.getMessage());
            case S125:
                return new S125Node(snode.getUid(), bbox, snode.getMessage());
            default:
                return null;
        }
    }

    /**
     * Constructors from an S125Node object.
     *
     * @param s125Node the S125Node object
     * @throws JAXBException when the S125Node XML content cannot be parsed
     */
    public static AISMessage21 s125ToAisMessage21(S125Node s125Node) throws JAXBException {
        // Default at first
        AISMessage21 aisMessage21 = new AISMessage21();

        // Try to unmarshall the S125Node object
        DataSet dataset = unmarshallS125(s125Node.getContent());

        // Extract the S125 Member NavAid Information
        Optional.ofNullable(dataset)
                .map(DataSet::getMembersAndImembers)
                .filter(((Predicate<List<AbstractFeatureMemberType>>) List::isEmpty).negate())
                .map(l -> l.get(0))
                .filter(MemberType.class::isInstance)
                .map(MemberType.class::cast)
                .map(MemberType::getAbstractFeature)
                .map(JAXBElement::getValue)
                .filter(S125NavAidStructureType.class::isInstance)
                .map(S125NavAidStructureType.class::cast)
                .ifPresent(navAid -> {
                    Optional.of(dataset)
                            .map(DataSet::getId)
                            .ifPresent(aisMessage21::setUid);
                    Optional.of(navAid)
                            .map(S125NavAidStructureType::getFeatureName)
                            .map(S125FeatureNameType::getName)
                            .ifPresent(aisMessage21::setName);
                    Optional.of(navAid).
                            map(S125NavAidStructureType::getAtonType)
                            .map(S125AtonType::value)
                            .map(AtonType::fromString)
                            .ifPresent(aisMessage21::setAtonType);
                    Optional.of(navAid)
                            .map(S125NavAidStructureType::getGeometry)
                            .map(PointCurveSurfaceProperty::getPointProperty)
                            .map(PointProperty::getPoint)
                            .map(PointType::getPos)
                            .map(Pos::getValues)
                            .map(list -> list.get(0))
                            .ifPresent(aisMessage21::setLatitude);
                    Optional.of(navAid)
                            .map(S125NavAidStructureType::getGeometry)
                            .map(PointCurveSurfaceProperty::getPointProperty)
                            .map(PointProperty::getPoint)
                            .map(PointType::getPos)
                            .map(Pos::getValues)
                            .map(list -> list.get(1))
                            .ifPresent(aisMessage21::setLongitude);
                    aisMessage21.setMmsi(navAid.getMmsi());
                    aisMessage21.setLength(navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getLength()).orElse(0)));
                    aisMessage21.setWidth(navAid.isVatonFlag() ? 0 : Math.round(Optional.ofNullable(navAid.getWidth()).orElse(0)));
                    aisMessage21.setRaim(navAid.isRaimFlag());
                    aisMessage21.setVaton(navAid.isVatonFlag());
                    aisMessage21.setTimestamp(LocalDateTime.now());
                });

        //Return the populated AIS message
        return aisMessage21;
    }

}
