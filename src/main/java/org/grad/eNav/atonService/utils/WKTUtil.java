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

package org.grad.eNav.atonService.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * The WKTUtil class.
 *
 * A helper utility that manipulates the WKT geometry strings.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class WKTUtil {

    /**
     * Converts a WKT geometry into a JTS geometry
     *
     * @param geometryAsWKT The geometry in WKT format
     * @return a JTS geometry
     * @throws ParseException if the WKT geometry was invalid
     */
    public static Geometry convertWKTtoGeometry(String geometryAsWKT) throws ParseException {
        WKTReader wktReader = new WKTReader();
        Geometry geometry = wktReader.read(geometryAsWKT);
        return geometry;
    }

    /**
     * Converts a WKT geometry into GeoJson format, via JTS geometry
     *
     * @param geometryAsWKT The geometry in WKT format
     * @return JsonNode with the geometry expressed in GeoJson format
     * @throws ParseException if the WKT geometry was invalid
     */
    public static JsonNode convertWKTtoGeoJson(String geometryAsWKT) throws ParseException {
        return GeometryJSONConverter.convertFromGeometry(WKTUtil.convertWKTtoGeometry(geometryAsWKT));
    }

}
