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

import _int.iala_aism.s125.gml._0_0.DataSet;
import _int.iala_aism.s125.gml._0_0.MemberType;
import _int.iala_aism.s125.gml._0_0.S125AidsToNavigationType;
import _net.opengis.gml.profiles.AbstractFeatureMemberType;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.FidFilterImpl;
import org.grad.eNav.atonService.models.GeomesaData;
import org.grad.eNav.atonService.models.GeomesaS125;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125AtonTypes;
import org.grad.eNav.atonService.models.dtos.S100AbstractNode;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.atonService.services.AidsToNavigationService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
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
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    /**
     * The S-125 Data Channel to publish the published data to.
     */
    @Autowired
    @Qualifier("s125PublicationChannel")
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Data Channel to publish the deleted data to.
     */
    @Autowired
    @Qualifier("s125DeletionChannel")
    PublishSubscribeChannel s125DeletionChannel;

    // Component Variables
    protected DataStore consumer;
    protected GeomesaData<S125Node> geomesaData;
    protected Geometry geometry;
    protected SimpleFeatureSource featureSource;

    /**
     * Once the listener has been initialised, it will create a consumer of
     * the data store provided and publish the incoming messages into the
     * AtoN data channel.
     *
     * @param consumer      The data store to consume the messages from
     */
    public void init(DataStore consumer,
                     GeomesaData<S125Node> geomesaData,
                     Geometry geometry) throws IOException {
        // Remember the input data
        this.consumer = consumer;
        this.geomesaData = geomesaData;
        this.geometry = geometry;

        // And add the feature listener to start reading
        this.featureSource = this.consumer.getFeatureSource(this.geomesaData.getTypeName());
        Optional.ofNullable(this.featureSource).ifPresent(fs -> fs.addFeatureListener(this));

        // Log an information message
        log.info(String.format("Initialised Geomesa Datastore Listener for area: %s",
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
     * The main data store listener operation where events are being handled.
     *
     * @param featureEvent      The feature event that took place
     */
    @Transactional
    public void changed(FeatureEvent featureEvent) {
        // We are only interested in Kafka Feature Messages, otherwise don't bother
        if(!(featureEvent instanceof KafkaFeatureEvent)) {
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
                    .flatMap(this::parseS125Dataset)
                    .map(MessageBuilder::withPayload)
                    .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                    .map(builder -> builder.setHeader("deletion", false))
                    .map(MessageBuilder::build)
                    .forEach(msg -> {
                        this.aidsToNavigationService.save(msg.getPayload());
                        this.s125PublicationChannel.send(msg);
                    });
        }
        // For feature deletions,
        else if (featureEvent.getType() == FeatureEvent.Type.REMOVED) {
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
                    .map(this.aidsToNavigationService::findByAtonNumber)
                    .map(aton -> aton.orElse(null))
                    .filter(Objects::nonNull)
                    .map(MessageBuilder::withPayload)
                    .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125))
                    .map(builder -> builder.setHeader("deletion", true))
                    .map(MessageBuilder::build)
                    .forEach(msg -> {
                        this.aidsToNavigationService.delete(msg.getPayload().getId());
                        this.s125DeletionChannel.send(msg);
                    });
        }
    }

    /**
     * A helper that processes the S125Node entry provided and parses the
     * contained S-125 Aids to Navigation entries
     *
     * @param s125Node  the S-125 dataset node to be processed
     * @return the contained list of Aids to Navigation entries
     */
    protected Stream<? extends AidsToNavigation> parseS125Dataset(S125Node s125Node){
        return Optional.of(s125Node)
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
                .filter(Objects::nonNull);
    }

}
