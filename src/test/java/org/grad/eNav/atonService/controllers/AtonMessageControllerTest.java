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

package org.grad.eNav.atonService.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grad.eNav.atonService.TestingConfiguration;
import org.grad.eNav.atonService.exceptions.DataNotFoundException;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.domain.AtonMessageType;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.atonService.models.dtos.datatables.*;
import org.grad.eNav.atonService.services.AtonMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AtonMessageController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@Import(TestingConfiguration.class)
class AtonMessageControllerTest {

    /**
     * The Mock MVC.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * The JSON Object Mapper.
     */
    @Autowired
    ObjectMapper objectMapper;

    /**
     * The Station Node Service mock.
     */
    @MockBean
    AtonMessageService atonMessageService;

    // Test Variables
    private List<AtonMessage> atonMessages;
    private Pageable pageable;
    private AtonMessage existingMessage;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Allow the object mapper to deserialize pages
        this.objectMapper.registerModule(new PageJacksonModule());
        this.objectMapper.registerModule(new SortJacksonModule());

        // Initialise the station nodes list
        this.atonMessages = new ArrayList<>();
        for(long i=0; i<10; i++) {
            AtonMessage node = new AtonMessage();
            node.setId(BigInteger.valueOf(i));
            node.setUid("UID" + i);
            node.setType(AtonMessageType.S125);
            node.setMessage("Node Message No" + i);
            this.atonMessages.add(node);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a AtoN message with an ID
        existingMessage = new AtonMessage();
        existingMessage.setId(BigInteger.valueOf(1));
        existingMessage.setUid("UID1");
        existingMessage.setType(AtonMessageType.S125);
        existingMessage.setMessage("Node Message");
    }

    /**
     * Test that we can retrieve all the AtoN messages currently in the database
     * in a paged result.
     */
    @Test
    void testGetAllNodes() throws Exception {
        // Created a result page to be returned by the mocked service
        Page<AtonMessage> page = new PageImpl<>(this.atonMessages.subList(0, 5), this.pageable, this.atonMessages.size());
        doReturn(page).when(this.atonMessageService).findAll(any(), any(), any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        Page<S125Node> result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(page.getSize(), result.getContent().size());

        // Validate the entries one by one
        for(int i=0; i< page.getSize(); i++) {
            assertEquals(page.getContent().get(i).getUid(), result.getContent().get(i).getAtonUID());
            assertEquals(page.getContent().get(i).getMessage(), result.getContent().get(i).getContent());
        }
    }

    /**
     * Test that the API supports the jQuery Datatables server-side paging
     * and search requests.
     */
    @Test
    void testGetNodesForDatatables() throws Exception {
        // Create a test datatables paging request
        DtColumn dtColumn = new DtColumn("id");
        dtColumn.setName("ID");
        dtColumn.setOrderable(true);
        DtOrder dtOrder = new DtOrder();
        dtOrder.setColumn(0);
        dtOrder.setDir(DtDirection.asc);
        DtPagingRequest dtPagingRequest = new DtPagingRequest();
        dtPagingRequest.setStart(0);
        dtPagingRequest.setLength(this.atonMessages.size());
        dtPagingRequest.setDraw(1);
        dtPagingRequest.setSearch(new DtSearch());
        dtPagingRequest.setOrder(Collections.singletonList(dtOrder));
        dtPagingRequest.setColumns(Collections.singletonList(dtColumn));

        // Created a result page to be returned by the mocked service
        Page<AtonMessage> page = new PageImpl<>(this.atonMessages.subList(0, 5), this.pageable, this.atonMessages.size());
        doReturn(page).when(this.atonMessageService).handleDatatablesPagingRequest(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(post("/api/messages/dt")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsString(dtPagingRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and validate the response
        DtPage<S125Node> result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(page.getSize(), result.getData().size());

        // Validate the entries one by one
        for(int i=0; i< page.getSize(); i++) {
            assertEquals(page.getContent().get(i).getUid(), result.getData().get(i).getAtonUID());
            assertEquals(page.getContent().get(i).getMessage(), result.getData().get(i).getContent());
        }
    }

    /**
     * Test that we can correctly delete an existing AtoN message by using a
     * valid ID.
     */
    @Test
    void testDeleteSNode() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(delete("/api/messages/{id}", this.existingMessage.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Test that if we do NOT find the AtoN message we are trying to delete, an
     * HTTP NOT_FOUND response will be returned.
     */
    @Test
    void testDeleteSNodeNotFound() throws Exception {
        doThrow(DataNotFoundException.class).when(this.atonMessageService).delete(any());

        // Perform the MVC request
        this.mockMvc.perform(delete("/api/messages/{id}", this.existingMessage.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that we can correctly delete an existing AtoN message by using a
     * valid UID.
     */
    @Test
    void testDeleteSNodeByUid() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(delete("/api/messages/uid/{uid}", this.existingMessage.getUid())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Test that if we do NOT find the AtoN message we are trying to delete, an
     * HTTP NOT_FOUND response will be returned.
     */
    @Test
    void testDeleteSNodeByUidNotFound() throws Exception {
        doThrow(DataNotFoundException.class).when(this.atonMessageService).deleteByUid(any());

        // Perform the MVC request
        this.mockMvc.perform(delete("/api/messages/uid/{id}", this.existingMessage.getId()))
                .andExpect(status().isNotFound());
    }

}