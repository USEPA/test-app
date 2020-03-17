import static org.junit.Assert.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ToxPredictor.Application.WebTEST;

public class CLITests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testArguments() throws ParseException {
		Options options = WebTEST.createOptions();
		String[] args = new String[] { "-i", "data\\BP\\BP_prediction.sdf", "-e", "BP", "-m", "hc" }; 
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		assertTrue(cmd.hasOption("input"));
	}
}
