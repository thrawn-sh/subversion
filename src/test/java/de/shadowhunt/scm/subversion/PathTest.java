package de.shadowhunt.scm.subversion;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {

	@Test
	public void comparePaths() {
		final Path path = Path.create("/a");
		final Path same = Path.create("/a");
		Assert.assertEquals("path compareTo same: 0", 0, path.compareTo(same));
		Assert.assertEquals("same compareTo path: 0", 0, same.compareTo(path));

		final Path other = Path.create("/b");
		Assert.assertTrue("path is smaller than other", (path.compareTo(other) < 0));
		Assert.assertTrue("other is bigger than path", (other.compareTo(path) > 0));
	}

	@Test
	public void createRootPath() {
		Assert.assertEquals("/ is ROOT", Path.ROOT, Path.create("/"));
		Assert.assertEquals("empty is ROOT", Path.ROOT, Path.create(""));
		Assert.assertEquals("null is ROOT", Path.ROOT, Path.create(null));
	}

	@Test
	public void createPath() {
		final Path expected = Path.create("/a/b/c/d.txt");
		Assert.assertEquals(expected, Path.create("/a/b/c/d.txt"));
		Assert.assertEquals(expected, Path.create("a/b/c/d.txt"));
		Assert.assertEquals(expected, Path.create("//a/b/c/d.txt"));
		Assert.assertEquals(expected, Path.create("a//b/c//d.txt"));
		Assert.assertEquals(expected, Path.create("/a/b/c/d.txt/"));
	}

	@Test
	public void equalsPath() {
		final Path path = Path.create("/a");
		Assert.assertEquals("path equals path", path, path);

		final Path same = Path.create("/a");

		Assert.assertNotSame("path and same are different object", path, same);
		Assert.assertEquals("path equals same", path, same);
		Assert.assertEquals("same equals path", same, path);

		final Path other = Path.create("/b");
		Assert.assertNotEquals("path doesn't equal other", path, other);
		Assert.assertNotEquals("same doesn't equal other", same, other);
	}

	@Test
	public void getParent() {
		final Path child = Path.create("/a/b/c/d.txt");
		Assert.assertEquals(Path.create("/a/b/c"), child.getParent());
		Assert.assertEquals(Path.ROOT, Path.ROOT.getParent());
	}

	@Test
	public void hashCodePath() {
		final Path path = Path.create("/a");
		Assert.assertEquals("path has same hashCode as path", path.hashCode(), path.hashCode());

		final Path same = Path.create("/a");

		Assert.assertEquals("path and same have same hashCode", path.hashCode(), same.hashCode());

		final Path other = Path.create("/b");
		Assert.assertNotEquals("path and other don't have same hashCode", path.hashCode(), other.hashCode());
	}
}
