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
import java.math.BigDecimal;
import java.util.List;

/**
 * The S-125 Generic Beacon Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Generic Beacon
 * type. It is modelled as an entity class on hibernate, but it is abstract so
 * that we can extend this for each Beacon type.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125GenericBeaconType
 */
@Entity
public abstract class GenericBeacon extends StructureObject {

    // Class Variables
    @Enumerated(EnumType.STRING)
    private S125BeaconShape beaconShape;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Colour.class)
    private List<S125Colour> colours;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125ColourPattern.class)
    private List<S125ColourPattern> colourPatterns;

    @Enumerated(EnumType.STRING)
    private S125RadarConspicuous radarConspicuous;

    @Enumerated(EnumType.STRING)
    private S125VisuallyConspicuous visuallyConspicuous;

    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    private S125MarksNavigationalSystemOf marksNavigationalSystemOf;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125NatureOfConstruction.class)
    private List<S125NatureOfConstruction> natureOfConstructions;

    private BigDecimal verticalLength;

    private String objectNameInNationalLanguage;

    private String objectName;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    private List<S125Status> statuses;

    /**
     * Gets beacon shape.
     *
     * @return the beacon shape
     */
    public S125BeaconShape getBeaconShape() {
        return beaconShape;
    }

    /**
     * Sets beacon shape.
     *
     * @param beaconShape the beacon shape
     */
    public void setBeaconShape(S125BeaconShape beaconShape) {
        this.beaconShape = beaconShape;
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
     * Gets vertical length.
     *
     * @return the vertical length
     */
    public BigDecimal getVerticalLength() {
        return verticalLength;
    }

    /**
     * Sets vertical length.
     *
     * @param verticalLength the vertical length
     */
    public void setVerticalLength(BigDecimal verticalLength) {
        this.verticalLength = verticalLength;
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
