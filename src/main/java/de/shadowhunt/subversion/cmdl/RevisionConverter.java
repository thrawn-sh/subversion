package de.shadowhunt.subversion.cmdl;

import de.shadowhunt.subversion.Revision;
import joptsimple.ValueConverter;

public class RevisionConverter implements ValueConverter<Revision> {

    @Override
    public Revision convert(final String value) {
        if ("HEAD".equals(value)) {
            return Revision.HEAD;
        }
        final int version = Integer.parseInt(value);
        return Revision.create(version);
    }

    @Override
    public String valuePattern() {
        return null;
    }

    @Override
    public Class<? extends Revision> valueType() {
        return Revision.class;
    }

}
