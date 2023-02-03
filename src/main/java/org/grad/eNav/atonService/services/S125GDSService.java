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
import org.geotools.data.DataStore;
import org.grad.eNav.atonService.components.S125GDSListener;
import org.grad.eNav.atonService.models.GeomesaS125;
import org.grad.eNav.atonService.utils.WKTUtil;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Optional;

/**
 * The AtoN Geomesa Data Store Service Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class S125GDSService {

    /**
     * The Application Context
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * The GDS Service Geometry Listening Definition.
     */
    @Value("${gla.rad.aton-service.geometry:POLYGON ((-180 -90, -180 90, 180 90, 180 -90, -180 -90))}")
    String geometryWKT;

    /**
     * The Geomesa Data Store.
     */
    @Autowired
    @Qualifier("gsDataStore")
    DataStore consumer;

    // Service Variables
    protected S125GDSListener gdsListener;
    protected boolean reloading;

    /**
     * Once the service has been initialised, we can that start the execution
     * of the kafka data store listeners as a separate components that will run
     * on independent threads. All incoming messages with then be consumed by
     * the same handler, but handled based on the topic.
     */
    @PostConstruct
    public void init() {
        log.info("Geomesa Data Store Service is booting up...");

        // Create the consumer
        if(this.consumer == null) {
            log.error("Unable to connect to data store");
            return;
        }

        // Get and initialise the listener workers' list
        // Note that the first listener also becomes responsible for handling
        // the station node deletions by UID. Unfortunately Geomesa does not
        // support geographic filtering in deletions, so we have to do it
        // manually.
        this.gdsListener = this.applicationContext.getBean(S125GDSListener.class);
        try {
            gdsListener.init(this.consumer,
                    new GeomesaS125(WKTUtil.convertWKTtoGeometry(this.geometryWKT)),
                    WKTUtil.convertWKTtoGeometry(this.geometryWKT));
        } catch (IOException | ParseException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        Optional.ofNullable(gdsListener).ifPresent(S125GDSListener::destroy);

        // If we are just reloading, don't drop the Geomesa DataStore Consumer
        if(this.reloading) {
            return;
        }

        log.info("Geomesa Data Store is shutting down...");
        this.consumer.dispose();
    }

    /**
     * Whenever we get changes in the stations' configuration, we will need
     * to reload the S125GDSListeners for each station, so basically we need
     * to call the init function again.
     */
    public void reload() {
        // Same as destroy - but set a flag to be safe
        this.reloading = true;
        this.destroy();
        this.reloading = false;

        // And re-initialise the service
        this.init();
    }

}
