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
package jp.techlier.weather.gpv.tools;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparison.*;

import java.util.ArrayList;
import java.util.List;

import jp.techlier.weather.gpv.GpvFileParser;
import jp.techlier.weather.gpv.GpvFileType;
import jp.techlier.weather.gpv.grib2.Grib2.BitmapSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.DataSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataType;
import jp.techlier.weather.gpv.grib2.Grib2.EndSection;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionSection;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.IdentificationSection;
import jp.techlier.weather.gpv.grib2.Grib2.IndicatorSection;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionSection;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionTemplateNumber;
import jp.techlier.weather.gpv.grib2.Grib2.SurfaceType;
import jp.techlier.weather.gpv.grib2.parser.MessageHolder;
import jp.techlier.weather.gpv.grib2.parser.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/17
 * @version $Id$
 */
public class GpvFileValidator implements MessageListener {

    private final Log logger_ = LogFactory.getLog(this.getClass());

    private GpvFileType fileType = GpvFileType.UNKNOWN;

    public void setFileTypeByFilename(final String filename) {
        this.fileType = GpvFileType.getFileType(filename);
        logger_.info(filename + ": " + fileType);
    }

    public void setFileType(final GpvFileType type) {
        this.fileType = type;
    }

    public GpvFileType getFileType() {
        return this.fileType;
    }


    @Override
    public void indicatorSection(final IndicatorSection section, final MessageHolder messages) {
        logger_.info(section);
        if (messages instanceof GpvFileParser) {
            setFileTypeByFilename(((GpvFileParser)messages).getFile().getName());
        }
    }


    @Override
    public void identificationSection(final IdentificationSection section, final MessageHolder messages) {
        logger_.info(section);
        switch (fileType) {
          case GSM_GLOBAL:
          case GSM_JP_SURF:
          case GSM_JP_PALL:
          case MSM_JP_SURF:
          case MSM_JP_PALL:
          case EPSW_GLOBAL:
          case EPSW_JP:
          case GWM_GLOBAL:
          case CWM_JP:
            validate("dataType", section.dataType(), is(DataType.FORECAST));
            break;
          case EPS1_GLOBAL:
          case EPS1_MGPV_GLOBAL:
            validate("dataType", section.dataType(), is(DataType.CONTROL_AND_PERTURBED));
            break;
          case UNKNOWN:
            break;
        }
    }

