/*
 * Copyright (c) 2011,2012 Techlier Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.techlier.weather.gpv.grib2;

import java.util.HashMap;
import java.util.Map;

import jp.techlier.weather.gpv.grib2.types.ByteBits;
import jp.techlier.weather.gpv.grib2.types.Container.Assertion;
import jp.techlier.weather.gpv.grib2.types.Content;
import jp.techlier.weather.gpv.grib2.types.EnumValue;
import jp.techlier.weather.gpv.grib2.types.EnumValues;
import jp.techlier.weather.gpv.grib2.types.Section;
import jp.techlier.weather.gpv.grib2.types.Template;
import jp.techlier.weather.gpv.grib2.types.TemplateNumber;


/**
 * Structure of GRIB2 Messages.
 *
 * <ul>
 * <li><a href="http://www.wmo.int/pages/prog/www/WMOCodes.html">
 * World Meteorological Organization: WMO Internationl Codes</a><ul>
 * <li><a href="http://www.wmo.int/pages/prog/www/WMOCodes/Guides/GRIB/GRIB2_062006.pdf">
 * Guide to FM92 GRIB Edition 2 (revised in November 2007)</a></li>
 * <li><a href="http://www.wmo.int/pages/prog/www/WMOCodes/TDCFtables.html">
 * TDCF tables extracted from the Manual on Codes, Volume I.2</a><ul>
 * <li><a href="http://www.wmo.int/pages/prog/www/WMOCodes/WMO306_vI2/2011edition/GRIB2ver7/GRIB2_7_0_0_Temp.pdf">
 * Templates (Version7)</a></li>
 * <li><a href="http://www.wmo.int/pages/prog/www/WMOCodes/WMO306_vI2/2011edition/GRIB2ver7/GRIB2_7_0_0_CodeFlag.pdf">
 * Code and Flag tables (Version7)</a></li>
 * </ul></li>
 * </ul></li>
 * </ul>
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/16
 */
public abstract class Grib2 {

    public enum SectionNumber implements EnumValue<SectionNumber> {
        INDICATOR_SECTION(INDICATOR_SECTION_NUMBER, IndicatorSection.class),
        IDENTIFICATION_SECTION(IDENTIFICATION_SECTION_NUMBER, IdentificationSection.class),
        GRID_DEFINITION_SECTION(GRID_DEFINITION_SECTION_NUMBER, GridDefinitionSection.class),
        PRODUCT_DEFITION_SECTION(PRODUCT_DEFITION_SECTION_NUMBER, ProductDefinitionSection.class),
        DATA_REPRESENTAITON_SECTION(DATA_REPRESENTAITON_SECTION_NUMBER, DataRepresentationSection.class),
        BITMAP_SECTION(BITMAP_SECTION_NUMBER, BitmapSection.class),
        DATA_SECTION(DATA_SECTION_NUMBER, DataSection.class),
        END_SECTION(END_SECTION_NUMBER, EndSection.class)
        ;

        private final int value_;
        private final Class<? extends Section> sectionType_;
        SectionNumber(final int value, Class<? extends Section> type) {
            this.value_ = value;
            this.sectionType_ = type;
        }
        @Override public int intValue() { return value_; }
        public Class<? extends Section> sectionType() { return sectionType_; }
        @Override public SectionNumber enumValue() { return this; }
        public static SectionNumber valueOf(final int value) {
            return EnumValues.valueOf(SectionNumber.class, value);
        }
    }


    public static final int INDICATOR_SECTION_NUMBER = 0;
    public static final int INDICATOR_SECTION_LENGTH = 16;
    public static final int INDICATOR_SECTION_MARKER = 0x47534942; // "GRIB"

    /**
     * Section 0: Indicator Section
     * <p>
     * The Indicator Section serves to: identify the start of the GRIB2 message in a human readable
     * form, describe the “Discipline” of the information contained in the message, indicate the Edition
     * Number of GRIB (2 in the case of GRIB2) used to encode the message, and the total length of the
     * message. The section is always 16 octets long. The contents of the Indicator Section are:
     * <pre>
     * Content No.    Contents
     * 1-4          “GRIB” (coded according to the International Alphabet No. 5)
     * 5-6          Reserved
     * 7            Discipline – GRIB Master Table Number (see Code Table 0.0)
     * 8            GRIB Edition Number (currently 2)
     * 9-16         Total length of GRIB message in octets (including Section 0)
     * </pre>
     * </p>
     */
    @SuppressWarnings("unused")
    @Assertion(section=INDICATOR_SECTION_NUMBER, length=INDICATOR_SECTION_LENGTH)
    public interface IndicatorSection extends Section {
        @Content(offset=1, length=4, value=INDICATOR_SECTION_MARKER) abstract String header();
        @Content(offset=5, length=2, value=-1) short __reserved();
        @Content(offset=7, length=1, type=MasterTableCode.class, value={0,10})
                                    EnumValue<MasterTableCode> discipline();
        @Content(offset=8, length=1, value=2) byte editionNumber();
        @Content(offset=9, length=8) long totalLength();
    }

    /**
     * Code table 0.0 - Discipline of processed data in the GRIB message, number of GRIB Master table
     * <pre>
     * Code figure  Meaning
     * 0            Meteorological products
     * 1            Hydrological products
     * 2            Land surface products
     * 3            Space products
     * 4-9          Reserved
     * 10           Oceanographic products
     * 11-191       Reserved
     * 192-254      Reserved for local use
     * 255          Missing
     * </pre>
     */
    public enum MasterTableCode implements EnumValue<MasterTableCode> {
        METEOROLOGICAL(0),
        //HYDOLOGICAL(1),
        //LAND_SURFACE(2),
        //SPACE(3),
        OCEANOGRAPHIC(10)
        ;

        private MasterTableCode(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public MasterTableCode enumValue() { return this; }
        public static MasterTableCode valueOf(final int value) {
            return EnumValues.valueOf(MasterTableCode.class, value);
        }
    }



    public static final int IDENTIFICATION_SECTION_NUMBER = 1;

