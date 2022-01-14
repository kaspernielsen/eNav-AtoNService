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

import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.domain.SNodeType;
import org.grad.eNav.atonService.models.dtos.datatables.*;
import org.grad.eNav.atonService.repos.AtonMessageRepo;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtonMessageServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    AtonMessageService atonMessageService;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Station Node Repository Mock.
     */
    @Mock
    AtonMessageRepo sNodeRepo;

    // Test Variables
    private List<AtonMessage> nodes;
    private Pageable pageable;
    private AtonMessage newNode;
    private AtonMessage existingNode;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the station nodes list
        this.nodes = new ArrayList<>();
        for(long i=0; i<10; i++) {
            AtonMessage node = new AtonMessage();
            node.setId(BigInteger.valueOf(i));
            node.setUid("UID" + i);
            node.setType(SNodeType.S125);
            node.setMessage("Node Message No" + i);
            this.nodes.add(node);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new station node
        this.newNode = new AtonMessage();
        this.newNode.setUid("UID1");
        this.newNode.setType(SNodeType.S125);
        this.newNode.setMessage("Node Message");

        // Create a station node with an ID
        this.existingNode = new AtonMessage();
        this.existingNode.setId(BigInteger.TEN);
        this.existingNode.setUid("UID10");
        this.existingNode.setType(SNodeType.S125);
        this.existingNode.setMessage("Node Message");
    }

    /**
     * Test that we can retrieve all the station nodes currently present in the
     * database.
     */
    @Test
    void testFindAll() {
        // Created a result page to be returned by the mocked repository
        doReturn(this.nodes).when(this.sNodeRepo).findAll();

        // Perform the service call
        List<AtonMessage> result = this.atonMessageService.findAll();

        // Test the result
        assertEquals(this.nodes.size(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertEquals(this.nodes.get(i), result.get(i));
        }
    }

    /**
     * Test that we can retrieve all the station nodes currently present in the
     * database through a paged call.
     */
    @Test
    void testFindAllPaged() {
        // Created a result page to be returned by the mocked repository
        Page<AtonMessage> page = new PageImpl<>(this.nodes.subList(0, 5), this.pageable, this.nodes.size());
        doReturn(page).when(this.sNodeRepo).findAll(this.pageable);

        // Perform the service call
        Page<AtonMessage> result = this.atonMessageService.findAll(pageable);

        // Test the result
        assertEquals(page.getSize(), result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertEquals(this.nodes.get(i), result.getContent().get(i));
        }
    }

    /**
     * Test that we can retrieve a single station node entry based on the
     * station node ID and all the eager relationships are loaded.
     */
    @Test
    void testFindOne() {
        doReturn(Optional.of(this.existingNode)).when(this.sNodeRepo).findById(this.existingNode.getId());

        // Perform the service call
        AtonMessage result = this.atonMessageService.findOne(this.existingNode.getId());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingNode.getId(), result.getId());
        assertEquals(this.existingNode.getUid(), result.getUid());
        assertEquals(this.existingNode.getType(), result.getType());
        assertEquals(this.existingNode.getMessage(), result.getMessage());
    }

    /**
     * Test that we if the provided station node ID does NOT exist, then when
     * trying to retrieve the respective station node will return a
     * DataNotFoundException.
     */
    @Test
    void testFindOneNotFound() {
        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.atonMessageService.findOne(this.existingNode.getId())
        );
    }

    /**
     * Test that we can retrieve a single station node entry based on the
     * station node UID and all the eager relationships are loaded.
     */
    @Test
    void testFindOneByUid() {
        doReturn(this.existingNode).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        AtonMessage result = this.atonMessageService.findOneByUid(this.existingNode.getUid());

        // Make sure the eager relationships repo call was called
        verify(this.sNodeRepo, times(1)).findByUid(this.existingNode.getUid());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingNode.getId(), result.getId());
        assertEquals(this.existingNode.getUid(), result.getUid());
        assertEquals(this.existingNode.getType(), result.getType());
        assertEquals(this.existingNode.getMessage(), result.getMessage());
    }

    /**
     * Test that we if the provided station node UID does NOT exist, then when
     * trying to retrieve the respective station node will return a
     * DataNotFoundException.
     */
    @Test
    void testFindOneByUidNotFound() {
        doReturn(null).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.atonMessageService.findOneByUid(this.existingNode.getUid())
        );
    }

    /**
     * Test that we can save correctly a new or existing station node if all
     * the validation checks are successful.
     */
    @Test
    void testSave() {
        doReturn(this.newNode).when(this.sNodeRepo).save(any());

        // Perform the service call
        AtonMessage result = this.atonMessageService.save(this.newNode);

        // Test the result
        assertNotNull(result);
        assertEquals(this.newNode.getId(), result.getId());
        assertEquals(this.newNode.getUid(), result.getUid());
        assertEquals(this.newNode.getType(), result.getType());
        assertEquals(this.newNode.getMessage(), result.getMessage());

        // Also that a saving call took place in the repository
        verify(this.sNodeRepo, times(1)).save(this.newNode);
    }

    /**
     * Test that we can successfully delete an existing station node.
     */
    @Test
    void testDelete() throws DataNotFoundException {
        doReturn(Optional.of(this.existingNode)).when(this.sNodeRepo).findById(this.existingNode.getId());
        doNothing().when(this.sNodeRepo).delete(this.existingNode);

        // Perform the service call
        this.atonMessageService.delete(this.existingNode.getId());

        // Verify that a deletion call took place in the repository
        verify(this.sNodeRepo, times(1)).delete(this.existingNode);
    }

    /**
     * Test that if we try to delete a non-existing station node then a
     * DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Optional.empty()).when(this.sNodeRepo).findById(this.existingNode.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.atonMessageService.delete(this.existingNode.getId())
        );
    }

    /**
     * Test that we can successfully delete an existing station node by its UID.
     */
    @Test
    void testDeleteByUid() {
        doReturn(this.existingNode).when(this.sNodeRepo).findByUid(this.existingNode.getUid());
        doNothing().when(this.atonMessageService).delete(this.existingNode.getId());

        // Perform the service call
        this.atonMessageService.deleteByUid(this.existingNode.getUid());

        // Verify that a deletion call took place in the repository
        verify(this.atonMessageService, times(1)).delete(this.existingNode.getId());
    }

    /**
     * Test that if we try to delete a non-existing station node by its UID then
     * a DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteByUidNotFound() {
        doReturn(null).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.atonMessageService.deleteByUid(this.existingNode.getUid())
        );
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
        doReturn(this.nodes.subList(0, 5)).when(mockedResult).hits();
        doReturn(mockedResult).when(mockedQuery).fetch(any(), any());
        doReturn(mockedQuery).when(this.atonMessageService).searchSNodesQuery(any(), any());

        // Perform the service call
        DtPage<AtonMessage> result = this.atonMessageService.handleDatatablesPagingRequest(dtPagingRequest);

        // Validate the result
        assertNotNull(result);
        assertEquals(5, result.getRecordsFiltered());

        // Test each of the result entries
        for(int i=0; i < result.getRecordsFiltered(); i++){
            assertEquals(this.nodes.get(i), result.getData().get(i));
        }
    }

}