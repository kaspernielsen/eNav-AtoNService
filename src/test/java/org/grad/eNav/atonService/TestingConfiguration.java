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

package org.grad.eNav.atonService;

import org.geotools.data.DataStore;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.config.GlobalConfig;
import org.grad.eNav.atonService.models.domain.DatasetContentLog;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.dtos.DatasetContentLogDto;
import org.grad.eNav.atonService.models.dtos.s125.AidsToNavigationDto;
import org.grad.eNav.atonService.models.dtos.s125.S125DataSetDto;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.mock;

/**
 * This is a test only configuration that will get activated when the "test"
 * profile is active.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@TestConfiguration
@Import(GlobalConfig.class)
@TestPropertySource({"classpath:application.properties"})
public class TestingConfiguration {

	/**
	 * Aids to Navigation Mapper from Domain to DTO.
	 */
	@Bean
	public DomainDtoMapper<AidsToNavigation, AidsToNavigationDto> aidsToNavigationToDtoMapper() {
		return new DomainDtoMapper<>();
	}

	/**
	 * DatasetMapper from Domain to DTO.
	 */
	@Bean
	public DomainDtoMapper<S125Dataset, S125DataSetDto> datasetDtoMapper() {
		return new DomainDtoMapper<>();
	}

	/**
	 * DatasetMapper from DTO to Domain.
	 */
	@Bean
	public DomainDtoMapper<S125DataSetDto, S125Dataset> datasetDomainMapper() {
		return new DomainDtoMapper<>();
	}

	/**
	 * DatasetContentLogMapper from Domain to DTO.
	 */
	@Bean
	public DomainDtoMapper<DatasetContentLog, DatasetContentLogDto> datasetContentLogDtoMapper() {
		return new DomainDtoMapper<>();
	}

	/**
	 * The registration listener for feign registers the client inside a
	 * client repository which is als needed, hence mocked.
	 */
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return mock(ClientRegistrationRepository.class);
	}

	/**
	 * Mock a Geomesa Data Store bean so that we pretend we have a connection
	 * while the actual GS Data Store configuration is not enabled.
	 *
	 * @return the Geomesa Data Store bean
	 */
	@Bean
	DataStore gsDataStore() {
		return mock(DataStore.class);
	}

}
