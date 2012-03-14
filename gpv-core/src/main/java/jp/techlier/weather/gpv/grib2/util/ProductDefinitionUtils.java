/*
 * Copyright (c) 2012 Techlier Inc. All rights reserved.
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
package jp.techlier.weather.gpv.grib2.util;

import java.util.Calendar;

import jp.techlier.weather.gpv.grib2.Grib2.GeneratingProcessType;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.SurfaceType;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/03/06
 * @version $Id$
 */
public class ProductDefinitionUtils {

    public static String description(final ProductDefinitionTemplate template) {
        return appendDetailsTo(template, new StringBuilder()).toString();
    }

    public static StringBuilder appendDetailsTo(final ProductDefinitionTemplate template, StringBuilder out) {
        /*
        out.append(holder.latestIndicatorSection().discipline()).append('|')
           .append(template.parameterCategory()).append('|')
           .append(template.parameterNumber()).append(':');
        */
        out.append(template.parameterNumber().enumValue().abbrev()).append(':');
        appendSurfaceParameterTo(template, out).append(':');
        appendForcastTimeTo(template, out).append(':');
        return out;
    }


    public static String surfaceParameter(final ProductDefinitionTemplate template) {
        return appendSurfaceParameterTo(template, new StringBuilder()).toString();
    }

    public static StringBuilder appendSurfaceParameterTo(final ProductDefinitionTemplate template, final StringBuilder out) {
        if (template.scaledValueOfFirstFixedSurface() >= 0) {
            double firstFixedSurrface = template.scaledValueOfFirstFixedSurface();
            if (template.firstFixedSurfaceType() == SurfaceType.ISOBARIC_SURFACE) {
                if (template.scaleFactorOfFirstFixedSurface() == -2) {
                    out.append(firstFixedSurrface).append(" hPa");
                }
                else {
                    firstFixedSurrface *= Math.pow(10, -template.scaleFactorOfFirstFixedSurface());
                    out.append(firstFixedSurrface).append(" Pa");
                }
            }
            else {
                if (template.scaleFactorOfFirstFixedSurface() != 0) {
                    firstFixedSurrface *= Math.pow(10, -template.scaleFactorOfFirstFixedSurface());
                }
                out.append(firstFixedSurrface).append(' ')
                   .append(template.firstFixedSurfaceType().enumValue().unit()).append(' ')
                   .append(template.firstFixedSurfaceType().enumValue().description());
            }
        }
        else {
            out.append(template.firstFixedSurfaceType().enumValue().description());
        }
        return out;
    }


    public static String forcastTime(final ProductDefinitionTemplate template) {
        return appendForcastTimeTo(template, new StringBuilder()).toString();
    }

    public static StringBuilder appendForcastTimeTo(final ProductDefinitionTemplate template, final StringBuilder out) {
        if (template.generatingProcessType() == GeneratingProcessType.INITIALIZATION) {
            out.append("anl");
        }
        else {
            out.append(template.forecastTime()).append(' ')
               .append(template.unitOfForecastTime().toString().toLowerCase())
               .append(" fcst");
        }
        return out;
    }

    public static Calendar forcastDate(final Calendar productionDate,
                                       final ProductDefinitionTemplate template) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(productionDate.getTimeInMillis());
        switch (template.unitOfForecastTime().enumValue()) {
          case HOUR:
            productionDate.add(Calendar.HOUR_OF_DAY, template.forecastTime());
            break;
          case DAY:
            productionDate.add(Calendar.DAY_OF_MONTH, template.forecastTime());
            break;
          case SIX_HOURS:
            productionDate.add(Calendar.HOUR_OF_DAY, template.forecastTime() * 6);
            break;
          default:
            throw new UnsupportedOperationException();
        }
        return productionDate;
    }


}
