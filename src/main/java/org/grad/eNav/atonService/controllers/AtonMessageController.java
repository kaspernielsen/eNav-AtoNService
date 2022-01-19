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

package org.grad.eNav.atonService.controllers;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.components.DomainDtoMapper;
import org.grad.eNav.atonService.models.domain.AtonMessage;
import org.grad.eNav.atonService.models.dtos.S125Node;
import org.grad.eNav.atonService.models.dtos.datatables.DtPage;
import org.grad.eNav.atonService.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.atonService.services.AtonMessageService;
import org.grad.eNav.atonService.utils.GeometryJSONConverter;
import org.grad.eNav.atonService.utils.HeaderUtil;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

/**
 * REST controller for managing Station Nodes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@RequestMapping("/api/messages")
@Slf4j
public class AtonMessageController {

    /**
     * The Station Node Service.
     */
    @Autowired
    AtonMessageService atonMessageService;

    /**
     * Object Mapper from Domain to DTO.
     */
    @Autowired
    DomainDtoMapper<AtonMessage, S125Node> atonMessageToS125Mapper;

    /**
     * Setup up addition model mapper configurations.
     */
    @PostConstruct
    void setup() {
        this.atonMessageToS125Mapper.getModelMapper().createTypeMap(AtonMessage.class, S125Node.class)
                .implicitMappings()
                .addMapping(AtonMessage::getUid, S125Node::setAtonUID)
                .addMappings(mapper -> mapper
                        .using(ctx -> GeometryJSONConverter.convertFromGeometry((Geometry) ctx.getSource()))
                        .map(src-> src.getGeometry(), S125Node::setBbox))
                .addMapping(AtonMessage::getMessage, S125Node::setContent);
    }

    /**
     * GET /api/messages : Returns a paged list of all current AtoN messages.
     *
     * @param uid the AtoN message UID
     * @param geometry the geometry for AtoN message filtering
     * @param startDate the start date for AtoN message filtering
     * @param endDate the end date for AtoN message filtering
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @ResponseStatus
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<S125Node>> getMessages(@RequestParam("uid") Optional<String> uid,
                                                         @RequestParam("geometry") Optional<Geometry> geometry,
                                                         @RequestParam("startDate") Optional<Date> startDate,
                                                         @RequestParam("endDate") Optional<Date> endDate,
                                                         Pageable pageable) {
        log.debug("REST request to get page of message");
        Page<AtonMessage> nodePage = this.atonMessageService.findAll(uid, geometry, pageable);
        return ResponseEntity.ok()
                .body(this.atonMessageToS125Mapper.convertToPage(nodePage, S125Node.class));
    }

    /**
     * POST /api/messages/dt : Returns a paged list of all current AtoN messages.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<S125Node>> getMessagesForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        log.debug("REST request to get page of message for datatables");
        return ResponseEntity.ok()
                .body(this.atonMessageToS125Mapper.convertToDtPage(this.atonMessageService.handleDatatablesPagingRequest(dtPagingRequest), dtPagingRequest, S125Node.class));
    }

    /**
     * DELETE /api/messages/{id} : Delete the "id" AtoN message.
     *
     * @param id the ID of the AtoN message to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteMessage(@PathVariable BigInteger id) {
        log.debug("REST request to delete message : {}", id);
        this.atonMessageService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("node", id.toString()))
                .build();
    }

    /**
     * DELETE /api/messages/uid/{uid} : Delete the "UID" AtoN message.
     *
     * @param uid the UID of the AtoN message to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/uid/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteMessageByUid(@PathVariable String uid) {
        log.debug("REST request to delete message by UID : {}", uid);
        // First translate the UID into a station node ID
        this.atonMessageService.deleteByUid(uid);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("node", uid))
                .build();
    }

}
