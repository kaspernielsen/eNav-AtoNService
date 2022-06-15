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

import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.BeaconCardinal;
import org.grad.eNav.atonService.models.dtos.datatables.*;
import org.grad.eNav.atonService.repos.AidsToNavigationRepo;
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

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AidsToNavigationServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    AidsToNavigationService aidsToNavigationService;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Aids to Navigation Repo mock.
     */
    @Mock
    AidsToNavigationRepo aidsToNavigationRepo;

    // Test Variables
    private List<AidsToNavigation> aidsToNavigationList;
    private Pageable pageable;
    private AidsToNavigation newAidsToNavigation;
    private AidsToNavigation existingAidsToNavigation;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

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

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new AtoN message
        this.newAidsToNavigation = new BeaconCardinal();
        this.newAidsToNavigation.setAtonNumber("AtonNumber001");
        this.newAidsToNavigation.setIdCode("ID001");
        this.newAidsToNavigation.setTextualDescription("Description of AtoN No 1");
        this.newAidsToNavigation.setTextualDescriptionInNationalLanguage("National Language Description of AtoN No 1" );
        this.newAidsToNavigation.setGeometry(factory.createPoint(new Coordinate(1, 1)));

        // Create an existing AtoN message with ID
        this.existingAidsToNavigation = new BeaconCardinal();
        this.existingAidsToNavigation.setId(BigInteger.valueOf(1));
        this.existingAidsToNavigation.setAtonNumber("AtonNumber010");
        this.existingAidsToNavigation.setIdCode("ID010");
        this.existingAidsToNavigation.setTextualDescription("Description of AtoN No 10");
        this.existingAidsToNavigation.setTextualDescriptionInNationalLanguage("National Language Description of AtoN No 10" );
        this.existingAidsToNavigation.setGeometry(factory.createPoint(new Coordinate(10, 10)));
    }

    /**
     * Test that we can search for all the Aids to Navigation currently present
     * in the database and matching the provided criteria, through a paged call.
     */
    @Test
    void testFindAllPaged() {
        // Mock the full text query
        SearchQuery<AidsToNavigation> mockedQuery = mock(SearchQuery.class);
        SearchResult<AidsToNavigation> searchResult = mock(SearchResult.class);
        SearchResultTotal searchResultTotal = mock(SearchResultTotal.class);
        doReturn(searchResult).when(mockedQuery).fetch(any(), any());
        doReturn(this.aidsToNavigationList.subList(0, 5)).when(searchResult).hits();
        doReturn(searchResultTotal).when(searchResult).total();
        doReturn(10L).when(searchResultTotal).hitCount();
        doReturn(mockedQuery).when(this.aidsToNavigationService).getAidsToNavigationSearchQuery(any(), any(), any(), any(), any());

        // Perform the service call
        Page<AidsToNavigation> result = this.aidsToNavigationService.findAll("uid", null, null, null, pageable);

        // Test the result
        assertNotNull(result);
        assertEquals(5, result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertEquals(this.aidsToNavigationList.get(i).getId(), result.getContent().get(i).getId());
            assertEquals(this.aidsToNavigationList.get(i).getAtonNumber(), result.getContent().get(i).getAtonNumber());
            assertEquals(this.aidsToNavigationList.get(i).getIdCode(), result.getContent().get(i).getIdCode());
            assertEquals(this.aidsToNavigationList.get(i).getTextualDescription(), result.getContent().get(i).getTextualDescription());
            assertEquals(this.aidsToNavigationList.get(i).getTextualDescriptionInNationalLanguage(), result.getContent().get(i).getTextualDescriptionInNationalLanguage());
        }
    }

    /**
     * Test that we can easily access the total number of results included in
     * the search for all the Aids to Navigation currently present
     * in the database and matching the provided criteria.
     */
    @Test
    void testFindAllTotalCount() {
        // Mock the full text query
        SearchQuery<AidsToNavigation> mockedQuery = mock(SearchQuery.class);
        doReturn(10L).when(mockedQuery).fetchTotalHitCount();
        doReturn(mockedQuery).when(this.aidsToNavigationService).getAidsToNavigationSearchQuery(any(), any(), any(), any(), any());

        // Perform the service call
        long result = this.aidsToNavigationService.findAllTotalCount("uid", null, null, null);

        // Test the result
        assertEquals(10, result);
    }

    /**
     * Test that we can retrieve the paged list of station nodes for a
     * Datatables pagination request (which by the way also includes search and
     * sorting definitions).
     */
    @Test
    void testGetStationNodesForDatatables() {
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
        SearchQuery mockedQuery = mock(SearchQuery.class);
        SearchResult mockedResult = mock(SearchResult.class);
        SearchResultTotal mockedResultTotal = mock(SearchResultTotal.class);
        doReturn(5L).when(mockedResultTotal).hitCount();
        doReturn(mockedResultTotal).when(mockedResult).total();
        doReturn(this.aidsToNavigationList.subList(0, 5)).when(mockedResult).hits();
        doReturn(mockedResult).when(mockedQuery).fetch(any(), any());
        doReturn(mockedQuery).when(this.aidsToNavigationService).getSearchAidsToNavigationQueryByText(any(), any());

        // Perform the service call
        Page<AidsToNavigation> result = this.aidsToNavigationService.handleDatatablesPagingRequest(dtPagingRequest);

        // Validate the result
        assertNotNull(result);
        assertEquals(5, result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertEquals(this.aidsToNavigationList.get(i).getId(), result.getContent().get(i).getId());
            assertEquals(this.aidsToNavigationList.get(i).getAtonNumber(), result.getContent().get(i).getAtonNumber());
            assertEquals(this.aidsToNavigationList.get(i).getIdCode(), result.getContent().get(i).getIdCode());
            assertEquals(this.aidsToNavigationList.get(i).getTextualDescription(), result.getContent().get(i).getTextualDescription());
            assertEquals(this.aidsToNavigationList.get(i).getTextualDescriptionInNationalLanguage(), result.getContent().get(i).getTextualDescriptionInNationalLanguage());
        }
    }

    /**
     * Test that we can save correctly a new or existing Aids to Navigation
     * entry if all the validation checks are successful.
     */
    @Test
    void testSave() {
        doReturn(this.newAidsToNavigation).when(this.aidsToNavigationRepo).save(any());

        // Perform the service call
        AidsToNavigation result = this.aidsToNavigationService.save(this.newAidsToNavigation);

        // Test the result
        assertNotNull(result);
        assertEquals(this.newAidsToNavigation.getId(), result.getId());
        assertEquals(this.newAidsToNavigation.getAtonNumber(), result.getAtonNumber());
        assertEquals(this.newAidsToNavigation.getIdCode(), result.getIdCode());
        assertEquals(this.newAidsToNavigation.getTextualDescription(), result.getTextualDescription());
        assertEquals(this.newAidsToNavigation.getTextualDescriptionInNationalLanguage(), result.getTextualDescriptionInNationalLanguage());

        // Also, that a saving call took place in the repository
        verify(this.aidsToNavigationRepo, times(1)).save(this.newAidsToNavigation);
    }

    /**
     * Test that we can successfully delete an existing Aids to Navigation entry.
     */
    @Test
    void testDelete() throws DataNotFoundException {
        doReturn(Optional.of(this.existingAidsToNavigation)).when(this.aidsToNavigationRepo).findById(this.existingAidsToNavigation.getId());
        doNothing().when(this.aidsToNavigationRepo).delete(this.existingAidsToNavigation);

        // Perform the service call
        this.aidsToNavigationService.delete(this.existingAidsToNavigation.getId());

        // Verify that a deletion call took place in the repository
        verify(this.aidsToNavigationRepo, times(1)).delete(this.existingAidsToNavigation);
    }

    /**
     * Test that if we try to delete a non-existing Aids to Navigation entry
     * then a DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Optional.empty()).when(this.aidsToNavigationRepo).findById(this.existingAidsToNavigation.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.aidsToNavigationService.delete(this.existingAidsToNavigation.getId())
        );
    }

    /**
     * Test that we can successfully delete an existing Aids to Navigation entry
     * by its AtoN Number.
     */
    @Test
    void testDeleteByAtonNumber() {
        doReturn(Optional.of(this.existingAidsToNavigation)).when(this.aidsToNavigationRepo).findByAtonNumber(this.existingAidsToNavigation.getAtonNumber());
        doNothing().when(this.aidsToNavigationService).delete(this.existingAidsToNavigation.getId());

        // Perform the service call
        this.aidsToNavigationService.deleteByAtonNumber(this.existingAidsToNavigation.getAtonNumber());

        // Verify that a deletion call took place in the repository
        verify(this.aidsToNavigationService, times(1)).delete(this.existingAidsToNavigation.getId());
    }

    /**
     * Test that if we try to delete a non-existing Aids to Navigation entry by
     * its AtoN Number then a DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteByAtonNumberNotFound() {
        doReturn(Optional.empty()).when(this.aidsToNavigationRepo).findByAtonNumber(this.existingAidsToNavigation.getAtonNumber());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.aidsToNavigationService.deleteByAtonNumber(this.existingAidsToNavigation.getAtonNumber())
        );
    }

}