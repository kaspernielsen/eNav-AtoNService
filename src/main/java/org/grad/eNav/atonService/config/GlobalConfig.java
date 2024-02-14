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

package org.grad.eNav.atonService.config;

import _int.iho.s100.gml.base._5_0.impl.DataSetIdentificationTypeImpl;
import _int.iho.s125.gml.cs0._1.AidsToNavigationType;
import _int.iho.s125.gml.cs0._1.Dataset;
import _int.iho.s125.gml.cs0._1.S100TruncatedDate;
import _int.iho.s125.gml.cs0._1.impl.*;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.grad.eNav.atonService.models.domain.s100.ServiceInformationConfig;
import org.grad.eNav.atonService.models.domain.s125.*;
import org.grad.eNav.atonService.models.domain.secom.SubscriptionRequest;
import org.grad.eNav.atonService.models.dtos.s125.AidsToNavigationDto;
import org.grad.eNav.atonService.models.enums.ReferenceTypeRole;
import org.grad.eNav.atonService.utils.GeometryS125Converter;
import org.grad.eNav.atonService.utils.ReferenceTypeS125Converter;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.atonService.utils.WKTUtils;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.models.SubscriptionRequestObject;
import org.locationtech.jts.io.ParseException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.ValidationException;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.ErrorMessage;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Global Configuration.
 *
 * A class to define the global configuration for the application.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@EnableConfigurationProperties(ServiceInformationConfig.class)
@Slf4j
public class GlobalConfig {

    /**
     * The Application Operator Name Information.
     */
    @Value("${gla.rad.aton-service.info.operatorName:Unknown}")
    private String appOperatorName;

