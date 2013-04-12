package de.shadowhunt.cmdl.command;

public interface Command {

	public abstract void execute(String... arguments) throws Exception;

}