    /**
     * 2.1.2 Section 1: Identification Section
     * <p>
     * The Identification Section contains characteristics that apply to all processed data in the GRIB
     * message. These characteristics identify the originating centre and sub-centre, indicate the GRIB
     * Master Table and Local Table versions used, and give the reference time, the production status,
     * and the type of processed data contained in this GRIB message. The contents of the Section are:
     * <pre>
     * Content No.    Contents
     * 1-4          Length of section in octets (21 or nn)
     * 5            Number of Section (“1”)
     * 6-7          Identification of originating/generating centre (see Common Code Table C-1)
     * 8-9          Identification of originating/generating sub-centre (allocated by originating/generating centre)
     * 10           GRIB Master Tables Version Number (see Code Table 1.0)
     * 11           GRIB Local Tables Version Number (see Code Table 1.1)
     * 12           Significance of Reference Time (see Code Table 1.2)
     * 13-14        Year (4 digits) |
     * 15           Month           |
     * 16           Day             | Reference time of data
     * 17           Hour            |
     * 18           Minute          |
     * 19           Second          |
     * 20           Production status of processed data in this GRIB message (see Code Table 1.3)
     * 21           Type of processed data in this GRIB message (see Code Table 1.4)
     * 22-nn        Reserved: need not be present
     * </pre>
     * Note that octets beyond 21 of the Identification Section are reserved for future use and need not
     * be present. Users of GRIB messages are strongly urged to always use the length of section given
     * in octets 1 – 4 of sections 1 - 7 to determine where the next section begins. Never assume a fixed
     * octet length of any section other than the Indicator Section (Section 0 – always 16 octets long) and
     * the End Section (Section 8 – always 4 octets long). Also note that the Section number is given in
     * octet 5. This is true for sections 1 – 7. It is required in order to identify Sections in a GRIB
     * message containing multiple data sets.
     * </p>
     */
    @Assertion(section=IDENTIFICATION_SECTION_NUMBER, length=22)
    public interface IdentificationSection extends Section {
        @Content(offset=6, length=2, value=34) short centreId(); // Tokyo
        @Content(offset=8, length=2, value=0) short subCentreId();
        @Content(offset=10, length=1, value={2,4,5}) byte masterVersion();
        @Content(offset=11, length=1, value=1) byte localVersion();
        @Content(offset=12, length=1, type=ReferenceTimeSignificance.class, value=1)
                                    EnumValue<ReferenceTimeSignificance> significanceOfRefecenceTime();
        @Content(offset=13, length=2) short year();
        @Content(offset=15, length=1) byte month();
        @Content(offset=16, length=1) byte mday();
        @Content(offset=17, length=1) byte hour();
        @Content(offset=18, length=1) byte minute();
        @Content(offset=19, length=1) byte second();
        @Content(offset=20, length=1, type=ProductionStatus.class, value=0)
                                    EnumValue<ProductionStatus> productionStatus();
        @Content(offset=21, length=1, type=DataType.class, value={1,5})
                                    EnumValue<DataType> dataType();
    }

    /**
     * Code table 1.2 - Significance of reference time
     * <pre>
     * Code figure  Meaning
     * 0            Analysis
     * 1            Start of forecast
     * 2            Verifying time of forecast
     * 3            Observation time
     * 4-191        Reserved
     * 192-254      Reserved for local use
     * 255          Missing
     * </pre>
     */
    public enum ReferenceTimeSignificance implements EnumValue<ReferenceTimeSignificance> {
        //ANALYSYS(0),
        START_OF_FORECAST(1),
        //VERIFING_OF_FORECAST(2),
        //OBSERVATION_TIME(3),
        ;

        private ReferenceTimeSignificance(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public ReferenceTimeSignificance enumValue() { return this; }
        public static ReferenceTimeSignificance valueOf(final int value) {
            return EnumValues.valueOf(ReferenceTimeSignificance.class, value);
        }
    }

    /**
     * Code table 1.3 - Production status of data
     * <pre>
     * Code figure  Meaning
     * 0            Operational products
     * 1            Operational test products
     * 2            Research products
     * 3            Re-analysis products
     * 4            THORPEX Interactive Grand Global Ensemble (TIGGE)
     * 5            THORPEX Interactive Grand Global Ensemble (TIGGE) test
     * 6-191        Reserved
     * 192-254      Reserved for local use
     * 255          Missing
     * </pre>
     */
    public enum ProductionStatus implements EnumValue<ProductionStatus> {
        OPERATIONAL(0),
        ;

        private ProductionStatus(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public ProductionStatus enumValue() { return this; }
        public static ProductionStatus valueOf(final int value) {
            return EnumValues.valueOf(ProductionStatus.class, value);
        }
    }

    /**
     * Code table 1.4 - Type of data
     * <pre>
     * Code figure  Meaning
     * 0            Analysis products
     * 1            Forecast products
     * 2            Analysis and forecast products
     * 3            Control forecast products
     * 4            Perturbed forecast products
     * 5            Control and perturbed forecast products
     * 6            Processed satellite observations
     * 7            Processed radar observations
     * 8            Event probability
     * 9-191        Reserved
     * 192-254      Reserved for local use
     * 255          Missing
     * </pre>
     */
    public enum DataType implements EnumValue<DataType> {
        //ANALYSYS(0),
        FORECAST(1),
        //ANALYSYS_AND_FORECAST(2),
        //CONTROL_FORECAST(3),
        //PERTURBED_FORECAST(4),
        CONTROL_AND_PERTURBED(5),
        //SATELLITE_OBSERVATIONS(6),
        //RADAR_OBSERVATIONS(7),
        ;

        private DataType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public DataType enumValue() { return this; }
        public static DataType valueOf(final int value) {
            return EnumValues.valueOf(DataType.class, value);
        }
    }



    public static final int GRID_DEFINITION_SECTION_NUMBER = 3;

    /**
     * Section 3: Grid Definition Section
     * <p>
     * The purpose of the Grid Definition Section is to define the grid surface and geometry of the data
     * values within the surface for the data contained in the next occurrence of the Data Section.
     * GRIB2 retains the powerful GRIB1 concept of a template in this Section and extends it to the
     * Product Definition, Data Representation, and Data Sections as well. Use of a template means
     * there are very few values common to all Grid Definition Sections possible in GRIB2. Rather, the
     * number of the Grid Definition Template used is encoded. The values that must follow are those
     * required by that particular Grid Definition Template. The contents of Section 3 are:
     * <pre>
     * Content No.    Contents
     * 1-4          Length of section in octets (nn)
     * 5            Number of Section (“3”)
     * 6            Source of grid definition (see Code Table 3.0 and Note 1)
     * 7-10         Number of data points
     * 11           Number of octets for optional list of numbers defining number of points (see Note 2)
     * 12           Interpretation of list of numbers defining number of points (see Code Table 3.11)
     * 13-14        Grid Definition Template Number (N) (see Code Table 3.1)
     * 15-xx        Grid Definition Template (see Template 3.N, where N is the Grid Definition Template Number given in octets 13-14)
     * [xx+1]-nn    Optional list of numbers defining number of points (see Notes 2, 3, and 4)
     * </pre>
     * </p>
     */
    @Assertion(section=GRID_DEFINITION_SECTION_NUMBER, length=72)
    public interface GridDefinitionSection extends Section {
        @Content(offset=6, length=1, value=0) byte sourceOfDefinition();
        @Content(offset=7, length=4) int numPoints();
        @Content(offset=11, length=1, value=0) byte numOptionalList();
        @Content(offset=12, length=1, value=0) byte optionalPointInterpretation();
        @Content(offset=13, length=2, type=GridDefinitionTemplateNumber.class, value=0)
                                    EnumValue<GridDefinitionTemplateNumber> templateNumber();
        @Content(offset=15) GridDefinitionTemplate template();
    }

