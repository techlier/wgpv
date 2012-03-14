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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/03/10
 * @version $Id$
 */
public class Grib2ContainerFactory {

    public static Grib2ContainerFactory getInstance() {
        return new Grib2ContainerFactory();
    }

    public <T extends Section> T newSection(Class<T> sectionType, final int sectionLength) {
        return (T)Proxy.newProxyInstance(sectionType.getClassLoader(),
                                         new Class[] { sectionType },
                                         new SectionInvocationHandler<T>(sectionType, sectionLength));
    }

    public <T extends Template> T newTemplate(Class<T> templateType) {
        return (T)Proxy.newProxyInstance(templateType.getClassLoader(),
                                         new Class[] { templateType },
                                         new TemplateInvocationHandler<T>(templateType));
    }


    private static abstract class ContainerInvocationHandler<T extends Container> implements InvocationHandler {
        private final Grib2ContainerImpl<T> container_;

        protected ContainerInvocationHandler(Grib2ContainerImpl<T> container) {
            this.container_ = container;
        }

        @Override
        public Object invoke(final Object proxy,
                             final Method method,
                             final Object[] args) throws Throwable {
            if (method.isAnnotationPresent(Content.class)) {
                final ContentInfo content = container_.getContentInfo(method.getName());
                assert content != null : method;
                return container_.get(content);
            }
            else {
                return method.invoke(container_, args);
            }
        }
    }

    private static class SectionInvocationHandler<T extends Section> extends ContainerInvocationHandler<T> {
        private SectionInvocationHandler(Class<T> sectionType, final int sectionLength) {
            super(new SectionImpl(sectionType, sectionLength));
        }
    }

    private static class SectionImpl<T extends Section> extends Grib2ContainerImpl<T> implements Section {
        /* @Content(offset=1, length=4) */ int length_;
        /* @Content(offset=5, length=1) */ int number_;

        SectionImpl(final Class<T> sectionType, final int sectionLength) {
            super(sectionType);
            this.number_ = sectionType.getAnnotation(Assertion.class).section();
            this.length_ = sectionLength;
        }

        @Override public int length() { return length_; }
        @Override public int number() { return number_; }

        @Override
        protected StringBuilder appendTo(final StringBuilder out) {
            out.append("section:").append(number_).append(',')
            .append("length:").append(length_).append(',');
            return super.appendTo(out);
        }
    }


    private static class TemplateInvocationHandler<T extends Template> extends ContainerInvocationHandler<T> {
        private TemplateInvocationHandler(Class<T> templateType) {
            super(new TemplateImpl(templateType));
        }
    }

    private static class TemplateImpl<T extends Template> extends Grib2ContainerImpl<T> implements Template {
        TemplateImpl(Class<T> templateType) {
            super(templateType);
        }

        @Override
        protected StringBuilder appendTo(final StringBuilder out) {
            out.append('{');
            return super.appendTo(out).append('}');
        }
    }

}
