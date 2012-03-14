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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import jp.techlier.weather.gpv.grib2.parser.MessageParser;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/16
 * @version $Id$
 */
public class GpvFileParser extends MessageParser implements FileFilter {

    public static final int BUFFER_SIZE = 1024 * 1024;

    private ByteBuffer defaultBuffer;

    private ByteBuffer defaultBuffer() {
        if (defaultBuffer == null) {
            defaultBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
        return defaultBuffer;
    }


    public void parseAll(final File file) throws IOException {
        if (file.isDirectory()) {
            for (final File child: listFiles(file)) {
                parseAll(child);
            }
        }
        else if (file.isFile()) {
            parse(file);
        }
    }

    protected File[] listFiles(final File dir) {
        return dir.listFiles(this);
    }

    /*(non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(final File file) {
        if (file.isFile()) {
            return (file.getName().endsWith(GpvFileType.GRIB2_SUFFIX));
        }
        return true;
    }


    private File source_;

    public File getFile() {
        return source_;
    }

    public void parse(final File file) throws IOException {
        parse(file, defaultBuffer());
    }

    private FileChannel input_;

    public void parse(final File file, final ByteBuffer buffer) throws IOException {
        source_ = file;
        final FileInputStream in = new FileInputStream(file); try {
            input_ = in.getChannel(); try {
                init(buffer);
                int totalLength = 0;
                while (buffer.hasRemaining()) {
                    totalLength += parse(buffer);
                }
                if (totalLength != file.length()) {
                    System.err.println("ファイルサイズと読み込み長さが一致しない: " + file.length() + ", " + totalLength);
                }
            } finally {
                input_.close();
                input_ = null;
            }
        } finally {
            in.close();
        }
    }


    public void init(final ByteBuffer buffer) throws IOException {
        super.reset();
        buffer.clear();
        refill(buffer);
    }

    @Override
    protected void refill(final ByteBuffer buffer) throws IOException {
        if (buffer.position() > 0) {
            buffer.compact();
        }
        input_.read(buffer);
        if (input_.position() == input_.size()) {
            buffer.limit(buffer.position());
        }
        buffer.position(0);
    }

}
