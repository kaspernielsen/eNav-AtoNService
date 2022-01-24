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

package org.grad.eNav.atonService;

import org.geotools.data.DataStore;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.config.GlobalConfig;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.security.auth.message.config.RegistrationListener;

import static org.mockito.Mockito.mock;

/**
 * This is a test only configuration that will get activated when the "test"
 * profile is active.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@TestConfiguration
@Import(GlobalConfig.class)
public class TestingConfiguration {

	/**
	 * AtoN message Mapper from Domain to DTO.
	 */
	@Bean
	public DomainDtoMapper<AtonMessage, S125Node> atonMessageToS125Mapper() {
		return new DomainDtoMapper<>();
	}

	/**
	 * Feign depends on the Eureka Registration Listener bean so let's mock one
	 * up.
	 *
	 * @return the Eureka Registration Listener bean
	 */
	@Bean
	RegistrationListener registrationListener() {
		return mock(RegistrationListener.class);
	}

	/**
	 * MOck a Geomesa Data Store bean so that we pretend we have a connection
	 * while the actual GS Data Store configuration is not enabled.
	 *
	 * @return the Geomesa Data Store bean
	 */
	@Bean
	DataStore gsDataStore() {
		return mock(DataStore.class);
	}

}
