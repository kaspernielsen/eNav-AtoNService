/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.atonService.utils.GeometryBinder;
import org.grad.eNav.atonService.utils.S100Utils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The AtoN Message Class
 * <p>
 * This class defines the database structure of the AtoN Message entries. These
 * are any types of S-100/S-200 IALA product specification objects that are
 * attributed to an AtoN at the current time. Most likely these are S125 message
 * as per the current IHO specifications.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Entity
@Table(name = "aton_message")
@Cacheable
@Indexed
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AtonMessage {

    private static final long serialVersionUID = 1L;

    @Id
    @ScaledNumberField(name = "id_sort", decimalScale=0, sortable = Sortable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aton_message_generator")
    @SequenceGenerator(name="aton_message_generator", sequenceName = "aton_message_seq", allocationSize=1)
    private BigInteger id;

    @NotNull
    @KeywordField(sortable = Sortable.YES)
    @Column(name = "uid", unique = true)
    private String uid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @KeywordField(normalizer = "lowercase", sortable = Sortable.YES)
    @Column(name = "type", columnDefinition = "varchar(30) default 'S125'")
    private AtonMessageType type;

    @NotNull
    @NonStandardField(valueBinder = @ValueBinderRef(type = GeometryBinder.class))
    @Column(name = "geometry")
    private Geometry geometry;

    @NotNull
    @Type(type="text")
    @FullTextField()
    @KeywordField(name = "message_sort", normalizer = "lowercase", sortable = Sortable.YES)
    @Column(name = "message")
    private String message;

    /**
     * Instantiates a new AtoN Message.
     */
    public AtonMessage() {
        // Empty constructor
    }

    /**
     * Instantiates a new AtoN message.
     *
     * @param uid      the uid
     * @param type     the type
     * @param geometry the geometry
     * @param message  the message
     */
    public AtonMessage(String uid, AtonMessageType type, Geometry geometry, String message) {
        this.uid = uid;
        this.type = type;
        this.geometry = geometry;
        this.message = message;
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
     * Gets uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public AtonMessageType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(AtonMessageType type) {
        this.type = type;
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
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Overrides the equality operator of the class.
     *
     * @param o the object to check the equality
     * @return whether the two objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AtonMessage)) return false;
        AtonMessage AtonMessage = (AtonMessage) o;
        return Objects.equals(id, AtonMessage.id);
    }

    /**
     * Overrides the hashcode generation of the object.
     *
     * @return the generated hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
