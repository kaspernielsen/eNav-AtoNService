/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.models.domain.s125;

import _int.iala_aism.s125.gml._0_0.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

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
    CARDINAL_BEACON("Cardinal Beacon", BeaconCardinalType.class,  BeaconCardinal.class),
    LATERAL_BEACON("Lateral Beacon", BeaconLateralType.class, BeaconLateral.class),
    ISOLATED_DANGER_BEACON("Isolated Danger Beacon", BeaconIsolatedDangerType.class, BeaconIsolatedDanger.class),
    SAFE_WATER_BEACON("Safe Water Beacon", BeaconSafeWaterType.class, BeaconSafeWater.class),
    SPECIAL_PURPOSE_BEACON("Special Purpose Beacon", BeaconSpecialPurposeGeneralType.class, BeaconSpecialPurpose.class),
    CARDINAL_BUOY("Cardinal Buoy", BuoyCardinalType.class, BuoyCardinal.class),
    LATERAL_BUOY("Lateral Buoy", BuoyLateralType.class, BuoyLateral.class),
    INSTALLATION_BUOY("Installation Buoy", BuoyInstallationType.class, BuoyInstallation.class),
    ISOLATED_DANGER_BUOY("Isolated Danger Buoy", BuoyIsolatedDangerType.class, BuoyIsolatedDanger.class),
    SAFE_WATER_BUOY("Safe Water Buoy", BuoySafeWaterType.class, BuoySafeWater.class),
    SPECIAL_PURPOSE_BUOY("Special Purpose Beacon", BuoySpecialPurposeGeneralType.class, BuoySpecialPurpose.class),
    DAYMARK("Daymark", DaymarkType.class, Daymark.class),
    FOG_SIGNAL("Fog Signal", FogSignalType.class, FogSignal.class),
    LIGHT("Light", LightType.class, Light.class),
    LIGHT_FLOAT("Light Float", LightFloatType.class, LightFloat.class),
    LANDMARK("Cardinal Beacon", LandmarkType.class, Landmark.class),
    LIGHTHOUSE("Lighthouse", LighthouseType.class, Lighthouse.class),
    LIGHT_VESSEL("Light Vessel", LightVesselType.class, LightVessel.class),
    NAVIGATION_LINE("Navigation Line", NavigationLineType.class, NavigationLine.class),
    OFFSHORE_PLATFORM("Offshore Platform", OffshorePlatformType.class, OffshorePlatform.class),
    PHYSICAL_AIS_ATON("Physical AIS AtoN", PhysicalAISAidToNavigationType.class, PhysicalAISAidToNavigation.class),
    PILE("Pile", PileType.class, Pile.class),
    RADAR_REFLECTOR("Radar Reflector", RadarReflectorType.class, RadarReflector.class),
    RADAR_TRANSPONDER_BEACON("Radar Transponder Beacon", RadarTransponderBeaconType.class, RadarTransponderBeacon.class),
    RADIO_STATION("Radio Station", RadioStationType.class, RadioStation.class),
    RECOMMENDED_TRACK("Recommended Track", RecommendedTrackType.class, RecommendedTrack.class),
    RETRO_REFLECTOR("Retro Reflector", RetroReflectorType.class, RetroReflector.class),
    SILO_TANK("Silo Tank", SiloTankType.class, SiloTank.class),
    SYNTHETIC_AIS_ATON("Virtual AtoN", SyntheticAISAidToNavigationType.class, SyntheticAISAidToNavigation.class),
    TOPMARK("Topmark", TopmarkType.class, Topmark.class),
    VIRTUAL_AIS_ATON("Virtual AtoN", VirtualAISAidToNavigationType.class, VirtualAISAidToNavigation.class),
    UNKNOWN("Unknown", AidsToNavigationType.class, AidsToNavigation.class);

    // Enum Variables
    final Class<? extends AidsToNavigationType> s125Class;
    final Class<? extends AidsToNavigation> localClass;
    final String description;

    /**
     * The S-125 AtoN Types Enum Constructor.
     *
     * @param description   The description of the AtoN type
     * @param s125Class     The S-125 Class to be mapped
     * @param localClass    The respective local persistence class
     */
    S125AtonTypes(String description, Class<? extends AidsToNavigationType> s125Class, Class<? extends AidsToNavigation> localClass) {
        this.description = description;
        this.s125Class = s125Class;
        this.localClass = localClass;
    }

    /**
     * Gets s 125 class.
     *
     * @return the s 125 class
     */
    public Class<? extends AidsToNavigationType> getS125Class() {
        return s125Class;
    }

    /**
     * Gets local equipment class.
     *
     * @return the local equipment class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends EquipmentType> getS125EquipmentClass() {
        return (Class<? extends EquipmentType>) Optional.of(this)
                .filter(S125AtonTypes::isEquipment)
                .map(S125AtonTypes::getS125Class)
                .orElse(null);
    }

    /**
     * Gets local structure class.
     *
     * @return the local structure class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends StructureObjectType> getS125StructureClass() {
        return (Class<? extends StructureObjectType>) Optional.of(this)
                .filter(S125AtonTypes::isStructure)
                .map(S125AtonTypes::getS125Class)
                .orElse(null);
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
     * Gets local equipment class.
     *
     * @return the local equipment class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Equipment> getLocalEquipmentClass() {
        return (Class<? extends Equipment>) Optional.of(this)
                .filter(S125AtonTypes::isEquipment)
                .map(S125AtonTypes::getLocalClass)
                .orElse(null);
    }

    /**
     * Gets local structure class.
     *
     * @return the local structure class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends StructureObject> getLocalStructureClass() {
        return (Class<? extends StructureObject>) Optional.of(this)
                .filter(S125AtonTypes::isStructure)
                .map(S125AtonTypes::getLocalClass)
                .orElse(null);
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
     * Is structure boolean.
     *
     * @return the boolean
     */
    public boolean isStructure() {
        return StructureObjectType.class.isAssignableFrom(this.s125Class);
    }

    /**
     * Is equipment boolean.
     *
     * @return the boolean
     */
    public boolean isEquipment() {
        return EquipmentType.class.isAssignableFrom(this.s125Class);
    }

    /**
     * Find the enum entry that corresponds to the provided S-125 class type.
     *
     * @param s125Class     The S-125 class type
     * @return The respective S-125 AtoN Type enum entry
     */
    public static <T extends AidsToNavigationType> S125AtonTypes fromS125Class(Class<T> s125Class) {
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
