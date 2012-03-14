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

import static jp.techlier.weather.gpv.grib2.Grib2.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import jp.techlier.weather.gpv.grib2.Grib2;
import jp.techlier.weather.gpv.grib2.Grib2.BitmapSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationSection;
import jp.techlier.weather.gpv.grib2.Grib2.DataRepresentationTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.DataSection;
import jp.techlier.weather.gpv.grib2.Grib2.EndSection;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionSection;
import jp.techlier.weather.gpv.grib2.Grib2.GridDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.IdentificationSection;
import jp.techlier.weather.gpv.grib2.Grib2.IndicatorSection;
import jp.techlier.weather.gpv.grib2.Grib2.ParameterCategory;
import jp.techlier.weather.gpv.grib2.Grib2.ParameterNumber;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionSection;
import jp.techlier.weather.gpv.grib2.Grib2.ProductDefinitionTemplate;
import jp.techlier.weather.gpv.grib2.Grib2.SectionNumber;
import jp.techlier.weather.gpv.grib2.types.Container;
import jp.techlier.weather.gpv.grib2.types.ContentInfo;
import jp.techlier.weather.gpv.grib2.types.EnumValue;
import jp.techlier.weather.gpv.grib2.types.EnumValues;
import jp.techlier.weather.gpv.grib2.types.Grib2ContainerFactory;
import jp.techlier.weather.gpv.grib2.types.HasIntValue;
import jp.techlier.weather.gpv.grib2.types.Section;
import jp.techlier.weather.gpv.grib2.types.Template;
import jp.techlier.weather.gpv.grib2.types.TemplateNumber;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 *
 * @author <a href="mailto:okamura@techlier.jp">Kz Okamura</a>
 * @since 2011/08/16
 * @version $Id$
 */
public abstract class MessageParser implements MessageHolder {

    final Log logger_ = LogFactory.getLog(this.getClass());


    protected boolean isEnabledSyntaxChecking_ = true;

    public void enableSyntaxChecking(boolean flag) {
        this.isEnabledSyntaxChecking_ = flag;
    }


    private class IndicatorSectionParser extends SectionParser<IndicatorSection> {
        // There isn't need optional implmentations.
    }

    private class IdentificationSectionParser extends SectionParser<IdentificationSection> {
        // There isn't need optional implmentations.
    }

    private class GridDefinitionSectionParser extends SectionParser<GridDefinitionSection> {
        @Override
        protected Object parseContent(final GridDefinitionSection section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            if (content.type() == GridDefinitionTemplate.class) {
                return parseTemplate(section.templateNumber(), in);
            }
            return super.parseContent(section, content, in);
        }
    }

    private class ProductDefinitionSectionParser extends SectionParser<ProductDefinitionSection> {
        @Override
        protected Object parseContent(final ProductDefinitionSection section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            if (content.type() == ProductDefinitionTemplate.class) {
                return parseTemplate(section.templateNumber(), new ProductDefinitionTemplateParser(), in);
            }
            return super.parseContent(section, content, in);
        }
    }

    class ProductDefinitionTemplateParser extends TemplateParser<ProductDefinitionTemplate> {
        @Override
        protected Object parseContent(final ProductDefinitionTemplate section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            final Class contentType = content.type();
            if (contentType == ParameterCategory.class) {
                assert content.length() == 1;
                final int value = in.get();
                try {
                    return ParameterCategory.valueOf(latestIndicatorSection_.discipline().enumValue(), value);
                } catch (Exception enumValueIsNotFound) {
                    return unknownEnumValue(contentType, value, enumValueIsNotFound);
                }
            }
            else if (contentType == ParameterNumber.class) {
                assert content.length() == 1;
                final int value = in.get();
                try {
                    return ParameterNumber.valueOf(section.parameterCategory().enumValue(), value);
                } catch (Exception enumValueIsNotFound) {
                    return unknownEnumValue(contentType, value, enumValueIsNotFound);
                }
            }
            return super.parseContent(section, content, in);
        }
    }

