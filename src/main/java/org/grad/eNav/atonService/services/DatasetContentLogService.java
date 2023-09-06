/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.services;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Sort;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.DatasetContentLog;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.models.enums.DatasetOperation;
import org.grad.eNav.atonService.models.enums.DatasetType;
import org.grad.eNav.atonService.repos.DatasetContentLogRepo;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * The S-125 Dataset Content Log Service.
 * <p/>
 * Service Implementation for managing the Dataset Content Log objects.
 * <p/>
 * Note that there is no deletion functionality in this service, since we want
 * to be able to store all the entries for auditing purposes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class DatasetContentLogService {

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;

    /**
     * The Dataset Service.
     */
    @Autowired
    DatasetService datasetService;

    /**
     * The Dataset Content Repo.
     */
    @Autowired
    DatasetContentLogRepo datasetContentLogRepo;

    // Service Variables
    private final String[] searchFields = new String[] {
            "uuid",
            "datasetType",
            "operation"
    };
    private final String[] searchFieldsWithSort = new String[] {
    };

    /**
     * Find one dataset content log by ID.
     *
     * @param id the UUID of the dataset
     * @return the dataset
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findOne(@NotNull BigInteger id) {
        return this.datasetContentLogRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(String.format("The requested dataset content log with ID %d was not found", id)));
    }

    /**
     * Find the latest dataset content log by UUID.
     *
     * @param uuid the UUID of the dataset
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findOriginal(@NotNull UUID uuid) {
        return this.datasetContentLogRepo.findOriginalForUuid(uuid)
                .orElse(null);
    }

    /**
     * Returns the sorted list of dataset content log entries for a specific
     * UUID that contain all the updates in the dataset but not the original
     * content, i.e. the deltas.
     *
     * @param uuid the UUID of the dataset
     * @return the list of the dataset content log entries that contain the deltas
     */
    public List<DatasetContentLog> findDeltas(@NotNull UUID uuid) {
        return this.datasetContentLogRepo.findByUuid(uuid)
                .stream()
                .filter(log -> log.getSequenceNo().compareTo(BigInteger.ZERO) > 0)
                .sorted(Comparator.comparing(DatasetContentLog::getSequenceNo))
                .collect(Collectors.toList());
    }

    /**
     * Find the latest dataset content by UUID.
     *
     * @param uuid the UUID of the dataset
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findLatest(@NotNull UUID uuid) {
        return this.findLatest(uuid, LocalDateTime.now());
    }

    /**
     * Find the latest by UUID before the provided reference local date-time.
     *
     * @param uuid              the UUID of the dataset
     * @param localDateTime     the reference local date-time
     * @return the dataset content
     */
    @Transactional(readOnly = true)
    public DatasetContentLog findLatest(@NotNull UUID uuid, LocalDateTime localDateTime) {
        return this.datasetContentLogRepo.findLatestForUuid(
                        uuid,
                        Optional.ofNullable(localDateTime).orElseGet(LocalDateTime::now),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseGet(() ->
                        // Handling cases where the content is not yet generated
                        // We need to first generate and store this manually
                        this.save(this.generateDatasetContentLog(this.datasetService.findOne(uuid), DatasetOperation.CREATED))
                );
    }

    /**
     * Get all the dataset content logs in a pageable search.
     *
     * @param pageable  The pageable result output
     * @return The matching dataset content logs in a paged response
     */
    @Transactional(readOnly = true)
    public Page<DatasetContentLog> findAll(Pageable pageable) {
        log.debug("Request to get Datasets Content Logs in a pageable search");

        // Return the query result
        return this.datasetContentLogRepo.findAll(pageable);
    }

    /**
     * Handles a datatables pagination request and returns the dataset content
     * log results list in an appropriate format to be viewed by a datatables
     * jQuery table.
     *
     * @param dtPagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    @Transactional(readOnly = true)
    public Page<DatasetContentLog> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        log.debug("Request to get Dataset Content Logs in a Datatables pageable search");
        // Create the search query
        final SearchQuery<DatasetContentLog> searchQuery = this.getDatasetContentLogSearchQueryByText(
                dtPagingRequest.getSearch().getValue(),
                dtPagingRequest.getLucenceSort(Arrays.asList(this.searchFieldsWithSort))
        );

        // Map the results to a paged response
        return Optional.of(searchQuery)
                .map(query -> query.fetch(dtPagingRequest.getStart(), dtPagingRequest.getLength()))
                .map(searchResult -> new PageImpl<>(searchResult.hits(), dtPagingRequest.toPageRequest(), searchResult.total().hitCount()))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), dtPagingRequest.toPageRequest(), 0));
    }

    /**
     * A simple saving operation that persists the models in the database using
     * the correct repository based on the instance type.
     *
     * @param datasetContentLog the Dataset entity to be saved
     * @return the saved Dataset entity
     */
    @Transactional
    public DatasetContentLog save(@NotNull DatasetContentLog datasetContentLog) {
        log.debug("Request to save Dataset Content Log: {}", datasetContentLog);

        // Save and return the dataset
        return this.datasetContentLogRepo.save(datasetContentLog);
    }

    /**
     * Provided a valid dataset this function will build the respective
     * dataset content and populate it with all entries that match its
     * geographical boundaries. The resulting object will then be marshalled
     * into an XML string and returned.
     *
     * @param s125Dataset the UUID of the dataset
     * @return the dataset content log entry
     */
    public DatasetContentLog generateDatasetContentLog(@NotNull S125Dataset s125Dataset, @NotNull DatasetOperation operation) {
        log.debug("Request to retrieve the content for Dataset with UUID : {}", s125Dataset.getUuid());

        // If everything is OK up to now start building the dataset content
        final DatasetContentLog datasetContentLog = new DatasetContentLog();
        datasetContentLog.setDatasetType(DatasetType.S125);
        datasetContentLog.setUuid(s125Dataset.getUuid());
        datasetContentLog.setGeometry(s125Dataset.getGeometry());
        datasetContentLog.setOperation(Optional.of(operation)
                .filter(not(DatasetOperation.AUTO::equals))
                .orElseGet(() -> s125Dataset.isNew() ? // <= Automatically select the operation
                        DatasetOperation.CREATED : DatasetOperation.UPDATED)
                );
        datasetContentLog.setSequenceNo(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getSequenceNo)
                .orElse(BigInteger.ZERO));
        datasetContentLog.setGeneratedAt(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getGeneratedAt)
                .orElse(LocalDateTime.now()));

        // Copy the content
        datasetContentLog.setContent(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getContent)
                .orElse(null));
        datasetContentLog.setContentLength(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getContentLength)
                .orElse(null));

        // Copy the delta
        datasetContentLog.setDelta(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getDelta)
                .orElse(null));
        datasetContentLog.setDeltaLength(Optional.of(s125Dataset)
                .map(S125Dataset::getDatasetContent)
                .map(DatasetContent::getDeltaLength)
                .orElse(null));

        // And return the dataset content
        return datasetContentLog;
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * search test. This query will be based solely on the dataset content log
     * table and will include the following fields:
     * <ul>
     *  <li>uuid</li>
     *  <li>datasetType</li>
     *  <li>operation</li>
     * </ul>
     *
     * @param searchText the UUID as text to be searched
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<DatasetContentLog> getDatasetContentLogSearchQueryByText(String searchText, Sort sort) {
        SearchSession searchSession = Search.session( this.entityManager );
        SearchScope<DatasetContentLog> scope = searchSession.scope( DatasetContentLog.class );
        return searchSession.search( scope )
                .extension(LuceneExtension.get())
                .where(f -> f.wildcard()
                        .fields(this.searchFields)
                        .matching(Optional.ofNullable(searchText).map(st -> "*" + st).orElse("") + "*")
                )
                .sort(f -> f.fromLuceneSort(sort))
                .toQuery();
    }

}
