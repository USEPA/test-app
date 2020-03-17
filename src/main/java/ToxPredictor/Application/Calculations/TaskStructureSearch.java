package ToxPredictor.Application.Calculations;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;

import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import AADashboard.Application.MoleculeUtilities;
import ToxPredictor.misc.MolFileUtilities;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.GUI.Miscellaneous.SwingWorker;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Utilities;

public class TaskStructureSearch {

	private static final Logger logger = LogManager.getLogger(TaskStructureSearch.class);
	
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

	int structureType;//smiles,CAS,or name
	int runType;//single chemical or batch
	
//	ChemicalFinder cf;
	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();

	DescriptorFactory df=new DescriptorFactory(false);
	static StructureDiagramGenerator sdg = new StructureDiagramGenerator();
	
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

		setCAS(molecule);
		
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

//					System.out.println(m.getAtomCount());
				
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				if (m==null || m.getAtomCount()==0) break;
				
    			String message="Loading "+m.getProperty("CAS")+", " +counter;
//    			System.out.println(message);
    			setMessage(message);

				
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
	
	public static void setCAS(AtomContainer m) {
		String CASfield=MolFileUtilities.getCASField(m);
		
		String CAS=null;
		
		if (!CASfield.equals("")) {
			CAS=(String)m.getProperty(CASfield);
			if (CAS != null) {
				CAS=CAS.trim();
				CAS=CAS.replace("/", "_");
				m.setProperty("CAS", CAS);
				
				ArrayList<DSSToxRecord>recs=ResolverDb.lookupByCAS(CAS);
				if (recs.size()>0)	ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);

			}
		
		} else {
			ArrayList<DSSToxRecord>recs=ResolverDb.lookupByAtomContainer(m);
			if (recs.size()>0)	ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);
			else {
//				m.setProperty("CAS", "C_"+System.currentTimeMillis());
				assignIDFromStructure(m);
			}
		}
		m.setProperty("Query", m.getProperty("CAS"));//store ID number as the string used in the query for export later so users can match up results
