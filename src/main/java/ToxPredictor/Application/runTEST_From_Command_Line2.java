package ToxPredictor.Application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesParser;

import wekalite.Instance;
import wekalite.Instances;
import wekalite.CSVLoader;
import QSAR.validation2.AllResultsXMLReader;
import QSAR.validation2.Validation2;
import QSAR.validation2.InstanceUtilities;
import QSAR.qsarOptimal.AllResults;
import ToxPredictor.Application.Calculations.PredictToxicityHierarchical;
import ToxPredictor.Application.Calculations.PredictToxicityLDA;
import ToxPredictor.Application.Calculations.PredictToxicityNearestNeighbor;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreator;
import ToxPredictor.Application.Calculations.TaskCalculations;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.MolFileUtilities;

public class runTEST_From_Command_Line2 {
	
	ChemicalFinder cf=new ChemicalFinder();
	
	Hashtable<String,Instances> ht_ccTraining= new Hashtable<String,Instances>();
	Hashtable<String,Instances> ht_ccTrainingFrag= new Hashtable<String,Instances>();
	Hashtable<String,Instances> ht_ccPrediction= new Hashtable<String,Instances>();
	Hashtable<String,AllResults> ht_allResults= new Hashtable<String,AllResults>();
	Hashtable<String,AllResults> ht_allResultsFrag= new Hashtable<String,AllResults>();
	
	///////////////////////////////////////////////////////
	java.util.Vector<String> vecMOA=new Vector<String>();
	Hashtable<String,AllResults>htAllResultsMOA=new Hashtable<String,AllResults>();
	Hashtable<String,Instances>htTrainingSetsMOA=new Hashtable<String,Instances>();
	
	Hashtable<String,AllResults>htAllResultsLC50=new Hashtable<String,AllResults>();
	Hashtable<String,Instances>htTrainingSetsLC50=new Hashtable<String,Instances>();
	///////////////////////////////////////////////////////

    int classIndex=1;
    int chemicalNameIndex=0;
    String DescriptorSet="2d";
	public static int minPredCount=2;//minimum number of predictions needed for consensus pred
	String del="\t";
	boolean useFragmentsConstraint=true;
	long minFileAgeHours=1000000000;

	///////////////////////////////////////////////////////

	Validation2 validation=new Validation2();
	DescriptorFactory df = new DescriptorFactory(false);
	
//	PredictToxicityFDA ptFDA = new PredictToxicityFDA();
	PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
	PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
//	PredictToxicityRandomForrestCaesar ptRFC= new PredictToxicityRandomForrestCaesar();	
	PredictToxicityLDA ptLDA= new PredictToxicityLDA();
	
	InstanceUtilities iu = new InstanceUtilities();
	MolFileUtilities mfu=new MolFileUtilities();

	
	
	private boolean isBinary(String endpoint) {
		//*add endpoint* 06
		if (endpoint.equals(TESTConstants.ChoiceReproTox) || endpoint.equals(TESTConstants.ChoiceMutagenicity) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
			return true;
		} else {
			return false;
		}
	}
	
