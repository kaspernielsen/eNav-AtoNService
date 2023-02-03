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

import _int.iala_aism.s125.gml._0_0.S125CategoryOfRecommendedTrack;
import _int.iala_aism.s125.gml._0_0.S125Status;
import _int.iala_aism.s125.gml._0_0.S125TrafficFlow;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * The S-125 Recommended Track Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Recommended
 * Tracktype. It is modelled as an entity that extends the
 * {@link AidsToNavigation} super class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125RecommendedTrackType
 */
@Entity
public class RecommendedTrack extends AidsToNavigation {

    // Class Variables
    @Enumerated(EnumType.STRING)
    protected S125CategoryOfRecommendedTrack categoryOfRecommendedTrack;

    protected String objectNameInNationalLanguage;

    protected String objectName;

    protected BigDecimal orientation;

    /**
     * The Statuses.
     */
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = S125Status.class)
    protected List<S125Status> statuses;

    /**
     * The Traffic flow.
     */
    @Enumerated(EnumType.STRING)
    protected S125TrafficFlow trafficFlow;

    /**
     * The Navigation lines.
     */
    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "recommended_track_nav_lines",
            joinColumns = { @JoinColumn(name = "recommended_track_id") },
            inverseJoinColumns = { @JoinColumn(name = "navigation_line_id") }
    )
    protected List<NavigationLine> navigationLines;

    /**
     * Gets category of recommended track.
     *
     * @return the category of recommended track
     */
    public S125CategoryOfRecommendedTrack getCategoryOfRecommendedTrack() {
        return categoryOfRecommendedTrack;
    }

    /**
     * Sets category of recommended track.
     *
     * @param categoryOfRecommendedTrack the category of recommended track
     */
    public void setCategoryOfRecommendedTrack(S125CategoryOfRecommendedTrack categoryOfRecommendedTrack) {
        this.categoryOfRecommendedTrack = categoryOfRecommendedTrack;
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
     * Gets orientation.
     *
     * @return the orientation
     */
    public BigDecimal getOrientation() {
        return orientation;
    }

    /**
     * Sets orientation.
     *
     * @param orientation the orientation
     */
    public void setOrientation(BigDecimal orientation) {
        this.orientation = orientation;
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
     * Gets traffic flow.
     *
     * @return the traffic flow
     */
    public S125TrafficFlow getTrafficFlow() {
        return trafficFlow;
    }

    /**
     * Sets traffic flow.
     *
     * @param trafficFlow the traffic flow
     */
    public void setTrafficFlow(S125TrafficFlow trafficFlow) {
        this.trafficFlow = trafficFlow;
    }

    /**
     * Gets navigation lines.
     *
     * @return the navigation lines
     */
    public List<NavigationLine> getNavigationLines() {
        return navigationLines;
    }

    /**
     * Sets navigation lines.
     *
     * @param navigationLines the navigation lines
     */
    public void setNavigationLines(List<NavigationLine> navigationLines) {
        this.navigationLines = navigationLines;
    }
}
