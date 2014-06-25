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

import java.net.URI;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;

public class MergeOperation extends AbstractVoidOperation {

    private final Set<Info> infos;

    private final Resource resource;

    public MergeOperation(final URI repository, final Resource resource, final Set<Info> infos) {
        super(repository);
        this.resource = resource;
        this.infos = infos;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final DavTemplateRequest request = new DavTemplateRequest("MERGE", repository);
        request.addHeader("X-SVN-Options", "release-locks");

        final StringBuilder body = new StringBuilder(XML_PREAMBLE);
        body.append("<merge xmlns=\"DAV:\"><source><href>");
        body.append(StringEscapeUtils.escapeXml10(repository.getPath() + resource.getValue()));
        body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
        if (!infos.isEmpty()) {
            body.append("<S:lock-token-list xmlns:S=\"svn:\">");
            for (final Info info : infos) {
                final String lockToken = info.getLockToken();
                assert (lockToken != null) : "must not be null";
                body.append("<S:lock><S:lock-path>");
                final Resource plain = info.getResource();
                body.append(StringEscapeUtils.escapeXml10(plain.getValueWithoutLeadingSeparator()));
                body.append("</S:lock-path>");
                body.append("<S:lock-token>");
                body.append(lockToken);
                body.append("</S:lock-token></S:lock>");
            }
            body.append("</S:lock-token-list>");
        }
        body.append("</merge>");
        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_OK == statusCode;
    }
}
