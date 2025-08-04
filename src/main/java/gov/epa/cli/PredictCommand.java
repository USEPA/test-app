package gov.epa.cli;

import java.util.Arrays;
import java.util.HashSet;

import ToxPredictor.Application.RunParams;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PredictCommand extends Command {
	public PredictCommand() {
		super("predict", "Predict properties using specified endpoint and method");
	}

	@Override
	public void configure(Subparser subparser) {
		subparser.addArgument("-i", "--input").dest("in").type(String.class)
		    .required(true).help("Input file");

		subparser.addArgument("-o", "--output").dest("out").type(String.class)
		    .required(false).help("Output file");

		subparser.addArgument("-e", "--endpoint").dest("endpoint").type(String.class)
		    .required(false).nargs("+").help("Endpoint");

		subparser.addArgument("-m", "--method").dest("method").type(String.class)
		    .required(false).nargs("+").help("Method");
		
		subparser.addArgument("-r", "--report").dest("report").type(String.class)
		    .required(false).nargs("+").help("Web report type")
		    .choices(Arrays.asList(new String[] { "JSON", "HTML", "PDF"}));
	}

	@Override
	public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
		RunParams params = new RunParams();
		params.inputFilePath = namespace.getString("in");
		params.outputFilePath = namespace.getString("out");
		params.endpoints = namespace.get("endpoint") == null ? null : 
		    namespace.getList("endpoint");
		params.methods = namespace.get("method") == null ? null :
		    namespace.getList("method");
		
		params.reportTypes = new HashSet<>();
		if (namespace.get("report") != null) {
		    for (Object type : namespace.getList("report")) {
		        params.reportTypes.add(WebReportType.valueOf((String)type));
            }
		}
		params.discardResults = true;
		
		WebTEST.go(params);
	}
}
