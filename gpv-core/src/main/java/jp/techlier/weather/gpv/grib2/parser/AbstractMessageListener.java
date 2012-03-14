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
package jp.techlier.weather.gpv.grib2.parser;

import jp.techlier.weather.gpv.grib2.Grib2.BitmapSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataSection;
import jp.techlier.weather.gpv.grib2.Grib2.EndSection;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionSection;
import jp.techlier.weather.gpv.grib2.Grib2.IdentificationSection;
import jp.techlier.weather.gpv.grib2.Grib2.IndicatorSection;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionSection;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/02/29
 * @version $Id$
 */
public abstract class AbstractMessageListener implements MessageListener {

    @Override
    public void indicatorSection(IndicatorSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void identificationSection(IdentificationSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void gridDefinitionSection(GridDefinitionSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void productDefinitionSection(ProductDefinitionSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void dataRepresentationSection(DataRepresentationSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void bitmapSection(BitmapSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void dataSection(DataSection section, MessageHolder holder) {
        // NOP
    }

    @Override
    public void endSection(EndSection section, MessageHolder holder) {
        // NOP
    }

}
