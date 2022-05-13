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

import _int.iala_aism.s125.gml._0_0.S125Colour;
import _int.iala_aism.s125.gml._0_0.S125ColourPattern;
import _int.iala_aism.s125.gml._0_0.S125MarksNavigationalSystemOf;
import _int.iala_aism.s125.gml._0_0.S125Status;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

/**
 * The S-125 Retro Reflector Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Retro Reflector
 * type. It is modelled as an entity that extends the {@link Equipment} super
 * class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125RetroReflectorType
 */
@Entity
public class RetroReflector extends Equipment {

    // Class Variables
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Colour.class)
    private List<S125Colour> colours;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125ColourPattern.class)
    private List<S125ColourPattern> colourPatterns;

    @Enumerated(EnumType.STRING)
    private S125MarksNavigationalSystemOf marksNavigationalSystemOf;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    private List<S125Status> statuses;

    /**
     * Gets colours.
     *
     * @return the colours
     */
    public List<S125Colour> getColours() {
        return colours;
    }

    /**
     * Sets colours.
     *
     * @param colours the colours
     */
    public void setColours(List<S125Colour> colours) {
        this.colours = colours;
    }

    /**
     * Gets colour patterns.
     *
     * @return the colour patterns
     */
    public List<S125ColourPattern> getColourPatterns() {
        return colourPatterns;
    }

    /**
     * Sets colour patterns.
     *
     * @param colourPatterns the colour patterns
     */
    public void setColourPatterns(List<S125ColourPattern> colourPatterns) {
        this.colourPatterns = colourPatterns;
    }

    /**
     * Gets marks navigational system of.
     *
     * @return the marks navigational system of
     */
    public S125MarksNavigationalSystemOf getMarksNavigationalSystemOf() {
        return marksNavigationalSystemOf;
    }

    /**
     * Sets marks navigational system of.
     *
     * @param marksNavigationalSystemOf the marks navigational system of
     */
    public void setMarksNavigationalSystemOf(S125MarksNavigationalSystemOf marksNavigationalSystemOf) {
        this.marksNavigationalSystemOf = marksNavigationalSystemOf;
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
