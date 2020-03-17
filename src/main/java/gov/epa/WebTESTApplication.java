package gov.epa;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import gov.epa.cli.PokeCommand;
import gov.epa.cli.PredictCommand;
import gov.epa.filter.CORSFilter;
import gov.epa.resources.AADashboardResource;
import gov.epa.resources.BCFResource;
import gov.epa.resources.BPResource;
import gov.epa.resources.BulkPredictResource;
import gov.epa.resources.DM_LC50Resource;
import gov.epa.resources.DensityResource;
import gov.epa.resources.ER_BinaryResource;
import gov.epa.resources.ER_LogRBAResource;
import gov.epa.resources.FHM_LC50Resource;
import gov.epa.resources.FPResource;
import gov.epa.resources.MPResource;
import gov.epa.resources.MutagenicityResource;
import gov.epa.resources.NcctSearchResource;
import gov.epa.resources.PredictionResultsResource;
import gov.epa.resources.Rat_LD50Resource;
import gov.epa.resources.ReproToxResource;
import gov.epa.resources.STResource;
import gov.epa.resources.TCResource;
import gov.epa.resources.TP_IGC50Resource;
import gov.epa.resources.VPResource;
import gov.epa.resources.ViscosityResource;
import gov.epa.resources.WSResource;
import gov.epa.resources.WebReportResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebTESTApplication extends Application<WebTESTConfiguration>
{
	private static final Logger logger = LogManager.getLogger(WebTESTApplication.class);
	
	public static void main(final String[] args) throws Exception
	{
		logger.info("Starting {}...", getSoftwareTitle());
		new WebTESTApplication().run(args);
	}

	@Override
	public String getName()
	{
		return getSoftwareTitle();
	}

	public static String getSoftwareTitle()
	{
		return TESTConstants.SoftwareTitle + " v" + TESTConstants.SoftwareVersion;
	}
	
	@Override
	public void initialize(final Bootstrap<WebTESTConfiguration> bootstrap)
	{
		
		bootstrap.addBundle(new SwaggerBundle<WebTESTConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(WebTESTConfiguration configuration) {
				configuration.swaggerBundleConfiguration.setResourcePackage("gov.epa.resources");
				return configuration.swaggerBundleConfiguration;
			}
		});
		
		bootstrap.addCommand(new PredictCommand());
		bootstrap.addCommand(new PokeCommand());
	}

	@Override
	public void run(final WebTESTConfiguration configuration, final Environment environment)
	{
		WebTEST.preloadDatasets();
		
		environment.jersey().register(new CORSFilter());
		environment.jersey().register(new AADashboardResource());
		environment.jersey().register(new PredictionResultsResource());
		environment.jersey().register(new WebReportResource());
		
		// Biological
		environment.jersey().register(new FHM_LC50Resource());
		environment.jersey().register(new DM_LC50Resource());
		environment.jersey().register(new TP_IGC50Resource());
		environment.jersey().register(new Rat_LD50Resource());
		// environment.jersey().register(new GA_EC50Resource());	// TODO: There is no training data - see "TEST-52 EC50GA does not work"
		environment.jersey().register(new BCFResource());
		environment.jersey().register(new ReproToxResource());
		environment.jersey().register(new MutagenicityResource());
		environment.jersey().register(new ER_BinaryResource());
		environment.jersey().register(new ER_LogRBAResource());
		
		// PhysChem
		environment.jersey().register(new BPResource());
		environment.jersey().register(new DensityResource());
		environment.jersey().register(new FPResource());
		environment.jersey().register(new MPResource());
		environment.jersey().register(new STResource());
		environment.jersey().register(new TCResource());
		environment.jersey().register(new VPResource());
		environment.jersey().register(new ViscosityResource());
        environment.jersey().register(new WSResource());
        environment.jersey().register(new BulkPredictResource());
        environment.jersey().register(new NcctSearchResource());
		
	}
}
