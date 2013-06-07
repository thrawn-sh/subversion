package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.shadowhunt.subversion.InfoEntry;

/**
 * Subversion propget command
 */
public class PropertyGetCommand extends AbstractInfoCommand {

	private static final String PROPERTY_OPTION = "p";

	/**
	 * Create a new Subversion propget command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public PropertyGetCommand(final PrintWriter out) {
		super("propget", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final InfoEntry info = getInfo(cmdl);

		final String property = getProperty(cmdl);
		final String value = info.getSubversionPropertyValue(property);
		if (value != null) {
			out.println(value);
		}
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
