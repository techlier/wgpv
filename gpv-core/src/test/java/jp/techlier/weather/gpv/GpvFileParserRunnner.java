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
package jp.techlier.weather.gpv;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import jp.techlier.weather.gpv.grib2.parser.samples.DataSectionDump;
import jp.techlier.weather.gpv.grib2.parser.samples.MessageDescriptor;
import jp.techlier.weather.gpv.tools.GpvFileValidator;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/02/28
 * @version $Id$
 */
public class GpvFileParserRunnner {

    /**
     * テスト用main
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        //Grib2ClassTransformer.initialize();
        final GpvFileParser parser = new GpvFileParser();
        parser.addListener(new GpvFileValidator());
        parser.addListener(new MessageDescriptor());
        parser.addListener(new DataSectionDump());
        //parser.parseAll(new File("../../data"));
        parser.parseAll(new File(getDatadir()));
        System.err.println("missing enum values = "+parser.missingEnumValues_);
    }

    static String getDatadir() throws IOException {
        Properties p = new Properties();
        p.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        return p.getProperty("jp.techlier.weather.gpv.datadir");
    }

}
