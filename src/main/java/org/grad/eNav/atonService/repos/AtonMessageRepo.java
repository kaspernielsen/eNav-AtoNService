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

package org.grad.eNav.atonService.repos;

import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

/**
 * Spring Data JPA repository for the AtoN Message entity.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface AtonMessageRepo extends JpaRepository<AtonMessage, BigInteger> {

    /**
     * Find one using the AtoN message UID.
     *
     * @return The AtoN message matching the UID
     */
    AtonMessage findByUid(String uid);

}
