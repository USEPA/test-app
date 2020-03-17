import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import AADashboard.Application.GenerateRecordsFromTEST;
import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST2;
import ToxPredictor.Utilities.CDKUtilities;
import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;
import gov.epa.api.TESTRecord;

public class CompareQSARModelResults {

	/**
	 * Load predictions from TEST 4.2 text file that has predictions from all methods
	 * @param endpoint
	 * @param set
	 * @return
	 */
	Instances loadOldResults(String endpoint,String set) {
		Instances instances=null;
		
		CSVLoader c=new CSVLoader();
		
		try {
//			String filepath=endpoint+"/"+endpoint+" "+set+" set predictions.txt";
//			java.io.InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filepath);
//			instances=c.getDatasetFromInputStream(ins, "\t");
			
			String filepath="data/"+endpoint+"/"+endpoint+" "+set+" set predictions.txt";
			instances=c.getDataSetFromFile(filepath, "\t");
		
// test it out:
//		Instance instance0=instances.instance(0);
//		String CAS=instance0.getName();
//		double HCValue=instance0.value("Hierarchical clustering");
//		System.out.println(CAS+"\t"+HCValue);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return instances;
		
	}
	
	/**
	 * Load predictions from web-test unit test
	 * 
	 * @param endpoint
	 * @param abbrev
	 * @param method
	 * @param set
	 * @return
	 */
	Instances loadNewResults(String endpoint,String abbrev,String method,String set) {
//		test-results\Bioaccumulation factor\Hierarchical clustering
		
		Instances instances=null;
		String filepath="test-results/"+endpoint+"/"+method+"/"+abbrev+"_"+set+".txt";
		try {
			
			CSVLoader c=new CSVLoader();
//			instances=c.getDataSetFromFile(filepath, "\t");
			
			File file=new File(filepath);
			
			if (!file.exists()) return null;
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();//not needed since pred column doesnt change
			
			instances=new Instances();
			
			String [] attributes={"pred"};
			instances.setAttributes(attributes);
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				
				String CAS=list.get(1);
				
				String strExp=list.get(2);
				double Tox=-9999;
				
				if (!strExp.equals("N/A")) {
					Tox=Double.parseDouble(list.get(2));
				}
				
				String strPred=list.get(3);
				double pred=-9999;
				
				if (!strPred.equals("N/A")) {
					pred=Double.parseDouble(strPred);	
				}
				 
				double [] descriptors={pred};//for now just store the one pred value
				
				Instance instance=new Instance();
				instance.setName(CAS);
				instance.setToxicity(Tox);
				instance.setAttributes(attributes);
				instance.setDescriptors(descriptors);
				instances.addInstance(instance);
			}
			
			br.close();

			//Test it:
//			Instance instance0=instances.instance(0);
//			String CAS=instance0.getName();
//			double pred=instance0.value("pred");
//			System.out.println(CAS+"\t"+pred);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return instances;
		
	}
	
	/**
	 * Compare QSAR predictions from TEST 4.2 and from unit test for web-test
	 * 
	 * @param instancesOld
	 * @param instancesNew
	 * @param endpoint
	 * @param endpointAbbrev
	 * @param method
	 * @param set
	 * @param vecCAS
	 */
	void compare(Instances instancesOld,Instances instancesNew,String endpoint,String endpointAbbrev, String method,String set,Vector<String>vecCAS) {

		String filepath="test-results/"+endpoint+"/"+method+"/"+endpointAbbrev+"_"+set+"_compare.txt";

		DecimalFormat df=new DecimalFormat("0.000");

		try {



			System.out.println("\r\n"+endpointAbbrev+"\t"+method+"\t"+set);

			if (instancesNew==null) {
				System.out.println("new results missing");
				return;
			}


			FileWriter fw=new FileWriter(filepath);

			//			if (instancesOld.numInstances()!=instancesNew.numInstances()) {
			//				fw.write("number of instances old="+instancesOld.numInstances()+"\r\n");
			//				fw.write("number of instances new="+instancesNew.numInstances()+"\r\n");
			//				
			//				System.out.print("number of instances old="+instancesOld.numInstances()+"\r\n");
			//				System.out.print("number of instances new="+instancesNew.numInstances()+"\r\n");
			//				
			//			}

			System.out.println("CAS\texpVal\tvalOld\tvalNew\terr");
			fw.write("CAS\texpVal\tvalOld\tvalNew\terr\r\n");

			for (int i=0;i<instancesOld.numInstances();i++) {
				Instance iOld=instancesOld.instance(i);
				//				Instance iNew=instancesNew.instance(i);
				//
				String CASold=iOld.getName();
				//				String CASnew=iNew.getName();

				Instance iNew=instancesNew.instance(CASold);

				if (iNew==null) {
					fw.write("CAS not found:"+CASold+"\r\n");
					System.out.print("CAS not found:"+CASold+"\r\n");
					continue;
				}

				//				if (!CASold.equals(CASnew)) {
				//					fw.write("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
				//					System.out.print("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
				//					break;
				//				}

				double expVal=iOld.getToxicity();

				double valOld=iOld.value(method);
				double valNew=iNew.value("pred");

				double err=Math.abs(valOld-valNew);


				if (err>0.01) {
					if (vecCAS.contains(CASold)) {
						fw.write(CASold+"*\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err)+"\r\n");
						System.out.print(CASold+"*\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err)+"\r\n");	

					} else {
						fw.write(CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err)+"\r\n");
						System.out.print(CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err)+"\r\n");	
					}

					fw.flush();
				}

			}
			//			fw.write("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");
			//			System.out.print("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

void compareInChiKey(String CAS,String endpoint) {
	
	
	String dbFilePath="databases/TEST_sets_2018_10_04.db";
	String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
	
	
	
	try {
		Connection conn=MySQL_DB.getConnection(dbFilePath);
		Statement stat=conn.createStatement();
		
		TESTRecord tr=GenerateRecordsFromTEST.getTESTRecord(stat, endpointAbbrev, "CAS", CAS);
		
		String sdfPath="data/"+endpointAbbrev+"/"+endpointAbbrev+"_prediction.sdf";
		
		
		
		AtomContainerSet acs = WebTEST2.LoadFromSDF(sdfPath);
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			
			
			if(ac.getProperty("CAS").equals(CAS)) {
				String [] results=CDKUtilities.generateInChiKey(ac);
				String inchiKeyOld=results[1];
				
				System.out.println(inchiKeyOld+"\t"+tr.InChiKey+"\t"+inchiKeyOld.compareTo(tr.InChiKey));
				break;
			}
			
		}
		
		
		
		
		
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	
}
	
Hashtable<String,String> getInChiKeyMap(String endpointAbbrev, String set) {
	Hashtable<String,String>htInChiKey=new Hashtable<>();
	ArrayList<String>inchiLines=ToxPredictor.Utilities.Utilities.readFileToArray("data/0 compare TEST set predictions/inchikeys/"+endpointAbbrev+"_"+set+".txt");
	for (String line:inchiLines) {
		String CAS=line.substring(0,line.indexOf("\t"));
		String inchi=line.substring(line.indexOf("\t")+1,line.length());
		htInChiKey.put(CAS, inchi);
		
//		System.out.println(CAS+"\t"+inchi);
		
	}
	return htInChiKey;

}
	
/**
 * Compare QSAR predictions from TEST 4.2 and from unit test for web-test
 * 
 * @param instancesOld
 * @param instancesNew
 * @param endpoint
 * @param endpointAbbrev
 * @param method
 * @param set
 * @param vecCAS
 */
void compareToDB_useInChiKey(FileWriter fw,Instances instancesOld,Statement stat,String endpoint,String endpointAbbrev, String method,String set,Vector<String>vecCAS,Vector<String>notFoundCAS,Hashtable<String,String>htInChiKey) {
	
	DecimalFormat df=new DecimalFormat("0.000");

	try {

		if (stat==null) {
			System.out.println("db missing");
			return;
		}

		Vector<String>results=new Vector<String>();
		

		for (int i=0;i<instancesOld.numInstances();i++) {
			Instance iOld=instancesOld.instance(i);
//			Instance iNew=instancesNew.instance(i);
//
			String CASold=iOld.getName();
			
			
			String InChiKey=htInChiKey.get(CASold);
			
//			System.out.println(CASold+"\t"+InChiKey);
			
			TESTRecord tr=GenerateRecordsFromTEST.getTESTRecord(stat, endpointAbbrev, "InChiKey", InChiKey);
			
			if (tr==null) {
				if (!notFoundCAS.contains(CASold)) notFoundCAS.add(CASold);
//				fw.write("CAS not found:"+CASold+"\r\n");
				System.out.print(method+"\t"+endpoint+"\tCAS not found:"+CASold+"\r\n");
				continue;
			}

//			if (!CASold.equals(CASnew)) {
//				fw.write("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
//				System.out.print("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
//				break;
//			}

			double expVal=iOld.getToxicity();

			double valOld=iOld.value(method);
			
//			System.out.println(method);
			
			
			double valNew=0;
			
			if (method.equals("Hierarchical clustering")) {
				String strNewVal=tr.Hierarchical;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Single model")) {
				String strNewVal=tr.SingleModel;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Group contribution")) {
				String strNewVal=tr.GroupContribution;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Nearest neighbor")) {
				String strNewVal=tr.NearestNeighbor;
				valNew = getDoubleVal(strNewVal);
			} else {
				System.out.println("unknown method:"+method);
				continue;
			}

			double err=Math.abs(valOld-valNew);

//			System.out.println(CASold+"\t"+err);
		
//			System.out.println(CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err));
					
			
			if (err>0.01) {
				
				String line="";
				if (vecCAS.contains(CASold)) {
					line=CASold+"*\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err);
				} else {
					line=CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err);					
				}
				
				results.add(line);				
				fw.flush();
			}
		}//end loop over instances
		

		
		if (results.size()>0) {

			fw.write(endpoint+"\t"+method+"\t"+set+"\r\n");
			System.out.println(endpoint+"\t"+method+"\t"+set);

			fw.write("CAS\texpVal\tvalOld\tvalNew\terr\r\n");
			for (String line:results) {
				fw.write(line+"\r\n");
				System.out.println(line);
			}
			fw.write("\n");
			System.out.println("");
		}
		
//		fw.write("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");
//		System.out.print("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");
		
		

	} catch (Exception ex) {
		ex.printStackTrace();
	}
}

/**
 * Compare QSAR predictions from TEST 4.2 and from unit test for web-test
 * 
 * @param instancesOld
 * @param instancesNew
 * @param endpoint
 * @param endpointAbbrev
 * @param method
 * @param set
 * @param vecCAS
 */
void compareToDB(FileWriter fw,Instances instancesOld,Statement stat,String endpoint,String endpointAbbrev, String method,String set,Vector<String>vecCAS,Vector<String>notFoundCAS) {
	
	DecimalFormat df=new DecimalFormat("0.000");

	try {

		if (stat==null) {
			System.out.println("db missing");
			return;
		}

		Vector<String>results=new Vector<String>();
		

		for (int i=0;i<instancesOld.numInstances();i++) {
			Instance iOld=instancesOld.instance(i);
//			Instance iNew=instancesNew.instance(i);
//
			String CASold=iOld.getName();
			
			
//			System.out.println(CASold+"\t"+InChiKey);
			
			TESTRecord tr=GenerateRecordsFromTEST.getTESTRecord(stat, endpointAbbrev, "CAS", CASold);
			
			if (tr==null) {
				if (!notFoundCAS.contains(CASold)) notFoundCAS.add(CASold);
//				fw.write("CAS not found:"+CASold+"\r\n");
//				System.out.print(method+"\t"+endpoint+"\tCAS not found:"+CASold+"\r\n");
				continue;
			}

//			if (!CASold.equals(CASnew)) {
//				fw.write("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
//				System.out.print("CAS mismatch:"+CASold+"\t"+CASnew+"\r\n");
//				break;
//			}

			double expVal=iOld.getToxicity();

			double valOld=iOld.value(method);
			
//			System.out.println(method);
			
			
			double valNew=0;
			
			if (method.equals("Hierarchical clustering")) {
				String strNewVal=tr.Hierarchical;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Single model")) {
				String strNewVal=tr.SingleModel;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Group contribution")) {
				String strNewVal=tr.GroupContribution;
				valNew = getDoubleVal(strNewVal);
			} else if (method.equals("Nearest neighbor")) {
				String strNewVal=tr.NearestNeighbor;
				valNew = getDoubleVal(strNewVal);
			} else {
				System.out.println("unknown method:"+method);
				continue;
			}

			double err=Math.abs(valOld-valNew);

//			System.out.println(CASold+"\t"+err);
		
//			System.out.println(CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err));
					
			
			if (err>0.01) {
				
				String line="";
				if (vecCAS.contains(CASold)) {
					line=CASold+"*\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err);
				} else {
					line=CASold+"\t"+expVal+"\t"+valOld+"\t"+valNew+"\t"+df.format(err);					
				}
				
				results.add(line);				
				fw.flush();
			}
		}//end loop over instances
		

		
		if (results.size()>0) {

			fw.write(endpoint+"\t"+method+"\t"+set+"\r\n");
			System.out.println(endpoint+"\t"+method+"\t"+set);

			fw.write("CAS\texpVal\tvalOld\tvalNew\terr\r\n");
			for (String line:results) {
				fw.write(line+"\r\n");
				System.out.println(line);
			}
			fw.write("\n");
			System.out.println("");
		}
		
//		fw.write("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");
//		System.out.print("* Denotes chemical that has molecular descriptor discrepancy"+"\r\n");
		
		

	} catch (Exception ex) {
		ex.printStackTrace();
	}
}

private double getDoubleVal(String strNewVal) {
	double valNew;
	if (strNewVal.equals("N/A")) {
		valNew=-9999;
	} else {
		valNew=Double.parseDouble(strNewVal);
	}
	return valNew;
}
	
	/**
	 * Parse the output for TEST4.2 from output text files. It then copies them back to the data folder.
	 * 
	 */
	void getTEST4results() {
		Vector<String>endpoints=new Vector<String>();
		for (int e : TESTParams.endpoints) {
		    endpoints.add(TESTConstants.getEndpoint(e));
		}
		
		
		for (int i=0;i<endpoints.size();i++) {
			String endpoint=endpoints.get(i);
			String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);

//			createPredictionFile(endpoint, endpointAbbrev, "training");
//			createPredictionFile(endpoint, endpointAbbrev, "test");
			
			this.createPredictionFile2(endpoint, endpointAbbrev, "training");
			this.createPredictionFile2(endpoint, endpointAbbrev, "prediction");

			
		}
		//TODO- make it generate new jar file?
		
	}

	
	String getHeader() {
		
		Vector <String> methods=new Vector<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);
		methods.add(TESTConstants.ChoiceSingleModelMethod);
		methods.add(TESTConstants.ChoiceFDAMethod);
		methods.add(TESTConstants.ChoiceGroupContributionMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);
		methods.add(TESTConstants.ChoiceConsensus);

		String newHeader="CAS\texpToxicValue\t";

		for (int i=0;i<methods.size();i++) {
			newHeader+=methods.get(i);
			if (i<methods.size()-1) {
				newHeader+="\t";
			}
		}
		
		return newHeader;
		
	}
	