    private class DataRepresentationSectionParser extends SectionParser<DataRepresentationSection> {
        @Override
        protected Object parseContent(final DataRepresentationSection section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            if (content.type() == DataRepresentationTemplate.class) {
                return parseTemplate(section.templateNumber(), in);
            }
            return super.parseContent(section, content, in);
        }
    }

    private class BitmapSectionParser extends SectionParser<BitmapSection> {
        @Override
        protected Object parseContent(final BitmapSection section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            if (content.name().equals("bitmap")) {
                if (section.bitmapIndicator() >= 0) {
                    final byte[] bitmap = new byte[section.length() - content.offset() + 1];
                    in.get(bitmap);
                    return bitmap;
                }
                else {
                    return ArrayUtils.EMPTY_BYTE_ARRAY;
                }
            }
            return super.parseContent(section, content, in);
        }
    }

    private class DataSectionParser extends SectionParser<DataSection> {
        @Override
        protected Object parseContent(final DataSection section,
                                      final ContentInfo content,
                                      final ByteBuffer in) {
            if (content.name().equals("data")) {
                final byte[] data = new byte[section.length() - content.offset() + 1];
                in.get(data);
                return data;
            }
            return super.parseContent(section, content, in);
        }
    }

    @SuppressWarnings("unused")
    private class EndSectionParser extends SectionParser<EndSection> {
        // There isn't need optional implmentations.
    }


    abstract class SectionParser<S extends Section> extends ContentsParser<S> {
        final S parse(final SectionNumber sectionNumber, final ByteBuffer in, final int sectionLength) {
            final S section; try {
                section = (S)containerFactory_.newSection(sectionNumber.sectionType(), sectionLength);
            } catch (Exception cannotInstanciate) {
                logger_.error("Cannot create section for "+sectionNumber);
                return null;
            }
            return parse(section, in);
        }

        protected <T extends Template> T parseTemplate(final EnumValue<? extends TemplateNumber<T>> templateNumber,
                                                       final ByteBuffer in) {
            return parseTemplate(templateNumber, new TemplateParser<T>(), in);
        }

        protected <T extends Template> T parseTemplate(final EnumValue<? extends TemplateNumber<T>> templateNumber,
                                                       final TemplateParser<T> parser,
                                                       final ByteBuffer in) {
            final T template; try {
                template = containerFactory_.newTemplate(templateNumber.enumValue().templateType());
            } catch (Exception cannotInstanciate) {
                logger_.error("Cannot create template for "+templateNumber);
                return null;
            }
            return parser.parse(template, in);
        }
    }

    class TemplateParser<T extends Template> extends ContentsParser<T> {
        // There isn't need optional implmentations.
    }


    abstract class ContentsParser<C extends Container> {
        C parse(final C container, final ByteBuffer in) {
            final int startPosition = in.position();
            int firstOctetNumber = -1;
            for (final ContentInfo content: ContentInfo.getOrderedContents(container.type())) {
                if (isEnabledSyntaxChecking_) {
                    if (firstOctetNumber < 0) {
                        firstOctetNumber = content.offset();
                    }
                    else if (in.position() != startPosition + content.offset() - firstOctetNumber) {
                        logger_.fatal("invalid offset definition: "+content);
                    }
                }
                final Object value = parseContent(container, content, in);
                if (value == null) {
                    logger_.error("unsupported content type: "+content);
                    continue;
                }
                if (isEnabledSyntaxChecking_) {
                    if (value instanceof Number) {
                        validateValue(content, Number.class.cast(value).intValue());
                    }
                    else if (value instanceof HasIntValue) {
                        validateValue(content, HasIntValue.class.cast(value).intValue());
                    }
                }
                container.set(content, value);
            }
            return container;
        }

