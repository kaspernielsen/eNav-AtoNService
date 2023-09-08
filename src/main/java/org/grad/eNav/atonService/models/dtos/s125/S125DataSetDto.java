/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.models.dtos.s125;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.utils.GeometryJSONDeserializer;
import org.grad.eNav.atonService.utils.GeometryJSONSerializer;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The S-125 Dataset DTO Class
 * <p>
 * This is the basic class for transmitting the S-125 Dataset onto third
 * parties. This is going to be encoded as a JSON object, and it does contain
 * all the fields of the locally persisted class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see S125Dataset
 */
public class S125DataSetDto {

    // Class Variables
    private UUID uuid;

    private S125DataSetIdentificationDto datasetIdentificationInformation;

    @JsonSerialize(using = GeometryJSONSerializer.class)
    @JsonDeserialize(using = GeometryJSONDeserializer.class)
    private Geometry geometry;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private DatasetContentDto datasetContent;

    private boolean cancelled;

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param uuid the uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets dataset identification information.
     *
     * @return the dataset identification information
     */
    public S125DataSetIdentificationDto getDatasetIdentificationInformation() {
        return datasetIdentificationInformation;
    }

    /**
     * Sets dataset identification information.
     *
     * @param datasetIdentificationInformation the dataset identification information
     */
    public void setDatasetIdentificationInformation(S125DataSetIdentificationDto datasetIdentificationInformation) {
        this.datasetIdentificationInformation = datasetIdentificationInformation;
    }

    /**
     * Gets geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets geometry.
     *
     * @param geometry the geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets created at.
     *
     * @param createdAt the created at
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets last updated at.
     *
     * @return the last updated at
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * Sets last updated at.
     *
     * @param lastUpdatedAt the last updated at
     */
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * Gets dataset content.
     *
     * @return the dataset content
     */
    public DatasetContentDto getDatasetContent() {
        return datasetContent;
    }

    /**
     * Sets dataset content.
     *
     * @param datasetContent the dataset content
     */
    public void setDatasetContent(DatasetContentDto datasetContent) {
        this.datasetContent = datasetContent;
    }

    /**
     * Is cancelled boolean.
     *
     * @return the boolean
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets cancelled.
     *
     * @param cancelled the cancelled
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
