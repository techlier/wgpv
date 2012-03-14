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
package jp.techlier.weather.gpv;


public enum GpvFileType {
    GSM_GLOBAL("_GSM_GPV_Rgl_"),
    GSM_JP_SURF("_GSM_GPV_Rjp_Lsurf_"),
    GSM_JP_PALL("_GSM_GPV_Rjp_L-pall_"),
    MSM_JP_SURF("_MSM_GPV_Rjp_Lsurf_"),
    MSM_JP_PALL("_MSM_GPV_Rjp_L-pall_"),
    EPSW_GLOBAL("_EPSW_GPV_Rgl_"),
    EPSW_JP("_EPSW_GPV_Rjp_"),
    GWM_GLOBAL("_GWM_GPV_Rgl_"),
    CWM_JP("_CWM_GPV_Rjp_"),
    EPS1_GLOBAL("_EPS1_GPV_Rgl_"),
    EPS1_MGPV_GLOBAL("_EPS1_MGPV_Rgl_"),
    UNKNOWN(null),
    ;

    public static final String GRIB2_SUFFIX = "grib2.bin";

    private GpvFileType(final String rule) {
        this.namingRule_ = rule != null ? rule.toLowerCase() : null;
    }

    private final String namingRule_;

    public boolean match(final String filename) {
        return matchLowercaseName(filename.toLowerCase());
    }

    private boolean matchLowercaseName(final String filename) {
        return this != UNKNOWN && filename.endsWith(GRIB2_SUFFIX) && filename.contains(namingRule_);
    }

    public static GpvFileType getFileType(String filename) {
        filename = filename.toLowerCase();
        for (final GpvFileType type: values()) {
            if (type.matchLowercaseName(filename)) return type;
        }
        return UNKNOWN;
    }
}
