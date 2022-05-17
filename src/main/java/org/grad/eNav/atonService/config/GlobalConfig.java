/*
 * Copyright (c) 2021 Maritime Connectivity Platform Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.config;

import _int.iala_aism.s125.gml._0_0.*;
import _int.iho.s100.gml.base._1_0_Ext.CurveProperty;
import _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty;
import _int.iho.s100.gml.base._1_0_Ext.PointProperty;
import _int.iho.s100.gml.base._1_0_Ext.SurfaceProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.grad.eNav.atonService.models.domain.s125.*;
import org.grad.eNav.atonService.models.dtos.s125.AidsToNavigationDto;
import org.grad.eNav.atonService.utils.GeometryS125Converter;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBException;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Global Configuration.
 *
 * A class to define the global configuration for the application.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@Slf4j
public class GlobalConfig {

    /**
     * The Application Operator Name Information.
     */
    @Value("${gla.rad.aton-service.info.operatorName:Unknown}")
    private String appOperatorName;

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
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(true);

        // Loop all the mapped S-125 AtoN types and configure the model mapper
        // to translate correctly from the S-125 onto the local classes
        for(S125AtonTypes atonType : S125AtonTypes.values()) {
            modelMapper.createTypeMap(atonType.getS125Class(), atonType.getLocalClass())
                    .implicitMappings()
                    .addMappings(mapper -> {
                        mapper.skip(AidsToNavigation::setId); // We don't know if the ID is correct so skip it
                        mapper.using(ctx -> new GeometryS125Converter().convertToGeometry(((S125AidsToNavigationType) ctx.getSource())))
                                .map(src-> src, AidsToNavigation::setGeometry);

                        // For some reason the MMSI code doesn't get mapped properly?!
                        if(atonType == S125AtonTypes.VIRTUAL_AIS_ATON) {
                            mapper.map(src -> ((S125VirtualAISAidToNavigationType)src).getMMSICode(),
                                    (dest, val) -> ((VirtualAISAidToNavigation)dest).setMmsiCode((BigDecimal) val));
                        }
                        if(atonType == S125AtonTypes.PHYSICAL_AIS_ATON) {
                            mapper.map(src -> ((S125PhysicalAISAidToNavigationType)src).getMMSICode(),
                                    (dest, val) -> ((PhysicalAISAidToNavigation)dest).setMmsiCode((BigDecimal) val));
                        }
                        if(atonType == S125AtonTypes.SYNTHETIC_AIS_ATON) {
                            mapper.map(src -> ((S125SyntheticAISAidToNavigationType)src).getMMSICode(),
                                    (dest, val) -> ((SyntheticAISAidToNavigation)dest).setMmsiCode((BigDecimal) val));
                        }
                    });
        }

        // Create the Base Aids to Navigation type map
        modelMapper.createTypeMap(AidsToNavigation.class, AidsToNavigationDto.class)
                .implicitMappings()
                .addMappings(mapper -> {
                    mapper.using(ctx -> S125AtonTypes.fromLocalClass(((AidsToNavigation) ctx.getSource()).getClass()).getDescription())
                            .map(src -> src, AidsToNavigationDto::setAtonType);
                    mapper.using(ctx -> this.convertTos125DataSet(modelMapper, Collections.singletonList((AidsToNavigation) ctx.getSource())))
                            .map(src -> src, AidsToNavigationDto::setContent);
                });

        // Add the base to all Aids to Navigation Mappings
        for(S125AtonTypes atonType : S125AtonTypes.values()) {
            // Skip the unknown type, we don't need it
            if(atonType == S125AtonTypes.UNKNOWN) {
                continue;
            }
            modelMapper.createTypeMap(atonType.getLocalClass(), AidsToNavigationDto.class)
                    .implicitMappings()
                    .includeBase(AidsToNavigation.class, AidsToNavigationDto.class);
            modelMapper.createTypeMap(atonType.getLocalClass(), atonType.getS125Class())
                    .implicitMappings()
                    .addMappings(mapper -> {
                        mapper.map(AidsToNavigation::getId, S125AidsToNavigationType::setId);
                        mapper.using(ctx -> this.convertToS125Geometry((AidsToNavigation) ctx.getSource()))
                                .map(src -> src, (dest, val) -> {
                                    try {
                                        new PropertyDescriptor("geometry", atonType.getS125Class()).getWriteMethod().invoke(dest, val);
                                    } catch (Exception ex) {
                                        System.out.println(ex.getMessage());
                                        //this.log.error(ex.getMessage());
                                    }
                                });
                    });
        }

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
    protected String convertTos125DataSet(ModelMapper modelMapper, List<AidsToNavigation> atons) {
        final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(modelMapper);
        final String datasetTitle = CaseUtils.toCamelCase("AtoN Dataset for " + atons.stream()
                .map(AidsToNavigation::getAtonNumber)
                .collect(Collectors.joining(" ")), true, ' ');
        final S125DataSet s125Dataset = new S125DataSet(datasetTitle);
        final DataSet dataset = s125DatasetBuilder.packageToDataset(s125Dataset, atons);
        try {
            return S125Utils.marshalS125(dataset);
        } catch (JAXBException ex) {
            this.log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * Converts the geometry for an Aids to Navigation object for the local JTS
     * format used for persistence to the S-125 data product specification
     * compatible one.
     *
     * @param aidsToNavigation the Aids to Navigation to get the geometry from
     * @return the S-125 compliant geometry description
     */
    protected Object convertToS125Geometry(AidsToNavigation aidsToNavigation) {
        PointCurveSurfaceProperty pointCurveSurfaceProperty = new GeometryS125Converter().convertFromGeometry(aidsToNavigation);
        if(pointCurveSurfaceProperty.getSurfaceProperty() != null) {
            SurfaceProperty surfaceProperty = new SurfaceProperty();
            surfaceProperty.setSurfaceProperty(pointCurveSurfaceProperty.getSurfaceProperty());
            return surfaceProperty;
        } else if(pointCurveSurfaceProperty.getSurfaceProperty() != null) {
            CurveProperty curveProperty = new CurveProperty();
            curveProperty.setCurveProperty(pointCurveSurfaceProperty.getCurveProperty());
            return curveProperty;
        } else {
            PointProperty pointProperty = new PointProperty();
            pointProperty.setPointProperty(pointCurveSurfaceProperty.getPointProperty());
            return pointProperty;
        }
    }

}
