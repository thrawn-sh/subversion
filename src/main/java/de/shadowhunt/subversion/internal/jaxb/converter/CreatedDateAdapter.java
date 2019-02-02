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

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.time.FastDateFormat;

public class CreatedDateAdapter extends XmlAdapter<String, Date> {

    private static final FastDateFormat CREATED;

    static {
        final TimeZone zulu = TimeZone.getTimeZone("ZULU");

        CREATED = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS", zulu, Locale.US);
    }

    public static Date parseDate(final String date) throws ParseException {
        final int lastIndex = date.length() - 1;
        if ('Z' != date.charAt(lastIndex)) {
            throw new IllegalArgumentException("date '" + date + "'is not in Zulu timezone");
        }

        final int index = date.indexOf('.');
        final String time;
        if (index > 0) {
            time = date.substring(0, index + 4); // remove nanoseconds + 'Z'
        } else {
            time = date.substring(0, lastIndex) + ".000";
        }
        return CREATED.parse(time);
    }

    @Override
    public String marshal(final Date date) throws Exception {
        if (date == null) {
            return null;
        }
        return CREATED.format(date);
    }

    @Override
    public Date unmarshal(final String date) throws Exception {
        if (date == null) {
            return null;
        }

        return parseDate(date);
    }

}
