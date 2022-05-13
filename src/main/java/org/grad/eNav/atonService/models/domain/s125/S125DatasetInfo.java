/*
 * Copyright (c) 2022 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.grad.eNav.atonService.models.domain.s125;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The S-125 Dataset Info Class.
 * <p>
 * This class contains the S-125 Dataset Identification Information so that it
 * can be stored and manipulated easily.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125DatasetInfo {

    //Class Variables
    private String datasetId;
    private String agency;
    private String encodingSpecification;
    private String encodingSpecificationEdition;
    private String productionIdentifier;
    private String productionEdition;
    private String fileIdentifier;
    private String title;
    private String language;
    private String abstractText;

    /**
     * Constructor with the default values.
     *
     * @param datasetId the S-125 Dataset ID
     * @param atons     the S-125 Dataset AtoN nodes
     */
    public S125DatasetInfo(String datasetId, String agency, List<AidsToNavigation> atons) {
        this.datasetId = datasetId;
        this.agency = agency;
        this.encodingSpecification = "S100 Part 10b";
        this.encodingSpecificationEdition = "1.0";
        this.productionIdentifier = "S-125";
        this.productionEdition = "0.0.1";
        this.fileIdentifier = String.format("S-125_%s_%s", this.agency, this.datasetId);
        this.title = "Niord S-125 Dataset";
        this.language = "en";
        this.abstractText = "Autogenerated S-125 Dataset for" + Optional.ofNullable(atons)
                .orElse(Collections.emptyList())
                .stream()
                .map(AidsToNavigation::getAtonNumber)
                .map(uid -> String.format(" %s", uid))
                .collect(Collectors.joining());

    }

    /**
     * Gets dataset id.
     *
     * @return the dataset id
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * Sets dataset id.
     *
     * @param datasetId the dataset id
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets agency.
     *
     * @return the agency
     */
    public String getAgency() {
        return agency;
    }

    /**
     * Sets agency.
     *
     * @param agency the agency
     */
    public void setAgency(String agency) {
        this.agency = agency;
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
     * Gets production identifier.
     *
     * @return the production identifier
     */
    public String getProductionIdentifier() {
        return productionIdentifier;
    }

    /**
     * Sets production identifier.
     *
     * @param productionIdentifier the production identifier
     */
    public void setProductionIdentifier(String productionIdentifier) {
        this.productionIdentifier = productionIdentifier;
    }

    /**
     * Gets production edition.
     *
     * @return the production edition
     */
    public String getProductionEdition() {
        return productionEdition;
    }

    /**
     * Sets production edition.
     *
     * @param productionEdition the production edition
     */
    public void setProductionEdition(String productionEdition) {
        this.productionEdition = productionEdition;
    }

    /**
     * Gets file identifier.
     *
     * @return the file identifier
     */
    public String getFileIdentifier() {
        return fileIdentifier;
    }

    /**
     * Sets file identifier.
     *
     * @param fileIdentifier the file identifier
     */
    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets language.
     *
     * @param language the language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets abstract text.
     *
     * @return the abstract text
     */
    public String getAbstractText() {
        return abstractText;
    }

    /**
     * Sets abstract text.
     *
     * @param abstractText the abstract text
     */
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

}
