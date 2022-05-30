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

package org.grad.eNav.atonService.services;


import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.repos.DatasetRepo;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.search.sort.dsl.LuceneSearchSortFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * The S-125 Dataset Service.
 *
 * Service Implementation for managing the S-125 Dataset objects.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
public class DatasetService {

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;


    /**
     * The Dataset Repo.
     */
    @Autowired
    DatasetRepo datasetRepo;

    // Service Variables
    private final String[] searchFields = new String[] {
            "datasetIdentificationInformation.datasetTitle",
            "datasetIdentificationInformation.encodingSpecification",
            "datasetIdentificationInformation.encodingSpecificationEdition",
            "datasetIdentificationInformation.productIdentifier",
            "datasetIdentificationInformation.productEdition",
            "datasetIdentificationInformation.applicationProfile",
            "datasetIdentificationInformation.datasetFileIdentifier",
            "datasetIdentificationInformation.datasetAbstract",
    };
    private final String[] searchFieldsWithSort = new String[] {

    };


    /**
     * Find one dataset by UUID.
     *
     * @param uuid the UUID
     * @return the dataset
     */
    @Transactional(readOnly = true)
    public S125DataSet findOne(UUID uuid) {
        return this.datasetRepo.findById(uuid)
                .orElseThrow(() -> new DataNotFoundException(String.format("The requested dataset with UUID %s was not found", uuid)));
    }

    /**
     * Get all the Datasets in a pageable search.
     *
     * @param pageable the pagination information
     * @return the list of Datasets
     */
    @Transactional(readOnly = true)
    public Page<S125DataSet> findAll(String datasetTitle,
                                     Geometry geometry,
                                     LocalDateTime fromTime,
                                     LocalDateTime toTime,
                                     Pageable pageable) {
        log.debug("Request to get S-125 Datasets in a pageable search");
        // Create the search query - always sort by name
        SearchQuery<S125DataSet> searchQuery = this.geDatasetSearchQuery(
                datasetTitle,
                geometry,
                fromTime,
                toTime,
                new Sort(new SortedSetSortField("uuid", false))
        );

        // Map the results to a paged response
        return Optional.of(searchQuery)
                .map(query -> query.fetch(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize()))
                .map(searchResult -> new PageImpl<>(searchResult.hits(), pageable, searchResult.total().hitCount()))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    /**
     * Handles a datatables pagination request and returns the results list in
     * an appropriate format to be viewed by a datatables jQuery table.
     *
     * @param dtPagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    @Transactional(readOnly = true)
    public Page<S125DataSet> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        log.debug("Request to get S-125 Datasets in a Datatables pageable search");
        // Create the search query
        SearchQuery<S125DataSet> searchQuery = this.getDatasetSearchQueryByText(
                dtPagingRequest.getSearch().getValue(),
                dtPagingRequest.getLucenceSort(Arrays.asList(searchFieldsWithSort))
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
     * @param dataSet the Dataset entity to be saved
     * @return the saved Dataset entity
     */
    @Transactional
    public S125DataSet save(S125DataSet dataSet) {
        log.debug("Request to save Dataset : {}", dataSet);

        // Now save for each type
        return this.datasetRepo.save(dataSet);
    }

    /**
     * Delete the Aids to Navigation by UUID.
     *
     * @param uuid the UUID of the Aids to Navigation 21ddfeb8-a647-49cd-a6a0-2df3b92251b9
     */
    @Transactional
    public void delete(UUID uuid) {
        log.debug("Request to delete Dataset with UUID : {}", uuid);

        // Make sure the station node exists
        final S125DataSet dataSet = this.datasetRepo.findById(uuid)
                .orElseThrow(() -> new DataNotFoundException(String.format("No Dataset found for the provided UUID: %s", uuid)));

        // Now delete the station node
        this.datasetRepo.delete(dataSet);
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * search test. This query will be based solely on the station nodes table
     * and will include the following fields:
     * -
     *
     * @param searchText the text to be searched
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<S125DataSet> getDatasetSearchQueryByText(String searchText, Sort sort) {
        SearchSession searchSession = Search.session( entityManager );
        SearchScope<S125DataSet> scope = searchSession.scope( S125DataSet.class );
        return searchSession.search( scope )
                .extension(LuceneExtension.get())
                .where(f -> f.wildcard()
                        .fields( this.searchFields )
                        .matching( Optional.ofNullable(searchText).map(st -> "*"+st).orElse("") + "*" ))
                .sort(f -> f.fromLuceneSort(sort))
                .toQuery();
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * AtoN UID and geometry. This query will be based solely on the aton
     * messages table and will include the following fields:
     * -
     * For any more elaborate search, the getSearchMessageQueryByText function
     * can be used.
     *
     * @param datasetTitle the Dataset title to be searched
     * @param geometry the geometry that the results should intersect with
     * @param fromTime the date-time the results should match from
     * @param toTime the date-time the results should match to
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<S125DataSet> geDatasetSearchQuery(String datasetTitle,
                                                            Geometry geometry,
                                                            LocalDateTime fromTime,
                                                            LocalDateTime toTime,
                                                            Sort sort) {
        // Then build and return the hibernate-search query
        SearchSession searchSession = Search.session( entityManager );
        SearchScope<S125DataSet> scope = searchSession.scope( S125DataSet.class );
        return searchSession.search( scope )
                .where( f -> f.bool(b -> {
                            b.must(f.matchAll());
                            Optional.ofNullable(datasetTitle).ifPresent(v -> b.must(f.match()
                                    .field("datasetIdentificationInformation.datasetTitle")
                                    .matching(v)));
                            Optional.ofNullable(fromTime).ifPresent(v -> b.must(f.range()
                                    .field("dateStart")
                                    .atLeast(fromTime)));
                            Optional.ofNullable(toTime).ifPresent(v -> b.must(f.range()
                                    .field("dateEnd")
                                    .atMost(toTime)));
                            Optional.ofNullable(geometry).ifPresent(g-> b.must(f.extension(LuceneExtension.get())
                                    .fromLuceneQuery(createGeoSpatialQuery(g))));
                        })
                )
                .sort(f -> ((LuceneSearchSortFactory)f).fromLuceneSort(sort))
                .toQuery();
    }

    /**
     * Creates a Lucene geo-spatial query based on the provided geometry. The
     * query isa recursive one based on the maxLevels defined (in this case 12,
     * which result in a sub-meter precision).
     *
     * @param geometry      The geometry to generate the spatial query for
     * @return The Lucene geo-spatial query constructed
     */
    protected Query createGeoSpatialQuery(Geometry geometry) {
        // Initialise the spatial strategy
        JtsSpatialContext ctx = JtsSpatialContext.GEO;
        int maxLevels = 12; //results in sub-meter precision for geo-hash
        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);
        RecursivePrefixTreeStrategy strategy = new RecursivePrefixTreeStrategy(grid,"geometry");

        // Create the Lucene GeoSpatial Query
        return Optional.ofNullable(geometry)
                .map(g -> new SpatialArgs(SpatialOperation.Intersects, new JtsGeometry(g, ctx, false , true)))
                .map(strategy::makeQuery)
                .orElse(null);
    }
}
