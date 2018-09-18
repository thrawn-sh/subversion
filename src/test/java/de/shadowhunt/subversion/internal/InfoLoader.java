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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.SAXParser;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class InfoLoader extends AbstractBaseLoader {

    static class InfoHandler extends BasicHandler {

        private final InfoImpl current = new InfoImpl();

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("token".equals(localName)) {
                final String text = getText();
                current.setLockToken(new LockToken(text.substring(16)));
                return;
            }

            if ("uuid".equals(localName)) {
                current.setRepositoryId(UUID.fromString(getText()));
                return;
            }

            if ("date".equals(localName)) {
                final Date date = DateUtils.parseCreatedDate(getText());
                current.setCreationDate(date);
                current.setLastModifiedDate(new Date((date.getTime() / 1000L) * 1000L));
            }
        }

        InfoImpl getInfo() {
            return current;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            clearText();

            if ("commit".equals(localName)) {
                final String revision = attributes.getValue("revision");
                current.setRevision(Revision.create(Integer.parseInt(revision)));
            }
        }
    }

    public static final String SUFFIX = ".info";

    private final ResourcePropertyLoader resourcePropertyLoader;

    InfoLoader(final File root, final Resource base) {
        super(root, base);
        resourcePropertyLoader = new ResourcePropertyLoader(root, base);
    }

    public Info load(final Resource resource, final Revision revision) throws Exception {
        final File infoFile = new File(root, resolve(revision) + base.getValue() + resource.getValue() + SUFFIX);

        final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
        final InfoHandler handler = new InfoHandler();

        saxParser.parse(infoFile, handler);
        final InfoImpl info = handler.getInfo();

        final String relative = StringUtils.removeStart(resource.getValue(), base.getValue());
        info.setResource(Resource.create(relative));
        final File file = new File(root, resolve(revision) + base.getValue() + resource.getValue());
        info.setDirectory(file.isDirectory());
        if (info.isFile()) {
            try (InputStream input = new FileInputStream(file)) {
                info.setMd5(DigestUtils.md5Hex(input));
            }
        }

        info.setProperties(resourcePropertyLoader.load(resource, revision));
        Assert.assertEquals("resource must match", resource, info.getResource());
        return info;
    }
}
