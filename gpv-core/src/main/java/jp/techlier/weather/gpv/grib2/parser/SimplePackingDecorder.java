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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.ScanningMode;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/18
 * @version $Id$
 */
public class SimplePackingDecorder implements Iterable<Double> {

    private final BitReader in_;
    private final double r_, e_, d_;
    private ScanningMode scanningMode_;
    private final int nI_, nJ_;
    private final int numBits_;
    private int bitRemains_;

    public SimplePackingDecorder(final GridDefinitionTemplate gridDefinition,
                                 final DataRepresentationTemplate dataRepresetation,
                                 final byte[] data) {
        this.r_ = dataRepresetation.referenceValue();
        //assert r_ >= 0;
        this.e_ = dataRepresetation.binaryScaleFactor();
        //assert e_ >= 0;
        this.d_ = dataRepresetation.decimalScaleFactor();
        //assert d_ >= 0;

        this.scanningMode_ = gridDefinition.scanningMode();
        this.nI_ = gridDefinition.numPointsAlongParallel();
        this.nJ_ = gridDefinition.numPointsAlongMeridian();
        this.numBits_ = dataRepresetation.numBits();
        assert numBits_ > 0 && numBits_ <= Integer.SIZE;
        this.bitRemains_ = nI_ * nJ_ * numBits_;
        assert data.length >= (bitRemains_ + Byte.SIZE - 1) / Byte.SIZE;
        this.in_ = new BitReader(new ByteArrayInputStream(data));
    }


    /**
     * 次のデータの存在を確認する。
     * @return true: 次データが存在する
     */
    public boolean hasNext() {
        return bitRemains_ >= numBits_;
    }

    /**
     * 次の値を変換する。
     * @return　Y
     */
    public double next() {
        final double x;
        try {
            x = in_.read(numBits_);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        bitRemains_ -= numBits_;

        double temp = r_ + x * Math.pow(2.0, e_);
        return d_ == 0 ? temp : temp / Math.pow(10.0, d_);
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            @Override
            public boolean hasNext() {
                return SimplePackingDecorder.this.hasNext();
            }

            @Override
            public Double next() {
                return SimplePackingDecorder.this.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * 全てのデータを復号した2次元配列を取得する。
     * @return Y
     */
    public double[][] matrix() {
        final double[][] result = new double[nJ_][nI_];
        if (scanningMode_.areSameDirections() && scanningMode_.isIDirectionConsective()) {
            for (int j = 0; j < nJ_; j++) {
                final double[] row = result[j];
                for (int i = 0; i < nI_; i++) {
                    row[i] = next();
                }
            }
        }
        else {
            readMatrix(result);
        }
        return result;
    }

    private void readMatrix(final double[][] result) {
        final boolean hasOpositDirection = !scanningMode_.areSameDirections();
        if (scanningMode_.isIDirectionConsective()) {
            for (int j = 0; j < nJ_; j++) {
                for (int i = 0; i < nI_; i++) {
                    result[j][i] = next();
                }
                if (hasOpositDirection && ++j < nJ_) {
                    for (int i = nI_; --i > 0;) {
                        result[j][i] = next();
                    }
                }
            }
        }
        else {
            for (int i = 0; i < nI_; i++) {
                for (int j = 0; j < nJ_; j++) {
                    result[j][i] = next();
                }
                if (hasOpositDirection && ++i < nI_) {
                    for (int j = nJ_; --j > 0;) {
                        result[j][i] = next();
                    }
                }
            }
        }
    }

}
