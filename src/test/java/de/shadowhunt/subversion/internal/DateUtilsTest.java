/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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
