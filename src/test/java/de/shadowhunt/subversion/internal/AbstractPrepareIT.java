package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class AbstractPrepareIT {

	private final File base;

	private final URI dumpUri;

	protected AbstractPrepareIT(final URI dumpUri, final File base) {
		this.dumpUri = dumpUri;
		this.base = base;
	}

	private static void extractArchive(final File zip, final File prefix) throws Exception {
		final ZipFile zipFile = new ZipFile(zip);
		final Enumeration<? extends ZipEntry> enu = zipFile.entries();
		while (enu.hasMoreElements()) {
			final ZipEntry zipEntry = enu.nextElement();

			final String name = zipEntry.getName();

			final File file = new File(prefix, name);
			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			}

			final File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			final InputStream is = zipFile.getInputStream(zipEntry);
			final FileOutputStream fos = new FileOutputStream(file);
			final byte[] bytes = new byte[1024];
			int length;
			while ((length = is.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			is.close();
			fos.close();

		}
		zipFile.close();
	}

	@Test
	public void pullCurrentDumpData() throws Exception {
		final boolean deleted = FileUtils.deleteQuietly(base);
		Assert.assertTrue(base + " could not be deleted", deleted);

		final boolean creaded = base.mkdirs();
		Assert.assertTrue(base + " could not be created", creaded);

		final File zip = new File(base, "dump.zip");
		FileUtils.copyURLToFile(dumpUri.toURL(), zip);
		Assert.assertTrue("could download " + zip, zip.isFile());

		extractArchive(zip, base);
	}
}
