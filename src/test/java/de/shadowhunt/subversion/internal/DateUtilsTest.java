/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void parseCreationDate_invalidDatePattern() throws Exception {
        final String dateString = "2000 01 13 01:02:03.456789Z";
        DateUtils.parseCreatedDate(dateString);
        Assert.fail("must not complete");
    }

    @Test
    public void parseCreationDate_noNano() throws Exception {
        final String dateString = "2000-01-13T01:02:03Z";
        final Date date = DateUtils.parseCreatedDate(dateString);
        Assert.assertEquals("creation date must match", new Date(947725323000L), date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseCreationDate_nonZulu() throws Exception {
        final String dateString = "2000-01-13T01:02:03.456789";
        DateUtils.parseCreatedDate(dateString);
        Assert.fail("must not complete");
    }

    @Test
    public void parseCreationDate_subversion() throws Exception {
        final String dateString = "2000-01-13T01:02:03.456789Z";
        final Date date = DateUtils.parseCreatedDate(dateString);
        Assert.assertEquals("creation date must match", new Date(947725323456L), date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLastModifiedDate_invalidDatePattern() throws Exception {
        final String dateString = "Thu, 13 Jan 2000 01:02:03";
        DateUtils.parseLastModifiedDate(dateString);
        Assert.fail("must not complete");
    }

    @Test
    public void parseLastModifiedDate_subversion() throws Exception {
        final String dateString = "Thu, 13 Jan 2000 01:02:03 GMT";
        final Date date = DateUtils.parseLastModifiedDate(dateString);
        Assert.assertEquals("creation date must match", new Date(947725323000L), date);
    }
}
