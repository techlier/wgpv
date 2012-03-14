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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;


abstract class Grib2ContainerImpl<T extends Container> implements Container {

    final Class<T> containerType_;
    final Map<String,ContentInfo> contentDefinition_;
    final Map<String,Object> contentValues_ = new HashMap();

    public Grib2ContainerImpl(Class<T> containerType) {
        containerType_ = containerType;
        contentDefinition_ = ContentInfo.getContentDefinitions(containerType);
    }

    public Class<T> type() {
        return containerType_;
    }

    public ContentInfo getContentInfo(final String name) {
        return contentDefinition_.get(name);
    }

    public Object get(ContentInfo content) {
        checkContentIsExists(content);
        return contentValues_.get(content.name());
    }

    public void set(ContentInfo content, Object value) {
        checkContentIsExists(content);
        contentValues_.put(content.name(), value);
    }

    private void checkContentIsExists(final ContentInfo content) {
        if (!contentDefinition_.containsKey(content.name())) {
            throw new IllegalArgumentException("No such content was defined: "+content.name());
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    protected StringBuilder appendTo(final StringBuilder out) {
        for (final ContentInfo content: contentDefinition_.values()) {
            out.append(content.abbrev()).append(':');
            Object value = get(content);
            if (content.type().isArray()) {
                out.append(value.getClass().getComponentType().getSimpleName())
                   .append('[').append(Array.getLength(value)).append(']');
            }
            else {
                out.append(value);
            }
            out.append(',');
        }
        return out;
    }

}
