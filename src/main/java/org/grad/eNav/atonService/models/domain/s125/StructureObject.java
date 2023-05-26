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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * The S-125 Structure Object Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Structure
 * type. It is modelled as an entity class on hibernate, but it is abstract so
 * that we can extend this for each Structure Object type.
 * <p>
 * Each structure contains a list of equipment objects that is hosts.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.StructureObjectType
 */
@Entity
public abstract class StructureObject extends AidsToNavigation {

    //Class Variables
    @JsonManagedReference
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Equipment> children;

    /**
     * Gets children.
     *
     * @return the children
     */
    public List<Equipment> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Sets children.
     *
     * @param children the children
     */
    public void setChildren(List<Equipment> children) {
        this.children = null;
        if (children!= null) {
            this.getChildren().addAll(children);
        }
    }
}
