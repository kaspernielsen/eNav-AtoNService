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

import _int.iala_aism.s125.gml._0_0.S125CategoryOfPile;
import _int.iala_aism.s125.gml._0_0.S125Colour;
import _int.iala_aism.s125.gml._0_0.S125ColourPattern;
import _int.iala_aism.s125.gml._0_0.S125VisuallyConspicuous;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;

/**
 * The S-125 Pile Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Pile type.
 * It is modelled as an entity that extends the {@link StructureObject} super
 * class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125PileType
 */
@Entity
public class Pile extends StructureObject {

    // Class Variables
    @Enumerated(EnumType.STRING)
    private S125CategoryOfPile categoryOfPile;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Colour.class)
    private List<S125Colour> colours;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125ColourPattern.class)
    private List<S125ColourPattern> colourPatterns;

    @Enumerated(EnumType.STRING)
    private S125VisuallyConspicuous visuallyConspicuous;

    private BigDecimal height;

    /**
     * Gets category of pile.
     *
     * @return the category of pile
     */
    public S125CategoryOfPile getCategoryOfPile() {
        return categoryOfPile;
    }

    /**
     * Sets category of pile.
     *
     * @param categoryOfPile the category of pile
     */
    public void setCategoryOfPile(S125CategoryOfPile categoryOfPile) {
        this.categoryOfPile = categoryOfPile;
    }

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
     * Gets visually conspicuous.
     *
     * @return the visually conspicuous
     */
    public S125VisuallyConspicuous getVisuallyConspicuous() {
        return visuallyConspicuous;
    }

    /**
     * Sets visually conspicuous.
     *
     * @param visuallyConspicuous the visually conspicuous
     */
    public void setVisuallyConspicuous(S125VisuallyConspicuous visuallyConspicuous) {
        this.visuallyConspicuous = visuallyConspicuous;
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    public BigDecimal getHeight() {
        return height;
    }

    /**
     * Sets height.
     *
     * @param height the height
     */
    public void setHeight(BigDecimal height) {
        this.height = height;
    }
}
