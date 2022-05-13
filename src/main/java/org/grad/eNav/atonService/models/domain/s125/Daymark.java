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

import _int.iala_aism.s125.gml._0_0.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;

/**
 * The S-125 Daymark Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Daymark type.
 * It is modelled as an entity that extends the {@link Equipment} super
 * class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125DaymarkType
 */
@Entity
public class Daymark extends Equipment {

    // Class Variables
    private S125CategoryOfSpecialPurposeMark categoryOfSpecialPurposeMark;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Colour.class)
    private List<S125Colour> colours;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125ColourPattern.class)
    private List<S125ColourPattern> colourPatterns;

    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125NatureOfConstruction.class)
    private List<S125NatureOfConstruction> natureOfConstructions;

    private String objectNameInNationalLanguage;

    private String objectName;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    private List<S125Status> statuses;

    private String topmarkDaymarkShape;

    /**
     * Gets category of special purpose mark.
     *
     * @return the category of special purpose mark
     */
    public S125CategoryOfSpecialPurposeMark getCategoryOfSpecialPurposeMark() {
        return categoryOfSpecialPurposeMark;
    }

    /**
     * Sets category of special purpose mark.
     *
     * @param categoryOfSpecialPurposeMark the category of special purpose mark
     */
    public void setCategoryOfSpecialPurposeMark(S125CategoryOfSpecialPurposeMark categoryOfSpecialPurposeMark) {
        this.categoryOfSpecialPurposeMark = categoryOfSpecialPurposeMark;
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

    /**
     * Gets nature of constructions.
     *
     * @return the nature of constructions
     */
    public List<S125NatureOfConstruction> getNatureOfConstructions() {
        return natureOfConstructions;
    }

    /**
     * Sets nature of constructions.
     *
     * @param natureOfConstructions the nature of constructions
     */
    public void setNatureOfConstructions(List<S125NatureOfConstruction> natureOfConstructions) {
        this.natureOfConstructions = natureOfConstructions;
    }

    /**
     * Gets object name in national language.
     *
     * @return the object name in national language
     */
    public String getObjectNameInNationalLanguage() {
        return objectNameInNationalLanguage;
    }

    /**
     * Sets object name in national language.
     *
     * @param objectNameInNationalLanguage the object name in national language
     */
    public void setObjectNameInNationalLanguage(String objectNameInNationalLanguage) {
        this.objectNameInNationalLanguage = objectNameInNationalLanguage;
    }

    /**
     * Gets object name.
     *
     * @return the object name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Sets object name.
     *
     * @param objectName the object name
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
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

    /**
     * Gets topmark daymark shape.
     *
     * @return the topmark daymark shape
     */
    public String getTopmarkDaymarkShape() {
        return topmarkDaymarkShape;
    }

    /**
     * Sets topmark daymark shape.
     *
     * @param topmarkDaymarkShape the topmark daymark shape
     */
    public void setTopmarkDaymarkShape(String topmarkDaymarkShape) {
        this.topmarkDaymarkShape = topmarkDaymarkShape;
    }
}
