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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;

/**
 * The S-125 Offshore Platform Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Offshore
 * Platform type. It is modelled as an entity that extends the
 * {@link StructureObject} super class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125OffshorePlatformType
 */
@Entity
public class OffshorePlatform extends StructureObject {

    // Class Variables
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125CategoryOfOffshorePlatform.class)
    private List<S125CategoryOfOffshorePlatform> categoryOfOffshorePlatforms;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Colour.class)
    private List<S125Colour> colours;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125ColourPattern.class)
    private List<S125ColourPattern> colourPatterns;

    @Enumerated(EnumType.STRING)
    private S125Condition condition;

    @Enumerated(EnumType.STRING)
    private S125RadarConspicuous radarConspicuous;

    private S125VisuallyConspicuous visuallyConspicuous;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125NatureOfConstruction.class)
    private List<S125NatureOfConstruction> natureOfConstructions;

    private String objectNameInNationalLanguage;

    private String objectName;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    private List<S125Status> statuses;

    /**
     * Gets category of offshore platforms.
     *
     * @return the category of offshore platforms
     */
    public List<S125CategoryOfOffshorePlatform> getCategoryOfOffshorePlatforms() {
        return categoryOfOffshorePlatforms;
    }

    /**
     * Sets category of offshore platforms.
     *
     * @param categoryOfOffshorePlatforms the category of offshore platforms
     */
    public void setCategoryOfOffshorePlatforms(List<S125CategoryOfOffshorePlatform> categoryOfOffshorePlatforms) {
        this.categoryOfOffshorePlatforms = categoryOfOffshorePlatforms;
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
     * Gets condition.
     *
     * @return the condition
     */
    public S125Condition getCondition() {
        return condition;
    }

    /**
     * Sets condition.
     *
     * @param condition the condition
     */
    public void setCondition(S125Condition condition) {
        this.condition = condition;
    }

    /**
     * Gets radar conspicuous.
     *
     * @return the radar conspicuous
     */
    public S125RadarConspicuous getRadarConspicuous() {
        return radarConspicuous;
    }

    /**
     * Sets radar conspicuous.
     *
     * @param radarConspicuous the radar conspicuous
     */
    public void setRadarConspicuous(S125RadarConspicuous radarConspicuous) {
        this.radarConspicuous = radarConspicuous;
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
}
