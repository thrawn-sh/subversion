package de.shadowhunt.cmdl.command.subversion;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

public class CheckoutCommand extends AbstractContentCommand {

	public CheckoutCommand() {
		super("checkout");
	}

	private static final String OUTPUT_OPTION = "output";

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final InputStream content = getContent(cmdl);
		final String filename = getOutput(cmdl);
		final OutputStream output = new FileOutputStream(filename);
		IOUtils.copy(content, output);
		IOUtils.closeQuietly(content);
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption(new Option("o", OUTPUT_OPTION, true, "output file"));
		return options;
	}

	protected final String getOutput(final CommandLine cmdl) {
		return cmdl.getOptionValue(OUTPUT_OPTION);
	}
}
