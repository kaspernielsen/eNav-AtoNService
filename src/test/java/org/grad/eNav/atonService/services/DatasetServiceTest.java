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
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.BeaconCardinal;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.models.dtos.datatables.*;
import org.grad.eNav.atonService.repos.DatasetRepo;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    DatasetService datasetService;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Dataset Content Service mock.
     */
    @Mock
    DatasetContentService datasetContentService;

    /**
     * The Dataset Repo mock.
     */
    @Mock
    DatasetRepo datasetRepo;

    /**
     * The S-125 Dataset Channel to publish the published data to.
     */
    @Mock
    PublishSubscribeChannel s125PublicationChannel;

    /**
     * The S-125 Dataset Channel to publish the deleted data to.
     */
    @Mock
    PublishSubscribeChannel s125RemovalChannel;


    // Test Variables
    private List<AidsToNavigation> aidsToNavigationList;
    private List<S125Dataset> datasetList;
    private Pageable pageable;
    private S125Dataset newDataset;
    private S125Dataset existingDataset;
    private DatasetContent newDatasetContent;
    private DatasetContent existingDatasetContent;
    private GeometryFactory factory;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a temp geometry factory to get a test geometries
        this.factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the AtoN messages list
        this.aidsToNavigationList = new ArrayList<>();
        for(long i=0; i<10; i++) {
            AidsToNavigation aidsToNavigation = new BeaconCardinal();
            aidsToNavigation.setId(BigInteger.valueOf(i));
            aidsToNavigation.setAtonNumber("AtonNumber" + i);
            aidsToNavigation.setIdCode("ID"+i);
            aidsToNavigation.setTextualDescription("Description of AtoN No" + i);
            aidsToNavigation.setTextualDescriptionInNationalLanguage("National Language Description of AtoN No" + i);
            aidsToNavigation.setGeometry(factory.createPoint(new Coordinate(i%180, i%90)));
            this.aidsToNavigationList.add(aidsToNavigation);
        }

        // Initialise the dataset nodes list
        this.datasetList = new ArrayList<>();
        for(long i=0; i<10; i++) {
            S125Dataset dataset = new S125Dataset(String.format("Dataset{}", i));
            dataset.setGeometry(this.factory.createPoint(new Coordinate(i, i)));
            this.datasetList.add(dataset);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a Dataset without a UUID
        this.newDataset = new S125Dataset("NewDataset");
        this.newDataset.setGeometry(this.factory.createPoint(new Coordinate(51.98, 1.28)));
        this.newDatasetContent = new DatasetContent();
        this.newDatasetContent.setId(BigInteger.ONE);
        this.newDatasetContent.setContent("New dataset content");
        this.newDatasetContent.setContentLength(BigInteger.valueOf(this.newDatasetContent.getContent().length()));
        this.newDatasetContent.setGeneratedAt(LocalDateTime.now());
        this.newDataset.setDatasetContent(this.newDatasetContent);

        // Create a Dataset with a UUID
        this.existingDataset = new S125Dataset("ExistingDataset");
        this.existingDataset.setUuid(UUID.randomUUID());
        this.existingDataset.setGeometry(this.factory.createPoint(new Coordinate(52.98, 2.28)));
        this.existingDatasetContent = new DatasetContent();
        this.existingDatasetContent.setId(BigInteger.TWO);
        this.existingDatasetContent.setContent("Existing dataset content");
        this.existingDatasetContent.setContentLength(BigInteger.valueOf(this.existingDatasetContent.getContent().length()));
        this.existingDatasetContent.setGeneratedAt(LocalDateTime.now());
        this.existingDataset.setDatasetContent(this.existingDatasetContent);
    }

    /**
     * Test that we can search for a single dataset based on it's ID.
     */
    @Test
    void testFindOne() {
        doReturn(Optional.of(this.existingDataset)).when(this.datasetRepo).findById(this.existingDataset.getUuid());

        // Perform the service call
        S125Dataset result = this.datasetService.findOne(this.existingDataset.getUuid());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingDataset.getUuid(), result.getUuid());
        assertEquals(this.existingDataset.getGeometry(), result.getGeometry());
        assertNotNull(result.getDatasetIdentificationInformation());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getDatasetTitle(), result.getDatasetIdentificationInformation().getDatasetTitle());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getEncodingSpecification(), result.getDatasetIdentificationInformation().getEncodingSpecification());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getEncodingSpecificationEdition(), result.getDatasetIdentificationInformation().getEncodingSpecificationEdition());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getProductIdentifier(),result.getDatasetIdentificationInformation().getProductIdentifier());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getProductEdition(), result.getDatasetIdentificationInformation().getProductEdition());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getApplicationProfile(), result.getDatasetIdentificationInformation().getApplicationProfile());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getDatasetLanguage(), result.getDatasetIdentificationInformation().getDatasetLanguage());
        assertEquals(this.existingDataset.getDatasetIdentificationInformation().getDatasetAbstract(), result.getDatasetIdentificationInformation().getDatasetAbstract());
    }

    /**
     * Test that we if we try to find a dataset with an ID that does not exist,
     * a DataNotFoundException will be thrown.
     */
    @Test
    void testFindOneNotFound() {
        doReturn(Optional.empty()).when(this.datasetRepo).findById(this.existingDataset.getUuid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.datasetService.delete(this.existingDataset.getUuid())
        );
    }

    /**
     * Test that we can search for all the datasets currently present in the
     * database and matching the provided criteria, through a paged call.
     */
    @Test
    void testFindAllPaged() {
        // Mock the full text query
        SearchQuery<AidsToNavigation> mockedQuery = mock(SearchQuery.class);
        SearchResult<AidsToNavigation> searchResult = mock(SearchResult.class);
        SearchResultTotal searchResultTotal = mock(SearchResultTotal.class);
        doReturn(searchResult).when(mockedQuery).fetch(any(), any());
        doReturn(this.datasetList.subList(0, 5)).when(searchResult).hits();
        doReturn(searchResultTotal).when(searchResult).total();
        doReturn(10L).when(searchResultTotal).hitCount();
        doReturn(mockedQuery).when(this.datasetService).getDatasetSearchQuery(any(), any(), any(), any(), any(), any());

        // Perform the service call
        Page<S125Dataset> result = this.datasetService.findAll(UUID.randomUUID(), null, null, null, Boolean.FALSE, pageable);

        // Test the result
        assertNotNull(result);
        assertEquals(5, result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertNotNull(result.getContent().get(i));
            assertEquals(this.datasetList.get(i).getUuid(), result.getContent().get(i).getUuid());
            assertEquals(this.datasetList.get(i).getGeometry(), result.getContent().get(i).getGeometry());
            assertNotNull(result.getContent().get(i).getDatasetIdentificationInformation());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetTitle(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetTitle());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getEncodingSpecification(), result.getContent().get(i).getDatasetIdentificationInformation().getEncodingSpecification());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getEncodingSpecificationEdition(), result.getContent().get(i).getDatasetIdentificationInformation().getEncodingSpecificationEdition());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getProductIdentifier(), result.getContent().get(i).getDatasetIdentificationInformation().getProductIdentifier());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getProductEdition(), result.getContent().get(i).getDatasetIdentificationInformation().getProductEdition());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getApplicationProfile(), result.getContent().get(i).getDatasetIdentificationInformation().getApplicationProfile());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetLanguage(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetLanguage());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetAbstract(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetAbstract());
        }
    }

    /**
     * Test that we can retrieve the paged list of datatable entries for a
     * Datatables pagination request (which by the way also includes search and
     * sorting definitions).
     */
    @Test
    void testHandleDatatablesPagingRequest() {
        // First create the pagination request
        DtPagingRequest dtPagingRequest = new DtPagingRequest();
        dtPagingRequest.setStart(0);
        dtPagingRequest.setLength(5);

        // Set the pagination request columns
        dtPagingRequest.setColumns(new ArrayList());
        Stream.of("uid", "type", "message")
                .map(DtColumn::new)
                .forEach(dtPagingRequest.getColumns()::add);

        // Set the pagination request ordering
        DtOrder dtOrder = new DtOrder();
        dtOrder.setColumn(0);
        dtOrder.setDir(DtDirection.asc);
        dtPagingRequest.setOrder(Collections.singletonList(dtOrder));

        // Set the pagination search
        DtSearch dtSearch = new DtSearch();
        dtSearch.setValue("search-term");
        dtPagingRequest.setSearch(dtSearch);

        // Mock the full text query
        SearchQuery<?> mockedQuery = mock(SearchQuery.class);
        SearchResult<?> mockedResult = mock(SearchResult.class);
        SearchResultTotal mockedResultTotal = mock(SearchResultTotal.class);
        doReturn(5L).when(mockedResultTotal).hitCount();
        doReturn(mockedResultTotal).when(mockedResult).total();
        doReturn(this.datasetList.subList(0, 5)).when(mockedResult).hits();
        doReturn(mockedResult).when(mockedQuery).fetch(any(), any());
        doReturn(mockedQuery).when(this.datasetService).getDatasetSearchQueryByText(any(), any(), any());

        // Perform the service call
        Page<S125Dataset> result = this.datasetService.handleDatatablesPagingRequest(dtPagingRequest);

        // Validate the result
        assertNotNull(result);
        assertEquals(5, result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertNotNull(result.getContent().get(i));
            assertEquals(this.datasetList.get(i).getUuid(), result.getContent().get(i).getUuid());
            assertEquals(this.datasetList.get(i).getGeometry(), result.getContent().get(i).getGeometry());
            assertNotNull(result.getContent().get(i).getDatasetIdentificationInformation());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetTitle(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetTitle());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getEncodingSpecification(), result.getContent().get(i).getDatasetIdentificationInformation().getEncodingSpecification());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getEncodingSpecificationEdition(), result.getContent().get(i).getDatasetIdentificationInformation().getEncodingSpecificationEdition());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getProductIdentifier(), result.getContent().get(i).getDatasetIdentificationInformation().getProductIdentifier());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getProductEdition(), result.getContent().get(i).getDatasetIdentificationInformation().getProductEdition());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getApplicationProfile(), result.getContent().get(i).getDatasetIdentificationInformation().getApplicationProfile());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetLanguage(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetLanguage());
            assertEquals(this.datasetList.get(i).getDatasetIdentificationInformation().getDatasetAbstract(), result.getContent().get(i).getDatasetIdentificationInformation().getDatasetAbstract());
        }
    }

    /**
     * Test that we can save correctly a new or existing dataset entry if all
     * the validation checks are successful.
     */
    @Test
    void testSave() {
        doReturn(this.newDataset).when(this.datasetRepo).save(any());

        // Create a data content generation response to wait for
        CompletableFuture<S125Dataset> contentGenerationTask = CompletableFuture.completedFuture(this.newDataset);
        doReturn(contentGenerationTask).when(this.datasetContentService).generateDatasetContent(any());

        // Perform the service call
        S125Dataset result = this.datasetService.save(new S125Dataset());

        // Test the result
        assertNotNull(result);
        assertEquals(this.newDataset.getUuid(), result.getUuid());
        assertEquals(this.newDataset.getGeometry(), result.getGeometry());
        assertNotNull(result.getDatasetIdentificationInformation());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getDatasetTitle(), result.getDatasetIdentificationInformation().getDatasetTitle());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getEncodingSpecification(), result.getDatasetIdentificationInformation().getEncodingSpecification());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getEncodingSpecificationEdition(), result.getDatasetIdentificationInformation().getEncodingSpecificationEdition());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getProductIdentifier(),result.getDatasetIdentificationInformation().getProductIdentifier());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getProductEdition(), result.getDatasetIdentificationInformation().getProductEdition());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getApplicationProfile(), result.getDatasetIdentificationInformation().getApplicationProfile());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getDatasetLanguage(), result.getDatasetIdentificationInformation().getDatasetLanguage());
        assertEquals(this.newDataset.getDatasetIdentificationInformation().getDatasetAbstract(), result.getDatasetIdentificationInformation().getDatasetAbstract());
        assertNotNull(result.getDatasetContent());
        assertEquals(this.newDataset.getDatasetContent().getContent(), result.getDatasetContent().getContent());
        assertEquals(this.newDataset.getDatasetContent().getContentLength(), result.getDatasetContent().getContentLength());

        // Wait until the end and verify that the message was published
        assertTrue(contentGenerationTask.isDone());
        verify(this.s125PublicationChannel, timeout(100).times(1)).send(any(Message.class));
    }

    /**
     * Test that if the dataset content generation fails, then a log entry will
     * be generated and no dataset will be published in the publish-subscribe
     * channels.
     */
    @Test
    void testSaveNotGenerated() {
        doReturn(this.newDataset).when(this.datasetRepo).save(any());

        // Create a data content generation response to wait for
        CompletableFuture<S125Dataset> contentGenerationTask = CompletableFuture.failedFuture(new RuntimeException("something went wrong"));
        doReturn(contentGenerationTask).when(this.datasetContentService).generateDatasetContent(any());

        // Perform the service call
        this.datasetService.save(new S125Dataset());

        // Also verify that the message was NOT published
        assertThrows(RuntimeException.class, contentGenerationTask::join);
        verify(this.s125PublicationChannel, timeout(100).times(0)).send(any(Message.class));
    }

    /**
     * Test that we can successfully delete an existing dataset entry.
     */
    @Test
    void testDelete() throws DataNotFoundException {
        doReturn(Optional.of(this.existingDataset)).when(this.datasetRepo).findById(this.existingDataset.getUuid());
        doNothing().when(this.datasetRepo).delete(this.existingDataset);

        // Perform the service call
        this.datasetService.delete(this.existingDataset.getUuid());

        // Verify that our message was deleted and sent
        verify(this.datasetRepo, times(1)).delete(any());
        verify(this.s125RemovalChannel, times(1)).send(any(Message.class));
    }

    /**
     * Test that if we try to delete a non-existing dataset entry then a
     * DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Optional.empty()).when(this.datasetRepo).findById(this.existingDataset.getUuid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.datasetService.delete(this.existingDataset.getUuid())
        );
    }

}