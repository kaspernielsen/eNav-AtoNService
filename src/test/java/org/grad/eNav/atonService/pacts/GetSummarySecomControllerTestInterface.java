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

import org.grad.eNav.atonService.services.DatasetService;
import org.grad.eNav.atonService.services.UnLoCodeService;

/**
 * The interface for testing the SECOM GetSummary controller using the Pacts
 * consumer driver contracts.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface GetSummarySecomControllerTestInterface {

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

}
