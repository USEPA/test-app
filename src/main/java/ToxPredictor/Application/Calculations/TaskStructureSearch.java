package ToxPredictor.Application.Calculations;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyName;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.Bond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IBond.Stereo;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;


import AADashboard.Application.MoleculeUtilities;
import ToxPredictor.misc.MolFileUtilities;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinWarning;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.GUI.Miscellaneous.SwingWorker;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Database.DSSToxRecord;
//import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.IndigoUtilities;
import ToxPredictor.Utilities.Utilities;

//import uk.ac.cam.ch.wwmm.opsin.NameToStructure;// uk\ac\cam\ch\opsin\opsin-core\2.5.0
//import uk.ac.cam.ch.wwmm.opsin.NameToStructureConfig;
//import uk.ac.cam.ch.wwmm.opsin.OpsinResult;



public class TaskStructureSearch {

	private static final Logger logger = LogManager.getLogger(TaskStructureSearch.class);
	
	public static final int TypeAny=9999;
	public static final int TypeCAS=0;
	public static final int TypeSmiles=1;
	public static final int TypeSDF=2;
	public static final int TypeSDF_In_Jar=3;
	public static final int TypeName = 4;
	
	
	SwingWorker worker;

	// task variables:
	private int lengthOfTask;

	private int current = 0; // variable used to update length of progress bar
								// (100 = done)

	private static boolean done = false;

	private static String statMessage; // message that tells what has been done so far
								// or currently
	Object gui = null;
	String filepath;

	public int structureType;//smiles,CAS,or name
	int runType;//single chemical or batch
	
//	ChemicalFinder cf;
	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();

	DescriptorFactory df=new DescriptorFactory(false);
	static StructureDiagramGenerator sdg = new StructureDiagramGenerator();
	NameToStructureOpsin nameToStructureOpsin=new NameToStructureOpsin(); 
	
	public TaskStructureSearch() {
	}

	public void init(String filepath,int structureType,int runType,Object gui) {
		this.filepath=filepath;
		this.structureType=structureType;
		this.runType=runType;
		
		this.gui=gui;
		
	}

	
	public static String parseSearchCAS(String srchCAS) {
		
		
		if (srchCAS.indexOf("-")>-1) {	//kill off zeros in front:
			String part1, part2;
			part1 = (Integer.parseInt(srchCAS.substring(0, srchCAS.indexOf("-"))))
					+ "";
			part2 = srchCAS.substring(srchCAS.indexOf("-"), srchCAS.length());
			srchCAS = part1 + part2;
		
		} else { //missing dashes- try to convert it:
			String temp=srchCAS;
			String part1,part2,part3;
			
			if (temp.length()>=4) {
				part3=temp.substring(temp.length()-1,temp.length());
				temp=temp.substring(0,temp.length()-1);

				part2=temp.substring(temp.length()-2,temp.length());
				temp=temp.substring(0,temp.length()-2);

				part1=temp;
				srchCAS=part1+"-"+part2+"-"+part3;
			} else {
				return srchCAS;
			}
		}

		return srchCAS;
		
	}
	
	/**
	 * Currently this method loads a molecule via a MDL molfile
	 * @param inFile file for MDL molfile
	 */
	public static AtomContainer loadFromMolFile(File inFile,Component f) {

		done=false;
		if (!inFile.exists()) {
			JOptionPane.showMessageDialog(f, "\""+inFile.getAbsolutePath()+"\" cannot be found.");
			return null;
		}

		AtomContainerSet acs=LoadFromSDF(inFile.getAbsolutePath());
		
		if (acs.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f,
					"Molecule not loaded, please select another mol file");
			return null;
		}

		
		AtomContainer molecule=(AtomContainer)acs.getAtomContainer(0);

		String error=molecule.getProperty("Error");
		if (error.contentEquals("Multiple molecules")) {
			JOptionPane.showMessageDialog(f,"Molecule not connected, please select another mol file");
			return null;
		}

		
		
