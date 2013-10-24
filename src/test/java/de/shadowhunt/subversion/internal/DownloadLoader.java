package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public class DownloadLoader extends BaseLoader {

	public static InputStream load(final Resource resource, final Revision revision) throws Exception {
		return new FileInputStream(new File(ROOT, resolve(revision) + resource.getValue()));
	}
}
