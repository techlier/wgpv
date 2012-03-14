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
package jp.techlier.weather.gpv.grib2;

import static jp.techlier.test.Matchers.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import jp.techlier.weather.gpv.grib2.Grib2.ScanningMode;
import jp.techlier.weather.gpv.grib2.types.Container;
import jp.techlier.weather.gpv.grib2.types.Container.Assertion;
import jp.techlier.weather.gpv.grib2.types.Content;
import jp.techlier.weather.gpv.grib2.types.ContentInfo;
import jp.techlier.weather.gpv.grib2.types.EnumValue;
import jp.techlier.weather.gpv.grib2.types.HasIntValue;
import jp.techlier.weather.gpv.grib2.types.Section;
import jp.techlier.weather.gpv.grib2.types.Template;
import jp.techlier.weather.gpv.grib2.types.TemplateNumber;

import org.junit.Test;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2012/02/26
 * @version $Id$
 */
@SuppressWarnings({"unchecked", "rawtypes", "boxing"})
public class Grib2StructureTest {

    @Test
    public void checkSectionAssertions() throws Exception {
        final Class[] sections = new Class[9];
        for (Class containerType: Grib2.class.getDeclaredClasses()) {
            if (Section.class.isAssignableFrom(containerType) && containerType != Section.class) {
                final String className = containerType.getSimpleName();
                Assertion anno = (Assertion)containerType.getAnnotation(Assertion.class);
                assertThat(className+" must have @Assertion", anno, isNotNull());
                int number = anno.section();
                assertThat(className+" has invalid section number", number, is(greaterThanOrEqualTo(0)));
                assertThat(className+" has invalid section number", number, is(lessThan(sections.length)));
                assertThat(className+" has duplicated section number", sections[number], isNull());
                sections[number] = containerType;
            }
        }
    }


    interface containerTypeTester {
        void test(Class targetClass) throws Exception;
    }

    void containerTest(containerTypeTester tester) throws Exception {
        containerTest(tester, Grib2.class);
    }

    void containerTest(containerTypeTester tester, Class targetClass) throws Exception {
        if (Container.class.isAssignableFrom(targetClass)) {
            tester.test(targetClass);
        }
        for (Class innerClass: targetClass.getDeclaredClasses()) {
            containerTest(tester, innerClass);
        }
    }

    @Test
    public void checkTemplateAssertions() throws Exception {
        containerTest(new containerTypeTester() {
            @Override
            public void test(Class containerType) {
                if (Template.class.isAssignableFrom(containerType) && containerType != Template.class) {
                    final String className = containerType.getSimpleName();
                    Assertion anno = (Assertion)containerType.getAnnotation(Assertion.class);
                    if (containerType == Grib2.ProductDefinitionTemplate.class) {
                        assertThat(className+" must not have @Assertion", anno, isNull());
                    }
                    else {
                        assertThat(className+" must have @Assertion", anno, isNotNull());
                        int number = anno.template();
                        assertThat(className+" has invalid template number", number, is(greaterThanOrEqualTo(0)));
                    }
                }
            }
        });
    }

    @Test
    public void checkContentTypes() throws Exception {
        containerTest(new containerTypeTester() {
            @Override
            public void test(Class containerType) {
                final String className = containerType.getSimpleName();
                for (Method contentAccsessor: containerType.getMethods()) {
                    final String fieldName = contentAccsessor.getReturnType().getSimpleName()+" "+className+"."+contentAccsessor.getName();
                    if (contentAccsessor.getAnnotation(Content.class) != null) {
                        Class fieldType = contentAccsessor.getReturnType();
                        Class contentType = contentAccsessor.getAnnotation(Content.class).type();
                        if (contentType == Object.class) {
                            contentType = null;
                        }
                        else {
                            assert contentType != Object.class;
                            assertTrue(fieldName+" is must assinable from "+contentType.getSimpleName(),
                                       fieldType.isAssignableFrom(contentType));
                        }

                        if (contentAccsessor.getName().equals("templateNumber")) {
                            assertThat(fieldName+" must be annotated with @Content.type",
                                       contentType, isNotNull());
                            assertTrue(fieldName+" must assignable to TemplateNumber",
                                       TemplateNumber.class.isAssignableFrom(contentType));
                            assertTrue(fieldName+" must transform to EnumValue",
                                       fieldType == EnumValue.class);
                            continue;
                        }
                        if (contentAccsessor.getName().equals("template")) {
                            assertTrue(fieldName+" must assignable to Template",
                                       Template.class.isAssignableFrom(fieldType));
                            continue;
                        }
                        if (contentAccsessor.getName().equals("scanningMode")) {
                            assertTrue(fieldName+" must be ScanningMode",
                                       fieldType == ScanningMode.class);
                            continue;
                        }

                        if (fieldType == EnumValue.class) {
                            assertThat(fieldName+" must be annotated with @Content.type",
                                       contentType, isNotNull());
                            assertTrue(fieldName+" must be enum",
                                       contentType.isEnum());
                            continue;
                        }

                        if (fieldType.isPrimitive()) continue;
                        if (fieldType == String.class) continue;
                        if (fieldType == byte[].class) continue;
                        fail(fieldName+" is unexpected type: "+fieldType);
                    }
                }
            }
        });
    }

