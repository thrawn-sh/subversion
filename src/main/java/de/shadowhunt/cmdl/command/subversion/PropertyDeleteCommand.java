package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.SubversionProperty;
import de.shadowhunt.scm.subversion.SubversionRepository;

/**
 * Subversion propdel command
 */
public class PropertyDeleteCommand extends AbstractSubversionCommand {

	private static final String PROPERTY_OPTION = "p";

	/**
	 * Create a new Subversion propdel command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public PropertyDeleteCommand(final PrintWriter out) {
		super("propdel", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final String property = getProperty(cmdl);

		final SubversionRepository repositry = createRepository(cmdl);

		final Path resource = getTargetResource(cmdl);
		repositry.deleteProperties(resource, "TODO", SubversionProperty.createCustomProperty(property, null));
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption(new Option(PROPERTY_OPTION, "property", false, "property name"));
		return options;
	}

	protected String getProperty(final CommandLine cmdl) {
		return cmdl.getOptionValue(PROPERTY_OPTION);
	}
}