//		System.out.println(m.getProperty("CAS")+"");
	}
	
	public static void main(String[] args) {
		TaskStructureSearch s=new TaskStructureSearch();
		s.filepath="L:/Priv/Cin/NRMRL/CompTox/TDS/DIPPR Update 2013/WARDATA have dippr sort.sdf";
		s.LoadFromSDF(s.filepath);
	}
	
	
	public static boolean isCASValid(String cas)  {

		try {
        cas = cas.replaceAll("-","");
        // Although the definition is usually expressed as a 
        // right-to-left fn, this works left-to-right.
        // Note the loop stops one character shy of the end.
        int sum = 0;
        for (int indx=0; indx < cas.length()-1; indx++) {
            sum += (cas.length()-indx-1)*Integer.parseInt(cas.substring(indx,indx+1));
        }
        // Check digit is the last char, compare to sum mod 10.
        return Integer.parseInt(cas.substring(cas.length()-1)) == (sum % 10);
		} catch (Exception ex) {
			return false;
		}
	}
	

	/**
	 * Loads atomcontainer from either a file or a string with carriage returns
	 * 
	 * @param listCAS
	 * @return
	 */
	public static AtomContainerSet LoadFromCASList(String listCAS) {
		AtomContainerSet moleculeSet = new AtomContainerSet();		
		
		
		try {
			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
			
			BufferedReader br = new BufferedReader(
					listCAS.indexOf('\n') >= 0 ? new StringReader(listCAS) : new FileReader(listCAS));
			
			int count=0;
			
			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;
				
				Line=Line.trim();
				
				if (Line.isEmpty()) continue;
				
				String CAS=Line;
				
//				System.out.println("*"+CAS+"*");
				
				
				
				count++;
				
				setMessage("Loading molecule "+count);

				AtomContainer m=null;
				
				if (!isCASValid(CAS)) {
					m = new AtomContainer();
					m.setProperty("Error", "CAS number: "+CAS+" is invalid");
					m.setProperty("CAS", CAS);
					m.setProperty("Query", CAS);
					moleculeSet.addAtomContainer(m);
					continue;
				}
								
				ArrayList<DSSToxRecord> records=ResolverDb.lookupByCAS(CAS);
											
				if (records.size()>0 ) {
					DSSToxRecord rec=records.get(0);
										
					if (rec.inchi==null) {
						m = new AtomContainer();
						m.setProperty("Error", "Inchi missing in database");
						m.setProperty("CAS", CAS);
						m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
						
					} else {
						InChIToStructure gen = factory.getInChIToStructure(rec.inchi, DefaultChemObjectBuilder.getInstance());
						m=(AtomContainer)gen.getAtomContainer();
						
						m=CleanUpMolecule(m);
						m.setProperty("Error", "");
						WebTEST4.checkAtomContainer(m);
						ResolverDb.assignDSSToxInfoFromFirstRecord(m, records);
					}					
					
//					System.out.println(CAS+"\t"+rec.gsid+"\t"+m.getAtomCount());
				} else {
					m = new AtomContainer();
					m.setProperty("Error", "CAS number not found");
					m.setProperty("CAS", CAS);
					m.setProperty("ErrorCode", WebTEST4.ERROR_CODE_STRUCTURE_ERROR);
				}
				
//				AtomContainer m = WebTEST4.loadSMILES(Smiles);
//				m = (AtomContainer) sdg.getMolecule();

				m.setProperty("Query", CAS);//store CAS number as the string used in the query for export later so users can match up results
				moleculeSet.addAtomContainer(m);
				
			}
//			System.out.println(moleculeSet.getAtomContainerCount());
			

			br.close();
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return moleculeSet;
		
	}
	
	/**
	 * Loads atomcontainer from either a file or a string with carriage returns
	 * 
	 * @param listName
	 * @return
	 */
	private static AtomContainerSet LoadFromNameList(String listName) {
		AtomContainerSet moleculeSet = new AtomContainerSet();		
		
		
		try {
			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();//TODO use indigo instead???
			
			BufferedReader br = new BufferedReader(
					listName.indexOf('\n') >= 0 ? new StringReader(listName) : new FileReader(listName));
			
			int count=0;
			
			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;
				
				Line=Line.trim();
				
				if (Line.isEmpty()) continue;
				
				String Name=Line.replace("_", " ").trim();
				
				count++;
				
				setMessage("Loading molecule "+count);

				AtomContainer m=null;
				
				//first search actor
				DSSToxRecord d=MoleculeUtilities.getDSSToxRecordFromDashboard(Name);
				
				if (d!=null) {
					try {
						m = WebTEST4.prepareSmilesMolecule(d.smiles);
						m = CleanUpMolecule(m);					
						DSSToxRecord.assignFromDSSToxRecord(m, d);
						System.out.println("Actor record found: "+d);
					} catch (Exception ex) {
						System.out.println("Error cleaning up molecule for smiles from actor="+d.smiles);
					}
				}
				
				if (m==null) {
					ArrayList<DSSToxRecord> records=ResolverDb.lookupByName(Name);

					if (records.size()>0) {
						DSSToxRecord rec=records.get(0);

						InChIToStructure gen = factory.getInChIToStructure(rec.inchi, DefaultChemObjectBuilder.getInstance());
						m=(AtomContainer)gen.getAtomContainer();

						m=CleanUpMolecule(m);

						m.setProperty("Error", "");					
						WebTEST4.checkAtomContainer(m);

						ResolverDb.assignDSSToxInfoFromFirstRecord(m, records);

						//					System.out.println(CAS+"\t"+rec.gsid+"\t"+m.getAtomCount());
					} else {
						m = new AtomContainer();
						m.setProperty("Error", "Name: "+Name+" not found");
						m.setProperty("CAS", "C_"+System.currentTimeMillis());
						m.setProperty("Query", Name);
						moleculeSet.addAtomContainer(m);
						continue;

					}
				}
				
				m.setProperty("Query", Name);//store name as the string used in the query for export later so users can match up results
				moleculeSet.addAtomContainer(m);
				
			}
//			System.out.println(moleculeSet.getAtomContainerCount());
			

			br.close();
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return moleculeSet;
		
	}
	
	public static void assignIDFromStructure(AtomContainer ac) {
		String[] inchi = CDKUtilities.generateInChiKey(ac);
		String inchiKey=inchi[1];
		ac.setProperty("CAS", "C_"+inchiKey);
	}
	
	
	public static AtomContainerSet LoadFromSmilesList(String smi) {

		AtomContainerSet moleculeSet = new AtomContainerSet();
		String delimiter = "";
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		

		try {
			BufferedReader br = new BufferedReader(
					smi.indexOf('\n') >= 0 ? new StringReader(smi) : new FileReader(smi));
			
			int count=0;
			
			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;
				if (Line.trim().equals(""))
					break;
			
				count++;
				
				setMessage("Loading molecule "+count);
			
				// if (delimiter.equals("")) {
				if (Line.indexOf("\t") > -1)
					delimiter = "\t";
				else if (Line.indexOf(",") > -1)
					delimiter = ",";
				else if (Line.indexOf(" ") > -1)
					delimiter = " ";
				else {
					// we will look up CAS later or assign one based on
					// the current time and the row number
				}

				String ID = null;
				String Smiles = null;

				if (!delimiter.equals("")) {
					List<String> l = ToxPredictor.Utilities.Utilities.Parse2(Line, delimiter);

					if (l.size() >= 2) {
						Smiles = (String) l.get(0);
						ID = (String) l.get(1);

					} else {
						return null;
					}
				} else {
					Smiles = Line;
				}
				
				//TODO- first search by Actor???
				
				AtomContainer m = WebTEST4.loadSMILES(Smiles);
				
				m = CleanUpMolecule(m);
				
				if ( !StringUtils.isEmpty(ID) ) {
					ID = cleanString(ID);
					m.setProperty("CAS", ID);
				} 

				m.setProperty("Query", Smiles);//store smiles as the string used in the query for export later so users can match up results
				moleculeSet.addAtomContainer(m);
				
			}

			br.close();
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return moleculeSet;
	}

	public static AtomContainer CleanUpMolecule(AtomContainer m) throws CDKException {

		DescriptorFactory df=new DescriptorFactory(false);
		df.Normalize(m);

		sdg.setMolecule(m);
		sdg.generateCoordinates();
		m = (AtomContainer) sdg.getMolecule();
		
		
		
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
	 * The actual long running task. This runs in a SwingWorker thread.
	 */
	public class ActualTask {
		ActualTask() {

			current = 0;
			done=false;
			
			((TESTApplication) gui).setCursor(Utilities.waitCursor);
			
			if (runType==TESTConstants.typeTaskBatch) {
				runBatch();
			} else {
				runSingle();
			}
			((TESTApplication) gui).setCursor(Utilities.defaultCursor);
			
			
		} // end ActualTaskConstructor

		/**
		 * It's potentially a cas if only has numbers after removing dashes
		 * @param text
		 * @return
		 */
		boolean isCAS(String text) {
			String text2=text.replace("-", "").replace("\n", "");
			if (text2.length()<5) return false;
			if (!isCASValid(text2)) return false;
			return true;
		}
		
		boolean isAllNumbers(String text) {		
			String text2=text.replace("-", "").replace("\n", "");
			if(text2.matches("[0-9]+")) return true;
			else return false;
		}
		
		
		
		private void runSingle() {
//			AtomContainerSet acs=null;
			TESTApplication f=((TESTApplication) gui);
			
			
			AtomContainer molecule=null;

//			First search actor
			DSSToxRecord d=MoleculeUtilities.getDSSToxRecordFromDashboard(filepath.replace("\n", ""));
			//TODO search using v2 actor
					
			if (d!=null) {
				try {
					molecule = WebTEST4.prepareSmilesMolecule(d.smiles);//TODO should we do it based on smiles or inchi?
					molecule = CleanUpMolecule(molecule);					
					DSSToxRecord.assignFromDSSToxRecord(molecule, d);
//					acs=new AtomContainerSet();
//					acs.addAtomContainer(m);
					System.out.println("Actor record found: "+d+", smiles: "+d.smiles);
				
				} catch (Exception ex) {
					System.out.println("Error cleaning up molecule for smiles from actor="+d.smiles);
				}
			} 
			
			
			if (molecule==null) {
				
				if (isAllNumbers(filepath)) {
					if (isCAS(filepath)) {
//						System.out.println("is cas!");		
						String CAS=parseSearchCAS(filepath.replace("\n", ""));
//						System.out.println("*"+CAS+"*");
						molecule=(AtomContainer)LoadFromCASList(CAS+"\n").getAtomContainer(0);
					} else {
						
						JOptionPane.showMessageDialog(f, "Text contains only numbers but is invalid CAS");
						done=true;
						return;
					}
				} else {

					//Try loading by smiles next:
					molecule=(AtomContainer)LoadFromSmilesList(filepath).getAtomContainer(0);
					

					if (molecule.getProperty("ErrorCode")==WebTEST.ERROR_CODE_STRUCTURE_ERROR) {
						//Cant parse the smiles, so try searching by name:
						molecule=(AtomContainer)LoadFromNameList(filepath).getAtomContainer(0);
						

						String err=molecule.getProperty("Error");
						if (err.contentEquals("Name not found")) {
							JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
							done=true;
							return;
						}
					} 
				}
			}
			
			
			done = true;

			String error=molecule.getProperty("Error");
			if (error.contentEquals("Multiple molecules")) {
				JOptionPane.showMessageDialog(f,"Molecule not connected, please select another mol file");
				return;
			}
						
			molecule=f.configureModel(molecule);
			
			if (molecule.getAtomCount()==0) {
				JOptionPane.showMessageDialog(f, filepath.replace("\n", "")+" is not found");
				f.panelSingleStructureDatabaseSearch.jtfCAS.setText("");
				f.panelSingleStructureDatabaseSearch.jtfName.setText("");
			} else {
				setCAS(molecule);
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
			
			f.setTitle(TESTConstants.SoftwareTitle);			
			f.panelSingleStructureDatabaseSearch.jtfIdentifier.requestFocus();
			
		}
		
		private void runBatch() {
			AtomContainerSet moleculeSet=null;
			
			if (structureType==TypeCAS) moleculeSet=LoadFromCASList(filepath);
			else if (structureType==TypeName) moleculeSet=LoadFromNameList(filepath);
			else if (structureType==TypeSmiles) moleculeSet=LoadFromSmilesList(filepath);
			else if (structureType==TypeSDF) moleculeSet=LoadFromSDF(filepath);
			else if (structureType==TypeSDF_In_Jar) moleculeSet=LoadFromSDFInJar();
			
//			if (moleculeSet!=null) 
//				MolFileUtilities.FixDuplicateCASNumbersInSDF(moleculeSet);

			
//			System.out.println(moleculeSet==null);
			
			if (moleculeSet==null) return;

			if (getMessage().equals("canceled")) return;
			
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