    @Override
    @SuppressWarnings("boxing")
    public void gridDefinitionSection(final GridDefinitionSection section, final MessageHolder message) {
        logger_.info(section);

        GridDefinitionTemplate template = section.template();
        int numLng = template.numPointsAlongParallel(); // Ni
        int numLat = template.numPointsAlongMeridian(); // Nj
        int latFirst = template.latitudeOfFirstGrid(); // La1
        int lngFirst = template.longtitudeOfFirstGrid(); // Lo1
        validate("resolutionAndComponentFlag", template.resolutionAndComponentFlag(), is((byte)0x30));
        int latLast = template.latitudeOfLastGrid(); // La2
        int lngLast = template.longtitudeOfLastGrid(); // Lo2
        int dLng = template.incrementI() * template.scanningMode().directionI(); // Di
        int dLat = template.incrementJ() * template.scanningMode().directionJ(); // Dj
        validate("scanningMode", template.scanningMode().intValue(), is(0));
        validate("directionI", template.scanningMode().directionI(), is(1));
        validate("directionJ", template.scanningMode().directionJ(), is(-1));

        switch (fileType) {
          case GSM_GLOBAL:
            validate("Ni", numLng, is(anyOf(720, 360)));
            if (numLng == 720) {
                validate("Ni", numLng, is(720));
                validate("Nj", numLat, is(361));
                validate("La1", latFirst, is(90000000));
                validate("Lo1", lngFirst,  is(0000000));
                validate("La2", latLast, is(-90000000));
                validate("Lo2", lngLast, is(359500000));
                validate("Di", template.incrementI(), is(500000));
                validate("Dj", template.incrementJ(), is(500000));
            }
            else {
                validate("Ni", numLng, is(360));
                validate("Nj", numLat, is(181));
                validate("La1", latFirst, is(90000000));
                validate("Lo1", lngFirst,  is(0000000));
                validate("La2", latLast, is(-90000000));
                validate("Lo2", lngLast, is(359000000));
                validate("Di", template.incrementI(), is(1000000));
                validate("Dj", template.incrementJ(), is(1000000));
            }
            break;

          case GWM_GLOBAL:
            validate("Ni", numLng, is(720));
            validate("Nj", numLat, is(301));
            validate("La1", latFirst, is(75000000));
            validate("Lo1", lngFirst,  is(0000000));
            validate("La2", latLast, is(-75000000));
            validate("Lo2", lngLast, is(359500000));
            validate("Di", template.incrementI(), is(500000));
            validate("Dj", template.incrementJ(), is(500000));
            break;

          case EPSW_GLOBAL:
            validate("Ni", numLng, is(144));
            validate("Nj", numLat, is(73));
            validate("La1", latFirst, is(90000000));
            validate("Lo1", lngFirst,  is(0000000));
            validate("La2", latLast, is(-90000000));
            validate("Lo2", lngLast, is(357500000));
            validate("Di", template.incrementI(), is(2500000));
            validate("Dj", template.incrementJ(), is(2500000));
            break;

          case GSM_JP_SURF:
          case GSM_JP_PALL:
            validate("Ni", numLng, is(121));
            validate("Nj", numLat, is(151));
            validate("La1", latFirst,  is(50000000));
            validate("Lo1", lngFirst, is(120000000));
            validate("La2", latLast,   is(20000000));
            validate("Lo2", lngLast,  is(150000000));
            validate("Di", template.incrementI(), is(250000));
            validate("Dj", template.incrementJ(), is(200000));
            break;

          case MSM_JP_SURF:
            validate("Ni", numLng, is(481));
            validate("Nj", numLat, is(505));
            validate("La1", latFirst,  is(47600000));
            validate("Lo1", lngFirst, is(120000000));
            validate("La2", latLast,   is(22400000));
            validate("Lo2", lngLast,  is(150000000));
            validate("Di", template.incrementI(), is(62500));
            validate("Dj", template.incrementJ(), is(50000));
            break;

          case MSM_JP_PALL:
            validate("Ni", numLng, is(241));
            validate("Nj", numLat, is(253));
            validate("La1", latFirst,  is(47600000));
            validate("Lo1", lngFirst, is(120000000));
            validate("La2", latLast,   is(22400000));
            validate("Lo2", lngLast,  is(150000000));
            validate("Di", template.incrementI(), is(125000));
            validate("Dj", template.incrementJ(), is(100000));
            break;

          case CWM_JP:
            validate("Ni", numLng, is(601));
            validate("Nj", numLat, is(601));
            validate("La1", latFirst,  is(50000000));
            validate("Lo1", lngFirst, is(120000000));
            validate("La2", latLast,   is(20000000));
            validate("Lo2", lngLast,  is(150000000));
            validate("Di", template.incrementI(), is(50000));
            validate("Dj", template.incrementJ(), is(50000));
            break;

          case EPSW_JP:
            validate("Ni", numLng, is(73));
            validate("Nj", numLat, is(40));
            validate("La1", latFirst,  is(71250000));
            validate("Lo1", lngFirst,  is(90000000));
            validate("La2", latLast,   is(22500000));
            validate("Lo2", lngLast,  is(180000000));
            validate("Di", template.incrementI(), is(1250000));
            validate("Dj", template.incrementJ(), is(1250000));
            break;

          default:
            logger_.error("unknwon filetype");
        }

        validate("numPoints",
                 template.numPointsAlongParallel() * template.numPointsAlongMeridian(),
                 is(section.numPoints()));
        validate("latitudeOfLastGrid", latFirst, latLast, numLat, dLat);
        validate("longtitudeOfLastGrid", lngFirst, lngLast, numLng, dLng);
    }