		return molecule;
		
//		f.setNameCAS(molecule);

	}
	
	/**
	 * Called to start the task.
	 */
	public void go() {
		worker = new SwingWorker() {
			public Object construct() {
				current = 0;
				done = false;
				// canceled = false;
				statMessage = "";
				return new ActualTask();
			}
		};
		worker.start();
	}

	/**
	 * Called to find out how much work needs to be done.
	 * 
	 * @return Length of Task
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/**
	 * Called to find out how much has been done.
	 * 
	 * @return Current progress in %
	 */
	public int getCurrent() {
		return current;
	}

	public void stop() {
		done = true;

		statMessage = "canceled";

		if (gui instanceof TESTApplication) {
			((TESTApplication) gui).setCursor(Utilities.defaultCursor);
		} 
		
//		else if (gui instanceof ToxApplet7) {
//			((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//		}

		// gui.setCursor(Utilities.defaultCursor);

		System.out.println("\n****stop!****\n");

		// worker.interrupt();// this doesnt seem to want to work!

	}

	
	/**
	 * Called to find out if the task has completed.
	 * 
	 * @return Done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the most recent status message, or null if there is no current
	 * status message.
	 * 
	 * @return statMessage
	 */
	public String getMessage() {
		return statMessage;
	}
	
	public static void setMessage(String message) {
		statMessage=message;
	}


	/**
	 * The actual long running task. This runs in a SwingWorker thread.
	 */

	
	private int GetSmilesCount() {
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
	

	
	private String GetSmilesDelimiter() {
		String del="";
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return del;
	}
	private int GetSDFCount() {
		int count=0;
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				
				if (Line.indexOf("$$$")>-1) count++;
			}
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	private int GetSDFCountInJar() {
		int count=0;
		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
			.getResourceAsStream(filepath);

			InputStreamReader isr = new InputStreamReader(ins);

			BufferedReader br = new BufferedReader(isr);

			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				
				if (Line.indexOf("$$$")>-1) count++;
			}
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	

	
	public AtomContainerSet LoadFromSDFInJar() {

		
		try {
			java.io.InputStream ins = this.getClass().getClassLoader()
			.getResourceAsStream(filepath);
			InputStreamReader isr = new InputStreamReader(ins);
			
			IteratingSDFReader reader = new IteratingSDFReader(isr,DefaultChemObjectBuilder.getInstance());
			
			return LoadFromSDF(reader);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private static AtomContainerSet LoadFromSDF(IteratingSDFReader mr) {
		AtomContainerSet moleculeSet=new AtomContainerSet();
		
		try {

			int counter = 0;
			
//			System.out.println(mr==null);

			
			while (mr.hasNext()) {
				
				counter++;
				
//				System.out.println(done);
				
				if (done) break;
				
//				System.out.println(counter);
				
				AtomContainer m=null;
				
				try {
					m = (AtomContainer)mr.next();

				
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) {
					System.out.println("null chemical, breaking");
					break;
				}
				
    			String message="Loading "+m.getProperty("CAS")+", " +counter;
//    			System.out.println(message);
    			setMessage(message);

//    			String CAS=m.getProperty("CAS");
//    			if (CAS.contains("NOCAS"))
//    				System.out.println(counter+"\t"+m.getProperty("CAS"));

    			
				
//				Hashtable props=m.getProperties();//TODO make sure properties are preserved
				
				m.setProperty("Error", "");				
				WebTEST4.checkAtomContainer(m);
				
				m=CleanUpMolecule(m);

				setCAS(m);

//				m=MolFileUtilities.CheckForAromaticBonds(m);
//				df.Normalize(m);

//				System.out.println(m.getAtomCount());
				m.setProperty("Query", m.getProperty("CAS"));//store ID number as the string used in the query for export later so users can match up results
				moleculeSet.addAtomContainer(m);
				
				
//				System.out.println(CAS+"\t"+m.getAtomCount());
				
			}// end while true;

			mr.close();
			
		} catch (Exception e) {
			e.printStackTrace();

		}
		
		return moleculeSet;
	}

	public static AtomContainerSet LoadFromSDF(String filepath) {
		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());								
			return LoadFromSDF(mr);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
			
		}
	}
	
	
	public static void setCAS(AtomContainer ac) {
		String CASfield=MolFileUtilities.getCASField(ac);
		
		String CAS=null;
		
		ArrayList<DSSToxRecord>recordsCAS=null;
		ArrayList<DSSToxRecord>recordsAC=null;
		
		if (!CASfield.equals("")) {
			CAS=(String)ac.getProperty(CASfield);
			if (CAS != null) {				
				CAS=CAS.trim();
				CAS=CAS.replace("/", "_");
				ac.setProperty("CAS", CAS);				
			}
			recordsCAS=ResolverDb2.lookupByCAS(CAS);		
		} else {
			recordsAC=ResolverDb2.lookupByAtomContainer(ac);						
		}
		
		
//		System.out.println("*" +CAS+"*");
//		System.out.println(CAS+"\t"+recordsCAS.size());
		
		if (CAS!=null) {
			DSSToxRecord rec=new DSSToxRecord();
			rec.cas=CAS;
			
			if (!MolFileUtilities.getNameField(ac).isEmpty()) {
				String NameField=MolFileUtilities.getNameField(ac);
				if (ac.getProperty(NameField)!=null) {
					rec.name=ac.getProperty(NameField);
				}
			} 
			
			if (rec.name!=null && rec.name.isEmpty()) {
				if (recordsCAS.size()>0) rec.name=recordsCAS.get(0).name;
			}
			
			if (rec.name==null || rec.name.isEmpty()) {

				if (recordsAC==null) {
					recordsAC=ResolverDb2.lookupByAtomContainer(ac);
				}

				if (recordsAC.size()>0) rec.name=recordsAC.get(0).name;
			}
			
			DSSToxRecord.assignFromDSSToxRecord(ac, rec);
			
		} else if (recordsAC!=null && recordsAC.size()>0)
			DSSToxRecord.assignDSSToxInfoFromFirstRecord(ac, recordsAC);
		else 
			ResolverDb2.assignRecordByStructureNotInDB(ac);
		
	
		
	}
	
	public static void main(String[] args) {
		TaskStructureSearch s=new TaskStructureSearch();
		s.filepath="L:/Priv/Cin/NRMRL/CompTox/TDS/DIPPR Update 2013/WARDATA have dippr sort.sdf";
		s.LoadFromSDF(s.filepath);
	}
	
	
//	public static boolean isCASValid(String cas)  {
//
//		try {
//        cas = cas.replaceAll("-","");
//        // Although the definition is usually expressed as a 
//        // right-to-left fn, this works left-to-right.
//        // Note the loop stops one character shy of the end.
//        int sum = 0;
//        for (int indx=0; indx < cas.length()-1; indx++) {
//            sum += (cas.length()-indx-1)*Integer.parseInt(cas.substring(indx,indx+1));
//        }
//        // Check digit is the last char, compare to sum mod 10.
//        return Integer.parseInt(cas.substring(cas.length()-1)) == (sum % 10);
//		} catch (Exception ex) {
//			return false;
//		}
//	}
	

//	/**
//	 * Loads atomcontainer from either a file or a string with carriage returns
//	 * 
//	 * @param listCAS
//	 * @return
//	 */
//	public static AtomContainerSet LoadFromCASList(String listCAS) {
//		AtomContainerSet moleculeSet = new AtomContainerSet();		
//		
//		
//		try {
//			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
//			
//			BufferedReader br = new BufferedReader(
//					listCAS.indexOf('\n') >= 0 ? new StringReader(listCAS) : new FileReader(listCAS));
//			
//			int count=0;
//			
//			while (true) {
//				String Line = br.readLine();
//
//				if (Line == null)
//					break;
//				
//				Line=Line.trim();
//				
//				if (Line.isEmpty()) continue;
//				
//				String CAS=Line;
//				
////				System.out.println("*"+CAS+"*");
//											
//				count++;
//				
//				setMessage("Loading molecule "+count);
//
//				AtomContainer m=null;
//				
//				
//				if (!ResolverDb2.isCAS(CAS)) {
//					m = new AtomContainer();
//					m.setProperty("CAS", CAS);
//					m.setProperty("Error", "CAS number: "+CAS+" is invalid");
//					m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
//										
//					DSSToxRecord rec=new DSSToxRecord();
//					rec.cas=CAS;					
//					ResolverDb2.logit("Invalid CAS", CAS, rec);
//					DSSToxRecord.assignFromDSSToxRecord(m,rec);
//
//					m.setProperty("Query", CAS);
//					moleculeSet.addAtomContainer(m);
//					continue;
//				}
//								
//				ArrayList<DSSToxRecord> records=ResolverDb2.lookupByCAS(CAS);
//					
////				System.out.println(records.size());
//				
//				if (records.size()>0 ) {					
//					ResolverDb2.logit("CAS", CAS, records.get(0));
//					m=getMoleculeFromDSSToxRecords(records);
//
//				} else {
//					m = new AtomContainer();
//					m.setProperty("CAS", CAS);
//					m.setProperty("Error", "CAS number not found");
//					m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
//				
//					DSSToxRecord rec=new DSSToxRecord();
//					rec.cas=CAS;					
//					ResolverDb2.logit("CAS not found", CAS, rec);
//					DSSToxRecord.assignFromDSSToxRecord(m,rec);
//					
//					ResolverDb2.logit("CAS not found", CAS, rec);		
//				}
//				
////				AtomContainer m = WebTEST4.loadSMILES(Smiles);
////				m = (AtomContainer) sdg.getMolecule();
//
//				m.setProperty("Query", CAS);//store CAS number as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//				
//			}
////			System.out.println(moleculeSet.getAtomContainerCount());
//			
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//		
//	}
//	
//	/**
//	 * Loads atomcontainer from either a file or a string with carriage returns
//	 * 
//	 * @param listCAS
//	 * @return
//	 */
//	public static AtomContainerSet LoadFromCASList(String listCAS) {
//		AtomContainerSet moleculeSet = new AtomContainerSet();		
//		
//		
//		try {
//			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
//			
//			BufferedReader br = new BufferedReader(
//					listCAS.indexOf('\n') >= 0 ? new StringReader(listCAS) : new FileReader(listCAS));
//			
//			int count=0;
//			
//			while (true) {
//				String Line = br.readLine();
//
//				if (Line == null)
//					break;
//				
//				Line=Line.trim();
//				
//				if (Line.isEmpty()) continue;
//				
//				String CAS=Line;
//				
////				System.out.println("*"+CAS+"*");
//							
//				
//				count++;
//				
//				setMessage("Loading molecule "+count);
//
//				AtomContainer m=null;
//				
//				if (!isCASValid(CAS)) {
//					m = new AtomContainer();
//					m.setProperty("Error", "CAS number: "+CAS+" is invalid");
//					m.setProperty("CAS", CAS);
//					m.setProperty("Query", CAS);
//					moleculeSet.addAtomContainer(m);
//					continue;
//				}
//								
//				ArrayList<DSSToxRecord> records=ResolverDb.lookupByCAS(CAS);
//											
//				if (records.size()>0 ) {
//					DSSToxRecord rec=records.get(0);
//										
//					if (rec.inchi==null) {
//						m = new AtomContainer();
//						m.setProperty("Error", "Inchi missing in database");
//						m.setProperty("CAS", CAS);
//						m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
//						
//					} else {
//						InChIToStructure gen = factory.getInChIToStructure(rec.inchi, DefaultChemObjectBuilder.getInstance());
//						m=(AtomContainer)gen.getAtomContainer();
//						
//						m=CleanUpMolecule(m);
//						m.setProperty("Error", "");
//						m.setProperty("Source","CAS in database");
//						WebTEST4.checkAtomContainer(m);
//						ResolverDb.assignDSSToxInfoFromFirstRecord(m, records);
//						logger.info("Record from CAS in database: "+rec);
//					}					
//					
////					System.out.println(CAS+"\t"+rec.gsid+"\t"+m.getAtomCount());
//				} else {
//					m = new AtomContainer();
//					m.setProperty("Error", "CAS number not found");
//					m.setProperty("CAS", CAS);
//					m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
//				}
//				
////				AtomContainer m = WebTEST4.loadSMILES(Smiles);
////				m = (AtomContainer) sdg.getMolecule();
//
//				m.setProperty("Query", CAS);//store CAS number as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//				
//			}
////			System.out.println(moleculeSet.getAtomContainerCount());
//			
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//		
//	}
	
//	/**
//	 * Loads atomcontainer from either a file or a string with carriage returns
//	 * 
//	 * @param listName
//	 * @return
//	 */
//	private static AtomContainerSet LoadFromNameList(String listName) {
//		AtomContainerSet moleculeSet = new AtomContainerSet();		
////		System.out.println("enter load from name list");
//		
//		try {
//			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
//			
//			BufferedReader br = new BufferedReader(
//					listName.indexOf('\n') >= 0 ? new StringReader(listName) : new FileReader(listName));
//			
//			int count=0;
//			
//			while (true) {
//				String Line = br.readLine();
//
//				if (Line == null)
//					break;
//				
//				Line=Line.trim();
//				
//				if (Line.isEmpty()) continue;
//				
//				String Name=Line.replace("_", " ").trim();
//				
//				count++;
//				
//				setMessage("Loading molecule "+count);
//
//				AtomContainer m=null;
//				
//				//1. Look up by name in database:
//				ArrayList<DSSToxRecord> records=ResolverDb.lookupByName(Name);
//
//				if (records.size()>0 && records.get(0).inchi!=null) {
//					m=getMoleculeFromDSSToxRecords(records, factory);
//					logger.info("Record from name in database: "+records.get(0));
//					m.setProperty("Source","Name");
//					
//					//					System.out.println(CAS+"\t"+rec.gsid+"\t"+m.getAtomCount());
//				} else {					
//
//					long t1=System.currentTimeMillis();
//										
//					//2. Search actor by name:														
//					DSSToxRecord d=MoleculeUtilities.getDSSToxRecordFromDashboard(Name.toLowerCase());
//					long t3=System.currentTimeMillis();
//					
////					System.out.println((t3-t2)+"\ttime for actor");
//					
//											
//					if (d!=null) {
////						System.out.println("smiles="+d.smiles+"");
//						if (d.smiles.isEmpty()) {
//							m = new AtomContainer();
//							m.setProperty("Error", "Smiles unavailable: "+Name);
//							logger.info("Actor record found but no smiles available: "+d);
//							
//							m.setProperty("CAS", d.cas);
//															
//						} else {
//							
//							try {
//								m = WebTEST4.prepareSmilesMolecule(d.smiles);
//								m = CleanUpMolecule(m);
//								m.setProperty("Error", "");					
//								WebTEST4.checkAtomContainer(m);
//								DSSToxRecord.assignFromDSSToxRecord(m, d);
//								logger.info("Actor record found: "+d);
//								m.setProperty("Source","Actor record from name");
//								
//							} catch (Exception ex) {
//								System.out.println("Error cleaning up molecule for smiles from actor="+d.smiles);
//							}
//							
//						}
//						
//					} else {
//						//Use OPSIN name to structure:						
//						OpsinResult or=NameToStructureOpsin.nameToSmiles(Name);
//						String smiles=or.getSmiles();
//						List<OpsinWarning>warnings=or.getWarnings();
//
//						//TODO implement OpsinWarning
//						
////						for (OpsinWarning warning:warnings) {
////							System.out.println(warning.getMessage());
////						}
//						
//						if (or.getSmiles()!=null) {
//
//							ArrayList<DSSToxRecord>recs=ResolverDb.lookupBySMILES(smiles);
//
//							
//							if (recs.size()>0) {
//								//3. Opsin name to structure match in database:
//								m=getMoleculeFromDSSToxRecords(recs, factory);
//								long t2=System.currentTimeMillis();
//								logger.info("Record from name to structure: "+recs.get(0));
//								m.setProperty("Source","Name to structure");							
////								System.out.println((t2-t1)+"\t"+recs.size());													
//							} else {
//								//4. Opsin name to structure but no match in database:
//								m=(AtomContainer)LoadFromSmilesList(smiles+"\n").getAtomContainer(0);
//								String errorCode=m.getProperty("ErrorCode");						
////								System.out.println("errorCode="+errorCode);	
//								m.setProperty(DSSToxRecord.strName, Name);
//								
//								if (errorCode==WebTEST.ERROR_CODE_STRUCTURE_ERROR) {
//									m = new AtomContainer();
//									m.setProperty("Error", "Name: "+Name+" not found");
//									m.setProperty("CAS", "C_"+System.currentTimeMillis());
//								} else logger.info("Structure from name to structure");
//																
//							}
//														
//						} else {//cant generate smiles from name to structure						
//							m = new AtomContainer();
//							m.setProperty("Error", "Name: "+Name+" not found");
//							m.setProperty("CAS", "C_"+System.currentTimeMillis());														
//						}
//						
//					}
//																						
//				}
//								
//				m.setProperty("Query", Name);//store name as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//				
//			}
////			System.out.println(moleculeSet.getAtomContainerCount());
//			
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//		
//	}
	
//	private static AtomContainerSet LoadFromAnyList(String lines) {
//		AtomContainerSet moleculeSet = new AtomContainerSet();		
//		//		System.out.println("enter load from name list");
//
//		try {
////			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
//
//			BufferedReader br = new BufferedReader(
//					lines.indexOf('\n') >= 0 ? new StringReader(lines) : new FileReader(lines));
//
//			int count=0;
//
//			while (true) {
//				String Line = br.readLine();
//				if (Line == null)
//					break;
//				Line=Line.trim();
//				if (Line.isEmpty()) continue;
//
//				Line=Line.replace("_", " ").trim();
//				count++;
//
//				setMessage("Loading molecule "+count);
//
//				AtomContainer m=null;
//
//				//
//				ArrayList<DSSToxRecord> records=ResolverDb2.lookup(Line);
//
//				if (records.size()>0) {
//					m=getMoleculeFromDSSToxRecords(records);
//										
//					if (records.get(0).inchi==null) 
//						m.setProperty("Error", "Invalid structure");
//					
//				} else {					
//					m = new AtomContainer();									 
//					m.setProperty("Error", "Identifier: "+Line+" not found");										
//					m.setProperty("CAS", "C_"+System.currentTimeMillis());
//										
//					DSSToxRecord r=new DSSToxRecord();
//					r.cas="C_"+System.currentTimeMillis();				
//					r.name=Line;						
//					DSSToxRecord.assignFromDSSToxRecord(m,r);					
//
//					ResolverDb2.logit("Identifier not found", Line, r);
//				}
//
//				m.setProperty("Query", Line);//store name as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//
//			}
//			//			System.out.println(moleculeSet.getAtomContainerCount());
//
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//		
//		
//	}

	
//	/**
//	 * Loads atomcontainer from either a file or a string with carriage returns
//	 * 
//	 * @param listName
//	 * @return
//	 */
//	private static AtomContainerSet LoadFromNameList(String listName) {
//		AtomContainerSet moleculeSet = new AtomContainerSet();		
//		//		System.out.println("enter load from name list");
//
//		try {
////			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
//
//			BufferedReader br = new BufferedReader(
//					listName.indexOf('\n') >= 0 ? new StringReader(listName) : new FileReader(listName));
//
//			int count=0;
//
//			while (true) {
//				String Line = br.readLine();
//				if (Line == null)
//					break;
//				Line=Line.trim();
//				if (Line.isEmpty()) continue;
//
//				String Name=Line.replace("_", " ").trim();
//				count++;
//
//				setMessage("Loading molecule "+count);
//
//				AtomContainer m=null;
//
//				//Look up by name:
//				ArrayList<DSSToxRecord> records=ResolverDb2.lookupByNameAdvanced(Name);
//
//				if (records.size()>0) {
//					m=getMoleculeFromDSSToxRecords(records);
//					m.setProperty("Source","Name");
//					
//					if (records.get(0).inchi==null) 
//						m.setProperty("Error", "Invalid structure");
//					
//				} else {					
//					m = new AtomContainer();									 
//					m.setProperty("Error", "Name: "+Name+" not found");										
//					m.setProperty("CAS", "C_"+System.currentTimeMillis());
//										
//					DSSToxRecord r=new DSSToxRecord();
//					r.cas="C_"+System.currentTimeMillis();				
//					r.name=Name;						
//					DSSToxRecord.assignFromDSSToxRecord(m,r);					
//
//					ResolverDb2.logit("Name not found", Name, r);
//				}
//
//				m.setProperty("Query", Name);//store name as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//
//			}
//			//			System.out.println(moleculeSet.getAtomContainerCount());
//
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//
//	}
	
	private static AtomContainer getMoleculeFromDSSToxRecords(ArrayList<DSSToxRecord>recs,InChIGeneratorFactory factory) {
		DSSToxRecord rec=recs.get(0);
		
		try {
			InChIToStructure gen = factory.getInChIToStructure(rec.inchi, DefaultChemObjectBuilder.getInstance());
			AtomContainer m=(AtomContainer)gen.getAtomContainer();
			m=CleanUpMolecule(m);
			m.setProperty("Error", "");					
			WebTEST4.checkAtomContainer(m);
			DSSToxRecord.assignFromDSSToxRecord(m, recs.get(0));
			
			return m;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static AtomContainer getMoleculeFromDSSToxRecords(ArrayList<DSSToxRecord> recs) {
		AtomContainer molecule=new AtomContainer();

		if (recs.size()>0) {

			DSSToxRecord rec=recs.get(0);					
			
			if (rec.smiles!=null && !rec.smiles.isEmpty()) {//smiles identifier or name to structure
				molecule = WebTEST4.prepareSmilesMolecule(rec.smiles);	

			} else {
								
				if (rec.mol!=null) {//TODO should we get from inchi instead?					
//							System.out.println("using mol="+rec.mol);
					AtomContainerSet acs=MolFileUtilities.loadFromSdfString(rec.mol);
//							
					if (acs!=null && acs.getAtomContainer(0)!=null && acs.getAtomContainer(0).getAtomCount()>0) {
						molecule=(AtomContainer)acs.getAtomContainer(0);					
					} else if(rec.inchi!=null) {
						
						molecule=CDKUtilities.getAtomContainer(rec.inchi);
						
						if (molecule==null) {
							molecule=new AtomContainer();
							rec.assignFromDSSToxRecord(molecule, rec);
							molecule.setProperty("Error", "Structure can't be parsed");						
							return molecule;							
						} else {
//							logger.info("Structure obtained from inchi instead of mol:"+rec.inchi);
						}
					} 
				}
			} 

			if (molecule==null) return null; 
									
			try {
				molecule = CleanUpMolecule(molecule);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			
			molecule.setProperty("Error", "");					
			WebTEST4.checkAtomContainer(molecule);

			
			
			rec.assignFromDSSToxRecord(molecule, rec);
													
		} else {
//			System.out.println("Molecule not in dsstox");
		}
		return molecule;
	}

	public static void fixNullBondStereo(AtomContainer molecule) {
//		if (true) return;
		
		for (int i=0;i<molecule.getBondCount();i++) {
			Bond b=(Bond)molecule.getBond(i);
			if (b.getStereo()==null) {
				b.setStereo(Stereo.NONE);//fix molecules loaded from v3000 mol files where stereo isnt set- not sure how to make JChemPaint not give error otherwise!
			}
//				System.out.println(b.getStereo());
		}
	}
	
	
	
//	public static AtomContainerSet LoadFromSmilesList(String smi) {
//
//		AtomContainerSet moleculeSet = new AtomContainerSet();
//		String delimiter = "";
//		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
//		
//
//		try {
//			BufferedReader br = new BufferedReader(
//					smi.indexOf('\n') >= 0 ? new StringReader(smi) : new FileReader(smi));
//			
//			int count=0;
//			
//			while (true) {
//				String Line = br.readLine();
//
//				if (Line == null)
//					break;
//				if (Line.trim().equals(""))
//					break;
//			
//				count++;
//				
//				setMessage("Loading molecule "+count);
//			
//				// if (delimiter.equals("")) {
//				if (Line.indexOf("\t") > -1)
//					delimiter = "\t";
////				else if (Line.indexOf(",") > -1)
////					delimiter = ",";
//				else if (Line.indexOf(" ") > -1)
//					delimiter = " ";
//				else {
//					// we will look up CAS later or assign one based on
//					// the current time and the row number
//				}
//
//				String ID = null;
//				String Smiles = null;
//
//				if (!delimiter.equals("")) {
//					List<String> l = ToxPredictor.Utilities.Utilities.Parse2(Line, delimiter);
//
//					if (l.size() >= 2) {
//						Smiles = (String) l.get(0);
//						ID = (String) l.get(1);
//
//					} else {
//						return null;
//					}
//				} else {
//					Smiles = Line;
//				}
//				
//				//TODO- first search by Actor???
//				
////				System.out.println(Smiles);
//				
////				AtomContainer m = WebTEST4.loadSMILES(Smiles);
//								
//				Inchi inchi = IndigoUtilities.toInchiIndigo(Smiles);							
//				ArrayList<DSSToxRecord>recs = ResolverDb2.lookupByInChis(inchi);			
//				
////				System.out.println(Smiles+"\t"+inchi.inchiKey);
//				
//				if (recs.size() > 0) {
//					ResolverDb2.logit("Smiles",Smiles,recs.get(0));
//					recs.get(0).smiles=Smiles;//store smiles since structures with unspecified isomer messes up cdk drawing for now
//				} else if (inchi!=null) {
//					DSSToxRecord r=new DSSToxRecord();
//					r.cas="C_"+inchi.inchiKey;				
//					r.smiles=Smiles;
//					recs.add(r);
//					ResolverDb2.logit("Smiles but no db match",Smiles,r);									
//				} 
//				
//				AtomContainer m=null;
//				
//				if (recs.size()>0) {
//					m=getMoleculeFromDSSToxRecords(recs);
//					
//					if (m.getAtomCount()==0) {
//						m.setProperty("Error", "Invalid smiles:"+Smiles);
//						DSSToxRecord rec=new DSSToxRecord();
//						rec.cas="C_"+System.currentTimeMillis();
//						DSSToxRecord.assignFromDSSToxRecord(m, rec);
//					}
//					
//				} else {
//					DSSToxRecord r=new DSSToxRecord();
//					r.cas="C_"+System.currentTimeMillis();									
//					r.smiles=Smiles;					
//					
//					m=new AtomContainer();
//					m.setProperty("Error", "Invalid smiles:"+Smiles);	
//					DSSToxRecord.assignFromDSSToxRecord(m,r);					
//					ResolverDb2.logit("Invalid smiles",Smiles,r);	
//				}
//								
//								
//				if ( !StringUtils.isEmpty(ID) ) {
//					ID = cleanString(ID);
//					m.setProperty("CAS", ID);
//				} 
//
//				m.setProperty("Query", Smiles);//store smiles as the string used in the query for export later so users can match up results
//				moleculeSet.addAtomContainer(m);
//				
//			}
//
//			br.close();
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return moleculeSet;
//	}

	public static AtomContainerSet LoadFromList(String lines,int type) {
		
		try {				
			if (lines.contains("\n")) {
				return loadFromLineDelimitedString(lines,type);
			} else {
				return loadFromTextFile(lines,type);
			}
					
			
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}
	
	private static AtomContainerSet loadFromTextFile(String filepath, int type) {
		AtomContainerSet acs = new AtomContainerSet();
		
		try {

			int count=0;

			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
			
				String Line=br.readLine();

				if (Line == null || Line.trim().equals(""))
					break;

				count++;
				
				if(done) 
					break;
				
				setMessage("Loading molecule "+count);				
				addMoleculeFromLine(Line, type, acs);
				
			}
			br.close();
			

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return acs;
	}

	private static AtomContainerSet loadFromLineDelimitedString(String lines,int type) {
		AtomContainerSet acs = new AtomContainerSet();
		
		try {			
			
			
			LinkedList<String>lineArray=ToxPredictor.Utilities.Utilities.Parse(lines, "\n");
			int count=0;

			for (String Line:lineArray)  {
				if (Line == null || Line.trim().equals(""))
					continue;
				
				if (done)
					break;
				
				count++;				
				setMessage("Loading molecule "+count);				
				addMoleculeFromLine(Line, type, acs);
				
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return acs;
		
	}

	


	private static void addMoleculeFromLine(String Line, int type,AtomContainerSet acs) {
		String ID = null;
		String identifier = null;

		if (Line.contains("\t")) {
			String [] vals=Line.split("\t");
			identifier=vals[0];
			ID=vals[1];					
			identifier = trimQuotes(identifier);
			ID=trimQuotes(ID);					
		} else {
			identifier = trimQuotes(Line);										
		}

//		System.out.println("Line="+Line);
//		System.out.println(identifier+"\t"+ID);

		AtomContainer m=null;

		if (type==TypeSmiles) {
			m=lookupBySmiles(identifier);
		} else if (type==TypeCAS) {
			m=lookupByCAS(identifier);
		} else if(type==TypeName) {
			m=lookupByName(identifier);
		} else if (type==TypeAny) {
			m=lookupByAny(identifier);
		}

		if ( !StringUtils.isEmpty(ID) ) {
			ID = cleanString(ID);
			m.setProperty("ID", ID);
			
			if(ResolverDb2.isCASValid(ID)) {
				m.setProperty("CAS", ID);
			}
			
		} 

		m.setProperty("Query", identifier);//store smiles as the string used in the query for export later so users can match up results
		acs.addAtomContainer(m);
	}
private static String trimQuotes(String identifier) {
	if (identifier.substring(0,1).equals("\"")) 
		identifier=identifier.substring(1,identifier.length());

	if (identifier.substring(identifier.length()-1,identifier.length()).equals("\"")) 
		identifier=identifier.substring(0,identifier.length()-1);
	return identifier;
}
	
	private static AtomContainer lookupByAny(String Line) {
		AtomContainer m=null;

		//
//		System.out.println("Line="+Line);
		ArrayList<DSSToxRecord> records=ResolverDb2.lookup(Line);

		if (records.size()>0) {
			m=getMoleculeFromDSSToxRecords(records);
								
			if (records.get(0).inchi==null) 
				m.setProperty("Error", "Invalid structure");
			
		} else {					
			m = new AtomContainer();									 
			m.setProperty("Error", "Identifier: "+Line+" not found");										
			m.setProperty("CAS", "C_"+System.currentTimeMillis());
								
			DSSToxRecord r=new DSSToxRecord();
			r.cas="C_"+System.currentTimeMillis();				
			r.name=Line;						
			DSSToxRecord.assignFromDSSToxRecord(m,r);					

			ResolverDb2.logit("Identifier not found", Line, r);
		}
		return m;
		
	}

	private static AtomContainer lookupByName(String Name) {
		//Look up by name:
		ArrayList<DSSToxRecord> records=ResolverDb2.lookup(Name,ToxPredictor.Database.ChemIdType.Name);

		AtomContainer m=null;
		
		if (records.size()>0) {
			m=getMoleculeFromDSSToxRecords(records);
			m.setProperty("Source","Name");
			
			if (records.get(0).inchi==null) 
				m.setProperty("Error", "Invalid structure");
			
		} else {					
			m = new AtomContainer();									 
			m.setProperty("Error", "Name: "+Name+" not found");										
			m.setProperty("CAS", "C_"+System.currentTimeMillis());
								
			DSSToxRecord r=new DSSToxRecord();
			r.cas="C_"+System.currentTimeMillis();				
			r.name=Name;						
			DSSToxRecord.assignFromDSSToxRecord(m,r);					

			ResolverDb2.logit("Name not found", Name, r);
		}
		return m;
		
	}

	public static AtomContainer lookupByCAS(String CAS) {

		AtomContainer m=null;
		
//		System.out.println(CAS+"\t"+ResolverDb2.isCAS(CAS));
		
		if (!ResolverDb2.isCAS(CAS)) {
			m = new AtomContainer();
			m.setProperty("CAS", CAS);
			m.setProperty("Error", "CAS number: "+CAS+" is invalid");
			m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
								
			DSSToxRecord rec=new DSSToxRecord();
			rec.cas=CAS;					
			ResolverDb2.logit("Invalid CAS", CAS, rec);
			DSSToxRecord.assignFromDSSToxRecord(m,rec);			
			return m;			
		}
						
		ArrayList<DSSToxRecord> records=ResolverDb2.lookupByCAS(CAS);
			
//		System.out.println(records.size());
		
		if (records.size()>0 ) {					
			ResolverDb2.logit("CAS", CAS, records.get(0));
			m=getMoleculeFromDSSToxRecords(records);

		} else {
			m = new AtomContainer();
			m.setProperty("CAS", CAS);
			m.setProperty("Error", "CAS number not found");
			m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
		
			DSSToxRecord rec=new DSSToxRecord();
			rec.cas=CAS;					
			ResolverDb2.logit("CAS not found", CAS, rec);
			DSSToxRecord.assignFromDSSToxRecord(m,rec);
			
			ResolverDb2.logit("CAS not found", CAS, rec);		
		}
		
		return m;
	}

	static AtomContainer lookupBySmiles(String Smiles) {
		Inchi inchi = IndigoUtilities.toInchiIndigo(Smiles);							
		ArrayList<DSSToxRecord>recs = ResolverDb2.lookupByInChis(inchi);			
		
//		System.out.println(Smiles+"\t"+inchi.inchiKey);
		
		if (recs.size() > 0) {
			ResolverDb2.logit("Smiles",Smiles,recs.get(0));
			recs.get(0).smiles=Smiles;//store smiles since structures with unspecified isomer messes up cdk drawing for now
		} else if (inchi!=null) {
			DSSToxRecord r=new DSSToxRecord();
			r.cas="C_"+inchi.inchiKey;				
			r.smiles=Smiles;
			recs.add(r);
			ResolverDb2.logit("Smiles but no db match",Smiles,r);									
		} 
		
		AtomContainer m=null;
		
		if (recs.size()>0) {
			m=getMoleculeFromDSSToxRecords(recs);
			
			if (m.getAtomCount()==0) {
				m.setProperty("Error", "Invalid smiles:"+Smiles);
				DSSToxRecord rec=new DSSToxRecord();
				rec.cas="C_"+System.currentTimeMillis();
				DSSToxRecord.assignFromDSSToxRecord(m, rec);
			}
			
		} else {
			DSSToxRecord r=new DSSToxRecord();
			r.cas="C_"+System.currentTimeMillis();									
			r.smiles=Smiles;					
			
			m=new AtomContainer();
			m.setProperty("Error", "Invalid smiles:"+Smiles);	
			DSSToxRecord.assignFromDSSToxRecord(m,r);					
			ResolverDb2.logit("Invalid smiles",Smiles,r);	
		}
		
		return m;
	}


	public static AtomContainer CleanUpMolecule(AtomContainer m) throws CDKException {
		
		DescriptorFactory df=new DescriptorFactory(false);
		df.Normalize(m);

//		System.out.println("here1 inchiKey="+Inchi.generateInChiKeyIndigo(m).inchiKey);

		fixNullBondStereo(m);
		
		//TODO- SDG- messes up inchikey so that when edit chemical from batch list it wants to change the CAS when click ok!
		//If dont use SDG it causes error when loading molecule from smiles...
		
		boolean haveAllCoords=true;
		for (int i=0;i<m.getAtomCount();i++) {	
			if(m.getAtom(i).getPoint2d()==null) {
				haveAllCoords=false;
			}
		}
		
		if (!haveAllCoords) {
//			System.out.println("gen coords");
			sdg.setMolecule(m);
			sdg.generateCoordinates();
			m = (AtomContainer) sdg.getMolecule();
		}
		
		
//		System.out.println("here2 inchiKey="+Inchi.generateInChiKeyIndigo(m).inchiKey);


		
		return m;
	}
	
	private static String cleanString(String ID) {
		ID = ID.replace("\"", "_");
		ID = ID.replace("/", "_");
		ID = ID.replace(":", "_");
		ID = ID.replace("*", "_");
		ID = ID.replace("?", "_");
		ID = ID.replace("<", "_");
		ID = ID.replace(">", "_");
		ID = ID.replace("|", "_");
		return ID;
	}
	
	@Deprecated
	private AtomContainerSet OldLoadFromSmilesList() {
		
		AtomContainerSet moleculeSet=new AtomContainerSet();
		String delimiter="";
		
		SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		sp.kekulise(false);
				
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();

		 
		int count=this.GetSmilesCount();
		
    	try {
    		
    		BufferedReader br=new BufferedReader(new FileReader(filepath));
    		int counter=0;
    		while (true) {

				if (done)
					break;

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
						
//						System.out.println(Smiles+"\t"+ID);
						
					} else if (l.size()==1) {
						Smiles = (String) l.get(0);
						long time=System.currentTimeMillis();
						ID="C"+counter+"_"+time; //generates unique ID so that output files can be stored in unique folders (i.e. dont get overwritten each time a new smiles file is ran)
					} else {
						System.out.println("error:"+Line);
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
				setMessage(message);

				
				
				AtomContainer m = null;
				
//				if (Smiles.indexOf(".")>-1) {
//					m = new Molecule();
//					m.setProperty("Error", "Molecules can only contain one fragment");
//					m.setProperty("CAS", ID);
//					moleculeSet.addMolecule(m);
//					continue;
//				}

				try {

					// m=sp.parseSmiles(Smiles);
					
					Smiles = CDKUtilities.FixSmiles(Smiles);//TODO
					
					
					
					m = (AtomContainer)sp.parseSmiles(Smiles);
					
					
					m = CleanUpMolecule(m);
					
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
				
				df.Normalize(m);

				m.setProperty("CAS", ID);
				moleculeSet.addAtomContainer(m);

			}
    		
    		br.close();
    		
   		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
		return moleculeSet;
	}

	/**
	 * Create a molecule from smiles without looking it up in the database
	 * 
	 * @param smiles
	 * @param f
	 */
	public void runSingle(String smiles,TESTApplication f) {
		//			AtomContainerSet acs=null;
		
		Inchi inchi = Inchi.generateInChiKeyIndigo(smiles);			

		ArrayList<DSSToxRecord>recs=new ArrayList<>();
		DSSToxRecord r=new DSSToxRecord();
		r.cas="C_"+inchi.inchiKey;				
		r.smiles=smiles;
		recs.add(r);
		
		System.out.println(smiles+"\t"+inchi.inchiKey);

		AtomContainer molecule = getMoleculeFromDSSToxRecords(recs);

		//			System.out.println("here1 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);

		done = true;

		String error=molecule.getProperty("Error");
		if (error.contentEquals("Multiple molecules")) {
			JOptionPane.showMessageDialog(f,"Molecule consists of multiple structural fragments, please select another compound");
			return;
		}

		molecule=f.configureModel(molecule);

		//			System.out.println("here2 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);

		//			StereoElementFactory stereo    = StereoElementFactory.using2DCoordinates(molecule);
		//			molecule.setStereoElements(stereo.createAll());


		if (molecule.getAtomCount()==0) {
			JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
			f.panelSingleStructureDatabaseSearch.jtfCAS.setText("");
			f.panelSingleStructureDatabaseSearch.jtfName.setText("");
		} else {
			//				setCAS(molecule);

			f.panelSingleStructureDatabaseSearch.jtfCAS.setText(molecule.getProperty(DSSToxRecord.strCAS));

			if (molecule.getProperty(DSSToxRecord.strName)!=null) {
				f.panelSingleStructureDatabaseSearch.jtfName.setText(molecule.getProperty(DSSToxRecord.strName));
			} else {
				f.panelSingleStructureDatabaseSearch.jtfName.setText("");
			}

		}

		String Query=filepath.replace("\n", "");
		molecule.setProperty("Query", Query);
		//			f.setNameCAS(molecule);

		//			System.out.println("here3 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);

		f.setTitle(TESTConstants.SoftwareTitle);			
		f.panelSingleStructureDatabaseSearch.jtfIdentifier.requestFocus();

	}


	/**
	 * The actual long running task. This runs in a SwingWorker thread.
	 */
	public class ActualTask {
		


		ActualTask() {

			current = 0;
			done=false;
			
//			((TESTApplication) gui).setCursor(Utilities.waitCursor);
			
			if (runType==TESTConstants.typeTaskBatch) {
				runBatch();
			} else {
				runSingle();
			}
//			((TESTApplication) gui).setCursor(Utilities.defaultCursor);
			
			
		} // end ActualTaskConstructor

		/**
		 * It's potentially a cas if only has numbers after removing dashes
		 * @param text
		 * @return
		 */
//		boolean isCAS(String text) {
//			String text2=text.replace("-", "").replace("\n", "");
//			if (text2.length()<5) return false;
//			if (!isCASValid(text2)) return false;
//			return true;
//		}
//		
//		boolean isAllNumbers(String text) {		
//			String text2=text.replace("-", "").replace("\n", "");
//			if(text2.matches("[0-9]+")) return true;
//			else return false;
//		}
		
//		AtomContainer getFromCAS(String search,TESTApplication f) {
//			
//			if (!isAllNumbers(search)) return null;
//
//			if (!isCAS(search)) {				
//				JOptionPane.showMessageDialog(f, "Text contains only numbers but is invalid CAS");
//				done=true;
//				return null;
//			}
//			
//			String CAS=parseSearchCAS(search.replace("\n", ""));
//			//						System.out.println("*"+CAS+"*");
//			return (AtomContainer)LoadFromCASList(CAS+"\n").getAtomContainer(0);
//			
//		}
		
//		private void runSingle() {
//			//			AtomContainerSet acs=null;
//			TESTApplication f=((TESTApplication) gui);
//			AtomContainer molecule=null;
//
//			//Try to get from CAS:
//			molecule=getFromCAS(filepath, f);
//						
//			if (molecule==null) {				
//				//Try to get from name:				
//				molecule=(AtomContainer)LoadFromNameList(filepath).getAtomContainer(0);			
//				String err=molecule.getProperty("Error");				
//				
//				if (err.contains("Name:") || err.contains("Smiles unavailable")) molecule=null;
//														
//				if(molecule==null) {
//					if (filepath.contains("'")) {//TODO add more invalid chars for smiles
//						molecule=null;					
//					} else {
//						//try to get from smiles:										
//						molecule=(AtomContainer)LoadFromSmilesList(filepath).getAtomContainer(0);
//						String errorCode=molecule.getProperty("ErrorCode");						
////						System.out.println("errorCode="+errorCode);						
//						if (errorCode==WebTEST.ERROR_CODE_STRUCTURE_ERROR) molecule=null;
//						
//
//					}					
//				}
//				
//								
//			}
//			
////			System.out.println("Source="+molecule.getProperty("Source"));
//					
//			if (molecule==null) {
//				JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
//				return;
//			}
//																							
//			done = true;
//
//			WebTEST4.checkAtomContainer(molecule);
//			
//			String error=molecule.getProperty("Error");
//			if (error.contentEquals("Multiple molecules")) {
//				JOptionPane.showMessageDialog(f,"Molecule consists of multiple structural fragments, please select another compound");
//				return;
//			}
//
//			molecule=f.configureModel(molecule);
//
//			if (molecule.getAtomCount()==0) {
//				JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
//				f.panelSingleStructureDatabaseSearch.jtfCAS.setText("");
//				f.panelSingleStructureDatabaseSearch.jtfName.setText("");
//			} else {
////				setCAS(molecule);
//				
//				
//				f.panelSingleStructureDatabaseSearch.jtfCAS.setText(molecule.getProperty(DSSToxRecord.strCAS));
//
//				if (molecule.getProperty(DSSToxRecord.strName)!=null) {
//					f.panelSingleStructureDatabaseSearch.jtfName.setText(molecule.getProperty(DSSToxRecord.strName));
//				} else {
//					f.panelSingleStructureDatabaseSearch.jtfName.setText("");
//				}
//
//			}
//
//			String Query=filepath.replace("\n", "");
//			molecule.setProperty("Query", Query);
//			//			f.setNameCAS(molecule);
//
//			f.setTitle(TESTConstants.SoftwareTitle);			
//			f.panelSingleStructureDatabaseSearch.jtfIdentifier.requestFocus();
//
//		}
		
		
		private void runSingle() {
			//			AtomContainerSet acs=null;
			TESTApplication f=((TESTApplication) gui);
			
//			System.out.println(filepath);
						
			ArrayList<DSSToxRecord>recs=ResolverDb2.lookup(filepath.replace("\n", "").trim());
			
			if (recs.size()==0) {
				JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
				return;
			}
			
			AtomContainer molecule = getMoleculeFromDSSToxRecords(recs);
			
//			System.out.println("here1 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);
																																		
			done = true;
			
			String error=molecule.getProperty("Error");
			if (error.contentEquals("Multiple molecules")) {
				JOptionPane.showMessageDialog(f,"Molecule consists of multiple structural fragments, please select another compound");
				return;
			}

			molecule=f.configureModel(molecule);
			
//			System.out.println("here2 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);
			
//			StereoElementFactory stereo    = StereoElementFactory.using2DCoordinates(molecule);
//			molecule.setStereoElements(stereo.createAll());


			if (molecule.getAtomCount()==0) {
				JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
				f.panelSingleStructureDatabaseSearch.jtfCAS.setText("");
				f.panelSingleStructureDatabaseSearch.jtfName.setText("");
			} else {
//				setCAS(molecule);
				
				f.panelSingleStructureDatabaseSearch.jtfCAS.setText(molecule.getProperty(DSSToxRecord.strCAS));

				if (molecule.getProperty(DSSToxRecord.strName)!=null) {
					f.panelSingleStructureDatabaseSearch.jtfName.setText(molecule.getProperty(DSSToxRecord.strName));
				} else {
					f.panelSingleStructureDatabaseSearch.jtfName.setText("");
				}

			}

			String Query=filepath.replace("\n", "");
			molecule.setProperty("Query", Query);
			//			f.setNameCAS(molecule);

//			System.out.println("here3 inchiKey="+Inchi.generateInChiKeyIndigo(molecule).inchiKey);
			
			f.setTitle(TESTConstants.SoftwareTitle);			
			f.panelSingleStructureDatabaseSearch.jtfIdentifier.requestFocus();

		}



		private void runBatch() {
			AtomContainerSet moleculeSet=null;

			if (structureType==TypeSDF) moleculeSet=LoadFromSDF(filepath);			
			else if (structureType==TypeSDF_In_Jar) moleculeSet=LoadFromSDFInJar();
			else moleculeSet=LoadFromList(filepath,structureType);
			
						
//			if (moleculeSet!=null) 
//				MolFileUtilities.FixDuplicateCASNumbersInSDF(moleculeSet);

			
//			System.out.println(moleculeSet==null);
			
			if (moleculeSet==null) return;

//			if (getMessage().equals("canceled")) return;
			
//			System.out.println(moleculeSet.getAtomContainerCount());
			
			boolean HaveError=false;
			
			
			for (int i=0;i<moleculeSet.getAtomContainerCount();i++) {
				AtomContainer m=(AtomContainer)moleculeSet.getAtomContainer(i);
				if (m.getProperty("Error")!=null && !m.getProperty("Error").equals("")) {
					HaveError=true;
					break;
				}
			}
//			System.out.println("here0");
			
			if (gui instanceof TESTApplication) {
				TESTApplication f=((TESTApplication) gui);
				f.panelBatch.addMoleculesToTable(moleculeSet);
			} else {
				//applet TODO
			}
			
			done = true;

				
		}
		

		public void setMessage(String message) {
			statMessage = message;
		}
	}

}
