--
-- PostgreSQL database dump
--

-- Dumped from database version 14.11 (Ubuntu 14.11-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.11 (Ubuntu 14.11-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: aggregation; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.aggregation (
    id numeric(38,0) NOT NULL,
    aggregation_type character varying(255),
    CONSTRAINT aggregation_aggregation_type_check CHECK (((aggregation_type)::text = ANY (ARRAY[('LEADING_LINE'::character varying)::text, ('RANGE_SYSTEM'::character varying)::text, ('MEASURED_DISTANCE'::character varying)::text, ('BUOY_MOORING'::character varying)::text])))
);


ALTER TABLE public.aggregation OWNER TO atonservice;

--
-- Name: aggregation_join_table; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.aggregation_join_table (
    aggregation_id numeric(38,0) NOT NULL,
    aton_id numeric(38,0) NOT NULL
);


ALTER TABLE public.aggregation_join_table OWNER TO atonservice;

--
-- Name: aggregation_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.aggregation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.aggregation_seq OWNER TO atonservice;

--
-- Name: aids_to_navigation; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.aids_to_navigation (
    category_of_special_purpose_mark smallint,
    date_end date,
    date_start date,
    estimated_range_of_transmission numeric(38,2),
    height numeric(38,2),
    id numeric(38,0) NOT NULL,
    mmsi_code numeric(38,0),
    multiplicity_of_lights numeric(38,0),
    orientation numeric(38,2),
    parent_id numeric(38,0),
    period_end date,
    period_start date,
    scale_minimum numeric(38,0),
    sector_limit_one numeric(38,2),
    sector_limit_two numeric(38,2),
    signal_period numeric(38,2),
    value_of_nominal_range numeric(38,2),
    vertical_length numeric(38,2),
    last_modified_at timestamp(6) without time zone,
    dtype character varying(31) NOT NULL,
    beacon_shape character varying(255),
    building_shape character varying(255),
    buoy_shape character varying(255),
    category_of_cardinal_mark character varying(255),
    category_of_fog_signal character varying(255),
    category_of_installation_buoy character varying(255),
    category_of_lateral_mark character varying(255),
    category_of_navigation_line character varying(255),
    category_of_pile character varying(255),
    category_of_radar_transponder_beacon_type character varying(255),
    category_of_radio_station character varying(255),
    category_of_recommended_track character varying(255),
    category_of_silo_tank character varying(255),
    colour character varying(255),
    condition character varying(255),
    exhibition_condition_of_light character varying(255),
    id_code character varying(255) NOT NULL,
    light_characteristic character varying(255),
    marks_navigational_system_of character varying(255),
    pictorial_representation character varying(255),
    radar_conspicuous character varying(255),
    radar_wave_length character varying(255),
    signal_group character varying(255),
    signal_sequence character varying(255),
    status character varying(255),
    topmark_daymark_shape character varying(255),
    traffic_flow character varying(255),
    virtualaisaid_to_navigation_type character varying(255),
    visual_prominence character varying(255),
    geometry public.geometry,
    CONSTRAINT aids_to_navigation_beacon_shape_check CHECK (((beacon_shape)::text = ANY (ARRAY[('STAKE_POLE_PERCH_POST'::character varying)::text, ('BEACON_TOWER'::character varying)::text, ('LATTICE_BEACON'::character varying)::text, ('PILE_BEACON'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_building_shape_check CHECK (((building_shape)::text = ANY (ARRAY[('HIGH_RISE_BUILDING'::character varying)::text, ('PYRAMID'::character varying)::text, ('CYLINDRICAL'::character varying)::text, ('SPHERICAL'::character varying)::text, ('CUBIC'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_buoy_shape_check CHECK (((buoy_shape)::text = ANY (ARRAY[('CONICAL_NUN_OGIVAL'::character varying)::text, ('CAN_CYLINDRICAL'::character varying)::text, ('SPHERICAL'::character varying)::text, ('PILLAR'::character varying)::text, ('SPAR_SPINDLE'::character varying)::text, ('BARREL_TUN'::character varying)::text, ('SUPER_BUOY'::character varying)::text, ('ICE_BUOY'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_cardinal_mark_check CHECK (((category_of_cardinal_mark)::text = ANY (ARRAY[('NORTH_CARDINAL_MARK'::character varying)::text, ('EAST_CARDINAL_MARK'::character varying)::text, ('SOUTH_CARDINAL_MARK'::character varying)::text, ('WEST_CARDINAL_MARK'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_fog_signal_check CHECK (((category_of_fog_signal)::text = ANY (ARRAY[('SIREN'::character varying)::text, ('BELL'::character varying)::text, ('WHISTLE'::character varying)::text, ('HORN'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_installation_buoy_check CHECK (((category_of_installation_buoy)::text = ANY (ARRAY[('CATENARY_ANCHOR_LEG_MOORING_CALM'::character varying)::text, ('SINGLE_BUOY_MOORING_SBM_OR_SPM'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_lateral_mark_check CHECK (((category_of_lateral_mark)::text = ANY (ARRAY[('PORT_HAND_LATERAL_MARK'::character varying)::text, ('STARBOARD_HAND_LATERAL_MARK'::character varying)::text, ('PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK'::character varying)::text, ('PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_navigation_line_check CHECK (((category_of_navigation_line)::text = ANY (ARRAY[('CLEARING_LINE'::character varying)::text, ('TRANSIT_LINE'::character varying)::text, ('LEADING_LINE_BEARING_A_RECOMMENDED_TRACK'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_pile_check CHECK (((category_of_pile)::text = ANY (ARRAY[('STAKE'::character varying)::text, ('POST'::character varying)::text, ('TRIPODAL'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_radar_transponder_beacon_t_check CHECK (((category_of_radar_transponder_beacon_type)::text = ANY (ARRAY[('RAMARK_RADAR_BEACON_TRANSMITTING_CONTINUOUSLY'::character varying)::text, ('RACON_RADAR_TRANSPONDER_BEACON'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_radio_station_check CHECK (((category_of_radio_station)::text = ANY (ARRAY[('DIFFERENTIAL_GNSS'::character varying)::text, ('AIS_BASE_STATION'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_recommended_track_check CHECK (((category_of_recommended_track)::text = ANY (ARRAY[('BASED_ON_A_SYSTEM_OF_FIXED_MARKS'::character varying)::text, ('NOT_BASED_ON_A_SYSTEM_OF_FIXED_MARKS'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_silo_tank_check CHECK (((category_of_silo_tank)::text = ANY (ARRAY[('SILO_IN_GENERAL'::character varying)::text, ('TANK_IN_GENERAL'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_category_of_special_purpose_mark_check CHECK (((category_of_special_purpose_mark >= 0) AND (category_of_special_purpose_mark <= 59))),
    CONSTRAINT aids_to_navigation_colour_check CHECK (((colour)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_condition_check CHECK (((condition)::text = ANY (ARRAY[('UNDER_CONSTRUCTION'::character varying)::text, ('RUINED'::character varying)::text, ('UNDER_RECLAMATION'::character varying)::text, ('PLANNED_CONSTRUCTION'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_exhibition_condition_of_light_check CHECK (((exhibition_condition_of_light)::text = ANY (ARRAY[('LIGHT_SHOWN_WITHOUT_CHANGE_OF_CHARACTER'::character varying)::text, ('DAYTIME_LIGHT'::character varying)::text, ('FOG_LIGHT'::character varying)::text, ('NIGHT_LIGHT'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_light_characteristic_check CHECK (((light_characteristic)::text = ANY (ARRAY[('FIXED'::character varying)::text, ('FLASHING'::character varying)::text, ('LONG_FLASHING'::character varying)::text, ('QUICK_FLASHING'::character varying)::text, ('VERY_QUICK_FLASHING'::character varying)::text, ('ULTRA_QUICK_FLASHING'::character varying)::text, ('ISOPHASED'::character varying)::text, ('OCCULTING'::character varying)::text, ('INTERRUPTED_QUICK_FLASHING'::character varying)::text, ('INTERRUPTED_VERY_QUICK_FLASHING'::character varying)::text, ('INTERRUPTED_ULTRA_QUICK_FLASHING'::character varying)::text, ('MORSE'::character varying)::text, ('FIXED_AND_FLASH'::character varying)::text, ('FLASH_AND_LONG_FLASH'::character varying)::text, ('OCCULTING_AND_FLASH'::character varying)::text, ('FIXED_AND_LONG_FLASH'::character varying)::text, ('OCCULTING_ALTERNATING'::character varying)::text, ('LONG_FLASH_ALTERNATING'::character varying)::text, ('FLASH_ALTERNATING'::character varying)::text, ('QUICK_FLASH_PLUS_LONG_FLASH'::character varying)::text, ('VERY_QUICK_FLASH_PLUS_LONG_FLASH'::character varying)::text, ('ULTRA_QUICK_FLASH_PLUS_LONG_FLASH'::character varying)::text, ('ALTERNATING'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_marks_navigational_system_of_check CHECK (((marks_navigational_system_of)::text = ANY (ARRAY[('IALA_A'::character varying)::text, ('IALA_B'::character varying)::text, ('NO_SYSTEM'::character varying)::text, ('OTHER_SYSTEM'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_radar_conspicuous_check CHECK (((radar_conspicuous)::text = ANY (ARRAY[('RADAR_CONSPICUOUS'::character varying)::text, ('NOT_RADAR_CONSPICUOUS'::character varying)::text, ('RADAR_CONSPICUOUS_HAS_RADAR_REFLECTOR'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_status_check CHECK (((status)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_traffic_flow_check CHECK (((traffic_flow)::text = ANY (ARRAY[('INBOUND'::character varying)::text, ('OUTBOUND'::character varying)::text, ('ONE_WAY'::character varying)::text, ('TWO_WAY'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_virtualaisaid_to_navigation_type_check CHECK (((virtualaisaid_to_navigation_type)::text = ANY (ARRAY[('NORTH_CARDINAL'::character varying)::text, ('EAST_CARDINAL'::character varying)::text, ('SOUTH_CARDINAL'::character varying)::text, ('WEST_CARDINAL'::character varying)::text, ('PORT_LATERAL'::character varying)::text, ('STARBOARD_LATERAL'::character varying)::text, ('PREFERRED_CHANNEL_TO_PORT'::character varying)::text, ('PREFERRED_CHANNEL_TO_STARBOARD'::character varying)::text, ('ISOLATED_DANGER'::character varying)::text, ('SAFE_WATER'::character varying)::text, ('SPECIAL_PURPOSE'::character varying)::text, ('NEW_DANGER_MARKING'::character varying)::text]))),
    CONSTRAINT aids_to_navigation_visual_prominence_check CHECK (((visual_prominence)::text = ANY (ARRAY[('VISUALLY_CONSPICUOUS'::character varying)::text, ('NOT_VISUALLY_CONSPICUOUS'::character varying)::text])))
);


ALTER TABLE public.aids_to_navigation OWNER TO atonservice;

--
-- Name: aids_to_navigation_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.aids_to_navigation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.aids_to_navigation_seq OWNER TO atonservice;

--
-- Name: association; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.association (
    id numeric(38,0) NOT NULL,
    association_type character varying(255),
    CONSTRAINT association_association_type_check CHECK (((association_type)::text = ANY (ARRAY[('CHANNEL_MARKINGS'::character varying)::text, ('DANGER_MARKINGS'::character varying)::text])))
);


ALTER TABLE public.association OWNER TO atonservice;

--
-- Name: association_join_table; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.association_join_table (
    association_id numeric(38,0) NOT NULL,
    aton_id numeric(38,0) NOT NULL
);


ALTER TABLE public.association_join_table OWNER TO atonservice;

--
-- Name: association_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.association_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.association_seq OWNER TO atonservice;

--
-- Name: beacon_special_purpose_category_of_special_purpose_marks; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.beacon_special_purpose_category_of_special_purpose_marks (
    beacon_special_purpose_id numeric(38,0) NOT NULL,
    category_of_special_purpose_marks character varying(255),
    CONSTRAINT beacon_special_purpose_categ_category_of_special_purpose__check CHECK (((category_of_special_purpose_marks)::text = ANY (ARRAY[('FIRING_DANGER_MARK'::character varying)::text, ('TARGET_MARK'::character varying)::text, ('MARKER_SHIP_MARK'::character varying)::text, ('DEGAUSSING_RANGE_MARK'::character varying)::text, ('BARGE_MARK'::character varying)::text, ('CABLE_MARK'::character varying)::text, ('SPOIL_GROUND_MARK'::character varying)::text, ('OUTFALL_MARK'::character varying)::text, ('ODAS_OCEAN_DATA_ACQUISITION_SYSTEM'::character varying)::text, ('RECORDING_MARK'::character varying)::text, ('SEAPLANE_ANCHORAGE_MARK'::character varying)::text, ('RECREATION_ZONE_MARK'::character varying)::text, ('PRIVATE_MARK'::character varying)::text, ('MOORING_MARK'::character varying)::text, ('LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY'::character varying)::text, ('LEADING_MARK'::character varying)::text, ('MEASURED_DISTANCE_MARK'::character varying)::text, ('NOTICE_MARK'::character varying)::text, ('TSS_MARK_TRAFFIC_SEPARATION_SCHEME'::character varying)::text, ('ANCHORING_PROHIBITED_MARK'::character varying)::text, ('BERTHING_PROHIBITED_MARK'::character varying)::text, ('OVERTAKING_PROHIBITED_MARK'::character varying)::text, ('TWO_WAY_TRAFFIC_PROHIBITED_MARK'::character varying)::text, ('REDUCED_WAKE_MARK'::character varying)::text, ('SPEED_LIMIT_MARK'::character varying)::text, ('STOP_MARK'::character varying)::text, ('GENERAL_WARNING_MARK'::character varying)::text, ('SOUND_SHIP_S_SIREN_MARK'::character varying)::text, ('RESTRICTED_VERTICAL_CLEARANCE_MARK'::character varying)::text, ('MAXIMUM_VESSEL_S_DRAUGHT_MARK'::character varying)::text, ('RESTRICTED_HORIZONTAL_CLEARANCE_MARK'::character varying)::text, ('STRONG_CURRENT_WARNING_MARK'::character varying)::text, ('BERTHING_PERMITTED_MARK'::character varying)::text, ('OVERHEAD_POWER_CABLE_MARK'::character varying)::text, ('CHANNEL_EDGE_GRADIENT_MARK'::character varying)::text, ('TELEPHONE_MARK'::character varying)::text, ('FERRY_CROSSING_MARK'::character varying)::text, ('PIPELINE_MARK'::character varying)::text, ('ANCHORAGE_MARK'::character varying)::text, ('CLEARING_MARK'::character varying)::text, ('CONTROL_MARK'::character varying)::text, ('DIVING_MARK'::character varying)::text, ('REFUGE_BEACON'::character varying)::text, ('FOUL_GROUND_MARK'::character varying)::text, ('YACHTING_MARK'::character varying)::text, ('HELIPORT_MARK'::character varying)::text, ('GNSS_MARK'::character varying)::text, ('SEAPLANE_LANDING_MARK'::character varying)::text, ('ENTRY_PROHIBITED_MARK'::character varying)::text, ('WORK_IN_PROGRESS_MARK'::character varying)::text, ('MARK_WITH_UNKNOWN_PURPOSE'::character varying)::text, ('WELLHEAD_MARK'::character varying)::text, ('CHANNEL_SEPARATION_MARK'::character varying)::text, ('MARINE_FARM_MARK'::character varying)::text, ('ARTIFICIAL_REEF_MARK'::character varying)::text, ('JETSKI_PROHIBITED'::character varying)::text, ('WRECK_MARK'::character varying)::text, ('FACILITY_PROTECTION_MARK'::character varying)::text, ('OIL_PIPELINE_PROTECTION_MARK'::character varying)::text, ('MARINE_CABLE_PROTECTION_MARK'::character varying)::text])))
);


ALTER TABLE public.beacon_special_purpose_category_of_special_purpose_marks OWNER TO atonservice;

--
-- Name: broadcast_by_join_table; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.broadcast_by_join_table (
    ais_aton_id numeric(38,0) NOT NULL,
    radio_station_id numeric(38,0) NOT NULL
);


ALTER TABLE public.broadcast_by_join_table OWNER TO atonservice;

--
-- Name: buoy_special_purpose_category_of_special_purpose_marks; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.buoy_special_purpose_category_of_special_purpose_marks (
    buoy_special_purpose_id numeric(38,0) NOT NULL,
    category_of_special_purpose_marks character varying(255),
    CONSTRAINT buoy_special_purpose_categor_category_of_special_purpose__check CHECK (((category_of_special_purpose_marks)::text = ANY (ARRAY[('FIRING_DANGER_MARK'::character varying)::text, ('TARGET_MARK'::character varying)::text, ('MARKER_SHIP_MARK'::character varying)::text, ('DEGAUSSING_RANGE_MARK'::character varying)::text, ('BARGE_MARK'::character varying)::text, ('CABLE_MARK'::character varying)::text, ('SPOIL_GROUND_MARK'::character varying)::text, ('OUTFALL_MARK'::character varying)::text, ('ODAS_OCEAN_DATA_ACQUISITION_SYSTEM'::character varying)::text, ('RECORDING_MARK'::character varying)::text, ('SEAPLANE_ANCHORAGE_MARK'::character varying)::text, ('RECREATION_ZONE_MARK'::character varying)::text, ('PRIVATE_MARK'::character varying)::text, ('MOORING_MARK'::character varying)::text, ('LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY'::character varying)::text, ('LEADING_MARK'::character varying)::text, ('MEASURED_DISTANCE_MARK'::character varying)::text, ('NOTICE_MARK'::character varying)::text, ('TSS_MARK_TRAFFIC_SEPARATION_SCHEME'::character varying)::text, ('ANCHORING_PROHIBITED_MARK'::character varying)::text, ('BERTHING_PROHIBITED_MARK'::character varying)::text, ('OVERTAKING_PROHIBITED_MARK'::character varying)::text, ('TWO_WAY_TRAFFIC_PROHIBITED_MARK'::character varying)::text, ('REDUCED_WAKE_MARK'::character varying)::text, ('SPEED_LIMIT_MARK'::character varying)::text, ('STOP_MARK'::character varying)::text, ('GENERAL_WARNING_MARK'::character varying)::text, ('SOUND_SHIP_S_SIREN_MARK'::character varying)::text, ('RESTRICTED_VERTICAL_CLEARANCE_MARK'::character varying)::text, ('MAXIMUM_VESSEL_S_DRAUGHT_MARK'::character varying)::text, ('RESTRICTED_HORIZONTAL_CLEARANCE_MARK'::character varying)::text, ('STRONG_CURRENT_WARNING_MARK'::character varying)::text, ('BERTHING_PERMITTED_MARK'::character varying)::text, ('OVERHEAD_POWER_CABLE_MARK'::character varying)::text, ('CHANNEL_EDGE_GRADIENT_MARK'::character varying)::text, ('TELEPHONE_MARK'::character varying)::text, ('FERRY_CROSSING_MARK'::character varying)::text, ('PIPELINE_MARK'::character varying)::text, ('ANCHORAGE_MARK'::character varying)::text, ('CLEARING_MARK'::character varying)::text, ('CONTROL_MARK'::character varying)::text, ('DIVING_MARK'::character varying)::text, ('REFUGE_BEACON'::character varying)::text, ('FOUL_GROUND_MARK'::character varying)::text, ('YACHTING_MARK'::character varying)::text, ('HELIPORT_MARK'::character varying)::text, ('GNSS_MARK'::character varying)::text, ('SEAPLANE_LANDING_MARK'::character varying)::text, ('ENTRY_PROHIBITED_MARK'::character varying)::text, ('WORK_IN_PROGRESS_MARK'::character varying)::text, ('MARK_WITH_UNKNOWN_PURPOSE'::character varying)::text, ('WELLHEAD_MARK'::character varying)::text, ('CHANNEL_SEPARATION_MARK'::character varying)::text, ('MARINE_FARM_MARK'::character varying)::text, ('ARTIFICIAL_REEF_MARK'::character varying)::text, ('JETSKI_PROHIBITED'::character varying)::text, ('WRECK_MARK'::character varying)::text, ('FACILITY_PROTECTION_MARK'::character varying)::text, ('OIL_PIPELINE_PROTECTION_MARK'::character varying)::text, ('MARINE_CABLE_PROTECTION_MARK'::character varying)::text])))
);


ALTER TABLE public.buoy_special_purpose_category_of_special_purpose_marks OWNER TO atonservice;

--
-- Name: dataset_content; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.dataset_content (
    content_length numeric(38,0),
    delta_length numeric(38,0),
    id numeric(24,0) NOT NULL,
    sequence_no numeric(38,0),
    generated_at timestamp(6) without time zone,
    content oid,
    delta oid
);


ALTER TABLE public.dataset_content OWNER TO atonservice;

--
-- Name: dataset_content_log; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.dataset_content_log (
    content_length numeric(38,0),
    delta_length numeric(38,0),
    id numeric(24,0) NOT NULL,
    sequence_no numeric(38,0),
    generated_at timestamp(6) without time zone,
    uuid uuid NOT NULL,
    dataset_type character varying(255),
    operation character varying(255),
    content oid,
    delta oid,
    geometry public.geometry,
    CONSTRAINT dataset_content_log_dataset_type_check CHECK (((dataset_type)::text = 'S125'::text)),
    CONSTRAINT dataset_content_log_operation_check CHECK (((operation)::text = ANY (ARRAY[('CREATED'::character varying)::text, ('UPDATED'::character varying)::text, ('CANCELLED'::character varying)::text, ('DELETED'::character varying)::text, ('OTHER'::character varying)::text, ('AUTO'::character varying)::text])))
);


ALTER TABLE public.dataset_content_log OWNER TO atonservice;

--
-- Name: dataset_content_log_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.dataset_content_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_content_log_seq OWNER TO atonservice;

--
-- Name: dataset_content_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.dataset_content_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_content_seq OWNER TO atonservice;

--
-- Name: dataset_identification_generator_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.dataset_identification_generator_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_identification_generator_seq OWNER TO atonservice;

--
-- Name: daymark_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.daymark_colour_patterns (
    daymark_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT daymark_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.daymark_colour_patterns OWNER TO atonservice;

--
-- Name: daymark_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.daymark_colours (
    daymark_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT daymark_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.daymark_colours OWNER TO atonservice;

--
-- Name: daymark_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.daymark_nature_of_constructions (
    daymark_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT daymark_nature_of_constructions_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.daymark_nature_of_constructions OWNER TO atonservice;

--
-- Name: daymark_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.daymark_statuses (
    daymark_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT daymark_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.daymark_statuses OWNER TO atonservice;

--
-- Name: feature_name; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.feature_name (
    display_name boolean,
    feature_id numeric(38,0),
    id numeric(38,0) NOT NULL,
    language character varying(255),
    name character varying(255)
);


ALTER TABLE public.feature_name OWNER TO atonservice;

--
-- Name: feature_name_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.feature_name_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.feature_name_seq OWNER TO atonservice;

--
-- Name: fog_signal_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.fog_signal_statuses (
    fog_signal_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT fog_signal_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.fog_signal_statuses OWNER TO atonservice;

--
-- Name: generic_beacon_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_beacon_colour_patterns (
    generic_beacon_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT generic_beacon_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.generic_beacon_colour_patterns OWNER TO atonservice;

--
-- Name: generic_beacon_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_beacon_colours (
    generic_beacon_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT generic_beacon_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.generic_beacon_colours OWNER TO atonservice;

--
-- Name: generic_beacon_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_beacon_nature_of_constructions (
    generic_beacon_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT generic_beacon_nature_of_construc_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.generic_beacon_nature_of_constructions OWNER TO atonservice;

--
-- Name: generic_beacon_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_beacon_statuses (
    generic_beacon_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT generic_beacon_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.generic_beacon_statuses OWNER TO atonservice;

--
-- Name: generic_buoy_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_buoy_colour_patterns (
    generic_buoy_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT generic_buoy_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.generic_buoy_colour_patterns OWNER TO atonservice;

--
-- Name: generic_buoy_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_buoy_colours (
    generic_buoy_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT generic_buoy_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.generic_buoy_colours OWNER TO atonservice;

--
-- Name: generic_buoy_nature_ofconstuctions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_buoy_nature_ofconstuctions (
    generic_buoy_id numeric(38,0) NOT NULL,
    nature_ofconstuctions character varying(255),
    CONSTRAINT generic_buoy_nature_ofconstuctions_nature_ofconstuctions_check CHECK (((nature_ofconstuctions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.generic_buoy_nature_ofconstuctions OWNER TO atonservice;

--
-- Name: generic_buoy_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.generic_buoy_statuses (
    generic_buoy_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT generic_buoy_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.generic_buoy_statuses OWNER TO atonservice;

--
-- Name: information; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.information (
    feature_id numeric(38,0),
    id numeric(38,0) NOT NULL,
    file_locator character varying(255),
    file_reference character varying(255),
    headline character varying(255),
    language character varying(255),
    text character varying(255)
);


ALTER TABLE public.information OWNER TO atonservice;

--
-- Name: information_seq; Type: SEQUENCE; Schema: public; Owner: atonservice
--

CREATE SEQUENCE public.information_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.information_seq OWNER TO atonservice;

--
-- Name: landmark_category_of_landmarks; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_category_of_landmarks (
    landmark_id numeric(38,0) NOT NULL,
    category_of_landmarks character varying(255),
    CONSTRAINT landmark_category_of_landmarks_category_of_landmarks_check CHECK (((category_of_landmarks)::text = ANY (ARRAY[('CHIMNEY'::character varying)::text, ('MAST'::character varying)::text, ('MONUMENT'::character varying)::text, ('DOME'::character varying)::text, ('RADAR_SCANNER'::character varying)::text, ('TOWER'::character varying)::text, ('WINDMOTOR'::character varying)::text])))
);


ALTER TABLE public.landmark_category_of_landmarks OWNER TO atonservice;

--
-- Name: landmark_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_colour_patterns (
    landmark_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT landmark_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.landmark_colour_patterns OWNER TO atonservice;

--
-- Name: landmark_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_colours (
    landmark_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT landmark_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.landmark_colours OWNER TO atonservice;

--
-- Name: landmark_functions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_functions (
    landmark_id numeric(38,0) NOT NULL,
    functions character varying(255),
    CONSTRAINT landmark_functions_functions_check CHECK (((functions)::text = ANY (ARRAY[('CUSTOMS_OFFICE'::character varying)::text, ('HOSPITAL'::character varying)::text, ('POST_OFFICE'::character varying)::text, ('HOTEL'::character varying)::text, ('RAILWAY_STATION'::character varying)::text, ('POLICE_STATION'::character varying)::text, ('WATER_POLICE_STATION'::character varying)::text, ('BANK_OFFICE'::character varying)::text, ('POWER_STATION'::character varying)::text, ('EDUCATIONAL_FACILITY'::character varying)::text, ('CHURCH'::character varying)::text, ('TEMPLE'::character varying)::text, ('TELEVISION'::character varying)::text, ('RADIO'::character varying)::text, ('RADAR'::character varying)::text, ('LIGHT_SUPPORT'::character varying)::text, ('BUS_STATION'::character varying)::text])))
);


ALTER TABLE public.landmark_functions OWNER TO atonservice;

--
-- Name: landmark_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_nature_of_constructions (
    landmark_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT landmark_nature_of_constructions_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.landmark_nature_of_constructions OWNER TO atonservice;

--
-- Name: landmark_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.landmark_statuses (
    landmark_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT landmark_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.landmark_statuses OWNER TO atonservice;

--
-- Name: light_category_of_lights; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_category_of_lights (
    light_id numeric(38,0) NOT NULL,
    category_of_lights character varying(255),
    CONSTRAINT light_category_of_lights_category_of_lights_check CHECK (((category_of_lights)::text = ANY (ARRAY[('LEADING_LIGHT'::character varying)::text, ('AERO_LIGHT'::character varying)::text, ('AIR_OBSTRUCTION_LIGHT'::character varying)::text, ('FOG_DETECTOR_LIGHT'::character varying)::text, ('FLOOD_LIGHT'::character varying)::text, ('STRIP_LIGHT'::character varying)::text, ('SUBSIDIARY_LIGHT'::character varying)::text, ('SPOTLIGHT'::character varying)::text, ('FRONT'::character varying)::text, ('REAR'::character varying)::text, ('LOWER'::character varying)::text, ('UPPER'::character varying)::text, ('EMERGENCY'::character varying)::text, ('HORIZONTALLY_DISPOSED'::character varying)::text, ('VERTICALLY_DISPOSED'::character varying)::text, ('BRIDGE_LIGHT'::character varying)::text])))
);


ALTER TABLE public.light_category_of_lights OWNER TO atonservice;

--
-- Name: light_float_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_float_colour_patterns (
    light_float_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT light_float_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.light_float_colour_patterns OWNER TO atonservice;

--
-- Name: light_float_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_float_colours (
    light_float_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT light_float_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.light_float_colours OWNER TO atonservice;

--
-- Name: light_float_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_float_nature_of_constructions (
    light_float_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT light_float_nature_of_constructio_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.light_float_nature_of_constructions OWNER TO atonservice;

--
-- Name: light_float_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_float_statuses (
    light_float_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT light_float_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.light_float_statuses OWNER TO atonservice;

--
-- Name: light_light_visibilities; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_light_visibilities (
    light_id numeric(38,0) NOT NULL,
    light_visibilities character varying(255),
    CONSTRAINT light_light_visibilities_light_visibilities_check CHECK (((light_visibilities)::text = ANY (ARRAY[('HIGH_INTENSITY'::character varying)::text, ('LOW_INTENSITY'::character varying)::text, ('FAINT'::character varying)::text, ('INTENSIFIED'::character varying)::text, ('UNINTENSIFIED'::character varying)::text, ('VISIBILITY_DELIBERATELY_RESTRICTED'::character varying)::text, ('OBSCURED'::character varying)::text, ('PARTIALLY_OBSCURED'::character varying)::text])))
);


ALTER TABLE public.light_light_visibilities OWNER TO atonservice;

--
-- Name: light_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_statuses (
    light_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT light_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.light_statuses OWNER TO atonservice;

--
-- Name: light_vessel_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_vessel_colour_patterns (
    light_vessel_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT light_vessel_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.light_vessel_colour_patterns OWNER TO atonservice;

--
-- Name: light_vessel_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_vessel_colours (
    light_vessel_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT light_vessel_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.light_vessel_colours OWNER TO atonservice;

--
-- Name: light_vessel_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_vessel_nature_of_constructions (
    light_vessel_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT light_vessel_nature_of_constructi_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.light_vessel_nature_of_constructions OWNER TO atonservice;

--
-- Name: light_vessel_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.light_vessel_statuses (
    light_vessel_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT light_vessel_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.light_vessel_statuses OWNER TO atonservice;

--
-- Name: navigation_line_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.navigation_line_statuses (
    navigation_line_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT navigation_line_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.navigation_line_statuses OWNER TO atonservice;

--
-- Name: offshore_platform_category_of_offshore_platforms; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.offshore_platform_category_of_offshore_platforms (
    offshore_platform_id numeric(38,0) NOT NULL,
    category_of_offshore_platforms character varying(255),
    CONSTRAINT offshore_platform_category_o_category_of_offshore_platfor_check CHECK (((category_of_offshore_platforms)::text = ANY (ARRAY[('PRODUCTION_PLATFORM'::character varying)::text, ('OBSERVATION_RESEARCH_PLATFORM'::character varying)::text])))
);


ALTER TABLE public.offshore_platform_category_of_offshore_platforms OWNER TO atonservice;

--
-- Name: offshore_platform_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.offshore_platform_colour_patterns (
    offshore_platform_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT offshore_platform_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.offshore_platform_colour_patterns OWNER TO atonservice;

--
-- Name: offshore_platform_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.offshore_platform_colours (
    offshore_platform_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT offshore_platform_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.offshore_platform_colours OWNER TO atonservice;

--
-- Name: offshore_platform_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.offshore_platform_nature_of_constructions (
    offshore_platform_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT offshore_platform_nature_of_const_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.offshore_platform_nature_of_constructions OWNER TO atonservice;

--
-- Name: offshore_platform_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.offshore_platform_statuses (
    offshore_platform_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT offshore_platform_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.offshore_platform_statuses OWNER TO atonservice;

--
-- Name: physicalaisaid_to_navigation_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.physicalaisaid_to_navigation_statuses (
    physicalaisaid_to_navigation_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT physicalaisaid_to_navigation_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.physicalaisaid_to_navigation_statuses OWNER TO atonservice;

--
-- Name: pile_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.pile_colour_patterns (
    pile_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT pile_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.pile_colour_patterns OWNER TO atonservice;

--
-- Name: pile_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.pile_colours (
    pile_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT pile_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.pile_colours OWNER TO atonservice;

--
-- Name: radar_reflector_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.radar_reflector_statuses (
    radar_reflector_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT radar_reflector_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.radar_reflector_statuses OWNER TO atonservice;

--
-- Name: radar_transponder_beacon_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.radar_transponder_beacon_statuses (
    radar_transponder_beacon_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT radar_transponder_beacon_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.radar_transponder_beacon_statuses OWNER TO atonservice;

--
-- Name: recommended_track_nav_lines; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.recommended_track_nav_lines (
    navigation_line_id numeric(38,0) NOT NULL,
    recommended_track_id numeric(38,0) NOT NULL
);


ALTER TABLE public.recommended_track_nav_lines OWNER TO atonservice;

--
-- Name: recommended_track_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.recommended_track_statuses (
    recommended_track_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT recommended_track_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.recommended_track_statuses OWNER TO atonservice;

--
-- Name: retro_reflector_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.retro_reflector_colour_patterns (
    retro_reflector_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT retro_reflector_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.retro_reflector_colour_patterns OWNER TO atonservice;

--
-- Name: retro_reflector_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.retro_reflector_colours (
    retro_reflector_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT retro_reflector_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.retro_reflector_colours OWNER TO atonservice;

--
-- Name: retro_reflector_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.retro_reflector_statuses (
    retro_reflector_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT retro_reflector_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.retro_reflector_statuses OWNER TO atonservice;

--
-- Name: s125_dataset_content_xref; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.s125_dataset_content_xref (
    dataset_content_id numeric(24,0),
    dataset_uuid uuid NOT NULL
);


ALTER TABLE public.s125_dataset_content_xref OWNER TO atonservice;

--
-- Name: s125dataset; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.s125dataset (
    cancelled boolean,
    dataset_identification_id numeric(38,0),
    created_at timestamp(6) without time zone,
    last_updated_at timestamp(6) without time zone,
    replaces uuid,
    uuid uuid NOT NULL,
    geometry public.geometry
);


ALTER TABLE public.s125dataset OWNER TO atonservice;

--
-- Name: s125dataset_identification; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.s125dataset_identification (
    dataset_reference_date date,
    id numeric(38,0) NOT NULL,
    application_profile character varying(255),
    dataset_abstract character varying(255),
    dataset_file_identifier character varying(255),
    dataset_language character varying(255),
    dataset_title character varying(255),
    encoding_specification character varying(255),
    encoding_specification_edition character varying(255),
    product_edition character varying(255),
    product_identifier character varying(255)
);


ALTER TABLE public.s125dataset_identification OWNER TO atonservice;

--
-- Name: s125dataset_identification_dataset_topic_categories; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.s125dataset_identification_dataset_topic_categories (
    s125dataset_identification_id numeric(38,0) NOT NULL,
    dataset_topic_categories character varying(255),
    CONSTRAINT s125dataset_identification_datas_dataset_topic_categories_check CHECK (((dataset_topic_categories)::text = ANY (ARRAY[('FARMING'::character varying)::text, ('BIOTA'::character varying)::text, ('BOUNDARIES'::character varying)::text, ('CLIMATOLOGY_METEOROLOGY_ATMOSPHERE'::character varying)::text, ('ECONOMY'::character varying)::text, ('ELEVATION'::character varying)::text, ('ENVIRONMENT'::character varying)::text, ('GEOSCIENTIFIC_INFORMATION'::character varying)::text, ('HEALTH'::character varying)::text, ('IMAGERY_BASE_MAPS_EARTH_COVER'::character varying)::text, ('INTELLIGENCE_MILITARY'::character varying)::text, ('INLAND_WATERS'::character varying)::text, ('LOCATION'::character varying)::text, ('OCEANS'::character varying)::text, ('PLANNING_CADASTRE'::character varying)::text, ('SOCIETY'::character varying)::text, ('STRUCTURE'::character varying)::text, ('TRANSPORTATION'::character varying)::text, ('UTILITIES_COMMUNICATION'::character varying)::text])))
);


ALTER TABLE public.s125dataset_identification_dataset_topic_categories OWNER TO atonservice;

--
-- Name: silo_tank_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.silo_tank_colour_patterns (
    silo_tank_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT silo_tank_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.silo_tank_colour_patterns OWNER TO atonservice;

--
-- Name: silo_tank_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.silo_tank_colours (
    silo_tank_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT silo_tank_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.silo_tank_colours OWNER TO atonservice;

--
-- Name: silo_tank_nature_of_constructions; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.silo_tank_nature_of_constructions (
    silo_tank_id numeric(38,0) NOT NULL,
    nature_of_constructions character varying(255),
    CONSTRAINT silo_tank_nature_of_constructions_nature_of_constructions_check CHECK (((nature_of_constructions)::text = ANY (ARRAY[('MASONRY'::character varying)::text, ('HARD_SURFACE'::character varying)::text, ('CONCRETED'::character varying)::text, ('LOOSE_BOULDERS'::character varying)::text, ('WOODEN'::character varying)::text, ('METAL'::character varying)::text, ('PAINTED'::character varying)::text, ('FIBERGLASS'::character varying)::text, ('PLASTIC'::character varying)::text])))
);


ALTER TABLE public.silo_tank_nature_of_constructions OWNER TO atonservice;

--
-- Name: silo_tank_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.silo_tank_statuses (
    silo_tank_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT silo_tank_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.silo_tank_statuses OWNER TO atonservice;

--
-- Name: subscription_request; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.subscription_request (
    container_type smallint,
    data_product_type smallint,
    created_at timestamp(6) without time zone,
    subscription_period_end timestamp(6) without time zone,
    subscription_period_start timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    data_reference uuid,
    uuid uuid NOT NULL,
    client_mrn character varying(255),
    product_version character varying(255),
    unlocode character varying(255),
    geometry public.geometry,
    subscription_geometry public.geometry,
    CONSTRAINT subscription_request_container_type_check CHECK (((container_type >= 0) AND (container_type <= 2))),
    CONSTRAINT subscription_request_data_product_type_check CHECK (((data_product_type >= 0) AND (data_product_type <= 27)))
);


ALTER TABLE public.subscription_request OWNER TO atonservice;

--
-- Name: syntheticaisaid_to_navigation_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.syntheticaisaid_to_navigation_statuses (
    syntheticaisaid_to_navigation_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT syntheticaisaid_to_navigation_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.syntheticaisaid_to_navigation_statuses OWNER TO atonservice;

--
-- Name: topmark_colour_patterns; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.topmark_colour_patterns (
    topmark_id numeric(38,0) NOT NULL,
    colour_patterns character varying(255),
    CONSTRAINT topmark_colour_patterns_colour_patterns_check CHECK (((colour_patterns)::text = ANY (ARRAY[('HORIZONTAL_STRIPES'::character varying)::text, ('VERTICAL_STRIPES'::character varying)::text, ('DIAGONAL_STRIPES'::character varying)::text, ('SQUARED'::character varying)::text, ('STRIPES_DIRECTION_UNKNOWN'::character varying)::text, ('BORDER_STRIPE'::character varying)::text, ('SINGLE_COLOUR'::character varying)::text, ('RECTANGLE'::character varying)::text, ('TRIANGLE'::character varying)::text, ('OTHER_PATTERN'::character varying)::text])))
);


ALTER TABLE public.topmark_colour_patterns OWNER TO atonservice;

--
-- Name: topmark_colours; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.topmark_colours (
    topmark_id numeric(38,0) NOT NULL,
    colours character varying(255),
    CONSTRAINT topmark_colours_colours_check CHECK (((colours)::text = ANY (ARRAY[('WHITE'::character varying)::text, ('BLACK'::character varying)::text, ('RED'::character varying)::text, ('GREEN'::character varying)::text, ('BLUE'::character varying)::text, ('YELLOW'::character varying)::text, ('GREY'::character varying)::text, ('BROWN'::character varying)::text, ('ORANGE'::character varying)::text, ('FLUORESCENT_WHITE'::character varying)::text, ('FLUORESCENT_RED'::character varying)::text, ('FLUORESCENT_GREEN'::character varying)::text, ('FLUORESCENT_ORANGE'::character varying)::text])))
);


ALTER TABLE public.topmark_colours OWNER TO atonservice;

--
-- Name: topmark_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.topmark_statuses (
    topmark_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT topmark_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.topmark_statuses OWNER TO atonservice;

--
-- Name: virtualaisaid_to_navigation_statuses; Type: TABLE; Schema: public; Owner: atonservice
--

CREATE TABLE public.virtualaisaid_to_navigation_statuses (
    virtualaisaid_to_navigation_id numeric(38,0) NOT NULL,
    statuses character varying(255),
    CONSTRAINT virtualaisaid_to_navigation_statuses_statuses_check CHECK (((statuses)::text = ANY (ARRAY[('PERMANENT'::character varying)::text, ('NOT_IN_USE'::character varying)::text, ('PERIODIC_INTERMITTENT'::character varying)::text, ('TEMPORARY'::character varying)::text, ('PRIVATE'::character varying)::text, ('PUBLIC'::character varying)::text, ('WATCHED'::character varying)::text, ('UN_WATCHED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANDIDATE'::character varying)::text, ('UNDER_MODIFICATION'::character varying)::text, ('CANDIDATE_FOR_MODIFICATION'::character varying)::text, ('UNDER_REMOVAL_DELETION'::character varying)::text, ('REMOVED_DELETED'::character varying)::text, ('EXPERIMENTAL'::character varying)::text, ('TEMPORARILY_DISCONTINUED'::character varying)::text, ('TEMPORARILY_RELOCATED'::character varying)::text])))
);


ALTER TABLE public.virtualaisaid_to_navigation_statuses OWNER TO atonservice;

--
-- Name: aggregation_join_table aggregation_join_table_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aggregation_join_table
    ADD CONSTRAINT aggregation_join_table_pkey PRIMARY KEY (aggregation_id, aton_id);


--
-- Name: aggregation aggregation_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aggregation
    ADD CONSTRAINT aggregation_pkey PRIMARY KEY (id);


--
-- Name: aids_to_navigation aids_to_navigation_id_code_key; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aids_to_navigation
    ADD CONSTRAINT aids_to_navigation_id_code_key UNIQUE (id_code);


--
-- Name: aids_to_navigation aids_to_navigation_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aids_to_navigation
    ADD CONSTRAINT aids_to_navigation_pkey PRIMARY KEY (id);


--
-- Name: association_join_table association_join_table_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.association_join_table
    ADD CONSTRAINT association_join_table_pkey PRIMARY KEY (association_id, aton_id);


--
-- Name: association association_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.association
    ADD CONSTRAINT association_pkey PRIMARY KEY (id);


--
-- Name: dataset_content_log dataset_content_log_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.dataset_content_log
    ADD CONSTRAINT dataset_content_log_pkey PRIMARY KEY (id);


--
-- Name: dataset_content dataset_content_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.dataset_content
    ADD CONSTRAINT dataset_content_pkey PRIMARY KEY (id);


--
-- Name: feature_name feature_name_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.feature_name
    ADD CONSTRAINT feature_name_pkey PRIMARY KEY (id);


--
-- Name: information information_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.information
    ADD CONSTRAINT information_pkey PRIMARY KEY (id);


--
-- Name: s125_dataset_content_xref s125_dataset_content_xref_dataset_content_id_key; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125_dataset_content_xref
    ADD CONSTRAINT s125_dataset_content_xref_dataset_content_id_key UNIQUE (dataset_content_id);


--
-- Name: s125_dataset_content_xref s125_dataset_content_xref_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125_dataset_content_xref
    ADD CONSTRAINT s125_dataset_content_xref_pkey PRIMARY KEY (dataset_uuid);


--
-- Name: s125dataset s125dataset_dataset_identification_id_key; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125dataset
    ADD CONSTRAINT s125dataset_dataset_identification_id_key UNIQUE (dataset_identification_id);


--
-- Name: s125dataset_identification s125dataset_identification_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125dataset_identification
    ADD CONSTRAINT s125dataset_identification_pkey PRIMARY KEY (id);


--
-- Name: s125dataset s125dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125dataset
    ADD CONSTRAINT s125dataset_pkey PRIMARY KEY (uuid);


--
-- Name: subscription_request subscription_request_pkey; Type: CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.subscription_request
    ADD CONSTRAINT subscription_request_pkey PRIMARY KEY (uuid);


--
-- Name: idx4mcuo154o08owisji6m02jnef; Type: INDEX; Schema: public; Owner: atonservice
--

CREATE INDEX idx4mcuo154o08owisji6m02jnef ON public.dataset_content_log USING btree (dataset_type, uuid, operation, sequence_no, generated_at);


--
-- Name: information fk2aomer28cjm92xcitwvk0ushk; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.information
    ADD CONSTRAINT fk2aomer28cjm92xcitwvk0ushk FOREIGN KEY (feature_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_float_nature_of_constructions fk2xvmp44u583s5j7mach3yp98u; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_float_nature_of_constructions
    ADD CONSTRAINT fk2xvmp44u583s5j7mach3yp98u FOREIGN KEY (light_float_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: offshore_platform_colours fk3omovbpo3ejssis657eqkh3ft; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.offshore_platform_colours
    ADD CONSTRAINT fk3omovbpo3ejssis657eqkh3ft FOREIGN KEY (offshore_platform_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: beacon_special_purpose_category_of_special_purpose_marks fk3wbq3tb33ytp70vsjxkeuevn4; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.beacon_special_purpose_category_of_special_purpose_marks
    ADD CONSTRAINT fk3wbq3tb33ytp70vsjxkeuevn4 FOREIGN KEY (beacon_special_purpose_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: offshore_platform_statuses fk4a92ructtenbqc6dln1l64qa0; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.offshore_platform_statuses
    ADD CONSTRAINT fk4a92ructtenbqc6dln1l64qa0 FOREIGN KEY (offshore_platform_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: syntheticaisaid_to_navigation_statuses fk4qkj34pvxx9srfyhjfh8sq0uo; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.syntheticaisaid_to_navigation_statuses
    ADD CONSTRAINT fk4qkj34pvxx9srfyhjfh8sq0uo FOREIGN KEY (syntheticaisaid_to_navigation_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_nature_of_constructions fk5fsuxp110jqqksdsb6sqhbxy1; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_nature_of_constructions
    ADD CONSTRAINT fk5fsuxp110jqqksdsb6sqhbxy1 FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: silo_tank_statuses fk5oflmf4ojxpwcvsjbgv0lrddu; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.silo_tank_statuses
    ADD CONSTRAINT fk5oflmf4ojxpwcvsjbgv0lrddu FOREIGN KEY (silo_tank_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_buoy_colour_patterns fk5txne3qukaebfjb2vow00hskp; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_buoy_colour_patterns
    ADD CONSTRAINT fk5txne3qukaebfjb2vow00hskp FOREIGN KEY (generic_buoy_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: retro_reflector_colour_patterns fk5uwkfqryybf53twhaexhawx9f; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.retro_reflector_colour_patterns
    ADD CONSTRAINT fk5uwkfqryybf53twhaexhawx9f FOREIGN KEY (retro_reflector_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: daymark_colour_patterns fk672e8amd17ud2myo2542v68rj; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.daymark_colour_patterns
    ADD CONSTRAINT fk672e8amd17ud2myo2542v68rj FOREIGN KEY (daymark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_vessel_colour_patterns fk6b35oy77hapi82aqex767k33k; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_vessel_colour_patterns
    ADD CONSTRAINT fk6b35oy77hapi82aqex767k33k FOREIGN KEY (light_vessel_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: s125dataset fk6iq9xxb0c1hipfvh9329n4fb3; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125dataset
    ADD CONSTRAINT fk6iq9xxb0c1hipfvh9329n4fb3 FOREIGN KEY (dataset_identification_id) REFERENCES public.s125dataset_identification(id);


--
-- Name: physicalaisaid_to_navigation_statuses fk6u448qfcfxwd7w4swx76tciiv; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.physicalaisaid_to_navigation_statuses
    ADD CONSTRAINT fk6u448qfcfxwd7w4swx76tciiv FOREIGN KEY (physicalaisaid_to_navigation_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: broadcast_by_join_table fk7k5gdm25xs6sydapvsfj2la3n; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.broadcast_by_join_table
    ADD CONSTRAINT fk7k5gdm25xs6sydapvsfj2la3n FOREIGN KEY (ais_aton_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_statuses fk7l1t5s3r0to3ab0v2k73591vh; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_statuses
    ADD CONSTRAINT fk7l1t5s3r0to3ab0v2k73591vh FOREIGN KEY (light_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_category_of_lights fk7l6fwt0qt53v7txmms37v01q1; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_category_of_lights
    ADD CONSTRAINT fk7l6fwt0qt53v7txmms37v01q1 FOREIGN KEY (light_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_buoy_statuses fk83oowdnwv9o8gj32dhkvs97t2; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_buoy_statuses
    ADD CONSTRAINT fk83oowdnwv9o8gj32dhkvs97t2 FOREIGN KEY (generic_buoy_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_beacon_colour_patterns fk8c9q44e199mu3yyd2xo8e3tke; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_beacon_colour_patterns
    ADD CONSTRAINT fk8c9q44e199mu3yyd2xo8e3tke FOREIGN KEY (generic_beacon_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_category_of_landmarks fk8n27xce5ev8842t2sja4lx4sd; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_category_of_landmarks
    ADD CONSTRAINT fk8n27xce5ev8842t2sja4lx4sd FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_vessel_statuses fk8x7eiobw1vjaaokxoekrow8db; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_vessel_statuses
    ADD CONSTRAINT fk8x7eiobw1vjaaokxoekrow8db FOREIGN KEY (light_vessel_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: buoy_special_purpose_category_of_special_purpose_marks fk9jw2xf1miah3fwe0kychek7pm; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.buoy_special_purpose_category_of_special_purpose_marks
    ADD CONSTRAINT fk9jw2xf1miah3fwe0kychek7pm FOREIGN KEY (buoy_special_purpose_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: daymark_statuses fk9uj205brx918jmmvva7dv1qwe; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.daymark_statuses
    ADD CONSTRAINT fk9uj205brx918jmmvva7dv1qwe FOREIGN KEY (daymark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: s125_dataset_content_xref fka4prate4l9qdl4218slsd6ved; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125_dataset_content_xref
    ADD CONSTRAINT fka4prate4l9qdl4218slsd6ved FOREIGN KEY (dataset_content_id) REFERENCES public.dataset_content(id);


--
-- Name: feature_name fkab5fn0026au99gmc56o0kaj6j; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.feature_name
    ADD CONSTRAINT fkab5fn0026au99gmc56o0kaj6j FOREIGN KEY (feature_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: silo_tank_colour_patterns fkamr0dmcs7cnq6sipg7wj9b8rm; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.silo_tank_colour_patterns
    ADD CONSTRAINT fkamr0dmcs7cnq6sipg7wj9b8rm FOREIGN KEY (silo_tank_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_float_colours fkba022nc7q3lj8gymm48xp7v12; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_float_colours
    ADD CONSTRAINT fkba022nc7q3lj8gymm48xp7v12 FOREIGN KEY (light_float_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: radar_transponder_beacon_statuses fkbddvdidj0cosfjt8nau1sik47; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.radar_transponder_beacon_statuses
    ADD CONSTRAINT fkbddvdidj0cosfjt8nau1sik47 FOREIGN KEY (radar_transponder_beacon_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: silo_tank_colours fkbenumyd918txgdetfa4ehdlfj; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.silo_tank_colours
    ADD CONSTRAINT fkbenumyd918txgdetfa4ehdlfj FOREIGN KEY (silo_tank_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: navigation_line_statuses fkbyxrjlfbms9c8whwph5engt82; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.navigation_line_statuses
    ADD CONSTRAINT fkbyxrjlfbms9c8whwph5engt82 FOREIGN KEY (navigation_line_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: pile_colour_patterns fkc0kbdg6qr78unrd9fm61g1e2i; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.pile_colour_patterns
    ADD CONSTRAINT fkc0kbdg6qr78unrd9fm61g1e2i FOREIGN KEY (pile_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: s125_dataset_content_xref fkc166wxoregs8jmn9comjgsl4g; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125_dataset_content_xref
    ADD CONSTRAINT fkc166wxoregs8jmn9comjgsl4g FOREIGN KEY (dataset_uuid) REFERENCES public.s125dataset(uuid);


--
-- Name: generic_beacon_colours fkcid9qtjlrhjb39v7tcpwh6tfu; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_beacon_colours
    ADD CONSTRAINT fkcid9qtjlrhjb39v7tcpwh6tfu FOREIGN KEY (generic_beacon_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: fog_signal_statuses fkd1fov6iuwkqj2rue5aph9lset; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.fog_signal_statuses
    ADD CONSTRAINT fkd1fov6iuwkqj2rue5aph9lset FOREIGN KEY (fog_signal_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: broadcast_by_join_table fkd4rfi17wmgtxabphk6qpdwc9o; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.broadcast_by_join_table
    ADD CONSTRAINT fkd4rfi17wmgtxabphk6qpdwc9o FOREIGN KEY (radio_station_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_colour_patterns fkdlymx2s2ovrw9qgqkf4yr9wio; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_colour_patterns
    ADD CONSTRAINT fkdlymx2s2ovrw9qgqkf4yr9wio FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_statuses fkdnhsghr8jgyf2ot43vagnyswm; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_statuses
    ADD CONSTRAINT fkdnhsghr8jgyf2ot43vagnyswm FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: association_join_table fkdv9k366f0aksy16igah9ys268; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.association_join_table
    ADD CONSTRAINT fkdv9k366f0aksy16igah9ys268 FOREIGN KEY (association_id) REFERENCES public.association(id);


--
-- Name: offshore_platform_nature_of_constructions fkdyg1o4yv2qnokwsanryipmq09; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.offshore_platform_nature_of_constructions
    ADD CONSTRAINT fkdyg1o4yv2qnokwsanryipmq09 FOREIGN KEY (offshore_platform_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_functions fkdysttmq6d6t9appu8v9t5viin; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_functions
    ADD CONSTRAINT fkdysttmq6d6t9appu8v9t5viin FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_buoy_nature_ofconstuctions fkef2xdjm0jjf9vd1rjmydxv8aj; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_buoy_nature_ofconstuctions
    ADD CONSTRAINT fkef2xdjm0jjf9vd1rjmydxv8aj FOREIGN KEY (generic_buoy_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: recommended_track_nav_lines fkeni1a0te0u215bn43ltt0cu1r; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.recommended_track_nav_lines
    ADD CONSTRAINT fkeni1a0te0u215bn43ltt0cu1r FOREIGN KEY (navigation_line_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: virtualaisaid_to_navigation_statuses fkfivvugv07uj29agevx1oso1k5; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.virtualaisaid_to_navigation_statuses
    ADD CONSTRAINT fkfivvugv07uj29agevx1oso1k5 FOREIGN KEY (virtualaisaid_to_navigation_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: aggregation_join_table fkfo0k46oyqmnspjake95x8wejs; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aggregation_join_table
    ADD CONSTRAINT fkfo0k46oyqmnspjake95x8wejs FOREIGN KEY (aggregation_id) REFERENCES public.aggregation(id);


--
-- Name: topmark_statuses fkfrgb3vgon7cwt2avbak7p7cfl; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.topmark_statuses
    ADD CONSTRAINT fkfrgb3vgon7cwt2avbak7p7cfl FOREIGN KEY (topmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: aggregation_join_table fkgi1orxdge89o874tr8uqv3g2m; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aggregation_join_table
    ADD CONSTRAINT fkgi1orxdge89o874tr8uqv3g2m FOREIGN KEY (aton_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: topmark_colour_patterns fkh78djj7ilv86539msxim0jkjl; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.topmark_colour_patterns
    ADD CONSTRAINT fkh78djj7ilv86539msxim0jkjl FOREIGN KEY (topmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_beacon_nature_of_constructions fkhtwo4tjm2umvxk5g35odb1w5b; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_beacon_nature_of_constructions
    ADD CONSTRAINT fkhtwo4tjm2umvxk5g35odb1w5b FOREIGN KEY (generic_beacon_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: pile_colours fkj16km8aaau28qdgw8gnipkfvw; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.pile_colours
    ADD CONSTRAINT fkj16km8aaau28qdgw8gnipkfvw FOREIGN KEY (pile_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_light_visibilities fkjtynffudccc1r1xumt3fjxbk3; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_light_visibilities
    ADD CONSTRAINT fkjtynffudccc1r1xumt3fjxbk3 FOREIGN KEY (light_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_float_statuses fkk9q8q6seqe0drxjth225y9t0s; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_float_statuses
    ADD CONSTRAINT fkk9q8q6seqe0drxjth225y9t0s FOREIGN KEY (light_float_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: aids_to_navigation fkl4aphybohji5bxrkpu2evte79; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.aids_to_navigation
    ADD CONSTRAINT fkl4aphybohji5bxrkpu2evte79 FOREIGN KEY (parent_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: daymark_nature_of_constructions fklexo016yqlakrcdxj5eh3xpuu; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.daymark_nature_of_constructions
    ADD CONSTRAINT fklexo016yqlakrcdxj5eh3xpuu FOREIGN KEY (daymark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: retro_reflector_colours fklsj15gkc4u2mdwms7utewcv9c; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.retro_reflector_colours
    ADD CONSTRAINT fklsj15gkc4u2mdwms7utewcv9c FOREIGN KEY (retro_reflector_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: recommended_track_nav_lines fklu033whce7cmk4enhusmkuign; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.recommended_track_nav_lines
    ADD CONSTRAINT fklu033whce7cmk4enhusmkuign FOREIGN KEY (recommended_track_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_float_colour_patterns fkm6yh2wjl4sx9v1bf17o90a5x0; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_float_colour_patterns
    ADD CONSTRAINT fkm6yh2wjl4sx9v1bf17o90a5x0 FOREIGN KEY (light_float_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: offshore_platform_colour_patterns fkmpg445gugc7hk2l3vcrj6v22p; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.offshore_platform_colour_patterns
    ADD CONSTRAINT fkmpg445gugc7hk2l3vcrj6v22p FOREIGN KEY (offshore_platform_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: recommended_track_statuses fkn0lmw8trccihu8avhljnditfy; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.recommended_track_statuses
    ADD CONSTRAINT fkn0lmw8trccihu8avhljnditfy FOREIGN KEY (recommended_track_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: topmark_colours fko38bvwbecuavymunepg8b83ca; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.topmark_colours
    ADD CONSTRAINT fko38bvwbecuavymunepg8b83ca FOREIGN KEY (topmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: daymark_colours fkolv2h9xncd5r5wi34iduf5bmg; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.daymark_colours
    ADD CONSTRAINT fkolv2h9xncd5r5wi34iduf5bmg FOREIGN KEY (daymark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: landmark_colours fkp5xq9ykqfhqelsd6vqybexxi8; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.landmark_colours
    ADD CONSTRAINT fkp5xq9ykqfhqelsd6vqybexxi8 FOREIGN KEY (landmark_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: offshore_platform_category_of_offshore_platforms fkpbhlf959xo5lg7xsaftlxykm4; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.offshore_platform_category_of_offshore_platforms
    ADD CONSTRAINT fkpbhlf959xo5lg7xsaftlxykm4 FOREIGN KEY (offshore_platform_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: s125dataset_identification_dataset_topic_categories fkpjkas0dq6udi67h382ccka5iw; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.s125dataset_identification_dataset_topic_categories
    ADD CONSTRAINT fkpjkas0dq6udi67h382ccka5iw FOREIGN KEY (s125dataset_identification_id) REFERENCES public.s125dataset_identification(id);


--
-- Name: association_join_table fkptw72of63ump5specjkwfoigi; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.association_join_table
    ADD CONSTRAINT fkptw72of63ump5specjkwfoigi FOREIGN KEY (aton_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_buoy_colours fkqlayplmfv7p4rubc3r7vx6geg; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_buoy_colours
    ADD CONSTRAINT fkqlayplmfv7p4rubc3r7vx6geg FOREIGN KEY (generic_buoy_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: generic_beacon_statuses fkrs59lewrie8cuivokrykq1sxm; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.generic_beacon_statuses
    ADD CONSTRAINT fkrs59lewrie8cuivokrykq1sxm FOREIGN KEY (generic_beacon_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: silo_tank_nature_of_constructions fks8beivg3tnqciji0y1fqwp77i; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.silo_tank_nature_of_constructions
    ADD CONSTRAINT fks8beivg3tnqciji0y1fqwp77i FOREIGN KEY (silo_tank_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_vessel_colours fksb65mo973oqic4bk110898ktd; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_vessel_colours
    ADD CONSTRAINT fksb65mo973oqic4bk110898ktd FOREIGN KEY (light_vessel_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: radar_reflector_statuses fksnim7v1rd2333g21ybcpt7chv; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.radar_reflector_statuses
    ADD CONSTRAINT fksnim7v1rd2333g21ybcpt7chv FOREIGN KEY (radar_reflector_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: light_vessel_nature_of_constructions fksw1yugjjvc3121lw576nlgin6; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.light_vessel_nature_of_constructions
    ADD CONSTRAINT fksw1yugjjvc3121lw576nlgin6 FOREIGN KEY (light_vessel_id) REFERENCES public.aids_to_navigation(id);


--
-- Name: retro_reflector_statuses fkteix1uk4u4ybcq2q1uhxdpa1; Type: FK CONSTRAINT; Schema: public; Owner: atonservice
--

ALTER TABLE ONLY public.retro_reflector_statuses
    ADD CONSTRAINT fkteix1uk4u4ybcq2q1uhxdpa1 FOREIGN KEY (retro_reflector_id) REFERENCES public.aids_to_navigation(id);


--
-- PostgreSQL database dump complete
--