        protected Object parseContent(final C container, final ContentInfo content, final ByteBuffer in) {
            System.out.println(content);
            final Class contentType = content.type();
            if (contentType.isPrimitive()) {
                if (contentType == Byte.TYPE) {
                    if (content.length() == 1) return Byte.valueOf(getSignedByte(in));
                    invalidLength(content, 1);
                }
                else if (contentType == Short.TYPE) {
                    switch (content.length()) {
                      case 1: return Short.valueOf(getSignedByte(in));
                      case 2: return Short.valueOf(getSignedShort(in));
                    }
                    invalidLength(content, "1 or 2");
                }
                else if (contentType == Integer.TYPE) {
                    switch (content.length()) {
                      case 1: return Integer.valueOf(getSignedByte(in));
                      case 2: return Integer.valueOf(getSignedShort(in));
                      case 4: return Integer.valueOf(getSignedInt(in));
                    }
                    invalidLength(content, "1, 2 or 4");
                }
                else if (contentType == Long.TYPE) {
                    switch (content.length()) {
                      case 1: return Long.valueOf(getSignedByte(in));
                      case 2: return Long.valueOf(getSignedShort(in));
                      case 4: return Long.valueOf(getSignedInt(in));
                      case 8: return Long.valueOf(getSignedLong(in));
                    }
                    invalidLength(content, "1, 2, 4 or 8");
                }
                else if (contentType == Float.TYPE) {
                    if (content.length() == 4) {
                        return Float.valueOf(in.getFloat());
                    }
                    invalidLength(content, 4);
                }
            }
            else if (contentType == String.class) {
                if (content.length() < 0) {
                    throw new IllegalArgumentException("string content must present length: " + content);
                }
                return ByteBufferUtils.getString(in, content.length());
            }
            else if (HasIntValue.class.isAssignableFrom(contentType)) {
                if (contentType.isEnum()) {
                    switch (content.length()) {
                      case 1: return enumValueOf(contentType, in.get());
                      case 2: return enumValueOf(contentType, in.getShort());
                    }
                    invalidLength(content, "1 or 2");
                }
                else {
                    switch (content.length()) {
                      case 1: return objectValueOf(contentType, in.get());
                      case 2: return objectValueOf(contentType, in.getShort());
                    }
                    invalidLength(content, "1 or 2");
                }
            }
            logger_.error("unexpected content field: "+content);
            return null;
        }

        /**
         * 符号bit形式で格納されたbyte値を取得する。
         * 0xff(全てのbitが1）は未定義数を表現する目的で用いられているため変換しない。
         * @param in
         * @return　2の補数形式によるbyte値
         */
        private byte getSignedByte(final ByteBuffer in) {
            final byte value = in.get();
            //return value >= -1 ? value : (byte)(-(value & Byte.MAX_VALUE));
            return (value & Byte.MIN_VALUE) == 0  || value == -1 ? value : (byte)((value ^ Byte.MAX_VALUE) + 1);
        }

        /**
         * 符号bit形式で格納されたshort値を取得する。
         * 0xffff(全てのbitが1）は未定義数を表現する目的で用いられているため変換しない。
         * @param in
         * @return　2の補数形式によるshort値
         */
        private short getSignedShort(final ByteBuffer in) {
            final short value = in.getShort();
            //return value >= -1 ? value : (short)(-(value & Short.MAX_VALUE));
            return (value & Short.MIN_VALUE) == 0  || value == -1 ? value : (short)((value ^ Short.MAX_VALUE) + 1);
        }

        /**
         * 符号bit形式で格納されたint値を取得する。
         * 0xffffffff(全てのbitが1）は未定義数を表現する目的で用いられているため変換しない。
         * @param in
         * @return 2の補数形式によるint値
         */
        private int getSignedInt(final ByteBuffer in) {
            final int value = in.getInt();
            //return value >= -1 ? value : -(value & Integer.MAX_VALUE);
            return (value & Integer.MIN_VALUE) == 0  || value == -1 ? value : ((value ^ Integer.MAX_VALUE) + 1);
        }

        /**
         * 符号bit形式で格納されたlong値を取得する。
         * 0xffffffffffffffffL(全てのbitが1）は未定義数を表現する目的で用いられているため変換しない。
         * @param in
         * @return 2の補数形式によるlong値
         */
        private long getSignedLong(final ByteBuffer in) {
            final long value = in.getLong();
            //return value >= -1L ? value : -(value & Long.MAX_VALUE);
            return (value & Long.MIN_VALUE) == 0  || value == -1L ? value : ((value ^ Long.MAX_VALUE) + 1);
        }

