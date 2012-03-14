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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/03/10
 * @version $Id$
 */
public class ContentInfo {

    public static Collection<ContentInfo> getOrderedContents(Class<? extends Container> containerType) {
        assert Container.class.isAssignableFrom(containerType);
        return getContentDefinitions(containerType).values();
    }


    private static Map<Class<? extends Container>,Map<String,ContentInfo>> globalDefinitions_ = new HashMap();

    public static Map<String,ContentInfo> getContentDefinitions(Class<? extends Container> containerType) {
        assert Container.class.isAssignableFrom(containerType);
        Map<String,ContentInfo> contents = globalDefinitions_.get(containerType);
        if (contents == null) {
            contents = new LinkedHashMap(); {
                for (final Method m: getContentAccessor(containerType)) {
                    contents.put(m.getName(), new ContentInfo(m));
                }
            }
            globalDefinitions_.put(containerType, contents);
        }
        return contents;
    }

    private static Collection<Method> getContentAccessor(Class<? extends Container> containerType) {
        assert Container.class.isAssignableFrom(containerType);
        if (!Container.class.isAssignableFrom(containerType)) {
            return Collections.emptySet();
        }
        final Set<Method> accessor = new TreeSet<Method>(new Comparator<Method>() {
            @Override
            public int compare(final Method f1, final Method f2) {
                return compare(f1.getAnnotation(Content.class),
                               f2.getAnnotation(Content.class));
            }
            private int compare(final Content anno1, final Content anno2) {
                assert anno1 != null;
                assert anno2 != null;
                return anno1.offset() - anno2.offset();
            }
        });
        for (final Method m: containerType.getMethods()) {
            if (m.isAnnotationPresent(Content.class)) accessor.add(m);
        }
        return accessor;
    }


    private final String name_;
    private final Class type_;
    private final Content anno_;

    ContentInfo(final Method accessor) {
        this.name_ = accessor.getName();
        this.anno_ = accessor.getAnnotation(Content.class);
        this.type_ = anno_.type() != Object.class ? anno_.type() : accessor.getReturnType();
    }

    public String name() {
        return name_;
    }

    public String abbrev() {
        return anno_.abbrev().length() > 0 ? anno_.abbrev() : name_;
    }

    public Class<?> type() {
        return type_;
    }

    public int offset() {
        return anno_.offset();
    }

    public int length() {
        return anno_.length();
    }

    public int[] expectedValues() {
        return anno_.value();
    }

    public String toString() {
        return type_.getSimpleName() + ' ' + name_
             + "{offset=" + anno_.offset()
             + ",length=" + anno_.length()
             + ",value=" + Arrays.toString(anno_.value())
             + '}';
    }

}
