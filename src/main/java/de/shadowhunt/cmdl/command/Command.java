package de.shadowhunt.cmdl.command;

public interface Command {

	public void execute(String... arguments) throws Exception;

	public String getName();
}