    /**
     * Code table 3.1 - Grid definition template number
     * <pre>
     * Code figure  Meaning                     Comments
     * 0            Latitude/longitude          Also called equidistant cylindrical, or Plate Carrée
     * 1            Rotated latitude/longitude
     * 2            Stretched latitude/longitude
     * 3            Stretched and rotated latitude/longitude
     * and others...
     * </pre>
     */
    public enum GridDefinitionTemplateNumber
            implements TemplateNumber<GridDefinitionTemplate>, EnumValue<GridDefinitionTemplateNumber> {
        LATLONG(0, GridDefinitionTemplate.class),
        //ROTATED_LATLONG(1),
        //STREACHED_LATLONG(2),
        //STREACHED_ROTATED_LATLONG(3),
        ;

        private final int value_;
        private final Class<? extends GridDefinitionTemplate> templateType_;

        private GridDefinitionTemplateNumber(final int value,
                                             final Class<? extends GridDefinitionTemplate> type) {
            this.value_ = value;
            this.templateType_ = type;
        }

        @Override public int intValue() { return value_; }
        @Override public Class<? extends GridDefinitionTemplate> templateType() { return templateType_; }
        @Override public GridDefinitionTemplateNumber enumValue() { return this; }
        public static GridDefinitionTemplateNumber valueOf(final int value) {
            return EnumValues.valueOf(GridDefinitionTemplateNumber.class, value);
        }
    }

    /**
     * Grid definition template 3.0 - latitude/longitude (or equidistant cylindrical, or Plate Carrée)
     * <pre>
     * Content No.    Contents
     * 15           Shape of the Earth (see Code table 3.2)
     * 16           Scale factor of radius of spherical Earth
     * 17-20        Scaled value of radius of spherical Earth
     * 21           Scale factor of major axis of oblate spheroid Earth
     * 22-25        Scaled value of major axis of oblate spheroid Earth
     * 26           Scale factor of minor axis of oblate spheroid Earth
     * 27-30        Scaled value of minor axis of oblate spheroid Earth
     * 31-34        Ni - number of points along a parallel
     * 35-38        Nj - number of points along a meridian
     * 39-42        Basic angle of the initial production domain (see Note 1)
     * 43-46        Subdivisions of basic angle used to define extreme longitudes and latitudes,
     *              and direction (see Note 1)
     * 47-50        La1 - latitude of first grid point (see Note 1)
     * 51-54        Lo1 - longitude of first grid point (see Note 1)
     * 55           Resolution and component flags (see Flag table 3.3)
     * 56-59        La2 - latitude of last grid point (see Note 1)
     * 60-63        Lo2 - longitude of last grid point (see Note 1)
     * 64-67        Di - i direction increment (see Note 1)
     * 68-71        Dj - j direction increment (see Note 1)
     * 72           Scanning mode (flags - see Flag table 3.4)
     * 73-nn        List of number of points along each meridian or parallel.
     *              (These octets are only present for quasi-regular grids as described
     *               in Notes 2 and 3)
     * </pre>
     */
    @SuppressWarnings("unused")
    @Assertion(section=GRID_DEFINITION_SECTION_NUMBER, template=0, length=72)
    public interface GridDefinitionTemplate extends Template {
        @Content(offset=15, length=1, value=6) byte shapeOfTheEarth();
        @Content(offset=16, length=1, value=-1) byte scaleFactorOfRadius();
        @Content(offset=17, length=4, value=-1) int scaledValueOfRadius();
        @Content(offset=21, length=1, value=-1) byte scaleFactorOfMajorAxis();
        @Content(offset=22, length=4, value=-1) int scaledValueOfMajorAxis();
        @Content(offset=26, length=1, value=-1) byte scaleFactorOfMinorAxis();
        @Content(offset=27, length=4, value=-1) int scaledValueOfMinorAxis();
        @Content(offset=31, length=4, abbrev="Ni") int numPointsAlongParallel();
        @Content(offset=35, length=4, abbrev="Nj") int numPointsAlongMeridian();
        @Content(offset=39, length=4, value=0) int basicAngle();
        @Content(offset=43, length=4, value=-1) int subdivisionsOfBasicAngle();
        @Content(offset=47, length=4, abbrev="La1") int latitudeOfFirstGrid();
        @Content(offset=51, length=4, abbrev="Lo1") int longtitudeOfFirstGrid();
        @Content(offset=55, length=1, value=0x30) byte resolutionAndComponentFlag();
        @Content(offset=56, length=4, abbrev="La2") int latitudeOfLastGrid();
        @Content(offset=60, length=4, abbrev="Lo2") int longtitudeOfLastGrid();
        @Content(offset=64, length=4, abbrev="Di") int incrementI();
        @Content(offset=68, length=4, abbrev="Dj") int incrementJ();
        @Content(offset=72, length=1, value=0) ScanningMode scanningMode();
    }

    /**
     * Flag table 3.4 - Scanning mode
     * <pre>
     * Bit No.  Value   Meaning
     * 1        0       Points of first row or column scan in the +i (+x) direction
     *          1       Points of first row or column scan in the -i (-x) direction
     * 2        0       Points of first row or column scan in the -j (-y) direction
     *          1       Points of first row or column scan in the +j (+y) direction
     * 3        0       Adjacent points in i (x) direction are consecutive
     *          1       Adjacent points in j (y) direction is consecutive
     * 4        0       All rows scan in the same direction
     *          1       Adjacent rows scans in the opposite direction
     * 5-8              Reserved
     * Notes:
     * (1) i direction: west to east along a parallel or left to right along an x-axis.
     * (2) j direction: south to north along a meridian, or bottom to top along a y-axis.
     * (3) If bit number 4 is set, the first row scan is as defined by previous flags.
     * </pre>
     */
    public static class ScanningMode extends ByteBits {
        /**
         * i direction: west to east along a parallel or left to right along an x-axis.
         * @return +1 or -1
         */
        public int directionI() { return isZero(1) ? +1 : -1; }

