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
import java.util.UUID;

import javax.xml.parsers.SAXParser;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class InfoLoader extends BaseLoader {

    public static final String SUFFIX = ".info";

    static class InfoHandler extends BasicHandler {

        private InfoImpl current = new InfoImpl();

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("token".equals(localName)) {
                final String text = getText();
                current.setLockToken(text.substring(16));
                return;
            }

            if ("uuid".equals(localName)) {
                current.setRepositoryId(UUID.fromString(getText()));
                return;
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

    private final ResourcePropertyLoader resourcePropertyLoader;

    InfoLoader(final File root) {
        super(root);
        resourcePropertyLoader = new ResourcePropertyLoader(root);
    }

    public Info load(final Resource resource, final Revision revision) throws Exception {
        final File infoFile = new File(root, resolve(revision) + resource.getValue() + SUFFIX);

        final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
        final InfoHandler handler = new InfoHandler();

        saxParser.parse(infoFile, handler);
        final InfoImpl info = handler.getInfo();

        info.setResource(resource);
        final File f = new File(root, resolve(revision) + resource.getValue());
        info.setDirectory(f.isDirectory());
        if (info.isFile()) {
            final FileInputStream fis = new FileInputStream(f);
            try {
                info.setMd5(DigestUtils.md5Hex(fis));
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }

        info.setProperties(resourcePropertyLoader.load(resource, revision));
        Assert.assertEquals("resource must match", resource, info.getResource());
        return info;
    }
}
