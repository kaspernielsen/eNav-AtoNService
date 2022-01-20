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

package org.grad.eNav.atonService.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.repos.AtonMessageRepo;
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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The AtoN Message Service Class
 *
 * Service Implementation for managing AtoN messages.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class AtonMessageService {

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;

    /**
     * The AtoN Message Repo.
     */
    @Autowired
    AtonMessageRepo sNodeRepo;

    // Service Variables
    private final String[] searchFields = new String[] {
            "uid",
            "type",
            "message"
    };
    private final String[] searchFieldsWithSort = new String[] {
            "id",
            "message"
    };

    /**
     * Get all the AtoN messages.
     *
     * @return the list of AtoN messages
     */
    @Transactional(readOnly = true)
    public List<AtonMessage> findAll() {
        log.debug("Request to get all AtoN messages");
        return this.sNodeRepo.findAll();
    }

    /**
     * Get all the AtoN messages in a pageable search.
     *
     * @param pageable the pagination information
     * @return the list of AtoN messages
     */
    @Transactional(readOnly = true)
    public Page<AtonMessage> findAll(Optional<String> uid,
                                     Optional<Geometry> geometry,
                                     Pageable pageable) {
        log.debug("Request to get AtoN messages in a pageable search");
        // Create the search query - always sort by name
        SearchQuery searchQuery = this.getMessageSearchQuery(uid, geometry, new Sort(new SortedNumericSortField("id_sort", SortField.Type.LONG, true)));

        // Map the results to a paged response
        return Optional.of(searchQuery)
                .map(query -> query.fetch(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize()))
                .map(searchResult -> new PageImpl<AtonMessage>(searchResult.hits(), pageable, searchResult.total().hitCount()))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));

    }

    /**
     * Get one AtoN message by ID.
     *
     * @param id the ID of the AtoN message
     * @return the AtoN message
     */
    @Transactional(readOnly = true)
    public AtonMessage findOne(BigInteger id) {
        log.debug("Request to get AtoN message : {}", id);
        return this.sNodeRepo.findById(id)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided ID: %d", id))
                );
    }

    /**
     * Get one AtoN message by UID.
     *
     * @param uid the UID of the AtoN message
     * @return the AtoN message
     */
    @Transactional(readOnly = true)
    public AtonMessage findOneByUid(String uid) {
        log.debug("Request to get Node with UID : {}", uid);
        return Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided UID: %s", uid))
                );
    }

    /**
     * Save a AtoN message.
     *
     * @param AtonMessage the entity to save
     * @return the persisted entity
     */
    @Transactional
    public AtonMessage save(AtonMessage AtonMessage) {
        log.debug("Request to save AtoN message : {}", AtonMessage);
        return this.sNodeRepo.save(AtonMessage);
    }

    /**
     * Delete the AtoN message by ID.
     *
     * @param id the ID of the AtoN message
     */
    @Transactional
    public void delete(BigInteger id) {
        log.debug("Request to delete AtoN message with ID : {}", id);
        // Make sure the station node exists
        final AtonMessage atonMessage = this.sNodeRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException(String.format("No station node found for the provided ID: %d", id)));

        // Now delete the station node
        this.sNodeRepo.delete(atonMessage);
    }

    /**
     * Delete the node by UID.
     *
     * @param uid the UID the node
     */
    public void deleteByUid(String uid) throws DataNotFoundException {
        log.debug("Request to delete AtoN message with UID : {}", uid);
        BigInteger id = Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .map(AtonMessage::getId)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided UID: %s", uid))
                );
        this.delete(id);
    }

    /**
     * Handles a datatables pagination request and returns the results list in
     * an appropriate format to be viewed by a datatables jQuery table.
     *
     * @param dtPagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    @Transactional(readOnly = true)
    public Page<AtonMessage> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        // Create the search query
        SearchQuery searchQuery = this.getSearchMessageQueryByText(
                dtPagingRequest.getSearch().getValue(),
                dtPagingRequest.getLucenceSort(Arrays.asList(searchFieldsWithSort))
        );

        // Map the results to a paged response
        return Optional.of(searchQuery)
                .map(query -> query.fetch(dtPagingRequest.getStart(), dtPagingRequest.getLength()))
                .map(searchResult -> new PageImpl<AtonMessage>(searchResult.hits(), dtPagingRequest.toPageRequest(), searchResult.total().hitCount()))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), dtPagingRequest.toPageRequest(), 0));
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * search test. This query will be based solely on the station nodes table
     * and will include the following fields:
     * - UID
     * - Type
     * - Message
     *
     * @param searchText the text to be searched
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<AtonMessage> getSearchMessageQueryByText(String searchText, Sort sort) {
        SearchSession searchSession = Search.session( entityManager );
        SearchScope<AtonMessage> scope = searchSession.scope( AtonMessage.class );
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
     * - UID
     * - Geometry
     * For any more elaborate search, the getSearchMessageQueryByText funtion
     * can be used.
     *
     * @param uid the AtoN UID to be searched
     * @param geometry the geometry that the results should intersect with
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<AtonMessage> getMessageSearchQuery(Optional<String> uid, Optional<Geometry> geometry, Sort sort) {
        // Then build and return the hibernate-search query
        SearchSession searchSession = Search.session( entityManager );
        SearchScope<AtonMessage> scope = searchSession.scope( AtonMessage.class );
        return searchSession.search( scope )
                .where( f -> f.bool(b -> {
                            b.must(f.matchAll());
                            uid.ifPresent(v -> b.must(f.match()
                                    .field("uid")
                                    .matching(v)));
                            geometry.ifPresent(g-> b.must(geometry.map(this::createGeoSpatialQuery)
                                    .map(f.extension(LuceneExtension.get())::fromLuceneQuery)
                                    .orElse(null)));
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
