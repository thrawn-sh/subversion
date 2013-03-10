package de.shadowhunt.scm.subversion;

import java.util.Arrays;

public class SubversionProperty {

	public enum Type {
		BASE(""),
		CUSTOM("C:"),
		DAV("V:"),
		SVN("S:");

		private final String prefix;

		private Type(final String prefix) {
			this.prefix = prefix;
		}

		String getPrefix() {
			return prefix;
		}
	}

	public static SubversionProperty createCustomProperty(final String name, final String value) {
		return new SubversionProperty(Type.CUSTOM, name, value);
	}

	static SubversionProperty[] filteroutSystemProperties(final SubversionProperty... properties) {
		if (properties == null) {
			return new SubversionProperty[0];
		}

		final SubversionProperty[] filtered = new SubversionProperty[properties.length];
		int index = 0;
		for (final SubversionProperty property : properties) {
			if ((property != null) && ((Type.CUSTOM == property.type) || (Type.SVN == property.type))) {
				filtered[index++] = property;
			}
		}
		return Arrays.copyOf(filtered, index);
	}

	private final String name;

	private final Type type;

	private final String value;

	SubversionProperty(final Type type, final String name, final String value) {
		this.type = type;
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SubversionProperty)) {
			return false;
		}
		final SubversionProperty other = (SubversionProperty) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionProperty [type=");
		builder.append(type);
		builder.append(", name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
