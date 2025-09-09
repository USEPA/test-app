package ToxPredictor.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

//import weka.core.Instance;
//import weka.core.Instances;

import wekalite.*;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Utilities.Utilities;



public class Lookup {

	/**
	 * @param args
	 */

	
	
	public static double CalculateCosineCoefficient(Instance c1, Instance c2,
			double[] Mean, double[] StdDev) {

		double TC = 0;
		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;

		for (int j = 2; j < c1.numValues(); j++) {

			double xj = c1.value(j);
			double yj = c2.value(j);

			if (StdDev[j] == 0) {
				xj = 0;
				yj = 0;

			} else {
				xj = (xj - Mean[j]) / StdDev[j];
				yj = (yj - Mean[j]) / StdDev[j];
			}

			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;
		}

		TC = SumXY / Math.sqrt(SumX2 * SumY2);
		// System.out.println(TC);

		return TC;

	}

	public boolean inTrainingSet(Instance chemical, Instances trainingDataSet) {


		double [] Mean = trainingDataSet.getMeans();
		double[] StdDev = trainingDataSet.getStdDevs();
		
		

		for (Instance chemicali:trainingDataSet.getInstances()) {

			double SimCoeff = CalculateCosineCoefficient(chemical, chemicali, Mean, StdDev);

			System.out.println(chemicali.getName() + "\t" + SimCoeff);

			if (SimCoeff > 0.995) {
				System.out.println("Match in training set is found, CAS = "
						+ chemicali.getName() + "\tSimCoeff=" + SimCoeff);
				return true;
			}
		}
		return false;
	}

	public class ExpRecord {
		public Double expToxValue;
		public String expCAS;
		public String expSet;
		public double MW;
		public String expMOA;
	}

	public Double LookupExpValByCAS(String CAS, Instances trainingDataSet) {

		int instanceNumber=trainingDataSet.getInstanceNumber(CAS);
		if(instanceNumber==-1) return null;
		Instance instance=trainingDataSet.instance(instanceNumber);
		return instance.classValue();
		
	}
	
	public ExpRecord LookupExpRecordByCAS(String CAS, Instances trainingDataSet,String set) {

		int instanceNumber=trainingDataSet.getInstanceNumber(CAS);
		if(instanceNumber==-1) return null;
		
		Instance instance=trainingDataSet.instance(instanceNumber);
		
		ExpRecord er=new ExpRecord();
		er.expToxValue=instance.classValue();
		er.MW=instance.value("MW");
		er.expCAS=CAS;
		er.expSet=set;
		return er;
		
	}

	public ExpRecord LookupExpValByStructure(Instance chemical,
			Instances trainingDataSet,String set) {

		double [] Mean = trainingDataSet.getMeans();
		double[] StdDev = trainingDataSet.getStdDevs();

		for (Instance chemicali:trainingDataSet.getInstances()) {
			
			double SimCoeff = CalculateCosineCoefficient(chemical, chemicali,
					Mean, StdDev);

			if (SimCoeff > 0.999) {
				ExpRecord e = new ExpRecord();
				e.expToxValue = chemicali.classValue();
				e.expCAS = chemicali.getName();
				e.MW=chemicali.value("MW");
				e.expSet=set;
				return e;
			}
		}

		return null;
	}
	
	public String LookupCASByStructure(Instance chemical,
			Instances trainingDataSet) {

		double [] Mean = trainingDataSet.getMeans();
		double[] StdDev = trainingDataSet.getStdDevs();


		for (Instance chemicali:trainingDataSet.getInstances()) {
			
			double SimCoeff = CalculateCosineCoefficient(chemical, chemicali,
					Mean, StdDev);

			if (SimCoeff > 0.999) {
				return chemicali.getName();
			}
		}

		return "";
	}

