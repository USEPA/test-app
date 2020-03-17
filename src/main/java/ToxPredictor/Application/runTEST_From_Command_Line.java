package ToxPredictor.Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openscience.cdk.AtomContainerSet;

import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;


public class runTEST_From_Command_Line {
	
	public static String inputFileTypeSDF="SDF";
	public static String inputFileTypeSMILES="SMILES";
	
	//File types:
	public static int numFileTypeSDF=1;
	public static int numFileTypeSMILES=2;
	
//	Endpoints:
	public static int ChoiceFHM_LC50=1;
	public static int ChoiceDM_LC50=2;
	public static int ChoiceTP_IGC50=3;
	public static int ChoiceRat_LD50=4;
	public static int ChoiceBCF=5;
	public static int ChoiceReproTox=6;
	public static int ChoiceMutagenicity=7;
	public static int ChoiceBoilingPoint=20;
	public static int ChoiceVaporPressure=21;
	public static int ChoiceMeltingPoint=22;
	public static int ChoiceFlashPoint=23;
	public static int ChoiceDensity=24;
	public static int ChoiceSurfaceTension=25;
	public static int ChoiceThermalConductivity=26;
	public static int ChoiceViscosity=27;
	public static int ChoiceWaterSolubility=28;

	public static int ChoiceDescriptors=99;

	
	//QSAR methods
	public static int ChoiceHierarchicalMethod=1;
	public static int ChoiceFDAMethod=2;
	public static int ChoiceSingleModelMethod=3;//not applicable to all endpoints
	public static int ChoiceNearestNeighborMethod=4;
	public static int ChoiceGroupContributionMethod=5;//not applicable to all endpoints
	public static int ChoiceConsensus=10;

	
	runTEST_From_Command_Line2 r2=new runTEST_From_Command_Line2();
	
