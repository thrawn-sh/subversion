package de.shadowhunt.cmdl.command;

import java.io.PrintWriter;

public abstract class AbstractCommand implements Command {

	private final String name;

	protected final PrintWriter out;

	protected AbstractCommand(final String name, final PrintWriter out) {
		this.name = name;
		this.out = out;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractCommand other = (AbstractCommand) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public final String toString() {
		return name;
	}
}