	/**
	 * This method loops through all the results files in the MyToxicity and compiles text file with 
	 * predictions from all methods. This way we can combine multiple runs until we get all the chemicals
	 * from a given training or prediction set
	 * 
	 * 
	 * @param endpoint
	 * @param endpointAbbrev
	 * @param set
	 */
	void createPredictionFile2(String endpoint,String endpointAbbrev,String set) {
		
		String trainingCSV="data/"+endpointAbbrev+"/"+endpointAbbrev+"_"+set+"_set-2d.csv";
		CSVLoader c=new CSVLoader();

		try {
			Instances instances=c.getDataSetFromFile(trainingCSV, ",");
//			System.out.println(iTrain.numInstances());
//			System.out.println(endpoint);
			
			File folder=new File("L:\\Priv\\Cin\\NRMRL\\CompTox\\MyToxicity");
			File [] files=folder.listFiles();
			
			
			String endpoint2=endpoint.replace(" ", "_");
			Vector<String>vecOverall=new Vector<String>();
			for (int i=0;i<files.length;i++) {
				
				if (files[i].getName().indexOf("Batch_"+endpoint2+"_all_methods")>-1) {
					
//					System.out.println(files[i].getName());	
					Vector<String>vec=this.getPredictionsFromTEST4_2(files[i].getAbsolutePath(), endpoint, instances);
					
					if (vec!=null) {
						System.out.println(files[i].getName()+"\t"+vec.size());
						
						for (int j=0;j<vec.size();j++) {
							if (!vecOverall.contains(vec.get(j))) {
								vecOverall.add(vec.get(j));
							}
						}
						
					}
					
				}
			}
			
			System.out.println(vecOverall.size()+" out of "+instances.numInstances()+"\r\n");
			
			Collections.sort(vecOverall);//sort results by CAS number 
			
			String newHeader=this.getHeader();

			if (set.equals("prediction")) set="test";//need to make sure results file is named consistently like we did before
			
			File fileOut=new File("test4.2-results/"+endpointAbbrev+"/"+endpointAbbrev+" "+set+ " set predictions.txt");
			FileWriter fw=new FileWriter(fileOut);
			
			fw.write(newHeader+"\r\n");
			for (int i=0;i<vecOverall.size();i++) {
				String line=vecOverall.get(i);
				while (line.indexOf("0")==0) {//trim off zeros added to make cas numbers sort correctly
					line=line.substring(1, line.length());
				}
				
				vecOverall.set(i, line);
				
				fw.write(line+"\r\n");
				fw.flush();
			}
			fw.close();
			
			//output missing:
			for (int i=0;i<instances.numInstances();i++) {
				String CASi=instances.instance(i).getName();
				
				boolean haveCAS=false;
				for (int j=0;j<vecOverall.size();j++) {
					String linej=vecOverall.get(j);
					String CASj=linej.substring(0,linej.indexOf("\t"));
					
					if (CASi.equals(CASj)) {
						haveCAS=true;
						break;
					}
				}
				
				if (!haveCAS) {
					System.out.println(CASi);
				}
			}
			
			System.out.println("");
			
			
			File fileData=new File("data/"+endpointAbbrev+"/"+endpointAbbrev+" "+set+ " set predictions.txt");
			
			//Copy to data folder:
			ToxPredictor.Utilities.Utilities.CopyFile(fileOut, fileData);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
			                
		
	}
	
	/**
	 * Compile results from multiple runs to get all the predictions from the gui
	 */
	void compileTEST4_2_results() {
		
		String endpoint=TESTConstants.ChoiceRat_LD50;
		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		this.createPredictionFile2(endpoint, endpointAbbrev, "training");
		this.createPredictionFile2(endpoint, endpointAbbrev, "prediction");
	}
	
	
	
	
	
	/**
	 * Go through text file that has predictions from all methods generated by TEST4.2
	 * 
	 * @param endpoint
	 * @param endpointAbbrev
	 * @param set
	 */
	void createPredictionFile(String endpoint, String endpointAbbrev, String set) {
		
		int num=-1;
		
		if (set.equals("training")) {
			num=1;
		} else if (set.equals("test")) {
			num=2;
		}
		
		String trainingInputPath="MyToxicity/Batch_"+endpoint.replace(" ", "_")+"_all_methods_"+num+".txt";
		
		File trainFile=new File(trainingInputPath);
		
		System.out.println(endpoint+"\t"+set);
			
		try {
			
//			CAS	expToxicValue	Hierarchical clustering	Single model	FDA	Group contribution	Nearest neighbor	Consensus
//			#|ID|Exp|Pred_Hierarchical clustering|Pred_Single model|Pred_Group contribution|Pred_FDA|Pred_Nearest neighbor|Pred_Consensus
			FileInputStream fis=new FileInputStream(trainFile);
			BufferedReader br=new BufferedReader(new java.io.InputStreamReader(fis));
			
			File folder=new File("test4.2-results/"+endpointAbbrev);
			
			if (!folder.exists()) folder.mkdir();
			
			File fileOut=new File("test4.2-results/"+endpointAbbrev+"/"+endpointAbbrev+" "+set+ " set predictions.txt");
			FileWriter fw=new FileWriter(fileOut);
										
			if (!endpoint.equals(TESTConstants.ChoiceMutagenicity) && !endpoint.equals(TESTConstants.ChoiceReproTox)) br.readLine();//discard note line about units
			String header=br.readLine();
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "ID","|");
			int colExp=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Exp","|");
			
			int colHC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceHierarchicalMethod,"|");
			int colSM=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceSingleModelMethod,"|");
			int colGC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceGroupContributionMethod,"|");
			int colFDA=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceFDAMethod,"|");
			int colNN=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceNearestNeighborMethod,"|");
			int colC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceConsensus,"|");
		
			fw.write(getHeader()+"\r\n");
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "|");
				
				String CAS=list.get(colCAS);
				String exp=list.get(colExp);
				String HC=list.get(colHC);
				
				String SM="-9999";
				if (colSM!=-1) {
					SM=list.get(colSM);	
				}
				
				String GC="-9999";
				if (colGC!=-1) {
					GC=list.get(colGC);	
				}
				
				String FDA=list.get(colFDA);
				String NN=list.get(colNN);
				String C=list.get(colC);
				
				String newLine=CAS+"\t"+exp+"\t"+HC+"\t"+SM+"\t"+FDA+"\t"+GC+"\t"+NN+"\t"+C+"\r\n";
				newLine=newLine.replace("N/A", "-9999");
				
				fw.write(newLine);
				fw.flush();
				
			}
			fw.close();
			
			File fileData=new File("data/"+endpointAbbrev+"/"+endpointAbbrev+" "+set+ " set predictions.txt");
			
			//Copy to data folder:
			ToxPredictor.Utilities.Utilities.CopyFile(fileOut, fileData);
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Go through text file that has predictions from all methods generated by TEST4.2
	 * 
	 * @param endpoint
	 * @param endpointAbbrev
	 * @param set
	 */
	Vector<String> getPredictionsFromTEST4_2(String filepath, String endpoint,Instances instances) {
		
		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		
		Vector<String>vec=new Vector<String>();
		
		
		try {
			
//			CAS	expToxicValue	Hierarchical clustering	Single model	FDA	Group contribution	Nearest neighbor	Consensus
//			#|ID|Exp|Pred_Hierarchical clustering|Pred_Single model|Pred_Group contribution|Pred_FDA|Pred_Nearest neighbor|Pred_Consensus
			FileInputStream fis=new FileInputStream(filepath);
			BufferedReader br=new BufferedReader(new java.io.InputStreamReader(fis));
			
			File folder=new File("test4.2-results/"+endpointAbbrev);
			
			if (!folder.exists()) folder.mkdir();
			
			if (!endpoint.equals(TESTConstants.ChoiceMutagenicity) && !endpoint.equals(TESTConstants.ChoiceReproTox)) br.readLine();//discard note line about units
			String header=br.readLine();
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "ID","|");
			int colExp=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Exp","|");
			
			int colHC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceHierarchicalMethod,"|");
			int colSM=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceSingleModelMethod,"|");
			int colGC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceGroupContributionMethod,"|");
			int colFDA=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceFDAMethod,"|");
			int colNN=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceNearestNeighborMethod,"|");
			int colC=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Pred_"+TESTConstants.ChoiceConsensus,"|");
		
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "|");
				
				String CAS=list.get(colCAS);
				
				if (instances.instance(CAS)==null) {//we have a CAS not present in our file so return nothing
//					return null;
					continue;
				}

				String exp=list.get(colExp);
				String HC=list.get(colHC);
				
				
				String SM="-9999";
				if (colSM!=-1) {
					SM=list.get(colSM);	
				}
				
				String GC="-9999";
				if (colGC!=-1) {
					GC=list.get(colGC);	
				}
				
				String FDA=list.get(colFDA);
				String NN=list.get(colNN);
				String C=list.get(colC);

				while (CAS.length()<13) {//for sorting purposes
					CAS="0"+CAS;
				}
				
				String newLine=CAS+"\t"+exp+"\t"+HC+"\t"+SM+"\t"+FDA+"\t"+GC+"\t"+NN+"\t"+C;
				newLine=newLine.replace("N/A", "-9999");
				
				vec.add(newLine);
				
			}
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return vec;
	}
	
	

	/**
	 * Go through descriptor discrepancy file and store a vector of CAS numbers
	 * @param filepath
	 * @return
	 */
	Vector<String>getDescriptorDiscrepancyCAS(String filepath) {
		Vector<String>vec=new Vector<String>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				String CAS=Line.substring(0, Line.indexOf("\t"));
				vec.add(CAS);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return vec;
		
		
	}
	
	
	
	/**
	 * Loop through endpoint and methods to compare all results
	 */
	void compareAllResults() {
		
		
		String descriptorerrorfile="test-results/descriptors/descriptor discrepancies.txt";
		Vector<String>vecCAS=this.getDescriptorDiscrepancyCAS(descriptorerrorfile);
		
		
		Vector <String> methods=new Vector<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);
		methods.add(TESTConstants.ChoiceSingleModelMethod);
		methods.add(TESTConstants.ChoiceGroupContributionMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);
