/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

final class DateUtils {

    private static final String CREATED_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private static final String LAST_MODIFIED_PATTERN = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private static final TimeZone ZULU = TimeZone.getTimeZone("ZULU");

    @CheckForNull
    static Date parseCreatedDate(@Nullable final String date) {
        if (date == null) {
            return null;
        }
        if ('Z' != date.charAt(date.length() - 1)) {
            throw new IllegalArgumentException("date '" + date + "'is not in Zulu timezone");
        }

        final int index = date.indexOf('.');
        final String time;
        if (index > 0) {
            time = date.substring(0, index + 4); // remove nanoseconds
        } else {
            time = date;
        }

        final DateFormat dateFormat = new SimpleDateFormat(CREATED_PATTERN, Locale.US);
        try {
            dateFormat.setTimeZone(ZULU);
            return dateFormat.parse(time);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("given date '" + date + "' can not be parsed", e);
        }
    }

    @CheckForNull
    static Date parseLastModifiedDate(@Nullable final String date) {
        if (date == null) {
            return null;
        }
        final DateFormat dateFormat = new SimpleDateFormat(LAST_MODIFIED_PATTERN, Locale.US);
        try {
            return dateFormat.parse(date);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("given date '" + date + "' can not be parsed", e);
        }
    }

    private DateUtils() {
        // prevent instantiation
    }
}
