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