//		methods.add(TESTConstants.ChoiceConsensus);//Consensus wont match since we aren't including FDA now!
		
		
		Vector <String> endpoints=new Vector<String>();
		endpoints.add(TESTConstants.ChoiceFHM_LC50);//All OK!
		endpoints.add(TESTConstants.ChoiceBCF);//All OK!
		endpoints.add(TESTConstants.ChoiceTP_IGC50);//All ok
		endpoints.add(TESTConstants.ChoiceDM_LC50);//All OK!
		endpoints.add(TESTConstants.ChoiceReproTox);//all ok!
		endpoints.add(TESTConstants.ChoiceMutagenicity);//All OK!
		endpoints.add(TESTConstants.ChoiceRat_LD50);//all ok!

		endpoints.add(TESTConstants.ChoiceViscosity);//All OK!
		endpoints.add(TESTConstants.ChoiceThermalConductivity);//All OK!
		endpoints.add(TESTConstants.ChoiceSurfaceTension);//All OK!
		endpoints.add(TESTConstants.ChoiceWaterSolubility);//All OK!		
		endpoints.add(TESTConstants.ChoiceVaporPressure);//All OK!
		endpoints.add(TESTConstants.ChoiceBoilingPoint);//All OK
		endpoints.add(TESTConstants.ChoiceMeltingPoint);//all ok
		
		endpoints.add(TESTConstants.ChoiceDensity);//all ok
		endpoints.add(TESTConstants.ChoiceFlashPoint);//new results missing!
