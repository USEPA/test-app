package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;

/**
* @author TMARTI02
*/
public class RunFromSmilesTest {

	
	

	public static void runSDF_all_endpoints_write_continuously(String SDFFilePath, String destJsonPath,boolean skipMissingSID,int maxCount,boolean debug) {

		
		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs= RunFromSmiles.readSDFV3000(SDFFilePath);
		if (debug) System.out.println("atom container count="+acs.getAtomContainerCount());
		AtomContainerSet acs2 = RunFromSmiles.filterAtomContainerSet(acs, skipMissingSID,maxCount);
		if (debug) System.out.println("atom container count="+acs2.getAtomContainerCount());

		try {
			
			File destFile=new File(destJsonPath);
			
//			if (destFile.exists() && destFile.length()>0) {
//				int count=RunFromSmiles.removeAlreadyRanChemicals(destJsonPath, acs2);
//				if (debug) System.out.println("Count removed since already ran="+count);
//			}
			
			FileWriter fw=new FileWriter(destJsonPath,destFile.exists());
			
			for (int i=0;i<acs2.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer) acs2.getAtomContainer(i);

				if (debug) System.out.println("Running:\t"+ac.getProperty(DSSToxRecord.strSmiles));
				
				AtomContainerSet acs3=new AtomContainerSet();
				acs3.addAtomContainer(ac);


				List<PredictionResults>results=RunFromSmiles.runEndpointsAsList(acs3, RunFromSmiles.allEndpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);
				
				for (PredictionResults pr:results) {
					fw.write(RunFromSmiles.gsonNotPretty.toJson(pr)+"\r\n");
					fw.flush();
				}
			}
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
public static void runSDF_all_endpoints_write_continuously_single(String SDFFilePath, String destJsonPath,boolean skipMissingSID,boolean debug) {

//		String [] endpoints=RunFromSmiles.allEndpoints;
		List<String> endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		
		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs= RunFromSmiles.readSDFV3000(SDFFilePath);
		if (debug) System.out.println("atom container count="+acs.getAtomContainerCount());
		AtomContainerSet acs2 = RunFromSmiles.filterAtomContainerSet(acs, skipMissingSID,1);
		if (debug) System.out.println("atom container count="+acs2.getAtomContainerCount());

		try {
			
			File destFile=new File(destJsonPath);
			
//			if (destFile.exists() && destFile.length()>0) {
//				int count=RunFromSmiles.removeAlreadyRanChemicals(destJsonPath, acs2);
//				if (debug) System.out.println("Count removed since already ran="+count);
//			}
			
			FileWriter fw=new FileWriter(destJsonPath,destFile.exists());
			
			
			AtomContainer ac=(AtomContainer) acs2.getAtomContainer(0);

			if (debug) System.out.println("Running:\t"+ac.getProperty(DSSToxRecord.strSmiles));

			AtomContainerSet acs3=new AtomContainerSet();
			acs3.addAtomContainer(ac);


			List<PredictionResults>results=RunFromSmiles.runEndpointsAsList(acs3, endpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);

			for (PredictionResults pr:results) {
				fw.write(RunFromSmiles.gson.toJson(pr)+"\r\n");
				fw.flush();
			}

			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static void main(String[] args) {
		
		boolean debug=true;

//		String folderSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\sdf\\";
//		String fileNameSDF="snapshot_compounds1.sdf";
//		String filepathSDF=folderSrc+fileNameSDF;
//
//		int maxCount=10;//set to -1 to run all in sdf
//		boolean skipMissingSID=true;
////		String destJsonPath="reports/TEST_results_all_endpoints_"+fileNameSDF.replace(".sdf", ".json");
//		String destJsonPath="reports/sampleBob8.json";
//		runSDF_all_endpoints_write_continuously(filepathSDF,destJsonPath,skipMissingSID,maxCount,debug);


		String folderSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\sdf\\";
		String fileNameSDF="sample.sdf";
		String filepathSDF=folderSrc+fileNameSDF;
		String destJsonPath="reports/sample_single.json";
		runSDF_all_endpoints_write_continuously_single(filepathSDF,destJsonPath,true,debug);
	}

}
