package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.shadowhunt.scm.subversion.SubversionInfo;

public class PropertyGetCommand extends AbstractInfoCommand {

	private static final String PROPERTY_OPTION = "property";

	public PropertyGetCommand() {
		super("propget");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionInfo info = getInfo(cmdl);

		final String property = getProperty(cmdl);
		final String value = info.getSubversionPropertyValue(property);
		if (value != null) {
			System.out.println(value);
		}
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption(new Option("p", PROPERTY_OPTION, true, "property name"));
		return options;
	}

	protected String getProperty(final CommandLine cmdl) {
		return cmdl.getOptionValue(PROPERTY_OPTION);
	}
}