//		
		
		for (int j=0;j<endpoints.size();j++) {
			String endpoint=endpoints.get(j);
			String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			
			System.out.println("\r\n***********************************************************");
			
			for (int i=0;i<methods.size();i++) {
				
				String set="training";
				String method=methods.get(i);

				if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
					if (!TESTConstants.haveSingleModelMethod(endpoint)) continue;					
				}
				
				if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
					if (!TESTConstants.haveGroupContributionMethod(endpoint)) {
						continue;
					}
				}

				Instances instancesOld=loadOldResults(endpointAbbrev,set);
				Instances instancesNew=loadNewResults(endpoint,endpointAbbrev,method,set);
				compare(instancesOld, instancesNew,endpoint,endpointAbbrev,method,set,vecCAS);
		
				set="prediction";
				instancesOld=loadOldResults(endpointAbbrev,"test");
				instancesNew=loadNewResults(endpoint,endpointAbbrev,method,set);
				
				compare(instancesOld, instancesNew,endpoint,endpointAbbrev,method,set,vecCAS);
		
			}
		}
	}
	
	
	/**
	 * Loop through endpoint and methods to compare all results
	 * 
	 * In this method predictions from WebTEST are stored in a database
	 * 
	 */
	void compareAllResultsWithPredDB(String dbFilePath,String descriptorDiscrepencyFilePath,String searchKey) {


		
		Vector<String>vecCAS=this.getDescriptorDiscrepancyCAS(descriptorDiscrepencyFilePath);		
//		Vector<String>vecCAS=new Vector<>();
		
		String outputFilePath="data/0 compare TEST set predictions/compare WebTEST to TEST4.2 searchKey="+searchKey+".txt";

		
		Vector<String>notFoundCAS=new Vector<>();

		Vector <String> methods=new Vector<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);
		methods.add(TESTConstants.ChoiceSingleModelMethod);
		methods.add(TESTConstants.ChoiceGroupContributionMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);
		//		methods.add(TESTConstants.ChoiceConsensus);//Consensus wont match since we aren't including FDA now!

		Vector <String> endpoints=new Vector<String>();
		endpoints.add(TESTConstants.ChoiceFHM_LC50);//All OK!
		endpoints.add(TESTConstants.ChoiceDM_LC50);//All OK!
		endpoints.add(TESTConstants.ChoiceTP_IGC50);//All ok
		endpoints.add(TESTConstants.ChoiceRat_LD50);//all ok!

		endpoints.add(TESTConstants.ChoiceBCF);//All OK!
		endpoints.add(TESTConstants.ChoiceReproTox);//all ok!
		endpoints.add(TESTConstants.ChoiceMutagenicity);//All OK!

