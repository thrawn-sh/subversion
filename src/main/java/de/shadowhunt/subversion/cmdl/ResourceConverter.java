package de.shadowhunt.subversion.cmdl;

import de.shadowhunt.subversion.Resource;
import joptsimple.ValueConverter;

public class ResourceConverter implements ValueConverter<Resource> {

    @Override
    public Resource convert(final String value) {
        return Resource.create(value);
    }

    @Override
    public String valuePattern() {
        return null;
    }

    @Override
    public Class<? extends Resource> valueType() {
        return Resource.class;
    }

}
