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

import _int.iala_aism.s125.gml._0_0.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * The S-125 Mapping enum.
 *
 * This enum is used as a map between the S-125 Aids to Navigation objects
 * and the local classes used for persistence. By mapping the classes in this
 * way, we can limit the amount of use required to manipulate the parsed XML
 * instance entries.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum S125AtonTypes {
    CARDINAL_BEACON("Cardinal Beacon", S125BeaconCardinalType.class,  BeaconCardinal.class, false),
    LATERAL_BEACON("Lateral Beacon", S125BeaconLateralType.class, BeaconLateral.class, false),
    ISOLATED_DANGER_BEACON("Isolated Danger Beacon", S125BeaconIsolatedDangerType.class, BeaconIsolatedDanger.class, false),
    SAFE_WATER_BEACON("Safe Water Beacon", S125BeaconSafeWaterType.class, BeaconSafeWater.class, false),
    SPECIAL_PURPOSE_BEACON("Special Purpose Beacon", S125BeaconSpecialPurposeGeneralType.class, BeaconSpecialPurpose.class, false),
    CARDINAL_BUOY("Cardinal Buoy", S125BuoyCardinalType.class, BuoyCardinal.class, false),
    LATERAL_BUOY("Lateral Buoy", S125BuoyLateralType.class, BuoyLateral.class, false),
    INSTALLATION_BUOY("Installation Buoy", S125BuoyInstallationType.class, BuoyInstallation.class, false),
    ISOLATED_DANGER_BUOY("Isolated Danger Buoy", S125BuoyIsolatedDangerType.class, BuoyIsolatedDanger.class, false),
    SAFE_WATER_BUOY("Safe Water Buoy", S125BuoySafeWaterType.class, BuoySafeWater.class, false),
    SPECIAL_PURPOSE_BUOY("Special Purpose Beacon", S125BuoySpecialPurposeGeneralType.class, BuoySpecialPurpose.class, false),
    DAYMARK("Daymark", S125DaymarkType.class, Daymark.class, false),
    FOG_SIGNAL("Fog Signal", S125FogSignalType.class, FogSignal.class, true),
    LIGHT("Light", S125LightType.class, Light.class, true),
    LIGHT_FLOAT("Light Float", S125LightFloatType.class, LightFloat.class, false),
    LANDMARK("Cardinal Beacon", S125LandmarkType.class, Landmark.class, false),
    LIGHTHOUSE("Lighthouse", S125LighthouseType.class, Lighthouse.class, false),
    LIGHT_VESSEL("Light Vessel", S125LightVesselType.class, LightVessel.class, false),
    NAVIGATION_LINE("Navigation Line", S125NavigationLineType.class, NavigationLine.class, false),
    OFFSHORE_PLATFORM("Offshore Platform", S125OffshorePlatformType.class, OffshorePlatform.class, false),
    PHYSICAL_AIS_ATON("Physical AIS AtoN", S125PhysicalAISAidToNavigationType.class, PhysicalAISAidToNavigation.class, true),
    PILE("Pile", S125PileType.class, Pile.class, false),
    RADAR_REFLECTOR("Radar Reflector", S125RadarReflectorType.class, RadarReflector.class, true),
    RADIO_STATION("Radio Station", S125RadioStationType.class, RadioStation.class, true),
    RECOMMENDED_TRACK("Recommended Track", S125RecommendedTrackType.class, RecommendedTrack.class, false),
    RETRO_REFLECTOR("Retro Reflector", S125RetroReflectorType.class, RetroReflector.class, true),
    SILO_TANK("Silo Tank", S125SiloTankType.class, SiloTank.class, true),
    SYNTHETIC_AIS_ATON("Virtual AtoN", S125SyntheticAISAidToNavigationType.class, SyntheticAISAidToNavigation.class, true),
    TOPMARK("Topmark", S125TopmarkType.class, Topmark.class, true),
    VIRTUAL_AIS_ATON("Virtual AtoN", S125VirtualAISAidToNavigationType.class, VirtualAISAidToNavigation.class, true),
    UNKNOWN("Unknown", S125AidsToNavigationType.class, AidsToNavigation.class, false);

    // Enum Variables
    final Class<? extends S125AidsToNavigationType> s125Class;
    final Class<? extends AidsToNavigation> localClass;
    final String description;
    final boolean equipment;

    /**
     * The S-125 AtoN Types Enum Constructor.
     *
     * @param description   The description of the AtoN type
     * @param s125Class     The S-125 Class to be mapped
     * @param localClass    The respective local persistence class
     * @param equipment     Whether this is an equipment entry or not
     */
    S125AtonTypes(String description, Class<? extends S125AidsToNavigationType> s125Class, Class<? extends AidsToNavigation> localClass, boolean equipment) {
        this.description = description;
        this.s125Class = s125Class;
        this.localClass = localClass;
        this.equipment = equipment;
    }

    /**
     * Gets s 125 class.
     *
     * @return the s 125 class
     */
    public Class<? extends S125AidsToNavigationType> getS125Class() {
        return s125Class;
    }

    /**
     * Gets local class.
     *
     * @return the local class
     */
    public Class<? extends AidsToNavigation> getLocalClass() {
        return localClass;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Is equipment boolean.
     *
     * @return the boolean
     */
    public boolean isEquipment() {
        return equipment;
    }

    /**
     * Find the enum entry that corresponds to the provided S-125 class type.
     *
     * @param s125Class     The S-125 class type
     * @return The respective S-125 AtoN Type enum entry
     */
    public static <T extends S125AidsToNavigationType> S125AtonTypes fromS125Class(Class<T> s125Class) {
        return Arrays.stream(S125AtonTypes.values())
                .filter(t -> t.getS125Class().equals(s125Class))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * Find the enum entry that corresponds to the provided local persistence
     * class type.
     *
     * @param localClass    The local persistence class type
     * @return The respective S-125 AtoN Type enum entry
     */
    public static <T extends AidsToNavigation> S125AtonTypes fromLocalClass(Class<T> localClass) {
        return Arrays.stream(S125AtonTypes.values())
                .filter(t -> t.getLocalClass().equals(localClass))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * Returns the type of the geometry that the S-125 type supports.
     *
     * @return the type of the geometry that the S-125 type supports
     * @throws NoSuchFieldException
     */
    public Field getS125GeometryField() throws NoSuchFieldException {
        Class current = this.getS125Class();
        // Look for the geometry field and iterate to the super class if necessary
        while(!Arrays.stream(current.getDeclaredFields()).map(Field::getName).anyMatch("geometry"::equals) && current.getSuperclass() != null) {
            current = current.getSuperclass();
        }
        // Once found (or not) return the type of the geometry
        return current.getDeclaredField("geometry");
    }
}
