/*
 * Copyright (c) 2022 GLA Research and Development Directorate
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

import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the S-125 Dataset Content entities.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface DatasetContentRepo extends JpaRepository<DatasetContent, BigInteger> {

    /**
     * Retrieves the latest database content entries for a specific UUID. It
     * also accepts a pageable argument to provide only a subset of the
     * matching entries (e.g. just the last one).
     *
     * @param uuid              The UUID of the dataset
     * @param createdAt         The creation date of the content
     * @return The dataset content if it exists
     */
    @Query("select d from DatasetContent d where d.uuid = :uuid AND d.createdAt <= :createdAt ORDER BY d.createdAt DESC")
    List<DatasetContent> findLatestForUuid(UUID uuid, LocalDateTime createdAt, Pageable pageable);

}