        /**
         * EnumValueインターフェースを持つenum値を取得する。
         * @param type
         * @param value
         * @return
         */
        protected <E extends Enum<E> & EnumValue<E>> EnumValue<E> enumValueOf(final Class<E> type, final int value) {
            try {
                return EnumValues.valueOf(type, value);
            } catch (Exception enumValueIsNotFound) {
                return unknownEnumValue(type, value, enumValueIsNotFound);
            }
        }

        /**
         * HasIntValueインターフェースを持つクラスのインスタンスを生成する。
         * @param type
         * @param value
         * @return
         */
        private HasIntValue objectValueOf(final Class<? extends HasIntValue> type, final int value) {
            try {
                return ConstructorUtils.invokeConstructor(type,
                                                          new Object[] { value },
                                                          new Class[] { Integer.TYPE });
            }
            catch (Throwable cannotCreateInstance) {
                logger_.error("valued object cannot instanciate: "+type+"("+value+")", cannotCreateInstance);
                return null;
            }
        }

        private final boolean validateValue(final ContentInfo content, final int actualValue) {
            if (content.expectedValues().length == 0) return true;
            for (final int expected: content.expectedValues()) {
                if (expected == actualValue) return true;
            }
            logger_.error("content value is expected "+Arrays.toString(content.expectedValues())+" but "+actualValue+": "+content);
            return false;
        }

        private final void invalidLength(final ContentInfo content, final Object expectedLength) {
            logger_.error("content length is expected "+expectedLength+" but annotated "+content.length()+": "+content);
        }
    }


    private Set<MessageListener> listenerSet_ = new CopyOnWriteArraySet<MessageListener>();

    public boolean addListener(final MessageListener listener) {
        return this.listenerSet_.add(listener);
    }

    public boolean removeListner(final MessageListener listener) {
        return this.listenerSet_.remove(listener);
    }

