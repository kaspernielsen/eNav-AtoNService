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

package org.grad.eNav.atonService.models.domain;

import org.grad.eNav.atonService.models.domain.s125.S125DataSet;
import org.grad.eNav.atonService.utils.GeometryBinder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.NonStandardField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ScaledNumberField;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The S-125 Dataset Entity Content Class
 * <p>
 * This class is used to log the generated S-125 dataset content into a
 * separate table that can be used for normal queries but also auditing
 * purposes. For existing datasets, this table will contain the history
 * of the dataset changes, but for non-existing ones, it will contain
 * a record of the deletion with its timestamp and last content version.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Entity
@Table(indexes = @Index(columnList = "datasetType, uuid, createdAt"))
@EntityListeners(AuditingEntityListener.class)
@Cacheable
@Indexed
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DatasetContent implements Serializable {

    // Class Variables
    @Id
    @ScaledNumberField(name = "id_sort", decimalScale=0, sortable = Sortable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_content_generator")
    @SequenceGenerator(name="dataset_content_generator", sequenceName = "dataset_content_seq", allocationSize=1)
    @Column(name = "id", nullable = false, precision = 24, scale = 0)
    private BigInteger id;

    @Enumerated(EnumType.STRING)
    private DatasetType datasetType;

    @GenericField(sortable = Sortable.YES)
    @Column(columnDefinition="uuid", updatable = false, nullable = false)
    private UUID uuid;

    @NonStandardField(name="geometry", valueBinder = @ValueBinderRef(type = GeometryBinder.class))
    private Geometry geometry;

    @GenericField()
    @CreatedDate
    private LocalDateTime createdAt;

    @OneToOne()
    @JoinTable(name = "dataset_content_xref",
            joinColumns =
                    { @JoinColumn(name = "dataset_content_id", referencedColumnName = "id", unique = true) },
            inverseJoinColumns =
                    { @JoinColumn(name = "dataset_uuid", referencedColumnName = "uuid", unique = true) })
    protected S125DataSet dataset;

    /**
     * This is actually created in Postgres as an OID field. To actually read
     * the contents of it we can run the following native query:
     * <p>
     *     select convert_from(lo_get(content), 'UTF-8') from dataset_content;
     * </p>
     */
    @Lob
    private String content;

    private BigInteger contentLength;

    /**
     * Gets id.
     *
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(BigInteger id) {
        this.id = id;
    }

    /**
     * Gets dataset type.
     *
     * @return the dataset type
     */
    public DatasetType getDatasetType() {
        return datasetType;
    }

    /**
     * Sets dataset type.
     *
     * @param datasetType the dataset type
     */
    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
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
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets content length.
     *
     * @return the content length
     */
    public BigInteger getContentLength() {
        return contentLength;
    }

    /**
     * Sets content length.
     *
     * @param contentLength the content length
     */
    public void setContentLength(BigInteger contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Gets dataset.
     *
     * @return the dataset
     */
    public S125DataSet getDataset() {
        return dataset;
    }

    /**
     * Sets dataset.
     *
     * @param dataset the dataset
     */
    public void setDataset(S125DataSet dataset) {
        this.dataset = dataset;
    }
}
