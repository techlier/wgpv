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

import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.DataSection;
import jp.techlier.weather.gpv.grib2.parser.AbstractMessageListener;
import jp.techlier.weather.gpv.grib2.parser.MessageHolder;
import jp.techlier.weather.gpv.grib2.parser.SimplePackingDecorder;


/**
 * Sample for using SimplePackingDecoder.
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/19
 * @version $Id$
 */
public class DataSectionDump extends AbstractMessageListener {

    @Override
    public void dataSection(final DataSection section, final MessageHolder messages) {
        final DataRepresentationTemplate template = messages.latestDataRepresentationSection().template();
        final SimplePackingDecorder decorder = new SimplePackingDecorder(messages.latestGridDefinitionSection().template(),
                                                                         template, section.data());
        double[][] data = decorder.matrix(); {
            System.out.print("{");
            for (int j = 0; j < data.length; j++) {
                System.out.print("{");
                for (int i = 0; i < data[j].length; i++) {
                    System.out.print(data[j][i]);
                    System.out.print(",");
                }
                System.out.println("},");
            }
            System.out.println("},");
        }
    }

}