    private void notifyListeners(final Section section) {
        assert section != null;
        if (!listenerSet_.isEmpty()) {
            final String handlerName = StringUtils.uncapitalize(section.type().getSimpleName());
            try {
                final Method handler = MessageListener.class.getMethod(handlerName,
                                                                       section.type(),
                                                                       MessageHolder.class);
                for (MessageListener listener: listenerSet_) {
                    handler.invoke(listener, section, this);
                }
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }


    private IndicatorSection latestIndicatorSection_;
    private IdentificationSection latestIdentificationSection_;
    private GridDefinitionSection latestGridDefinitionSection_;
    private ProductDefinitionSection latestProductDefinitionSection_;
    private DataRepresentationSection latestDataRepresentationSection_;
    private BitmapSection latestBitmapSection_;
    private DataSection latestDataSection_;

    public IndicatorSection latestIndicatorSection() { return latestIndicatorSection_; }
    public IdentificationSection latestIdentificationSection() { return latestIdentificationSection_; }
    public GridDefinitionSection latestGridDefinitionSection() { return latestGridDefinitionSection_; }
    public ProductDefinitionSection latestProductDefinitionSection() { return latestProductDefinitionSection_; }
    public DataRepresentationSection latestDataRepresentationSection() { return latestDataRepresentationSection_; }
    public BitmapSection latestBitmapSection() { return latestBitmapSection_; }
    public DataSection latestDataSection() { return latestDataSection_; }

    protected void clearLatestSection(final int sectionNumber) {
      switch (sectionNumber) {
        case -1:
          latestIndicatorSection_ = null;
          //$FALL-THROUGH$
        case INDICATOR_SECTION_NUMBER:
          latestIdentificationSection_ = null;
          //$FALL-THROUGH$
        case IDENTIFICATION_SECTION_NUMBER:
          latestGridDefinitionSection_ = null;
          //$FALL-THROUGH$
        case GRID_DEFINITION_SECTION_NUMBER:
          latestProductDefinitionSection_ = null;
          //$FALL-THROUGH$
        case PRODUCT_DEFITION_SECTION_NUMBER:
          latestDataRepresentationSection_ = null;
          //$FALL-THROUGH$
        case DATA_REPRESENTAITON_SECTION_NUMBER:
          latestBitmapSection_ = null;
          //$FALL-THROUGH$
        case BITMAP_SECTION_NUMBER:
          latestDataSection_ = null;
          //$FALL-THROUGH$
        case DATA_SECTION_NUMBER:
      }
    }


    private static final Grib2ContainerFactory containerFactory_ = Grib2ContainerFactory.getInstance();

    protected IndicatorSection parseFirstSection(final ByteBuffer in) throws IOException {
        clearLatestSection(INDICATOR_SECTION_NUMBER);
        return latestIndicatorSection_ = new IndicatorSectionParser().parse(SectionNumber.INDICATOR_SECTION, in, INDICATOR_SECTION_LENGTH);
    }


    private int startPosition_;

    protected Section parseNextSection(final ByteBuffer in) throws IOException {
        startPosition_ = in.position();
        final int sectionLength = in.getInt();
        if (sectionLength <= 0) {
            throw new IllegalStateException("invalid section length: " + sectionLength);
        }
        else if (sectionLength == END_SECTION_MARKER) {
            return containerFactory_.newSection(EndSection.class, END_SECTION_LENGTH);
        }
        else {
            if (in.remaining() < sectionLength) {
                refill(in);
                startPosition_ = -4;
            }

            final SectionNumber sectionNumber = SectionNumber.valueOf(in.get());
            clearLatestSection(sectionNumber.intValue());
            switch (sectionNumber) {
              case IDENTIFICATION_SECTION:
                return latestIdentificationSection_ = new IdentificationSectionParser().parse(sectionNumber, in, sectionLength);
              case GRID_DEFINITION_SECTION:
                return latestGridDefinitionSection_ = new GridDefinitionSectionParser().parse(sectionNumber, in, sectionLength);
              case PRODUCT_DEFITION_SECTION:
                return latestProductDefinitionSection_ = new ProductDefinitionSectionParser().parse(sectionNumber, in, sectionLength);
              case DATA_REPRESENTAITON_SECTION:
                return latestDataRepresentationSection_ = new DataRepresentationSectionParser().parse(sectionNumber, in, sectionLength);
              case BITMAP_SECTION:
                return latestBitmapSection_ = new BitmapSectionParser().parse(sectionNumber, in, sectionLength);
              case DATA_SECTION:
                return latestDataSection_ = new DataSectionParser().parse(sectionNumber, in, sectionLength);
              default:
                throw new UnsupportedOperationException("unknown section number: " + sectionNumber);
            }
        }
    }


    public long parse(final ByteBuffer in) throws IOException {
        Section section = parseFirstSection(in);
        System.out.println(section);
        long totalLength = section.length();
        notifyListeners(section);

        do {
            section = parseNextSection(in);
            assert section != null;

            validateSectionLength(in, section);
            totalLength += section.length();
            notifyListeners(section);

            if (in.remaining() < 5) {
                refill(in);
            }
        } while (!(section instanceof Grib2.EndSection));

        return totalLength;
    }


    private boolean validateSectionLength(final ByteBuffer in, final Section section) {
        final int actualPosition = in.position();
        final int expectedPosition = startPosition_ + section.length();
        if (actualPosition > expectedPosition) {
            logger_.fatal(String.format("読み込み長さが合わない: expected %d but %d: %s", expectedPosition,  actualPosition, section));
            throw new IllegalArgumentException();
        }
        else if (actualPosition < expectedPosition) {
            logger_.error(String.format("読み込み長さが合わない: expected %d but %d: %s", expectedPosition,  actualPosition, section));
            in.position(expectedPosition);
            return false;
        }
        return true;
    }


    public void reset() {
        clearLatestSection(-1);
    }


    protected abstract void refill(final ByteBuffer buffer) throws IOException;


    public Map<Class<? extends Enum<?>>,Set<Integer>> missingEnumValues_ = new HashMap();

    protected <E extends Enum<E> & EnumValue<E>> EnumValue<E> unknownEnumValue(final Class<E> type, final int value, final Throwable cause) {
        logger_.error(type.getSimpleName() + " cannot resolved.", cause);
        if (!missingEnumValues_.containsKey(type)) {
            missingEnumValues_.put(type, new TreeSet());
        }
        missingEnumValues_.get(type).add(value);
        return EnumValues.unknownValue(type, value);
    }

}