    /**
     * <p>
     * Add an HTTP trace repository in memory to be used for the respective
     * actuator.
     * </p>
     * <p>
     * The functionality has been removed by default in Spring Boot 2.2.0. For
     * more info see:
     * </p>
     * <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2.0-M3-Release-Notes#actuator-http-trace-and-auditing-are-disabled-by-default">...</a>
     *
     * @return the in memory HTTP trance repository
     */
    @ConditionalOnProperty(value = "management.endpoint.httpexchanges.enabled", havingValue = "true")
    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new InMemoryHttpExchangeRepository();
    }

    /**
     * The Model Mapper allows easy mapping between DTOs and domain objects.
     *
     * @return the model mapper bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // ================================================================== //
        // Provide a configuration for all the mappings here to keep tidy     //
        // ================================================================== //

        // Since the S-125 objects contains lists that do NOT have a setter,
        // we can use the protected fields directly to perform the mapping.
        // Note that this creates ambiguity with the existing setters, so we
        // should account for that.
        modelMapper.getConfiguration()
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PROTECTED)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(true);

        // Configure the dataset top-level mapping first
        modelMapper.emptyTypeMap(S125Dataset.class, DatasetImpl.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> modelMapper.map(((S125Dataset)ctx.getSource()).getDatasetIdentificationInformation(), DataSetIdentificationTypeImpl.class) )
                            .map(src -> src, DatasetImpl::setDatasetIdentificationInformation);
                    mapper.using(ctx -> new DatasetImpl.MembersImpl())
                            .map(src -> src, DatasetImpl::setMembers);
                })
                .implicitMappings();

        // Loop all the mapped S-125 AtoN types and configure the model mapper
        // to translate correctly from the S-125 onto the local classes and
        // vice versa
        for(S125AtonTypes atonType : S125AtonTypes.values()) {
            // Skip the unknown type, we don't need it
            if(atonType == S125AtonTypes.UNKNOWN) {
                continue;
            }

            modelMapper.createTypeMap(atonType.getS125Class(), atonType.getLocalClass())
                    .implicitMappings()
                    .addMappings(mapper -> {
                        mapper.skip(AidsToNavigation::setId); // We don't know if the ID is correct so skip it
                        mapper.using(ctx -> S125Utils.s100TruncatedDateToLocalDate(((AidsToNavigationType)ctx.getSource()).getDateStart()))
                                .map(src -> src, AidsToNavigation::setDateStart);
                        mapper.using(ctx -> S125Utils.s100TruncatedDateToLocalDate(((AidsToNavigationType)ctx.getSource()).getDateEnd()))
                                .map(src -> src, AidsToNavigation::setDateEnd);
                        mapper.using(ctx -> S125Utils.s100TruncatedDateToLocalDate(((AidsToNavigationType)ctx.getSource()).getPeriodStart()))
                                .map(src -> src, AidsToNavigation::setPeriodStart);
                        mapper.using(ctx -> S125Utils.s100TruncatedDateToLocalDate(((AidsToNavigationType)ctx.getSource()).getPeriodEnd()))
                                .map(src -> src, AidsToNavigation::setPeriodEnd);
                        mapper.using(ctx -> new GeometryS125Converter().convertToGeometry(((AidsToNavigationType) ctx.getSource())))
                                .map(src -> src, AidsToNavigation::setGeometry);
                    });

            // For structures, don't map the children
            if(atonType.isStructure()) {
                modelMapper.typeMap(atonType.getS125Class(), atonType.getLocalStructureClass())
                        .addMappings(mapper -> {
                            mapper.skip(StructureObject::setChildren);
                        });
            }

            // For equipment, don't map the parent
            if(atonType.isEquipment()) {
                modelMapper.typeMap(atonType.getS125Class(), atonType.getLocalEquipmentClass())
                        .addMappings(mapper -> {
                            mapper.skip(Equipment::setParent);
                        });
            }

            modelMapper.createTypeMap(atonType.getLocalClass(), atonType.getS125Class())
                    .implicitMappings()
                    .addMappings(mapper -> {
                        mapper.using(ctx -> "ID-ATON-" + ((AidsToNavigation) ctx.getSource()).getId())
                                .map(src -> src, AidsToNavigationType::setId);
                        mapper.using(ctx -> S125Utils.localDateToS100TruncatedDate(((AidsToNavigation)ctx.getSource()).getDateStart()))
                                .map(src -> src, AidsToNavigationType::setDateStart);
                        mapper.using(ctx -> S125Utils.localDateToS100TruncatedDate(((AidsToNavigation)ctx.getSource()).getDateEnd()))
                                .map(src -> src, AidsToNavigationType::setDateEnd);
                        mapper.using(ctx -> S125Utils.localDateToS100TruncatedDate(((AidsToNavigation)ctx.getSource()).getPeriodStart()))
                                .map(src -> src, AidsToNavigationType::setPeriodStart);
                        mapper.using(ctx -> S125Utils.localDateToS100TruncatedDate(((AidsToNavigation)ctx.getSource()).getPeriodEnd()))
                                .map(src -> src, AidsToNavigationType::setPeriodEnd);
                        mapper.using(ctx -> modelMapper.map(((AidsToNavigation)ctx.getSource()).getInformations(), new TypeToken<List<InformationTypeImpl>>() {}.getType()) )
                                .map(src -> src, AidsToNavigationTypeImpl::setInformations);
                        mapper.using(ctx -> modelMapper.map(((AidsToNavigation)ctx.getSource()).getFeatureNames(), new TypeToken<List<FeatureNameTypeImpl>>() {}.getType()) )
                                .map(src -> src, AidsToNavigationTypeImpl::setFeatureNames);
                        mapper.using(ctx -> new GeometryS125Converter().convertFromGeometry((AidsToNavigation) ctx.getSource()))
                                .map(src -> src, (dest, val) -> {
                                    try {
                                        new PropertyDescriptor("geometries", atonType.getS125Class()).getWriteMethod().invoke(dest, val);
                                    } catch (Exception ex) {
                                        log.error(ex.getMessage());
                                    }
                                });
                    });

            // For structures, map the children to reference types
            if(atonType.isStructure()) {
                modelMapper.typeMap(atonType.getLocalStructureClass(), atonType.getS125StructureClass())
                        .addMappings(mapper -> {
                            mapper.using(ctx -> new ReferenceTypeS125Converter().convertToReferenceTypes(((StructureObject)ctx.getSource()).getChildren(), ReferenceTypeRole.CHILD))
                                    .map(src-> src, StructureObjectTypeImpl::setchildren);
                        });
            }

            // For equipment, map the parent to single reference type
            if(atonType.isEquipment()) {
                modelMapper.typeMap(atonType.getLocalEquipmentClass(), atonType.getS125EquipmentClass())
                        .addMappings(mapper -> {
                            mapper.using(ctx -> new ReferenceTypeS125Converter().convertToReferenceType(((Equipment) ctx.getSource()).getParent(), ReferenceTypeRole.PARENT))
                                    .map(src-> src, EquipmentTypeImpl::setParent);
                        });
            }
        }

        // Create the Aggregation/Association type maps
        modelMapper.createTypeMap(AggregationImpl.class, Aggregation.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.skip(Aggregation::setId); // We don't know if the ID is correct so skip it
                    mapper.skip(Aggregation::setPeers);
                });
        modelMapper.createTypeMap(AssociationImpl.class, Association.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.skip(Association::setId); // We don't know if the ID is correct so skip it
                    mapper.skip(Association::setPeers);
                });
        modelMapper.createTypeMap(Aggregation.class, AggregationImpl.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.using(ctx -> "ID-AGGR-" + ((Aggregation) ctx.getSource()).getId())
                            .map(src -> src, AggregationImpl::setId);
                    mapper.using(ctx -> new ReferenceTypeS125Converter().convertToReferenceTypes(((Aggregation) ctx.getSource()).getPeers(), ReferenceTypeRole.AGGREGATION))
                            .map(src-> src, AggregationImpl::setPeers);
                });
        modelMapper.createTypeMap(Association.class, AssociationImpl.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.using(ctx -> "ID-ASSO-" + ((Association) ctx.getSource()).getId())
                            .map(src -> src, AssociationImpl::setId);
                    mapper.using(ctx -> new ReferenceTypeS125Converter().convertToReferenceTypes(((Association) ctx.getSource()).getPeers(), ReferenceTypeRole.ASSOCIATION))
                            .map(src-> src, AssociationImpl::setPeers);
                });

        // Create the Base Aids to Navigation type map for the DTOs
        modelMapper.createTypeMap(AidsToNavigation.class, AidsToNavigationDto.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.using(ctx -> S125AtonTypes.fromLocalClass(((AidsToNavigation) ctx.getSource()).getClass()).getDescription())
                            .map(src -> src, AidsToNavigationDto::setAtonType);
                    mapper.using(ctx -> convertTos125DataSet(modelMapper, Collections.singletonList((AidsToNavigation) ctx.getSource())))
                            .map(src -> src, AidsToNavigationDto::setContent);
                });

        // Add the base to all Aids to Navigation DTO Mappings
        for(S125AtonTypes atonType : S125AtonTypes.values()) {
            // Skip the unknown type, we don't need it
            if(atonType == S125AtonTypes.UNKNOWN) {
                continue;
            }
            modelMapper.createTypeMap(atonType.getLocalClass(), AidsToNavigationDto.class)
                    .implicitMappings()
                    .includeBase(AidsToNavigation.class, AidsToNavigationDto.class);
        }

        // Now map the SECOM Subscription Requests
        modelMapper.createTypeMap(SubscriptionRequestObject.class, SubscriptionRequest.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.using(ctx -> Optional.of(ctx)
                            .map(MappingContext::getSource)
                            .map(String.class::cast)
                            .map(g -> {
                                try {
                                    return WKTUtils.convertWKTtoGeometry(g);
                                } catch (ParseException ex) {
                                    throw new ValidationException(Collections.singletonList(new ErrorMessage(ex.getMessage())));
                                }
                            })
                            .orElse(null))
                            .map(SubscriptionRequestObject::getGeometry, SubscriptionRequest::setGeometry);
                });
        // ================================================================== //

        return modelMapper;
    }

    /**
     * Converts a whole list of Aids to Navigation objects into an XML string
     * representation conforming to the S-125 data product specification.
     *
     * @param atons the list of the Aids to Navigation objects
     * @return the respective S-125 data string representation
     */
    public static String convertTos125DataSet(ModelMapper modelMapper, List<AidsToNavigation> atons) {
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(modelMapper);
        final String datasetTitle = CaseUtils.toCamelCase("AtoN Dataset for " + atons.stream()
                .map(AidsToNavigation::getIdCode)
                .collect(Collectors.joining(" ")), true, ' ');
        final S125Dataset s125Dataset = new S125Dataset(datasetTitle);
        final Dataset dataset = s125DatasetBuilder.packageToDataset(s125Dataset, atons);
        try {
            return S125Utils.marshalS125(dataset);
        } catch (JAXBException ex) {
            return "";
        }
    }

}
