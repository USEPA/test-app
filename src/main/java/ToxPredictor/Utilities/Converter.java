package ToxPredictor.Utilities;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoObject;

public class Converter {

	private static final Logger logger = LogManager.getLogger(Converter.class);

	private static CommandLine parseCli(String[] args) {
		// create the command line parser
		DefaultParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption("i", "in", true, "Input (*.sdf) file");
		options.addOption("o", "out", true, "Output (*.sdf) file");

		try {
			return parser.parse(options, args);
		} catch (ParseException exp) {
			logger.catching(exp);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ResolverDb", options);
		}

		return null;
	}

	private static void convertSdf(String input, String output) {
		Indigo indigo = new Indigo();
		indigo.setOption("molfile-saving-mode", "2000");

		if (StringUtils.isEmpty(output))
			output = input.toLowerCase().replace(".sdf", "-out.sdf");

		IndigoObject saver = indigo.writeFile(output);

		String errSdf = input.toLowerCase().replace(".sdf", "-err.sdf");
		IndigoObject errSaver = indigo.writeFile(errSdf);

		logger.info("{} => {}", input, output);

		int count = 0, errors = 0;
		for (IndigoObject item : indigo.iterateSDFile(input)) {
			try {
				saver.sdfAppend(item);
				if (++count % 1000 == 0)
					logger.debug("{} records written", count);
			} catch (Exception ex) {
				errors++;
				logger.catching(ex);

				indigo.setOption("ignore-stereochemistry-errors", true);
				try {
					item.setProperty("error", ex.getMessage());
					errSaver.sdfAppend(item);
				} catch (IndigoException ex2) {
				}
				indigo.setOption("ignore-stereochemistry-errors", false);
			}
		}

		logger.info("{} records converted, {} errors", count, errors);
	}

	public static void main(String[] args) {
		CommandLine cli = parseCli(args);
		if (cli != null) {
			String input = cli.getOptionValue('i');
			String output = cli.getOptionValue('o');

			if (!new File(input).isDirectory())
				convertSdf(input, output);
			else {
				File dir = new File(input);
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".sdf");
					}
				});

				for (File file : files) {
					convertSdf(file.getPath(), null);
				}
			}
		}

	}

}
