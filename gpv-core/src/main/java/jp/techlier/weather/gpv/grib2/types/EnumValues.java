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
package jp.techlier.weather.gpv.grib2.types;

import java.util.EnumSet;



/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/02/26
 * @version $Id$
 */
public abstract class EnumValues {

    /**
     * @param enumType
     * @param intValue
     * @return enum value
     * @throws EnumConstantNotPresentException 該当するEnum値が存在しない。
     */
    public static <E extends Enum<E> & HasIntValue> E valueOf(final Class<E> enumType, final int intValue)
            throws EnumConstantNotPresentException {
        for (final E e: EnumSet.allOf(enumType)) {
            if (e.intValue() == intValue) return e;
        }
        throw new EnumConstantNotPresentException(enumType, "HasIntValue("+intValue+")");
    }

    /**
     * 未定義のenum値の検出を示すためのEnumValueを生成する。
     * @param value
     * @return
     */
    public static <E extends Enum<E> & EnumValue<E>> EnumValue<E> unknownValue(final Class<E> enumType, final int value) {
        return new EnumValue<E>() {
            @Override public int intValue() { return value; }
            @Override public E enumValue() { return null; }
            @Override public String toString() { return "UNKNOWN("+value+")"; }
        };
    }

}
