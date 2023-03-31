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

import _int.iho.s100.gml.base._5_0.MDTopicCategoryCode;
import jakarta.persistence.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * The Dataset Identification Entity Class
 * <p>
 * This class contains all the dataset identification information that will
 * be used to populate the S-125 dataset identification information structure.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Cacheable
@Indexed
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class S125DataSetIdentification {

    // Class Variables
    @Id
    @ScaledNumberField(name = "id_sort", decimalScale=0, sortable = Sortable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_identification_generator")
    @SequenceGenerator(name="dataset_identification_generator", sequenceName = "dataset_identification_generator_seq", allocationSize=1)
    private BigInteger id;

    @OneToOne(mappedBy = "datasetIdentificationInformation")
    private S125DataSet s125Dataset;

    @KeywordField(sortable = Sortable.YES)
    private String encodingSpecification;

    @KeywordField(sortable = Sortable.YES)
    private String encodingSpecificationEdition;

    @KeywordField(sortable = Sortable.YES)
    private String productIdentifier;

    @KeywordField(sortable = Sortable.YES)
    private String productEdition;

    @KeywordField(sortable = Sortable.YES)
    private String applicationProfile;

    @KeywordField(sortable = Sortable.YES)
    private String datasetFileIdentifier;

    @KeywordField(sortable = Sortable.YES)
    private String datasetTitle;

    @GenericField()
    @LastModifiedDate
    private LocalDate datasetReferenceDate;

    @KeywordField(sortable = Sortable.YES)
    private String datasetLanguage;

    @FullTextField()
    private String datasetAbstract;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = MDTopicCategoryCode.class, fetch = FetchType.EAGER)
    private List<MDTopicCategoryCode> datasetTopicCategories;

    /**
     * Instantiates a new Data set identification.
     */
    public S125DataSetIdentification() {

    }

    /**
     * Instantiates a new Data set identification.
     *
     * @param datasetFileIdentifier the dataset file identifier
     */
    public S125DataSetIdentification(String datasetFileIdentifier) {
        this.encodingSpecification = "S100 Part 10b";
        this.encodingSpecificationEdition = "1.0";
        this.productIdentifier = "S-125";
        this.productEdition = "0.0.1";
        this.applicationProfile = "AtoN Service";
        this.datasetFileIdentifier = datasetFileIdentifier;
        this.datasetTitle = "GRAD e-Navigation S-125 Dataset";
        this.datasetReferenceDate = LocalDate.now();
        this.datasetLanguage = Locale.getDefault().getISO3Language();
    }

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
     * Gets encoding specification.
     *
     * @return the encoding specification
     */
    public String getEncodingSpecification() {
        return encodingSpecification;
    }

    /**
     * Sets encoding specification.
     *
     * @param encodingSpecification the encoding specification
     */
    public void setEncodingSpecification(String encodingSpecification) {
        this.encodingSpecification = encodingSpecification;
    }

    /**
     * Gets encoding specification edition.
     *
     * @return the encoding specification edition
     */
    public String getEncodingSpecificationEdition() {
        return encodingSpecificationEdition;
    }

    /**
     * Sets encoding specification edition.
     *
     * @param encodingSpecificationEdition the encoding specification edition
     */
    public void setEncodingSpecificationEdition(String encodingSpecificationEdition) {
        this.encodingSpecificationEdition = encodingSpecificationEdition;
    }

    /**
     * Gets product identifier.
     *
     * @return the product identifier
     */
    public String getProductIdentifier() {
        return productIdentifier;
    }

    /**
     * Sets product identifier.
     *
     * @param productIdentifier the product identifier
     */
    public void setProductIdentifier(String productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    /**
     * Gets product edition.
     *
     * @return the product edition
     */
    public String getProductEdition() {
        return productEdition;
    }

    /**
     * Sets product edition.
     *
     * @param productEdition the product edition
     */
    public void setProductEdition(String productEdition) {
        this.productEdition = productEdition;
    }

    /**
     * Gets application profile.
     *
     * @return the application profile
     */
    public String getApplicationProfile() {
        return applicationProfile;
    }

    /**
     * Sets application profile.
     *
     * @param applicationProfile the application profile
     */
    public void setApplicationProfile(String applicationProfile) {
        this.applicationProfile = applicationProfile;
    }

    /**
     * Gets dataset file identifier.
     *
     * @return the dataset file identifier
     */
    public String getDatasetFileIdentifier() {
        return datasetFileIdentifier;
    }

    /**
     * Sets dataset file identifier.
     *
     * @param datasetFileIdentifier the dataset file identifier
     */
    public void setDatasetFileIdentifier(String datasetFileIdentifier) {
        this.datasetFileIdentifier = datasetFileIdentifier;
    }

    /**
     * Gets dataset title.
     *
     * @return the dataset title
     */
    public String getDatasetTitle() {
        return datasetTitle;
    }

    /**
     * Sets dataset title.
     *
     * @param datasetTitle the dataset title
     */
    public void setDatasetTitle(String datasetTitle) {
        this.datasetTitle = datasetTitle;
    }

    /**
     * Gets dataset reference date.
     *
     * @return the dataset reference date
     */
    public LocalDate getDatasetReferenceDate() {
        return datasetReferenceDate;
    }

    /**
     * Sets dataset reference date.
     *
     * @param datasetReferenceDate the dataset reference date
     */
    public void setDatasetReferenceDate(LocalDate datasetReferenceDate) {
        this.datasetReferenceDate = datasetReferenceDate;
    }

    /**
     * Gets dataset language.
     *
     * @return the dataset language
     */
    public String getDatasetLanguage() {
        return datasetLanguage;
    }

    /**
     * Sets dataset language.
     *
     * @param datasetLanguage the dataset language
     */
    public void setDatasetLanguage(String datasetLanguage) {
        this.datasetLanguage = datasetLanguage;
    }

    /**
     * Gets dataset abstract.
     *
     * @return the dataset abstract
     */
    public String getDatasetAbstract() {
        return datasetAbstract;
    }

    /**
     * Sets dataset abstract.
     *
     * @param datasetAbstract the dataset abstract
     */
    public void setDatasetAbstract(String datasetAbstract) {
        this.datasetAbstract = datasetAbstract;
    }

    /**
     * Gets dataset topic categories.
     *
     * @return the dataset topic categories
     */
    public List<MDTopicCategoryCode> getDatasetTopicCategories() {
        return datasetTopicCategories;
    }

    /**
     * Sets dataset topic categories.
     *
     * @param datasetTopicCategories the dataset topic categories
     */
    public void setDatasetTopicCategories(List<MDTopicCategoryCode> datasetTopicCategories) {
        this.datasetTopicCategories = datasetTopicCategories;
    }
}