	String getAbbrev(String endpoint) {
		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50)) return TESTConstants.abbrevChoiceFHM_LC50;
		if (endpoint.equals(TESTConstants.ChoiceDM_LC50)) return TESTConstants.abbrevChoiceDM_LC50;
		if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) return TESTConstants.abbrevChoiceTP_IGC50;
		if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) return TESTConstants.abbrevChoiceRat_LD50;
		if (endpoint.equals(TESTConstants.ChoiceBCF)) return TESTConstants.abbrevChoiceBCF;
		if (endpoint.equals(TESTConstants.ChoiceReproTox)) return TESTConstants.abbrevChoiceReproTox;
		if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) return TESTConstants.abbrevChoiceMutagenicity;
		if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) return TESTConstants.abbrevChoiceER_Binary;
		if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) return TESTConstants.abbrevChoiceER_LogRBA;
		if (endpoint.equals(TESTConstants.ChoiceBoilingPoint)) return TESTConstants.abbrevChoiceBoilingPoint;
		if (endpoint.equals(TESTConstants.ChoiceVaporPressure)) return TESTConstants.abbrevChoiceVaporPressure;
		if (endpoint.equals(TESTConstants.ChoiceMeltingPoint)) return TESTConstants.abbrevChoiceMeltingPoint;
		if (endpoint.equals(TESTConstants.ChoiceFlashPoint)) return TESTConstants.abbrevChoiceFlashPoint;
		if (endpoint.equals(TESTConstants.ChoiceDensity)) return TESTConstants.abbrevChoiceDensity;
		if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) return TESTConstants.abbrevChoiceSurfaceTension;
		if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) return TESTConstants.abbrevChoiceThermalConductivity;
		if (endpoint.equals(TESTConstants.ChoiceViscosity)) return TESTConstants.abbrevChoiceViscosity;
		if (endpoint.equals(TESTConstants.ChoiceWaterSolubility)) return TESTConstants.abbrevChoiceWaterSolubility;
		
		return "?";
		
	}
	private int GetSmilesCount(String filepath) {
		int count=0;
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				Line=Line.trim();
				if (Line.length()>0) count++;
			}
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	public AtomContainerSet LoadFromSmiles(String filepath) {
		
		AtomContainerSet atomContainerSet=new AtomContainerSet();
		String delimiter="";

		
		SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
		int count=this.GetSmilesCount(filepath);
    	try {
    		
    		BufferedReader br=new BufferedReader(new FileReader(filepath));
    		int counter=0;
    		while (true) {


				String Line = br.readLine();
				counter++;

				if (Line == null)
					break;
				if (Line.trim().equals(""))
					break;

				// if (delimiter.equals("")) {
				if (Line.indexOf("\t") > -1)
					delimiter = "\t";
				else if (Line.indexOf(",") > -1)
					delimiter = ",";
				else if (Line.indexOf(" ") > -1)
					delimiter = " ";
				else {
					//we will look up CAS later or assign one based on 
					//the current time and the row number
				}

				String ID="";
				String Smiles="";

				boolean haveID=false;
				
				if (!delimiter.equals("")) {
					haveID=true;
					ArrayList l = (ArrayList) ToxPredictor.Utilities.Utilities
					.Parse2(Line, delimiter);
					
					if (l.size()>=2) {
						Smiles = (String) l.get(0);
						ID = (String) l.get(1);
					} else {
						return null;
					}
					
				} else {					
					Smiles=Line;
//					ID="ID"+counter+"_"+System.currentTimeMillis();
//					ID="ID"+counter;
					       //1286482360066
//					long bob=1286482360000L;
//					long time=System.currentTimeMillis()-bob;
					long time=System.currentTimeMillis();
					ID="C"+counter+"_"+time; //generates unique ID so that output files can be stored in unique folders (i.e. dont get overwritten each time a new smiles file is ran)
//					System.out.println(ID);
//					ID="Unknown";
					
				}

				
//				if (l.size() != 2)
//					return null;

				// if (ID.equals("50925-42-3")) continue;

				// replace any characters that could mess up creation of file
				// paths later:
				ID = ID.replace("\"", "_");
				ID = ID.replace("/", "_");
				ID = ID.replace(":", "_");
				ID = ID.replace("*", "_");
				ID = ID.replace("?", "_");
				ID = ID.replace("<", "_");
				ID = ID.replace(">", "_");
				ID = ID.replace("|", "_");

				String message = "Loading " + ID + ", " + counter + " of "
						+ count;

				if (ID.indexOf("C")>-1) {
					message="Loading "+counter+" of "+ count;
				}
				
				// System.out.println(message);
//				setMessage(message);

				AtomContainer m = null;
				try {


					m = (AtomContainer) sp.parseSmiles(Smiles);
					m.setProperty("Error", "");
					
					if (mfu.HaveBadElement(m)) {
						m.setProperty("Error",
						"Molecule contains unsupported element");
					} else if (m.getAtomCount() == 1) {
						m.setProperty("Error", "Only one nonhydrogen atom");
					} else if (m.getAtomCount() == 0) {
						m.setProperty("Error", "Number of atoms equals zero");
					} else if (!mfu.HaveCarbon(m)) {
						m.setProperty("Error", "Molecule does not contain carbon");
					}

					if (Smiles.indexOf(".")>-1) {
						m.setProperty("Error", "Molecules can only contain one fragment");
					}
					
					

				} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
					//    				
					m = new AtomContainer();
					System.out.println("Error: " + e.getMessage() + ", "
							+ Smiles);
//					m.setProperty("Error", "Error: " + e.getMessage()
//							+ ", " + Smiles);
					
					m.setProperty("Error", e.getMessage()
							+ ", SMILES=" + Smiles);

				}
				
				
				m.setProperty("CAS", ID);
				atomContainerSet.addAtomContainer(m);

			}
    		
    		br.close();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
		return atomContainerSet;
	}

	
	public static AtomContainerSet readFromSDF(String SDFFilePath) {
		try {
			
			AtomContainerSet atomContainerSet=new AtomContainerSet();
			
			 IteratingSDFReader reader = new IteratingSDFReader(
			   new FileInputStream(SDFFilePath), DefaultChemObjectBuilder.getInstance());
			 while (reader.hasNext()) {
			   IAtomContainer molecule = (IAtomContainer)reader.next();
			   atomContainerSet.addAtomContainer(molecule);
			 }
			
			return atomContainerSet;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public AtomContainerSet LoadFromSDF(String filepath) {
    	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();

		AtomContainerSet moleculeSetFromFile=readFromSDF(filepath);
		

		//create new molecule set since cant replace molecules in the vector (i.e. there's no setMolecule method)
		AtomContainerSet moleculeSetExport=new AtomContainerSet();
		
		try {

			for (int i = 0; i < moleculeSetFromFile.getAtomContainerCount(); i++) {
				AtomContainer m = (AtomContainer) moleculeSetFromFile.getAtomContainer(i);
				String CASfield = MolFileUtilities.getCASField(m);

				String CAS = null;

				if (!CASfield.equals("")) {
					CAS = (String) m.getProperty(CASfield);
					if (CAS != null) {
						CAS = CAS.trim();
						CAS = CAS.replace("/", "_");
						m.setProperty("CAS", CAS);
					}
				}

				if (CAS == null) {
					String nameField = MolFileUtilities.getNameField(m);
					String name = null;

					if (!nameField.equals("")) {
						name = (String) m.getProperty(nameField);
					}

					if (name == null) {
						// m.setProperty("Error",
						// "<html>CAS and Name fields are both empty</html>");
						// m.setProperty("CAS", "Unknown");

						long time = System.currentTimeMillis();
						m.setProperty("CAS", "C" + i + "_" + time);// generates
						// unique ID 

					} else {
						m.setProperty("CAS", name);
					}
				}
				// System.out.println(CAS+"\t"+m.getProperty("NAME")+"\t"+m.getProperty("aritmentic mean"));

				String message = "Loading " + m.getProperty("CAS") + ", "
				+ i + " of " +  moleculeSetFromFile.getAtomContainerCount();
				// System.out.println(message);
//				setMessage(message);

				String error = (String) m.getProperty("Error");

				// System.out.println(m.getProperty("CAS")+"\t"+error);
				// System.out.println(m.getProperty("CAS")+"\t"+error==null);

				if (error == null || error.equals("")) {

					m.setProperty("Error", "");

					if (mfu.HaveBadElement(m)) {
						m.setProperty("Error",
						"Molecule contains unsupported element");
					} else if (m.getAtomCount() == 1) {
						m.setProperty("Error", "Only one nonhydrogen atom");
					} else if (m.getAtomCount() == 0) {
						m.setProperty("Error", "Number of atoms equals zero");
					} else if (!mfu.HaveCarbon(m)) {
						m.setProperty("Error", "Molecule does not contain carbon");
					}

					AtomContainerSet moleculeSet2 = (AtomContainerSet) ConnectivityChecker
					.partitionIntoMolecules(m);

					if (moleculeSet2.getAtomContainerCount() > 1) {

						// m.setProperty("Error","Multiple molecules, largest fragment retained");
						m.setProperty("Error", "Multiple molecules");

					}
				} else {
					// otherwise preserve error stored in sdf file
				}

//				m = (Molecule)MolFileUtilities.CheckForAromaticBonds(m);
				
				moleculeSetExport.addAtomContainer(m);

				//
			}// end loop over molecules

		} catch (Exception e) {
			e.printStackTrace();

		}
		
		return moleculeSetExport;

	}
	
	private boolean isLogMolar(String endpoint) {
		//*add endpoint* 07
		if (endpoint.equals(TESTConstants.ChoiceBoilingPoint) || 
				endpoint.equals(TESTConstants.ChoiceDensity) ||
				endpoint.equals(TESTConstants.ChoiceFlashPoint) ||
				endpoint.equals(TESTConstants.ChoiceMeltingPoint) ||
				endpoint.equals(TESTConstants.ChoiceSurfaceTension) ||
				endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
			return false;
		} else {
			return true;
		}
	}
	
	void LoadTrainingDataSet(String endpoint,String abbrev) {
		String csvTraining_2d;
		String csvTrainingFrag;
		String csvPrediction_2d;

		String train2d = "_training_set-2d.csv";
		String trainfrag = "_training_set-frag.csv";
		String pred2d = "_prediction_set-2d.csv";

		csvTraining_2d = abbrev + "/" + abbrev + train2d;
		csvPrediction_2d = abbrev + "/" + abbrev + pred2d;
		csvTrainingFrag = abbrev + "/" + abbrev + trainfrag;

		try {
			CSVLoader atf = new CSVLoader();

			if (ht_ccTraining.get(endpoint) == null) {
//				System.out.println(csvTraining_2d);
				ht_ccTraining.put(endpoint, atf.getDataSetFromJarFile(csvTraining_2d));
				
			}

			if (ht_ccPrediction.get(endpoint) == null) {// always load so
														// that can
				ht_ccPrediction.put(endpoint, atf.getDataSetFromJarFile(csvPrediction_2d));

			}

			if (ht_ccTrainingFrag.get(endpoint) == null) {
				try {
					ht_ccTrainingFrag.put(endpoint, atf.getDataSetFromJarFile(csvTrainingFrag));
				} catch (Exception e) {
					// System.out.println(e);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void loadTrainingData(String endpoint,String method) {
		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			return;
		}
		
		String abbrev=this.getAbbrev(endpoint);
		
		System.out.print("Loading training set data...");
		this.LoadTrainingDataSet(endpoint,abbrev);
		System.out.print("done\n");
		
//		if (method.equals(TESTConstants.ChoiceLDA)) {
//			LoadLDAFiles();
//		}

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod)
				|| method.equals(TESTConstants.ChoiceSingleModelMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
			System.out.print("Loading cluster data file...");
			this.LoadHierarchicalXMLFile(endpoint,abbrev);
			System.out.print("done\n");
		}

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
//			System.out.println("Loading frag model...");
			this.LoadFragmentXMLFile(endpoint,abbrev);
//			System.out.print("done\n");
			// }
		}

		Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
		Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);

//		trainingDataSet2d.setClassIndex(classIndex);
//		trainingDataSetFrag.setClassIndex(classIndex);

	}
	
	
	void LoadFragmentXMLFile(String endpoint,String abbrev) {
		
		String xmlFileName = "";
		
		xmlFileName=abbrev+"/"+abbrev+"_training_set-frag.xml";
		boolean HaveFile=this.HaveFileInJar(xmlFileName);			
		
		if (!HaveFile) {				
			return;
		}

		try {
	
			if (ht_allResultsFrag.get(endpoint)==null) {
				ht_allResultsFrag.put(endpoint,readAllResultsFormat2_2(xmlFileName, ht_ccTrainingFrag.get(endpoint),true));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	boolean HaveFileInJar(String filepath) {
		try {
			java.io.InputStream is=this.getClass().getClassLoader().getResourceAsStream(filepath);
			is.read();
			return true;
		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("here");
			return false;
		}
		
		
	}
	void LoadHierarchicalXMLFile(String endpoint,String abbrev) {
		
		String xmlFileName="";
	
		xmlFileName=abbrev+"/"+abbrev+"_training_set-2d.xml";
		boolean HaveFile=this.HaveFileInJar(xmlFileName);
		
		if (!HaveFile) {				
			return;
		}

		try {
		
			if (ht_allResults.get(endpoint)==null) {
				ht_allResults.put(endpoint,readAllResultsFormat2_2(xmlFileName, ht_ccTraining.get(endpoint),true));
			}
							
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AllResults readAllResultsFormat2_2(String resultsXML,Instances trainingDataset,boolean isXMLFileInsideJar) {
		
		try {
			
//			System.out.println("Loading cluster data file...0%");					
//			System.out.print("Loading cluster data file...%");
			AllResultsXMLReader arxr=new  AllResultsXMLReader();
			
			AllResults ar = arxr.readAllResults(resultsXML,trainingDataset,true);
			return ar;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	
	private void calculate(int molNum, int molCount,AtomContainer m, 
			FileWriter fw,String endpoint,String method,int index) {

		
		//TODO: move following to variables that are passed to calculate method
		boolean isBinaryEndpoint=this.isBinary(endpoint);
		boolean isLogMolarEndpoint=this.isLogMolar(endpoint);
		String abbrev=this.getAbbrev(endpoint);
		
		DecimalFormat d3=new DecimalFormat("0.000");
		
		String strOutputFolder=null;//dont want to output webpages

		DescriptorData dd = new DescriptorData();
		dd.ID = (String) m.getProperty("CAS");
		String CAS=dd.ID;

//		System.out.println(CAS);
		
		Integer Index=(Integer) m.getProperty("Index");
		//		statMessage += "Molecule #" + Index;
//		System.out.println("Molecule ID = " + dd.CAS+" ("+(molNum+1)+" of " +molCount+")");

		boolean Use3D=false;
		df.Calculate3DDescriptors = Use3D;
		dd.ThreeD = Use3D;

		// calculate 2D and non quantum 3D descriptors:

		int descresult = df.CalculateDescriptors(m, dd, false,false,
				true, "");

		//		df.WriteOut2DDescriptors(m, dd, strOutputFolder4,del);

		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			if (descresult>-1) {
				df.WriteCSVLine(fw, dd, del);	
			} else {
				try {
				fw.write(dd.ID+del+"error\r\n");
				} catch (Exception ex)  {
					ex.printStackTrace();
				}
			}
			
		}


		if (descresult == -1) {
			// done=true; // commented out: dont kill rest of run for batch
			System.out.println("Error calculating descriptors for "+dd.ID);
			return;
		}

		//		ToxPredictor.Utilities.SaveStructureToFile.CreateScaledImageFile(m, "testchemical",strOutputFolder4, false, false, true,30,200); 

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			//array to store predictions for all methods for consensus method:
			ArrayList predictedToxicities=new ArrayList();
			ArrayList predictedUncertainties=new ArrayList();


			AllResults allResults=ht_allResults.get(endpoint);//shortcut to results object				
			AllResults allResultsFrag=ht_allResultsFrag.get(endpoint); //shortcut to results object

			Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
			Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);
			Instances testDataSet2d = ht_ccPrediction.get(endpoint);

//			String ToxFieldName=trainingDataSet2d.attribute(classIndex).name();
			
			String ToxFieldName="Tox";

			java.util.Hashtable ht=dd.CreateDataHashtable(ToxFieldName,true,true,false,false,false);

			
			
			//Create instances for test chemical (for GCM it will only contain fragment descriptors): 
			String[] varArrayFrag = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSetFrag);
			Instances evalInstancesFrag = iu.createInstances(ht, varArrayFrag);
			Instance evalInstanceFrag = evalInstancesFrag.firstInstance();

			String[] varArray2d = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSet2d);
			Instances evalInstances2d = iu.createInstances(ht,varArray2d);
			Instance evalInstance2d = evalInstances2d.firstInstance();

			Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.ID, evalInstance2d,
					trainingDataSet2d, testDataSet2d);


			// **************************************************

			int result = 0;

			String statMessage = "Calculating "+endpoint+"...";
			//			statMessage += "Molecule ID = " + dd.CAS;

			//			statMessage += "Molecule ID = " + dd.CAS+" (#"+(molNum+1)+")";
			statMessage += "Molecule ID = " + dd.ID+" ("+(molNum+1)+" of " +molCount+")";
			//			statMessage += "Molecule #" + Index;
			
//			System.out.println(statMessage);

			// ******************************************************

			double predToxVal = -9999;
			double predToxUnc=1;//TODO: add code to calculate this

			ReportOptions options = new ReportOptions();
			options.reportBase = strOutputFolder;
			options.embedImages = true;
			
//			if (method.equals(TESTConstants.ChoiceFDAMethod)) {
//				result = ptFDA.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
//						strOutputFolder, DescriptorSet, evalInstances2d,
//						trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
//						useFragmentsConstraint, this, er);
//				predToxVal = ptFDA.predToxVal;
//			} else if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
			
			if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, allResults,
						evalInstances2d, evalInstance2d, trainingDataSet2d,trainingDataSet2d,
						testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
				result = ptNN.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, evalInstances2d,
						trainingDataSet2d, testDataSet2d, dd.MW, er, options);
				predToxVal = ptNN.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, allResults,
						evalInstances2d, evalInstance2d, trainingDataSet2d,trainingDataSet2d,
						testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, allResultsFrag,
						evalInstancesFrag, evalInstance2d, trainingDataSetFrag,trainingDataSet2d,
						testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
//			} else if (method.equals(TESTConstants.ChoiceRandomForrestCaesar)) { 
//				result=ptRFC.CalculateToxicity(CAS, method, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
//						strOutputFolder, DescriptorSet, evalInstances2d,
//						trainingDataSet2d, testDataSet2d, dd.MW,
//						this, er,ht);
//				predToxVal = ptRFC.predToxVal;

			} else if (method.equals(TESTConstants.ChoiceLDA)) { 
				result = ptLDA.CalculateToxicity(CAS, method, endpoint, 
						isBinaryEndpoint, isLogMolarEndpoint, abbrev, 
						strOutputFolder, DescriptorSet, htAllResultsMOA, 
						htTrainingSetsMOA, htAllResultsLC50, htTrainingSetsLC50, 
						evalInstances2d, trainingDataSet2d, testDataSet2d, 
						dd.MW, dd.MW_Frag, useFragmentsConstraint, er, vecMOA, options);
				predToxVal = ptLDA.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceConsensus)) {
				//TODO move following into a subroutine:
				//Hierarchical:				
				result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceHierarchicalMethod, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, allResults,
						evalInstances2d, evalInstance2d, trainingDataSet2d,trainingDataSet2d,
						testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);
				predictedToxicities.add(ptH.predToxVal);
				predictedUncertainties.add(ptH.predToxUnc);

				//Single Model
				if (TESTConstants.haveSingleModelMethod(endpoint)) {
					//					System.out.println("SM r2="+orSM.getR2());
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceSingleModelMethod, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
							strOutputFolder, DescriptorSet, allResults,
							evalInstances2d, evalInstance2d, trainingDataSet2d,trainingDataSet2d,
							testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);
					predictedToxicities.add(ptH.predToxVal);
					predictedUncertainties.add(ptH.predToxUnc);
				}

				//Group contribution:
				if (TESTConstants.haveGroupContributionMethod(endpoint)) {
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceGroupContributionMethod, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
							strOutputFolder, DescriptorSet, allResultsFrag,
							evalInstancesFrag, evalInstance2d, trainingDataSetFrag,trainingDataSet2d,
							testDataSet2d, dd.MW,dd.MW_Frag, useFragmentsConstraint, er,minFileAgeHours, options);

					predictedToxicities.add(ptH.predToxVal);
					predictedUncertainties.add(ptH.predToxUnc);
				}

