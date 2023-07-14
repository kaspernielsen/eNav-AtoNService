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


import _int.iala_aism.s125.gml._0_0.Dataset;
import _int.iho.s100.gml.base._5_0.MDTopicCategoryCode;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.grad.eNav.atonService.aspects.LogDataset;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.domain.s125.S125DatasetIdentification;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.repos.DatasetRepo;
import org.grad.eNav.atonService.utils.S125DatasetBuilder;
import org.grad.eNav.s125.utils.S125Utils;
import org.grad.secom.core.models.enums.SECOM_DataProductType;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.search.sort.dsl.LuceneSearchSortFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.function.Predicate.not;

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
     * The Model Mapper.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;


    /**
     * The Aids to Navigation Service.
     */
    @Autowired
    AidsToNavigationService aidsToNavigationService;

    /**
     * The S-125 Dataset Channel to publish the published data to.
     */
    @Autowired
    @Qualifier("s125PublicationChannel")
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Dataset Channel to publish the deleted data to.
     */
    @Autowired
    @Qualifier("s125DeletionChannel")
    PublishSubscribeChannel s125DeletionChannel;

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
     * @param uuid the UUID of the dataset
     * @return the dataset
     */
    @Transactional(readOnly = true)
    public S125Dataset findOne(UUID uuid) {
        return this.datasetRepo.findById(uuid)
                .orElseThrow(() -> new DataNotFoundException(String.format("The requested dataset with UUID %s was not found", uuid)));
    }

    /**
     * Get all the datasets in a pageable search.
     *
     * @param uuid      The dataset UUID
     * @param geometry  The dataset geometry
     * @param fromTime  the dataset validity starting time
     * @param toTime    the dataset validity ending time
     * @param pageable  The pageable result ouput
     * @return The matching datasets in a paged response
     */
    @Transactional(readOnly = true)
    public Page<S125Dataset> findAll(UUID uuid,
                                     Geometry geometry,
                                     LocalDateTime fromTime,
                                     LocalDateTime toTime,
                                     Pageable pageable) {
        log.debug("Request to get S-125 Datasets in a pageable search");
        // Create the search query - always sort by name
        SearchQuery<S125Dataset> searchQuery = this.getDatasetSearchQuery(
                uuid,
                geometry,
                fromTime,
                toTime,
                new Sort(new SortedSetSortField("uuid", false))
        );

        // Map the results to a paged response
        return Optional.of(searchQuery)
                .map(query -> pageable.isPaged() ? query.fetch(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize()) : query.fetchAll())
                .map(searchResult -> new PageImpl<>(searchResult.hits(), pageable, searchResult.total().hitCount()))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
    }

    /**
     * Handles a datatables pagination request and returns the dataset results
     * list in an appropriate format to be viewed by a datatables jQuery table.
     *
     * @param dtPagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    @Transactional(readOnly = true)
    public Page<S125Dataset> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        log.debug("Request to get S-125 Datasets in a Datatables pageable search");
        // Create the search query
        SearchQuery<S125Dataset> searchQuery = this.getDatasetSearchQueryByText(
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
     * A simple saving operation that persists the datasets in the database using
     * the correct repository based on the instance type.
     *
     * @param dataset the Dataset entity to be saved
     * @return the saved Dataset entity
     */
    @LogDataset
    @Transactional
    public S125Dataset save(@NotNull S125Dataset dataset) {
        log.debug("Request to save Dataset : {}", dataset);

        // Instantiate the dataset UUID if it does not exist
        if(Objects.isNull(dataset.getUuid())) {
            dataset.setUuid(UUID.randomUUID());
        }

        // Set the dataset ISO 19115-1 topic category if not defined
        if(Optional.of(dataset)
                .map(S125Dataset::getDatasetIdentificationInformation)
                .map(S125DatasetIdentification::getDatasetTopicCategories)
                .filter(not(List::isEmpty))
                .isEmpty()) {
            dataset.getDatasetIdentificationInformation().setDatasetTopicCategories(Collections.singletonList(MDTopicCategoryCode.OCEANS));
        }

        // Generate the new dataset content before the saving operation
        dataset.setDatasetContent(this.generateDatasetContent(dataset));

        // Now save the dataset - Merge to pick up all the latest changes
        final S125Dataset result = this.datasetRepo.save(dataset);
        final S125Dataset merged = this.entityManager.merge(result);

        // Publish the updated dataset to the publication channel
        this.s125PublicationChannel.send(MessageBuilder.withPayload(merged)
                .setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125)
                .setHeader("deletion", false)
                .build());

        // And return the object for AOP
        return merged;
    }

    /**
     * Delete the dataset specified by its UUID.
     *
     * @param uuid the UUID of the dataset
     */
    @LogDataset(operation = "DELETE")
    @Transactional
    public S125Dataset delete(UUID uuid) {
        log.debug("Request to delete Dataset with UUID : {}", uuid);

        // Make sure the dataset exists
        final S125Dataset result = this.datasetRepo.findById(uuid)
                .orElseThrow(() -> new DataNotFoundException(String.format("The requested dataset with UUID %s was not found", uuid)));

        // Now delete the dataset
        this.datasetRepo.delete(result);

        // Publish the updated dataset to the deleted channel
        this.s125DeletionChannel.send(MessageBuilder.withPayload(result)
                .setHeader(MessageHeaders.CONTENT_TYPE, SECOM_DataProductType.S125)
                .setHeader("deletion", true)
                .build());

        // And return the object for AOP
        return result;
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
    protected SearchQuery<S125Dataset> getDatasetSearchQueryByText(String searchText, Sort sort) {
        SearchSession searchSession = Search.session( this.entityManager );
        SearchScope<S125Dataset> scope = searchSession.scope( S125Dataset.class );
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
     * @param uuid the dataset UUID to be searched
     * @param geometry the geometry that the results should intersect with
     * @param fromTime the date-time the results should match from
     * @param toTime the date-time the results should match to
     * @param sort the sorting selection for the search query
     * @return the full text query
     */
    protected SearchQuery<S125Dataset> getDatasetSearchQuery(UUID uuid,
                                                             Geometry geometry,
                                                             LocalDateTime fromTime,
                                                             LocalDateTime toTime,
                                                             Sort sort) {
        // Then build and return the hibernate-search query
        SearchSession searchSession = Search.session( this.entityManager );
        SearchScope<S125Dataset> scope = searchSession.scope( S125Dataset.class );
        return searchSession.search( scope )
                .where( f -> f.bool(b -> {
                            b.must(f.matchAll());
                            Optional.ofNullable(uuid).ifPresent(v -> b.must(f.match()
                                    .field("uuid")
                                    .matching(v)));
//                            Optional.ofNullable(fromTime).ifPresent(v -> b.must(f.range()
//                                    .field("createdAt")
//                                    .atLeast(fromTime)));
//                            Optional.ofNullable(toTime).ifPresent(v -> b.must(f.range()
//                                    .field("createdAt")
//                                    .atMost(toTime)));
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

    /**
     * Provided a valid dataset this function will build the respective
     * dataset content and populate it with all entries that match its
     * geographical boundaries. The resulting object will then be marshalled
     * into an XML string and returned.
     *
     * @param s125DataSet the UUID of the dataset
     * @return the generated dataset content object
     */
    public DatasetContent generateDatasetContent(@NotNull S125Dataset s125DataSet) {
        log.debug("Request to retrieve the content for Dataset with UUID : {}", s125DataSet.getUuid());

        // Get all the matching Aids to Navigation
        final Page<AidsToNavigation> atonPage = this.aidsToNavigationService.findAll(
                null,
                s125DataSet.getGeometry(),
                null,
                null,
                Pageable.unpaged()
        );

        // If everything is OK up to now start building the dataset content
        final DatasetContent datasetContent = new DatasetContent();
        datasetContent.setContent("");
        datasetContent.setContentLength(BigInteger.ZERO);

        // Now try to marshal the dataset into an XML string
        try {
            final S125DatasetBuilder s125DatasetBuilder = new S125DatasetBuilder(this.modelMapper);
            final Dataset dataset = s125DatasetBuilder.packageToDataset(s125DataSet, atonPage.getContent());
            datasetContent.setContent(S125Utils.marshalS125(dataset, Boolean.FALSE));
            datasetContent.setContentLength(BigInteger.valueOf(datasetContent.getContent().length()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        // And return the dataset content
        return datasetContent;
    }
}
