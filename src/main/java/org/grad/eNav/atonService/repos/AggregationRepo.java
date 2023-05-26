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

package org.grad.eNav.atonService.repos;

import org.grad.eNav.atonService.models.domain.s125.Aggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.Set;

/**
 * Spring Data JPA repository for the Aggregation entities.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface AggregationRepo extends JpaRepository<Aggregation, BigInteger>  {

    /**
     * Retrieves all the aggregations that include the number of the AtoN
     * provided in the input parameter.
     *
     * @param atonNumber    The AtoN number to find the corresponding aggregations
     * @return the aggregations that include the specified AtoN number
     */
    @Query("SELECT a FROM Aggregation a JOIN a.peers p WHERE p.atonNumber = :atonNumber")
    Set<Aggregation> findByIncludedAtonNumber(String atonNumber);

}