//				//FDA:					
//				result = ptFDA.CalculateToxicity(CAS, TESTConstants.ChoiceFDAMethod, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
//						strOutputFolder, DescriptorSet, evalInstances2d,
//						trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
//						useFragmentsConstraint, this, er);	
//				predictedToxicities.add(ptFDA.predToxVal);
//				predictedUncertainties.add(ptFDA.predToxUnc);

				//Nearest neighbor:					
				result = ptNN.CalculateToxicity(CAS, TESTConstants.ChoiceNearestNeighborMethod, endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
						strOutputFolder, DescriptorSet, evalInstances2d,
						trainingDataSet2d, testDataSet2d, dd.MW, er, options);
				predictedToxicities.add(ptNN.predToxVal);
				predictedUncertainties.add(ptNN.predToxUnc);

				//				for (int i=0;i<methods.size();i++) {
				//					System.out.println(methods.get(i)+"\t"+predictedToxicities.get(i));
				//				}

				predToxVal=calculateConsensusToxicity(predictedToxicities);
				predToxUnc=1;//TODO: add code to calculate this

				predictedToxicities.add(predToxVal);

//				ChemicalCluster ccTraining = new ChemicalCluster(trainingDataSet2d);
//				double[] Mean = ccTraining.CalculateMeans();
//				double[] StdDev = ccTraining.CalculateStdDevs();
//
//				Hashtable<Double, Instance> htTestMatch = DescriptorCalculationTask7
//				.FindClosestChemicals(evalInstance2d,
//						testDataSet2d, true, false, true,chemicalNameIndex,Mean,StdDev);
//
//				Hashtable<Double, Instance> htTrainMatch = DescriptorCalculationTask7
//				.FindClosestChemicals(evalInstance2d, trainingDataSet2d,
//						true, false, true, chemicalNameIndex,Mean,StdDev);
//
//				
//				PredictToxicityWebPageCreator ptwc=new PredictToxicityWebPageCreator();
//
//				ptwc.WriteConsensusResultsWebPages(predToxVal, predToxUnc,
//						method, strOutputFolder, CAS, endpoint,abbrev,
//						isBinaryEndpoint, isLogMolarEndpoint, er, dd.MW, "OK",
//						htTestMatch,htTrainMatch, methods, predictedToxicities,
//						predictedUncertainties,chemicalNameIndex);

			}//end choice consensus

			
			
			if (result == -1) {
				return;
			}

			System.out.println(index+"\t"+dd.ID+"\t"+d3.format(er.expToxValue)+"\t"+d3.format(predToxVal)+"\t");//add trailing tab for blank error field

			if (!isBinaryEndpoint) {
				this.WriteToxicityResultsForChemical(fw,endpoint,method,index,dd.ID,er.expToxValue,predToxVal,dd.MW,del,"");
			} else {
				//TODO
				this.WriteBinaryToxResultsForChemical(fw,endpoint,method,index,dd.ID,er.expToxValue,predToxVal,dd.MW,del,"");
			}

