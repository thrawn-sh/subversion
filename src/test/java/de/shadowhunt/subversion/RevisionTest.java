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
package de.shadowhunt.subversion;

import org.junit.Assert;
import org.junit.Test;

public class RevisionTest {

    @Test
    public void compareRevisions() {
        final Revision revision = Revision.create(5);
        final Revision same = Revision.create(5);
        Assert.assertEquals("revision compareTo same: 0", 0, revision.compareTo(same));
        Assert.assertEquals("same compareTo revision: 0", 0, same.compareTo(revision));

        final Revision other = Revision.create(19);
        Assert.assertTrue("revision is smaller than other", (revision.compareTo(other) < 0));
        Assert.assertTrue("other is bigger than revision", (other.compareTo(revision) > 0));
    }

    @Test
    public void createLegalRevision() {
        final Revision revision = Revision.create(5);
        Assert.assertNotNull("revision must not be null", revision);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNegativeRevision() {
        Revision.create(-1);
    }

    @Test
    public void equalsRevision() {
        final Revision revision = Revision.create(5);
        Assert.assertEquals("revision equals revision", revision, revision);

        final Revision same = Revision.create(5);

        Assert.assertNotSame("revision and same are different object", revision, same);
        Assert.assertEquals("revision equals same", revision, same);
        Assert.assertEquals("same equals revision", same, revision);

        final Revision other = Revision.create(19);
        Assert.assertNotEquals("revision doesn't equal other", revision, other);
        Assert.assertNotEquals("same doesn't equal other", same, other);
    }

    @Test
    public void hashCodeRevision() {
        final Revision revision = Revision.create(5);
        Assert.assertEquals("revision has same hashCode as revision", revision.hashCode(), revision.hashCode());

        final Revision same = Revision.create(5);

        Assert.assertEquals("revision and same have same hashCode", revision.hashCode(), same.hashCode());

        final Revision other = Revision.create(19);
        Assert.assertNotEquals("revision and other don't have same hashCode", revision.hashCode(), other.hashCode());
    }
}
