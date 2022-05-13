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

package org.grad.eNav.atonService.components;

import _int.iala_aism.s125.gml._0_0.*;
import _net.opengis.gml.profiles.AbstractFeatureMemberType;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.FidFilterImpl;
import org.grad.eNav.atonService.models.GeomesaData;
import org.grad.eNav.atonService.models.GeomesaS125;
import org.grad.eNav.atonService.models.domain.s125.*;
import org.grad.eNav.atonService.models.dtos.S100AbstractNode;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.atonService.utils.GeometryS125Converter;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.geomesa.kafka.utils.KafkaFeatureEvent;
import org.locationtech.jts.geom.Geometry;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * The AtoN Geomesa Data Store Listener Class
 *
 * This class defines the main operation of the AtoN listening operation
 * on the Geomesa Kafka Data Store.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class S125GDSListener implements FeatureListener {

    /**
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The AtoN Data Channel to publish the incoming data to.
     */
    @Autowired
    @Qualifier("publishSubscribeChannel")
    PublishSubscribeChannel publishSubscribeChannel;

    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    // Component Variables
    protected DataStore consumer;
    protected GeomesaData<S125Node> geomesaData;
    protected Geometry geometry;
    protected SimpleFeatureSource featureSource;
    protected boolean deletionHandler;

    /**
     * Once the listener has been initialised, it will create a consumer of
     * the data store provided and publish the incoming messages into the
     * AtoN data channel.
     *
     * @param consumer      The data store to consume the messages from
     */
    public void init(DataStore consumer,
                     GeomesaData<S125Node> geomesaData,
                     Geometry geometry,
                     boolean handleDeletions) throws IOException {
        // Remember the input data
        this.consumer = consumer;
        this.geomesaData = geomesaData;
        this.geometry = geometry;
        this.deletionHandler = handleDeletions;

        // Configure the model mapper for S-125
        Optional.ofNullable(this.modelMapper).ifPresent(this::initialiseModelMapper);

        // And add the feature listener to start reading
        this.featureSource = this.consumer.getFeatureSource(this.geomesaData.getTypeName());
        Optional.ofNullable(this.featureSource).ifPresent(fs -> fs.addFeatureListener(this));

        // Log an information message
        log.info(String.format("Initialised AtoN message listener for area: %s",
                 GeometryJSONConverter.convertFromGeometry(geometry)));
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("AtoN message Listener is shutting down...");
        this.featureSource.removeFeatureListener(this);
    }

    /**
     * Returns whether the listener is setup as to handle S-125 station node
     * deletions.
     *
     * @return Whether the listener handles S-125 deletion events
     */
    public boolean isDeletionHandler() {
        return this.deletionHandler;
    }

    /**
     * The main data store listener operation where events are being handled.
     *
     * @param featureEvent      The feature event that took place
     */
    public void changed(FeatureEvent featureEvent) {
        // We are only interested in Kafka Feature Messages, otherwise don't bother
        if(!(featureEvent instanceof  KafkaFeatureEvent)) {
            return;
        }

        // For feature additions/changes
        if (featureEvent.getType() == FeatureEvent.Type.CHANGED) {
            // Extract the S-125 message and send it
            Optional.of(featureEvent)
                    .filter(KafkaFeatureEvent.KafkaFeatureChanged.class::isInstance)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged.class::cast)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged::feature)
                    .filter(this.geomesaData.getSubsetFilter()::evaluate)
                    .map(Collections::singletonList)
                    .map(sl -> new GeomesaS125().retrieveData(sl))
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(MessageBuilder::withPayload)
                    .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, this.geomesaData.getTypeName()))
                    .map(MessageBuilder::build)
                    .forEach(msg -> {
                        this.saveAtonMessage(msg.getPayload());
                        this.publishSubscribeChannel.send(msg);
                    });
        }
        // For feature deletions,
        else if (featureEvent.getType() == FeatureEvent.Type.REMOVED && this.deletionHandler) {
            // Extract the S-125 message UID and use it to delete all referencing nodes
            Optional.of(featureEvent)
                    .filter(KafkaFeatureEvent.KafkaFeatureRemoved.class::isInstance)
                    .map(KafkaFeatureEvent.KafkaFeatureRemoved.class::cast)
                    .map(KafkaFeatureEvent.KafkaFeatureRemoved::getFilter)
                    .filter(FidFilterImpl.class::isInstance)
                    .map(FidFilterImpl.class::cast)
                    .map(FidFilterImpl::getFidsSet)
                    .orElse(Collections.emptySet())
                    .stream()
                    .forEach(this.aidsToNavigationService::deleteByAtonNumber);
        }
    }

    /**
     * A helper that processes the S125Node entry provided and stored it in the
     * database for future reference.
     *
     * @param s125Node  the S125Node to be saved
     */
    protected void saveAtonMessage(S125Node s125Node){
        // Create or update the AtoN message entry
        Optional.of(s125Node)
                .map(S100AbstractNode::getContent)
                .map(xml -> {
                    try {
                        return S125Utils.unmarshallS125(xml);
                    } catch (JAXBException ex) {
                        return null;
                    }
                })
                .map(DataSet::getImembersAndMembers)
                .filter(((Predicate<List<AbstractFeatureMemberType>>) List::isEmpty).negate())
                .orElse(Collections.emptyList())
                .stream()
                .filter(MemberType.class::isInstance)
                .map(MemberType.class::cast)
                .map(MemberType::getAbstractFeature)
                .map(JAXBElement::getValue)
                .filter(S125AidsToNavigationType.class::isInstance)
                .map(S125AidsToNavigationType.class::cast)
                .map(s125Aton -> this.modelMapper.map(s125Aton, S125AtonTypes.fromS125Class(s125Aton.getClass()).getLocalClass()))
                .filter(Objects::nonNull)
                .forEach(this.aidsToNavigationService::save);
    }

    /**
     * This helper function initialises the model mapper for the S-125 to
     * local Aids to Navigation entity objects.
     *
     * Note that since the local {@link AidsToNavigation) class is abstract
     * we need to specify the mapping for each of the implementation classes.
     *
     * @param modelMapper the model mapper to be initialised
     */
    protected void initialiseModelMapper(ModelMapper modelMapper) {
        // Loop all the mapped S-125 AtoN types and configure the model mapper
        for(S125AtonTypes type : S125AtonTypes.values()) {
            modelMapper.createTypeMap(type.getS125Class(), type.getLocalClass())
                    .implicitMappings()
                    .addMappings(mapper -> {
                        mapper.skip(AidsToNavigation::setId);
                        mapper.using(ctx -> new GeometryS125Converter().convertToGeometry(((S125AidsToNavigationType) ctx.getSource())))
                                .map(src-> src, AidsToNavigation::setGeometry);
                    });
        }
    }

}
