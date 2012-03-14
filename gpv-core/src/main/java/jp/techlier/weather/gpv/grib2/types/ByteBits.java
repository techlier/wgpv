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
package jp.techlier.weather.gpv.grib2.types;


public abstract class ByteBits implements HasIntValue {

    protected ByteBits(final int value) {
        this.value_ = value;
    }

    private final int value_;

    @Override public int intValue() {
        return value_;
    }

    protected boolean isZero(final int index) {
        return (value_ & (0x80 >>> index - 1)) == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value_);
    }

    public String toBitString() {
        String result = Integer.toString(value_, 2);
        if (result.length() < Byte.SIZE) {
            result = "00000000" + result;
        }
        if (result.length() > Byte.SIZE) {
            result = result.substring(result.length() - Byte.SIZE);
        }
        return result;
    }

}