	public static double LookUpToxVal(String srchCAS, String ToxVar,
			String filename, String delimiter) {

		try {
			// System.out.println("here");
			srchCAS = srchCAS.trim();

			// System.out.println(filename);

			File myFile = new File(filename);
						

			BufferedReader br = new BufferedReader(new FileReader(myFile));
			String Header = br.readLine();

			List<String> headerlist = Utilities.Parse3(Header, delimiter);

			// determine field number:
			// int fieldnumber=-1;

			int fieldnumber = Utilities.GetColumnNumber(ToxVar, headerlist);

			// System.out.println(ToxVar+"\t"+Header);

			// for (int i=0;i<=headerlist.size()-1;i++) {
			// String s=(String)headerlist.get(i);
			// if (s.equals(ToxVar)) {
			// fieldnumber=i;
			// break;
			// }
			// }

			while (true) {
				String Line = br.readLine();
				
				

				if (!(Line instanceof String)) {
					br.close();
					return -99;
				}

				List<String> list = Utilities.Parse3(Line, delimiter);
				String currentCAS = (String) list.get(0); // for now assume
															// CAS is first
															// field
				currentCAS = currentCAS.trim();

//				System.out.println(srchCAS+"\t"+currentCAS);
				
				if (list.size()<2) continue;
				
				if (currentCAS.equals(srchCAS)) {
					String strval = (String) list.get(fieldnumber);
					double val = Double.parseDouble(strval);

					br.close();
					return val;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return 0;

	}

	public static Double LookUpToxVal(String srchCAS, int ToxColumn,
			String filename, String delimiter) {

		String Line="";
		try {

			srchCAS = srchCAS.trim();

			File myFile = new File(filename);

			BufferedReader br = new BufferedReader(new FileReader(myFile));
			br.readLine();//Header
			
//			System.out.println(Header);
			
//			List<String> headerlist = Utilities.Parse3(Header, delimiter);

			while (true) {
				Line = br.readLine();

				if (Line==null) {
					br.close();					
					return null;
				}
				

//				System.out.println(Line);
				List<String> list = Utilities.Parse3(Line, delimiter);
				
//				if (list.size()==0)  {
//					System.out.println(srchCAS+" not found in "+filename);
//					return -9999;
//				}

//				if (srchCAS.equals("066999-80-2")) {
//					System.out.println(Line);
//				}

				String currentCAS = list.get(0); // for now assume
				currentCAS = currentCAS.trim();
				
				
//				System.out.println(srchCAS+"\t"+currentCAS);

				if (currentCAS.equals(srchCAS)) {
					String strval = (String) list.get(ToxColumn);
					double val = Double.parseDouble(strval);

					br.close();
					return val;
				}
			}

		} catch (Exception e) {
			System.out.println(Line);
			e.printStackTrace();

		}

		return null;

	}

	/**
	 * Looks up toxicity using CAS number from text file in jar file
	 * 
	 * @param srchCAS
	 * @param ToxColumn
	 * @param filename
	 * @param delimiter
	 * @return
	 */
	public Double LookUpToxVal2(String srchCAS, int ToxColumn, String filename,
			String delimiter) {

		try {

			srchCAS = srchCAS.trim();

//			File myFile = new File(filename);

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			BufferedReader br = new BufferedReader(new InputStreamReader(ins));
			br.readLine();//header

//			List<String> headerlist = Utilities.Parse(Header, delimiter);//header

			while (true) {
				String Line = br.readLine();

				if (!(Line instanceof String)) {
					br.close();
					return null;
				}

				List<String> list = Utilities.Parse(Line, delimiter);
				String currentCAS = list.get(0); // for now assume
															// CAS is first
															// field
				currentCAS = currentCAS.trim();

				if (currentCAS.equals(srchCAS)) {
					String strval = list.get(ToxColumn);
					double val = Double.parseDouble(strval);

					br.close();
					return val;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return null;

	}

	public static Double LookUpExpKow(String CAS) {

		File myFile = new File("ToxPredictor/system/EXPKOW.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(myFile))) {
			while (CAS.length() < 11) {
				CAS = "0" + CAS;
			}

			// System.out.println(CAS);

			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				if (Line.indexOf(CAS) == 0) {
					List<String> l = Utilities.Parse(Line, "|");
					String strKow = l.get(2);
					double Kow = Double.parseDouble(strKow);
					return Kow;
				}

			}

			br.close();
			return null;
			
		} catch (Exception e) {
			return null;
		}

	}

	public String LookUpSmiles(String CAS) {
		try {

			String file = "smilecas-5-9-2006.txt";

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file);

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			while (CAS.length() < 11) {
				CAS = "0" + CAS;
			}

			// System.out.println(CAS);

			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				if (Line.indexOf(CAS) == 0) {

					List<String> l = Utilities.Parse(Line, "|");
					String smiles = l.get(2);
					return smiles;
				}

			}

			br.close();
			return "missing";

		} catch (Exception e) {
			return "missing";
		}

	}

	/**
	 * Searches a file (in a jar) for the record specified by the keyValue
	 * and then returns the value for valueColumnName 
	 * 
	 * @param filename
	 * @param keyValue
	 * @param keyColumnName
	 * @param valueColumnName
	 * @param delimiter
	 * @return
	 */
	public String LookUpValueInJarFile(String filename, String keyValue,
			String keyColumnName, String valueColumnName, String delimiter) {
		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header = br.readLine();
			
//			System.out.println(valueColumnName);

			int keyColumnNumber = ToxPredictor.Utilities.Utilities
					.FindFieldNumber(header, keyColumnName, delimiter);
			int valueColumnNumber = ToxPredictor.Utilities.Utilities
					.FindFieldNumber(header, valueColumnName, delimiter);

			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				java.util.List<String> l = ToxPredictor.Utilities.Utilities
						.Parse3(Line, delimiter);

				String currentKey = l.get(keyColumnNumber);

				if (keyValue.equals(currentKey)) {
					String currentValue = l.get(valueColumnNumber);
					return currentValue;
				}
			}

			br.close();
			return "N/A";

		} catch (Exception e) {
			return "N/A";
		}

	}
	
	/**
	 * Useful for getting list of cas numbers in prediction file
	 * 
	 * @param filename
	 * @param keyColumnName
	 * @param delimiter
	 * @return
	 */
	public HashSet<String> lookUpValsInJar(String filename, String keyColumnName, String delimiter) {
		
		HashSet<String>vals=new HashSet<>();
		
		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header = br.readLine();
			
//			System.out.println(valueColumnName);

			int keyColumnNumber = ToxPredictor.Utilities.Utilities
					.FindFieldNumber(header, keyColumnName, delimiter);

			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				java.util.List<String> l = ToxPredictor.Utilities.Utilities
						.Parse3(Line, delimiter);
				String currentKey = l.get(keyColumnNumber);
				vals.add(currentKey);
			}

			br.close();
			return vals;

		} catch (Exception e) {
			return null;
		}
	}

	
	public HashSet<String> lookUpValsInSDF(String filename, String keyColumnName) {

		try {

			java.io.InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);

			HashSet<String> values = new HashSet<>();

			IteratingSDFReader reader = new IteratingSDFReader(ins, DefaultChemObjectBuilder.getInstance());

			while (reader.hasNext()) {
				IAtomContainer molecule = reader.next();

				// Extract the CAS number property
				String casNumber = (String) molecule.getProperty(keyColumnName);

				if (casNumber != null) {
					values.add(casNumber);
				}
			}

			reader.close();
			return values;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Searches a file (in a jar) for the record specified by the keyValue
	 * and then returns the value for valueColumnName 
	 * 
	 * @param filename
	 * @param keyValue
	 * @param keyColumnName
	 * @param valueColumnName
	 * @param delimiter
	 * @return
	 */
	public Double LookUpValueConsensusValueOmitFDAInJarFile(String filename, String keyValue,
			String keyColumnName, String delimiter) {
		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header = br.readLine();
			
//			System.out.println(valueColumnName);

			int keyColumnNumber = ToxPredictor.Utilities.Utilities
					.FindFieldNumber(header, keyColumnName, delimiter);
			
			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				java.util.List<String> l = ToxPredictor.Utilities.Utilities
						.Parse3(Line, delimiter);

				String currentKey = l.get(keyColumnNumber);

				if (keyValue.equals(currentKey)) {
					Vector<String>methods=new Vector<>();
					methods.add(TESTConstants.ChoiceHierarchicalMethod);
					methods.add(TESTConstants.ChoiceSingleModelMethod);
					methods.add(TESTConstants.ChoiceGroupContributionMethod);
					methods.add(TESTConstants.ChoiceNearestNeighborMethod);

					double predConsensus=0;
					int npreds=0;

					for (String method:methods) {
						int colNumber = ToxPredictor.Utilities.Utilities
								.FindFieldNumber(header, method, delimiter);
						
						double pred=(Double.parseDouble(l.get(colNumber)));
						
						if(pred>-9999) {
							predConsensus+=pred;
							npreds++;
						}
						
					}
					
					if (npreds<WebTEST4.minPredCount) return null;
					else {
						predConsensus/=(double)npreds;						
						return predConsensus;						
					}
				}
			}

			br.close();
			return null;

		} catch (Exception e) {
			return null;
		}

	}
	
	/**
	 * Searches a file (in a jar) for the record specified by the keyValue
	 * and then returns the value for valueColumnName 
	 * 
	 * @param filename
	 * @param keyValue
	 * @param keyColumnName
	 * @param valueColumnName
	 * @param delimiter
	 * @return
	 * 
	 * TODO this needs to exclude FDA when calculating consensus...
	 * 
	 * 
	 */
	public Double CalculateMAE(String filename, String expColumnName,
			String methodColumnName, String delimiter) {
		try {

			
			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header = br.readLine();
//			System.out.println(header);
//			System.out.println(valueColumnName);

			int colExp = ToxPredictor.Utilities.Utilities
			.FindFieldNumber(header, expColumnName, delimiter);

			int colPred= ToxPredictor.Utilities.Utilities
					.FindFieldNumber(header, methodColumnName, delimiter);

			double MAE=0;
			double count=0;
			
			while (true) {
				String Line = br.readLine();
				if (!(Line instanceof String))
					break;

				java.util.List<String> l = ToxPredictor.Utilities.Utilities
						.Parse(Line, delimiter);
				
				String strExp=l.get(colExp);
				String strPred=l.get(colPred);
				
				
				if (strPred.equals("N/A"))continue;

				double exp=Double.parseDouble(strExp);
				double pred=Double.parseDouble(strPred);
				
				if (pred==-9999) continue;
				
//				System.out.println(exp+"\t"+pred);
				
				MAE+=Math.abs(exp-pred);
				count++;
			}
			MAE/=count;

			br.close();
			return MAE;

		} catch (Exception e) {
			return null;
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String abbrev="LC50";
		String predfilename = abbrev + "/" + abbrev
		+ " test set predictions.txt";
		
		Lookup l=new Lookup();
		
		System.out.println(l.CalculateMAE(predfilename, "expToxicValue", "Consensus", "\t"));


	}

}
