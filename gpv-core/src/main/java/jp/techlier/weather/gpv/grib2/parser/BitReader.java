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
package jp.techlier.weather.gpv.grib2.parser;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * 入力ストリームから指定されたビットサイズのデータを逐次読みだす。
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/18
 * @version $Id$
 */
class BitReader implements Closeable {

    final InputStream in_;

    /**
     * 指定されたInputStreamを入力とするBitStreamを生成する。
     * @param in
     */
    public BitReader(final InputStream in) {
        in_ = in;
    }

    private int nextIndex_;
    private int next_;

    /**
     * 指定されたbit長さのデータを読み込む。
     * @param bitLength 読み込むビット長。
     * @return 読み込まれたデータ。
     * @throws IOException
     */
    public int read(final int bitLength) throws IOException {
        if (bitLength < 0 || bitLength > Integer.SIZE) {
            throw new IndexOutOfBoundsException("illegal length: " + bitLength);
        }
        if (bitLength == 0) return 0;

        int remains = bitLength;
        int value = 0;
        switch (nextIndex_) {
          case 0: value = read(); break;
          case 1: value = next_ & 0x7f; break;
          case 2: value = next_ & 0x3f; break;
          case 3: value = next_ & 0x1f; break;
          case 4: value = next_ & 0x0f; break;
          case 5: value = next_ & 0x07; break;
          case 6: value = next_ & 0x03; break;
          case 7: value = next_ & 0x01; break;
          default: assert false: "invalid index: " + nextIndex_;
        }
        remains -= 8 - nextIndex_;
        while (remains > 0) {
            value <<= Byte.SIZE;
            value |= read();
            remains -= Byte.SIZE;
        }
        if (remains < 0) {
            next_ = (byte)value;
            value >>= -remains;
            nextIndex_ = 8 + remains;
        }
        else {
            next_ = -1;
            nextIndex_ = 0;
        }
        return value;
    }

    private int read() throws IOException {
        final int value = in_.read();
        if (value < 0) throw new EOFException();
        return value;
    }

    @Override
    public void close() throws IOException {
        in_.close();
    }

}