	//If run from command line, this class uses this method:
	public void go(String [] args) {
		String inputFilePath=args[0];
		int iFileType=Integer.parseInt(args[1]);
		String outputFilePath=args[2];
		int iEndpoint=Integer.parseInt(args[3]);
		int iMethod=Integer.parseInt(args[4]);

		String endpoint=this.getEndpoint(iEndpoint);
		String method=this.getMethod(iMethod);
		String inputFileType=this.getFileType(iFileType);
		
		if (endpoint.equals("?")) {
			System.out.println("invalid endpoint");
			return;
		}

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors) && method.equals("?")) {
			System.out.println("invalid method");
			return;
		}
		if (inputFileType.equals("?")) {
			System.out.println("invalid filetype");
			return;
		}
		
		go(inputFilePath, inputFileType,outputFilePath, endpoint, method);
	}
	
	private String getFileType(int i) {
		if (i==numFileTypeSDF) {
			return this.inputFileTypeSDF;
		} else if (i==numFileTypeSMILES) {
			return this.inputFileTypeSMILES;
		} else 
			return "?";
	}
	
	private String getEndpoint(int i) {
		if (i==ChoiceFHM_LC50) {
			return TESTConstants.ChoiceFHM_LC50;
		} else if (i==ChoiceDM_LC50) {
			return TESTConstants.ChoiceDM_LC50;
		} else if (i==ChoiceTP_IGC50) {
			return TESTConstants.ChoiceTP_IGC50;
		} else if (i==ChoiceRat_LD50) {
			return TESTConstants.ChoiceRat_LD50;
		} else if (i==ChoiceBCF) {
			return TESTConstants.ChoiceBCF;
		} else if (i==ChoiceReproTox) {
			return TESTConstants.ChoiceReproTox;
		} else if (i==ChoiceMutagenicity) {
			return TESTConstants.ChoiceMutagenicity;
		} else if (i==ChoiceDescriptors) {
			return TESTConstants.ChoiceDescriptors;
		} else if (i==ChoiceBoilingPoint) {
			return TESTConstants.ChoiceBoilingPoint;
		} else if (i==ChoiceVaporPressure) {
			return TESTConstants.ChoiceVaporPressure;
		} else if (i==ChoiceMeltingPoint) {
			return TESTConstants.ChoiceMeltingPoint;
		} else if (i==ChoiceFlashPoint) {
			return TESTConstants.ChoiceFlashPoint;
		} else if (i==ChoiceDensity) {
			return TESTConstants.ChoiceDensity;
		} else if (i==ChoiceSurfaceTension) {
			return TESTConstants.ChoiceSurfaceTension;
		} else if (i==ChoiceThermalConductivity) {
			return TESTConstants.ChoiceThermalConductivity;
		} else if (i==ChoiceViscosity) {
			return TESTConstants.ChoiceViscosity;
		} else if (i==ChoiceWaterSolubility) {
			return TESTConstants.ChoiceWaterSolubility;
		} else {
			return "?";
		}
	}
	private String getMethod(int i) {
		if (i==ChoiceHierarchicalMethod) {
			return TESTConstants.ChoiceHierarchicalMethod;
		} else if (i==ChoiceFDAMethod) {
			return TESTConstants.ChoiceFDAMethod;
		} else if (i==ChoiceSingleModelMethod) {
			return TESTConstants.ChoiceSingleModelMethod;
		} else if (i==ChoiceNearestNeighborMethod) {
			return TESTConstants.ChoiceNearestNeighborMethod;
		} else if (i==ChoiceGroupContributionMethod) {
			return TESTConstants.ChoiceGroupContributionMethod;
		} else if (i==ChoiceConsensus) {
			return TESTConstants.ChoiceConsensus;
		} else {
			return "?";
		}
		
	}
	
	
	//this method is easier if running this class from java such as in eclipse
	public void go(String inputFilePath,String inputFileType,String outputFilePath,String endpoint,String method) {
		
//		Example input:
//		String inputFilePath="LC50_prediction.sdf";
//		String inputFileType=r.inputFileTypeSDF;
//		String endpoint=TESTConstants.ChoiceFHM_LC50;
//		String method=TESTConstants.ChoiceHierarchicalMethod;
//		String outputFilePath="results.txt";

		///////////////////////////////////////////////////////////////
		AtomContainerSet ms=null;
		
		if (inputFileType.toUpperCase().equals(inputFileTypeSDF)) {
			ms=r2.LoadFromSDF(inputFilePath);
		} else if (inputFileType.toUpperCase().equals(inputFileTypeSMILES)) {
			ms=r2.LoadFromSmiles(inputFilePath);
		} else {
			JOptionPane.showMessageDialog(null, "enter either SDF or SMILES");
			return;
		}
		
		if (ms==null) {
			System.out.println("molecule set = null");
			return;
		}
		
		if (method.equals(TESTConstants.ChoiceSingleModelMethod) && !TESTConstants.haveSingleModelMethod(endpoint)){
			System.out.println("Single model method is unavailable for this method");
			return;
		}
		
		if (method.equals(TESTConstants.ChoiceGroupContributionMethod) && !TESTConstants.haveGroupContributionMethod(endpoint)){
			System.out.println("Group contribution is unavailable for this method");
			return;
		}
		
//		if (method.equals(TESTConstants.ChoiceRandomForrestCaesar) && !endpoint.equals(TESTConstants.ChoiceReproTox)){
//			System.out.println("Caesar random forest method is only available for reprotox endpoint");
//			return;
//		}
		
		///////////////////////////////////////////////////////////////
		//load model data (if isn't already in memory):
		
		long t1=System.currentTimeMillis();
		r2.loadTrainingData(endpoint,method);
		long t2=System.currentTimeMillis();
		
//		System.out.println((t2-t1)/1000+" secs");
		

		
		///////////////////////////////////////////////////////////////
		//do predictions:
		r2.doPredictions(endpoint,method,ms,outputFilePath);
		
	}
	

	void runFromJava() {
		ToxPredictor.Application.runTEST_From_Command_Line r=new ToxPredictor.Application.runTEST_From_Command_Line();

//		String inputFilePath="Sample_MDL_SDfile.sdf";
//		String inputFilePath="badsmiles.smi";
//		String inputFilePath="smiles.smi";
		
		String inputFilePath="ToxPredictor/test descriptors/ValidatedStructures2d.sdf";
		
		
		String outputFilePath="ToxPredictor/test descriptors/descriptors_cdk1.5.txt";
		
		int iFileType=runTEST_From_Command_Line.numFileTypeSDF;
//		int iFileType=runTEST_From_Command_Line.numFileTypeSMILES;
		
//		int iEndpoint=runTEST_From_Command_Line.ChoiceFHM_LC50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceTP_IGC50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceRat_LD50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceReproTox;
		int iEndpoint=runTEST_From_Command_Line.ChoiceDescriptors;
//		
//		int iMethod=runTEST_From_Command_Line.ChoiceHierarchicalMethod;
		int iMethod=runTEST_From_Command_Line.ChoiceConsensus;
		
		r.go(inputFilePath,outputFilePath,iFileType,iEndpoint,iMethod);
		
	}
	
	
	void runFromJava2() {
		ToxPredictor.Application.runTEST_From_Command_Line r=new ToxPredictor.Application.runTEST_From_Command_Line();

		String inputFilePath="ToxPredictor/test descriptors/bad chemicals.smi";
		String outputFilePath="ToxPredictor/test descriptors/bad chemicals descriptors_cdk1.5.txt";
		
//		int iFileType=runTEST_From_Command_Line.numFileTypeSDF;
		int iFileType=runTEST_From_Command_Line.numFileTypeSMILES;
		
//		int iEndpoint=runTEST_From_Command_Line.ChoiceFHM_LC50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceTP_IGC50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceRat_LD50;
//		int iEndpoint=runTEST_From_Command_Line.ChoiceReproTox;
		int iEndpoint=runTEST_From_Command_Line.ChoiceDescriptors;
//		
//		int iMethod=runTEST_From_Command_Line.ChoiceHierarchicalMethod;
		int iMethod=runTEST_From_Command_Line.ChoiceConsensus;
		
		r.go(inputFilePath,outputFilePath,iFileType,iEndpoint,iMethod);
		
	}
	private void go(String inputFilePath,String outputFilePath,int iFileType,int iEndpoint,int iMethod) {
		String endpoint=this.getEndpoint(iEndpoint);
		String method=this.getMethod(iMethod);
		String inputFileType=this.getFileType(iFileType);
		this.go(inputFilePath, inputFileType, outputFilePath, endpoint, method);
	}
	
	
	void compareDescriptorFiles() {
		
		String folder="ToxPredictor/test descriptors";
		String filepath0=folder+"/descriptors_cdk1.5.txt";
		String filepath1=folder+"/descriptors_cdk1.5 - Copy.txt";
				
		ToxPredictor.Utilities.Utilities.CopyFile(new File(filepath0), new File(filepath1));
		
		
		
		String filepath2=folder+"/descriptors.txt";
		
		wekalite.CSVLoader c=new CSVLoader();
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath1));
			BufferedReader br2=new BufferedReader(new FileReader(filepath2));
			
			Instances i1=c.getDataSetFromFileNoTox(filepath1, "\t");
			Instances i2=c.getDataSetFromFileNoTox(filepath2, "\t");
			
			String del="\t";
			
			System.out.println("sets loaded");
			

			for (int i=0;i<i1.numInstances();i++) {
				
				Instance instance1=i1.instance(i);
				
				if (i2.instance(instance1.getName())==null) continue;
				
				Instance instance2=i2.instance(instance1.getName());
				
//				System.out.println(instance1.getName()+"\t"+instance1.value("XLOGP")+"\t"+instance2.value("XLOGP"));
				
				
				for (int j=0;j<instance1.numAttributes();j++) {
					double val1=instance1.value(j);
					double val2=instance2.value(j);
					
					double err=Math.abs(val1-val2);
					
//					if (err>1e-4 && instance1.attribute(j).indexOf("XLOGP")==-1) {
//						System.out.println(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+err);
//						break;
//					}
					
					if (err>1e-4) {
						System.out.println(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+err);
						break;
					}

				}
				
				
				
			}
			
			
