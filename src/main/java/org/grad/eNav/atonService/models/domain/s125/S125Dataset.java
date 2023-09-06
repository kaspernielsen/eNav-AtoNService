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

package org.grad.eNav.atonService.models.domain.s125;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.grad.eNav.atonService.models.domain.DatasetContent;
import org.grad.eNav.atonService.utils.GeometryBinder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.NonStandardField;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * The S-125 Dataset Entity Class
 * <p>
 * This class is used to map the fixed datasets defined for the S-125 Aids to
 * Navigation entries. Potentially they could be autogenerated but users should
 * also be able to edit them. Datasets reference a specific geometry which will
 * then be used to map all the contained Aids to Navigation to each dataset.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Cacheable
@Indexed
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class S125Dataset {

    // Class Variables
    @Id
    @GenericField(sortable = Sortable.YES)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter (
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    @Column(columnDefinition="uuid", unique = true, updatable = false, nullable = false)
    private UUID uuid;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "dataset_identification_id", referencedColumnName = "id")
    @IndexedEmbedded(includeEmbeddedObjectId = false)
    private S125DatasetIdentification datasetIdentificationInformation;

    @NonStandardField(name="geometry", valueBinder = @ValueBinderRef(type = GeometryBinder.class))
    private Geometry geometry;

    @GenericField(sortable = Sortable.YES)
    @CreatedDate
    private LocalDateTime createdAt;

    @GenericField(sortable = Sortable.YES)
    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = true)
    @JoinTable(name = "s125_dataset_content_xref",
            joinColumns =
                    { @JoinColumn(name = "dataset_uuid", referencedColumnName = "uuid", unique = true) },
            inverseJoinColumns =
                    { @JoinColumn(name = "dataset_content_id", referencedColumnName = "id", unique = true) })
    @IndexedEmbedded(includeEmbeddedObjectId = false)
    private DatasetContent datasetContent;

    @GenericField()
    private Boolean cancelled;

    /**
     * Instantiates a new Dataset.
     */
    public S125Dataset() {
        this("auto_generated_s_125_dataset" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Instantiates a new Dataset.
     *
     * @param datasetFileIdentifier the dataset file identifier
     */
    public S125Dataset(String datasetFileIdentifier) {
        this.datasetIdentificationInformation = new S125DatasetIdentification(datasetFileIdentifier);
    }

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
    public S125DatasetIdentification getDatasetIdentificationInformation() {
        return datasetIdentificationInformation;
    }

    /**
     * Sets dataset identification information.
     *
     * @param datasetIdentificationInformation the dataset identification information
     */
    public void setDatasetIdentificationInformation(S125DatasetIdentification datasetIdentificationInformation) {
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
    public DatasetContent getDatasetContent() {
        return datasetContent;
    }

    /**
     * Sets dataset content.
     *
     * @param datasetContent the dataset content
     */
    public void setDatasetContent(DatasetContent datasetContent) {
        this.datasetContent = datasetContent;
    }

    /**
     * Gets cancelled.
     *
     * @return the cancelled
     */
    public Boolean getCancelled() {
        return cancelled;
    }

    /**
     * Sets cancelled.
     *
     * @param cancelled the cancelled
     */
    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * This helper function can assist in identifying if a dataset object
     * seems to be new. This can be indicated by two things:
     * <ul>
     *     <li>THs dataset has no UUID</li>
     *     <li>The created and modified times are the same</li>
     * </ul>
     */
    public boolean isNew() {
            return Objects.isNull(this.getUuid()) ||
                    Objects.equals(this.getCreatedAt(), this.getLastUpdatedAt());
    }

    /**
     * This helper function will copy all the important information of a
     * dataset (such as the identification information, and will assign it
     * to a branch new dataset which will be returned.
     *
     * @return the constructed dataset copy
     */
    @JsonIgnore
    public S125Dataset copy() {
        // Create the copy
        S125Dataset copy = new S125Dataset();
        copy.setDatasetIdentificationInformation(this.getDatasetIdentificationInformation().copy());
        copy.setGeometry(this.getGeometry());

        // Make sure the copy if always NOT cancelled
        copy.setCancelled(Boolean.FALSE);

        // And return it
        return copy;
    }

}
