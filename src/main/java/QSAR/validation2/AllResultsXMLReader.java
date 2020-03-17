package QSAR.validation2;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
//import java.util.ListIterator;
import java.util.Vector;

import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;
import ToxPredictor.Application.Calculations.TaskCalculations;
import wekalite.*;

public class AllResultsXMLReader {
	CSVLoader atf = new CSVLoader();
	public boolean done = false;
	public boolean debug=false;
//	public boolean debug=true;
	
	
	public Instances loadTrainingSet(String trainingFilePath) {
		Instances trainingSet=null;
		try {
			trainingSet = atf.getDataSetFromFile(trainingFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainingSet;
	}
	
	void testLoad() {
		debug=false;
		
		String endpoint="LD50";
//	    String desc="_Final"+NearestNeighborMethod.FindFinalNumber(endpoint);
//	    String folder="ToxPredictor/DescriptorTextTables/"+endpoint+desc+" data files/2d";
//		String trainingFilePath=folder+"/"+endpoint+"_training_set-2d-rnd1.csv";
//		String resultsXML=folder+"/"+endpoint+"_training_set-2d-rnd1.xml";

//		String folder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
		String folder="L:/Priv/Cin/NRMRL/CompTox/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
		String trainingFilePath=folder+"/"+endpoint+"_training_set-2d.csv";
		String predictionFilePath=folder+"/"+endpoint+"_prediction_set-2d.csv";
		String resultsXML=folder+"/"+endpoint+"_training_set-2d.xml";
		
		boolean isXMLFileInsideJar=false;

//		for (int i=1;i<=2;i++) {
			long t1=System.currentTimeMillis();

			Instances trainingSet=loadTrainingSet(trainingFilePath);
			long t2=System.currentTimeMillis();
			System.out.println("time to load training set = "+(t2-t1)/1000.0+" secs");

			trainingSet.calculateMeans();
			trainingSet.calculateStdDevs();

//			Instances predictionSet=a.loadTrainingSet(predictionFilePath);

			AllResults allResults=readAllResults(resultsXML, trainingSet, isXMLFileInsideJar,null);
			long t3=System.currentTimeMillis();
			System.out.println("time to load allresults = "+(t3-t2)/1000.0+" secs");
//		}

			System.out.println("total time = "+(t3-t1)/1000.0+" secs");
		
	}
	
	public static void main(String[] args) {
		long t0=System.currentTimeMillis();

		AllResultsXMLReader a=new AllResultsXMLReader();
		a.testLoad();
	}
	

	/**
	 * This method is used by external classes to load the AllResults object
	 * from an XML file
	 * 
	 * @param resultsXML
	 *            - path of XML file
	 * @param trainingDataset
	 *            - instances for trainingDataSet
	 * @param source
	 * @return
	 */
	public AllResults readAllResults(String resultsXML,
			Instances trainingSet, boolean isXMLFileInsideJar) {
		try {
			AllResults ar = goThroughXMLFile(resultsXML, isXMLFileInsideJar,
					false);

			long t1=System.currentTimeMillis();
			regenAllResults(trainingSet, ar,trainingSet.getStdDevs());
			long t2=System.currentTimeMillis();
			
//			System.out.println("time to regen results:"+(t2-t1)/1000.0);
//			this.regenAllOptimalResults(ar.getResults(), trainingSet);//slower than method above it!

			ar.setOffsets(trainingSet.getMeans());
			ar.setScales(trainingSet.getStdDevs());
			return ar;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public AllResults readAllResults(String resultsXML,Instances trainingSet,boolean isXMLFileInsideJar, TaskCalculations task) {
		this.done=false;

		try {

			if (task!=null) {
				task.setMessage("Loading cluster data file...0%");
			}

			AllResults ar = goThroughXMLFile(resultsXML,isXMLFileInsideJar,false);

			
			long t1=System.currentTimeMillis();
			//*****************************************************
			//sort the results object inside AllResults object so that items in
			//vector are in ascending order with respect to the cluster number
			// (they are in reverse order previously):
			Vector<OptimalResults>results=ar.getResults();
			Vector<OptimalResults> vResults2=new Vector<OptimalResults>();
			for (int i=results.size()-1;i>=0;i--) {
				vResults2.add(results.get(i));
			}
			ar.setResults(vResults2);
			//*****************************************************************
			long t2=System.currentTimeMillis();
			
//			System.out.println("Time to resort results objects:"+(t2-t1)/1000.0);
			//Note: above sorting only takes like 0.001 seconds! So dont need to change code or recreate xml file!			
			

			//			System.out.println("readAllResults:"+resultsXML);

			double [] offsets=trainingSet.getMeans();
			double [] scales=trainingSet.getStdDevs();

			ar.setOffsets(offsets);
			ar.setScales(scales);

			//			System.out.println(task.getClass().getName());
			//System.out.println("here");

			long totsum=0;
			for (int i=0;i<ar.getResults().size();i++) {
				totsum+=ar.getResults().get(i).getNumChemicals();
			}
			long currentsum=0;//progress is proportional to total number of chemicals in all clusters

			for (int i=0;i<ar.getResults().size();i++) {

				if (done) {
					//System.out.println("Loading of xml file aborted");
					return null;
				}
				OptimalResults or=(OptimalResults)ar.getResults().get(i);

				currentsum+=or.getNumChemicals();

				this.regenOptimalResults(or, trainingSet, scales);

				if (done) return null;

				//				if (i%100==0) System.out.println("regenOptimalResults:"+i);


				String msg="Loading cluster data file...";
				double progress=((double)currentsum/(double)totsum);

				java.text.DecimalFormat df=new java.text.DecimalFormat("0%");
				msg+=df.format(progress);

				if (task!=null) {
					task.setMessage(msg);
				}

			}

			//			System.out.println("done reading xml file");

			return ar;


		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	
	private void regenAllOptimalResults(Vector<OptimalResults>results,Instances trainingSet) {

		try {

			for (int i=0;i<results.size();i++) {
				OptimalResults or=results.get(i);
				or.setCentroid(new double [trainingSet.numAttributes()]);
				if (or.isValid()) {
					or.setObserved(new double[or.getNumChemicals()]);
					or.setX(new double[or.getNumChemicals()][or.getNumDescriptors() + 1]);
				}
			}
			

			for (int i=0;i<trainingSet.numInstances();i++) {

				Instance instance=trainingSet.instance(i);	

				String CAS=instance.getName();
				double Tox=instance.getToxicity();

				double [] descriptors=instance.getDescriptors();

				for (int numResult=0;numResult<results.size();numResult++) {
					OptimalResults or=results.get(numResult);

					for (int numChemical=0; numChemical<or.getNumChemicals(); numChemical++) {

						if (or.getChemicalNames()[numChemical].equals(CAS)) {
							
							for (int numAttribute=0;numAttribute<trainingSet.numAttributes();numAttribute++) {
								double val=descriptors[numAttribute];
								or.getCentroid()[numAttribute] += val;
							}

							if (or.isValid()) {
								or.getObserved()[numChemical]=Tox;
								for (int numDescriptor = 0; numDescriptor < or.getNumDescriptors(); numDescriptor++) {
									int descriptorNumber = or.getDescriptors()[numDescriptor];
									or.getX()[numChemical][numDescriptor] = descriptors[descriptorNumber];
								}
							}

						}//end if CAS matches
					}

				}//end loop over all OptimalResults objects

				i++;//increment overall chemical number

			}//end loop over all record sets
				
			for (int ii=0;ii<results.size();ii++) {
				OptimalResults or=results.get(ii);

				for (int jj=0; jj<trainingSet.numAttributes(); jj++) {
					or.getCentroid()[jj] /= or.getNumChemicals();
				}
				if (or.isValid()) {
					for (int kk=0;kk<or.getNumChemicals();kk++) {
						or.getX()[kk][or.getNumDescriptors()] = 1.0;
					}
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param trainingSet
	 * @param ar
	 */
	public void regenAllResults(Instances trainingSet, AllResults ar,double [] scales) {
		
		if (debug) {
			OptimalResults or = (OptimalResults) ar.getResults().get(0);
			this.regenOptimalResults(or, trainingSet,scales);
			return;
		} 
		
		for (int i = 0; i < ar.getResults().size(); i++) {
			if (done) return;
			OptimalResults or = (OptimalResults) ar.getResults().get(i);
			this.regenOptimalResults(or, trainingSet,scales);
			
		}
	}

	private int[] getChemicalsUsed(OptimalResults or, Instances trainingSet) {
		int[] chemicalsUsed = new int[or.getNumChemicals()];

		for (int i = 0; i < or.getNumChemicals(); i++) {
			chemicalsUsed[i] = trainingSet.getInstanceNumber(or.getChemicalNames()[i]);			
		}
		return chemicalsUsed;

	}

	private double[][] getX(OptimalResults or, Instances trainingSet) {

		double[][] x = new double[or.getNumChemicals()][or.getDescriptorNames().length + 1];

		for (int i = 0; i < or.getNumChemicals(); i++) {
			int chemicalNumber = or.getChemicalsUsed()[i];
			for (int j = 0; j < or.getNumDescriptors(); j++) {

				int descriptorNumber = or.getDescriptors()[j];
				x[i][j] = trainingSet.instance(chemicalNumber).value(
						descriptorNumber);
			}

			x[i][or.getDescriptorNames().length] = 1.0;
		}
		
//		if (or.getClusterNumber()==11852) {
//			int chemicalNumber = or.getChemicalsUsed()[0];
//			int descriptorNumber = or.getDescriptors()[0];
//			
//			System.out.println(chemicalNumber+"\t"+descriptorNumber+"\t"+trainingSet.instance(chemicalNumber).value(
//					descriptorNumber));
//			
//		}
		
		
		return x;
	}

	private double [] calculateCentroid (OptimalResults or,Instances trainingSet) {
		double [] means=new double [trainingSet.numAttributes()];
		
		for (int i=0;i<or.getNumChemicals();i++) {
            for (int j=0;j<trainingSet.numAttributes();j++) {
            	double val=trainingSet.instance(or.getChemicalsUsed()[i]).value(j);
            	means[j] += val;
            }
        }

		for (int j=0; j<trainingSet.numAttributes(); j++) {
            means[j] /= or.getNumChemicals();;
        }
		
		return means;
	}
	
    public double recalcRMax(OptimalResults or,Instances trainingSet,double [] scales) {
        // TODO Auto-generated method stub
        double[] dist = new double[or.getNumChemicals()];

//        trainingSet.CalculateStdDevs();
//        double [] scales=trainingSet.getStdDevs();
        
        double distmax =0.0;

        if (or.getCentroid().length > 1){
            for (int j=0; j<or.getNumChemicals(); j++) {
                Instance chemical = trainingSet.instance(or.getChemicalNames()[j]);
                dist [j]= 0.0;
                for (int i=0; i<or.getCentroid().length; i++) {
                    if (scales[i] != 0.0){
                        double sumc =(chemical.value(i)- or.getCentroid()[i])/scales[i];
                        dist[j] += sumc*sumc;
                    }
                }
                if (  Math.sqrt(dist[j]) > distmax)  distmax = Math.sqrt(dist[j]);
            }
        }
        double rMax= distmax;// this was the highest distance of chemical in cluster
        return rMax;
    }
	
	private void regenOptimalResults(OptimalResults or, Instances trainingSet,double []scales) {

		try {

			if (or.getChemicalsUsed()==null) {
				or.setChemicalsUsed(this.getChemicalsUsed(or, trainingSet));
			} else if (or.getChemicalNames()==null) {
				or.setChemicalNames(this.getChemicalNames(or,trainingSet));
			}
			


			// ***************************************************************
//			long t1=System.currentTimeMillis();
//			Instances cc = this.RebuildCluster(trainingSet, or);
//			cc.calculateMeans();
//			or.setCentroid(cc.getMeans());
//			long t2=System.currentTimeMillis();
//			if (debug) System.out.println("time to calculate centroid = "+(t2-t1)+ " milisec");
			// ***************************************************************
			//Note it is not necessary to rebuild the cluster if we can calculate the centroid here:
			long t1=System.currentTimeMillis();
			or.setCentroid(this.calculateCentroid(or, trainingSet));
			long t2=System.currentTimeMillis();
			if (debug) System.out.println("time to calculate centroid = "+(t2-t1)+ " milisec");
			// ***************************************************************

			if (done)
				return;

			if (or.getBcoeff() == null) {
				or.setValid(false);// TMM 1/23/12
			}

			
			if (done)
				return;

			if (or.isValid()) {
				
				if (or.getDescriptors()==null) {
					or.setDescriptors(this.getDescriptors(or, trainingSet));	
				} else if (or.getDescriptorNames()==null) {
					or.setDescriptorNames(this.getDescriptorNames(or,trainingSet));
				}
				
				if (done)
					return;

				or.setObserved(this.getObserved(or, trainingSet));
				if (done)
					return;

//				if (or.getClusterNumber()==11852) {
//					for (int i=0;i<or.getDescriptors().length;i++) {
//						System.out.println(i+"\t"+or.getDescriptors()[i]);
//					}
//				}

				
				or.setX(this.getX(or, trainingSet));

				
//				System.out.println((or==null)+"\t"+(trainingSet==null));
				
				//The rmax values in the xml file may be wrong if there were outliers that were removed!
				double rMax2=recalcRMax(or, trainingSet,scales);
//				System.out.println(or.clusterNumber+"\t"+or.rMax+"\t"+rMax2);
				or.setrMax(rMax2);

				or.regenHMax();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] getDescriptorNames(OptimalResults or, Instances trainingSet) {
		// TODO Auto-generated method stub
		
		or.setNumDescriptors(or.getDescriptors().length);

		String [] descriptorNames=new String [or.getNumDescriptors()];
		
		for (int i=0;i<or.getNumDescriptors();i++) {
			descriptorNames[i]=trainingSet.attribute(or.getDescriptors()[i]);
		}
		
		return descriptorNames;
	}

	private String[] getChemicalNames(OptimalResults or, Instances trainingSet) {
		// TODO Auto-generated method stub
		
		String [] chemicalNames=new String [or.getNumChemicals()];
		
		for (int i=0;i<or.getNumChemicals();i++) {
			chemicalNames[i]=trainingSet.instance(or.getChemicalsUsed()[i]).getName();
		}
		
		return chemicalNames;
	}

	private int[] getDescriptors(OptimalResults or, Instances trainingSet) {
		
		or.setNumDescriptors(or.getDescriptorNames().length);
		
		int [] descriptors=new int[or.getNumDescriptors()];
		
		for (int i=0;i<or.getNumDescriptors();i++) {
			String descriptorName=or.getDescriptorNames()[i];
			descriptors[i]=trainingSet.getAttributeNumber(descriptorName);
			
			if (descriptors[i]==-1) {
				System.out.println(or.getClusterNumber()+"\t"+descriptorName);
			}
		}
		
		return descriptors;
	}

	private double[] getObserved(OptimalResults or, Instances trainingSet) {
		double[] observed = new double[or.getNumChemicals()];

		for (int i = 0; i < or.getNumChemicals(); i++) {
			int chemicalNumber = or.getChemicalsUsed()[i];
			observed[i] = trainingSet.instance(chemicalNumber).getToxicity();
		}
		return observed;
	}

	private AllResults goThroughXMLFile(String resultsXML,
			boolean isXMLFileInsideJar, boolean ReadOffsetsScales)
			throws Exception {
		AllResults allResults = null;


		java.io.InputStream ins = null;
		XMLDecoder decoder = null;

		if (isXMLFileInsideJar) {
			ins = this.getClass().getClassLoader()
					.getResourceAsStream(resultsXML);
			decoder = new XMLDecoder(new BufferedInputStream(ins));
		} else {
			decoder = new XMLDecoder(new java.io.FileInputStream(resultsXML));
		}

		try {
			allResults = new AllResults();

			if (ReadOffsetsScales) {
				allResults.setOffsets((double[]) decoder.readObject());
				allResults.setScales((double[]) decoder.readObject());
			}
			
			Vector vResults = allResults.getResults();
			OptimalResults results = (OptimalResults) decoder.readObject();

			while (results != null) {
				vResults.add(results);
				results = (OptimalResults) decoder.readObject();
				
				if (done) return null;
				
//				System.out.println(results.clusterNumber);

			}
			
			

			


		} catch (ArrayIndexOutOfBoundsException ex) {
			// we are finished with building vResults when this occurs
			// System.out.println("error: "+ex+" \nfor "+resultsXML);
			// ex.printStackTrace();
		} catch (Exception e) {
		} finally {
			decoder.close();
		}
		return allResults;
	}

	
	private boolean HaveChemical(String CAS, OptimalResults or) {
		
		for (int i=0;i<or.getNumChemicals();i++) {	
			if (or.getChemicalNames()[i].equals(CAS)) {
				return true;
			}
		}
		return false;
	}
	private Instances RebuildCluster(Instances trainingSet, OptimalResults or) {

		Instances instances = null;
		//loop over chemical names:
		for (int i=0;i<or.getNumChemicals();i++) {
			String name=or.getChemicalNames()[i];

			Instance instance=trainingSet.instance(name);
			
			if (instances== null) {
				instances = trainingSet
				.createInstancesFromInstance(instance);
			} else {
				instances.addInstance(instance);
			}
		}
		
		return instances;
	}

}
