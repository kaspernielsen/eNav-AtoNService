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
import _net.opengis.gml.profiles.*;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.domain.AtonMessageType;
import org.grad.eNav.atonService.models.dtos.S100AbstractNode;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Boolean;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

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
    public static AtonMessage toAtonMessage(S100AbstractNode snode) {
        // Sanity check
        if(Objects.isNull(snode)) {
            return null;
        }

        // We first need to extract the bounding box of the snode message
        final AbstractFeatureType dataset;

        // Unmarshall the station node message  using the appropriate message time
        try {
            if (S125Node.class.equals(snode.getClass())) {
                dataset = S100Utils.unmarshallS125(snode.getContent());
            } else {
                log.error("Unsupported S100 dataset translation operation detected...");
                return null;
            }
        } catch (JAXBException | NumberFormatException ex) {
            log.error(ex.getMessage());
            return null;
        }

        // Setup a geometry factory based on the bounding box SRS
        final Integer srid = Optional.ofNullable(dataset)
                .map(AbstractFeatureType::getBoundedBy)
                .map(BoundingShapeType::getEnvelope)
                .map(EnvelopeType::getSrsName)
                .map(crs -> crs.split(":")[1])
                .map(Integer::valueOf)
                .orElse(4326);
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);

        // Now construct the AtoN Message based on the node type
        if (S125Node.class.equals(snode.getClass())) {
            // Construct the geometry using the S125 Member NavAid Geometry
            Geometry geometry = Optional.ofNullable(dataset)
                    .filter(DataSet.class::isInstance)
                    .map(DataSet.class::cast)
                    .map(DataSet::getMembersAndImembers)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(MemberType.class::isInstance)
                    .map(MemberType.class::cast)
                    .map(MemberType::getAbstractFeature)
                    .map(JAXBElement::getValue)
                    .filter(S125NavAidStructureType.class::isInstance)
                    .map(S125NavAidStructureType.class::cast)
                    .map(S125NavAidStructureType::getGeometry)
                    .map(PointCurveSurfaceProperty::getPointProperty)
                    .map(PointProperty::getPoint)
                    .map(PointType::getPos)
                    .map(Pos::getValues)
                    .map(pos -> new Coordinate(pos.get(1), pos.get(0)))
                    .map(factory::createPoint)
                    .findFirst()
                    .orElse(null);
            // Construct the AtoN message
            return new AtonMessage(
                    S125Node.class.cast(snode).getAtonUID(),
                    AtonMessageType.S125,
                    geometry,
                    snode.getContent());
        } else {
            // Nothing to return
            return null;
        }

    }

}
