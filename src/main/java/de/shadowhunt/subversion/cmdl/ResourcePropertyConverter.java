package de.shadowhunt.subversion.cmdl;

import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import joptsimple.ValueConverter;

public class ResourcePropertyConverter implements ValueConverter<ResourceProperty> {

    @Override
    public ResourceProperty convert(final String value) {
        final String[] parts = value.split("|");
        if (parts.length != 2) {
            return null;
        }
        return new ResourceProperty(Type.SUBVERSION_CUSTOM, parts[0], parts[1]);
    }

    @Override
    public String valuePattern() {
        return "<NAME>|<VALUE>";
    }

    @Override
    public Class<? extends ResourceProperty> valueType() {
        return ResourceProperty.class;
    }

}
