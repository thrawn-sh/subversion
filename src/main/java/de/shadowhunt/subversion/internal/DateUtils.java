/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;

final class DateUtils {

    private static final FastDateFormat CREATED;

    private static final FastDateFormat LAST_MODIFIED;

    static {
        final TimeZone zulu = TimeZone.getTimeZone("ZULU");

        CREATED = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS", zulu, Locale.US);
        LAST_MODIFIED = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss 'GMT'", zulu, Locale.US);
    }

    static Date parseCreatedDate(final String date) {
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

        try {
            return CREATED.parse(time);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("given date '" + date + "' can not be parsed", e);
        }
    }

    static Date parseLastModifiedDate(final String date) {
        try {
            return LAST_MODIFIED.parse(date);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("given date '" + date + "' can not be parsed", e);
        }
    }

    private DateUtils() {
        // prevent instantiation
    }
}
