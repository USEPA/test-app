package ToxPredictor.misc;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FixReports {

	void fixFolder(File folder) {
		
		String strOld="Bioaccumulation factor";
		String strNew="Bioconcentration factor";
		
		File Folder2=new File(folder.getAbsolutePath()+"/web-reports/ToxRuns");
		
		if (!Folder2.exists())  {
			System.out.println("No ToxRuns folder:"+Folder2.getAbsolutePath());
			return;
		}
		
		File [] files=Folder2.listFiles();
		
//		System.out.println("done listing files");
		
		for (int i=0;i<files.length;i++) {
			
			if (i%100==0) System.out.println(i);
			
			File filei=files[i];
			
			if (filei.getName().indexOf("ToxRun")==-1) continue;
			
			File folderBCFold=new File(files[i].getAbsolutePath()+File.separator+strOld);
			File folderBCFnew=new File(files[i].getAbsolutePath()+File.separator+strNew);
			
			if (folderBCFold.exists()) {
				folderBCFold.renameTo(folderBCFnew);
			}
						
			File fileBCFjson=new File(folderBCFnew.getAbsolutePath()+File.separator+"PredictionResultsConsensus.json");
			File fileBCFhtml=new File(folderBCFnew.getAbsolutePath()+File.separator+"PredictionResultsConsensus.html");
			
			try {

				String strJSON=FileUtils.readFileToString(fileBCFjson);
				strJSON=strJSON.replace(strOld, strNew);
				
				String strHTML=FileUtils.readFileToString(fileBCFhtml);
				strHTML=strHTML.replace(strOld, strNew);

				FileUtils.writeStringToFile(fileBCFjson, strJSON);
				FileUtils.writeStringToFile(fileBCFhtml, strHTML);
				
//				if (filei.getName().equals("ToxRun_11-30-3")) {
//					System.out.println(fileBCFjson.getAbsolutePath());
//					System.out.println(strJSON);
//					FileUtils.writeStringToFile(fileBCFjson, strJSON);
//					FileUtils.writeStringToFile(fileBCFhtml, strHTML);
//				}
					
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FixReports fr=new FixReports();

		
//		String folder="R:/NCCT Results 11_16_17/web-reports-qr9";
//		fr.fixFolder(folder);

		File mainFolder=new File("R:/NCCT Results 11_16_17");
		
		File [] files=mainFolder.listFiles();
		
		for (int i=0;i<files.length;i++) {
			if (!files[i].isDirectory()) continue;
			
			if (files[i].getName().indexOf("web-reports-")==-1) continue;
			
			System.out.println(files[i].getName());
			
			fr.fixFolder(files[i]);
			
		}
		
	}

}