//			double tol=1e-4;
//			
//			Set<String> keys =data1.keySet();
//	        for(String key: keys){
//	        	LinkedList <String>list1=data1.get(key);
//	        	
//	        	if (data2.get(key)!=null) {
//	        		LinkedList <String>list2=data2.get(key);
//	        		
////	        		System.out.println(key+"\t"+list1.get(0)+"\t"+list2.get(0));
//	        		
//	        		
//	        		for (int i=0;i<list1.size();i++) {
//	        			double val1=Double.parseDouble(list1.get(i));
//	        			double val2=Double.parseDouble(list2.get(i));
//	        			double diff=Math.abs(val1-val2);
//	        			if (diff>tol) {
//	        				System.out.println(key+"\t"+i+"\t"+diff);//todo output descriptor thats wrong
//	        				break;
//	        			}
//	        		}
//	        		
//	        		
//	        	}
//	        	
//	            
//	        }
	        
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		runTEST_From_Command_Line r=new runTEST_From_Command_Line();
//		r.go(args);
		
//		r.runFromJava();
//		r.runFromJava2();
		r.compareDescriptorFiles();
		
		
		//For debug, use easy way to choose options:
//		String inputFilePath="LC50_prediction.sdf";
//		String inputFileType=r.inputFileTypeSDF;
//		String endpoint=TESTConstants.ChoiceFHM_LC50;
////		String method=TESTConstants.ChoiceHierarchicalMethod;
//		String method=TESTConstants.ChoiceConsensus;
//		String outputFilePath="results.txt";
//		r.go(inputFilePath, inputFileType, outputFilePath, endpoint, method);
		
	}
}
