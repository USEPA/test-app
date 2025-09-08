package gov.epa.test.api.predict;

import java.io.File;

/**
 * @author Todd Martin
 * This class creates API for running large batch calculations of TEST reports with plots
 * 
 */

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Database.ResolverDb2;

@SpringBootApplication
public class PredictApplication {
	
	private static final Logger logger = LogManager.getLogger(PredictApplication.class);

	public static void main(String[] args) {
		
		String method = TESTConstants.ChoiceConsensus;
//		List<String>endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		List<String>endpoints=RunFromSmiles.allEndpoints;
		
		ResolverDb2.sqlitePath="databases"+File.separator+"snapshot.db";
		SpringApplication.run(PredictApplication.class, args);
		WebTEST4.printEachPrediction=false;//so that doesnt out separate line for each endpoint and slow down server calcs
		

		for (int i=0;i<endpoints.size();i++) {
			String endpoint=endpoints.get(i);
			WebTEST4.loadTrainingData(endpoint,method);
			logger.info("{}", endpoint+" ("+(i+1)+" of "+endpoints.size()+")");
		}

	}

}
