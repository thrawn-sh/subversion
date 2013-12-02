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

import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link ResourceProperty} represents a resource property
 */
@Immutable
public final class ResourceProperty {

	/**
	 * {@link Comparator} that compares {@link ResourceProperty} by their type and name
	 */
	public static final Comparator<ResourceProperty> TYPE_NAME_COMPARATOR = new Comparator<ResourceProperty>() {

		@Override
		public int compare(final ResourceProperty rp1, final ResourceProperty rp2) {
			Validate.notNull(rp1, "rp1 must not be null");
			Validate.notNull(rp2, "rp2 must not be null");

			final int result = rp1.getType().compareTo(rp2.getType());
			if (result != 0) {
				return result;
			}
			return rp1.getName().compareTo(rp2.getName());
		}
	};

	private final String name;

	private final Type type;

	private final String value;

	/**
	 * Create a new {@link ResourceProperty} with the given {@link Type}, name and value
	 *
	 * @param type {@link Type} of the {@link ResourceProperty}
	 * @param name name of the {@link ResourceProperty}
	 * @param value value of the {@link ResourceProperty}
	 *
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public ResourceProperty(final Type type, final String name, final String value) {
		Validate.notNull(type, "type must not be null");
		Validate.notNull(name, "name must not be null");
		Validate.notNull(value, "value must not be null");

		this.type = type;
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of the {@link ResourceProperty}
	 *
	 * @return the name of the {@link ResourceProperty}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the {@link Type} of the {@link ResourceProperty}
	 *
	 * @return the {@link Type} of the {@link ResourceProperty}
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the value of the {@link ResourceProperty}
	 *
	 * @return the value of the {@link ResourceProperty}
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ResourceProperty)) {
			return false;
		}

		final ResourceProperty that = (ResourceProperty) o;

		if (!name.equals(that.name)) {
			return false;
		}
		if (type != that.type) {
			return false;
		}
		if (!value.equals(that.value)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ResourceProperty [type=");
		builder.append(type);
		builder.append(", name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append(']');
		return builder.toString();
	}

	/**
	 * {@link ResourceProperty} can have various types, depending of the context they are used
	 */
	public static enum Type {
		CUSTOM("C:"),
		SVN("S:");

		private final String prefix;

		private Type(final String prefix) {
			this.prefix = prefix;
		}

		/**
		 * Returns the prefix of the {@link Type}
		 *
		 * @return the prefix of the {@link Type}
		 */
		public String getPrefix() {
			return prefix;
		}
	}
}
