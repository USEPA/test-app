package gov.epa.cli;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.databind.ObjectMapper;

import ToxPredictor.Application.TESTConstants;
import gov.epa.WebTESTConfiguration;
import gov.epa.api.PredictionQuery;
import gov.epa.api.PredictionResult;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PokeCommand extends ConfiguredCommand<WebTESTConfiguration> {
	public PokeCommand() {
        super("poke", "Predict properties using RESTful web service");
    }

	@Override
	public void configure(Subparser subparser) {
		subparser
			.addArgument("-i", "--in")
			.dest("in")
			.type(String.class)
			.required(true)
			.help("Input file");
		
		subparser
			.addArgument("-o", "--out")
			.dest("out")
			.type(String.class)
			.required(false)
			.help("Output file");
		
		subparser
			.addArgument("-u", "--url")
			.dest("url")
			.type(URL.class)
			.required(true)
			.help("WebTEST services base URL");
		
        subparser
        	.addArgument("-e", "--endpoint")
        	.dest("endpoint")
        	.type(String.class)
            .required(true)
            .help("Endpoint");
        
        subparser.addArgument("-m", "--method")
        	.dest("method")
        	.type(String.class)
        	.required(false)
        	.help("Method");
	}

	@Override
	protected void run(Bootstrap<WebTESTConfiguration> bootstrap, Namespace namespace, WebTESTConfiguration configuration) throws Exception {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client
				.target(namespace.getString("url"))
				.path(namespace.getString("endpoint"));
		
		String fmt = FilenameUtils.getExtension(namespace.getString("in")).equalsIgnoreCase("sdf")
				? TESTConstants.abbrevFormatSDF
				: FilenameUtils.getExtension(namespace.getString("in")).equalsIgnoreCase("smi")
				? TESTConstants.abbrevFormatSMI
				: null; 
		
		PredictionQuery q = new PredictionQuery();
		String in = namespace.getString("in");
		q.setQuery(org.apache.commons.io.FileUtils.readFileToString(new File(in)));
		q.setFormat(fmt);
		q.setMethod(namespace.getString("method"));
		
		PredictionResult result = target
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(q, MediaType.APPLICATION_JSON_TYPE), PredictionResult.class);
		
		ObjectMapper mapper = new ObjectMapper();
		String out = namespace.getString("out");
		if ( Strings.isEmpty(out) )
			out = ToxPredictor.Utilities.FileUtils.replaceExtension(in, ".json");
		mapper.writeValue(new File(out), result);
	}
}
