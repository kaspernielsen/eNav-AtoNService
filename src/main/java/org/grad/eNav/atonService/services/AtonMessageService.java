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
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.exceptions.InvalidRequestException;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.repos.AtonMessageRepo;
import org.hibernate.search.backend.lucene.LuceneExtension;
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
    public Page<AtonMessage> findAll(Pageable pageable) {
        log.debug("Request to get all AtoN messages in a pageable search");
        return this.sNodeRepo.findAll(pageable);
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
    public DtPage<AtonMessage> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        // Create the search query
        SearchQuery searchQuery = this.searchSNodesQuery(
                dtPagingRequest.getSearch().getValue(),
                dtPagingRequest.getLucenceSort(Arrays.asList(searchFieldsWithSort))
        );

        // For some reason we need this casting otherwise JDK8 complains
        return Optional.of(searchQuery)
                .map(query -> query.fetch(dtPagingRequest.getStart(), dtPagingRequest.getLength()))
                .map(searchResult -> new PageImpl<AtonMessage>(searchResult.hits(), dtPagingRequest.toPageRequest(), searchResult.total().hitCount()))
                .map(Page.class::cast)
                .map(page -> new DtPage<>((Page<AtonMessage>)page, dtPagingRequest))
                .orElseGet(DtPage::new);
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
    protected SearchQuery<AtonMessage> searchSNodesQuery(String searchText, Sort sort) {
        SearchSession searchSession = Search.session( entityManager );
        SearchScope<AtonMessage> scope = searchSession.scope( AtonMessage.class );
        return searchSession.search( scope )
                .extension(LuceneExtension.get())
                .where( scope.predicate().wildcard()
                        .fields( this.searchFields )
                        .matching( Optional.ofNullable(searchText).map(st -> "*"+st).orElse("") + "*" )
                        .toPredicate() )
                .sort(f -> f.fromLuceneSort(sort))
                .toQuery();
    }

    /**
     * Creates a Lucene query based on the query string provided. The query
     * string should follow the Lucene query syntax.
     *
     * @param queryString   The query string that follows the Lucene query syntax
     * @return The Lucene query constructed
     */
    protected Query createLuceneQuery(String queryString) {
        // First parse the input string to make sure it's right
        MultiFieldQueryParser parser = new MultiFieldQueryParser(this.searchFields, new StandardAnalyzer());
        parser.setDefaultOperator( QueryParser.Operator.AND );
        return Optional.ofNullable(queryString)
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    try {
                        return parser.parse(q);
                    } catch (org.apache.lucene.queryparser.classic.ParseException ex) {
                        this.log.error(ex.getMessage());
                        throw new InvalidRequestException(ex.getMessage());
                    }
                })
                .orElse(null);
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
        int maxLevels = 12; //results in sub-meter precision for geohash
        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);
        RecursivePrefixTreeStrategy strategy = new RecursivePrefixTreeStrategy(grid,"geometry");

        // Create the Lucene GeoSpatial Query
        return Optional.ofNullable(geometry)
                .map(g -> new SpatialArgs(SpatialOperation.Intersects, new JtsGeometry(g, ctx, false , true)))
                .map(strategy::makeQuery)
                .orElse(null);
    }

}
