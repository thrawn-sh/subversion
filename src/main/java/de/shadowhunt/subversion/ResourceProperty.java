/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.concurrent.Immutable;

/**
 * {@link ResourceProperty} represents a resource property
 */
@Immutable
public class ResourceProperty {

	/**
	 * {@link ResourceProperty} can have various types, depending of the context they are used
	 */
	public enum Type {
		BASE(""),
		CUSTOM("C:"),
		DAV("V:"),
		SVN("S:");

		private final String prefix;

		private Type(final String prefix) {
			this.prefix = prefix;
		}

		/**
		 * Returns the prefix of the {@link Type}
		 * @return the prefix of the {@link Type}
		 */
		public String getPrefix() {
			return prefix;
		}
	}

	/**
	 * {@link Comparator} that compares {@link ResourceProperty} by their names
	 */
	public static final Comparator<ResourceProperty> NAME_COMPARATOR = new Comparator<ResourceProperty>() {

		@Override
		public int compare(final ResourceProperty rp1, final ResourceProperty rp2) {
			return rp1.getName().compareTo(rp2.getName());
		}
	};

	/**
	 * Factory method to create custom properties
	 * @param name name of the property
	 * @param value value of the property
	 * @return {@link ResourceProperty} with the given name and value, type is always {@code Type.CUSTOM}
	 */
	public static ResourceProperty createCustomProperty(final String name, final String value) {
		return new ResourceProperty(Type.CUSTOM, name, value);
	}

	/**
	 * Filter out {@code Type.Base} and {@code Type.DAV} {@link ResourceProperty}
	 * @param properties {@link ResourceProperty} that shall be filtered
	 * @return filtered {@link ResourceProperty}
	 */
	public static ResourceProperty[] filteroutSystemProperties(final ResourceProperty... properties) {
		final ResourceProperty[] filtered = new ResourceProperty[properties.length];
		int index = 0;
		for (final ResourceProperty property : properties) {
			if ((property != null) && ((Type.CUSTOM == property.type) || (Type.SVN == property.type))) {
				filtered[index++] = property;
			}
		}
		return Arrays.copyOf(filtered, index);
	}

	private final String name;

	private final Type type;

	private final String value;

	ResourceProperty(final Type type, final String name, final String value) {
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
		if (!(obj instanceof ResourceProperty)) {
			return false;
		}
		final ResourceProperty other = (ResourceProperty) obj;
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