    private void validate(final String name, final int from, final int to, final int count, final int delta) {
        int actual = from + (count - 1) * delta;
        if (to != actual) {
            logger_.error(String.format("%s: <%d+(%d-1)*(%d)> expected: is <%d> but: <%d>",
                                        name, from, count, delta, to, actual));
        }
    }


    @Override
    public void productDefinitionSection(final ProductDefinitionSection section, final MessageHolder messages) {
        logger_.info(section);

        ProductDefinitionTemplate template = section.template();
        switch (fileType) {
          case GSM_JP_SURF:
            validate("surfaceType", template.firstFixedSurfaceType(), is(not(SurfaceType.ISOBARIC_SURFACE)));
            break;
          case MSM_JP_SURF:
            validate("templateNumber", section.templateNumber(),
                     is(anyOf(ProductDefinitionTemplateNumber.ANALYSYS_OR_FORECAST,
                              ProductDefinitionTemplateNumber.AVERAGE)));
            validate("surfaceType", template.firstFixedSurfaceType(), is(not(SurfaceType.ISOBARIC_SURFACE)));
            break;
          case GSM_JP_PALL:
            validate("surfaceType", template.firstFixedSurfaceType(), is(SurfaceType.ISOBARIC_SURFACE));
            break;
          case MSM_JP_PALL:
            validate("templateNumber", section.templateNumber(),
                     is(ProductDefinitionTemplateNumber.ANALYSYS_OR_FORECAST));
            validate("surfaceType", template.firstFixedSurfaceType(), is(SurfaceType.ISOBARIC_SURFACE));
            break;
          default:
            break;
        }
        validate("scaleFactor", template.scaleFactorOfFirstFixedSurface(),
                 is(allOf(lessThanOrEqualTo((byte)1), greaterThanOrEqualTo((byte)-2))));
    }

    @Override
    public void dataRepresentationSection(final DataRepresentationSection section, final MessageHolder messages) {
        logger_.info(section);

        validate("numBits", section.template().numBits(), is(anyOf((byte)12, (byte)16)));
    }

    @Override
    public void bitmapSection(final BitmapSection section, final MessageHolder messages) {
        logger_.info(section);
    }

    @Override
    @SuppressWarnings("boxing")
    public void dataSection(final DataSection section, final MessageHolder holder) {
        logger_.info(section);

        DataRepresentationTemplate template = holder.latestDataRepresentationSection().template();
        switch (fileType) {
          case EPS1_GLOBAL:
          case EPS1_MGPV_GLOBAL:
            validate("numBits", template.numBits(), is((byte)16));
            validate("data.length", section.data().length,
                     is(holder.latestGridDefinitionSection().numPoints() * 2));
            break;
          default:
            validate("numBits", template.numBits(), is((byte)12));
            validate("data.length", section.data().length,
                     is((holder.latestGridDefinitionSection().numPoints() * template.numBits() + Byte.SIZE - 1) / Byte.SIZE));
            break;
        }
    }

    @Override
    public void endSection(final EndSection section, final MessageHolder messages) {
        logger_.info(section);
    }


    protected void validate(final String name, final Object actual, final Matcher matcher) {
        if (!matcher.matches(actual)) {
            final Description description= new StringDescription()
                    .appendText(name)
                    .appendText(" expected: ").appendDescriptionOf(matcher)
                    .appendText(" but: ").appendValue(actual);
            logger_.error(description);
        }
    }


    public static <T> Matcher<T> anyOf(T... values) {
        final List<Matcher<? super T>> matchers = new ArrayList(values.length);
        for (T value: values) {
            matchers.add(is(value));
        }
        return org.hamcrest.core.AnyOf.anyOf(matchers);
    }

}
