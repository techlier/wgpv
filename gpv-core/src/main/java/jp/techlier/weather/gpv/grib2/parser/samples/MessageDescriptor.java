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
package jp.techlier.weather.gpv.grib2.parser.samples;

import jp.techlier.weather.gpv.grib2.Grib2.IdentificationSection;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionSection;
import jp.techlier.weather.gpv.grib2.parser.AbstractMessageListener;
import jp.techlier.weather.gpv.grib2.parser.MessageHolder;
import jp.techlier.weather.gpv.grib2.util.ProductDefinitionUtils;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/19
 * @version $Id$
 */
public class MessageDescriptor extends AbstractMessageListener {

    protected long datetime_;
    protected int part_;

    @Override
    public void identificationSection(final IdentificationSection section, final MessageHolder messages) {
        datetime_ = section.year() * 10000 + section.month() * 100 + section.mday();
        datetime_ *= 1000000;
        datetime_ += section.hour() * 10000 + section.minute() * 100 + section.second();
        part_ = 0;
    }

    @Override
    public void productDefinitionSection(final ProductDefinitionSection section, final MessageHolder messages) {
        final StringBuilder out = new StringBuilder();
        out.append(++part_).append(':')
           .append("d=").append(datetime_).append(':');
        ProductDefinitionUtils.appendDetailsTo(section.template(), out);
        System.out.println(out);
    }



}