        /**
         * j direction: south to north along a meridian, or bottom to top along a y-axis.
         * @return +1 or -1
         * */
        public int directionJ() { return isZero(2) ? -1 : +1; }

        public boolean isIDirectionConsective() { return isZero(3); }

        public boolean areSameDirections() { return isZero(4); }

        public ScanningMode(final int value) { super(value); }
    }



    public static final int PRODUCT_DEFITION_SECTION_NUMBER = 4;

    /**
     * Section 4: Product Definition Section
     * <p>
     * The purpose of the Product Definition Section is to describe the nature of the data contained in
     * the next occurrence of the Data Section. The contents of Section 4 are:
     * <pre>
     * Content No.    Contents
     * 1-4          Length of section in octets (nn)
     * 5            Number of Section (“4”)
     * 6-7          Number of coordinate values after Template (see Note 1)
     * 8-9          Product Definition Template Number (see Code Table 4.0)
     * 10-xx        Product Definition Template (see Template 4.X, where X is the Product
     *              Definition Template Number given in octets 8-9)
     * [xx+1]-nn    Optional list of coordinate values (see Notes 2 and 3)
     * </pre>
     * </p>
     */
    @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, length={34, 37, 58, 61})
    public interface ProductDefinitionSection extends Section {
        @Content(offset=6, length=2, value=0) short numCoordinateValues();
        @Content(offset=8, length=2, type=ProductDefinitionTemplateNumber.class)
                                   EnumValue<ProductDefinitionTemplateNumber> templateNumber();
        @Content(offset=10) ProductDefinitionTemplate template();
    }

    /**
     * Code table 4.0 - Product definition template number
     * <pre>
     * Code figure  Meaning
     * 0            Analysis or forecast at a horizontal level or in a horizontal layer at a point in time
     * 1            Individual ensemble forecast, control and perturbed, at a horizontal level or in a
     *              horizontal layer at a point in time
     * 2            Derived forecasts based on all ensemble members at a horizontal level or in a horizontal
     *              layer at a point in time
     * 3            Derived forecasts based on a cluster of ensemble members over a rectangular area at a
     *              horizontal level or in a horizontal layer at a point in time
     * 4            Derived forecasts based on a cluster of ensemble members over a circular area at a hori-
     *              zontal level or in a horizontal layer at a point in time
     * 5            Probability forecasts at a horizontal level or in a horizontal layer at a point in time
     * 6            Percentile forecasts at a horizontal level or in a horizontal layer at a point in time
     * 7            Analysis or forecast error at a horizontal level or in a horizontal layer at a point in time
     * 8            Average, accumulation, extreme values or other statistically processed values at a hori-
     *              zontal level or in a horizontal layer in a continuous or non-continuous time interval
     * 9            Probability forecasts at a horizontal level or in a horizontal layer in a continuous or
     *              non-continuous time interval
     * 10           Percentile forecasts at a horizontal level or in a horizontal layer in a continuous or
     *              non-continuous time interval
     * 11           Individual ensemble forecast, control and perturbed, at a horizontal level or in a hori-
     *              zontal layer, in a continuous or non-continuous interval
     * 12           Derived forecasts based on all ensemble members at a horizontal level or in a horizontal
     *              layer, in a continuous or non-continuous interval
     * 13           Derived forecasts based on a cluster of ensemble members over a rectangular area, at a
     *              horizontal level or in a horizontal layer, in a continuous or non-continuous interval
     * 14           Derived forecasts based on a cluster of ensemble members over a circular area, at a
     *              horizontal level or in a horizontal layer, in a continuous or non-continuous interval
     * 15           Average, accumulation, extreme values, or other statistically-processed values over a
     *              spatial area at a horizontal level or in a horizontal layer at a point in time
     * and others...
     * <pre>
     */
    public enum ProductDefinitionTemplateNumber
            implements TemplateNumber<ProductDefinitionTemplate>, EnumValue<ProductDefinitionTemplateNumber> {
        ANALYSYS_OR_FORECAST(0, ProductDefinitionTemplate.Forecast.class),
        ENSEMBLE_POINT_IN_TIME(1, ProductDefinitionTemplate.PointInTimeEnsembleForecast.class),
        AVERAGE(8, ProductDefinitionTemplate.Average.class),
        ENSEMBLE_TIME_INTERVAL(11, ProductDefinitionTemplate.TimeIntervalEnsembleForecast.class),
        DERIVED_ENSEMBLE_TIME_INTERVAL(12, ProductDefinitionTemplate.DerivedTimeIntervalEnsembleForecast.class),
        ;

        private final int value_;
        private final Class<? extends ProductDefinitionTemplate> templateType_;

        private ProductDefinitionTemplateNumber(final int value,
                                                final Class<? extends ProductDefinitionTemplate> type) {
            this.value_ = value;
            this.templateType_ = type;
        }

        @Override public int intValue() { return value_; }
        @Override public Class<? extends ProductDefinitionTemplate> templateType() { return templateType_; }
        @Override public ProductDefinitionTemplateNumber enumValue() { return this; }
        public static ProductDefinitionTemplateNumber valueOf(final int value) {
            return EnumValues.valueOf(ProductDefinitionTemplateNumber.class, value);
        }
    }

    /**
     * Product definition template 4.0 - analysis or forecast at a horizontal level or in a horizontal layer at a point in time
     * <pre>
     * Content No.    Contents
     * 10           Parameter category (see Code table 4.1)
     * 11           Parameter number (see Code table 4.2)
     * 12           Type of generating process (see Code table 4.3)
     * 13           Background generating process identifier (defined by originating centre)
     * 14           Analysis or forecast generating process identifier (defined by originating centre)
     * 15-16        Hours of observational data cutoff after reference time (see Note)
     * 17           Minutes of observational data cutoff after reference time
     * 18           Indicator of unit of time range (see Code table 4.4)
     * 19-22        Forecast time in units defined by octet 18
     * 23           Type of first fixed surface (see Code table 4.5)
     * 24           Scale factor of first fixed surface
     * 25-28        Scaled value of first fixed surface
     * 29           Type of second fixed surface (see Code table 4.5)
     * 30           Scale factor of second fixed surface
     * 31-34        Scaled value of second fixed surface
     * </pre>
     */
    @SuppressWarnings("unused")
    public interface ProductDefinitionTemplate extends Template {
        @Content(offset=10, length=1, type=ParameterCategory.class)
                                    EnumValue<ParameterCategory> parameterCategory();
        @Content(offset=11, length=1, type=ParameterNumber.class)
                                    EnumValue<ParameterNumber> parameterNumber();
        @Content(offset=12, length=1, type=GeneratingProcessType.class)
                                    EnumValue<GeneratingProcessType> generatingProcessType();
        @Content(offset=13, length=1) byte backgroundGenerationProcessId();
        @Content(offset=14, length=1, value=-1) byte forcastGenerationProcessId();
        @Content(offset=15, length=2) short cutoffHours();
        @Content(offset=17, length=1) byte cutoffMinutes();
        @Content(offset=18, length=1, type=UnitOfTimeRange.class)
                                    EnumValue<UnitOfTimeRange> unitOfForecastTime();
        @Content(offset=19, length=4) int forecastTime();
        @Content(offset=23, length=1, type=SurfaceType.class)
                                    EnumValue<SurfaceType> firstFixedSurfaceType();
        @Content(offset=24, length=1) byte scaleFactorOfFirstFixedSurface();
        @Content(offset=25, length=4) int scaledValueOfFirstFixedSurface();
        @Content(offset=29, length=1, value=-1) byte secondFixedSurfaceType();
        @Content(offset=30, length=1, value=-1) byte scaleFactorOfSecondFixedSurface();
        @Content(offset=31, length=4, value=-1) int scaledValueOfSecondFixedSurface();

