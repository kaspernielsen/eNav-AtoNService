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

package org.grad.eNav.atonService.models.domain.s125;

import _int.iho.s125.gml.cs0._1.StatusType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * The S-125 Synthetic AIS Aids to Navigation Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Synthetic AIS
 * Aids to Navigation type. It is modelled as an entity that extends the
 * {@link AISAidToNavigation} super class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iho.s125.gml.cs0._1.SyntheticAISAidToNavigation
 */
@Entity
public class SyntheticAISAidToNavigation extends AISAidToNavigation {

    // Class Variables
    private BigDecimal estimatedRangeOfTransmission;

    private BigInteger mmsiCode;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = StatusType.class)
    private List<StatusType> statuses;

    /**
     * Gets estimated range of transmission.
     *
     * @return the estimated range of transmission
     */
    public BigDecimal getEstimatedRangeOfTransmission() {
        return estimatedRangeOfTransmission;
    }

    /**
     * Sets estimated range of transmission.
     *
     * @param estimatedRangeOfTransmission the estimated range of transmission
     */
    public void setEstimatedRangeOfTransmission(BigDecimal estimatedRangeOfTransmission) {
        this.estimatedRangeOfTransmission = estimatedRangeOfTransmission;
    }

    /**
     * Gets mmsi code.
     *
     * @return the mmsi code
     */
    public BigInteger getMmsiCode() {
        return mmsiCode;
    }

    /**
     * Sets mmsi code.
     *
     * @param mmsiCode the mmsi code
     */
    public void setMmsiCode(BigInteger mmsiCode) {
        this.mmsiCode = mmsiCode;
    }

    /**
     * Gets statuses.
     *
     * @return the statuses
     */
    public List<StatusType> getStatuses() {
        return statuses;
    }

    /**
     * Sets statuses.
     *
     * @param statuses the statuses
     */
    public void setStatuses(List<StatusType> statuses) {
        this.statuses = statuses;
    }
}
