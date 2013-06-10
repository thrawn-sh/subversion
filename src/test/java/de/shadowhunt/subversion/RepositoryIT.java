package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RepositoryIT {

	private static String BASE;

	protected static Path EXISTING_DIR_WITH_DIRS = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_with_dirs");

	protected static Path EXISTING_DIR_WITH_FILES = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_with_files");

	protected static Path EXISTING_EMPTY_DIR = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_empty");

	protected static Path EXISTING_FILE = Path.create("/trunk/00000000-0000-0000-0000-000000000000/revisions.txt");

	protected static Path EXISTING_MIXED_DIR = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed");

	protected static Path EXISTING_MULTIPLE_PROPERTIES = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed/a/file.txt");

	protected static Path EXISTING_PROPERTY_VERSION = Path.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed/file.txt");

	protected static Path NON_EXISTING = Path.create("/trunk/00000000-0000-0000-0000-000000000000/no_exisiting.txt");

	protected static Repository REPO = SubversionFactory.getInstance(URI.create("http://subversion-16.vm.shadowhunt.de/svn-basic/test"), false, ServerVersion.V1_6);

	protected static String retrieveContent(final InputStream download) throws IOException {
		try {
			return StringUtils.trimToEmpty(IOUtils.toString(download, "UTF-8"));
		} finally {
			IOUtils.closeQuietly(download);
		}
	}

	@BeforeClass
	public static void setup() {
		BASE = "/trunk/" + UUID.randomUUID().toString();
		REPO.setCredentials("svnuser", "svnpass", null);
	}

	@Test
	public void downloadExisitingDir() {
		final InputStream download = REPO.download(EXISTING_EMPTY_DIR, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);
		// returns html showing the directory content
		IOUtils.closeQuietly(download);
	}

	@Test
	public void downloadExisitingFileHeadRevision() throws IOException {
		final InputStream download = REPO.download(EXISTING_FILE, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);

		final String content = retrieveContent(download);
		Assert.assertEquals("content must match", "Test 9", content);
	}

	@Test
	public void downloadExisitingFileRevision() throws IOException {
		final InputStream download = REPO.download(EXISTING_FILE, Revision.create(20));
		Assert.assertNotNull("InputStream must not be null", download);

		final String content = retrieveContent(download);
		Assert.assertEquals("content must match", "Test 4", content);
	}

	@Test
	public void existsExisitingDir() {
		Assert.assertTrue("folder exisits in head revision", REPO.exists(EXISTING_EMPTY_DIR, Revision.HEAD));
		Assert.assertFalse("folder didn't exisits in revision 1", REPO.exists(EXISTING_EMPTY_DIR, Revision.INITIAL));
	}

	@Test
	public void existsExisitingFile() {
		Assert.assertTrue("file exisits in head revision", REPO.exists(EXISTING_FILE, Revision.HEAD));
		Assert.assertFalse("file didn't exisits in revision 1", REPO.exists(EXISTING_FILE, Revision.INITIAL));
	}

	@Test
	public void existsNonExisitingPath() {
		Assert.assertFalse("revisions file doesn't exisits in head revision", REPO.exists(NON_EXISTING, Revision.HEAD));
		Assert.assertFalse("revisions file didn't exisits in revision 1", REPO.exists(NON_EXISTING, Revision.INITIAL));
	}

	@Test
	public void infoExisitingDirHeadRevision() {
		final InfoEntry info = REPO.info(EXISTING_EMPTY_DIR, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertNull("folder don't have md5", info.getMd5());
		Assert.assertEquals("path are equal", EXISTING_EMPTY_DIR, info.getPath());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last cahnged at", Revision.create(2), info.getRevision());
		Assert.assertEquals("repository root is", "/svn-basic/test", info.getRoot());
		Assert.assertFalse("resource is not a file", info.isFile());
		Assert.assertTrue("resource is a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExisitingDirRevision() {
		final Revision revision = Revision.create(2);
		final InfoEntry info = REPO.info(EXISTING_EMPTY_DIR, revision, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertNull("folder don't have md5", info.getMd5());
		Assert.assertEquals("path are equal", EXISTING_EMPTY_DIR, info.getPath());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last cahnged at", revision, info.getRevision());
		Assert.assertEquals("repository root is", "/svn-basic/test", info.getRoot());
		Assert.assertFalse("resource is not a file", info.isFile());
		Assert.assertTrue("resource is a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExisitingFileHeadRevision() {
		final InfoEntry info = REPO.info(EXISTING_FILE, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertEquals("is expected file", "c9aa593b08050501075f060ba1708196", info.getMd5());
		Assert.assertEquals("path are equal", EXISTING_FILE, info.getPath());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last cahnged at", Revision.create(30), info.getRevision());
		Assert.assertEquals("repository root is", "/svn-basic/test", info.getRoot());
		Assert.assertTrue("resource is a file", info.isFile());
		Assert.assertFalse("resource is not a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExisitingFileRevision() {
		final Revision revision = Revision.create(20);
		final InfoEntry info = REPO.info(EXISTING_FILE, revision, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertEquals("is expected file", "5ae6c6c33fea175753939e037efeb751", info.getMd5());
		Assert.assertEquals("path are equal", EXISTING_FILE, info.getPath());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last cahnged at", revision, info.getRevision());
		Assert.assertEquals("repository root is", "/svn-basic/test", info.getRoot());
		Assert.assertTrue("resource is a file", info.isFile());
		Assert.assertFalse("resource is not a directory", info.isDirectory());
	}

	@Test
	public void infoMultipleProperties() {
		final InfoEntry info = REPO.info(EXISTING_MULTIPLE_PROPERTIES, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 5, properties.length);
		for (final ResourceProperty property : properties) {
			final char count = property.getName().charAt(10);
			Assert.assertEquals("type is always custom", ResourceProperty.Type.CUSTOM, property.getType());
			Assert.assertEquals("value is expected", "value " + count, property.getValue());
		}
	}

	@Test
	public void infoMultiplePropertiesRevision() {
		final InfoEntry info = REPO.info(EXISTING_MULTIPLE_PROPERTIES, Revision.create(11), true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 3, properties.length);
		for (final ResourceProperty property : properties) {
			final char count = property.getName().charAt(10);
			Assert.assertEquals("type is always custom", ResourceProperty.Type.CUSTOM, property.getType());
			Assert.assertEquals("value is expected", "value " + count, property.getValue());
		}
	}

	@Test(expected = SubversionException.class)
	public void infoNonExisitingPath() {
		REPO.info(NON_EXISTING, Revision.HEAD, true);
		Assert.fail("no info for non exisiting path must be created");
	}

	@Test
	public void infoPropertyVersions() {
		final InfoEntry info = REPO.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 1, properties.length);

		Assert.assertEquals("property value must match", "value 5", info.getResourcePropertyValue("myProperty"));
	}

	@Test
	public void infoPropertyVersionsRevision() {
		final InfoEntry info = REPO.info(EXISTING_PROPERTY_VERSION, Revision.create(6), true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 1, properties.length);

		Assert.assertEquals("property value must match", "value 3", info.getResourcePropertyValue("myProperty"));
	}

	@Test
	public void lastLogExisitingDir() {
		final LogEntry log = REPO.lastLog(EXISTING_EMPTY_DIR);
		Assert.assertNotNull("LogEntry must not be null", log);

		Assert.assertEquals("comment must match", "create test structure", log.getComment());
		Assert.assertNotNull("Date must not be null", log.getDate());
		Assert.assertEquals("revision must match", Revision.create(2), log.getRevision());
		Assert.assertEquals("user must match", "root", log.getUser());
	}

	@Test
	public void lastLogExisitingFile() {
		final LogEntry log = REPO.lastLog(EXISTING_FILE);
		Assert.assertNotNull("LogEntry must not be null", log);

		Assert.assertEquals("comment must match", "adding revisions file 9", log.getComment());
		Assert.assertNotNull("Date must not be null", log.getDate());
		Assert.assertEquals("revision must match", Revision.create(30), log.getRevision());
		Assert.assertEquals("user must match", "root", log.getUser());
	}

	@Test(expected = SubversionException.class)
	public void lastLogNonExisitingPath() {
		REPO.lastLog(NON_EXISTING);
		Assert.fail("no log for non exisiting path must be created");
	}

	@Test
	public void listExisitingDirWithDirs() {
		final List<InfoEntry> listEmpty = REPO.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("InfoEntries must not be null", listEmpty);
		Assert.assertEquals("number of LogEntries must match", 1, listEmpty.size());

		final List<InfoEntry> listFiles = REPO.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("InfoEntries must not be null", listFiles);
		Assert.assertTrue("number of LogEntries must match", listFiles.isEmpty());

		final List<InfoEntry> listImmediates = REPO.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("InfoEntries must not be null", listImmediates);
		Assert.assertEquals("number of LogEntries must match", 5, listImmediates.size());

		final List<InfoEntry> listInfinity = REPO.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("InfoEntries must not be null", listInfinity);
		Assert.assertEquals("number of LogEntries must match", 5, listInfinity.size());
	}

	@Test
	public void listExisitingDirWithFiles() {
		final List<InfoEntry> listEmpty = REPO.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("InfoEntries must not be null", listEmpty);
		Assert.assertEquals("number of LogEntries must match", 1, listEmpty.size());

		final List<InfoEntry> listFiles = REPO.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("InfoEntries must not be null", listFiles);
		Assert.assertEquals("number of LogEntries must match", 4, listFiles.size());

		final List<InfoEntry> listImmediates = REPO.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("InfoEntries must not be null", listImmediates);
		Assert.assertEquals("number of LogEntries must match", 5, listImmediates.size());

		final List<InfoEntry> listInfinity = REPO.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("InfoEntries must not be null", listInfinity);
		Assert.assertEquals("number of LogEntries must match", 5, listInfinity.size());
	}

	@Test
	public void listExisitingEmptyDir() {
		final List<InfoEntry> listEmpty = REPO.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("InfoEntries must not be null", listEmpty);
		Assert.assertEquals("number of LogEntries must match", 1, listEmpty.size());

		final List<InfoEntry> listFiles = REPO.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("InfoEntries must not be null", listFiles);
		Assert.assertTrue("number of LogEntries must match", listFiles.isEmpty());

		final List<InfoEntry> listImmediates = REPO.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("InfoEntries must not be null", listImmediates);
		Assert.assertEquals("number of LogEntries must match", 1, listImmediates.size());

		final List<InfoEntry> listInfinity = REPO.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("InfoEntries must not be null", listInfinity);
		Assert.assertEquals("number of LogEntries must match", 1, listInfinity.size());
	}

	@Test
	public void listExisitingFile() {
		final List<InfoEntry> listEmpty = REPO.list(EXISTING_FILE, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("InfoEntries must not be null", listEmpty);
		Assert.assertEquals("number of LogEntries must match", 1, listEmpty.size());

		final List<InfoEntry> listFiles = REPO.list(EXISTING_FILE, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("InfoEntries must not be null", listFiles);
		Assert.assertEquals("number of LogEntries must match", 1, listFiles.size());

		final List<InfoEntry> listImmediates = REPO.list(EXISTING_FILE, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("InfoEntries must not be null", listImmediates);
		Assert.assertEquals("number of LogEntries must match", 1, listImmediates.size());

		final List<InfoEntry> listInfinity = REPO.list(EXISTING_FILE, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("InfoEntries must not be null", listInfinity);
		Assert.assertEquals("number of LogEntries must match", 1, listInfinity.size());
	}

	@Test
	public void listExisitingMixedDir() {
		final List<InfoEntry> listEmpty = REPO.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("InfoEntries must not be null", listEmpty);
		Assert.assertEquals("number of LogEntries must match", 1, listEmpty.size());

		final List<InfoEntry> listFiles = REPO.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("InfoEntries must not be null", listFiles);
		Assert.assertEquals("number of LogEntries must match", 1, listFiles.size());

		final List<InfoEntry> listImmediates = REPO.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("InfoEntries must not be null", listImmediates);
		Assert.assertEquals("number of LogEntries must match", 3, listImmediates.size());

		final List<InfoEntry> listInfinity = REPO.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("InfoEntries must not be null", listInfinity);
		Assert.assertEquals("number of LogEntries must match", 6, listInfinity.size());
	}

	@Test(expected = SubversionException.class)
	public void listNonExisitingPath() {
		REPO.list(NON_EXISTING, Revision.HEAD, Depth.EMPTY, false);
		Assert.fail("no listings for non exisiting path must be created");
	}

	@Test
	@Ignore
	public void lockingExisitingDir() {
		final InfoEntry beforeLock = REPO.info(EXISTING_EMPTY_DIR, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", beforeLock);
		Assert.assertFalse(beforeLock.isLocked());
		Assert.assertNull("not locked => no lock owner", beforeLock.getLockOwner());
		Assert.assertNull("not locked => no lock token", beforeLock.getLockToken());

		try {
			REPO.lock(EXISTING_EMPTY_DIR);

			final InfoEntry afterLock = REPO.info(EXISTING_EMPTY_DIR, Revision.HEAD, false);
			Assert.assertNotNull("InfoEntry must not be null", afterLock);
			Assert.assertTrue(afterLock.isLocked());
			Assert.assertEquals("locked => lock owner", "svnuser", afterLock.getLockOwner());
			Assert.assertNotNull("locked => lock token", afterLock.getLockToken());
		} finally {
			// ensure we don't leave any locks behind
			REPO.unlock(EXISTING_EMPTY_DIR);
		}

		final InfoEntry afterUnlock = REPO.info(EXISTING_EMPTY_DIR, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterUnlock);
		Assert.assertFalse(afterUnlock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterUnlock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterUnlock.getLockToken());
	}

	@Test
	@Ignore
	public void lockingExisitingFile() {
		final InfoEntry beforeLock = REPO.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", beforeLock);
		Assert.assertFalse(beforeLock.isLocked());
		Assert.assertNull("not locked => no lock owner", beforeLock.getLockOwner());
		Assert.assertNull("not locked => no lock token", beforeLock.getLockToken());

		try {
			REPO.lock(EXISTING_FILE);

			final InfoEntry afterLock = REPO.info(EXISTING_FILE, Revision.HEAD, false);
			Assert.assertNotNull("InfoEntry must not be null", afterLock);
			Assert.assertTrue(afterLock.isLocked());
			Assert.assertEquals("locked => lock owner", "svnuser", afterLock.getLockOwner());
			Assert.assertNotNull("locked => lock token", afterLock.getLockToken());
		} finally {
			// ensure we don't leave any locks behind
			REPO.unlock(EXISTING_FILE);
		}

		final InfoEntry afterUnlock = REPO.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterUnlock);
		Assert.assertFalse(afterUnlock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterUnlock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterUnlock.getLockToken());
	}

	@Test(expected = SubversionException.class)
	public void lockingNonExisitingPath() {
		REPO.lock(NON_EXISTING);
		Assert.fail("locking of non exisiting path must not be possible");
	}

	@Test
	public void logExisitingFileAscending() {
		final List<LogEntry> logs = REPO.log(EXISTING_FILE, Revision.INITIAL, Revision.HEAD);
		Assert.assertNotNull("LogEntries must not be null", logs);
		Assert.assertEquals("number of LogEntries must match", 9, logs.size());

		Revision last = Revision.INITIAL;
		for (int i = 0; i < 9; i++) {
			final LogEntry entry = logs.get(i);

			Assert.assertEquals("comment must match", "adding revisions file " + (i + 1), entry.getComment());
			Assert.assertNotNull("Date must not be null", entry.getDate());
			final Revision revision = entry.getRevision();
			Assert.assertTrue(last + " must be smaller than " + revision, (last.compareTo(revision) < 0));
			last = revision;
			Assert.assertEquals("user must match", "root", entry.getUser());
		}
	}

	@Test
	public void logExisitingFileDescending() {
		final List<LogEntry> logs = REPO.log(EXISTING_FILE, Revision.HEAD, Revision.INITIAL);
		Assert.assertNotNull("LogEntries must not be null", logs);
		Assert.assertEquals("number of LogEntries must match", 9, logs.size());

		Revision last = Revision.create(99);
		for (int i = 0; i < 9; i++) {
			final LogEntry entry = logs.get(i);

			Assert.assertEquals("comment must match", "adding revisions file " + (9 - i), entry.getComment());
			Assert.assertNotNull("Date must not be null", entry.getDate());
			final Revision revision = entry.getRevision();
			Assert.assertTrue(last + " must be smaller than " + revision, (last.compareTo(revision) > 0));
			last = revision;
			Assert.assertEquals("user must match", "root", entry.getUser());
		}
	}

	@Test(expected = SubversionException.class)
	public void logNonExisitingPath() {
		REPO.log(NON_EXISTING, Revision.INITIAL, Revision.HEAD);
		Assert.fail("no logs for non exisiting path must be created");
	}

	@Test
	public void createFolder() {
		final Path folder = Path.create(BASE + "/emptyFolder");
		//		Assert.assertFalse("new folder must not exist", REPO.exists(folder, Revision.HEAD));
		REPO.createFolder(folder, "create emptyFolder");
		//		Assert.assertTrue("created folder must exist", REPO.exists(folder, Revision.HEAD));
	}
}
