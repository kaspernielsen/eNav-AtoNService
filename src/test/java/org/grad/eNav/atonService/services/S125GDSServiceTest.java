/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.services;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.SimpleFeatureSource;
import org.grad.eNav.atonService.components.S125GDSListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S125GDSServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    S125GDSService s125GDSService;

    /**
     * The Application Context mock.
     */
    @Mock
    ApplicationContext applicationContext;

    /**
     * The AtoN Information Channel to publish the published data to.
     */
    @Mock
    PublishSubscribeChannel atonPublicationChannel;

    /**
     * The AtoN Information Channel to publish the deleted data to.
     */
    @Mock
    PublishSubscribeChannel atonDeletionChannel;

    /**
     * The Geomesa Data Store mock.
     */
    @Mock
    DataStore consumer;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Set the service geometry
        this.s125GDSService.geometryWKT = "POINT (52.001 1.002)";
    }

    /**
     * Test that the S125 Geomesa Datastore service can initialise correctly
     * and register a datastore listener for each of the stations present in
     * the database.
     */
    @Test
    void testInit() throws IOException {
        // Create a Datastore Listener to be returned by the listener initialisation
        doAnswer((invocation) -> new S125GDSListener()).when(this.applicationContext).getBean(S125GDSListener.class);
        SimpleFeatureSource featureSource = mock(SimpleFeatureSource.class);
        doReturn(featureSource).when(this.consumer).getFeatureSource(any(String.class));

        // Perform the service call
        this.s125GDSService.init();

        // Assert that only the one listener is set as a deletion handler
        assertNotNull(this.s125GDSService.gdsListener);
    }

    /**
     * Test that the S125 Geomesa Datastore service will not initialise if a
     * valid Geomesa Datastore does NOT exist.
     */
    @Test
    void testInitNoDatastore() {
        // Cancel the consumer datastore
        this.s125GDSService.consumer = null;

        // Perform the service call
        this.s125GDSService.init();

        // Assert no listener were generated
        assertNull(this.s125GDSService.gdsListener);
    }

    /**
     * Test that the S125 Geomesa Datastore service can be destroyed gracefully,
     * and it disconnects from the connected Geomesa datastore.
     */
    @Test
    void testDestroy() {
        // Create a mock Datastore Listener to be returned by the listener initialisation
        S125GDSListener mockListener = mock(S125GDSListener.class);
        doReturn(mockListener).when(this.applicationContext).getBean(S125GDSListener.class);

        // First initialise the service to pick up the listeners
        this.s125GDSService.init();

        // Perform the service call
        this.s125GDSService.destroy();

        // Make sure the listeners and the Geomesa datastore gets disconnected
        verify(mockListener, times(1)).destroy();
        verify(this.consumer, times(1)).dispose();
    }

    /**
     * Test that the S125 Geomesa Datastore service can be destroyed gracefully,
     * but if this happens during a reloading operation, the Geomesa DataStore
     * consumer will NOT be dropped.
     */
    @Test
    void testDestroyWhileReloading() {
        // Create a mock Datastore Listener to be returned by the listener initialisation
        S125GDSListener mockListener = mock(S125GDSListener.class);
        doReturn(mockListener).when(this.applicationContext).getBean(S125GDSListener.class);

        // First initialise the service to pick up the listeners
        this.s125GDSService.init();

        // Mock a reloading operation
        this.s125GDSService.reloading = true;

        // Perform the service call
        this.s125GDSService.destroy();

        // Make sure the listeners and the Geomesa datastore gets disconnected
        verify(mockListener, times(1)).destroy();
        verify(this.consumer, never()).dispose();
    }

    /**
     * Test that the S125 Geomesa Datastore service can reload by calling the
     * initialisation procedure on demand.
     */
    @Test
    void testReload() throws IOException {
        // Create a mock Datastore Listener to be returned by the listener initialisation
        doAnswer((invocation) -> new S125GDSListener()).when(this.applicationContext).getBean(S125GDSListener.class);
        SimpleFeatureSource featureSource = mock(SimpleFeatureSource.class);
        doReturn(featureSource).when(this.consumer).getFeatureSource(any(String.class));

        // Perform the service call
        this.s125GDSService.reload();

        // Assert that only the one listener is set as a deletion handler
        assertNotNull(this.s125GDSService.gdsListener);
    }

}