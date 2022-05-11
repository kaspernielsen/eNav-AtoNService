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

import org.grad.eNav.atonService.utils.GeometryBinder;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.NonStandardField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ScaledNumberField;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * The S-125 Aids to Navigation Entity Class.
 * <p>
 * This is the basic class for implementing the S-125-compatible Aids to
 * Navigation type. It is modelled as an entity class on hibernate, but it is
 * abstract so that we can extend this for each Aids to Navigation type.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 * @see _int.iala_aism.s125.gml._0_0.S125AidsToNavigationType
 */
@Entity
public abstract class AidsToNavigation {

    // Class Variables
    @Id
    @ScaledNumberField(name = "id_sort", decimalScale=0, sortable = Sortable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aids_to_navigation_generator")
    @SequenceGenerator(name="aids_to_navigation_generator", sequenceName = "aids_to_navigation_seq", allocationSize=1)
    private BigInteger id;

    @NotNull
    @KeywordField(sortable = Sortable.YES)
    @Column(unique=true)
    private String atonNumber;

    @NotNull
    @KeywordField(sortable = Sortable.YES)
    @Column(unique=true)
    private String idCode;

    private Date dateEnd;

    private Date dateStart;

    private Date periodEnd;

    private Date periodStart;

    @ElementCollection
    private List<String> informations;

    @ElementCollection
    private List<String> informationInNationalLanguages;

    private String textualDescription;

    private String textualDescriptionInNationalLanguage;

    @ElementCollection
    private List<String> seasonalActionRequireds;

    private BigInteger scaleMinimum;

    private String pictorialRepresentation;

    @NonStandardField(valueBinder = @ValueBinderRef(type = GeometryBinder.class))
    protected Geometry geometry;

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
    public Date getDateEnd() {
        return dateEnd;
    }

    /**
     * Sets date end.
     *
     * @param dateEnd the date end
     */
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * Gets date start.
     *
     * @return the date start
     */
    public Date getDateStart() {
        return dateStart;
    }

    /**
     * Sets date start.
     *
     * @param dateStart the date start
     */
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * Gets period end.
     *
     * @return the period end
     */
    public Date getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Sets period end.
     *
     * @param periodEnd the period end
     */
    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    /**
     * Gets period start.
     *
     * @return the period start
     */
    public Date getPeriodStart() {
        return periodStart;
    }

    /**
     * Sets period start.
     *
     * @param periodStart the period start
     */
    public void setPeriodStart(Date periodStart) {
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
}