////		TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
////		TESTConstants.ChoiceEstrogenReceptor,
//
		endpoints.add(TESTConstants.ChoiceBoilingPoint);//All OK
		endpoints.add(TESTConstants.ChoiceVaporPressure);//All OK!
		endpoints.add(TESTConstants.ChoiceMeltingPoint);//all ok
		endpoints.add(TESTConstants.ChoiceFlashPoint);//All OK
		endpoints.add(TESTConstants.ChoiceDensity);//all ok
		endpoints.add(TESTConstants.ChoiceSurfaceTension);//All OK!
		endpoints.add(TESTConstants.ChoiceThermalConductivity);//All OK!
		endpoints.add(TESTConstants.ChoiceViscosity);//All OK!
		endpoints.add(TESTConstants.ChoiceWaterSolubility);//All OK!		

		
		Connection conn=null;
		Statement stat=null;

		
		try {
			conn=MySQL_DB.getConnection(dbFilePath);
			stat=conn.createStatement();

			FileWriter fw=new FileWriter (outputFilePath);

			for (int j=0;j<endpoints.size();j++) {
				String endpoint=endpoints.get(j);
				String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);

				for (int i=0;i<methods.size();i++) {

					String method=methods.get(i);

					if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
						if (!TESTConstants.haveSingleModelMethod(endpoint)) continue;					
					}

					if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
						if (!TESTConstants.haveGroupContributionMethod(endpoint)) {
							continue;
						}
					}

					//				String set="training";
					//				Instances instancesOld=loadOldResults(endpointAbbrev,set);
					//				Instances instancesNew=loadNewResults(endpoint,endpointAbbrev,method,set);
					//				compare(instancesOld, instancesNew,endpoint,endpointAbbrev,method,set,vecCAS);

					
					if (searchKey.equals(WebTEST2.strCAS)) {
						String set="prediction";
						Instances instancesOld=loadOldResults(endpointAbbrev,"test");
						compareToDB(fw,instancesOld, stat, endpoint, endpointAbbrev, method, set, vecCAS,notFoundCAS);
						
						set="training";
						instancesOld=loadOldResults(endpointAbbrev,"training");
						compareToDB(fw,instancesOld, stat, endpoint, endpointAbbrev, method, set, vecCAS,notFoundCAS);

					} else if (searchKey.equals(WebTEST2.strInChiKey)) {
						String set="prediction";
						Instances instancesOld=loadOldResults(endpointAbbrev,"test");
						Hashtable<String,String>htInChiKey=this.getInChiKeyMap(endpointAbbrev, set);
						compareToDB_useInChiKey(fw,instancesOld, stat, endpoint, endpointAbbrev, method, set, vecCAS,notFoundCAS,htInChiKey);
						
						set="training";
						instancesOld=loadOldResults(endpointAbbrev,"training");
						htInChiKey=this.getInChiKeyMap(endpointAbbrev, set);
						compareToDB_useInChiKey(fw,instancesOld, stat, endpoint, endpointAbbrev, method, set, vecCAS,notFoundCAS,htInChiKey);
						
					}
					
					
					

				}


			}
			
			fw.close();
			
			//TODO- there are some that arent found because we stored by inchikey and we didnt allow duplicates (primary key)
			System.out.println("\nNot found cas:");
			for(String CAS:notFoundCAS) {
				System.out.println(CAS);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	
	
	void checkDateOnResultsFiles() {
		
		File folder=new File("test-results");
		
		File [] folders=folder.listFiles();
		
		for ( int i=0;i<folders.length;i++) {
			
			File folderi=folders[i];//endpoint name
			
			if (!folderi.isDirectory()) continue;
			
			if (folderi.getName().equals("bad chemicals")) continue;
			if (folderi.getName().equals("descriptors")) continue;
			
			File []folders2=folderi.listFiles();
			
			
			System.out.println(folderi.getName());
			
			for (int j=0;j<folders2.length;j++) {
				
				File folderj=folders2[j];
				
				if (folderj.getName().equals("Single model")) {
					
					if (!TESTConstants.haveSingleModelMethod(folderi.getName())) {
						continue;
					}
				}
				System.out.println(folderj.getName());
				
				if (!folderj.isDirectory()) continue;
				
				
				File [] files2=folderj.listFiles();
				
				for (int k=0;k<files2.length;k++) {
					
					File filek=files2[k];
					
					if (filek.getName().indexOf("compare")>-1) continue;
					

					Date lastModified = new Date(filek.lastModified()); 
					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
					String formattedDateString = formatter.format(lastModified); 					
					System.out.println(filek.getName()+"\t"+formattedDateString);
					
					
				}
				
				System.out.println("");
				
				
			}
			
			System.out.println("");	
			
			
		}
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		// TODO add loops for all methods and all endpoints
		CompareQSARModelResults c=new CompareQSARModelResults();
//		c.compileTEST4_2_results();
//		c.getTEST4results();
//		c.compareAllResults();
		
		
		String searchKey=WebTEST2.strCAS;
//		String searchKey=WebTEST2.strInChiKey;//get lots of discrepancies due to structure inconsistencies
		String dbFilePath="databases/TEST_sets_2018_10_19.db";
		String descriptorDiscrepancyFile="data/descriptors/descriptor discrepancies.txt";		
		c.compareAllResultsWithPredDB(dbFilePath,descriptorDiscrepancyFile,searchKey);
		
		
//		c.compareInChiKey("108-80-5", TESTConstants.ChoiceBCF);
//		c.checkDateOnResultsFiles();
	}

}
