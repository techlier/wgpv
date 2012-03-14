package jp.techlier.test;
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


import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/02/29
 * @version $Id$
 */
public class Matchers extends CoreMatchers {

    public static <T> Matcher<? super T> isNull() {
        return nullValue();
    }

    public static <T> Matcher<? super T> isNotNull() {
        return notNullValue();
    }

    public static <T> Matcher<T> anyOf(T... values) {
        final List<Matcher<? super T>> matchers = new ArrayList(values.length);
        for (T value: values) {
            matchers.add(is(value));
        }
        return org.hamcrest.core.AnyOf.anyOf(matchers);
    }

}