    @Test
    public void checkContentPosition() throws Exception {
        containerTest(new containerTypeTester() {
            @Override
            public void test(Class containerType) {
                if (!containerType.isAnnotationPresent(Assertion.class)) return;
                final String className = containerType.getSimpleName();
                assertThat(className+" must has contents",
                           ContentInfo.getOrderedContents(containerType).size(), is(greaterThan(0)));
                int containerSize = max(((Assertion)containerType.getAnnotation(Assertion.class)).length());
                int nextOffset = 0;
                for (ContentInfo content: ContentInfo.getOrderedContents(containerType)) {
                    final String description = content.type().getSimpleName()+" "+className+"."+content.name();
                    assertThat(description+" invalid offset", content.offset(), is(greaterThan(0)));
                    if (nextOffset > 0) {
                        assertThat(description+" invalid offset", content.offset(), is(nextOffset));
                    }
                    if (content.length() > 0) {
                        nextOffset = content.offset() + content.length();
                    }
                    assertThat(description+" invalid length", nextOffset - 1, is(lessThanOrEqualTo(containerSize)));
                }
            }
        });
    }

    int max(int[] values) {
        if (values == null || values.length == 0) return Integer.MAX_VALUE;
        int max = values[0];
        for (int i = values.length; --i > 0; ) {
            if (values[i] > max) max = values[i];
        }
        return max;
    }


    @Test
    public void checkContentLength() throws Exception {
        containerTest(new containerTypeTester() {
            @Override
            public void test(Class containerType) {
                if (!containerType.isAnnotationPresent(Assertion.class)) return;
                final String className = containerType.getSimpleName();
                assertThat(className+" must has contents",
                           ContentInfo.getOrderedContents(containerType).size(), is(greaterThan(0)));
                for (ContentInfo content: ContentInfo.getOrderedContents(containerType)) {
                    final String description = content.type().getSimpleName()+" "+className+"."+content.name();
                    Class contentType = content.type();

                    if (contentType == Byte.TYPE) {
                        assertThat(description, content.length(), is(1));
                        continue;
                    }
                    if (contentType == Short.TYPE || EnumValue.class.isAssignableFrom(contentType)) {
                        assertThat(description, content.length(), is(anyOf(1, 2)));
                        continue;
                    }
                    if (contentType == Integer.TYPE) {
                        assertThat(description, content.length(), is(anyOf(1, 2, 4)));
                        continue;
                    }
                    if (contentType == Long.TYPE) {
                        assertThat(description, content.length(), is(anyOf(1, 2, 4, 8)));
                        continue;
                    }
                    if (contentType == Float.TYPE) {
                        assertThat(description, content.length(), is(4));
                        continue;
                    }
                    if (contentType == String.class) {
                        assertThat(description, content.length(), is(greaterThan(0)));
                        continue;
                    }
                    if (contentType == byte[].class || Template.class.isAssignableFrom(contentType)) {
                        assertThat(description, content.length(), is(-1));
                        continue;
                    }
                    if (contentType == ScanningMode.class) {
                        assertThat(description, content.length(), is(1));
                        continue;
                    }
                    fail(description+" is unexpected type: "+contentType);
                }
            }
        });
    }


    @Test
    public void testEnumValuesDidNotDuplicate() throws Exception {
        for (Class cl: Grib2.class.getDeclaredClasses()) {
            if (cl.isEnum() && HasIntValue.class.isAssignableFrom(cl)) {
                final Map<Integer,Object> allValues = new HashMap();
                for (Object e: EnumSet.allOf(cl)) {
                    Integer value = HasIntValue.class.cast(e).intValue();
                    assertThat(e + " was already defined as same value", allValues.containsKey(value), is(false));
                    allValues.put(value, e);
                }
            }
        }
    }

}