        @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, template=0, length=34)
        public interface Forecast extends ProductDefinitionTemplate {
            // no field
        }

        /**
         * Product definition template 4.1 - individual ensemble forecast, control and perturbed,
         * at a horizontal level or in a horizontal layer at a point in time
         * <pre>
         * Content No.    Contents
         * 35           Type of ensemble forecast (see Code table 4.6)
         * 36           Perturbation number
         * 37           Number of forecasts in ensemble
         * </pre>
         */
        @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, template=1, length=37)
        public interface PointInTimeEnsembleForecast extends ProductDefinitionTemplate {
            @Content(offset=35, length=1, type=EnsembleForecastType.class)
                                        EnumValue<EnsembleForecastType> ensembleForcastType();
            @Content(offset=36, length=1) byte perturbationNumber();
            @Content(offset=37, length=1) byte numForcasts();
        }

        /**
         * Product definition template 4.8 - average, accumulation and/or extreme values or
         * other statistically-processed values at a horizontal level or in a horizontal layer
         * in a continuous or non-continuous time interval
         * <pre>
         * Content No.    Contents
         * 35-36        Year   |
         * 37           Month  |
         * 38           Day    | time of end of overall time interval
         * 39           Hour   |
         * 40           Minute |
         * 41           Second |
         * 42           n - number of time range specifications describing the time intervals used to calculate the statistically-processed field
         * 43-46        Total number of data values missing in statistical process
         *      (47-58 Specification of the outermost (or only) time range over which statistical processing is done)
         * 47           Statistical process used to calculate the processed field from the field at each time increment during the time range (see Code table 4.10)
         * 48           Type of time increment between successive fields used in the statistical processing (see Code table 4.11)
         * 49           Indicator of unit of time for time range over which statistical processing is done (see Code table 4.4)
         * 50-53        Length of the time range over which statistical processing is done, in units defined by the previous octet
         * 54           Indicator of unit of time for the increment between the successive fields used (see Code table 4.4)
         * 55-58        Time increment between successive fields, in units defined by the previous octet (see Notes 3 and 4)
         * 59-nn        These octets are included only if n>1, where nn = 46 + 12 x n
         * 59-70        As octets 47 to 58, next innermost step of processing
         * 71-nn        Additional time range specifications, included in accordance with the value of n. Contents as octets 47 to 58, repeated as necessary
         * </pre>
         */
        @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, template=8, length=58)
        public interface Average extends ProductDefinitionTemplate {
            @Content(offset=35, length=2) short year();
            @Content(offset=37, length=1) byte month();
            @Content(offset=38, length=1) byte day();
            @Content(offset=39, length=1) byte hour();
            @Content(offset=40, length=1) byte minute();
            @Content(offset=41, length=1) byte second();
            @Content(offset=42, length=1, abbrev="n") byte numTimeRange();
            @Content(offset=43, length=4) int numMissingDataValues();
            @Content(offset=47, length=1, type=StatisticalProcessingType.class)
                            EnumValue<StatisticalProcessingType> statisticalProcessingType();
            @Content(offset=48, length=1, type=TimeIntervalsType.class)
                            EnumValue<TimeIntervalsType> timeIntervalsType();
            @Content(offset=49, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeRangeLength();
            @Content(offset=50, length=4) int timeRangeLength();
            @Content(offset=54, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeIncrement();
            @Content(offset=55, length=4) int timeIncrement();
        }

        /**
         * Product definition template 4.11 - individual ensemble forecast, control and perturbed,
         * at a horizontal level or in a horizontal layer in a continuous or non-continuous time interval
         * <pre>
         * Content No.    Contents
         * 38-39        Year of end of overall time interval
         * 40           Month of end of overall time interval
         * 41           Day of end of overall time interval
         * 42           Hour of end of overall time interval
         * 43           Minute of end of overall time interval
         * 44           Second of end of overall time interval
         * 45           n - number of time range specifications describing the time intervals used to calculate the statistically-processed field
         * 46-49        Total number of data values missing in statistical process
         *      (50-61 Specification of the outermost (or only) time range over which statistical processing is done)
         * 50           Statistical process used to calculate the processed field from the field at each time increment during the time range (see Code table 4.10)
         * 51           Type of time increment between successive fields used in the statistical processing (see Code table 4.11)
         * 52           Indicator of unit of time for time range over which statistical processing is done (see Code table 4.4)
         * 53-56 Length of the time range over which statistical processing is done, in units defined by the previous octet
         * 57           Indicator of unit of time for the increment between the successive fields used (see Code table 4.4)
         * 58-61        Time increment between successive fields, in units defined by the previous octet (see Note 3)
         * 62-nn        These octets are included only if n>1, where nn = 49 + 12 x n
         * 62-73        As octets 50 to 61, next innermost step of processing
         * 74-nn        Additional time range specifications, included in accordance with the value of n. Contents as octets 50 to 61, repeated as necessary
         * </pre>
         */
        @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, template=11, length=61)
        public interface TimeIntervalEnsembleForecast extends PointInTimeEnsembleForecast {
            @Content(offset=38, length=2) short year();
            @Content(offset=40, length=1) byte month();
            @Content(offset=41, length=1) byte day();
            @Content(offset=42, length=1) byte hour();
            @Content(offset=43, length=1) byte minute();
            @Content(offset=44, length=1) byte second();
            @Content(offset=45, length=1, abbrev="n") byte numTimeRange();
            @Content(offset=46, length=4) int numMissingDataValues();
            @Content(offset=50, length=1, type=StatisticalProcessingType.class)
                            EnumValue<StatisticalProcessingType> statisticalProcessingType();
            @Content(offset=51, length=1, type=TimeIntervalsType.class)
                            EnumValue<TimeIntervalsType> timeIntervalsType();
            @Content(offset=52, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeRangeLength();
            @Content(offset=53, length=4) int timeRangeLength();
            @Content(offset=57, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeIncrement();
            @Content(offset=58, length=4) int timeIncrement();
        }


        /**
         * Product definition template 4.12 - derived forecasts based on all ensemble members
         * at a horizontal level or in a horizontal layer in a continuous or non-continuous time interval
         *
         * Product definition template 4.11 - individual ensemble forecast, control and perturbed,
         * at a horizontal level or in a horizontal layer in a continuous or non-continuous time interval
         * <pre>
         * Content No.    Contents
         * 35 Derived forecast (see Code table 4.7) Operational
         * 36 Number of forecasts in the ensemble (N) Operational
         * 37-38 Year of end of overall time interval Operational
         * 39 Month of end of overall time interval Operational
         * 40 Day of end of overall time interval Operational
         * 41 Hour of end of overall time interval Operational
         * 42 Minute of end of overall time interval Operational
         * 43 Second of end of overall time interval Operational
         * 44 n - number of time range specifications describing the time intervals used to calculate the Operational statistically-processed field
         * 45-48 Total number of data values missing in statistical process Operational
         * 49-60 Specification of the outermost (or only) time range over which statistical Operational processing is done
         * 49 Statistical process used to calculate the processed field from the field at each time incre- Operational ment during the time range (see Code table 4.10)
         * 50 Type of time increment between successive fields used in the statistical processing (see Operational Code table 4.11)
         * 51 Indicator of unit of time for time range over which statistical processing is done (see Code Operational table 4.4)
         * 52-55 Length of the time range over which statistical processing is done, in units defined by the Operational previous octet
         * 56 Indicator of unit of time for the increment between the successive fields used (see Code Operational table 4.4)
         * 57-60 Time increment between successive fields, in units defined by the previous octet (see Notes Operational 3 and 4)
         * 61-nn These octets are included only if n>1, where nn = 48 + 12 x n Operational
         * 61-72 As octets 49 to 60, next innermost step of processing Operational
         * 73-nn Additional time range specifications, included in accordance with the value of n. Contents Operational as octets 49 to 60, repeated as necessary
         * </pre>
         */
        @Assertion(section=PRODUCT_DEFITION_SECTION_NUMBER, template=12, length=60)
        public interface DerivedTimeIntervalEnsembleForecast extends ProductDefinitionTemplate {
            @Content(offset=35, length=1, type=EnsembleForecastType.class)
                            EnumValue<EnsembleForecastType> ensembleForcastType();
            @Content(offset=36, length=1) byte numForcasts();
            @Content(offset=37, length=2) short year();
            @Content(offset=39, length=1) byte month();
            @Content(offset=40, length=1) byte day();
            @Content(offset=41, length=1) byte hour();
            @Content(offset=42, length=1) byte minute();
            @Content(offset=43, length=1) byte second();
            @Content(offset=44, length=1, abbrev="n") byte numTimeRange();
            @Content(offset=45, length=4) int numMissingDataValues();
            @Content(offset=49, length=1, type=StatisticalProcessingType.class)
                            EnumValue<StatisticalProcessingType> statisticalProcessingType();
            @Content(offset=50, length=1, type=TimeIntervalsType.class)
                            EnumValue<TimeIntervalsType> timeIntervalsType();
            @Content(offset=51, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeRangeLength();
            @Content(offset=52, length=4) int timeRangeLength();
            @Content(offset=56, length=1, type=UnitOfTimeRange.class)
                            EnumValue<UnitOfTimeRange> unitOfTimeIncrement();
            @Content(offset=57, length=4) int timeIncrement();
        }
    }

    /**
     * Code table 4.1 - Parameter category by product discipline
     */
    public enum ParameterCategory implements EnumValue<ParameterCategory> {
        TEMPERATURE(MasterTableCode.METEOROLOGICAL, 0),
        MOISTURE(MasterTableCode.METEOROLOGICAL, 1),
        MOMENTUM(MasterTableCode.METEOROLOGICAL, 2),
        MASS(MasterTableCode.METEOROLOGICAL, 3),
        CLOUD(MasterTableCode.METEOROLOGICAL, 6),

        WAVES(MasterTableCode.OCEANOGRAPHIC, 0),
        ;

        private ParameterCategory(final MasterTableCode product, final int value) {
            this.product_ = product;
            this.value_ = calcValue(product, value);
        }
        private final MasterTableCode product_;
        public MasterTableCode product() { return product_; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public ParameterCategory enumValue() { return this; }

        private static int calcValue(final MasterTableCode product, final int value) {
            assert value >= 0 && value < 0x100 : product+","+value;
            return product.intValue() << 8 | (value & 0xff);
        }

        public int categoryNumber() {
            return value_ & 0xff;
        }

        public static ParameterCategory valueOf(final MasterTableCode product, final int value) {
            return EnumValues.valueOf(ParameterCategory.class, calcValue(product, value));
        }
    }

    /**
     * Code table 4.2 - Parameter number by product discipline and parameter category
     */
    public enum ParameterNumber implements EnumValue<ParameterNumber> {
        // Product discipline 0 - Meteorological products, parameter category 0: temperature
        TEMPERATURE("TMP", ParameterCategory.TEMPERATURE, 0, "K"),
        ANOMALY_TEMPERATURE("TMPA", ParameterCategory.TEMPERATURE, 9, "K"),

        // Product discipline 0 - Meteorological products, parameter category 1: moisture
        RELATIVE_HUMIDITY("RH", ParameterCategory.MOISTURE, 1, "%"),
        TOTAL_PRECIPITATION("TP", ParameterCategory.MOISTURE, 8, "kg/m^2"),
        RESERVED_MOISTURE_210("MS201", ParameterCategory.TEMPERATURE, 210, "???"),

        WIND_U("UGRD", ParameterCategory.MOMENTUM, 2, "m/s"),
        WIND_V("VGRD", ParameterCategory.MOMENTUM, 3, "m/s"),
        VERTICAL_PRESSURE_VELOCITY("VVEL", ParameterCategory.MOMENTUM, 8, "Pa/s"),

        PRESSURE("PRES", ParameterCategory.MASS, 0, "Pa"),
        SEA_LEVEL_PRESSURE("PRMSL", ParameterCategory.MASS, 1, "Pa"),
        GEOPOTENTIAL_HEIGHT("HGT", ParameterCategory.MASS, 5, "gpm"),
        ANOMALY_PRESSURE("PRESA", ParameterCategory.MASS, 8, "Pa"),
        ANOMALY_GEOPOTENTIAL_HEIGHT("HGTA", ParameterCategory.MASS, 9, "gpm"),

        TOTAL_CLOUD_COVER("TCDC", ParameterCategory.CLOUD, 1, "%"),
        LOW_CLOUD_COVER("LCDC", ParameterCategory.CLOUD, 3, "%"),
        MEDIUM_CLOUD_COVER("MCDC", ParameterCategory.CLOUD, 4, "%"),
        HIGHT_CLOUD_COVER("HCDC", ParameterCategory.CLOUD, 5, "%"),

        WIND_WAVES_AND_SWELL("HSTGW", ParameterCategory.WAVES, 3, "m"),
        PRIMARY_WAVE_DIRECTION("DIRPW", ParameterCategory.WAVES, 10, "°"),
        PRIMARY_WAVE_MEAN_PERIOD("PERPW", ParameterCategory.WAVES, 11, "s"),
        ;

        private ParameterNumber(final String abbrev,
                                final ParameterCategory category, final int value,
                                final String unit) {
            this.abbrev_ = abbrev;
            this.category_ = category;
            this.value_ = calcValue(category, value);
            this.unit_ = unit;
        }
        private final String abbrev_;
        public String abbrev() { return abbrev_; }
        private final ParameterCategory category_;
        public ParameterCategory category() { return category_; }
        private final int value_;
        @Override public int intValue() { return value_; }
        private final String unit_;
        public String unit() { return unit_; }
        @Override public ParameterNumber enumValue() { return this; }

        private static int calcValue(final ParameterCategory category, final int value) {
            assert value >= 0 && value < 0x100 : category+","+value;
            return category.intValue() << 8 | (value & 0xff);
        }

        public int parameterNumber() {
            return value_ & 0xff;
        }

        public static ParameterNumber valueOf(final ParameterCategory category, final int value) {
            return EnumValues.valueOf(ParameterNumber.class, calcValue(category, value));
        }


        private static final Map<String,ParameterNumber> abbrevs_ = new HashMap();

        public static ParameterNumber forName(final String name) {
            if (abbrevs_.isEmpty()) {
                for (ParameterNumber e: values()) {
                    abbrevs_.put(e.abbrev_, e);
                }
            }
            ParameterNumber e = abbrevs_.get(name);
            if (e == null) e = Enum.valueOf(ParameterNumber.class, name);
            return e;
        }
    }

    /**
     * Code table 4.3 - Type of generating process
     */
    public enum GeneratingProcessType implements EnumValue<GeneratingProcessType> {
        //ANALYSIS(0),
        INITIALIZATION(1),
        FORECAST(2),
        ASSEMBLE_FORECAST(4),
        ;

        private GeneratingProcessType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public GeneratingProcessType enumValue() { return this; }
        public static GeneratingProcessType valueOf(final int value) {
            return EnumValues.valueOf(GeneratingProcessType.class, value);
        }
    }

    /**
     * Code table 4.4 - Indicator of unit of time range
     */
    public enum UnitOfTimeRange implements EnumValue<UnitOfTimeRange> {
        HOUR(1),
        DAY(2),
        SIX_HOURS(11),
        ;

        private UnitOfTimeRange(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public UnitOfTimeRange enumValue() { return this; }
        public static UnitOfTimeRange valueOf(final int value) {
            return EnumValues.valueOf(UnitOfTimeRange.class, value);
        }
    }

    /**
     * Code table 4.5 - Fixed surface types and units
     */
    public enum SurfaceType implements EnumValue<SurfaceType> {
        GROUND(1, null, "surface"),
        ISOBARIC_SURFACE(100, "Pa", ""),
        SEA_LEVEL(101, null, "mean sea level"),
        HEIGHT_LEVEL(103, "m", "above ground"),
        ;

        private SurfaceType(final int value, final String unit, final String desc) {
            this.value_ = value;
            this.unit_ = unit;
            this.description_ = desc;
        }
        private final int value_;
        @Override public int intValue() { return value_; }
        private final String unit_;
        public String unit() { return unit_; }
        private final String description_;
        public String description() { return description_; }
        @Override public SurfaceType enumValue() { return this; }
        public static SurfaceType valueOf(final int value) {
            return EnumValues.valueOf(SurfaceType.class, value);
        }
    }

    /**
     * Code table 4.6 - Type of ensemble forecast
     */
    public enum EnsembleForecastType implements EnumValue<EnsembleForecastType> {
        UNPERTURBED_HIGH_RESOLUTION(0),
        UNPERTURBED_LOW_RESOLUTION(1),
        NEGATIVELY_PERTURBED(2),
        POSITIVELY_PERTURBED(3),
        MULTI_MODEL(4),
        RESERVED_5(5),
        ;

        private EnsembleForecastType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public EnsembleForecastType enumValue() { return this; }
        public static EnsembleForecastType valueOf(final int value) {
            return EnumValues.valueOf(EnsembleForecastType.class, value);
        }
    }

    /**
     * Code table 4.10 - Type of statistical processing
     */
    public enum StatisticalProcessingType implements EnumValue<StatisticalProcessingType> {
        AVERAGE(0),
        ACCUMULATION(1),
        //MAXIMUM(2),
        //MINIMUM(3),
        //DIFFERENCE_END_MINUS_START(4),
        //ROOT_MEAN_SQUARE(5),
        //STANDARD_DEVIATION(6),
        //COVARIANCE(7),
        //DIFFERENCE_START_MINUS_END(8),
        //RATIO(9),
        ;

        private StatisticalProcessingType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public StatisticalProcessingType enumValue() { return this; }
        public static StatisticalProcessingType valueOf(final int value) {
            return EnumValues.valueOf(StatisticalProcessingType.class, value);
        }
    }

    /**
     * Code table 4.11 - Type of time intervals
     * <pre>
     * 1        Successive times processed have same forecast time,
     *          start time of forecast is incremented
     * 2        Successive times processed have same start time of forecast,
     *          forecast time is incremented
     * 3        Successive times processed have start time of forecast incremented
     *          and forecast time decremented so that valid time remains constant
     * 4        Successive times processed have start time of forecast decremented
     *          and forecast time incremented so that valid time remains constant
     * 5        Floating subinterval of time between forecast time and end of
     *          overall time interval
     * </pre>
     */
    public enum TimeIntervalsType implements EnumValue<TimeIntervalsType> {
        //INCREMENTED_START_TIME(1),
        INCREMENTED_FORECAST_TIME(2),
        ;

        private TimeIntervalsType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public TimeIntervalsType enumValue() { return this; }
        public static TimeIntervalsType valueOf(final int value) {
            return EnumValues.valueOf(TimeIntervalsType.class, value);
        }
    }



    public static final int DATA_REPRESENTAITON_SECTION_NUMBER = 5;

    /**
     * 2.1.6 Section 5: Data Representation Section
     * <p>
     * The purpose of the Data Representation Section is to describe how the data values are
     * represented in the next occurrence of the Data Section. The contents of Section 5 are:
     * <pre>
     * Content No. Contents
     * 1-4 Length of section in octets (nn)
     * 5 Number of Section (“5”)
     * 6-9 Number of data points where one or more values are specified in Section 7
     * when a bit map is present, total number of data points when a bit map is
     * absent
     * 10-11 Data Representation Template Number (see code Table 5.0)
     * 12-nn Data Representation Template (see Template 5.X, where X is the Data
     * Representation Template Number given in octets 10-11)
     * </pre>
     * The pattern taken by the contents of a section that uses a template may be becoming familiar by
     * now. In this Section, the special case noted in the description of the contents of octets 6 - 9 has to
     * do with the use of a bit map. The value in octets 6 - 9 indicates to the user how many data point
     * values are to be found in the Data Section. However, when a bit map is used (this is discussed in
     * Section 2.1.7), the number of data point values to be found in the data Section (given by octets 6 -
     * 9 of the Data Representation Section) may be smaller than the number. The number of data point
     * values to be found in the data Section (given by octets 6 - 9 of the Data Representation Section)
     * may therefore be fewer than the number of data points themselves (given by octets 7 - 10 of the
     * Grid Definition Section).
     */
    @Assertion(section=DATA_REPRESENTAITON_SECTION_NUMBER, length=21)
    public interface DataRepresentationSection extends Section {
        @Content(offset=6, length=4) int numDataPoints();
        @Content(offset=10, length=2, type=DataRepresentationTemplateNumber.class, value=0)
                                    EnumValue<DataRepresentationTemplateNumber> templateNumber();
        @Content(offset=12) DataRepresentationTemplate template();
    }

    public enum DataRepresentationTemplateNumber
            implements TemplateNumber<DataRepresentationTemplate>, EnumValue<DataRepresentationTemplateNumber> {
        SIMPLE_GRID(0, DataRepresentationTemplate.class),
        ;

        private final int value_;
        private final Class<? extends DataRepresentationTemplate> templateType_;

        private DataRepresentationTemplateNumber(final int value,
                                                 final Class<? extends DataRepresentationTemplate> type) {
            this.value_ = value;
            this.templateType_ = type;
        }

        @Override public int intValue() { return value_; }
        @Override public Class<? extends DataRepresentationTemplate> templateType() { return templateType_; }
        @Override public DataRepresentationTemplateNumber enumValue() { return this; }
        public static DataRepresentationTemplateNumber valueOf(final int value) {
            return EnumValues.valueOf(DataRepresentationTemplateNumber.class, value);
        }
    }

    @Assertion(section=DATA_REPRESENTAITON_SECTION_NUMBER, template=0, length=21)
    public interface DataRepresentationTemplate extends Template {
        @Content(offset=12, length=4, abbrev="R") float referenceValue();
        @Content(offset=16, length=2, abbrev="E") short binaryScaleFactor();
        @Content(offset=18, length=2, abbrev="D") short decimalScaleFactor();
        @Content(offset=20, length=1, value={12,16}) byte numBits();
        @Content(offset=21, length=1, type=FieldValueType.class, value=0)
                                    EnumValue<FieldValueType> originalFieldValuesType();
    }

    public enum FieldValueType implements EnumValue<FieldValueType> {
        FLOAT(0),
        //INTEGER(1),
        ;

        private FieldValueType(final int value) { this.value_ = value; }
        private final int value_;
        @Override public int intValue() { return value_; }
        @Override public FieldValueType enumValue() { return this; }
        public static FieldValueType valueOf(final int value) {
            return EnumValues.valueOf(FieldValueType.class, value);
        }
    }



    public static final int BITMAP_SECTION_NUMBER = 6;

    /**
     * 2.1.7 Section 6: Bit-Map Section
     * <p>
     * The purpose of the Bit-Map Section is to indicate the presence or absence of data at each of the
     * grid points, as applicable, in the next occurrence of the Data Section. The contents of Section 6
     * are:
     * <pre>
     * Content No. Contents
     * 1-4 Length of section in octets (nn)
     * 5 Number of Section (“6”)
     * 6 Bit-map indicator (see code Table 6.0 and Note 1)
     * 7-nn Bit-map - Contiguous bits with a bit to data point correspondence, ordered as
     * defined in Section 3. A bit set equal to 1 implies the presence of a data value
     * at the corresponding data point, whereas a value of 0 implies the absence of
     * such a value.
     * </pre>
     * <p>
     */
    @Assertion(section=BITMAP_SECTION_NUMBER)
    public interface BitmapSection extends Section {
        @Content(offset=6, length=1) byte bitmapIndicator();
        @Content(offset=7) byte[] bitmap();
    }



    public static final int DATA_SECTION_NUMBER = 7;

    /**
     * 2.1.8 Section 7: Data Section
     * <p>
     * The Data Section contains the data values themselves. The contents of Section 7 are:
     * <pre>
     * Content No. Contents
     * 1-4 Length of section in octets (nn)
     * 5 Number of Section (“7”)
     * 6-nn Data in a format described by Data Template 7.X, where X is the Data
     * Representation Template number given in octets 10-11 of Section 5
     * </pre>
     * <p>
     */
    @Assertion(section=DATA_SECTION_NUMBER)
    public interface DataSection extends Section {
        @Content(offset=6) byte[] data();
    }



    public static final int END_SECTION_NUMBER = 8;
    public static final int END_SECTION_LENGTH = 4;
    public static final int END_SECTION_MARKER = 0x37373737; // "7777"

    /**
     * 2.1.9 Section 8: End Section
     * <p>
     * The End Section serves to identify the end of the GRIB2 message in a human readable form. The
     * contents of Section 7 are:
     * <pre>
     * Content No. Contents
     * 1-4 “7777” (coded according to the International Alphabet No. 5)
     * The End Section is always 4 octets long. The Indicator (16 octets long) and End Sections are the
     * only fixed-length sections in a GRIB2 message.
     * </pre>
     * </p>
     */
    @Assertion(section=END_SECTION_NUMBER, length=END_SECTION_LENGTH)
    public interface EndSection extends Section {
        @Content(offset=1, length=4, value=END_SECTION_MARKER) String marker();
    }

}
