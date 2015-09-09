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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.shadowhunt.subversion.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public abstract class AbstractPrepare {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private static boolean extractArchive(final File zip, final File prefix) throws Exception {
        try (final ZipFile zipFile = new ZipFile(zip)) {
            final Enumeration<? extends ZipEntry> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                final ZipEntry zipEntry = enu.nextElement();

                final String name = zipEntry.getName();

                final File file = new File(prefix, name);
                if (name.charAt(name.length() - 1) == Resource.SEPARATOR_CHAR) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("can not create directory structure: " + file);
                    }
                    continue;
                }

                final File parent = file.getParentFile();
                if (parent != null) {
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("can not create directory structure: " + parent);
                    }
                }

                try (final InputStream is = zipFile.getInputStream(zipEntry)) {
                    try (final FileOutputStream fos = new FileOutputStream(file)) {
                        final byte[] bytes = new byte[1024];
                        int length;
                        while ((length = is.read(bytes)) >= 0) {
                            fos.write(bytes, 0, length);
                        }
                    }
                }
            }
        }
        return true;
    }

    private final File base;

    private final URI dumpUri;

    private final URI md5Uri;

    protected AbstractPrepare(final URI dumpUri, final URI md5Uri, final File base) {
        this.dumpUri = dumpUri;
        this.md5Uri = md5Uri;
        this.base = base;
    }

    private String calculateMd5(final File zip) throws IOException {
        try (final InputStream is = new FileInputStream(zip)) {
            return DigestUtils.md5Hex(is);
        }
    }

    private String copyUrlToString(final URL source) throws IOException {
        try (final InputStream is = source.openStream()) {
            return IOUtils.toString(is, UTF8);
        }
    }

    public void pullCurrentDumpData() throws Exception {
        final File zip = new File(base, "dump.zip");
        if (zip.exists()) {
            final String localMD5 = calculateMd5(zip);
            final String remoteMD5 = copyUrlToString(md5Uri.toURL());
            if (localMD5.equals(remoteMD5)) {
                return;
            }
        }
        FileUtils.deleteQuietly(base);

        base.mkdirs();
        FileUtils.copyURLToFile(dumpUri.toURL(), zip);
        extractArchive(zip, base);
    }
}
