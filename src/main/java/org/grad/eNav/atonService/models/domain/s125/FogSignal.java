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

import _int.iala_aism.s125.gml._0_0.S125CategoryOfFogSignal;
import _int.iala_aism.s125.gml._0_0.S125Status;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;

/**
 * The S-125 Fog Signal Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Fog Signal
 * type. It is modelled as an entity that extends the {@link Equipment} super
 * class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125FogSignalType
 */
@Entity
public class FogSignal extends Equipment {

    // Class Variables
    @Enumerated(EnumType.STRING)
    private S125CategoryOfFogSignal categoryOfFogSignal;

    private String signalSequence;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    private List<S125Status> statuses;

    /**
     * Gets category of fog signal.
     *
     * @return the category of fog signal
     */
    public S125CategoryOfFogSignal getCategoryOfFogSignal() {
        return categoryOfFogSignal;
    }

    /**
     * Sets category of fog signal.
     *
     * @param categoryOfFogSignal the category of fog signal
     */
    public void setCategoryOfFogSignal(S125CategoryOfFogSignal categoryOfFogSignal) {
        this.categoryOfFogSignal = categoryOfFogSignal;
    }

    /**
     * Gets signal sequence.
     *
     * @return the signal sequence
     */
    public String getSignalSequence() {
        return signalSequence;
    }

    /**
     * Sets signal sequence.
     *
     * @param signalSequence the signal sequence
     */
    public void setSignalSequence(String signalSequence) {
        this.signalSequence = signalSequence;
    }

    /**
     * Gets statuses.
     *
     * @return the statuses
     */
    public List<S125Status> getStatuses() {
        return statuses;
    }

    /**
     * Sets statuses.
     *
     * @param statuses the statuses
     */
    public void setStatuses(List<S125Status> statuses) {
        this.statuses = statuses;
    }
}
