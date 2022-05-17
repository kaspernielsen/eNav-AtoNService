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

package org.grad.eNav.atonService.models.dtos.s125;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.grad.eNav.atonService.utils.GeometryJSONDeserializer;
import org.grad.eNav.atonService.utils.GeometryJSONSerializer;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.locationtech.jts.geom.Geometry;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

/**
 * The S-125 Aids to Navigation DTO Entity Class.
 * <p>
 * This is the basic class for transmitting the S-125 Aids to Navigation data
 * onto third parties. This is going to be encoded as a JSON object and it
 * does not contain AtoN type specific information, just the basics for
 * identifying an AtoN, so the commin fields.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see org.grad.eNav.atonService.models.domain.s125.AidsToNavigation
 */
public class AidsToNavigationDto {

    // Class Variables
    private BigInteger id;

    private String atonNumber;

    private String idCode;

    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateEnd;

    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateStart;

    private LocalDate periodEnd;

    private LocalDate periodStart;

    private List<String> informations;

    private List<String> informationInNationalLanguages;

    private String textualDescription;

    private String textualDescriptionInNationalLanguage;

    private List<String> seasonalActionRequireds;

    private BigInteger scaleMinimum;

    private String pictorialRepresentation;

    @JsonSerialize(using = GeometryJSONSerializer.class)
    @JsonDeserialize(using = GeometryJSONDeserializer.class)
    private Geometry geometry;

    private String atonType;
    private String content;

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
     * Gets aton number.
     *
     * @return the aton number
     */
    public String getAtonNumber() {
        return atonNumber;
    }

    /**
     * Sets aton number.
     *
     * @param atonNumber the aton number
     */
    public void setAtonNumber(String atonNumber) {
        this.atonNumber = atonNumber;
    }

    /**
     * Gets id code.
     *
     * @return the id code
     */
    public String getIdCode() {
        return idCode;
    }

    /**
     * Sets id code.
     *
     * @param idCode the id code
     */
    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    /**
     * Gets date end.
     *
     * @return the date end
     */
    public LocalDate getDateEnd() {
        return dateEnd;
    }

    /**
     * Sets date end.
     *
     * @param dateEnd the date end
     */
    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * Gets date start.
     *
     * @return the date start
     */
    public LocalDate getDateStart() {
        return dateStart;
    }

    /**
     * Sets date start.
     *
     * @param dateStart the date start
     */
    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * Gets period end.
     *
     * @return the period end
     */
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Sets period end.
     *
     * @param periodEnd the period end
     */
    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    /**
     * Gets period start.
     *
     * @return the period start
     */
    public LocalDate getPeriodStart() {
        return periodStart;
    }

    /**
     * Sets period start.
     *
     * @param periodStart the period start
     */
    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    /**
     * Gets informations.
     *
     * @return the informations
     */
    public List<String> getInformations() {
        return informations;
    }

    /**
     * Sets informations.
     *
     * @param informations the informations
     */
    public void setInformations(List<String> informations) {
        this.informations = informations;
    }

    /**
     * Gets information in national languages.
     *
     * @return the information in national languages
     */
    public List<String> getInformationInNationalLanguages() {
        return informationInNationalLanguages;
    }

    /**
     * Sets information in national languages.
     *
     * @param informationInNationalLanguages the information in national languages
     */
    public void setInformationInNationalLanguages(List<String> informationInNationalLanguages) {
        this.informationInNationalLanguages = informationInNationalLanguages;
    }

    /**
     * Gets textual description.
     *
     * @return the textual description
     */
    public String getTextualDescription() {
        return textualDescription;
    }

    /**
     * Sets textual description.
     *
     * @param textualDescription the textual description
     */
    public void setTextualDescription(String textualDescription) {
        this.textualDescription = textualDescription;
    }

    /**
     * Gets textual description in national language.
     *
     * @return the textual description in national language
     */
    public String getTextualDescriptionInNationalLanguage() {
        return textualDescriptionInNationalLanguage;
    }

    /**
     * Sets textual description in national language.
     *
     * @param textualDescriptionInNationalLanguage the textual description in national language
     */
    public void setTextualDescriptionInNationalLanguage(String textualDescriptionInNationalLanguage) {
        this.textualDescriptionInNationalLanguage = textualDescriptionInNationalLanguage;
    }

    /**
     * Gets seasonal action requireds.
     *
     * @return the seasonal action requireds
     */
    public List<String> getSeasonalActionRequireds() {
        return seasonalActionRequireds;
    }

    /**
     * Sets seasonal action requireds.
     *
     * @param seasonalActionRequireds the seasonal action requireds
     */
    public void setSeasonalActionRequireds(List<String> seasonalActionRequireds) {
        this.seasonalActionRequireds = seasonalActionRequireds;
    }

    /**
     * Gets scale minimum.
     *
     * @return the scale minimum
     */
    public BigInteger getScaleMinimum() {
        return scaleMinimum;
    }

    /**
     * Sets scale minimum.
     *
     * @param scaleMinimum the scale minimum
     */
    public void setScaleMinimum(BigInteger scaleMinimum) {
        this.scaleMinimum = scaleMinimum;
    }

    /**
     * Gets pictorial representation.
     *
     * @return the pictorial representation
     */
    public String getPictorialRepresentation() {
        return pictorialRepresentation;
    }

    /**
     * Sets pictorial representation.
     *
     * @param pictorialRepresentation the pictorial representation
     */
    public void setPictorialRepresentation(String pictorialRepresentation) {
        this.pictorialRepresentation = pictorialRepresentation;
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
     * Gets aton type.
     *
     * @return the aton type
     */
    public String getAtonType() {
        return atonType;
    }

    /**
     * Sets aton type.
     *
     * @param atonType the aton type
     */
    public void setAtonType(String atonType) {
        this.atonType = atonType;
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
}