//			if (method.equals(TESTConstants.ChoiceConsensus)) {
//				//TODO
//				this.WriteToxicityResultsForChemicalAllMethods(fw3,index,dd.CAS,er.expToxValue,methods,predictedToxicities,dd.MW,del,"");
//			}
			
		} else {// Descriptors
			System.out.println(dd.ID+"\t");
			//TODO
//			WriteDescriptorResultsForChemicalToWebPage(fw,index,CAS,"");
		}

	}
	
private double calculateConsensusToxicity(ArrayList <Double>preds) {
		
		double pred=0;
		
		int predcount=0;
		
		for (int i=0;i<preds.size();i++) {
			if (preds.get(i)>-9999) {
				predcount++;
				pred+=preds.get(i);
			}
		}
		
		if (predcount<minPredCount) return -9999;
		
		pred/=(double)predcount;
//		System.out.println(pred);
		return pred;
	}
	
	public Lookup.ExpRecord LookupExpVal(String CAS, Instances trainingDataSet2d, Instances testDataSet2d) {
		Lookup lookup = new Lookup();
//		trainingDataSet2d.setClassIndex(classIndex);
//		testDataSet2d.setClassIndex(classIndex);

		Lookup.ExpRecord er;

		// lookup first in training set based on CAS
		er = lookup.LookupExpRecordByCAS(CAS, trainingDataSet2d);
//		System.out.println(er.expToxValue);
		if (er.expToxValue != -9999) {			
			er.expSet = "Training";			
			return er;
		}

		// ******************************************************
		// next lookup in external test set based on CAS:
		er = lookup.LookupExpRecordByCAS(CAS, testDataSet2d);
//		System.out.println(er.expToxValue);
		if (er.expToxValue != -9999) {
			er.expSet = "Test";
			return er;
		}

		// ******************************************************
		er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";

		return er;

	}
	
	void WriteOverallTextHeader(FileWriter fw, String d,String endpoint) throws Exception {

		fw.write("#" + d + "ID" + d);
		System.out.print("#" + d + "ID" + d);
		
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		if (!isBinary(endpoint)) {
			if (isLogMolar(endpoint)) {
				fw.write("Exp_Value:"+molarlogunits+d);
				fw.write("Pred_Value:"+molarlogunits+d);
				System.out.print("Exp_Value:"+molarlogunits+d);
				System.out.print("Pred_Value:"+molarlogunits+d);
			}

			fw.write("Exp_Value:"+massunits+d);
			fw.write("Pred_Value:"+massunits+d+"error");
		} else {
			fw.write("Exp_Value"+d);
			fw.write("Pred_Value"+d);
			
			System.out.print("Exp_Value"+d);
			System.out.print("Pred_Value"+d);

			fw.write("Exp_Result"+d);
			fw.write("Pred_Result"+d+"error");
			
			
		}
		fw.write("\r\n");
		System.out.print("error\r\n");

	}
	
	void WriteToxicityResultsForChemical(FileWriter fw2,String endpoint,String method,int index,String CAS,double ExpToxVal,double PredToxVal,double MW,String d,String error) {
		try {
	
			java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00");
			java.text.DecimalFormat d2exp=new java.text.DecimalFormat("0.00E00");

			double ExpToxValMass=-9999;
			double PredToxValMass=-9999;

			if (isLogMolar(endpoint)) {
				if (PredToxVal!=-9999) {
					PredToxValMass=PredictToxicityWebPageCreator.getToxValMass(endpoint,PredToxVal,MW);
				}
			
				if (ExpToxVal!=-9999) {
					ExpToxValMass=PredictToxicityWebPageCreator.getToxValMass(endpoint,ExpToxVal,MW); 
				}
			} else {
				PredToxValMass=PredToxVal;
				ExpToxValMass=ExpToxVal;
			}
			

//			String f="ToxRuns/ToxRun_" + CAS;
//			String f2 =  f+ "/" + endpoint;
//			String f3=f+"/StructureData";
//
//			String filename = "PredictionResults";
//			filename += method.replaceAll(" ", "");
//			filename += ".html";
//

//			fw.write("<tr>\n");
//			fw.write("<td>"+index+"</td>\n");//#
			
//			if (error.equals("")) {
//				fw.write("<td><a href=\"" + f2 + "/" + filename + "\">"
//					+ CAS + "</a></td>\n");
//
//				fw.write("<td><a href=\""+f3+"/structure.png"+"\"><img src=\"" + f3+ "/"
//					+ "structure.png" + "\" height=200 border=0></a></td>\n");
//			} else {
//				fw.write("<td>"+CAS+"</td>\n");
//				fw.write("<td>Error: "+error+"</td>\n");
//			}

			fw2.write(index+d+CAS+d);
			
			// ********************************************************
			//Molar values

			if (isLogMolar(endpoint)) {
				if (ExpToxVal == -9999) {
//					fw.write("<td>N/A</td>\n");
					fw2.write("N/A" + d);
				} else {
//					fw.write("<td>" + d2.format(ExpToxVal) + "</td>\n");
					fw2.write(d2.format(ExpToxVal) + d);
				}
				if (PredToxVal == -9999) {
//					fw.write("<td>N/A</td>\n");
					fw2.write("N/A" + d);
				} else {
//					fw.write("<td>" + d2.format(PredToxVal) + "</td>\n");
					fw2.write(d2.format(PredToxVal) + d);
				}
			}
			
			// ********************************************************
			//Mass values
			if (ExpToxVal ==-9999) {
//				fw.write("<td align=\"center\">N/A</td>\n");//mass
				fw2.write("N/A"+d);
			} else {
				if (Math.abs(ExpToxValMass)<0.1) {
//					fw.write("<td align=\"center\">"+d2exp.format(ExpToxValMass)+"</td>\n"); //mass
					fw2.write(d2exp.format(ExpToxValMass)+d); //mass
				} else {
//					fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n"); //mass
					fw2.write(d2.format(ExpToxValMass)+d); //mass
				}
			}
			
			if (PredToxVal==-9999) {
//				fw.write("<td align=\"center\">N/A</td>\n"); //mass units
				fw2.write("N/A");
			} else { 
				if (Math.abs(PredToxValMass)<0.1) {
//					fw.write("<td align=\"center\">"+d2exp.format(PredToxValMass)+"</td>\n"); //mass
					fw2.write(d2exp.format(PredToxValMass)); //mass
				} else {
//					fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n"); //mass
					fw2.write(d2.format(PredToxValMass)); //mass
				}
			}
			
			//write error:
			fw2.write(d+error);
			
			if (!error.equals(""))
				System.out.println(index+d+CAS+d+"N/A"+d+"N/A"+d+error);
			
//			fw.write("</tr>\n");
			fw2.write("\r\n");
			
//			fw.flush();
			fw2.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPredictions(String endpoint,String method,AtomContainerSet atomContainerSet,String outputFilePath) {
		//TODO recode this so that all predictions are done simultaneously (speed up hierarchical by only looping through clusters once)
		
		try {

			FileWriter fw=new FileWriter(outputFilePath);
//			ArrayList methods=DescriptorCalculationTask7.getMethods(endpoint); //shortcut to results object

			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				df.WriteCSVHeader(fw, del);
			} else {
				this.WriteOverallTextHeader(fw,del,endpoint);
			}
			
			for (int i = 0; i < atomContainerSet.getAtomContainerCount(); i++) {
				AtomContainer m = (AtomContainer) atomContainerSet.getAtomContainer(i);
				int index = i+1;
				String error = (String) m.getProperty("Error");

				
				
				if (!error.equals("")) {//something wrong with chemical dont do calculations but write to file:

					String CAS = (String) m.getProperty("CAS");
					
					
					if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
						Instances trainingDataSet2d = ht_ccTraining
								.get(endpoint);
						Instances testDataSet2d = ht_ccPrediction
								.get(endpoint);

						Lookup.ExpRecord er = LookupExpVal(CAS,
								trainingDataSet2d, testDataSet2d);

						if (!isBinary(endpoint)) {
							this.WriteToxicityResultsForChemical(fw,endpoint,method,
									index, CAS, er.expToxValue, -9999,
									er.MW, del, error);
							
						}else {
							this.WriteBinaryToxResultsForChemical(fw, endpoint,method,
									index, CAS, er.expToxValue, -9999,
									er.MW, del, error);
						}
						
//						if (method.equals(TESTConstants.ChoiceConsensus)) {
//							this.WriteToxicityResultsForChemicalAllMethods(fw3,index,CAS,er.expToxValue,methods,null,er.MW,del,error);
//						}
						
					} else {														
//						this.WriteDescriptorResultsForChemicalToWebPage(fw, index,CAS,error);
						fw.write(CAS+del+"Error:"+error+"\r\n");
					}
				} else {
					
					String CAS = (String) m.getProperty("CAS");
					
//					if (!CAS.equals("7585-39-9")) {
//						continue;
//					}

					//Do calculations:
					this.calculate(i, atomContainerSet.getAtomContainerCount(), m,fw,endpoint,method,index);
				}
				
			} //end loop over molecules
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	void WriteBinaryToxResultsForChemical(FileWriter fw2,String endpoint,String method,int index,String CAS,double ExpToxVal,double PredToxVal,double MW,String d,String error) {
		
		try {
			
			java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00");


			String f="ToxRuns/ToxRun_" + CAS;
			String f2 =  f+ "/" + endpoint;
			String f3=f+"/StructureData";

			String filename = "PredictionResults";
			filename += method.replaceAll(" ", "");
			filename += ".html";


//			fw.write("<tr>\n");
//			fw.write("<td>"+index+"</td>\n");//#
//			
//			if (error.equals("")) {
//				fw.write("<td><a href=\"" + f2 + "/" + filename + "\">"
//					+ CAS + "</a></td>\n");
//
//				fw.write("<td><a href=\""+f3+"/structure.png"+"\"><img src=\"" + f3+ "/"
//					+ "structure.png" + "\" height=200 border=0></a></td>\n");
//			} else {
//				fw.write("<td>"+CAS+"</td>\n");
//				fw.write("<td>Error: "+error+"</td>\n");
//			}


			fw2.write(index+d+CAS+d);
			
			
			
			if (ExpToxVal == -9999) {
//				fw.write("<td>N/A</td>\n");
				fw2.write("N/A"+d);
			} else {
//				fw.write("<td>" + d2.format(ExpToxVal) + "</td>\n");
				fw2.write(d2.format(ExpToxVal)+d);
			}
			if (PredToxVal == -9999) {
//				fw.write("<td>N/A</td>\n");
				fw2.write("N/A"+d);
			} else {
//				fw.write("<td>" + d2.format(PredToxVal) + "</td>\n");
				fw2.write(d2.format(PredToxVal)+d);
			}
			
			
//			System.out.println(ExpToxVal+"\t"+PredToxVal);
			
			if (ExpToxVal ==-9999) {
//				fw.write("<td align=\"center\">N/A</td>\n");//mass
				fw2.write("N/A"+d);
			} else {
				if (ExpToxVal<0.5) {
					
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); //mass
						fw2.write("Developmental NON-toxicant"+d); //mass
					
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) { 
//						fw.write("<td align=\"center\">Mutagenicity Negative</td>\n"); //mass
						fw2.write("Mutagenicity Negative"+d); //mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); //mass
						fw2.write("Does NOT bind to estrogen receptor"+d); //mass
					}
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fw.write("<td align=\"center\">Developmental toxicant</td>\n"); //mass
						fw2.write("Developmental toxicant"+d); //mass
					
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) { 
//						fw.write("<td align=\"center\">Mutagenicity Positive</td>\n"); //mass
						fw2.write("Mutagenicity Positive"+d); //mass
						} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); //mass
						fw2.write("Binds to estrogen receptor"+d); //mass
					}

				}
			}
			
			if (PredToxVal==-9999) {
//				fw.write("<td align=\"center\">N/A</td>\n"); //mass units
				fw2.write("N/A");
			} else { 

				if (PredToxVal<0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); //mass
						fw2.write("Developmental NON-toxicant"); //mass
					
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) { 
//						fw.write("<td align=\"center\">Mutagenicity Negative</td>\n"); //mass
						fw2.write("Mutagenicity Negative"); //mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); //mass
						fw2.write("Does NOT bind to estrogen receptor"+d); //mass
					}
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fw.write("<td align=\"center\">Developmental toxicant</td>\n"); //mass
						fw2.write("Developmental toxicant"); //mass
					
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) { 
//						fw.write("<td align=\"center\">Mutagenicity Positive</td>\n"); //mass
						fw2.write("Mutagenicity Positive"); //mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); //mass
						fw2.write("Binds to estrogen receptor"+d); //mass
					}
				}
			}
			
			//write error:
			fw2.write(d+error);
			
			if (!error.equals(""))
				System.out.println(index+d+CAS+d+"N/A"+d+"N/A"+d+error);

			
//			fw.write("</tr>\n");
			fw2.write("\r\n");
			
//			fw.flush();
			fw2.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
