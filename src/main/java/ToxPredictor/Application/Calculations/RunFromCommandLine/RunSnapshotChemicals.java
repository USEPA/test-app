package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.io.File;

import ToxPredictor.Application.WebTEST4;

/**
* @author TMARTI02
*/
public class RunSnapshotChemicals {

	public static void main(String[] args) {

		
		if(args==null) {
			System.out.println("Missing args");
			return;
		}
		int num =  Integer.parseInt(args[0]);
		
		boolean removeAlreadyRan = true;
		
		String snapshot="snapshot-2025-07-30";
		String folderSrc = "data"+File.separator+"dsstox"+File.separator+snapshot;
		String folderDest ="data"+File.separator+"TEST5.1.3"+File.separator+"reports"+File.separator+snapshot;
		new File(folderDest).mkdirs();
		
		int from=1+50000*(num-1);
		String filenameSDF = "50k_chunk_from_"+from+".sdf";

		String filenameJson = filenameSDF.replace(".sdf", ".json");

		int maxCount = -1;// set to -1 to run all in sdf
		boolean skipMissingSID = true;

		String sdfPath = folderSrc+File.separator + filenameSDF;
		String destJsonPath = folderDest+File.separator+ filenameJson;
		
		File f=new File(sdfPath);
		
		if(!f.exists()) {
			System.out.println("Missing sdf:"+f.getAbsolutePath());
			return;
		} else {
			System.out.println("Running sdf:"+f.getAbsolutePath());
		}
		
		WebTEST4.printEachPrediction=false;
		
		RunFromSDF.runSDF_all_endpoints_write_continuously(sdfPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan);

		

	}

}
