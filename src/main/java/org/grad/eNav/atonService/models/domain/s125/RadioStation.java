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

package org.grad.eNav.atonService.models.domain.s125;

import _int.iala_aism.s125.gml._0_0.S125CategoryOfRadioStation;
import _int.iala_aism.s125.gml._0_0.S125Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

/**
 * The S-125 Radio Station Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Radio Station
 * type. It is modelled as an entity that extends the {@link Equipment} super
 * class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125RadioStationType
 */
@Entity
public class RadioStation extends Equipment {

    // Class Variables
    @Enumerated(EnumType.STRING)
    private S125CategoryOfRadioStation categoryOfRadioStation;

    @Enumerated(EnumType.STRING)
    private S125Status status;

    @ManyToOne
    private AISAidToNavigation broadcasts;

    /**
     * Gets category of radio station.
     *
     * @return the category of radio station
     */
    public S125CategoryOfRadioStation getCategoryOfRadioStation() {
        return categoryOfRadioStation;
    }

    /**
     * Sets category of radio station.
     *
     * @param categoryOfRadioStation the category of radio station
     */
    public void setCategoryOfRadioStation(S125CategoryOfRadioStation categoryOfRadioStation) {
        this.categoryOfRadioStation = categoryOfRadioStation;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public S125Status getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(S125Status status) {
        this.status = status;
    }

    /**
     * Gets broadcasts.
     *
     * @return the broadcasts
     */
    public AISAidToNavigation getBroadcasts() {
        return broadcasts;
    }

    /**
     * Sets broadcasts.
     *
     * @param broadcasts the broadcasts
     */
    public void setBroadcasts(AISAidToNavigation broadcasts) {
        this.broadcasts = broadcasts;
    }
}
