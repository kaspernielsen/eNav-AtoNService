/*
 * Copyright (c) 2023 GLA Research and Development Directorate
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

import jakarta.persistence.EntityManager;
import org.grad.eNav.atonService.exceptions.SavingFailedException;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.BeaconCardinal;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.repos.DatasetContentRepo;
import org.junit.jupiter.api.Assertions;
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
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetContentServiceTest {

    // Regular expression to look for member tags
    final Pattern DATASET_MEMBER_PATTERN = Pattern.compile("<(/)?[\\s\\S][\\s\\S]\\d:member>");

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    DatasetContentService datasetContentService;

    /**
     * The Model Mapper.
     */
    @Spy
    ModelMapper modelMapper;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Aids to Navigation Service.
     */
    @Mock
    AidsToNavigationService aidsToNavigationService;

    /**
     * The Dataset Content Repo mock.
     */
    @Mock
    DatasetContentRepo datasetContentRepo;

    /**
     * The S-125 Dataset Channel to publish the deleted data to.
     */
    @Mock
    PublishSubscribeChannel s125DeletionChannel;

    // Test Variables
    private GeometryFactory factory;
    private List<AidsToNavigation> aidsToNavigationList;
    private S125Dataset newDataset;
    private S125Dataset existingDataset;
    private DatasetContent newDatasetContent;
    private DatasetContent existingDatasetContent;

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

        // Create a new Dataset with a UUID (required for saving)
        this.newDataset = new S125Dataset("NewDataset");
        this.newDataset.setUuid(UUID.randomUUID());
        this.newDataset.setGeometry(this.factory.createPoint(new Coordinate(52.98, 2.28)));
        this.newDataset.setCreatedAt(LocalDateTime.now());
        this.newDataset.setLastUpdatedAt(LocalDateTime.now());

        // Create a Dataset with a UUID
        this.existingDataset = new S125Dataset("ExistingDataset");
        this.existingDataset.setUuid(UUID.randomUUID());
        this.existingDataset.setGeometry(this.factory.createPoint(new Coordinate(52.98, 2.28)));
        this.existingDataset.setCreatedAt(LocalDateTime.now());
        this.existingDataset.setLastUpdatedAt(LocalDateTime.now());

        // Create a test new content for the dataset
        this.newDatasetContent = new DatasetContent();
        this.newDatasetContent.setContent("New dataset content");
        this.newDatasetContent.setContentLength(BigInteger.valueOf(this.newDatasetContent.getContent().length()));
        this.newDatasetContent.setGeneratedAt(LocalDateTime.now());
        this.newDatasetContent.setDataset(this.newDataset);
        this.newDatasetContent.setDelta("");
        this.newDatasetContent.setDeltaLength(BigInteger.ZERO);

        // And create a test existing content for the dataset
        this.existingDatasetContent = new DatasetContent();
        this.existingDatasetContent.setId(BigInteger.TWO);
        this.existingDatasetContent.setContent("Existing dataset content");
        this.existingDatasetContent.setContentLength(BigInteger.valueOf(this.existingDatasetContent.getContent().length()));
        this.existingDatasetContent.setGeneratedAt(LocalDateTime.now());
        this.existingDataset.setDatasetContent(this.existingDatasetContent);
    }

    /**
     * Test that we can successfully save a new dataset content into the
     * database and the updated result will be returned.
     */
    @Test
    void testSave() {
        doReturn(this.newDatasetContent).when(this.datasetContentRepo).save(any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.save(this.newDatasetContent);

        // Test the result
        assertNotNull(result);
        Assertions.assertEquals(this.newDatasetContent.getId(), result.getId());
        Assertions.assertEquals(this.newDatasetContent.getGeneratedAt(), result.getGeneratedAt());
        Assertions.assertEquals(this.newDatasetContent.getSequenceNo(), result.getSequenceNo());
        Assertions.assertEquals(this.newDatasetContent.getContent(), result.getContent());
        Assertions.assertEquals(this.newDatasetContent.getContentLength(), result.getContentLength());
        Assertions.assertEquals(this.newDatasetContent.getDelta(), result.getDelta());
        Assertions.assertEquals(this.newDatasetContent.getDeltaLength(), result.getDeltaLength());
    }

    /**
     * Test that we can successfully save a new dataset content into the
     * database and the updated result will be returned.
     */
    @Test
    void testSaveWithoutDataset() {
        // Remove the dataset link from the dataset content
        this.newDatasetContent.setDataset(null);

        // Perform the service call
        assertThrows(SavingFailedException.class, () ->
                this.datasetContentService.save(this.newDatasetContent)
        );
    }

    /**
     * Test that we can successfully generate the content of a dataset provided
     * that we can access its respective member entries. In the current case
     * this should be an S-125 dataset with the same number of members in the
     * content as the AtoN that are assigned to it.
     */
    @Test
    void testGenerateDatasetContent() throws ExecutionException, InterruptedException {
        final int numOfAtons = 5;
        final Page<AidsToNavigation> aidsToNavigationPage = new PageImpl<>(this.aidsToNavigationList.subList(0, numOfAtons), Pageable.ofSize(5), this.aidsToNavigationList.size());

        // Mock the service calls
        doReturn(aidsToNavigationPage).when(this.aidsToNavigationService).findAll(any(), any(), any(), any(), any());
        doAnswer((inv) -> inv.getArgument(0)).when(this.datasetContentService).save(any());

        // Perform the service call
        CompletableFuture<S125Dataset> result = this.datasetContentService.generateDatasetContent(this.existingDataset);

        // Test the result
        assertNotNull(result);
        assertTrue(result.isDone());
        assertNotNull(result.get());

        // Now extract the dataset from the result
        S125Dataset resultDataset = result.get();
        assertNotNull(resultDataset.getDatasetContent());
        assertNotNull(resultDataset.getDatasetContent().getContent());
        assertEquals(2*numOfAtons, DATASET_MEMBER_PATTERN.matcher(resultDataset.getDatasetContent().getContent()).results().count());
        assertEquals(BigInteger.valueOf(result.get().getDatasetContent().getContent().length()), result.get().getDatasetContent().getContentLength());

        // Make also sure that we save and published the generated content
        verify(this.datasetContentService, times(1)).save(any(DatasetContent.class));
        verify(this.s125DeletionChannel, times(1)).send(any(Message.class));
    }

    /**
     * Test that we ty to generate the content of a dataset provided and an
     * exception is thrown, the CompletableFuture response will include the
     * message of that exception.
     */
    @Test
    void testGenerateDatasetContentWithException() throws ExecutionException, InterruptedException {
        final int numOfAtons = 5;
        final Page<AidsToNavigation> aidsToNavigationPage = new PageImpl<>(this.aidsToNavigationList.subList(0, numOfAtons), Pageable.ofSize(5), this.aidsToNavigationList.size());

        // Mock the service calls
        doReturn(aidsToNavigationPage).when(this.aidsToNavigationService).findAll(any(), any(), any(), any(), any());
        doThrow(new MappingException(Collections.emptyList())).when(this.modelMapper).map(any(), any());

        // Perform the service call
        CompletableFuture<S125Dataset> result = this.datasetContentService.generateDatasetContent(this.existingDataset);

        // Test the result
        assertNotNull(result);
        assertTrue(result.isDone());
        assertTrue(result.isCompletedExceptionally());
        assertThrows(ExecutionException.class, result::get);

        // Make also sure that we save and published the generated content
        verify(this.datasetContentRepo, never()).save(any(DatasetContent.class));
        verify(this.s125DeletionChannel, never()).send(any(Message.class));
    }

}