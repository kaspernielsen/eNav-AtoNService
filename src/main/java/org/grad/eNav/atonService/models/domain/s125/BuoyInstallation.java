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

import _int.iala_aism.s125.gml._0_0.S125CategoryOfInstallationBuoy;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * The S-125 Buoy Installation Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Buoy
 * Installation type. It is modelled as an entity that extends the
 * {@link GenericBuoy} super class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125BuoyInstallationType
 */
@Entity
public class BuoyInstallation extends GenericBuoy {

    // Class Variables
    @Enumerated(EnumType.STRING)
    private S125CategoryOfInstallationBuoy categoryOfInstallationBuoy;

    /**
     * Gets category of installation buoy.
     *
     * @return the category of installation buoy
     */
    public S125CategoryOfInstallationBuoy getCategoryOfInstallationBuoy() {
        return categoryOfInstallationBuoy;
    }

    /**
     * Sets category of installation buoy.
     *
     * @param categoryOfInstallationBuoy the category of installation buoy
     */
    public void setCategoryOfInstallationBuoy(S125CategoryOfInstallationBuoy categoryOfInstallationBuoy) {
        this.categoryOfInstallationBuoy = categoryOfInstallationBuoy;
    }
}
