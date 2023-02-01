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

import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.models.domain.DatasetType;
import org.grad.eNav.atonService.models.domain.s125.AidsToNavigation;
import org.grad.eNav.atonService.models.domain.s125.BeaconCardinal;
import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.repos.DatasetContentRepo;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DatasetContentServiceTest {

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
     * The Aids to Navigation Service mock.
     */
    @Mock
    AidsToNavigationService aidsToNavigationService;

    /**
     * The Dataset Service mock.
     */
    @Mock
    DatasetService datasetService;

    /**
     * The Dataset Content Repo mock.
     */
    @Mock
    DatasetContentRepo datasetContentRepo;

    // Test Variables
    private List<AidsToNavigation> aidsToNavigationList;
    private S125DataSet newDataset;
    private S125DataSet existingDataset;
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

        // Create a Dataset without a UUID
        this.newDataset = new S125DataSet("NewDataset");
        this.newDataset.setCreatedAt(LocalDateTime.now());
        this.newDataset.setLastUpdatedAt(LocalDateTime.now());
        this.newDataset.setGeometry(this.factory.createPoint(new Coordinate(51.98, 1.28)));

        // Create a Dataset with a UUID
        this.existingDataset = new S125DataSet("ExistingDataset");
        this.existingDataset.setUuid(UUID.randomUUID());
        this.existingDataset.setCreatedAt(LocalDateTime.now());
        this.existingDataset.setLastUpdatedAt(LocalDateTime.now());
        this.existingDataset.setGeometry(this.factory.createPoint(new Coordinate(52.98, 2.28)));

        // Create a Dataset Content for the new dataset
        this.newDatasetContent = new DatasetContent();
        this.newDatasetContent.setDatasetType(DatasetType.S125);
        this.newDatasetContent.setCreatedAt(LocalDateTime.now());
        this.newDatasetContent.setGeometry(this.newDataset.getGeometry());
        this.newDatasetContent.setContent("New Dataset Content");
        this.newDatasetContent.setContentLength(BigInteger.valueOf(this.newDatasetContent.getContent().length()));

        // Create a Dataset Content for the existing dataset
        this.existingDatasetContent = new DatasetContent();
        this.existingDatasetContent.setId(BigInteger.ONE);
        this.existingDatasetContent.setDatasetType(DatasetType.S125);
        this.existingDatasetContent.setCreatedAt(LocalDateTime.now());
        this.existingDatasetContent.setGeometry(this.existingDataset.getGeometry());
        this.existingDatasetContent.setContent("Existing Dataset Content");
        this.existingDatasetContent.setContentLength(BigInteger.valueOf(this.existingDatasetContent.getContent().length()));
    }

    /**
     * Test that we can find the latest content of a dataset specified by its
     * dataset UUID, without requiring a reference date-time.
     */
    @Test
    void testFindLatest() {
        doReturn(Collections.singletonList(this.existingDatasetContent)).when(this.datasetContentRepo).findLatestForUuid(any(), any(), any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.findLatest(this.existingDataset.getUuid());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingDatasetContent.getId(), result.getId());
        assertEquals(this.existingDatasetContent.getUuid(), result.getUuid());
        assertEquals(this.existingDatasetContent.getDatasetType(), result.getDatasetType());
        assertEquals(this.existingDatasetContent.getCreatedAt(), result.getCreatedAt());
        assertEquals(this.existingDatasetContent.getGeometry(), result.getGeometry());
        assertEquals(this.existingDatasetContent.getContent(), result.getContent());
        assertEquals(this.existingDatasetContent.getContentLength(), result.getContentLength());
    }

    /**
     * Test that we can find the latest content of a dataset specified by its
     * dataset UUID, as well as a reference date-time.
     */
    @Test
    void testFindLatestWithReferenceDateTime() {
        doReturn(Collections.singletonList(this.existingDatasetContent)).when(this.datasetContentRepo).findLatestForUuid(any(), any(), any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.findLatest(this.existingDataset.getUuid(), LocalDateTime.now());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingDatasetContent.getId(), result.getId());
        assertEquals(this.existingDatasetContent.getUuid(), result.getUuid());
        assertEquals(this.existingDatasetContent.getDatasetType(), result.getDatasetType());
        assertEquals(this.existingDatasetContent.getCreatedAt(), result.getCreatedAt());
        assertEquals(this.existingDatasetContent.getGeometry(), result.getGeometry());
        assertEquals(this.existingDatasetContent.getContent(), result.getContent());
        assertEquals(this.existingDatasetContent.getContentLength(), result.getContentLength());
    }

    /**
     * Test that we can find the latest content of a dataset specified by its
     * dataset UUID, even if it does not exist, since it will be autogenerated
     * on the fly.
     */
    @Test
    void testFindLatestIfNotExists() {
        final int numOfAtons = 5;
        final Page<AidsToNavigation> aidsToNavigationPage = new PageImpl<>(this.aidsToNavigationList.subList(0, numOfAtons), Pageable.ofSize(5), this.aidsToNavigationList.size());
        doReturn(Collections.emptyList()).when(this.datasetContentRepo).findLatestForUuid(any(), any(), any());
        doReturn(this.existingDataset).when(this.datasetService).findOne(eq(this.existingDataset.getUuid()));
        doReturn(aidsToNavigationPage).when(this.aidsToNavigationService).findAll(any(), any(), any(), any(), any());
        doReturn(this.existingDatasetContent).when(this.datasetContentRepo).save(any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.findLatest(this.existingDataset.getUuid(), LocalDateTime.now());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingDatasetContent.getId(), result.getId());
        assertEquals(this.existingDatasetContent.getUuid(), result.getUuid());
        assertEquals(this.existingDatasetContent.getDatasetType(), result.getDatasetType());
        assertEquals(this.existingDatasetContent.getCreatedAt(), result.getCreatedAt());
        assertEquals(this.existingDatasetContent.getGeometry(), result.getGeometry());
        assertEquals(this.existingDatasetContent.getContent(), result.getContent());
        assertEquals(this.existingDatasetContent.getContentLength(), result.getContentLength());
    }

    /**
     * Test that we can successfully save a new dataset content into the
     * database and the updated result will be returned.
     */
    @Test
    void testSave() {
        doReturn(this.existingDatasetContent).when(this.datasetContentRepo).save(any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.save(this.newDatasetContent);

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingDatasetContent.getId(), result.getId());
        assertEquals(this.existingDatasetContent.getUuid(), result.getUuid());
        assertEquals(this.existingDatasetContent.getDatasetType(), result.getDatasetType());
        assertEquals(this.existingDatasetContent.getCreatedAt(), result.getCreatedAt());
        assertEquals(this.existingDatasetContent.getGeometry(), result.getGeometry());
        assertEquals(this.existingDatasetContent.getContent(), result.getContent());
        assertEquals(this.existingDatasetContent.getContentLength(), result.getContentLength());
    }

    /**
     * Test that we can successfully generate the content of a dataset provided
     * that we can access its respective member entries. In the current case
     * this should be an S-125 dataset with the same number of members in the
     * content as the AtoN that are assigned to it. Also note that the dataset
     * ID is set to null since this is a new content object and hasn't been
     * store in the database yet.
     */
    @Test
    void testGenerateDatasetContent() {
        final int numOfAtons = 5;
        final Page<AidsToNavigation> aidsToNavigationPage = new PageImpl<>(this.aidsToNavigationList.subList(0, numOfAtons), Pageable.ofSize(5), this.aidsToNavigationList.size());
        doReturn(aidsToNavigationPage).when(this.aidsToNavigationService).findAll(any(), any(), any(), any(), any());

        // Perform the service call
        DatasetContent result = this.datasetContentService.generateDatasetContent(this.existingDataset);

        // Test the result
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(this.existingDataset.getUuid(), result.getUuid());
        assertEquals(DatasetType.S125, result.getDatasetType());
        assertNotNull(result.getCreatedAt());
        assertEquals(this.existingDataset.getGeometry(), result.getGeometry());
        assertNotNull(result.getContent());
        assertEquals(BigInteger.valueOf(result.getContent().length()), result.getContentLength());
        assertEquals(numOfAtons, StringUtils.countMatches(result.getContent(), "<member>"));
        assertEquals(numOfAtons, StringUtils.countMatches(result.getContent(), "</member>"));
    }

}