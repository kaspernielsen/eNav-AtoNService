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

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 * The S-125 Equipment Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Equipment
 * type. It is modelled as an entity class on hibernate, but it is abstract so
 * that we can extend this for each Equipment type.
 * <p>
 * Each equipment links to a parent structure object where it is located.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.EquipmentType
 */
@Entity
public abstract class Equipment extends AidsToNavigation {

    // Class Variables
    @JsonBackReference
    @ManyToOne
    private StructureObject parent;

    /**
     * Gets parent.
     *
     * @return the parent
     */
    public StructureObject getParent() {
        return parent;
    }

    /**
     * Sets parent.
     *
     * @param parent the parent
     */
    public void setParent(StructureObject parent) {
        this.parent = parent;
    }
}
