/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal.jaxb.converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.shadowhunt.subversion.LockToken;

public class LockTokenAdapter extends XmlAdapter<String, LockToken> {

    @Override
    public String marshal(final LockToken token) throws Exception {
        if (token == null) {
            return null;
        }
        return token.toString();
    }

    @Override
    public LockToken unmarshal(final String token) throws Exception {
        if (token == null) {
            return null;
        }
        return new LockToken(token);
    }

}
