/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.internal.jaxb.converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

public class MessageAdapter extends XmlAdapter<String, String> {

    private static final CharSequenceTranslator MARSHALL_TRANSLATOR;

    private static final CharSequenceTranslator UNMARSHALL_TRANSLATOR;

    static {
        MARSHALL_TRANSLATOR = new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE);
        UNMARSHALL_TRANSLATOR = new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE);
    }

    @Override
    public String marshal(final String message) throws Exception {
        final String timmed = StringUtils.trimToEmpty(message);
        return MARSHALL_TRANSLATOR.translate(timmed);
    }

    @Override
    public String unmarshal(final String message) throws Exception {
        final String timmed = StringUtils.trimToEmpty(message);
        return UNMARSHALL_TRANSLATOR.translate(timmed);
    }

}
