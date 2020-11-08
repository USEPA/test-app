package ToxPredictor.Application.GUI;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Vector;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.TESTConstants;
//import gov.epa.api.Chemical;

// Class to store settings for fraMain7
//Ideally this class would be inside fraMain7 but this is quickest solution
//TODO: add more settings

public class ApplicationSettings implements Serializable{
	

	private String outputFolderPath;
	
	
	private boolean CreateDetailedReports=false;//default
	private boolean CreateReports=false;//default
	

	private boolean RelaxFragmentConstraint=false;//default
	
	private String Endpoint=TESTConstants.ChoiceFHM_LC50;//default
	private String Method=TESTConstants.ChoiceConsensus;//default

	private Vector<String>recentFilePaths=new Vector<String>();
	private Vector<String>recentBatchFilePaths=new Vector<String>();
	
	private int maxNumRecentFilePaths=10;
	private static String strUserFolder=System.getProperty("user.home") + File.separator + ".TEST";
//	private static String xmlFilePath=strUserFolder+File.separator+"settings.xml";
	private static String jsonFilePath=strUserFolder+File.separator+"settings.json";
	

	
	public boolean isCreateReport() {
		return CreateReports;
	}

	public void setCreateReport(boolean createReport) {
		CreateReports = createReport;
	}

	public String getEndpoint() {
		return Endpoint;
	}

	public void setEndpoint(String endpoint) {
		Endpoint = endpoint;
	}

	public String getMethod() {
		return Method;
	}

	public void setMethod(String method) {
		Method = method;
	}

	public Vector<String> getRecentFilePaths() {
		return recentFilePaths;
	}

	public void setRecentFilePaths(Vector<String> recentFilePaths) {
		this.recentFilePaths = recentFilePaths;
	}


	public boolean isCreateDetailedReport() {
		return CreateDetailedReports;
	}

	public void setCreateDetailedReport(boolean createDetailedReport) {
		CreateDetailedReports = createDetailedReport;
	}

	public boolean isRelaxFragmentConstraint() {
		return RelaxFragmentConstraint;
	}

	public void setRelaxFragmentConstraint(boolean relaxFragmentConstraint) {
		RelaxFragmentConstraint = relaxFragmentConstraint;
	}
	
	public void setOutputFolderPath(String outputFolderPath) {
		this.outputFolderPath = outputFolderPath;
	}

	public String getOutputFolderPath() {
		return outputFolderPath;
	}
	
	
	/**
	 * Adds a filepath but makes sure it is added at the end and deletes any duplicates
	 * @param filepath
	 */
	public void addFilePath(String filepath) {
		//remove any matches to current file path
		
		Vector<String> vec=this.recentFilePaths;
		
		for (int i=0;i<vec.size();i++) {
			if (vec.get(i).equals(filepath)) {
				vec.remove(i);
			}
		}
		
		//remove any duplicates
		for (int i=0;i<vec.size();i++) {
			for (int j=i+1;j<vec.size();j++) {
				if (vec.get(i).equals(vec.get(j))) {
					vec.remove(i);
					i--;
				}
			}
		}

		//add filepath to end:
		vec.add(filepath);
		
		//if after removing duplicates we have too many, delete first one:
		if (vec.size()>maxNumRecentFilePaths) {
			vec.remove(0);
		}
		
//		for (int i=0;i<recentFilePaths.size();i++) {
//			String filepathi=recentFilePaths.get(i);
//			File file=new File(filepathi);
//			System.out.println(i+"\t"+file.getName());
//		}
		
	}
	
	public void addBatchFilePath(String filepath) {
		
		Vector<String> vec=this.recentBatchFilePaths;
		
		for (int i=0;i<vec.size();i++) {
			if (vec.get(i).equals(filepath)) {
				vec.remove(i);
			}
		}
		
		//remove any duplicates
		for (int i=0;i<vec.size();i++) {
			for (int j=i+1;j<vec.size();j++) {
				if (vec.get(i).equals(vec.get(j))) {
					vec.remove(i);
					i--;
				}
			}
		}

		//add filepath to end:
		vec.add(filepath);
		
		//if after removing duplicates we have too many, delete first one:
		if (vec.size()>maxNumRecentFilePaths) {
			vec.remove(0);
		}
		
//		System.out.println("");
//		for (int i=0;i<vec.size();i++) {
//			System.out.println(vec.get(i));
//		}

	}

	
	
//	public Vector<String> getRecentBatchFilePaths() {
//		return recentBatchFilePaths;
//	}
//
//	public void setRecentBatchFilePaths(Vector<String> recentBatchFilePaths) {
//		this.recentBatchFilePaths = recentBatchFilePaths;
//	}

	public void saveSettingsToFile() {
		
//		System.out.println(recentFilePaths.size());
//		this.deleteRedundantRecentFiles();
//		System.out.println(recentFilePaths.size());
		
		File userFolder=new File(strUserFolder);		
		if (!userFolder.exists()) userFolder.mkdir();
		
		//TODO: create class to store settings...
		try {
			
//			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlFilePath)));
//			
//			encoder.writeObject(this);
//			encoder.flush();
//			encoder.close();
			
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String json=gson.toJson(this);
			
			FileWriter fw = new FileWriter(jsonFilePath);
			fw.write(json);
			fw.flush();
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	public ApplicationSettings loadSettingsFromFile() {		
		File userFolder=new File(strUserFolder);
		//		System.out.println(strUserFolder);
		File jsonFile=new File(jsonFilePath);
		
		if (!userFolder.exists() || !jsonFile.exists()) {
			return new ApplicationSettings();
		}

		ApplicationSettings as=null;

		//TODO: create class to store settings...
		try {
			//			System.out.println(xmlFilePath);
			//			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(xmlFilePath)));
			//			as=(applicationSettings)decoder.readObject();
			//			decoder.close();


			Gson gson = new Gson();

			FileReader fr=new FileReader(jsonFilePath);
			as = gson.fromJson(fr, this.getClass());
			fr.close();


		} catch (Exception e) {
			as=new ApplicationSettings();	
			System.out.println("Error loading Application settings, creating new one");
		}
		return as;

	}

}
