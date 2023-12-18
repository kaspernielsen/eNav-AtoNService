/*
 * Copyright (c) 2023 GLA Research and Development Directorate
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

package org.grad.eNav.atonService.pacts;

import au.com.dius.pact.provider.junitsupport.State;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * The interface for testing the SECOM GetSummary controller using the Pacts
 * consumer driver contracts.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface GetSummarySecomControllerTestInterface {

    /**
     * Provides a geometry factory to setup test geometries.
     *
     * @return the test geometry factory
     */
    GeometryFactory getGeometryFactory();

    /**
     * Provides the mocked dataset service to the tests.
     *
     * @return the mocked dataset service.
     */
    DatasetService getDatasetService();

    /**
     * Provides the mocked UnLoCode service to the tests.
     *
     * @return the mocked UnLoCode service.
     */
    UnLoCodeService getUnLoCodeService();

    /**
     * Test that the SECOM Get Summary interface will return an appropriate
     * response on various queries.
     *
     * @param data the request data
     */
    @State("Test SECOM Get Summary") // Method will be run before testing interactions that require "with-data" state
    default void testSecomGetSummarySuccess(Map<?,?> data) {
        // Create a new dataset for testing
        S125Dataset s125Dataset = new S125Dataset("TestDataset");
        s125Dataset.setUuid(UUID.randomUUID());
        s125Dataset.setGeometry(this.getGeometryFactory().createPoint(new Coordinate(52.98, 2.28)));
        s125Dataset.setLastUpdatedAt(LocalDateTime.now());
        s125Dataset.setCancelled(false);

        // Mock the service responses
        doReturn(new PageImpl<>(Collections.singletonList(s125Dataset), Pageable.ofSize(1), 1))
                .when(this.getDatasetService())
                .findAll(any(), any(), any(), any(), any(), any());

        // And proceed with the testing
        System.out.println("Service now checking the get summary interface with " + data);
    }

}
