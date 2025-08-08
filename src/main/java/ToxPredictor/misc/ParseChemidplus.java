package ToxPredictor.misc;

import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.ImageIcon;

import ToxPredictor.MyDescriptors.AtomicProperties;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.AdaptUtilities;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.SaveStructureToFile;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.Utilities.chemicalcompare;


import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class ParseChemidplus {

	/**
	 * @param args
	 */

	Lookup lookup = new Lookup();
	String Delimiter="|";

//	String f1="C:/Documents and Settings/tmarti02/My Documents/javax/cdk from jar/";
	String f1="";
	
//	String f2="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/structures/";
//	String f2="ToxPredictor/chemidplus/structures/";
//	String f2="ToxPredictor/structures/";
	String f2="L:/Priv/Cin/NRMRL/CompTox/javax/cdk/ToxPredictor/Structures/";
	String f3=f1+f2;
	
	public String DougMolFolder=f3+"DougDB3d";
//	public String ChemidplusMolFolder=f3+"chemidplus3d";		
//	public String Chemidplus3dMolFolder=f3+"chemidplus3d new";
	
	public String Chemidplus3dMolFolder=f3+"chemidplus3d";
	public String Chemidplus2dMolFolder=f3+"chemidplus2d";
	
	public String DSSToxFolder=f3+"DSSTox_withH";
	public String NameFolder=f3+"Name_withH";
	public String NIST3DFolder=f3+"NIST3d";
	public String NIST2DFolder=f3+"NIST2d_withH";
	public String IndexNet2DFolder=f3+"INDEXNET2d_withH";
//	public String SmilesFolder=f3+"Smiles";
	public String SmilesFolder=f3+"Smiles_Marvin_withH";
	public String OtherFolder=f3+"Other";
	
//	public String NIHMolFolder="L:/Priv/Cin/NRMRL/CompTox/3Dcoordinates/database/coords";
	public String NIHMolFolder="L:/Priv/Cin/NRMRL/CompTox/javax/cdk/ToxPredictor/Structures/NIH/coords";
	
//	public String DougMolFolder="N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol";
//	public String ChemidplusMolFolder="N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol-chemidplus-3d";
	
	public String ChemidplusJarMolFolder="Chemidplus3d";
	public String DougJarMolFolder="DougDB3d";
	public String DSSToxJarMolFolder="DSSTox_withH";
	public String NIST2dJarMolFolder="NIST2d_withH";
	public String NIST3dJarMolFolder="NIST3d";	
	public String NIH3dJarMolFolder="coords";
	
	public String IndexNet2dJarMolFolder="INDEXNET2d_withH";
	public String SmilesJarMolFolder="Smiles_Marvin_withH";
	public String Name2dJarMolFolder="Name_withH";
	public String OtherJarMolFolder="Other";
	
//	public String MolSrc="jar";
	public String MolSrc="folder";

	
//	public void CreateStructureWebPage(int Month, int Day) {
//		ToxPredictor.Utilities.SmilesParser sp = new ToxPredictor.Utilities.SmilesParser();
//		Lookup lookup = new Lookup();
//
//		AtomContainer m = null;
//
//		String strfolder = "N:/NRMRL-PRIV/CompTox/LD50_ChemID_Data";
//		File folder = new File(strfolder);
//		File output = new File("ToxPredictor/chemidplus/chemidstructures"
//				+ Month + "-" + Day + ".html");
//		// System.out.println(folder.exists());
//
//		// MoleculeFragmenter3 mf3 = new MoleculeFragmenter3();
//
//		DescriptorFactory df = new DescriptorFactory(true);
//
//		File files[] = folder.listFiles();
//
//		GregorianCalendar cal = new GregorianCalendar(TimeZone
//				.getTimeZone("EST"), Locale.US);
//
//		try {
//			FileWriter fw = new FileWriter(output);
//			fw.write("<html><head>");
//			fw.write("<title>");
//			fw.write(Month + "-" + Day);
//			fw.write("</title></head>\n");
//
//			boolean RightDate = false;
//
//			for (int i = 0; i < files.length; i++) {
//				if (i % 100 == 0)
//					System.out.println(i);
//				if (!files[i].isDirectory()) {
//
//					long time = files[i].lastModified();
//
//					cal.setTimeInMillis(files[i].lastModified());
//					// NOTE: In Java January =0 so june =5
//
//					if (cal.get(Calendar.MONTH) != Month - 1
//							|| cal.get(Calendar.DAY_OF_MONTH) != Day) {
//						if (RightDate)
//							break; // stop when no longer have right date
//						continue;
//					} else
//						RightDate = true;
//
//					System.out.println(files[i].getName() + "\t"
//							+ cal.get(Calendar.YEAR) + "\t"
//							+ (cal.get(Calendar.MONTH) + 1) + "\t"
//							+ cal.get(Calendar.DAY_OF_MONTH));
//
//					FileData fd = this.RetrieveDataFromFile(files[i]);
//
//					if (fd.Formula.indexOf(".") > -1)
//						continue; // skip salt like compounds
//
//					if (fd.CAS.equals("50-76-0"))
//						continue; // takes too long
//
//					String Smiles = lookup.LookUpSmiles(fd.CAS);
//
//					File struct = new File(strfolder + File.separator + fd.CAS
//							+ "_files" + File.separator + "RenderImage.png");
//
//					// fw.write(Smiles+"<br>\n");
//
//					Smiles = CDKUtilities.FixSmiles(Smiles);
//
//					boolean ParsedOK = true;
//					try {
//						m = sp.parseSmiles(Smiles);
//					} catch (Exception e) {
//						ParsedOK = false;
//					}
//
//					Hashtable FragmentList = null;
//
//					if (ParsedOK) {
//
//						boolean HaveUnassigned = false;
//
//						DescriptorData dd = new DescriptorData();
//						df.CalculateDescriptors(m, dd, false, true, "");
//						FragmentList = dd.FragmentList;
//
//						for (int ii = 0; ii <= m.getAtomCount() - 1; ii++) {
//							if (!df.moleculefragmenter.Assigned[ii]) {
//								HaveUnassigned = true;
//								break;
//							}
//
//						}
//
//						DecimalFormat df2 = new DecimalFormat("0.00");
//
//						fw.write("<p>CAS: " + fd.CAS + "</p>\n");
//						fw.write("MW: " + fd.MW + "<br>\n");
//
//						double dMW = Double.parseDouble(fd.MW);
//						double diff = Math.abs(dMW
//								- dd.MW_Frag);
//
//						if (diff > 0.2) {
//							fw.write("MW-frags: <font color=\"red\">");
//							fw.write(df2.format(dd.MW_Frag));
//							fw.write("</font><br>\n");
//						} else {
//							fw.write("MW-frags: "
//									+ df2.format(dd.MW_Frag)
//									+ "<br>\n");
//						}
//						fw.write("<table>\n");
//						fw.write("<tr>\n");
//
//						fw.write("<td>\n");
//						this.WriteFragmentTable(fw, FragmentList);
//						fw.write("</td>\n");
//
//						if (HaveUnassigned) {
//
//							SaveStructureToFile.CreateImageFile(m, fd.CAS
//									+ "-2",
//									"ToxPredictor/chemidplus/structures", true,
//									false, true, 500);
//
//							fw.write("<td>\n");
//							this.WriteUnassignedTable(fw, m, df, fd);
//							fw.write("</td>\n");
//						} else {
//							fw.write("<td><br></td>\n");
//						}
//
//						fw.write("<td>\n");
//						fw.write("<img src=\"file:///" + struct
//								+ "\" align=\"bottom\">\n");
//						fw.write("</td>\n");
//
//						File imgfile = new File(
//								"ToxPredictor/chemidplus/structures/" + fd.CAS
//										+ ".png");
//
//						if (!(imgfile.exists())) {
//							SaveStructureToFile.CreateImageFile(m, fd.CAS,
//									"ToxPredictor/chemidplus/structures",
//									false, false, true, 250);
//						}
//
//						fw.write("<td>\n");
//						fw.write("<img src=\"structures/" + fd.CAS
//								+ ".png\">\n");
//						fw.write("</td>\n");
//
//						fw.write("</tr>\n");
//						fw.write("</table>\n");
//						fw.write("<br><hr>\n");
//
//						fw.flush();
//
//					} else {
//						fw.write("<table>\n");
//						fw.write("<tr>\n");
//
//						fw.write("<td>Smiles Parser error: " + Smiles
//								+ "</td>\n");
//
//						fw.write("<td>\n");
//						fw.write("<img src=\"file:///" + struct
//								+ "\" align=\"bottom\">\n");
//						fw.write("</td>\n");
//
//						fw.write("</tr>\n");
//						fw.write("</table>\n");
//						fw.write("<br><hr>\n");
//
//						fw.flush();
//
//					}
//
//				}
//
//			}
//
//			fw.write("</html>");
//			fw.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	private void WriteUnassignedTable(FileWriter fw, IAtomContainer m,
			DescriptorFactory df, FileData fd) {
		try {

			fw
					.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("\t<th><a href=\"structures/" + fd.CAS
					+ "-2.png\">AtomNumber</a></th>\n");

			fw.write("\t<th>Estate</th>\n");

			fw.write("</tr>\n");

			for (int ii = 0; ii <= m.getAtomCount() - 1; ii++) {

				if (!df.moleculefragmenter.Assigned[ii]) {

					fw.write("<tr>\n");
					fw.write("\t<td>" + (ii + 1) + "</td>\n");
					fw.write("\t<td>" + df.Fragment[ii] + "</td>\n");
					fw.write("</tr>\n");
				}

			}

			fw.write("</table>\n");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void FigureOutWhichOnesWerentDownloadedFromChemidplus() {
		
		String strDir="ToxPredictor/chemidplus/chemidplus extra mutagenicity";
		
		String CASlist="ToxPredictor/chemidplus/parse/CAS list for mutagenicity chemicals not in chemidplus structure data.txt";
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(CASlist));
			
			while (true) {
				
				String CAS=br.readLine();
				
				if (CAS==null) break;
				
				File webpage=new File(strDir+"/"+CAS+".htm");
				
				if (!webpage.exists()) {
					System.out.println(CAS);
				}
			}
			
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void WriteFragmentTable(FileWriter fw, Hashtable hash) {
		// write sorted frag table:

		try {

			fw
					.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("\t<th>Fragment</th>\n");
			fw.write("\t<th>Count</th>\n");

			fw.write("</tr>\n");

			sarray my_array = new sarray();
			for (Enumeration e = hash.keys(); e.hasMoreElements();) {

				// retrieve the object_key
				String object_key = (String) e.nextElement();
				// retrieve the object associated with the key
				double object_val = (Double) hash.get(object_key);

				// build a sortable object with the two pieces of information
				sortable s = new sortable(object_key, object_val);
				// add the sortable object to the sarray
				my_array.add(s);

			}

			// sort the sarray
			Collections.sort(my_array);

			for (int i = 0; i < my_array.size(); i++) {

				sortable s = (sortable) my_array.elementAt(i);

				String var = (String) s.getKey();
				double val = (Double) s.getObject();

				if (val > 0) {
					fw.write("<tr>\n");
					if (var.indexOf("H") == 0) {
						fw.write("\t<td><font color=\"red\">" + var
								+ "</font></td>\n");

					} else {
						fw.write("\t<td>" + var + "</td>\n");
					}

					fw.write("\t<td>" + val + "</td>\n");
					fw.write("</tr>\n");
				}

			}

			// for (java.util.Enumeration e = hash.keys(); e
			// .hasMoreElements();) {
			// String var = (String) e.nextElement();
			//
			// double val = (Double) hash.get(var);
			//
			// if (val > 0) {
			// fw.write("<tr>\n");
			// if (var.equals("H")) {
			// fw.write("\t<td><font color=\"red\">" + var
			// + "</font></td>\n");
			//
			// } else {
			// fw.write("\t<td>" + var + "</td>\n");
			// }
			//
			// fw.write("\t<td>" + val + "</td>\n");
			// fw.write("</tr>\n");
			// }
			//
			// }

			fw.write("</table>\n");
			fw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * Old routine- parses html files and puts in single flat text database
//	 *
//	 */
//	public void ParseData() {
//
//		ToxPredictor.Utilities.SmilesParser sp = new ToxPredictor.Utilities.SmilesParser();
//		Lookup lookup = new Lookup();
//
//		AtomContainer m = null;
//		MFAnalyser mfa = null;
//		MFAnalyser mfa2 = null;
//		File folder = new File("N:/NRMRL-PRIV/CompTox/LD50_ChemID_Data");
//		File output = new File("ToxPredictor/system/chemiddata.txt");
//		// System.out.println(folder.exists());
//
//		File files[] = folder.listFiles();
//
//		try {
//			FileWriter fw = new FileWriter(output);
//
//			fw
//					.write("CAS\tName\tSystematicName\tMW\tFormula\tCalculatedFormula\tFormulaCheck\tSmiles\tLD50\tLD50Units\tEffect\tReference\tReferenceURL\n");
//
//			// for (int i = 0; i < 10; i++) {
//			for (int i = 0; i < files.length; i++) {
//
//				if (i % 10 == 0)
//					System.out.println((i / 2));
//				if (!files[i].isDirectory()) {
//					// System.out.println(files[i]);
//
//					FileData fd = this.RetrieveDataFromFile(files[i]);
//
//					String Smiles = lookup.LookUpSmiles(fd.CAS);
//
//					if (fd.Formula.indexOf(".") > -1) {
//						fd.FormulaCheck = "Salt";
//					} else if (Smiles.equals("missing")) {
//						fd.FormulaCheck = "MissingSmiles";
//					} else {
//
//						Smiles = CDKUtilities.FixSmiles(Smiles);
//
//						boolean ParsedOK = true;
//						try {
//							m = sp.parseSmiles(Smiles);
//						} catch (Exception e) {
//							System.out.println("parse error:" + Smiles);
//							ParsedOK = false;
//							fd.FormulaCheck = "SmilesParserError";
//						}
//
//						if (ParsedOK) {
//							CDKUtilities.AddHydrogens(m);
//
//							mfa = new MFAnalyser(m);
//
//							AtomContainer m2 = new AtomContainer();
//							mfa2 = new MFAnalyser(fd.Formula, m2);
//
//							Hashtable h = mfa.getFormulaHashtable();
//							Hashtable h2 = mfa2.getFormulaHashtable();
//
//							fd.CalculatedFormula = mfa.getMolecularFormula();
//							// System.out.println(mfa.getMolecularFormula()+"\t"+Formula);
//
//							boolean FormulaOK = true;
//
//							// have to check formulas using hashtables
//							// since the elements might be in a
//							// different order
//
//							try {
//								for (java.util.Enumeration e = h.keys(); e
//										.hasMoreElements();) {
//									String var = (String) e.nextElement();
//
//									int val = (Integer) h.get(var);
//									int val2 = (Integer) h2.get(var);
//									// System.out.println(var+"\t"+val+"\t"+val2);
//
//									if (val != val2) {
//										FormulaOK = false;
//										break;
//									}
//
//								}
//							} catch (Exception e) {
//								FormulaOK = false;
//							}
//
//							if (FormulaOK)
//								fd.FormulaCheck = "OK";
//							else
//								fd.FormulaCheck = "Mismatch";
//						}
//
//					} // end else smiles != missing
//
//					// System.out.println(Formula+"\t"+mfa.getMolecularFormula()+"\t"+MW+"\t"+mfa.getMass());
//
//					// if (!mfa.getMolecularFormula().equals(Formula)) {
//					// System.out.println(mfa.getMolecularFormula()+"\t"+Formula);
//					// }
//
////					fw.write(fd.CAS + "\t" + fd.Name + "\t" + fd.SystematicName
////							+ "\t" + fd.MW + "\t" + fd.Formula + "\t"
////							+ fd.CalculatedFormula + "\t" + fd.FormulaCheck
////							+ "\t" + Smiles + "\t" + fd.LD50 + "\t"
////							+ fd.LD50Units + "\t" + fd.Effect + "\t"
////							+ fd.Reference + "\t" + fd.ReferenceURL + "\n");
//					fw.flush();
//
//				}
//			}
//
//			fw.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	private FileData RetrieveDataFromFile(File htmlfile) {

		// <td class="TableCell"><font style="font-weight: bold; color:
		// red;">rat</font></td> <td class="TableCell"><font style="font-weight:
		// bold; color: red;">LD50</font></td> <td class="TableCell"><font
		// style="font-weight: bold; color: red;">oral</font></td>

//		String strName = "<span class=\"name\">";
		
		// 6/29/07 - chemidplus changed their formatting:
		String strName="<span class=\"name\" style=\"width: 100%; text-align: center;\">";
		
		String strLD50 = "<td class=\"TableCell\"><font style=\"font-weight: bold; color: red;\">rat</font></td>   <td class=\"TableCell\"><font style=\"font-weight: bold; color: red;\">LD50</font></td>   <td class=\"TableCell\"><font style=\"font-weight: bold; color: red;\">oral</font></td>";
		String strSynonyms="<strong>Synonyms </strong><br>";
		String strSystematicName = "<strong>Systematic Name </strong><br>";
		String strFormula = "<strong>Molecular Formula </strong><br>";

		FileData fd = new FileData();
		
		fd.FileName=htmlfile.getName();
		

		try {
			BufferedReader br = new BufferedReader(new FileReader(htmlfile));

			String Line;

			int LD50Count = 0;
			int CloseTagCount = 0;

			
			boolean FoundStrFormula=false;
			
			while (true) {
				Line = br.readLine();

//				if (FoundStrFormula) System.out.println(Line);
				if (!(Line instanceof String))
					break;

				if (Line.indexOf("</html>") > -1)
					CloseTagCount++;

				if (Line.indexOf(strName) > -1) {
					this.GetName(br, fd);
				} else if (Line.indexOf(strSynonyms) > -1) {
					this.GetSynonyms(br, fd);				
				} else if (Line.indexOf(strSystematicName) > -1) {										
					this.GetSystematicName(br, fd);
				} else if (Line.indexOf("RN:&nbsp;") > -1) {
					fd.CAS = Line.substring(Line.indexOf(";") + 1, Line
							.length());
				} else if (Line.indexOf("MW:&nbsp;") > -1) {
					//note as of 6/29/07 chemidplus doesnt have MW listed on full records web page
					
					fd.MW = Line
							.substring(Line.indexOf(";") + 1, Line.length())
							.trim();
				} else if (Line.indexOf(strFormula) > -1) {
//					System.out.println("found strFormula");
					FoundStrFormula=true;
					
					this.GetFormula2(br, fd);
				} else if (Line.indexOf(strLD50) > -1) {
//					System.out.println("found strLD50");
					this.GetLD50(br, fd, Line, strLD50);
				}
			}

			
			if (fd.CAS.equals("")) {
				String n=htmlfile.getName();
				fd.CAS=n.substring(0,n.indexOf("."));
				System.out.println(fd.CAS);
			}
			
			br.close();

			if (LD50Count > 1)
				System.out.println(LD50Count);

			if (CloseTagCount == 2)
				fd.FileComplete = true;
			// System.out.println(fd.CAS+"\t"+fd.LD50);

			return fd;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	private void GetName(BufferedReader br, FileData fd) {
		try {
			fd.Name = br.readLine();
			
			if (fd.Name==null) return;
			
			if (fd.Name.indexOf("<br>")>-1) {
				fd.Name = fd.Name.substring(0, fd.Name.indexOf("<br>"));	
			}
			
			if (fd.Name.indexOf("[") > -1) {
				fd.Name = fd.Name.substring(0, fd.Name.indexOf("["));
			}
			
			fd.Name = fd.Name.trim();
		} catch (Exception e) {
			System.out.println("error in getting name for "+fd.FileName);
			e.printStackTrace();
		}

	}

	/**
	 * Gets the first formula listed in html file
	 * @param br
	 * @param fd
	 */
	private void GetFormula(BufferedReader br, FileData fd) {
		String Line = null;
		try {
			while (true) {
				Line = br.readLine();

				if (Line == null)
					return;

				if (Line.indexOf("<a href=") > -1) {
					Line = br.readLine();
					break;
				}
			}

			fd.Formula = Line.trim().replaceAll("-", "");
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets all the formulas for a given compound in html file and stores in formula array in FileData
	 * @param br
	 * @param fd
	 */
	private void GetFormula2(BufferedReader br, FileData fd) {
		String Line = null;
		try {
			while (true) {
				Line = br.readLine();

				if (Line == null)
					return;

				if (Line.indexOf("<a href=") > -1) {
					Line = br.readLine();
					String Formula = Line.trim().replaceAll("-", "");
					fd.Formulas.add(Formula);					
				}
				
				if (Line.indexOf("</td></tr><tr>")>-1 || Line.indexOf("<strong>Molecular Formula Fragments </strong><br>")>-1) {
					break;
				}
				
				
			}

			//use first formula in Formula field:
			
			fd.Formula=(String)fd.Formulas.get(0);
			
//			if (fd.Formulas.size()>1) {
//				System.out.println(fd.CAS);
//				for (int i=0;i<fd.Formulas.size();i++) {
//					System.out.println(fd.Formulas.get(i));
//				}
//				System.out.println("");
//			}

			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * For now this method stores multiple tox records using semicolons to store
	 * values for each record
	 * 
	 * Possibly rewrite to store as ArrayList of ToxicityData objects
	 * 
	 * @param br
	 * @param fd
	 * @param Line
	 * @param strLD50
	 */
	private void GetLD50(BufferedReader br, FileData fd, String Line,
			String strLD50) {


		while (Line.indexOf(strLD50) > -1) {

			CarcinogenicityRecord tr=new CarcinogenicityRecord();

			fd.ToxicityRecords.add(tr);
			
			Line = Line.substring(Line.indexOf(strLD50) + strLD50.length(),
					Line.length());

			// if (fd.CAS.equals("60828-78-6"))
			// System.out.println(Line);

			// need to store first tox value in case it has > or < symbol
			
			String Line4=null;
			String Line2=null;
			
			
				
			Line4 = Line.substring(Line.indexOf(">") + 1, Line
						.indexOf("("));
				
			Line2 = Line.substring(Line.indexOf("(") + 1, Line
						.indexOf(")"));
							
			if (Line.indexOf("mg(Fe)/k")>-1) {				
				Line2 = Line.substring(Line.indexOf(")") + 1, Line.length());
				Line2 = Line2.substring(Line2.indexOf("(") + 1, Line2.indexOf(")"));
//				System.out.println(Line2+"\n"+Line4);
			}

//			if (fd.CAS.equals("23868-54-4")) System.out.println(Line2);
			
			
			
			
			if (Line2.indexOf("m") > -1) {

				tr.LD50 = Line2.substring(0, Line2.indexOf("m"));
				tr.LD50Units = Line2.substring(Line2.indexOf("m"), Line2
						.length());

			} else if (Line2.indexOf("i") > -1) {

				tr.LD50 = Line2.substring(0, Line2.indexOf("i"));
				tr.LD50Units = Line2.substring(Line2.indexOf("i"), Line2
						.length());
//				System.out.println(fd.CAS + "\t" + LD50New + "\t"
//						+ LD50UnitsNew);

			} else {
				System.out.println(fd.CAS + "\t" + Line2);
			}

			if (Line4.indexOf("&gt;") > -1) {
				tr.LD50 = ">" + tr.LD50;
			} else if (Line4.indexOf("&lt;") > -1) {
				tr.LD50 = "<" + tr.LD50;
			}


			// ******************************************************************

			String Line3 = Line.substring(Line.indexOf("</td>") + 6, Line
					.length());
			Line3 = Line3.substring(Line3.indexOf(">") + 1, Line3.length());
			
			
			tr.Effect = Line3.substring(0, Line3.indexOf("</td>"));
									
			if (tr.Effect.equals("&nbsp;"))
				tr.Effect = "Unknown";


			// ******************************************************************

			Line3 = Line3
					.substring(Line3.indexOf("</td>") + 30, Line3.length());
			// System.out.println("line3: "+Line3);
		
			
			if (Line3.indexOf("</td>") > -1) {
				tr.Reference = Line3.substring(0, Line3.indexOf("</td>"));

				if (tr.Reference.indexOf("<a href") > -1) {
					tr.ReferenceURL = tr.Reference.substring(tr.Reference
							.indexOf("<a href") + 9, tr.Reference.length());

					tr.ReferenceURL = tr.ReferenceURL.substring(0,
							tr.ReferenceURL.indexOf("\""));

					tr.Reference = tr.Reference.substring(0, tr.Reference
							.indexOf("<a href") - 4);

				} else {
					tr.ReferenceURL = "Unknown";
				}
			} else {
				tr.Reference = "Unknown";
				tr.ReferenceURL = "Unknown";
			}

			tr.Reference=tr.Reference.replaceAll("\"","");
			
			
//			if (fd.CAS.equals("23868-54-4")) {
//				System.out.println(tr.Effect);
//				System.out.println(tr.Reference);
//				System.out.println(tr.ReferenceURL);
//			}

		} // end while loop
		
		

			// System.out.println(fd.LD50);
			// System.out.println(fd.LD50Units+"\n");
			// System.out.println(fd.Effect);
			// System.out.println(fd.Reference);
			// System.out.println(fd.ReferenceURL);
		
	}

	private void GetSystematicName(BufferedReader br, FileData fd) {
		String Line = null;

		fd.SystematicName = new ArrayList<>();

		try {

			while (true) {
				Line = br.readLine();
				if (Line.indexOf("Registry Numbers") > -1 || Line.indexOf("Superlist Name")>-1)
					break;

				if (Line.indexOf("<a href=") > -1) {
					Line = br.readLine();
					String NewSystematicName = Line.trim();
					NewSystematicName = NewSystematicName
							.replaceAll("<br>", "");
					
					fd.SystematicName.add(NewSystematicName);
					
				}

			}

			// if (fd.CAS.equals("55242-55-2")) {
			// System.out.println(fd.CAS+"\t"+fd.SystematicName);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void GetSynonyms(BufferedReader br, FileData fd) {
		String Line = null;

		fd.Synonyms = new ArrayList<>();

		try {

			while (true) {
				Line = br.readLine();
				
				
				if (Line.indexOf("Systematic Name") > -1 || Line.indexOf("CAS Registry Number ")>-1)
					break;

				if (Line.indexOf("<a href=") > -1) {
					Line = br.readLine();
					String NewSynonym = Line.trim();
					NewSynonym = NewSynonym
							.replaceAll("<br>", "");
					
					fd.Synonyms.add(NewSynonym);
					
				}

			}

			if (Line.indexOf("Systematic Name") > -1) {
				this.GetSystematicName(br,fd);
			}
					

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public void CompareStructuresFromNameToBestStructure(String chemnums) {
			DescriptorFactory df=new DescriptorFactory(false);
			
			String sdffolder="ToxPredictor/chemidplus/sdf_from_name/SDF Files With Structures";
			
			String ofolder="ToxPredictor/chemidplus/compare/Name";
			String filename="Good_Data_Structures_"+chemnums+"-2.sdf";		
			
			File SDFfilename=new File(sdffolder+"/"+filename);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
			chemicalcompare cc=new chemicalcompare();
			System.out.println(chemnums);
			try {
	
				BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
				FileWriter fw=new FileWriter("ToxPredictor/chemidplus/compare/Name/compare_"+chemnums+".html");
				FileWriter fw2=new FileWriter("ToxPredictor/chemidplus/compare/Name/formulacompare_"+chemnums+".html");
				
				fw.write("<html><head><title>\r\n");
				fw.write("Compare structures-"+SDFfilename.getName()+"\r\n");			     
				fw.write("</title></head>\r\n");
				
				
				fw2.write("<html><head><title>\r\n");
				fw2.write("Compare formulas-"+SDFfilename.getName()+"\r\n");
				fw2.write("</title></head>\r\n");
				
				fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
				fw2.write("<tr bgcolor=\"#D3D3D3\">\n");		
				fw2.write("\t<th>CAS</th>\n");
				fw2.write("\t<th>Src</th>\n");
				fw2.write("\t<th>CalculatedFormula</th>\n");
				fw2.write("\t<th>Formula</th>\n");				
				fw2.write("</tr>\n");
				
				
				IteratingSDFReader mr = new IteratingSDFReader(br, DefaultChemObjectBuilder.getInstance());
	
				
	
				IAtomContainer mSDF; // molecule from SDF File
	
				int counter = 0;
				int count=0;
				
				while (mr.hasNext()) {
	
					try {
	
						mSDF = mr.next();
						counter++;
	
//						if (mSDF.getAtomCount()==0) break;
						
	//					if (counter % 1000 == 0) System.out.println(counter);
	
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
	
	
	
					String CAS =(String)mSDF.getProperty("CAS");
					String NAME = (String)mSDF.getProperty("NAME");
	
					FileData fd=new FileData();
					
					fd.CAS=CAS;
					
					this.CheckForMolFiles2(fd);				
					IAtomContainer molecule=this.GetBestMolecule(fd); // molecule from mol files or from smiles
					
					if (molecule==null) continue;
					
					df.Normalize(mSDF);
					df.Normalize(molecule);
					
									
					IAtomContainer mSDF2=CDKUtilities.addHydrogens(mSDF);
					IAtomContainer molecule2=CDKUtilities.addHydrogens(molecule);
					
					boolean IsSalt1=this.IsSalt(molecule2);
					boolean IsSalt2=this.IsSalt(mSDF2);
					
					
					String ImageFileName1=fd.CAS+".png";
					String ImageFileName2=fd.CAS+"_SDF"+".png";

					
					if (mSDF2==null || molecule2==null) {
						
						SaveStructureToFile.CreateImageFile(molecule2,CAS,ofolder+"/images");
						SaveStructureToFile.CreateImageFile(mSDF,CAS+"_SDF",ofolder+"/images");
						System.out.println(CAS);
						count++;
						this.WriteHTMLForDifference(ofolder,count,"From mol file","From name",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
						continue;
						
					}
					
					boolean WriteOut=false;
					boolean Isomorph=false;
					
					try {
					
						Isomorph=cc.isIsomorphFingerPrinter(molecule2,mSDF2);
					} catch (Exception ex1) {
						WriteOut=true;					
					}
					

					
					if (WriteOut || !Isomorph) {					
						
						
						
						fd.CalculatedFormula=CDKUtilities.generateHTMLFormula(molecule2);
						
						
						fd.Formula=CDKUtilities.generateFormula(mSDF2);
											
						SaveStructureToFile.CreateImageFile(molecule2,CAS,ofolder+"/images");
						SaveStructureToFile.CreateImageFile(mSDF2,CAS+"_SDF",ofolder+"/images");
//						System.out.println(CAS);
						count++;
						this.WriteHTMLForDifference(ofolder,count,"From mol file","From name",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
						
						if (!fd.CalculatedFormula.equals(fd.Formula)) {
							fw2.write("<tr>\n");		

							String CAS2=CAS;
							while (CAS2.length()<12) {
								CAS2="0"+CAS2;
							}

							fw2.write("\t<td>"+CAS2+"</td>\n");
							fw2.write("\t<td>"+fd.StructureSource+"</td>\n");
							fw2.write("\t<td>"+fd.CalculatedFormula+"</td>\n");
							fw2.write("\t<td>"+fd.Formula+"</td>\n");
														
							
							fw2.write("</tr>\n");
							
							fw2.flush();
						}
						
					}
					
					
				}// end while true;
			
				
				fw.write("</html>\r\n");
				fw2.write("</table></html>\r\n");
				
				br.close();
				fw.close();
				fw2.close();
				
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	
	public void CompareStructuresFromDSSToxToBestStructureForGoodDataChemicals() {

		DescriptorFactory df=new DescriptorFactory(false);
		//			MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();

		try {

			String CASfile="ToxPredictor/chemidplus/compare/Good Data-HaveDSSTOX2d CAS list.txt";

			BufferedReader br = new BufferedReader(new FileReader(CASfile));

			String folder="ToxPredictor/chemidplus/compare/DSSTox-GoodData";

			FileWriter fw=new FileWriter(folder+"/"+"compare_DSSTOX_GoodData.html");
			FileWriter fw2=new FileWriter(folder+"/"+"formulacompare_DSSTOX_GoodData.html");

			FileWriter fw3=new FileWriter(folder+"/"+"compare_DSSTOX_GoodData_nosalt-GoodData.html");
			FileWriter fw4=new FileWriter(folder+"/"+"formulacompare_DSSTOX_GoodData_singlechemical_nosalt.html");

			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures to DSSTox For Good Data"+"\r\n");

			fw.write("</title></head>\r\n");



			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas-to DSSTox For Good Data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");		
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Src</th>\n");
			fw2.write("\t<th>CalculatedFormula</th>\n");
			fw2.write("\t<th>DSSToxFormula</th>\n");
			fw2.write("\t<th>Description</th>\n");
			fw2.write("\t<th>FormulaMatch</th>\n");
			fw2.write("</tr>\n");
			fw2.flush();

			fw4.write("<html><head><title>\r\n");
			fw4.write("Compare formulas-to DSSTox For Good Data"+"-No Salt, Single chemical\r\n");
			fw4.write("</title></head>\r\n");

			fw4.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
			fw4.write("<tr bgcolor=\"#D3D3D3\">\n");		
			fw4.write("\t<th>CAS</th>\n");
			fw4.write("\t<th>Src</th>\n");
			fw4.write("\t<th>CalculatedFormula</th>\n");
			fw4.write("\t<th>DSSToxFormula</th>\n");
			fw4.write("\t<th>Description</th>\n");
			fw4.write("</tr>\n");
			fw4.flush();



			int counter = 0;
			int count1=0;
			int count3=0;

			while (true) {

				try {

					String CAS=br.readLine();

					if (CAS==null) break;


					FileData fd=new FileData();
					fd.CAS=CAS;

					this.CheckForMolFiles2(fd);				
					IAtomContainer best_molecule=this.GetBestMoleculeNoDSSTox(fd);
					IAtomContainer DSSTOX_molecule = this.LoadChemicalFromMolFile(fd.CAS+"_DSSTOX2d",this.DSSToxFolder);

					String Formula=(String)DSSTOX_molecule.getProperty("STRUCTURE_Formula");					
					String NAME = "DSSTox";					
					String TestSubstance_Description=(String)DSSTOX_molecule.getProperty("TestSubstance_Description");

					NAME=NAME+" ("+TestSubstance_Description+")";

					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}


					if (best_molecule==null) continue;

					df.Normalize(best_molecule);
					df.Normalize(DSSTOX_molecule);

					best_molecule=CDKUtilities.addHydrogens(best_molecule);
					DSSTOX_molecule=CDKUtilities.addHydrogens(DSSTOX_molecule);

					//					CDKUtilities.AddHydrogens(best_molecule);
					//					CDKUtilities.AddHydrogens(DSSTOX_molecule);

					boolean IsSalt1=this.IsSalt(best_molecule);
					boolean IsSalt2=this.IsSalt(DSSTOX_molecule);


					String ImageFileName1=fd.CAS+".png";
					String ImageFileName2=fd.CAS+"_DSSTox"+".png";


					if (best_molecule==null || DSSTOX_molecule==null) {

						SaveStructureToFile.CreateImageFile(best_molecule,CAS,folder+"/images");
						SaveStructureToFile.CreateImageFile(DSSTOX_molecule,CAS+"_DSSTox",folder+"/images");
						System.out.println(CAS);
						count1++;


						this.WriteHTMLForDifference(folder,count1,"From best source","From DSSTox",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
						continue;

					}

					boolean WriteOut=false;
					boolean Isomorph=false;

					try {

						Isomorph=cc.isIsomorphFingerPrinter(best_molecule,DSSTOX_molecule);
					} catch (Exception ex1) {
						WriteOut=true;					
					}



					if (WriteOut || !Isomorph) {					


						fd.CalculatedFormula=CDKUtilities.generateFormula(best_molecule);

						fd.Formula=CDKUtilities.fixFormula(Formula);

						SaveStructureToFile.CreateImageFile(best_molecule,CAS,folder+"/images");
						SaveStructureToFile.CreateImageFile(DSSTOX_molecule,CAS+"_DSSTox",folder+"/images");
						//						System.out.println(CAS);

						count1++;
						this.WriteHTMLForDifference(folder,count1,"From best source","From DSSTox",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);



						if (!IsSalt1 &&  !IsSalt2 && TestSubstance_Description.equals("single chemical compound")) {
							count3++;
							this.WriteHTMLForDifference("ToxPredictor/chemidplus/compare/DSSTox-GoodData",count3,"From best source","From DSSTox",fw3,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
						}



						fw2.write("<tr>\n");								
						fw2.write("\t<td>"+CAS2+"</td>\n");
						fw2.write("\t<td>"+fd.StructureSource+"</td>\n");

						if (IsSalt1) {
							fw2.write("\t<td><font color=\"red\">"+fd.CalculatedFormula+"</font></td>\n");
						} else {
							fw2.write("\t<td>"+fd.CalculatedFormula+"</td>\n");	
						}

						if (IsSalt2) {
							fw2.write("\t<td><font color=\"red\">"+fd.Formula+"</font></td>\n");							
						} else {
							fw2.write("\t<td>"+fd.Formula+"</td>\n");
						}

						fw2.write("\t<td>"+TestSubstance_Description+"</td>\n");

						fw2.write("\t<td>"+fd.CalculatedFormula.equals(fd.Formula)+"</td>\n");

						fw2.write("</tr>\n");

						fw2.flush();


						if (!IsSalt1 &&  !IsSalt2 && TestSubstance_Description.equals("single chemical compound")) {
							if (!fd.CalculatedFormula.equals(fd.Formula)) {
								fw4.write("<tr>\n");								
								fw4.write("\t<td>"+CAS2+"</td>\n");
								fw4.write("\t<td>"+fd.StructureSource+"</td>\n");
								fw4.write("\t<td>"+fd.CalculatedFormula+"</td>\n");
								fw4.write("\t<td>"+fd.Formula+"</td>\n");											
								fw4.write("\t<td>"+TestSubstance_Description+"</td>\n");
								fw4.write("</tr>\n");

								fw4.flush();
							}
						}

					}



					counter++;

					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}




			}// end while true;

			System.out.println(counter);

			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			
			fw4.write("</table></html>\r\n");

			br.close();
			fw.close();
			fw2.close();
			fw3.close();
			fw4.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 *  creates ArrayList of molecule sets (each molecule set has isomorphic structure)
	 * @param AtomContainerSet
	 */
	public ArrayList CompareChemicalsInList(AtomContainerSet AtomContainerSet,String method) {
		//for some reason cloning the AtomContainerSet makes it have 3 molecules if start with one!
		
		chemicalcompare cc=new chemicalcompare();
		
		ArrayList MasterList=new ArrayList();

		IAtomContainer molecule0=AtomContainerSet.getAtomContainer(0);		
		AtomContainerSet AtomContainerSet1=new AtomContainerSet();		
		AtomContainerSet1.addAtomContainer(molecule0);
		AtomContainerSet.removeAtomContainer(molecule0);
		MasterList.add(AtomContainerSet1);
		


		while (AtomContainerSet.getAtomContainerCount()>0) {
			
			IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
			
			boolean HaveMatch=false;
			
			for (int i=0;i<MasterList.size();i++) {
				AtomContainerSet AtomContainerSeti=(AtomContainerSet)MasterList.get(i);
				IAtomContainer moleculei=AtomContainerSeti.getAtomContainer(0);
				
				boolean Isomorph=false;
				
//				System.out.print("Checking isomorph "+molecule.getProperty("Source")+" with "+moleculei.getProperty("Source")+"...");
				try {
					if (method.equals(cc.methodFingerPrinter)) {
						
						AllRingsFinder arf = new AllRingsFinder();
						arf.setTimeout(60000);
						
						IRingSet rs = arf.findAllRings(molecule);
						IRingSet rsi = arf.findAllRings(moleculei);
												
						Isomorph=cc.isIsomorphFingerPrinter(molecule,moleculei,rs,rsi);
						
					} else if (method.equals(cc.method2dDescriptors)) {
						Isomorph=cc.isIsomorph2ddescriptors(molecule,moleculei);
//						System.out.println(Isomorph);
					} else if (method.equals(cc.methodUIT)) {
						Isomorph=cc.isIsomorphUIT(molecule, moleculei);
					} else if (method.equals(cc.methodHybrid)) {
						Isomorph=cc.isIsomorphHybrid(molecule,moleculei,true,true,true);
					}
//					System.out.print("done\n");
//					System.out.println(Isomorph);
				} catch (Exception ex1) {
					System.out.println(ex1);
				}
				
//				System.out.print("done\n");
				
				if (Isomorph) {
					//add to AtomContainerSeti:
					AtomContainerSeti.addAtomContainer(molecule);
					AtomContainerSet.removeAtomContainer(molecule);
					HaveMatch=true;
					break;
					
				} 
				
			}//end MasterList loop
			
			if (!HaveMatch) {
				//make new AtomContainerSet:								
				AtomContainerSet AtomContainerSetNew=new AtomContainerSet();
				AtomContainerSetNew.addAtomContainer(molecule);
				MasterList.add(AtomContainerSetNew);
				AtomContainerSet.removeAtomContainer(molecule);									
			}
			
			
		}//end AtomContainerSet while loop
		
		//print results:
		
//		for (int i=0;i<MasterList.size();i++) {
//			AtomContainerSet AtomContainerSeti=(AtomContainerSet)MasterList.get(i);
//			
//			System.out.print(i+"\t");
//			for (int j=0;j<AtomContainerSeti.getAtomContainerCount();j++) {
//				System.out.print(AtomContainerSeti.getAtomContainer(j).getProperty("Source")+"\t");
//			}
//			System.out.print("\n");
//		}
//		System.out.print("\n");
		
		return MasterList;
		
		
	}
	
	
	
	
	/**
		 * Loops through list of cas number of good chemicals then compares best and second best structures- if difference found compares to next best structure
		 *
		 */
		public void CompareStructuresForGoodDataChemicals() {
			
			DescriptorFactory df=new DescriptorFactory(false);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
			chemicalcompare cc=new chemicalcompare();
	
			try {
	
				String CASfile="ToxPredictor/chemidplus/compare/Good Data - CAS list.txt";
					
				BufferedReader br = new BufferedReader(new FileReader(CASfile));
				
				String folder="ToxPredictor/chemidplus/compare/GoodData";
				
				FileWriter fw=new FileWriter(folder+"/"+"compareGoodData.html");
				FileWriter fw2=new FileWriter(folder+"/"+"formulacompareGoodData.html");
				
				
				fw.write("<html><head><title>\r\n");
				fw.write("Compare structures to DSSTox For Good Data"+"\r\n");
				fw.write("</title></head>\r\n");
				
				fw2.write("<html><head><title>\r\n");
				fw2.write("Compare formulas-to DSSTox For Good Data"+"\r\n");
				fw2.write("</title></head>\r\n");
				
				fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
				fw2.write("<tr bgcolor=\"#D3D3D3\">\n");		
				fw2.write("\t<th>CAS</th>\n");
				fw2.write("\t<th>Src</th>\n");
				fw2.write("\t<th>CalculatedFormula</th>\n");
				fw2.write("\t<th>DSSToxFormula</th>\n");
				fw2.write("\t<th>Description</th>\n");
				fw2.write("\t<th>FormulaMatch</th>\n");
				fw2.write("</tr>\n");
				fw2.flush();
				
				
				int counter = 0;
				int count1=0;
	
	//			while (true) {
				while (counter<50) {
						
					
					try {
	
						String CAS=br.readLine();
						
						if (CAS==null) break;
						
						
						FileData fd=new FileData();
						fd.CAS=CAS;
						
						this.CheckForMolFiles2(fd);
						
						AtomContainerSet AtomContainerSet=this.getAtomContainers2(fd);
						
	//					System.out.print(CAS+"\t");
	//					for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
	//						System.out.print(AtomContainerSet.getAtomContainer(i).getProperty("Source")+"\t");
	//					}
	//					System.out.print("\n");
						
						
						IAtomContainer best_molecule=AtomContainerSet.getAtomContainer(0);
						
	//					System.out.println(best_molecule.getProperty("Source"));
											
						if (AtomContainerSet.getAtomContainerCount()<2) {
							System.out.println("Only 1 structure for "+CAS);
							counter++;
							continue;
						}
						
						
						
						IAtomContainer second_best_molecule = AtomContainerSet.getAtomContainer(1);
											
						String CAS2=CAS;
						while (CAS2.length()<12) {
							CAS2="0"+CAS2;
						}
	
						
						if (best_molecule==null) continue;
						
	//					for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
	//						AtomContainer m=AtomContainerSet.getAtomContainer(i);
	//						df.Normalize(m);
	//					}
						
	//					best_molecule=myHA.AddExplicitHydrogens(best_molecule);
	//					second_best_molecule=myHA.AddExplicitHydrogens(second_best_molecule);
	
	//					CDKUtilities.AddHydrogens(best_molecule);
	//					CDKUtilities.AddHydrogens(DSSTOX_molecule);
						
						
						String ImageFileName1=fd.CAS+"-1.png";
						String ImageFileName2=fd.CAS+"-2.png";
	
													
						
						boolean WriteOut=false;
						boolean Isomorph=false;
						
						try {
						
							Isomorph=cc.isIsomorphFingerPrinter(best_molecule,second_best_molecule);
							
	//						System.out.println(Isomorph);
						} catch (Exception ex1) {
							WriteOut=true;					
						}
						
	
						
						if (WriteOut || !Isomorph) {					
							
							fd.CalculatedFormula=CDKUtilities.calculateFormula(best_molecule);
							fd.Formula=CDKUtilities.calculateFormula(second_best_molecule);							
							
												
							SaveStructureToFile.CreateImageFile(best_molecule,CAS+"-1",folder+"/images");
							SaveStructureToFile.CreateImageFile(second_best_molecule,CAS+"-2",folder+"/images");
	//						System.out.println(CAS);
							
							count1++;
							
							this.WriteHTMLForDifference(folder,count1,(String)best_molecule.getProperty("Source"),(String)second_best_molecule.getProperty("Source"),fw,fd,ImageFileName1,ImageFileName2,false,false,"",counter);
							
							
							fw2.write("<tr>\n");
							fw2.write("\t<td>" + CAS2 + "</td>\n");
							fw2.write("\t<td>" + fd.StructureSource + "</td>\n");
	
							fw2.write("\t<td>" + fd.CalculatedFormula+ "</td>\n");
							
	
							fw2.write("\t<td>" + fd.Formula + "</td>\n");
	
	
	
							fw2.write("\t<td>"
									+ fd.CalculatedFormula.equals(fd.Formula)
									+ "</td>\n");
	
							fw2.write("</tr>\n");
	
							fw2.flush();
							
						}
	
						counter++;
						
						if (counter % 100 == 0) System.out.println(counter);
	
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
	
					
					
					
				}// end while true;
			
	//			System.out.println(counter);
				
				fw.write("</html>\r\n");
				fw2.write("</table></html>\r\n");			
				
				
				br.close();
				fw.close();
				fw2.close();
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}

		
		/**
		 * Loops through list of chemicals with "Other" structures and compares to the other sources
		 *
		 */
	public void CompareStructuresForChemicalsWithOtherStructure() {

		this.MolSrc = "Folder";

		DescriptorFactory df = new DescriptorFactory(false);
//		MyHydrogenAdder myHA = new MyHydrogenAdder();
		chemicalcompare cc = new chemicalcompare();

		String DoCAS="81340-58-1";
//		String comparemethod = "Fingerprinter";
		 String comparemethod="2D-Descriptors";
//		 String comparemethod="UIT";

		String structuredatafilepath = "ToxPredictor/chemidplus/parse/StructureData-12-03-09.txt";
		String structuredata2filepath="ToxPredictor/chemidplus/parse/StructureData2-12-03-09.txt";
		
		String outputFolder = "ToxPredictor/chemidplus/compare/OtherTest";

		File OtherFolder = new File("ToxPredictor/structures/Other");
		File[] files = OtherFolder.listFiles();

		// *****************************************************************
		//Following code sorts mol files by CAS number where length of CAS is taken into account:
		Hashtable ht=new Hashtable();
		for (int i = 0; i < files.length; i++) {
			File file=files[i];
			String filename=file.getName();
			String key= filename.substring(0, filename.indexOf("."));
			while (key.length()<11) key="0"+key;
			ht.put(key, file);
		}
		Vector v=new Vector(ht.keySet());
		Collections.sort(v);
		
	    for ( Enumeration e = v.elements(); e.hasMoreElements();) {
			// 	retrieve the object_key
			String object_key = (String) e.nextElement();
			// 	retrieve the object associated with the key
			File object_val = (File) ht.get ( object_key );
//			System.out.println(object_val.getName());
	    }
		// *****************************************************************
		
		
		
		
		File ffolder = new File(outputFolder);
		if (!ffolder.exists())
			ffolder.mkdir();

		File ImageFolder = new File(outputFolder + "/images");
		if (!ImageFolder.exists())
			ImageFolder.mkdir();

		try {

			FileWriter fw = new FileWriter(outputFolder + "/" + "compare.html");
			FileWriter fw2 = new FileWriter(outputFolder + "/" + "compare_manyisomers.html");
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data" + "\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare structures - Good data" + "\r\n");
			fw2.write("</title></head>\r\n");


			int counter = 0;
			int count1 = 0;

			java.util.Date startDate =new Date();
			startDate.setYear(109);//2009
			startDate.setMonth(6);
			startDate.setDate(1);
			
			
			LinkedList headerList=this.LookupLineInTextFile(structuredata2filepath, "CAS");
			
		    for ( Enumeration en = v.elements(); en.hasMoreElements();) {
				// 	retrieve the object_key
				String object_key = (String) en.nextElement();
				// 	retrieve the object associated with the key
				File file = (File) ht.get ( object_key );


				try {

					String filename = file.getName();

					String CAS = filename.substring(0, filename.indexOf("."));
					
					long date=file.lastModified();
					java.util.Date currentDate=new java.util.Date(date);
					
//					System.out.println(startDate+"\t"+currentDate);
					
//					if (currentDate.before(startDate)) continue;
					
					if (!CAS.equals(DoCAS)) continue;
					
					System.out.println(CAS);

					

					
					LinkedList structureData2List=this.LookupLineInTextFile(structuredata2filepath, CAS);
					
					Hashtable htStructureData2=new Hashtable();
					
					if (structureData2List!=null) {
						for (int i=0;i<headerList.size();i++) {
							htStructureData2.put(headerList.get(i), structureData2List.get(i));
//							System.out.println(CAS+"\t"+headerList.get(i)+"\t"+structureData2List.get(i));
						}
					}
					
					String Validated="false";
					if (structureData2List!=null) {
						Validated=(String)structureData2List.get(5);						
					}
					if (CAS == null)
						break;

					FileData fd = new FileData();
					fd.CAS = CAS;

					this.CheckForMolFiles2(fd);

					int molcount = 0;

					if (fd.HaveNIH3DMolFile)
						molcount++;
					if (fd.HaveDougDB3DMolFile)
						molcount++;
					if (fd.HaveNIST2DMolFile)
						molcount++;
					if (fd.HaveNIST3DMolFile)
						molcount++;
					if (fd.HaveIndexNet2DMolFile)
						molcount++;
					if (fd.HaveChemidplus3DMolFile)
						molcount++;
					if (fd.HaveDSSTox2DMolFile)
						molcount++;
					if (fd.HaveName2DMolFile)
						molcount++;
					if (fd.HaveSmiles)
						molcount++;
					if (fd.HaveOther)
						molcount++;

//					if (molcount < 2)
//						System.out.println(CAS + "\t" + fd.StructureSource
//								+ "\t" + molcount);

					// System.out.println(CAS+"\t"+fd.HaveOther);

					AtomContainerSet AtomContainerSet = this.getAtomContainers2(fd);

//					String eval = this.EvaluateMolecules(AtomContainerSet);
//
//					if (!eval.equals("OK")) {
//						System.out.println(CAS + "\t" + eval + "\n");
//						continue;
//					}

					if (AtomContainerSet.getAtomContainerCount() == 0) {
						System.out.println(CAS + "\tNo structure\n");
						continue;
					}

					String CAS2 = CAS;
					while (CAS2.length() < 12) {
						CAS2 = "0" + CAS2;
					}

					if (AtomContainerSet.getAtomContainerCount() == 1) {
						// System.out.println(CAS);
						count1++;

						IAtomContainer molecule0 = AtomContainerSet.getAtomContainer(0);
						AtomContainerSet AtomContainerSet1 = new AtomContainerSet();
						AtomContainerSet1.addAtomContainer(molecule0);
						ArrayList MasterList = new ArrayList();
						MasterList.add(AtomContainerSet1);

						this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
						
					} else {

						ArrayList MasterList = this.CompareChemicalsInList(
								AtomContainerSet, comparemethod);
						// System.out.println("done comparing chemicals in list");

//						if (MasterList.size() > 1
//								|| AtomContainerSet.getAtomContainerCount() == 1) {
							count1++;
//							this.WriteHTMLForDifference2(outputFolder,
//									MasterList, fw, fd, count1,
//									structuredatafilepath);
							this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
							if (MasterList.size()>3) {
								this.WriteHTMLForDifference2(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath,htStructureData2,Validated);
							}
//						}
					}

					counter++;

//					if (counter % 100 == 0)
//						System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

			}// end for loop

			// System.out.println(counter);

			fw.write("</html>\r\n");
			fw.close();
			
			fw2.write("</html>\r\n");
			fw2.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
		
	//breaks large compare web page into smaller pages so can be loaded in firefox
	public void breakUpCompareWebPage(String folder,String filename) {
		
//		String set="additional kow chemicals";
//		String folder="ToxPredictor/chemidplus/compare/"+set;
		
		String filepath=folder+"/"+filename;
		
		int numChemicalsPerFile=100;
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			for (int i=1;i<=3;i++) br.readLine();
			
			boolean stop=false;
			
			int counter=0;
			
			FileWriter fw=new FileWriter(folder+"/compare1.html");
			
			while (true) {
			
//				counter++;
				
				if (counter%numChemicalsPerFile==0) {
					fw.close();
					int filenum=counter/numChemicalsPerFile+1;
					fw=new FileWriter(folder+"/compare"+filenum+".html");
				}
				
//				System.out.println(counter);
				
				int isomorphCount=0;
				
				Vector <String>record=new Vector<String>();
				
				while (true) {
					String Line=br.readLine();
					
					if (Line==null) {
						stop=true;
						break;
					}
					
					if (Line.indexOf("Isomorph ")>-1){
						isomorphCount++;
					}
					record.add(Line);
//					fw.write(Line+"\r\n");
				
					if (Line.equals("</table><br><br>")) break;
				}
				
				if (isomorphCount>=1) {
					counter++;
					for (int i=0;i<record.size();i++) {
						fw.write(record.get(i)+"\r\n");
					}
					System.out.println(counter+"\t"+isomorphCount);
				}
				
				if (stop) break;

			}
			
			br.close();
			fw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	/**
	 * Loops through list of cas number of good chemicals then compares all structures
	 *
	 */
	public void CompareStructuresForGoodDataChemicals2(String CASFile,String CASFolder,String outputFolder,String structuredatafilepath,String comparemethod) {
		
		this.MolSrc="Folder";
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();

//		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
//		String comparemethod="UIT";

		try {

//			String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData.txt";
//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - CAS revised list.txt";
//			String folder="ToxPredictor/chemidplus/compare/GoodData-Revised-"+comparemethod;
			
//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - extra CAS list.txt";
//			String folder="ToxPredictor/chemidplus/compare/Good Data - extra CAS list-"+comparemethod;

//			String CASfile="ToxPredictor/chemidplus/compare/CAS list for fhm chemicals not in chemidplus ld50 database.txt";
//			String CASfile="ToxPredictor/chemidplus/compare/CAS list for fhm2.txt";

//			String CASfile="ToxPredictor/chemidplus/parse/CAS Numbers for additional IGC50 chemicals.txt";
//			String folder="ToxPredictor/chemidplus/compare/IGC50";
			
//			String CASfile="ToxPredictor/chemidplus/compare/CAS list for fhm3.txt";
//			String folder="ToxPredictor/chemidplus/compare/fhm3";
			
			String pathCASFile=CASFolder+"/"+CASFile;
			BufferedReader br = new BufferedReader(new FileReader(pathCASFile));
						
			
			File ffolder=new File(outputFolder);
			if (!ffolder.exists()) ffolder.mkdir();
			
			File ImageFolder=new File(outputFolder+"/images");
			if (!ImageFolder.exists()) ImageFolder.mkdir();
					
			
			FileWriter fw=new FileWriter(outputFolder+"/"+"compare.html");
			FileWriter fw2=new FileWriter(outputFolder+"/"+"formulacompare.html");
			FileWriter fw3=new FileWriter(outputFolder+"/"+"formulacompare.txt");
			FileWriter fw4=new FileWriter(outputFolder+"/"+"notgood.txt");
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data"+"\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas- Good data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Chemidplus Formula</th>\n");
			for (int i=1;i<=9;i++) {
				fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
			}
			fw2.write("</tr>\n");
			
			fw3.write("CAS\tChemidplus Formula\t");
			for (int i=1;i<=9;i++) {
				fw3.write("Isomorph "+i+"\t\t");		
			}
			fw3.write("\n");
			fw3.flush();
			
			int counter = 0;
			int count1=0;

			while (true) {
//			while (counter<50) {
				try {

					String CAS=br.readLine();
					
					
					
					if (CAS==null) {
//						System.out.println("break!");
						break;
					}
					
					FileData fd=new FileData();
					fd.CAS=CAS;

//					System.out.println(CAS);
					
					
//					if (!CAS.equals("74431-23-5")) continue;
//					if (!CAS.equals("17109-36-3")) continue;
					
					this.CheckForMolFiles2(fd);
//					System.out.println("here0");
					
					
					int molcount=0;
					
					if (fd.HaveNIH3DMolFile) molcount++;
					if (fd.HaveDougDB3DMolFile) molcount++;
					if (fd.HaveNIST2DMolFile) molcount++;
					if (fd.HaveNIST3DMolFile) molcount++;
					if (fd.HaveIndexNet2DMolFile) molcount++;
					if (fd.HaveChemidplus3DMolFile) molcount++;	
					if (fd.HaveChemidplus2DMolFile) molcount++;
					if (fd.HaveDSSTox2DMolFile) molcount++;
					if (fd.HaveName2DMolFile) molcount++;				
					if (fd.HaveSmiles) molcount++;

					if (molcount<2) {
						System.out.println(CAS+"\t"+fd.StructureSource+"\t"+molcount);
//						continue;
					}
					
//					System.out.println(CAS+"\t"+fd.HaveOther);
					
					
					AtomContainerSet atomContainerSet=this.getAtomContainers2(fd);
					
//					String eval=this.EvaluateMolecules(CAS,AtomContainerSet);

					boolean haveMixture=false;
					for (int i=0;i<atomContainerSet.getAtomContainerCount();i++) {
						AtomContainerSet ms = (AtomContainerSet) ConnectivityChecker
						.partitionIntoMolecules(atomContainerSet.getAtomContainer(i));

						if (ms.getAtomContainerCount() > 1) {
							haveMixture=true;
							System.out.println(CAS+"\t"+atomContainerSet.getAtomContainer(i).getProperty("Source")+"\tSalt");
							atomContainerSet.removeAtomContainer(i);
							i--;
						}
						
					}
					

					if (atomContainerSet.getAtomContainerCount()==0) {
						System.out.println(CAS+"\tno good structures (only 1 fragment)");
						fw4.write(CAS+"\tMixture/Salt\n");
						fw4.flush();
						continue;
					}
					
//					if (haveMixture) {
//						fw4.write(CAS+"\tMixture\n");
//						fw4.flush();
//						continue;

					String eval=this.EvaluateMolecules(CAS,atomContainerSet);

					if (!eval.equals("OK")) {
						fw4.write(CAS+"\t"+eval+"\n");
						fw4.flush();
//						System.out.println("here1a");
						continue;
					}
					
					if (atomContainerSet.getAtomContainerCount()==0) {
						fw4.write(CAS+"\tNo structure\n");
						continue;
					}

//					System.out.println("here1");
					
//					if (AtomContainerSet.getAtomContainerCount()<2) {
//						System.out.println("Only 1 structure for "+CAS);
//						counter++;
//						continue;
//					}
					
					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}


					
					if (atomContainerSet.getAtomContainerCount()==1) {
//						System.out.println("here2a");

//						System.out.println(CAS);
						count1++;

						IAtomContainer molecule0=atomContainerSet.getAtomContainer(0);		
						AtomContainerSet AtomContainerSet1=new AtomContainerSet();		
						AtomContainerSet1.addAtomContainer(molecule0);						
						ArrayList MasterList=new ArrayList();
						MasterList.add(AtomContainerSet1);
						
						
						this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath);
						this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
						this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
					} else {
//						System.out.println("here2b");
						ArrayList MasterList=this.CompareChemicalsInList(atomContainerSet,comparemethod);
//						System.out.println("done comparing chemicals in list");
						
						if (MasterList.size()>1 || atomContainerSet.getAtomContainerCount()==1) {
//							System.out.println(CAS);
							count1++;
//							System.out.print("here1-");
							this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath);
//							System.out.print("here2");
							this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
//							System.out.print("here3");
							this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
//							System.out.print("here4\n");
							
//							if (AtomContainerSet.getAtomContainerCount()==0) {
//								System.out.println("No structures for "+CAS);
//							}
						}
					}
//					System.out.println("here3");
					
					counter++;
					
					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				
				
				
			}// end while true;
		
//			System.out.println(counter);
			
			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			

			br.close();
			fw.close();
			fw2.close();
			fw3.close();
			fw4.close();


		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Loops through list of cas number of good chemicals then compares all structures
	 *
	 */
	public void CompareStructuresForSDF_ToOurStructures(String SDFFilePath,String structuredatafilepath,String outputFolder,String structuredata2filepath) {
		
		this.MolSrc="Folder";
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();

//		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
//		String comparemethod="UIT";
		String comparemethod=cc.methodHybrid;
		
		try {
			
			LinkedList headerList=this.LookupLineInTextFile(structuredata2filepath, "CAS");
			
			BufferedReader br = new BufferedReader(new FileReader(SDFFilePath));
			IteratingSDFReader mr=new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
			
			File ffolder=new File(outputFolder);
			if (!ffolder.exists()) ffolder.mkdir();
			
			File ImageFolder=new File(outputFolder+"/images");
			if (!ImageFolder.exists()) ImageFolder.mkdir();
					
			
			FileWriter fw=new FileWriter(outputFolder+"/"+"compare.html");
			FileWriter fw2=new FileWriter(outputFolder+"/"+"formulacompare.html");
			FileWriter fw3=new FileWriter(outputFolder+"/"+"formulacompare.txt");
			FileWriter fw4=new FileWriter(outputFolder+"/"+"notgood.txt");
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data"+"\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas- Good data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Chemidplus Formula</th>\n");
			for (int i=1;i<=9;i++) {
				fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
			}
			fw2.write("</tr>\n");
			
			fw3.write("CAS\tChemidplus Formula\t");
			for (int i=1;i<=9;i++) {
				fw3.write("Isomorph "+i+"\t\t");		
			}
			fw3.write("\n");
			fw3.flush();
			
			int counter = 0;
			int count1=0;

			while (mr.hasNext()) {
//			while (counter<50) {
				try {

					IAtomContainer molSDF=null;
					try {
						molSDF= mr.next();
					} catch(Exception e) {
						break;
					}
					
					FileData fd=new FileData();
					
					if (molSDF==null || molSDF.getProperties()==null) {
						break;
					}
					
					String CASField=MolFileUtilities.getCASField(molSDF);
					String CAS=(String)molSDF.getProperty(CASField);
					
					if (CAS==null) {
						System.out.println("CAS is null, #atoms="+molSDF.getAtomCount());
						break;
					}

//					String SDFname=(String)molSDF.getProperty("Name");
					
					molSDF=CDKUtilities.addHydrogens(molSDF);// dont need if use marvin to add hydrogens to SDF
					
					molSDF.setProperty("Source","SDF");
//					molSDF.setProperty("Source","SDF: "+SDFname);
					
					CAS=CAS.trim();
					fd.CAS=CAS;

					
//					if (!CAS.equals("54573-75-0")) continue;
					
//					if (!CAS.equals("81-11-8")) continue;
					
					if (CAS.indexOf("NOCAS")>-1) continue;
					
					this.CheckForMolFiles2(fd);
					
					int molcount=0;
					
					if (fd.HaveNIH3DMolFile) molcount++;
					if (fd.HaveDougDB3DMolFile) molcount++;
					if (fd.HaveNIST2DMolFile) molcount++;
					if (fd.HaveNIST3DMolFile) molcount++;
					if (fd.HaveIndexNet2DMolFile) molcount++;
					if (fd.HaveChemidplus3DMolFile) molcount++;	
					if (fd.HaveChemidplus2DMolFile) molcount++;//dont want to double count chemidplus?
					if (fd.HaveDSSTox2DMolFile) molcount++;
					if (fd.HaveName2DMolFile) molcount++;				
					if (fd.HaveSmiles) molcount++;
					if (fd.HaveOther) molcount++;

					System.out.println(fd.CAS+"\t"+fd.StructureSource+"\t"+molcount);
					
					AtomContainerSet AtomContainerSet=this.getAtomContainers2(fd);
					AtomContainerSet.addAtomContainer(molSDF);
					
					String eval=this.EvaluateMolecules(CAS,AtomContainerSet);
					
					if (!eval.equals("OK")) {
						fw4.write(CAS+"\t"+eval+"\n");
						fw4.flush();
						continue;
					}
					
					if (AtomContainerSet.getAtomContainerCount()==0) {
						fw4.write(CAS+"\tNo structure\n");
						continue;
					}
					
					
//					if (AtomContainerSet.getAtomContainerCount()<2) {
//						System.out.println("Only 1 structure for "+CAS);
//						counter++;
//						continue;
//					}
					
					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}

					//****************************************
					LinkedList structureData2List=this.LookupLineInTextFile(structuredata2filepath, CAS);
					
					
//					System.out.println(structureData2List.size());
					
					Hashtable htStructureData2=new Hashtable();
					
					if (structureData2List!=null) {
						for (int i=0;i<headerList.size();i++) {
							htStructureData2.put(headerList.get(i), structureData2List.get(i));
							System.out.println(CAS+"\t"+headerList.get(i)+"\t"+structureData2List.get(i));
						}
					}
					String Validated="false";
					if (structureData2List!=null) {
						Validated=(String)structureData2List.get(5);						
					}
					// **********************************************

					if (AtomContainerSet.getAtomContainerCount()==1) {
//						System.out.println(CAS);
						count1++;

						IAtomContainer molecule0=AtomContainerSet.getAtomContainer(0);		
						AtomContainerSet AtomContainerSet1=new AtomContainerSet();		
						AtomContainerSet1.addAtomContainer(molecule0);						
						ArrayList MasterList=new ArrayList();
						MasterList.add(AtomContainerSet1);
						
						this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
						this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
						this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
					} else {
					
						ArrayList MasterList=this.CompareChemicalsInList(AtomContainerSet,comparemethod);
//						System.out.println("done comparing chemicals in list");
						
						if (MasterList.size()>1 || AtomContainerSet.getAtomContainerCount()==1) {
//							System.out.println(CAS);
							count1++;
//							System.out.print("here1-");
							this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
//							System.out.print("here2");
							this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
//							System.out.print("here3");
							this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
//							System.out.print("here4\n");
							
//							if (AtomContainerSet.getAtomContainerCount()==0) {
//								System.out.println("No structures for "+CAS);
//							}
						}
					}
					
					
					counter++;
					
					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				
				
				
			}// end while true;
		
//			System.out.println(counter);
			
			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			
			fw3.close();
			fw4.close();
			
			br.close();
			fw.close();
			fw2.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	
	/**
	 * Loops through list of cas number of good chemicals then compares all structures
	 * This version takes cas from header of each molecule in mol file (sdf was marvin converted smiles file which doesnt create cas field)
	 */
	public void CompareStructuresForSDF_ToOurStructures2(String SDFFilePath,String structuredatafilepath,String outputFolder,String structuredata2filepath) {
		
		this.MolSrc="Folder";
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();

//		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
//		String comparemethod="UIT";
		String comparemethod=cc.methodHybrid;
		
		try {
			
			LinkedList headerList=this.LookupLineInTextFile(structuredata2filepath, "CAS");
			
			BufferedReader br = new BufferedReader(new FileReader(SDFFilePath));
			IteratingSDFReader mr=new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
			
			File ffolder=new File(outputFolder);
			if (!ffolder.exists()) ffolder.mkdir();
			
			File ImageFolder=new File(outputFolder+"/images");
			if (!ImageFolder.exists()) ImageFolder.mkdir();
					
			
			FileWriter fw=new FileWriter(outputFolder+"/"+"compare.html");
			FileWriter fw2=new FileWriter(outputFolder+"/"+"formulacompare.html");
			FileWriter fw3=new FileWriter(outputFolder+"/"+"formulacompare.txt");
			FileWriter fw4=new FileWriter(outputFolder+"/"+"notgood.txt");
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data"+"\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas- Good data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Chemidplus Formula</th>\n");
			for (int i=1;i<=9;i++) {
				fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
			}
			fw2.write("</tr>\n");
			
			fw3.write("CAS\tChemidplus Formula\t");
			for (int i=1;i<=9;i++) {
				fw3.write("Isomorph "+i+"\t\t");		
			}
			fw3.write("\n");
			fw3.flush();
			
			int counter = 0;
			int count1=0;

			while (mr.hasNext()) {
//			while (counter<50) {
				try {

					
					IAtomContainer molSDF=mr.next();
					
					FileData fd=new FileData();
					
					if (molSDF==null || molSDF.getProperties()==null) {
						break;
					}
					
					
					String CAS=(String)molSDF.getProperty("header");
					
					if (CAS==null) {
						System.out.println("CAS is null, #atoms="+molSDF.getAtomCount());
						break;
					}

					if (CAS.equals("57-24-9")) continue;
					
					
					molSDF=CDKUtilities.addHydrogens(molSDF);// dont need if use marvin to add hydrogens to SDF

//					String SDFname=(String)molSDF.getProperty("Name");
//					molSDF.setProperty("Source","SDF: "+SDFname);

					molSDF.setProperty("Source","SDF");

					CAS=CAS.trim();
					fd.CAS=CAS;

//					if (!CAS.equals("81-11-8")) continue;
					
					if (CAS.indexOf("NOCAS")>-1) continue;
					
					this.CheckForMolFiles2(fd);
					
					int molcount=0;
					
					if (fd.HaveNIH3DMolFile) molcount++;
					if (fd.HaveDougDB3DMolFile) molcount++;
					if (fd.HaveNIST2DMolFile) molcount++;
					if (fd.HaveNIST3DMolFile) molcount++;
					if (fd.HaveIndexNet2DMolFile) molcount++;
					if (fd.HaveChemidplus3DMolFile) molcount++;	
					if (fd.HaveChemidplus2DMolFile) molcount++;//dont want to double count chemidplus?
					if (fd.HaveDSSTox2DMolFile) molcount++;
					if (fd.HaveName2DMolFile) molcount++;				
					if (fd.HaveSmiles) molcount++;
					if (fd.HaveOther) molcount++;

					System.out.println(fd.CAS+"\t"+fd.StructureSource+"\t"+molcount);
					
					AtomContainerSet AtomContainerSet=this.getAtomContainers2(fd);
					AtomContainerSet.addAtomContainer(molSDF);
					
					String eval=this.EvaluateMolecules(CAS,AtomContainerSet);
					
					if (!eval.equals("OK")) {
						fw4.write(CAS+"\t"+eval+"\n");
						fw4.flush();
						continue;
					}
					
					if (AtomContainerSet.getAtomContainerCount()==0) {
						fw4.write(CAS+"\tNo structure\n");
						continue;
					}
					
					
//					if (AtomContainerSet.getAtomContainerCount()<2) {
//						System.out.println("Only 1 structure for "+CAS);
//						counter++;
//						continue;
//					}
					
					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}

					//****************************************
					LinkedList structureData2List=this.LookupLineInTextFile(structuredata2filepath, CAS);
					
					Hashtable htStructureData2=new Hashtable();
					
					if (structureData2List!=null) {
						for (int i=0;i<headerList.size();i++) {
							htStructureData2.put(headerList.get(i), structureData2List.get(i));
//							System.out.println(CAS+"\t"+headerList.get(i)+"\t"+structureData2List.get(i));
						}
					}
					String Validated="false";
					if (structureData2List!=null) {
						Validated=(String)structureData2List.get(5);						
					}
					// **********************************************

					if (AtomContainerSet.getAtomContainerCount()==1) {
//						System.out.println(CAS);
						count1++;

						IAtomContainer molecule0=AtomContainerSet.getAtomContainer(0);		
						AtomContainerSet AtomContainerSet1=new AtomContainerSet();		
						AtomContainerSet1.addAtomContainer(molecule0);						
						ArrayList MasterList=new ArrayList();
						MasterList.add(AtomContainerSet1);
						
						this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
						this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
						this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
					} else {
					
						ArrayList MasterList=this.CompareChemicalsInList(AtomContainerSet,comparemethod);
//						System.out.println("done comparing chemicals in list");
						
						if (MasterList.size()>1 || AtomContainerSet.getAtomContainerCount()==1) {
//							System.out.println(CAS);
							count1++;
//							System.out.print("here1-");
							this.WriteHTMLForDifference2(outputFolder,MasterList,fw,fd,count1,structuredatafilepath,htStructureData2,Validated);
//							System.out.print("here2");
							this.WriteHTMLForDifference3(outputFolder,MasterList,fw2,fd,count1,structuredatafilepath);
//							System.out.print("here3");
							this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
//							System.out.print("here4\n");
							
//							if (AtomContainerSet.getAtomContainerCount()==0) {
//								System.out.println("No structures for "+CAS);
//							}
						}
					}
					
					
					counter++;
					
					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				
				
				
			}// end while true;
		
//			System.out.println(counter);
			
			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			
			fw3.close();
			fw4.close();
			
			br.close();
			fw.close();
			fw2.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Loops through chemicals in StructureData3 and sees if new Marvin converted smiles from latest Smiles DB (5-9-06) matches the RevisedSource
	 *
	 */
	public void CompareSmilesToBestStructure() {
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();
		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
		

		try {

			
//			String CASfile="ToxPredictor/chemidplus/compare/All Structure3 w revised source.txt";
//			String folder="ToxPredictor/chemidplus/compare/Smiles";
			
//			String CASfile="ToxPredictor/chemidplus/compare/All Structure3 new incorrect smiles structures.txt";
//			String folder="ToxPredictor/chemidplus/compare/Smiles-NewIncorrect";
			
			String CASfile="ToxPredictor/chemidplus/compare/All Structure3 corrected smiles.txt";
			String folder="ToxPredictor/chemidplus/compare/Smiles-NewCorrect";
			
			String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData.txt";
			
			
			BufferedReader br = new BufferedReader(new FileReader(CASfile));
						
			
			File ffolder=new File(folder);
			if (!ffolder.exists()) ffolder.mkdir();
			
			File ImageFolder=new File(folder+"/images");
			if (!ImageFolder.exists()) ImageFolder.mkdir();
						
			
			FileWriter fw=new FileWriter(folder+"/"+"compare.html");
			FileWriter fw2=new FileWriter(folder+"/"+"formulacompare.html");
			FileWriter fw3=new FileWriter(folder+"/"+"formulacompare.txt");
			FileWriter fw4=new FileWriter(folder+"/"+"notgood.txt");
			FileWriter fw5=new FileWriter(folder+"/"+"different.txt");
			
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data"+"\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas- Good data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Chemidplus Formula</th>\n");
			for (int i=1;i<=9;i++) {
				fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
			}
			fw2.write("</tr>\n");
			
			fw3.write("CAS\tChemidplus Formula\t");
			for (int i=1;i<=9;i++) {
				fw3.write("Isomorph "+i+"\t\t");		
			}
			fw3.write("\n");
			fw3.flush();
			
			int counter = 0;
			int count1=0;

			while (true) {
//			while (counter<50) {
				try {

					String Line=br.readLine();
					
					if (Line==null) break;
					
					String CAS=Line.substring(0,Line.indexOf("\t"));
					String RevisedSrc=Line.substring(Line.indexOf("\t")+1,Line.length());
					
					if (RevisedSrc.equals("Smiles")) continue;
					if (RevisedSrc.equals("N/A")) continue;
					
					FileData fd=new FileData();
					fd.CAS=CAS;
//					System.out.println(CAS+"\t"+RevisedSrc);
					
					AtomContainerSet AtomContainerSet=new AtomContainerSet();
					
					
					
					fd.HaveSmiles=this.HaveMolFileInJar(this.SmilesJarMolFolder+"/"+CAS+".mol");
					fd.Smiles = lookup.LookUpSmiles(CAS);
					
					if (!fd.HaveSmiles) continue;
					try {
						String Source=RevisedSrc;
						IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
						molecule.setProperty("Source",Source);
						if (molecule != null) AtomContainerSet.addAtomContainer(molecule);

					} catch (Exception e) {
						System.out.println("Cant load molecule for "+CAS+", source ="+RevisedSrc);
						continue;
					}

					try {
						String Source="Smiles";
						IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
						molecule.setProperty("Source",Source+" "+fd.Smiles);
					
						if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
					} catch (Exception e) {
						System.out.println("Cant load molecule for "+CAS+", source =Smiles");
						continue;
					}
						
						
					
					String eval=this.EvaluateMolecules(CAS,AtomContainerSet);
					
					if (!eval.equals("OK")) {
						fw4.write(CAS+"\t"+eval+"\n");
						fw4.flush();
						continue;
					}
					
					if (AtomContainerSet.getAtomContainerCount()==0) {
						fw4.write(CAS+"\tNo structure\n");
						continue;
					}

					
					
					System.out.println(CAS);
					

					if (CAS.equals("2278-50-4")) continue;
//					if (true) continue;

					
					
					ArrayList MasterList=this.CompareChemicalsInList(AtomContainerSet,comparemethod);

//					if (MasterList.size()>1 || AtomContainerSet.getAtomContainerCount()==1) {
//							System.out.println(CAS);
							count1++;
//							System.out.print("here1-");
							this.WriteHTMLForDifference2(folder,MasterList,fw,fd,count1,structuredatafilepath);
//							System.out.print("here2");
							this.WriteHTMLForDifference3(folder,MasterList,fw2,fd,count1,structuredatafilepath);
//							System.out.print("here3");
							this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
//							System.out.print("here4\n");
							fw5.write(CAS+"\r\n");
							fw5.flush();
//					}
					
					
					
					counter++;
					
					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				
				
				
			}// end while true;
		
//			System.out.println(counter);
			
			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			
			fw3.close();
			fw4.close();
			fw5.close();
			
			br.close();
			fw.close();
			fw2.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	
	public static String EvaluateMolecules(String CAS,AtomContainerSet ims) {
		
		for (int i=0;i<ims.getAtomContainerCount();i++) {
			
			IAtomContainer mol=ims.getAtomContainer(i);
			
			String Source=(String)mol.getProperty("Source");
//			String CAS=(String)mol.getProperty("CAS");
			
			for (int j=0; j<mol.getAtomCount();j++) {

				String var = mol.getAtom(j).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

//					System.out.println(CAS+"\t"+Source+"\tBad element\t"+var);
					return "Bad element";					
				}
			}
			
			if (IsSalt(mol)) {
//				System.out.println(CAS+"\t"+Source+"\tSalt");
				return "Salt";
			}
			
			
			
		}
		
		
		
		return "OK";
		
	}
	
	
	
	/**
	 * Compares for structures that were generated from SMILES using CDK and Marvin View
	 * compares all structures in LD50 database w/smiles
	 */
	public void CompareStructuresForSmiles() {
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();
		AllRingsFinder arf = new AllRingsFinder();
		arf.setTimeout(60000);


		try {

//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - CAS revised list.txt";
//			String folder="ToxPredictor/chemidplus/compare/GoodData-Revised-"+comparemethod;
			
//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - extra CAS list.txt";
			String folder="ToxPredictor/chemidplus/compare/Compare CAS generated structures";
			
			String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData.txt";
			
//			BufferedReader br = new BufferedReader(new FileReader(CASfile));
			String structureFolder="ToxPredictor/chemidplus/structures";
			File structureFolder1=new File(structureFolder+"/Smiles");
			File structureFolder2=new File(structureFolder+"/Smiles_Marvin_withH");			
			
			File ffolder=new File(folder);
			if (!ffolder.exists()) ffolder.mkdir();
			
			File ImageFolder=new File(folder+"/images");
			if (!ImageFolder.exists()) ImageFolder.mkdir();
			
			
			
			FileWriter fw=new FileWriter(folder+"/"+"compareGoodData.html");
			FileWriter fw2=new FileWriter(folder+"/"+"formulacompareGoodData.html");
			FileWriter fw3=new FileWriter(folder+"/"+"formulacompareGoodData.txt");
			
			fw.write("<html><head><title>\r\n");
			fw.write("Compare structures - Good data"+"\r\n");
			fw.write("</title></head>\r\n");
			
			fw2.write("<html><head><title>\r\n");
			fw2.write("Compare formulas- Good data"+"\r\n");
			fw2.write("</title></head>\r\n");

			fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw2.write("\t<th>CAS</th>\n");
			fw2.write("\t<th>Chemidplus Formula</th>\n");
			for (int i=1;i<=9;i++) {
				fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
			}
			fw2.write("</tr>\n");
			
			fw3.write("CAS\tChemidplus Formula\t");
			for (int i=1;i<=9;i++) {
				fw3.write("Isomorph "+i+"\t\t");		
			}
			fw3.write("\n");
			fw3.flush();
			
			int counter = 0;
			int count1=0;

			File [] files1=structureFolder1.listFiles();
			
			
			String parent=files1[0].getParent();
			
			for (int i=0;i<files1.length;i++) {
				
//				System.out.println(files1[i].getName());
				if (i%100==0) System.out.println(i);
				
				String CAS=files1[i].getName();
				
				CAS=CAS.substring(0,CAS.indexOf("_"));
				
				FileData fd=new FileData();
				fd.CAS=CAS;
				
				File molfileCDK=files1[i];
				File molfileMarvin=new File(structureFolder2.getAbsolutePath()+"/"+CAS+"_Smiles_Marvin.mol");

				if (!molfileMarvin.exists()) {
					System.out.println(CAS+"-Marvin missing");
					continue;
				}
				
				IAtomContainer moleculeCDK=this.LoadChemicalFromMolFile(CAS+"_Smiles",structureFolder1.getAbsolutePath() );
				IAtomContainer moleculeMarvin=this.LoadChemicalFromMolFile(CAS+"_Smiles_Marvin", structureFolder2.getAbsolutePath());				

				moleculeCDK.setProperty("Source","CDK");
				moleculeMarvin.setProperty("Source","Marvin");
				
				AtomContainerSet AtomContainerSet=new AtomContainerSet();				
				AtomContainerSet.addAtomContainer(moleculeCDK);
				AtomContainerSet.addAtomContainer(moleculeMarvin);

				ArrayList MasterList=this.CompareChemicalsInList(AtomContainerSet, "Fingerprinter");
				
				
				if (MasterList.size()>1 || AtomContainerSet.getAtomContainerCount()==1) {				
					count1++;
					this.WriteHTMLForDifference2(folder,MasterList,fw,fd,count1,structuredatafilepath);
					this.WriteHTMLForDifference3(folder,MasterList,fw2,fd,count1,structuredatafilepath);
					this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
				}
				
				
				
			}// end while true;
		
//			System.out.println(counter);
			
			fw.write("</html>\r\n");
			fw2.write("</table></html>\r\n");			
			fw3.close();
			
			
			fw.close();
			fw2.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	/**
		 * loops through chemicals in DSSTox SDF file and compares to best available structure
		 *
		 */
		public void CompareStructuresFromDSSToxToBestStructure() {
			DescriptorFactory df=new DescriptorFactory(false);
			
			String sdffolder="ToxPredictor/SDF Files";
			
			String filename="DSSToxMaster_v1a_8804_10Apr2006.sdf";		
			
			File SDFfilename=new File(sdffolder+"/"+filename);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
			chemicalcompare cc=new chemicalcompare();
			
			try {
	
				BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
				
				String folder="ToxPredictor/chemidplus/compare/All_DSSTox";
				
				FileWriter fw=new FileWriter(folder+"/"+"compare_All_DSSTOX.html");
				FileWriter fw2=new FileWriter(folder+"/"+"formulacompare_All_DSSTOX.html");			
				FileWriter fw3=new FileWriter(folder+"/"+"compare_All_DSSTOX_nosalt.html");
				FileWriter fw4=new FileWriter(folder+"/"+"formulacompare_All_DSSTOX_singlechemical_nosalt.html");
				
				
				fw.write("<html><head><title>\r\n");
				fw.write("Compare structures-"+SDFfilename.getName()+"\r\n");
	//			fw.write("<STYLE TYPE=\"text/css\">\r\n");
	//			fw.write("P.breakhere {page-break-after: always}\r\n");
	//			fw.write("</STYLE>\r\n");
			     
				fw.write("</title></head>\r\n");
				
						
				
				fw2.write("<html><head><title>\r\n");
				fw2.write("Compare formulas-"+SDFfilename.getName()+"\r\n");
				fw2.write("</title></head>\r\n");
				
				fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
				fw2.write("<tr bgcolor=\"#D3D3D3\">\n");		
				fw2.write("\t<th>CAS</th>\n");
				fw2.write("\t<th>Src</th>\n");
				fw2.write("\t<th>CalculatedFormula</th>\n");
				fw2.write("\t<th>DSSToxFormula</th>\n");
				fw2.write("\t<th>Description</th>\n");
				fw2.write("</tr>\n");
				fw2.flush();
				
				fw4.write("<html><head><title>\r\n");
				fw4.write("Compare formulas-"+SDFfilename.getName()+"-No Salt, Single chemical\r\n");
				fw4.write("</title></head>\r\n");
				
				fw4.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");			
				fw4.write("<tr bgcolor=\"#D3D3D3\">\n");		
				fw4.write("\t<th>CAS</th>\n");
				fw4.write("\t<th>Src</th>\n");
				fw4.write("\t<th>CalculatedFormula</th>\n");
				fw4.write("\t<th>DSSToxFormula</th>\n");
				fw4.write("\t<th>Description</th>\n");
				fw4.write("</tr>\n");
				fw4.flush();
				
				IteratingSDFReader mr = new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
	
				String[] strData = new String[400];
	
				IAtomContainer mSDF; // molecule from SDF File
				
				int counter = 0; // total chemical count
				int count1=0; // count of discrepancies in isomorphism
				int count3=0; // count of discrepancies in isomorphism
				
				while (mr.hasNext()) {
	
					try {
	
						mSDF = mr.next();
						counter++;
						
	
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
	
					// System.out.println(m.getAtomCount());
	

	
					String CAS = (String)mSDF.getProperty("TestSubstance_CASRN");
	
					if (CAS.equals("NOCAS")) continue;
					
//					System.out.println(CAS);
					
					String Formula=(String)mSDF.getProperty("STRUCTURE_Formula");
	//				if (!CAS.equals("6109-97-3")) continue;
					
					String NAME = "DSSTox";
					
					String TestSubstance_Description=(String)mSDF.getProperty("TestSubstance_Description");
					
					NAME=NAME+" ("+TestSubstance_Description+")";
	
					FileData fd=new FileData();
					
					fd.CAS=CAS;
					
					if (CAS.equals("83-34-1")) System.out.println("here1");
					
					
					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}
	
								
					this.CheckForMolFiles2(fd);				
					IAtomContainer molecule=this.GetBestMoleculeNoDSSTox(fd); // molecule from mol files or from smiles
					
	//				System.out.println(fd.FormulaCheckSource);
					
					if (molecule==null) continue;
					
					df.Normalize(mSDF);
					df.Normalize(molecule);
												
					
					//if (this.GetHydrogenCount(m)!=this.GetHydrogenCount(molecule)) {
	//					CDKUtilities.AddHydrogens(molecule);
	//					CDKUtilities.AddHydrogens(mSDF);
					IAtomContainer mSDF2=CDKUtilities.addHydrogens(mSDF);
					IAtomContainer molecule2=CDKUtilities.addHydrogens(molecule);
					
					//}
	
					
								
					boolean IsSalt1=this.IsSalt(molecule2);
					boolean IsSalt2=this.IsSalt(mSDF2);
					
					
					String ImageFileName1=fd.CAS+".png";
					String ImageFileName2=fd.CAS+"_DSSTox"+".png";
	
					
					if (mSDF2==null || molecule2==null) {
						
						SaveStructureToFile.CreateImageFile(molecule,CAS,folder+"/images");
						SaveStructureToFile.CreateImageFile(mSDF,CAS+"_DSSTox",folder+"/images");
						
						count1++;
						this.WriteHTMLForDifference(folder,count1,"From best structure","From DSSTOX",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
						continue;
						
					}
					
					boolean WriteOut=false;
					boolean Isomorph=false;
					
					try {
					
						Isomorph=cc.isIsomorphFingerPrinter(molecule2,mSDF2);
					} catch (Exception ex1) {
						WriteOut=true;					
					}
					
	
					
					
					if (WriteOut || !Isomorph) {					
						
						fd.CalculatedFormula=CDKUtilities.generateFormula(molecule2);
						
						try {
							fd.Formula=CDKUtilities.fixFormula(Formula);
						} catch (Exception e) {
							fd.Formula=Formula;
						}
						

											
						SaveStructureToFile.CreateImageFile(molecule2,CAS,folder+"/images");
						SaveStructureToFile.CreateImageFile(mSDF2,CAS+"_DSSTox",folder+"/images");
	//					System.out.println(CAS);
						
						count1++;
						this.WriteHTMLForDifference(folder,count1,"From best structure","From DSSTOX",fw,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
	
						if (!fd.CalculatedFormula.equals(fd.Formula)) {
							fw2.write("<tr>\n");								
							fw2.write("\t<td>"+CAS2+"</td>\n");
							fw2.write("\t<td>"+fd.StructureSource+"</td>\n");
							
							if (IsSalt1) {
								fw2.write("\t<td><font color=\"red\">"+fd.CalculatedFormula+"</font></td>\n");
							} else {
								fw2.write("\t<td>"+fd.CalculatedFormula+"</td>\n");	
							}
							
							if (IsSalt2) {
								fw2.write("\t<td><font color=\"red\">"+fd.Formula+"</font></td>\n");							
							} else {
								fw2.write("\t<td>"+fd.Formula+"</td>\n");
							}
						
							fw2.write("\t<td>"+TestSubstance_Description+"</td>\n");
							fw2.write("</tr>\n");
							
							fw2.flush();
						}
						
						if (!IsSalt1 &&  !IsSalt2 && TestSubstance_Description.equals("single chemical compound")) {

							count3++;
							this.WriteHTMLForDifference(folder,count3,"From best structure","From DSSTOX",fw3,fd,ImageFileName1,ImageFileName2,IsSalt1,IsSalt2,NAME,counter);
							
							if (!fd.CalculatedFormula.equals(fd.Formula)) {
								fw4.write("<tr>\n");								
								fw4.write("\t<td>"+CAS2+"</td>\n");
								fw4.write("\t<td>"+fd.StructureSource+"</td>\n");
								fw4.write("\t<td>"+fd.CalculatedFormula+"</td>\n");
								fw4.write("\t<td>"+fd.Formula+"</td>\n");											
								fw4.write("\t<td>"+TestSubstance_Description+"</td>\n");
								fw4.write("</tr>\n");
							
								fw4.flush();
							}
						} // end if both not salts and single chemical compound
						
					} // end not isisomorph
					
					
					if (counter%100==0) System.out.println(counter);
				}// end while true;
			
				System.out.println(counter);
				
				fw.write("</html>\r\n");
				fw2.write("</table></html>\r\n");			
				fw4.write("</table></html>\r\n");
				
				br.close();
				fw.close();
				fw2.close();
				fw3.close();
				fw4.close();
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
		
		
		
		/**
	 * loops through chemicals in DSSTox SDF file and compares to best available structure
	 *
	 */
	public void ParseDSSTox() {
		DescriptorFactory df=new DescriptorFactory(false);
		
		String sdffolder="ToxPredictor/SDF Files";
		
		String filename="DSSToxMaster_v1a_8804_10Apr2006.sdf";		
		
		File SDFfilename=new File(sdffolder+"/"+filename);
		
		try {

			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
			
			String folder="ToxPredictor/chemidplus/compare/";
			
			FileWriter fw=new FileWriter(folder+"/"+"DSSTox.txt");
						
//			MDLReader mr = new MDLReader();

			int counter=0;
			
			
			Hashtable htfirst=null;
			
			
			for (int ii=1;ii<=10000;ii++) {

				try {

					ArrayList datalist=new ArrayList();
					
//					AtomContainer mSDF = mr.readMolecule(br);
					
					// read until > <					 
					String Line="";
					while ( true ) {
						 Line=br.readLine();
						 if (Line.indexOf(">  <")>-1) {
							 break;
						 }
					 }

					datalist.add(Line);
					
					
					// read until > <
					while ( true ) {
						 Line=br.readLine();
						 if (Line.indexOf("$$$$")>-1) {
							 break;
						 }
						 if (Line.equals("")) continue;
						 datalist.add(Line);						 
					 }
					
					
//					for (int i=0;i<datalist.size();i++) {
//						System.out.println(datalist.get(i));
//					}
					
					// now go through array and get data:
					
					int count=0;
					
					Hashtable ht=new Hashtable();
					
					while (true) {
						
						String key=(String)datalist.get(count);
						
						key=key.substring(key.indexOf("<"),key.length());
						key=key.substring(1,key.length()-1);
						
						System.out.println(count+"\t"+key);
						
						String val="";
						
						boolean Stop=false;
						
						while (true) {
							
							count++;
							
							if (count==datalist.size()) {
								Stop=true;
								break;
							}
							
							String bob=(String)datalist.get(count);
							
//							System.out.println(bob);
							
							if (bob.indexOf(">  <")==-1) {
								val+=bob;
							} else if (bob.indexOf(">  <")==0){
								break;
							} 
							
						}
						
//						System.out.println(count+"\t"+key+"\t"+val);
						ht.put(key,val);
						if (Stop) break;
																		
					}
					
					if (counter==0) {
						
						htfirst=ht;
						
						for ( Enumeration e = htfirst.keys() ; e.hasMoreElements() ; ) {
							fw.write((String) e.nextElement()+"\t");						
						}
						fw.write("\n");
						fw.flush();
					}
					
					
					for ( Enumeration e =htfirst.keys() ; e.hasMoreElements() ; ) {

						// 	retrieve the object_key
						String key = (String) e.nextElement();
						// 	retreve the object associated with the key
						String val = (String) ht.get ( key );
						
						fw.write((String) val+"\t");

					}
					fw.write("\n");
					fw.flush();

					counter++;
					
				} catch (Exception e) {
//					e.printStackTrace();
					
				}
		
				
			}// end while true;
		
					
			br.close();
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	
	private void WriteHTMLForDifference(String folder,int count,String namecol1,String namecol2,FileWriter fw,FileData fd,String ImageFileName1, String ImageFileName2,boolean IsSalt1,boolean IsSalt2,String Name,int Counter) {
		try {
//			System.out.println("here");
			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw.write("<caption><big><font color=\"blue\">"+fd.CAS+" ("+count+")</font></big></caption>\n");
			
			
			fw.write("<tr bgcolor=\"#D3D3D3\">\n");		
		fw.write("\t<th><br></th>\n");
		fw.write("\t<th>"+namecol1+"</th>\n");
		fw.write("\t<th>"+namecol2+"</th>\n");		
		fw.write("</tr>\n");
		
		fw.write("<tr>\n");
		fw.write("\t<td>Source</td>\n");
		fw.write("\t<td>"+fd.StructureSource+"</td>\n");
		fw.write("\t<td width=\"350\">"+Name+"</td>\n");
		fw.write("</tr>\n");
			

		fw.write("<tr>\n");
		fw.write("\t<td>Structure</td>\n");
		
		File image1=new File(folder+"/images/"+ImageFileName1);
		File image2=new File(folder+"/images/"+ImageFileName2);
		
		
		
		if (image1.exists()) {
			fw.write("\t<td><img src=\"images/"+ImageFileName1+"\"></td>\n");	
		} else {
			fw.write("\t<td>Could not draw structure</td>\n");
		}

		if (image2.exists()) {
			fw.write("\t<td><img src=\"images/"+ImageFileName2+"\"></td>\n");	
		} else {
			fw.write("\t<td>Could not draw structure</td>\n");
		}
		
		fw.write("</tr>\n");

		fw.write("<tr>\n");
		fw.write("\t<td>Formula</td>\n");
		
		
		if (IsSalt1) {
			fw.write("\t<td><font color=\"red\">"+fd.CalculatedFormula+"- <b>Salt</b>"+"</font></td>\n");
		} else {
			fw.write("\t<td>"+fd.CalculatedFormula+"</td>\n");	
		}
		
		if (IsSalt2) {
			fw.write("\t<td><font color=\"red\">"+fd.Formula+"- <b>Salt</b>"+"</font></td>\n");
		} else {
			fw.write("\t<td>"+fd.Formula+"</td>\n");	
		}
		
			
		fw.write("</tr>\n");
		
		
		
		fw.write("</table>\n");
		fw.write("<br><br>\n");
		
//		if (Counter%3==0 && Counter>2) {
//			fw.write("<P CLASS=\"breakhere\">\r\n");
//		}
		fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void WriteHTMLForDifference2(String outputfolder,ArrayList MasterList,FileWriter fw,FileData fd,int count,String structuredatafilepath) {
//			String folder="ToxPredictor/chemidplus/compare/GoodData";
			
			
			LinkedList infoList=this.LookupLineInTextFile(structuredatafilepath,fd.CAS);
			
			
			String ChemidplusFormula="";
			String ChemidplusName="";
			
			if (infoList!=null) {
				ChemidplusFormula=(String)infoList.get(4);
				ChemidplusName=(String)infoList.get(2);
				
				// if systematicname is unavailable use name:
				if (ChemidplusName==null || ChemidplusName.equals("") || ChemidplusName.equals("N/A")) {
					ChemidplusName=(String)infoList.get(1);	
				}

//				IAtomContainer mtemp=new AtomContainer();
//				
//				if (!ChemidplusFormula.equals("N/A")) {
//					ChemidplusFormula=CDKUtilities.generateHTMLFormula(mtemp);
//				}
			} else {
//				System.out.println(fd.CAS+"\tRecord missing");
			}
			
				
			
			try {
				
				fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
//				fw.write("<caption><big><font color=\"blue\">");
				fw.write("<caption><big>");
				
				fw.write(fd.CAS+" ("+count+") - ");
				
				if (infoList!=null) {
					fw.write(ChemidplusName+"<br>");
					fw.write("Formula = "+ChemidplusFormula+"<br>");
				}
//				fw.write("</font></big></caption>\n");
				fw.write("</big></caption>\n");
				
				fw.write("<tr bgcolor=\"#D3D3D3\">\n");
				
				for (int i=0;i<MasterList.size();i++) {
					fw.write("\t<th>Isomorph "+(i+1)+"</th>\n");		
				}
				fw.write("</tr>\n");
				
				fw.write("<tr>\n");
				for (int i=0;i<MasterList.size();i++) {
					AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
					
					fw.write("\t<td>");
					for (int j=0;j<AtomContainerSet.getAtomContainerCount();j++) {
						fw.write(AtomContainerSet.getAtomContainer(j).getProperty("Source")+"<br>");
					}
					fw.write("</td>\n");
					
				}
				fw.write("</tr>\n");
						
				fw.write("<tr>\n");
				for (int i=0;i<MasterList.size();i++) {
					AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
					
					IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
					
					File ImageFile=new File(outputfolder+"/images/"+fd.CAS+"_"+i+".png");
					
	//				System.out.println(ImageFile.exists());

					int size=400;
//					if (!ImageFile.exists())
						SaveStructureToFile.CreateImageFile(molecule,fd.CAS+"_"+i,outputfolder+"/images");
						fw.write("\t<td><img src=\"images/"+fd.CAS+"_"+i+".png"+"\"></td>\n");


//					
//						SaveStructureToFile.CreateScaledImageFile(molecule,
//								fd.CAS+"_"+i, outputfolder+"/images", false, false,
//								true, 30, size);
//					fw.write("\t<td><img src=\"images/"+fd.CAS+"_"+i+".png"+"\" height="+size+"></td>\n");	
				}
				fw.write("</tr>\n");
	
				
				fw.write("<tr>\n");
				for (int i=0;i<MasterList.size();i++) {
					AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
					
					IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
					
					String Formula=CDKUtilities.generateHTMLFormula(molecule);
					
					fw.write("\t<td>"+Formula+"</td>\n");
					
//					if (Formula.equals(ChemidplusFormula))
//						fw.write("\t<td>"+Formula+"</td>\n");
//					else
//						fw.write("\t<td><font color=\"red\">"+Formula+"</font></td>\n");
				}
				fw.write("</tr>\n");
	
				
				fw.write("</table><br><br>\n");
	//			
				
			
			fw.flush();
	
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	/** This version includes info from StructureData2
	 * 
	 * @param outputfolder
	 * @param MasterList
	 * @param fw
	 * @param fd
	 * @param count
	 * @param structuredatafilepath
	 * @param htStructureData2
	 * @param validated
	 */
	private void WriteHTMLForDifference2(String outputfolder,ArrayList MasterList,FileWriter fw,FileData fd,int count,String structuredatafilepath,Hashtable htStructureData2,String validated) {
//		String folder="ToxPredictor/chemidplus/compare/GoodData";
		
		String sourceCorrectStructure=(String)htStructureData2.get("SourceCorrectStructure");
		
		LinkedList infoList=this.LookupLineInTextFile(structuredatafilepath,fd.CAS);
		
		String ChemidplusFormula="";
		String ChemidplusName="";
		
		if (infoList!=null) {
			ChemidplusFormula=(String)infoList.get(4);
			ChemidplusFormula=CDKUtilities.fixFormula(ChemidplusFormula);
			ChemidplusName=(String)infoList.get(2);

		}
		
			
		
		try {
			
			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
//			fw.write("<caption><big><font color=\"blue\">");
			fw.write("<caption><big>");
			
			fw.write(fd.CAS+" ("+count+") - ");
			
			if (infoList!=null) {
				fw.write(ChemidplusName+"<br>");
				fw.write(ChemidplusFormula+"<br>");
			}
			fw.write("Validated=");
			if (validated.equals("true")) {
				fw.write("<font color=green>true</font><br>");
			} else {
				fw.write("<font color=red>false</font><br>");
			}
			fw.write("SourceCorrectStructure="+sourceCorrectStructure);
			
//			fw.write("</font></big></caption>\n");
			fw.write("</big></caption>\n");
			
			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			
			for (int i=0;i<MasterList.size();i++) {
				fw.write("\t<th>Isomorph "+(i+1)+"</th>\n");		
			}
			fw.write("</tr>\n");
			
			fw.write("<tr>\n");
			
			
			
			for (int i = 0; i < MasterList.size(); i++) {
				AtomContainerSet AtomContainerSet = (AtomContainerSet) MasterList.get(i);

				fw.write("\t<td>\n");

				fw.write("\t\t<table width=100%>\n");
				
				if (htStructureData2.size() > 0) {

					String correctVal = null;

					for (int j = 0; j < AtomContainerSet.getAtomContainerCount(); j++) {
						String source0 = (String) AtomContainerSet.getAtomContainer(j)
								.getProperty("Source");
						
						
						String correctKey = source0.replace("MolFile", "")
								+ "Correct";
						correctVal = (String) htStructureData2.get(correctKey);

						
						String color = "blue";
						
						
						
						if (correctVal != null && !correctVal.equals("")) {
//							System.out.println(source0+"\t"+correctVal+"**");
							if (correctVal.equals("true")) {
								correctVal = "correct";
								color = "green";
							}
							if (correctVal.equals("false")) {
								correctVal = "wrong";
								color = "red";
							}

						} else {							
							if (source0.equals("Other")
									&& AtomContainerSet.getAtomContainer(j).getProperty(
											"Source").equals("Other")) {
								color="green";
								correctVal="correct";
							} else {
								color="magenta";
								correctVal="Unknown";
							}
						}
						
						
						fw.write("\t\t\t<tr>\n");
						fw.write("\t\t\t\t<td>"+AtomContainerSet.getAtomContainer(j).getProperty("Source")+
								"</td>\n");
						fw.write("\t\t\t\t<td><font color=" + color + ">" + correctVal
								+ "</font></td>\r\n");
						fw.write("\t\t\t</tr>\n");


						
					} //end f loop
				} else {
					System.out.println("No entry in SD2 for "+fd.CAS);

					for (int j = 0; j < AtomContainerSet.getAtomContainerCount(); j++) {
						String source0 = (String) AtomContainerSet.getAtomContainer(j)
								.getProperty("Source");
						String correctKey = source0.replace("MolFile", "")
								+ "Correct";
						
//						if (source0.equals("Chemidplus2DMolFile")) continue;

						fw.write("\t\t\t<tr>\n");
						fw.write("\t\t\t\t<td>"+AtomContainerSet.getAtomContainer(j).getProperty("Source")+
								"</td>\n");
						fw.write("\t\t\t\t<td><font color=magenta>Unknown (not in SD2)</font></td>\r\n");
						fw.write("\t\t\t</tr>\n");
					} //end f loop

				}
				
				fw.write("\t\t</table>\n");
				fw.write("\t</td>\n");
				
				
			} // end master list i loop
			fw.write("</tr>\n");
					
			fw.write("<tr>\n");
			for (int i=0;i<MasterList.size();i++) {
				AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
				
				IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
				
//				File ImageFile=new File(outputfolder+"/images/"+fd.CAS+"_"+i+".png");
				
//				System.out.println(ImageFile.exists());

				
//				if (!ImageFile.exists())
					SaveStructureToFile.CreateImageFile(molecule,fd.CAS+"_"+i,outputfolder+"/images");
					fw.write("\t<td><img src=\"images/"+fd.CAS+"_"+i+".png"+"\"></td>\n");


//				
//					SaveStructureToFile.CreateScaledImageFile(molecule,
//							fd.CAS+"_"+i, outputfolder+"/images", false, false,
//							true, 30, size);
//				fw.write("\t<td><img src=\"images/"+fd.CAS+"_"+i+".png"+"\" height="+size+"></td>\n");	
			}
			fw.write("</tr>\n");

			
			fw.write("<tr>\n");
			for (int i=0;i<MasterList.size();i++) {
				AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
				
				IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
				
				
				 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(molecule);
				 String formula=MolecularFormulaManipulator.getHTML(mf);
				
				fw.write("\t<td>"+formula+"</td>\n");
				
//				if (Formula.equals(ChemidplusFormula))
//					fw.write("\t<td>"+Formula+"</td>\n");
//				else
//					fw.write("\t<td><font color=\"red\">"+Formula+"</font></td>\n");
			}
			fw.write("</tr>\n");

			
			fw.write("</table><br><br>\n");
//			
			
		
		fw.flush();

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void WriteHTMLForDifference3(String outputfolder,ArrayList MasterList,FileWriter fw,FileData fd,int count,String structuredatafilepath) {
//		String folder="ToxPredictor/chemidplus/compare/GoodData";
		
		LinkedList infoList=this.LookupLineInTextFile(structuredatafilepath,fd.CAS);
		
		String ChemidplusFormula="";
		
		if (infoList!=null) {
			ChemidplusFormula=(String)infoList.get(4);
//			String ChemidplusName=(String)infoList.get(1);
		
			 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(ChemidplusFormula,DefaultChemObjectBuilder.getInstance());
			 ChemidplusFormula=MolecularFormulaManipulator.getHTML(mf);
		}
	
		try {
			
						
			
			fw.write("<tr>\n");
			fw.write("\t<td>"+fd.CAS+"</td>\n");
			fw.write("\t<td>"+ChemidplusFormula+"</td>\n");
			
			for (int i=0;i<MasterList.size();i++) {
				AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
				
				fw.write("\t<td>");
				for (int j=0;j<AtomContainerSet.getAtomContainerCount();j++) {
					fw.write(AtomContainerSet.getAtomContainer(j).getProperty("Source")+"<br>");
				}
				fw.write("</td>\n");
				
				IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
				
				String Formula=CDKUtilities.generateHTMLFormula(molecule);
				
				if (Formula.equals(ChemidplusFormula))
					fw.write("\t<td>"+Formula+"</td>\n");
				else
					fw.write("\t<td><font color=\"red\">"+Formula+"</font></td>\n");
	
				
			}
	
			for (int i=MasterList.size();i<9;i++) {
				fw.write("\t<td><br></td>\n");				
				fw.write("\t<td><br></td>\n");
			}
	
			
			
			fw.write("</tr>\n");
			
			
	
			
		
		fw.flush();
	
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void WriteFormulaTableEntry(ArrayList MasterList,FileWriter fw,FileData fd,int count) {
		String folder="ToxPredictor/chemidplus/compare/GoodData";
		
		LinkedList infoList=this.LookupLineInTextFile("ToxPredictor/chemidplus/parse/structuredatanew.txt",fd.CAS);
		
		String ChemidplusFormula="";
		
		if (infoList!=null) {
			ChemidplusFormula=(String)infoList.get(4);
			String ChemidplusName=(String)infoList.get(1);
		
			ChemidplusFormula=CDKUtilities.fixFormula(ChemidplusFormula);
		}

		try {
			
						
			
			
			fw.write(fd.CAS+"\t"+ChemidplusFormula+"\t");
			
			for (int i=0;i<MasterList.size();i++) {
				AtomContainerSet AtomContainerSet=(AtomContainerSet)MasterList.get(i);
				
				for (int j=0;j<AtomContainerSet.getAtomContainerCount();j++) {
					fw.write(AtomContainerSet.getAtomContainer(j).getProperty("Source")+"");
					if (j<AtomContainerSet.getAtomContainerCount()-1) fw.write(", ");
				}
				fw.write("\t");
				
				IAtomContainer molecule=AtomContainerSet.getAtomContainer(0);
				
				String Formula=CDKUtilities.generateFormula(molecule);
				
				fw.write(Formula+"\t");
			}

			for (int i=MasterList.size();i<9;i++) {
				fw.write("\t\t");								
			}

			fw.write("\n");
			
			fw.flush();

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	
	
	
	public void parseChemIDplusHTMLFiles(boolean WriteStructureData,boolean WriteToxData,boolean WriteSystematicNameData,boolean WriteSynonymData,boolean WriteFormulaData) {

		try {

//			File mainfolder = new File(
//					"C:/Documents and Settings/tmarti02/My Documents/comptox/ChemIDplus All");
			File mainfolder = new File("N:/NRMRL-PRIV/CompTox/LD50_ChemID_Data");
			

			File[] folders = mainfolder.listFiles();

			for (int i = 0; i < folders.length; i++) {
				if (folders[i].getName().equals("9001-10000")) {
					System.out.println(folders[i]);
					this.RetrieveDataFromFolder(folders[i],WriteStructureData,WriteToxData,WriteSystematicNameData,WriteSynonymData,WriteFormulaData);
				}
			}

		} catch (Exception e) {
			System.out.println(e);			
		}

	}
	
		
		
	
	

	public void CombineOutputFiles(File folder,File outputfile) {
				

		File[] files = folder.listFiles();

		try {

			FileWriter fw = new FileWriter(outputfile);

			for (int i = 0; i < files.length; i++) {

				if (files[i].getName().indexOf("txt") > -1
						&& files[i].getName().indexOf("Copy") == -1
						&& files[i].getName().indexOf("all.txt") == -1) {

					BufferedReader br = new BufferedReader(new FileReader(
							files[i]));
					String Header = br.readLine();
					if (i == 0)
						fw.write(Header + "\r\n");

					while (true) {
						String Line=br.readLine();
						if (Line==null) break;
						fw.write(Line+"\r\n");
					}
					
					
					br.close();

				}
			}

			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private void RetrieveDataFromFolder(File folder,boolean WriteStructureData,boolean WriteToxData, boolean WriteSystematicNameData,boolean WriteSynonymData,boolean WriteFormulaData) {
		
		
		File outputStructureData = new File("ToxPredictor/chemidplus/parse/StructureData/StructureData_" + folder.getName()
				+ ".txt");

		File outputToxData = new File("ToxPredictor/chemidplus/parse/ToxData/ToxData_" + folder.getName()
				+ ".txt");
		
		File outputSystematicNameData = new File("ToxPredictor/chemidplus/parse/SystematicNameData/SystematicNameData_" + folder.getName()
				+ ".txt");
		
		File outputSynonymData = new File("ToxPredictor/chemidplus/parse/SynonymData/SynonymData_" + folder.getName()
				+ ".txt");
		

		File outputFormulaData = new File("ToxPredictor/chemidplus/parse/FormulaData/FormulaData_" + folder.getName()
				+ ".txt");
		
		
		FileWriter fw=null;
		FileWriter fwToxData=null;
		FileWriter fwSystematicNameData=null;
		FileWriter fwSynonymData=null;
		FileWriter fwFormulaData=null;
		
		try {

			if (WriteStructureData)  {
				fw = new FileWriter(outputStructureData);
				fw.write(FileData.getHeaderStructureData(Delimiter));						
				fw.flush();				
			}
			
			if (WriteToxData) {
				fwToxData = new FileWriter(outputToxData);			
				fwToxData.write(FileData.getHeaderToxData(Delimiter));
				fwToxData.flush();
			}
			
			if (WriteSystematicNameData) {
				fwSystematicNameData = new FileWriter(outputSystematicNameData);			
				fwSystematicNameData.write(FileData.getHeaderSystematicNameData(Delimiter));
				fwSystematicNameData.flush();
			}
			
			if (WriteSynonymData) {
				fwSynonymData = new FileWriter(outputSynonymData);			
				fwSynonymData.write(FileData.getHeaderSynonymData(Delimiter));
				fwSynonymData.flush();
			}
			
			if (WriteFormulaData) {
				fwFormulaData = new FileWriter(outputFormulaData);			
				fwFormulaData.write(FileData.getHeaderFormulaData(Delimiter));
				fwFormulaData.flush();
			}

			
			
			File[] files = folder.listFiles();

			
			
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					
//					if (!files[i].getName().equals("140-40-9.htm")) continue;
					
					
//					IAtomContainer molecule = null;

//					System.out.println(files[i].getName());
					FileData fd = this.RetrieveDataFromFile(files[i]);

					System.out.println(fd.CAS);
					
//					if (fd.ToxicityRecords.size()==0) {
//						System.out.println("No tox records for "+fd.CAS);
//					}
					
//					if (!fd.CAS.equals("140-40-9")) continue;
					
					if (!fd.FileComplete) {
						System.out.println("Error- data file for CAS# "
								+ fd.CAS + " is incomplete");
						continue;
					}

					
					if (WriteStructureData) {
						
						this.GetFormulaFromList(fd);
						this.GetSystematicNameFromList(fd);
								
//						this.CheckForMolFiles(fd);
//						this.CheckIfHaveCorrectedFormula(fd);
//						this.EvaluateFormula(fd);						//												
//						this.CheckIfNotedChemical(fd);
						
						if (!fd.Formula.equals("Unspecified") && !fd.Formula.equals("") && fd.Formula != null  && fd.Description.equals("")) {			
							
							// for some reason cdk doesnt update formula with atoms 
							// in right order so need to do mfa 2x
							
							fd.FormulaFromChemidplusWebPage=ChemicalFinder.CorrectFormula(fd.FormulaFromChemidplusWebPage);
							
							this.CheckForBadElement(fd.FormulaFromChemidplusWebPage,fd);
						} else {
							fd.BadElement="N/A";
						}
						
						
						
						fw.write(fd.toStringStructureData(Delimiter));
						fw.flush();

					}
										
					if (WriteToxData) {
						fwToxData.write(fd.toStringToxData("|"));					
						fwToxData.flush();
					}
					
					if (WriteSystematicNameData) {
//						System.out.println(fd.toStringSystematicNameData("|"));
						fwSystematicNameData.write(fd.toStringSystematicNameData("|"));					
						fwSystematicNameData.flush();
					}				

					if (WriteSynonymData) {
						fwSynonymData.write(fd.toStringSynonymData("|"));					
						fwSynonymData.flush();
					}				

					if (WriteFormulaData) {
						fwFormulaData.write(fd.toStringFormulaData("|"));					
						fwFormulaData.flush();
					}				
					
				}
			}

			if (WriteStructureData) fw.close();
			if (WriteToxData) fwToxData.close();
			if (WriteSystematicNameData) fwSystematicNameData.close();
			if (WriteSynonymData) fwSynonymData.close();
			if (WriteFormulaData) fwFormulaData.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	public void CheckForMolFiles(FileData fd) {
//
//		String CAS=fd.CAS;
//		
//		fd.Smiles = lookup.LookUpSmiles(CAS);
//		
//		//		fd.HaveNIST2DMolFile=downloadmolfiles.isNISTMolFileAvailable(fd.CAS,2);
////		fd.HaveNIST3DMolFile=downloadmolfiles.isNISTMolFileAvailable(fd.CAS,3);
//
//		fd.HaveNIST2DMolFile=this.HaveMolFileInJar("NIST2d/"+CAS+"_NIST2d.mol");
//		fd.HaveNIST3DMolFile=this.HaveMolFileInJar("NIST3d/"+CAS+"_NIST3d.mol");
//		
//		fd.HaveName2DMolFile=this.HaveMolFile(CAS+"_Name2d",NameFolder);
//		fd.HaveDSSTox2DMolFile=this.HaveMolFile(CAS+"_DSSTOX2d",DSSToxFolder);
//		fd.HaveDougDB3DMolFile=this.HaveMolFile(CAS,DougMolFolder);
//
//		//fd.HaveNIH3DMolFile=this.HaveMolFile(CAS,NIHMolFolder);
//		fd.HaveNIH3DMolFile=this.HaveMolFileInJar("coords/"+CAS+".mol");
//		
//		fd.HaveIndexNet2DMolFile=this.HaveMolFileInJar(CAS+"_IndexNet2d.mol");		
//
//		fd.HaveChemidplus3DMolFile=this.HaveMolFile(CAS,ChemidplusMolFolder);
//		
//		
//	}
	
	public void CheckForMolFiles2(FileData fd) {
		
		String CAS=fd.CAS;
		
		if (this.MolSrc.equals("jar")) {			
			fd.HaveChemidplus3DMolFile=this.HaveMolFileInJar(this.ChemidplusJarMolFolder+"/"+CAS+".mol");			
			fd.HaveDougDB3DMolFile=this.HaveMolFileInJar(this.DougJarMolFolder+"/"+CAS+".mol");
			fd.HaveDSSTox2DMolFile=this.HaveMolFileInJar(this.DSSToxJarMolFolder+"/"+CAS+".mol");
			fd.HaveNIST3DMolFile=this.HaveMolFileInJar(this.NIST3dJarMolFolder+"/"+CAS+".mol");
			fd.HaveNIST2DMolFile=this.HaveMolFileInJar(this.NIST2dJarMolFolder+"/"+CAS+".mol");
			fd.HaveNIH3DMolFile=this.HaveMolFileInJar(this.NIH3dJarMolFolder+"/"+CAS+".mol");
			fd.HaveIndexNet2DMolFile=this.HaveMolFileInJar(this.IndexNet2dJarMolFolder+"/"+CAS+".mol");
			fd.HaveName2DMolFile=this.HaveMolFileInJar(this.Name2dJarMolFolder+"/"+CAS+".mol");
			fd.HaveSmiles=this.HaveMolFileInJar(this.SmilesJarMolFolder+"/"+CAS+".mol"); 		
			fd.HaveOther=this.HaveMolFileInJar(this.OtherJarMolFolder+"/"+CAS+".mol");
			
			if (!fd.HaveChemidplus3DMolFile) {
				fd.HaveChemidplus3DMolFile=this.HaveMolFile(CAS,Chemidplus3dMolFolder);
			}
			
			
		} else {
			fd.HaveChemidplus3DMolFile=this.HaveMolFile(CAS,Chemidplus3dMolFolder);
			fd.HaveChemidplus2DMolFile=this.HaveMolFile(CAS,Chemidplus2dMolFolder);
			fd.HaveDougDB3DMolFile=this.HaveMolFile(CAS,this.DougMolFolder);
			fd.HaveDSSTox2DMolFile=this.HaveMolFile(CAS,this.DSSToxFolder);
			fd.HaveNIST3DMolFile=this.HaveMolFile(CAS,this.NIST3DFolder);
			fd.HaveNIST2DMolFile=this.HaveMolFile(CAS,this.NIST2DFolder);
			fd.HaveNIH3DMolFile=this.HaveMolFile(CAS,this.NIHMolFolder);
			fd.HaveIndexNet2DMolFile=this.HaveMolFile(CAS,this.IndexNet2DFolder);
			fd.HaveName2DMolFile=this.HaveMolFile(CAS,this.NameFolder);
			fd.HaveSmiles=this.HaveMolFile(CAS,this.SmilesFolder);
			fd.HaveOther=this.HaveMolFile(CAS,this.OtherFolder);
			
			
		}
		
		
		fd.Smiles = lookup.LookUpSmiles(CAS);
		
	}
	
	
	
	public String GetBestSource(FileData fd) {
		String Source="";
		
		if (fd.HaveOther) {
			return "Other";
		} else if (fd.HaveDSSTox2DMolFile) {
			return "DSSTox2DMolFile";
		} else if (fd.HaveChemidplus3DMolFile) {			
			return "Chemidplus3DMolFile";
		} else if (fd.HaveChemidplus2DMolFile) {			
			return "Chemidplus2DMolFile";
		} else if (fd.HaveDougDB3DMolFile) {
			return "DougDB3DMolFile";
		} else if (fd.HaveNIST3DMolFile) {
			return "NIST3DMolFile";
		} else if (fd.HaveNIST2DMolFile) {						
			return "NIST2DMolFile";
		} else if (fd.HaveNIH3DMolFile) {
			return "NIH3DMolFile";
		} else if (fd.HaveIndexNet2DMolFile) {
			return "IndexNet2DMolFile";
		} else if (fd.HaveSmiles) {
			return "Smiles";
		} else if (fd.HaveName2DMolFile) {
			return "Name2DMolFile";
		} else {
			return "N/A";
		}

	}
	
	public String GetBestSourceNoDSSTox(FileData fd) {
		String Source="";
		
		if (fd.HaveChemidplus3DMolFile) {			
			return "Chemidplus3DMolFile";
		} else if (fd.HaveDougDB3DMolFile) {
			return "DougDB3DMolFile";
		} else if (fd.HaveNIST3DMolFile) {
			return "NIST3DMolFile";
		} else if (fd.HaveNIST2DMolFile) {						
			return "NIST2DMolFile";
		} else if (fd.HaveNIH3DMolFile) {
			return "NIH3DMolFile";
		} else if (fd.HaveIndexNet2DMolFile) {
			return "IndexNet2DMolFile";
		} else if (fd.HaveSmiles) {
			return "Smiles";
		} else if (fd.HaveName2DMolFile) {
			return "Name2DMolFile";
		} else {
			return "N/A";
		}

	}
	
	public IAtomContainer GetBestMolecule(FileData fd) {		
		String BestSource=this.GetBestSource(fd);	
		fd.StructureSource=BestSource;
		
		return this.getAtomContainerFromSource(BestSource, fd.CAS);
			
	}
	
	public IAtomContainer GetBestMolecule(String CAS) {		
		
		FileData fd=new FileData();
		fd.CAS=CAS;
		
		CheckForMolFiles2(fd);
		
		String BestSource=this.GetBestSource(fd);	
		fd.StructureSource=BestSource;
		
		return this.getAtomContainerFromSource(BestSource, CAS);
			
	}
	
	public IAtomContainer getAtomContainerFromSource(String source,String CAS) {
		
		IAtomContainer molecule=null;
		
		
		try {
			
		if (this.MolSrc.equals("jar")) {

			if (source.equals("DSSTox2DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.DSSToxJarMolFolder+"/"+CAS+".mol");
			} else if (source.equals("Chemidplus3DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.ChemidplusJarMolFolder+"/"+CAS+".mol");
				if (molecule==null) molecule = this.LoadChemicalFromMolFile(CAS,Chemidplus3dMolFolder);
			} else if (source.equals("DougDB3DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.DougJarMolFolder+"/"+CAS+".mol");					
			} else if (source.equals("NIST3DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.NIST3dJarMolFolder+"/"+CAS+".mol");				
			} else if (source.equals("NIST2DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.NIST2dJarMolFolder+"/"+CAS+".mol");
			} else if (source.equals("NIH3DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.NIH3dJarMolFolder+"/"+CAS+".mol");
			} else if (source.equals("IndexNet2DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.IndexNet2dJarMolFolder+"/"+CAS+".mol");				
			} else if (source.equals("Smiles")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.SmilesJarMolFolder+"/"+CAS+".mol"); 						
			} else if (source.equals("Name2DMolFile")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.Name2dJarMolFolder+"/"+CAS+".mol");				
			} else if (source.equals("Other")) {
				molecule=this.LoadChemicalFromMolFileInJar(this.OtherJarMolFolder+"/"+CAS+".mol");
			}

		} else {
			
			if (source.equals("DSSTox2DMolFile")) {				
				molecule = this.LoadChemicalFromMolFile(CAS,this.DSSToxFolder);
			} else if (source.equals("Chemidplus3DMolFile")) {
				molecule = this.LoadChemicalFromMolFile(CAS,Chemidplus3dMolFolder);
			} else if (source.equals("Chemidplus2DMolFile")) {
				molecule = this.LoadChemicalFromMolFile(CAS,Chemidplus2dMolFolder);	
			} else if (source.equals("DougDB3DMolFile")) {
				molecule = this.LoadChemicalFromMolFile(CAS,this.DougMolFolder);	
			} else if (source.equals("NIST3DMolFile")) {
				molecule=this.LoadChemicalFromMolFile(CAS,this.NIST3DFolder);
			} else if (source.equals("NIST2DMolFile")) {
				molecule=this.LoadChemicalFromMolFile(CAS,this.NIST2DFolder);
			} else if (source.equals("NIH3DMolFile")) {
				molecule=LoadChemicalFromMolFile(CAS,this.NIHMolFolder);
			} else if (source.equals("IndexNet2DMolFile")) {
				molecule=this.LoadChemicalFromMolFile(CAS,this.IndexNet2DFolder);
			} else if (source.equals("Smiles")) {
				//future work- fix to make like other sources
				molecule=this.LoadChemicalFromMolFile(CAS,this.SmilesFolder);
			} else if (source.equals("Name2DMolFile")) {
				molecule = this.LoadChemicalFromMolFile(CAS,this.NameFolder);
			} else if (source.equals("Other")) {
				molecule = this.LoadChemicalFromMolFile(CAS,this.OtherFolder);
			}
			
		}


		} catch (Exception e) {
			System.out.println("error reading for "+CAS+"\t"+source);
		}
		
		return molecule;
		
	}
	
//	public AtomContainerSet getAtomContainers(FileData fd) {
//		
//		
//		AtomContainerSet AtomContainerSet=new AtomContainerSet();
//		
//		// look for chemid plus first (only downloaded in cases where it was known to have right formula or if no other structure was available-or if it was correct over another structure)
//	
//		
//		if (fd.HaveDSSTox2DMolFile) {
//			AtomContainer molecule = this.LoadChemicalFromMolFile(fd.CAS+"_DSSTOX2d",this.DSSToxFolder);
//			molecule.setProperty("Source","DSSTox2DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);						
//		} 
//	
//		
//		if (fd.HaveChemidplus3DMolFile) {			
//			AtomContainer molecule = this.LoadChemicalFromMolFile(fd.CAS,ChemidplusMolFolder);
//			molecule.setProperty("Source","Chemidplus3DMolFile");			
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);			
//		} 
//	
//	
//		
//		if (fd.HaveDougDB3DMolFile) {
//			AtomContainer molecule = this.LoadChemicalFromMolFile(fd.CAS,DougMolFolder);
//			molecule.setProperty("Source","DougDB3DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//		}
//		
//		
//		if (fd.HaveNIST3DMolFile) {
//			AtomContainer molecule=LoadChemicalFromMolFileInJar("NIST3d/"+fd.CAS+"_NIST3d.mol");						
//			molecule.setProperty("Source","NIST3DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//		
//		} 
//		
//		if (fd.HaveNIST2DMolFile) {						
//			AtomContainer molecule=LoadChemicalFromMolFileInJar("NIST2d/"+fd.CAS+"_NIST2d.mol");
//			molecule.setProperty("Source","NIST2DMolFile");
//			
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//	
//		} 
//		
//		if (fd.HaveNIH3DMolFile) {
//			// load molecule:			
////			AtomContainer molecule = this.LoadChemicalFromMolFile(fd.CAS,NIHMolFolder);
//			AtomContainer molecule=LoadChemicalFromMolFileInJar("coords/"+fd.CAS+".mol");
//			
//			molecule.setProperty("Source","NIH3DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//	
//		
//		} 
//		
//		if (fd.HaveIndexNet2DMolFile) {
//			AtomContainer molecule=this.LoadChemicalFromMolFileInJar(fd.CAS+"_IndexNet2d.mol");
//			molecule.setProperty("Source","IndexNet2DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//						
//		}
//		
//		if (!fd.Smiles.equals("missing")) {
//	
//			//try to get structure from smiles
//			fd.Smiles = CDKUtilities.FixSmiles(fd.Smiles);
//			
//			SmilesParser sp=new SmilesParser();
//			
//			boolean ParsedOK = true;
//			try {
//				AtomContainer molecule = sp.parseSmiles(fd.Smiles);
//				molecule.setProperty("Source","Smiles");
//				if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//	
//			} catch (Exception e) {
//			}
//		}
//		
//		if (fd.HaveName2DMolFile) {
//			AtomContainer molecule = this.LoadChemicalFromMolFile(fd.CAS+"_Name2d",this.NameFolder);
//			molecule.setProperty("Source","Name2DMolFile");
//			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
//						
//		} 
//				
//		return AtomContainerSet;
//	}

	public AtomContainerSet getAtomContainers2(FileData fd) {
		
		
		AtomContainerSet AtomContainerSet=new AtomContainerSet();
		
		String Source="";
		
		try {
		
		// look for chemid plus first (only downloaded in cases where it was known to have right formula or if no other structure was available-or if it was correct over another structure)

		boolean debug=false;
		
		if (fd.HaveDSSTox2DMolFile) {
			if (debug) System.out.print("Getting DSSTOXMolFile...");			
			Source="DSSTox2DMolFile";			
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
			if (debug) System.out.print("done\n");
		} 

		
		if (fd.HaveChemidplus3DMolFile) {			
			if (debug) System.out.print("Getting Chemidplus3DMolFile...");
			Source="Chemidplus3DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);			
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
			if (debug) System.out.print("done\n");
		} 

		if (fd.HaveChemidplus2DMolFile) {			
			if (debug) System.out.print("Getting Chemidplus2DMolFile...");
			Source="Chemidplus2DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);			
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
			if (debug) System.out.print("done\n");
		} 

		
		if (fd.HaveDougDB3DMolFile) {
			if (debug) System.out.print("Getting Doug3DMolFile...");
			Source="DougDB3DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
			if (debug) System.out.print("done\n");
		}
		
		
		if (fd.HaveNIST3DMolFile) {
			Source="NIST3DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
		
		} 
		
		if (fd.HaveNIST2DMolFile) {						
			Source="NIST2DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);			
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);

		} 
		
		if (fd.HaveNIH3DMolFile) {
			Source="NIH3DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);			
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
		} 
		
		if (fd.HaveIndexNet2DMolFile) {
			Source="IndexNet2DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);						
		}
		
		if (fd.HaveSmiles) {
			Source="Smiles";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
		}
		
		if (fd.HaveName2DMolFile) {
			Source="Name2DMolFile";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);
						
		}
		
		if (fd.HaveOther) {
			Source="Other";
			IAtomContainer molecule = this.getAtomContainerFromSource(Source,fd.CAS);
			molecule.setProperty("Source",Source);
			if (molecule != null) AtomContainerSet.addAtomContainer(molecule);						
		} 

		
		if (debug) System.out.println("Done getting molecules");
		
		} catch (Exception e) {
			System.out.println("error reading for "+fd.CAS+"\t"+Source);
		}
		
//		fd.HaveDSSTox2DMolFile=this.HaveMolFile(fd.CAS+"_DSSTOX2d",f+"DSSTOX_withH");
//		fd.HaveNIST3DMolFile=this.HaveMolFile(fd.CAS+"_NIST3d",f+"NIST3d");
//		fd.HaveNIST2DMolFile=this.HaveMolFile(fd.CAS+"_NIST2d",f+"NIST2d_withH");
//		fd.HaveNIH3DMolFile=this.HaveMolFile(fd.CAS,NIHMolFolder);
//		fd.HaveIndexNet2DMolFile=this.HaveMolFile(fd.CAS+"_IndexNet2d",f+"INDEXNET2d_withH");
		
		
				
		return AtomContainerSet;
	}
	
	
	/**
	 * This version doesnt use DSSTOX as one of choices
	 * @param fd
	 * @return
	 */
	private IAtomContainer GetBestMoleculeNoDSSTox(FileData fd) {
		String BestSource=this.GetBestSourceNoDSSTox(fd);
		fd.StructureSource=BestSource;
		return this.getAtomContainerFromSource(BestSource, fd.CAS);	
	}		
		
	
	
	private void GetFormulaFromList(FileData fd) {
		
		for (int i=0;i<fd.Formulas.size();i++) {
			String Formula=(String)fd.Formulas.get(i);
			fd.FormulaFromChemidplusWebPage=Formula;	
			
			if (Formula.indexOf(".") > -1 || Formula.indexOf(" x ") > -1) {
				fd.Description = "Multiple Molecules";					
				break;
			} else if (Formula.indexOf("(") > -1) {
				fd.Description = "Poly";			
				break;
			} else if (Formula.equals("")) {
				fd.Description = "N/A";						
				break;
			}
						
		}
		
		
	}
	/**
	 * This sub tries to find longest systematic name- if there is not systematic names it finds the longest synonym
	 * @param fd
	 */
	
	private void GetSystematicNameFromList(FileData fd) {
		fd.SystematicNameLongest="";
		
		for (int i=0;i<fd.SystematicName.size();i++) {
			String CurrentSystematicName=(String)fd.SystematicName.get(i);
			
			if (CurrentSystematicName.indexOf("8CI")>-1 || CurrentSystematicName.indexOf("9CI")>-1) {
				fd.SystematicNameLongest=CurrentSystematicName;
			} else {			
				if (fd.SystematicNameLongest.indexOf("8CI")==-1 && fd.SystematicNameLongest.indexOf("9CI")==-1) {
					if (CurrentSystematicName.length()>fd.SystematicNameLongest.length()) {
						fd.SystematicNameLongest=CurrentSystematicName;
					}
				}
			}
						
		}
		fd.SystematicNameLongest=fd.SystematicNameLongest.trim();
		
		if (fd.SystematicNameLongest.equals("")) {
			String longestSynonym="";
			for (int i=0;i<fd.Synonyms.size();i++) {
				String CurrentSynonym=(String)fd.Synonyms.get(i);
				
				if (CurrentSynonym.indexOf("8CI")>-1 || CurrentSynonym.indexOf("9CI")>-1) {
					longestSynonym=CurrentSynonym;
				} else {			
					if (longestSynonym.indexOf("8CI")==-1 && longestSynonym.indexOf("9CI")==-1) {
						if (CurrentSynonym.length()>longestSynonym.length()) {
							longestSynonym=CurrentSynonym;
						}
					}
				}
			}
//			System.out.println(fd.CAS+"\t"+longestSynonym);
			fd.SystematicNameLongest=longestSynonym;
			
		}

		
	}
	
	private void EvaluateFormula(FileData fd) {
		IAtomContainer molecule = null;
		
		
				
		try {			
			if (!fd.Formula.equals("") && fd.Formula != null  && fd.Description.equals("")) {			
		
				fd.Formula=CDKUtilities.fixFormula(fd.Formula);
				
				this.CheckForBadElement(fd.Formula,fd);
			} else {
				fd.BadElement="N/A";
			}
						
			molecule = this.GetBestMolecule(fd);

			if (molecule == null) {
				fd.StructureSource = "N/A";
				fd.CalculatedFormula="N/A";
				return;
			}
						
			
			molecule=CDKUtilities.addHydrogens(molecule);
			fd.CalculatedFormula = CDKUtilities.calculateFormula(molecule);
			
			if (fd.BadElement.equals("N/A")) {
				this.CheckForBadElement(molecule,fd);
			}
			

		} catch (Exception e) {
//			e.printStackTrace();
		}
		
		
	}
	
	public int GetHydrogenCount(IAtomContainer m) {
		int HCount=0;
		for (int i=0;i<m.getAtomCount();i++) {
			if (m.getAtom(i).getSymbol().equals("H")) {
				HCount++;
			}
		}
		return HCount;
	}
	
	private boolean isHydrogenCountTheSame(Hashtable h1, Hashtable h2) {
		
		try {
			
			int Count1=(Integer) h1.get("H");
			int Count2=(Integer) h2.get("H");
			
			return (Count1==Count2);
		
		} catch (Exception e) {
			return false;
		}
		
		
	}
	
	void CheckForBadElement(String formula,FileData fd) {
		
		try {
			
			fd.BadElement = "false";
			
			
			MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(fd.Formula,DefaultChemObjectBuilder.getInstance());
			
			IAtomContainer ac=MolecularFormulaManipulator.getAtomContainer(mf);
			
			for (int i=0;i<ac.getAtomCount();i++) {
				
				String var=ac.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

					fd.BadElement = "true";
					// System.out.println(fd.CAS+" has bad element: "+var);
					return;
				}
			}
			

		} catch (Exception e) {
			fd.BadElement = "N/A";
		}
	}
	
	
	void CheckForBadElement(HashMap h2,FileData fd) {
		
		try {
			
			fd.BadElement = "false";
			
			Iterator iterator=h2.keySet().iterator();
			
			while (iterator.hasNext()) {

				String var = (String) iterator.next();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

					fd.BadElement = "true";
					// System.out.println(fd.CAS+" has bad element: "+var);
					break;
				}
			}

		} catch (Exception e) {
			fd.BadElement = "N/A";
		}
	}
	
	public void RemoveRecordsFromStructureData2(String folder,String oldstructuredata2filename,String newstructuredata2filename,String CASlistfilename) {

		try {
			
			//Load CAS list to omit from new structuredata2 file:
			
			BufferedReader brCAS=new BufferedReader (new FileReader(folder+"/"+CASlistfilename));
			
			java.util.Hashtable<String,Boolean> htCAS=new java.util.Hashtable(); 
			while (true) {
				String CAS=brCAS.readLine();
				if (CAS==null) break;
				
				htCAS.put(CAS, Boolean.TRUE);
				
			}
			brCAS.close();
			
			BufferedReader br=new BufferedReader (new FileReader(folder+"/"+oldstructuredata2filename));
			
			FileWriter fw=new FileWriter(folder+"/"+newstructuredata2filename);
			
			fw.write(br.readLine()+"\r\n");//header
			
			while (true) {
				String Line=br.readLine();
				
//				System.out.println(Line);
				if (Line==null) break;
				
				String CAS=Line.substring(0,Line.indexOf("\t"));
				
				if (htCAS.get(CAS)!=null) {
					System.out.println(CAS+"\t"+htCAS.get(CAS));	
				} else {
					fw.write(Line+"\r\n");
				}
				
			}
			
			
			fw.close();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method goes through structuredata2 and updates the "Have" 
	 * fields. If a src now has a mol file but the "correct" field is not set, 
	 * we check if the other sources have the same structure so we can use its
	 * "correct" field value.
	 */
	
	public void FixHaveFieldsInStructureData2() {
		
		String folder="ToxPredictor/chemidplus/parse/check Have mol files in structuredata2";
		String inputFileName="structure data2_3-27-12.txt";
		String outputFileName="structure data2_3-27-12_fix_have_and_fix_correct.txt";
//		String outputFileName="bob.txt";
		
		String [] srcs={"NIH3D","NIST2D","NIST3D","DougDB3D","IndexNet2D","Chemidplus3D","DSSTox2D","Name2D","Smiles"};
		
		String startCAS="100-00-5";
//		String startCAS="632-95-1";
		boolean start=false;
		
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+inputFileName));
			String header=br.readLine();
			
			FileWriter fw=new FileWriter(folder+"/"+outputFileName);
			
			fw.write(FileData.getHeaderStructureData2("|"));
			fw.flush();
			
			List hlist=ToxPredictor.Utilities.Utilities.Parse(header, "\t");

			int colCAS=0;
			int colStructureSource=1;
			int colCalculatedFormula=2;
			int colCalculatedMolecularWeight=3;
			int colBadElement=4;
			int colValidated=5;
			int colSourceCorrectStructure=6;
			int colHaveNIH3DMolFile=7;
			int colNIH3DCorrect=8;
			int colHaveNIST2DMolFile=9;
			int colNIST2DCorrect=10;
			int colHaveNIST3DMolFile=11;
			int colNIST3DCorrect=12;
			int colHaveDougDB3DMolFile=13;
			int colDougDB3DCorrect=14;
			int colHaveIndexNet2DMolFile=15;
			int colIndexNet2DCorrect=16;
			int colHaveChemidplus3DMolFile=17;
			int colChemidplus3DCorrect=18;
			int colHaveDSSTox2DMolFile=19;
			int colDSSTox2DCorrect=20;
			int colHaveName2DMolFile=21;
			int colName2DCorrect=22;
			int colSmiles=23;
			int colSmilesCorrect=24;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null)  break;
				
				List <String>list=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				FileData fd=new FileData();
				
				fd.CAS=list.get(colCAS);
				
				if (fd.CAS.equals(startCAS)) start=true;
				
				if (!start) continue;
				
//				if (!fd.CAS.equals("21884-44-6")) {
//					continue;
//				}
				
				fd.StructureSource=list.get(colStructureSource);
				fd.CalculatedFormula=list.get(colCalculatedFormula);
				fd.CalculatedMolecularWeight=Double.parseDouble(list.get(colCalculatedMolecularWeight));
				fd.BadElement=list.get(colBadElement);
				fd.Validated=Boolean.parseBoolean(list.get(colValidated));
				fd.SourceCorrectStructure=list.get(colSourceCorrectStructure);
				fd.Smiles=list.get(colSmiles);
				
				CheckForMolFiles2(fd);
				
//				System.out.println(fd.HaveChemidplus3DMolFile);
				
				fd.NIH3DCorrect=list.get(colNIH3DCorrect);
				fd.NIST2DCorrect=list.get(colNIST2DCorrect);
				fd.NIST3DCorrect=list.get(colNIST3DCorrect);
				fd.DougDB3DCorrect=list.get(colDougDB3DCorrect);
				fd.IndexNet2DCorrect=list.get(colIndexNet2DCorrect);
				fd.Chemidplus3DCorrect=list.get(colChemidplus3DCorrect);
				fd.DSSTox2DCorrect=list.get(colDSSTox2DCorrect);
				fd.Name2DCorrect=list.get(colName2DCorrect);
				fd.SmilesCorrect=list.get(colSmilesCorrect);

//				if (fd.CAS.equals("112-02-7")) {
//					//dont fix it
//				} else {
//					fixCorrectFields(srcs, fd);	
//				}
				
				fixCorrectFields(srcs, fd);	

				
				fw.write(fd.toStringStructureData2("|"));
				fw.flush();
				
			}
			
			br.close();
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param srcs
	 * @param fd
	 * @param ms
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	private void fixCorrectFields(String[] srcs, FileData fd) throws NoSuchFieldException,
			IllegalAccessException, Exception {
		
		AtomContainerSet ms=null;
		
		for (int i=0;i<srcs.length;i++) {
			
			String strHaveField="Have"+srcs[i];
			
			if (!srcs[i].equals("Smiles")) strHaveField+="MolFile";
			
			Field fieldHaveField=fd.getClass().getField(strHaveField);
			boolean have=fieldHaveField.getBoolean(fd);
			
			String strCorrectField=srcs[i]+"Correct";
			Field fieldCorrect=fd.getClass().getField(strCorrectField);
			
			String correct=(String)fieldCorrect.get(fd);
			
			if (have && correct.equals("") && fd.Validated) {
				
				System.out.println(fd.CAS);

				if (ms==null) {
					ms=this.getAtomContainers2(fd);
				}
				
				IAtomContainer mol=null;
				String srcMol=srcs[i];
				if (!srcs[i].equals("Smiles")) srcMol+="MolFile";
				
				for (int j=0;j<ms.getAtomContainerCount();j++) {
					IAtomContainer mj=ms.getAtomContainer(j);
					String srcj=(String)mj.getProperty("Source");

					if (srcj.equals(srcMol)) {
						mol=mj;
						break;
					}
				}
				
				AtomContainerSet AtomContainerSet = (AtomContainerSet) ConnectivityChecker
					.partitionIntoMolecules(mol);
				if (AtomContainerSet.getAtomContainerCount()>1) {
					continue;
				} 

				
				for (int j=0;j<ms.getAtomContainerCount();j++) {
					IAtomContainer mj=ms.getAtomContainer(j);
					String srcj=(String)mj.getProperty("Source");

					if (srcj.equals(srcMol)) continue;

					String correctj="";
					
					if (srcj.equals("Other")) {
						correctj="true";
					} else {
						String strCorrectj=srcj.replace("MolFile", "")+"Correct";
						Field fieldCorrectj=fd.getClass().getField(strCorrectj);
						correctj=(String)fieldCorrectj.get(fd);
					}
					
					//if srcj doesn't have the correct field set, then continue:
					if (correctj==null || correctj.equals("")) {
//									System.out.println("No good:"+fd.CAS+"\t"+srcMol+"\t"+srcj+"\t"+correctj);
						continue;
					}
					
					//now compare the 2 structures:
					boolean match=chemicalcompare.isIsomorphHybrid(mj, mol, true,true,true);
					
					//If structures match, set correct field to that of srcj:
					if (match) {
						fieldCorrect.set(fd,correctj);
//								System.out.println(fd.CAS+"\t"+srcMol+"\t"+srcj+"\t"+correctj+"\tFixed!");
						break;
					}
				
				}//end loop over molecules we have

			} else if (!have) {//end if need Correct to be fixed
				fieldCorrect.set(fd,"");

				if (fd.SourceCorrectStructure.equals(srcs[i])) {
					System.out.println(fd.CAS+"\tWarning: structure missing for SourceCorrectStructure");
				}
			}
			
		}//end loop over srcs
		
	}
	
	void CheckForBadElement(IAtomContainer mol,FileData fd) {
		
		try {
			fd.BadElement = "false";
			
			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

					fd.BadElement = "true";
					// System.out.println(fd.CAS+" has bad element: "+var);
					break;
				}
			}

		} catch (Exception e) {
			fd.BadElement = "N/A";
		}
	}
	
	
	
	

	
	
	
	
	
	
	
	public static boolean IsSalt(IAtomContainer molecule) {
		
		
	    AtomContainerSet AtomContainerSet;
	    
		   try {
			   //clone molecule to preserve it:
			  		   
			   AtomContainerSet = (AtomContainerSet) ConnectivityChecker
				.partitionIntoMolecules(molecule);
			   if (AtomContainerSet.getAtomContainerCount()>1) {
				   return true;
			   } else return false;
			   
			   
		   } catch (Exception e) {
			   return true;
		   }

	}
	
	LinkedList LookupLineInTextFile(String filename,String CAS) {
		String Line="";
		try {
			File myFile=new File(filename);
			
			BufferedReader br=new BufferedReader(new FileReader(myFile));
			
			
			while (true) {
				Line=br.readLine();								
				
				if (Line ==null) break;
				if (Line.equals("")) break;
			
				LinkedList l=null;
				
				if (Line.indexOf("|")>-1)
					l=Utilities.Parse(Line,"|");
				else if (Line.indexOf("\t")>-1) {
					l=Utilities.Parse(Line,"\t");
				}
				
				String currentCAS=(String)l.get(0);
				
				if (currentCAS.equals(CAS))  {					
//					System.out.println(Line);
//					System.out.println(l.size());
					return l;
				}
			}			
			
			br.close();
			
			
			
		} catch (Exception e) {
			System.out.println(Line);
			e.printStackTrace();
		}
		return null;		
		
	}
	
	
	void CompareALOGPForTwoMolFiles(String CAS,String src1,String src2) {
		try {

			IAtomContainer m1=this.getAtomContainerFromSource(src1, CAS);

			IAtomContainer m2=this.getAtomContainerFromSource(src2, CAS);

			DescriptorFactory df=new DescriptorFactory(true);
			DescriptorData dd=new DescriptorData();

			df.Calculate3DDescriptors=false;
			df.alogp.debug=true;
			System.out.println(src1);
			df.CalculateDescriptors(m1, dd, false);

			ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m1, CAS+"_"+src1, "ToxPredictor/Check");
			ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m2, CAS+"_"+src2, "ToxPredictor/Check");

			//		for (int i=0;i<df.rs.getAtomContainerCount();i++) {
			//			org.openscience.cdk.Ring r=(org.openscience.cdk.Ring)df.rs.getAtomContainer(i);
			//			System.out.println(i+"\t"+r.getFlag(CDKConstants.ISAROMATIC));
			//		}	
			//		System.out.println(dd.ALOGP);
			//		System.out.println("\n");
			System.out.println(src2);
			df.CalculateDescriptors(m2, dd, false);
			//		for (int i=0;i<df.rs.getAtomContainerCount();i++) {
			//			org.openscience.cdk.Ring r=(org.openscience.cdk.Ring)df.rs.getAtomContainer(i);
			//			System.out.println(i+"\t"+r.getFlag(CDKConstants.ISAROMATIC));
			//		}
			//		System.out.println(dd.ALOGP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	void PerformCalcsForNewFHMChemicals() {
	
		this.MolSrc="folder";
		
		// parse chemidplus files and Create
		// StructureData,SystematicNameData,Synonyms, and Formula entries
		// remember to add these entries to corresponding tables in access db
//		File extrafhmfolder=new File("ToxPredictor/chemidplus/chemidplus extra fhm3");
//		RetrieveDataFromFolder(extrafhmfolder,true,false,true,true,true);
		
		
		// **********************************************************************
		//create StructureData2 entries:
//		String CASfolder="ToxPredictor/chemidplus/parse";

//		String CASfile="CAS numbers for additional fhm chemicals2.txt";
//		String OutputFileName="StructureData2-additional fhm chemicals2.txt";
		
//		String CASfile="CAS numbers for additional fhm chemicals3.txt";
//		String OutputFileName="StructureData2-additional fhm chemicals3.txt";

//		CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,false);
		
		
		// ******************************************************************
		// check structures:
//		String CASfolder="ToxPredictor/chemidplus/compare";		
//		String CASfile="CAS list for fhm4.txt"; //this file has been filtered to remove chemicals w/o structures
//		String outputfolder="ToxPredictor/chemidplus/compare/additional fhm4";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra fhm2.txt";

		
		String CASfolder="ToxPredictor/chemidplus/parse";
		String CASfile="CAS list for changed have fields.txt"; //this file has been filtered to remove chemicals w/o structures
		String outputfolder="ToxPredictor/chemidplus/compare/changed have fields";
		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";

		
//		String CASfile="CAS list for fhm5.txt"; //this file has been filtered to remove chemicals w/o structures
//		String outputfolder="ToxPredictor/chemidplus/compare/additional fhm5";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra fhm3.txt";
		

		CompareStructuresForGoodDataChemicals2(CASfile,CASfolder,outputfolder,structuredatafilepath,"Fingerprinter");
	}
	
	void PerformStructureData2CalcsForAllChemicals() {
		
		this.MolSrc="folder";
//		this.MolSrc="jar";
		
		// **********************************************************************
		//create StructureData2 entries:
		String CASfolder="ToxPredictor/chemidplus/parse";

		String CASfile="CAS numbers for all chemicals-4-29-08.txt";
//		String CASfile="test.txt";
		
		String OutputFileName="StructureData2-all-4-29-08.txt";
//		String OutputFileName="StructureData2-test.txt";
		
		
		CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,true,false);
		
	}
	void PerformCalcsForNewRainbowTroutChemicals() {
	
		this.MolSrc="folder";
		
		// parse chemidplus files and Create
		// StructureData,SystematicNameData,Synonyms, and Formula entries
		// remember to add these entries to corresponding tables in access db
//		File extrafhmfolder=new File("ToxPredictor/chemidplus/chemidplus extra rainbow trout");
//		RetrieveDataFromFolder(extrafhmfolder,true,false,true,true,true);
		
		
		// **********************************************************************
		//create StructureData2 entries:
//		String CASfolder="ToxPredictor/chemidplus/parse";
//		String CASfile="CAS numbers for additional rt chemicals.txt";
//		String OutputFileName="StructureData2-additional rt chemicals.txt";
//
//		CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,false);
		
		
		// ******************************************************************
		// check structures:
		String CASfolder="ToxPredictor/chemidplus/compare";

		String CASfile="CAS list for new rt.txt"; //this file has been filtered to remove chemicals w/o structures
		String outputfolder="ToxPredictor/chemidplus/compare/additional rainbow trout";
		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra rainbow trout.txt";

		this.
		CompareStructuresForGoodDataChemicals2(CASfile,CASfolder,outputfolder,structuredatafilepath,"Fingerprinter");
	}
	void PerformCalcsForNewBlueGillChemicals() {
	
		this.MolSrc="folder";
		
		// parse chemidplus files and Create
		// StructureData,SystematicNameData,Synonyms, and Formula entries
		// remember to add these entries to corresponding tables in access db
//		File extrafhmfolder=new File("ToxPredictor/chemidplus/chemidplus extra bluegill");
//		RetrieveDataFromFolder(extrafhmfolder,true,false,true,true,true);
		
		
		// **********************************************************************
		//create StructureData2 entries:
//		String CASfolder="ToxPredictor/chemidplus/parse";
//		String CASfile="CAS numbers for additional bg chemicals.txt";
//		String OutputFileName="StructureData2-additional bg chemicals.txt";
//
//		CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,false);
		
		
		// ******************************************************************
		// check structures:
//		dont need to since all were completed with rt chemicals
	}
	
	
	/**
	 * Checks if Have field and Correct field = true for RevisedSource
	 */
	void CheckStructureData2(String structuredata2filepath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(structuredata2filepath));
			
			String header=br.readLine();
			java.util.List hlist=Utilities.Parse(header, "\t");
			
			int colStructureSource=Utilities.GetColumnNumber("StructureSource",hlist);
			int colSourceCorrectStructure=Utilities.GetColumnNumber("SourceCorrectStructure",hlist);
			
			while (true) {
//			for (int i = 1; i <= 10000; i++) {
				String Line = br.readLine();
				if (Line==null) break;

				java.util.List list = Utilities.Parse(Line, "\t");

				String StructureSource = (String) list.get(colStructureSource);
				String SourceCorrectStructure = (String) list
						.get(colSourceCorrectStructure);
				String RevisedSource = "";

				String CAS = (String) list.get(0);
				
				if (!SourceCorrectStructure.equals("")) {
					RevisedSource = SourceCorrectStructure;
				} else {
					RevisedSource = StructureSource;
				}

				if (RevisedSource.equals("N/A")) continue;
				
				
				// ************************************************************
				
				String colNameHaveRevisedSource = "Have" + RevisedSource;

				if (RevisedSource.equals("Smiles")) {
					colNameHaveRevisedSource="Smiles";
				}
				
				String HaveRevisedSource;
				
				if (!RevisedSource.equals("Other")) {
					int colHaveRevisedSource = Utilities.GetColumnNumber(
							colNameHaveRevisedSource, hlist);
					if (colHaveRevisedSource == -1)
						System.out.println("error:" + colNameHaveRevisedSource);

					HaveRevisedSource= (String) list.get(colHaveRevisedSource);
				} else {
					HaveRevisedSource= this.HaveMolFile(CAS,this.OtherFolder)+"";
//					System.out.println(CAS+"\t"+HaveRevisedSource);
				}
						
				// System.out.println(RevisedSource+"\t"+HaveRevisedSource);

				// ************************************************************
				String colNameRevisedSourceCorrect = RevisedSource.replace(
						"MolFile", "")
						+ "Correct";
				
				String RevisedSourceCorrect="";
				
				if (RevisedSource.equals("Other")) {
					RevisedSourceCorrect="true";
				} else {
					int colRevisedSourceCorrect = Utilities.GetColumnNumber(
							colNameRevisedSourceCorrect, hlist);

					if (colRevisedSourceCorrect == -1)
						System.out.println("error:" + colNameRevisedSourceCorrect);
					RevisedSourceCorrect = (String) list
							.get(colRevisedSourceCorrect);
					
				}
				
				// System.out.println(RevisedSource+"\t"+RevisedSourceCorrect);

				if (RevisedSource.equals("Smiles")) {
					if (HaveRevisedSource.equals("missing")) {
						System.out.println(CAS+"\tSmiles\tmissing smiles");
					} else if (!RevisedSourceCorrect.equals("true")) {
						System.out.println(CAS+"\tSmiles\t"+RevisedSourceCorrect);
					}
					
				} else {
					if (HaveRevisedSource.equals("false")) {
						System.out.println(CAS+"\t"+RevisedSource+"\t"+"Structure Missing");
					}
					if (!RevisedSourceCorrect.equals("true")) {
						System.out.println(CAS+"\t"+RevisedSource+"\tRevisedSourceCorrect="+RevisedSourceCorrect);
					}
				}
				
//				System.out.println(RevisedSource + "\t" + HaveRevisedSource
//						+ "\t" + RevisedSourceCorrect);
			}
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		
	}
	
	void createStructureManifest(String folderpath) {

		File Folder = new File(folderpath);

		File[] files = Folder.listFiles();

		try {
			MDLV2000Reader mr=null; 			

			FileWriter fw = new FileWriter(folderpath + "/manifest.txt");

			fw.write("CAS\tformula\r\n");
			
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory())
					continue;
				if (file.getName().indexOf(".mol") == -1)
					continue;

//				if (i == 10)
//					break;

				
				String CAS=file.getName().substring(0,file.getName().indexOf("."));

				BufferedReader br=new BufferedReader(new FileReader(file));
				mr=new MDLV2000Reader(br);
	            IAtomContainer molecule = mr.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));

	            br.close();
				
//				MyHydrogenAdder myHA = new MyHydrogenAdder();
//				AtomContainer mol_w_hydrogens = myHA.AddExplicitHydrogens(mol);
				
				// First, get the molecular formula of the chemical
				
				String formula="N/A";
				
				try {
					
					formula = CDKUtilities.generateFormula(molecule);
				} catch (Exception ex1){
					formula="error";
				}
				
				System.out.println(CAS+"\t"+formula);
				
//				if (formula.indexOf("H")==-1) {
//					System.out.println(CAS+"\t"+formula);
//				}
				
				fw.write(CAS+"\t"+formula+"\r\n");
				fw.flush();
				

			}
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void deleteExtraFilesFromChemidplusDownloadFiles() {
		File folder=new File("ToxPredictor/chemidplus");
		
		for (int i=0;i<folder.listFiles().length;i++) {
			File folderi=folder.listFiles()[i];
			System.out.println(folderi.getName());
			
			if (!folderi.isDirectory()) continue;
			
			for (int j=0;j<folderi.listFiles().length;j++) {
				File folderj=folderi.listFiles()[j];
				if (!folderj.isDirectory()) continue;
				if (folderj.getName().indexOf("_files")==-1) continue;
//				System.out.println(folderj.getName());
				
				for (int k=0;k<folderj.listFiles().length;k++) {
					File filek=folderj.listFiles()[k];
					if (filek.isDirectory()) continue;
					if (filek.getName().equals("RenderImage.png")) continue;
					
					String n=filek.getName();
					
					if (n.equals("arrowYellowffcc66.gif") 
							|| n.equals("banner.gif")
							|| n.equals("bannerInsetTox.gif")
							|| n.equals("boxLeftEdgeBgSlice.gif")
							|| n.equals("boxRightEdgeBgSlice.gif")
							|| n.equals("boxSECornerRound.gif")
							|| n.equals("boxTopEdgeBgSlice.gif")
							|| n.equals("chemidstyles.css")
							|| n.equals("infosmall.gif")
							|| n.equals("pageFunctions.js")
							|| n.equals("query.js")
							|| n.equals("shim.gif")
							|| n.equals("sisHoroRule3b5c7f-2.gif")
							|| n.equals("chembannerAdvanced.jpg")
							|| n.equals("nlmHeaderLogo.gif")
							|| n.equals("whiteline.gif")
							|| n.equals("sisHoroRule3b5c7f.gif")) {
						filek.delete();
					}
//					System.out.println(filek.getName());
					
				}// end k loop
				
				//repeat delete 
				for (int k=0;k<folderj.listFiles().length;k++) {
					File filek=folderj.listFiles()[k];
					if (filek.isDirectory()) continue;
					if (filek.getName().equals("RenderImage.png")) continue;
					
					String n=filek.getName();
					
					if (n.equals("arrowYellowffcc66.gif") 
							|| n.equals("banner.gif")
							|| n.equals("bannerInsetTox.gif")
							|| n.equals("boxLeftEdgeBgSlice.gif")
							|| n.equals("boxRightEdgeBgSlice.gif")
							|| n.equals("boxSECornerRound.gif")
							|| n.equals("boxTopEdgeBgSlice.gif")
							|| n.equals("chemidstyles.css")
							|| n.equals("infosmall.gif")
							|| n.equals("pageFunctions.js")
							|| n.equals("query.js")
							|| n.equals("shim.gif")
							|| n.equals("sisHoroRule3b5c7f-2.gif")
							|| n.equals("sisHoroRule3b5c7f.gif")) {
						filek.delete();
					}
//					System.out.println(filek.getName());
					
				}
			}// end j loop
			
			
			
		}// end i loop

	}
	
	void CombineSynonymsIntoOneRecord() {
		String folder="ToxPredictor/chemidplus/parse";
		
		String inputfilename="SynonymData.txt";
		String outputfilename="SynonymData-one record per chemical.txt";
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+inputfilename));
			
			FileWriter fw=new FileWriter(folder+"/"+outputfilename);
			
			String CAS="";
			Vector<String>synonyms=null;
			
			while (true) {
				String Line=br.readLine();
				
				
				if (Line==null) break;
				
				String [] data=Line.split("\t");
				
				String newCAS=data[0];
				String newSynonym=data[1];
				
				newSynonym=newSynonym.replace("\"", "");
				
				if (!newCAS.equals(CAS)) {
					
					if (synonyms!=null) {
						
						Collections.sort(synonyms);
						
						for (int i=1;i<synonyms.size();i++) {
							if (synonyms.get(i).equals(synonyms.get(i-1))) {
								synonyms.remove(i--);
							}
						}
						
						fw.write(CAS+"\t");
						for (int i=0;i<synonyms.size();i++) {
							fw.write(synonyms.get(i));
							if (i<synonyms.size()-1) {
								fw.write(";");
							} else {
								fw.write("\r\n");
								fw.flush();
							}
						}
					}
					
					synonyms=new Vector<String>();
					CAS=newCAS;
				} 
				
				synonyms.add(newSynonym);
				
			}
			
			
			br.close();
			fw.close();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// new ParseChemidplus().ParseData();

		// new ParseChemidplus().CreateStructureWebPage(6,27,2);

		ParseChemidplus p = new ParseChemidplus();
		
//		p.deleteExtraFilesFromChemidplusDownloadFiles();
//		File bob=new File(p.NIHMolFolder+"/"+"1700-02-3");
////		File bob=new File(p.NIHMolFolder);
//		System.out.println(bob.exists());
		
//		p.CombineSynonymsIntoOneRecord();
		
//		if (true) return;
		
 
		String folder="ToxPredictor/chemidplus/parse/additional epiphys mp chemicals";
		String oldstructuredata2filename="StructureData2.txt";
		String newstructuredata2filename="StructureData2New.txt";
		String CASlistfilename ="CAS list need structuredata2.txt";
		p.RemoveRecordsFromStructureData2(folder, oldstructuredata2filename, newstructuredata2filename, CASlistfilename);
		
		//
		
//		p.createStructureManifest(p.Chemidplus3dMolFolder);
//		p.createStructureManifest(p.DougMolFolder);
//		p.createStructureManifest(p.IndexNet2DFolder);
//		p.createStructureManifest(p.DSSToxFolder);
//		p.createStructureManifest(p.NameFolder);
//		p.createStructureManifest(p.NIST2DFolder);
//		p.createStructureManifest(p.NIST3DFolder);
//		p.createStructureManifest(p.SmilesFolder);
		
		
//		if (true) return;
//		p.CompareStructuresForChemicalsWithOtherStructure();
		
		
//		String structuredata2filepath="ToxPredictor/chemidplus/parse/StructureData2-8-11-09.txt";
//		p.CheckStructureData2(structuredata2filepath);
		
		
//		p.FigureOutWhichOnesWerentDownloadedFromChemidplus();
		
//		p.PerformStructureData2CalcsForAllChemicals();
		
//		p.PerformCalcsForNewFHMChemicals();
//		p.PerformCalcsForNewRainbowTroutChemicals();
//		p.PerformCalcsForNewBlueGillChemicals();
		
//		p.CompareALOGPForTwoMolFiles("1214-39-7","DougDB3DMolFile","IndexNet2DMolFile");
//		p.CompareALOGPForTwoMolFiles("41814-78-2","DougDB3DMolFile","IndexNet2DMolFile");	
//		p.CompareALOGPForTwoMolFiles("2866-43-5","DougDB3DMolFile","IndexNet2DMolFile");
		

		
//		p.CompareALOGPForTwoMolFiles("5331-91-9","NIH3DMolFile","IndexNet2DMolFile");
		
//		p.CompareSmilesToBestStructure();
		
//		if (true) return;
		
	
// ***************************************************************
		
//		FileData fd=new FileData();
//		
////				
//		
//		fd.CAS="71-43-2";
////		fd.CAS="54-80-8";
////		fd.CAS="452-06-2";
//		
//		p.MolSrc="jar";
//		p.MolSrc="notjar";
//		
//		p.CheckForMolFiles2(fd);
//		AtomContainerSet ims=p.getAtomContainers2(fd);
//		
//		
//		for (int i=0;i<ims.getAtomContainerCount();i++) {
//			AtomContainer m=ims.getAtomContainer(i);
////			System.out.println(m.getAtomCount());
//			System.out.println((i+1)+"\t"+m.getAtomCount()+"\t"+m.getProperty("Source")+"");
//		}
//		
//		if (true) return;
		
		
		// ***************************************************************
				
//		p.CompareStructuresForGoodDataChemicals2("cas list for logkow.txt","ToxPredictor/chemidplus/compare","ToxPredictor/chemidplus/compare/LogKow-fingerprinter","ToxPredictor/chemidplus/parse/StructureData14738.txt","Fingerprinter");
//		p.CompareStructuresForGoodDataChemicals2("cas list for logkow.txt","ToxPredictor/chemidplus/compare","ToxPredictor/chemidplus/compare/LogKow-UIT","ToxPredictor/chemidplus/parse/StructureData14738.txt","UIT");
//		p.CompareStructuresForGoodDataChemicals2("cas list for logkow.txt","ToxPredictor/chemidplus/compare","ToxPredictor/chemidplus/compare/LogKow-2D-Descriptors","ToxPredictor/chemidplus/parse/StructureData14738.txt","2D-Descriptors");
	
		
//		p.DSSToxFolder="ToxPredictor/DescriptorTextTables/Cancer data files/mol files v5a with H";
//		p.CompareStructuresForGoodDataChemicals2("CAS numbers for additional CPDB and NTP chemicals.txt","ToxPredictor/chemidplus/parse","ToxPredictor/chemidplus/compare/additional CPDB and NTP chemicals","ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra CPDB and NTP.txt","Fingerprinter");
//		p.CompareStructuresForGoodDataChemicals2("cas list for NTP chemicals not in CPDB.txt","ToxPredictor/chemidplus/compare","ToxPredictor/chemidplus/compare/NTP chemicals not in CPDB","ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra CPDB and NTP.txt","Fingerprinter");

//		p.CompareStructuresForGoodDataChemicals2("CAS numbers for additional topkat ld50 chemicals.txt","ToxPredictor/chemidplus/parse","ToxPredictor/chemidplus/compare/additional topkat ld50","ToxPredictor/chemidplus/parse/StructureData/StructureData_chemidplus extra topkat ld50.txt");

		
//		String CASFile="CAS list for cancer amines.txt";
//		String CASFolder="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/compare";
//		String outputFolder="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/compare/cancer_amines";
//		String structuredatafilepath="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/parse/StructureData2.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list BP NIST.txt";
//		String CASFolder="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/parse";
//		String outputFolder="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/compare/BP_NIST_chemicals";
//		String structuredatafilepath="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/parse/StructureData.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list for additional bcf chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional bcf";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list for additional BCF 701 chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional bcf 701";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
//		String outputFolder="ToxPredictor/chemidplus/compare/bcf sdf";
//		String SDFpath="ToxPredictor/DescriptorTextTables/BCF data files/BCF.sdf";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder);

		
//		String outputFolder="ToxPredictor/chemidplus/compare/IGC50_Names_from_Papers2";
//		String SDFpath="C:/Documents and Settings/tmarti02/My Documents/comptox/IGC50/IGC50 name structures2.sdf";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String structuredata2filepath="ToxPredictor/chemidplus/parse/StructureData2-10-18-11.txt";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder,structuredata2filepath);

		
//		String CASFile="CAS list for additional 643 bcf chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/bcf643";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
//		String CASFile="CAS list for additional viscosity chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional viscosity";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="cas list for ST chemicals need structuredata2.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional ST-omit ones with only 1 structure";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
////		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="cas list for ST.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/ST";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
////		String comparemethod="Fingerprinter";
//		String comparemethod="2D-Descriptors";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
		
//		String CASFile="CAS list for mutagenicity chemicals not in structuredata2.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/mutagenicity_unc";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list for extra thermal conductivity chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/thermalconductivity";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

		
//		String CASFile="CAS list good mace barron chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/mace barron good";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

////		String CASFile="CAS list benfenati other chemicals.txt";
//		String CASFile="cas list benfenati other chemicals3.txt";
////		String CASFile="cas list benfenati special chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/benfenati other chemicals3";
////		String outputFolder="ToxPredictor/chemidplus/compare/benfenati special chemicals";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod=chemicalcompare.methodHybrid;
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
//
//		
//		if (true) System.exit(0);
		
//		String CASFile="CAS Numbers for additional green algae compounds.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional green algae";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData-9-28-09.txt";
////		String comparemethod="Fingerprinter";
//		String comparemethod="UIT";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list for WS chemicals not validated in StructureData2.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional WS";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
////		String comparemethod="UIT";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
//		String CASFile="CAS list need structuredata2.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse/additional epiphys mp chemicals";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional mp";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

		
//		String CASFile="cas list for extra density chemicals that need validation.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional Density";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//////		String comparemethod="UIT";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);

//		String CASFile="CAS list for additional physical property chemicals-6-21-11-3.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional physical property";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//////		String comparemethod="UIT";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
//		String CASFile="cas list extra IGC50 schwobel chemicals.txt";
//		String CASFolder="ToxPredictor/chemidplus/parse/additional IGC50 schwobel chemicals";
//		String outputFolder="ToxPredictor/chemidplus/compare/additional IGC50 schwobel";
//		String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureDataNew.txt";
//		String comparemethod="Fingerprinter";
//		p.CompareStructuresForGoodDataChemicals2(CASFile, CASFolder, outputFolder, structuredatafilepath, comparemethod);
		
		
//		String outputFolder2="ToxPredictor/chemidplus/compare/bcf643_sdf";
//		String SDFpath="ToxPredictor/benfenati/643 BCF set/tot_643-3.sdf";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder2);

//		String outputFolder2="ToxPredictor/chemidplus/compare/mutagenicity_unc_sdf";
//		String SDFpath="C:/Documents and Settings/tmarti02/My Documents/comptox/mutagenicity-unc/AMES_OVERALL_SET w CAS dearomatize CAS sort.SDF";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder2);

//		String outputFolder2="ToxPredictor/chemidplus/compare/reproToxSDF";
//		String SDFpath="ToxPredictor/benfenati/repro tox set/Develop_tox_260907_2.sdf";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder2);
		
//		String outputFolder2="ToxPredictor/chemidplus/compare/CPDBv5d";
//		String SDFpath="C:/Documents and Settings/tmarti02/My Documents/comptox/cancer/CPDB v5d/CPDBAS_v5d.sdf";
//		p.CompareStructuresForSDF_ToOurStructures(SDFpath, structuredatafilepath,outputFolder2);
		
		
//		if (true) return;

		// ***************************************************************

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra mace barron");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra viscosity");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);
		
//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra bcf2");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);
		
//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra fhm");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra IGC50");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra topkat ld50");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);
		
//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra CPDB and NTP");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra mutagenicity");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus extra reprotox");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

//		File chemfolder=new File("ToxPredictor/chemidplus/chemidplus-9-24-09");
//		p.RetrieveDataFromFolder(chemfolder,true,false,true,true,true);

		
//		if (true) return;
		 //* *********************************************************************************
		
//		String CASfolder="ToxPredictor/chemidplus/parse";

//		String CASfile="CAS numbers for additional fhm chemicals2.txt";
//		String OutputFileName="StructureData2-additional fhm chemicals2.txt";

//		String CASfile="CAS list for additional 643 bcf chemicals-not in structuredata2.txt";
//		String OutputFileName="StructureData2-additional bcf 643 chemicals.txt";

//		String CASfile="CAS Numbers for additional IGC50 chemicals.txt";
//		String OutputFileName="StructureData2-additional IGC50 chemicals.txt";

//		String CASfile="CAS numbers in Chemidplus database 7-17-07.txt";
//		String OutputFileName="StructureData2-7-17-07.txt";

//		String CASfile="CAS numbers for additional topkat ld50 chemicals.txt";
//		String OutputFileName="StructureData2-additional topkat ld50 chemicals.txt";

//		String CASfile="CAS numbers for additional CPDB and NTP chemicals.txt";
//		String OutputFileName="StructureData2-additional CPDB and NTP chemicals.txt";
		
//		String CASfile="CAS list BP NIST.txt";
//		String CASfolder="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/chemidplus/parse";
//		String OutputFileName="StructureData2-BP NIST chemicals.txt";

//		String CASfile="CAS list for mutagenicity chemicals not in structuredata2.txt";
//		String OutputFileName="StructureData2-additional mutagenicity chemicals.txt";

//		String CASfile="CAS list for new reproTox chemicals.txt";
//		String OutputFileName="StructureData2-additional reproTox chemicals.txt";

//		String CASfile="CAS list bob.txt";
//		String OutputFileName="StructureData2-bob.txt";

//		String CASfile="CAS Numbers for additional green algae compounds.txt";
//		String OutputFileName="StructureData2-additional green algae.txt";

//		String CASfile="CAS numbers in union of datasets not in SD2-10-13-09.txt";
//		String OutputFileName="StructureData2-in union of datasets not in SD2-10-13-09.txt";

//		String CASfile="CAS numbers in SD2 10-14-09.txt";
//		String OutputFileName="StructureData2-fix Have fields.txt";
		
//		String CASfile="CAS list 71022-43-0.txt";
//		String OutputFileName="StructureData2-71022-43-0.txt";
		
//		String CASfile="CAS list for additional mace barron chemicals.txt";
//		String OutputFileName="StructureData2-additional mace barron chemicals.txt";

//		String CASfile="CAS list bob.txt";
//		String OutputFileName="StructureData2-bob.txt";
		
//		String CASfile="CAS list need structuredata2.txt";
//		String OutputFileName="StructureData2-extra FP.txt";
		
//		String CASfile="cas list for extra density chemicals that need validation.txt";
//		String OutputFileName="StructureData2-extra density need validation.txt";

		
//		String CASfile="CAS list for extra thermal conductivity chemicals.txt";
//		String OutputFileName="StructureData2-extra thermal conductivity chemicals.txt";

//		String CASfile="CAS numbers for appendix a chemicals not in SD2.txt";
//		String OutputFileName="StructureData2-extra appendix a chemicals.txt";

//		String CASfile="CAS list for WS chemicals not validated in StructureData2.txt";
//		String OutputFileName="StructureData2-extra WS chemicals.txt";
		
//		String CASfile="CAS list for additional BCF 701 chemicals.txt";
//		String OutputFileName="StructureData2-extra BCF 701 chemicals.txt";

//		String CASfile="22311-25-7.txt";
//		String OutputFileName="StructureData2-22311-25-7.txt";
//		
//		String CASfile="cas list for ST chemicals need structuredata2.txt";
//		String OutputFileName="StructureData2-extra ST chemicals.txt";
		
//		String CASfile="CAS list for additional physical property chemicals-6-21-11-3.txt";
//		String OutputFileName="StructureData2-extra physical property chemicals.txt";

//		String CASfile="cas list 8-26-11_2.txt";
//		String OutputFileName="StructureData2-8-26-11_2.txt";
		
//		String CASfolder="ToxPredictor/chemidplus/parse/additional epiphys mp chemicals";
//		String CASfile ="CAS list need structuredata2.txt";
//		String OutputFileName="StructureData2-extra mp chemicals.txt";

		
//		String CASfile ="cas list need structure data2-8-29-11.txt";
//		String OutputFileName="StructureData2-8-29-11.txt";
//		
//		String CASfile="cas list extra IGC50 schwobel chemicals.txt";
//		String CASfolder="ToxPredictor/chemidplus/parse/additional IGC50 schwobel chemicals";
//		String OutputFileName="StructureData2-IGC50 schwobel.txt";
//		p.CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,true,true);

		
//		String CASfile="cas list add benfenati to structuredata2.txt";
//		String CASfolder="ToxPredictor/chemidplus/parse";
//		String OutputFileName="StructureData2-extrabenfenati.txt";
//		p.CreateStructureData2Table(CASfolder,CASfile,OutputFileName,CASfolder,true,true);


		
//		p.CreateStructureData2Table("1196-92-5",CASfolder,true,true);
		
//		if (true) return;
		
//		 *********************************************************************************
//		AtomContainer molecule =p.LoadChemicalFromMolFile("26545-62-0",p.ChemidplusMolFolder);	
////		MyHydrogenAdder myHA=new MyHydrogenAdder();
////		molecule=myHA.AddExplicitHydrogens(molecule);
//
//		HydrogenAdder hydrogenAdder = new HydrogenAdder("org.openscience.cdk.tools.ValencyChecker");
//										
//		try {
//			hydrogenAdder.addHydrogensToSatisfyValency(molecule);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		if (true) return;
// *********************************************************************************		

//		MolFileUtilities mfu= new MolFileUtilities();
//		AtomContainer m=p.LoadChemicalFromMolFile("6109-97-3","ToxPredictor/MOL Files");
//		SaveStructureToFile.CreateImageFile(m,"6109-97-3","ToxPredictor/temp",false,true,true,350);
					
//		p.CompareStructuresForGoodDataChemicals();
		
		
//		p.CompareStructuresForSmiles();
//		p.CompareStructuresForSmiles2();
		
		
//		p.CompareStructuresFromDSSToxToBestStructure();
//		p.CompareStructuresFromDSSToxToBestStructureForGoodDataChemicals();
//		p.ParseDSSTox();
		
//		if (true) return;

// ***********************************************************
//		p.CompareStructuresFromNameToBestStructure("1_1000");
//		p.CompareStructuresFromNameToBestStructure("1000_1998");
//		p.CompareStructuresFromNameToBestStructure("1999_3000");
//		p.CompareStructuresFromNameToBestStructure("3001_4000");
//		p.CompareStructuresFromNameToBestStructure("4001_5000");
//		p.CompareStructuresFromNameToBestStructure("5001_6000");
//		p.CompareStructuresFromNameToBestStructure("6001_7000");
//		p.CompareStructuresFromNameToBestStructure("7001_8116");
//		if (true) return;
		
		
		
		// ***********************************************************
		//Parse chemid plus html file folders and generate text databases:
//		p.parseChemIDplusHTMLFiles(true,false,false,false,false);		
//		p.parseChemIDplusHTMLFiles(true,true,true,true,true);
		
//		p.parseChemIDplusHTMLFiles(false,true,false,false,false);
		
		
//		if (true) return;
//		 ***********************************************************
		
//		FileData fd=new FileData();
//		fd.CAS="37343-87-6";
//		p.CheckIfNotedChemical(fd);
				
		//combine results from different folders:

//		File folder=new File("ToxPredictor/chemidplus/parse/StructureData");
//		File outputfile=new File("ToxPredictor/chemidplus/parse/StructureData.txt");
//		p.CombineOutputFiles(folder,outputfile);

//		System.out.println(p.HaveMolFileInJar("coords/1000-00-6.mol"));
//		AtomContainer m=p.LoadChemicalFromMolFileInJar("coords/1000-00-6.mol");
//		System.out.println(m.getAtomCount());

		
//		File folder=new File("ToxPredictor/chemidplus/parse/ToxData");
//		File outputfile=new File("ToxPredictor/chemidplus/parse/ToxData.txt");
//		p.CombineOutputFiles(folder,outputfile);
//
//		folder=new File("ToxPredictor/chemidplus/parse/SystematicNameData");
//		outputfile=new File("ToxPredictor/chemidplus/parse/SystematicNameData.txt");
//		p.CombineOutputFiles(folder,outputfile);
//		
//		folder=new File("ToxPredictor/chemidplus/parse/SynonymData");
//		outputfile=new File("ToxPredictor/chemidplus/parse/SynonymData.txt");
//		p.CombineOutputFiles(folder,outputfile);
//
//		folder=new File("ToxPredictor/chemidplus/parse/FormulaData");
//		outputfile=new File("ToxPredictor/chemidplus/parse/FormulaData.txt");
//		p.CombineOutputFiles(folder,outputfile);

		if (true) System.exit(0);

	}

	
	void CheckIfNotedChemical(FileData fd) {
		
		
		String file="ToxPredictor/chemidplus/chemidplus noted chemicals.txt";
		
		try {
		
			BufferedReader br=new BufferedReader(new FileReader(file));
			
			br.readLine();
			
			while (true) {
				
				String Line=br.readLine();
				
				if (Line==null) break;
				
				LinkedList list=Utilities.Parse(Line,"\t");
				
				String currentCAS=(String)list.get(0);
				String Description=(String)list.get(1);
				
				if (currentCAS.equals(fd.CAS)) {
					fd.Description=Description;
//					fd.FormulaCheckSource="Chemidplus";
//					System.out.println(fd.CAS+"\t"+fd.FormulaCheck);
					break;
				}
				
			}
			
			br.close();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
	void CheckIfHaveCorrectedFormula(FileData fd) {
		
		
		String file="ToxPredictor/chemidplus/chemidplus corrected formulas.txt";
		
		try {
		
			BufferedReader br=new BufferedReader(new FileReader(file));
			
			br.readLine();
			
			while (true) {
				
				String Line=br.readLine();
				
				if (Line==null) break;
				
				LinkedList list=Utilities.Parse(Line,"\t");
				
				String currentCAS=(String)list.get(0);
				String correctedFormula=(String)list.get(1);
				
				if (currentCAS.equals(fd.CAS)) {
					fd.Formula=correctedFormula;
					break;
				}
				
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
	public boolean HaveMolFile(String CAS,String folder) {

		try {

//			File f2 = new File(folder);
			// System.out.println(f2.exists());

			String filename = folder + File.separator + CAS + ".mol";
			
			File f = new File(filename);
			
			return (f.exists());

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean HaveMolFileInJar(String filepath) {
		
		try {

						
			java.io.InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filepath);
			
			InputStreamReader isr=new InputStreamReader(ins);
			BufferedReader br=new BufferedReader(isr);

			br.readLine();			
			br.close();
			return true;
			
		} catch (Exception e) {
			return false;	
		}
			
		
	}

	
// boolean HaveDougDBMolFile(String CAS,String folder) {
//
//		try {
//		
//			File f=new File("ToxPredictor/chemidplus/DougCASList.txt");
//			BufferedReader br=new BufferedReader(new FileReader(f));
//			
//			while (true){
//				String Line=br.readLine();								
//				if (Line==null) {
//					br.close();
//					return false;
//				}
//				
//				if (Line.equals(CAS)) {
//					br.close();
//					return true;
//				}
//								
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
	
		
	
	
	
	public static IAtomContainer LoadChemicalFromMolFile(String CAS,String folder) {
		
		try {
			
			String filePath = folder + File.separator + CAS + ".mol";
			File f = new File(filePath);
			
//			System.out.println(filename);
			
			if(!f.exists()) {
				System.out.println(filePath);
				
				return null;
			}

			
			MDLV2000Reader mr=new MDLV2000Reader(new FileInputStream(filePath));
            IAtomContainer molecule = mr.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
						
//			for (int i=0;i<molecule.getAtomCount();i++) {
//				System.out.println(i+"\t"+molecule.getAtom(i).getSymbol()+"\t"+molecule.getAtom(i).getFormalCharge());
//			}

			return molecule;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public IAtomContainer LoadChemicalFromMolFileInJar(String filename) {
		
		try {

			 			
			java.io.InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filename);
			
			
//			System.out.println("*"+filename);
			
			InputStreamReader isr=new InputStreamReader(ins);
			
			
//			BufferedReader br=new BufferedReader(isr);
//			ToxPredictor.Utilities.MDLReader mr=new ToxPredictor.Utilities.MDLReader(); 			
//			AtomContainer molecule=mr.readMolecule(br);

			
			MDLV2000Reader mr=new MDLV2000Reader(isr);
			IAtomContainer molecule = mr.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
			mr.close();

			return molecule;

		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}

	}

	
	/**
	 * Loops through list of cas numbers and generates StructureData2 table
	 *
	 */
	public void CreateStructureData2Table(String CAS,String OutputFolderName,boolean UseDefaultCorrectValues,boolean Validated) {
		
		DescriptorFactory df=new DescriptorFactory(false);
//		MyHydrogenAdder myHA=new MyHydrogenAdder();
		chemicalcompare cc=new chemicalcompare();
//		MFAnalyser mfa=null;
		
		try {

			
			FileWriter fw=new FileWriter(OutputFolderName+"/StructureData2 "+CAS+".txt");
			
			int counter = 0;
			int count1=0;

				try {

					
					FileData fd=new FileData();
					
					if (counter==0)  {
						fw.write(fd.getHeaderStructureData2("|"));
						fw.flush();
					}
					
					fd.CAS=CAS;
					fd.Validated=Validated;
					
//					if (!CAS.equals("477-33-8")) continue;
					
					this.CheckForMolFiles2(fd);
					
					IAtomContainer bestMolecule=this.GetBestMolecule(fd);
					
//					AtomContainerSet AtomContainerSet=this.getAtomContainers2(fd);
					
					String CAS2=CAS;
					while (CAS2.length()<12) {
						CAS2="0"+CAS2;
					}

//					AtomContainer bestMolecule=AtomContainerSet.getAtomContainer(0);
					
					int molcount=0;
					
					if (fd.HaveNIH3DMolFile) molcount++;
					if (fd.HaveDougDB3DMolFile) molcount++;
					if (fd.HaveNIST2DMolFile) molcount++;
					if (fd.HaveNIST3DMolFile) molcount++;
					if (fd.HaveIndexNet2DMolFile) molcount++;
					if (fd.HaveChemidplus3DMolFile) molcount++;	
					if (fd.HaveDSSTox2DMolFile) molcount++;
					if (fd.HaveName2DMolFile) molcount++;
				
					if (!fd.Smiles.equals("missing")) molcount++;
					
					if (molcount==0)
						System.out.println(CAS+"\t"+fd.StructureSource+"\t"+molcount);
					
					if (CAS.equals("10028-15-6")) {
						System.out.println(fd.HaveNIST2DMolFile);
					}
					
					
					if (bestMolecule instanceof IAtomContainer) {
//						fd.StructureSource=(String)bestMolecule.getProperty("Source");
						
						
						
						 AtomContainerSet AtomContainerSet2 = (AtomContainerSet)ConnectivityChecker
							.partitionIntoMolecules(bestMolecule);
						 
						 fd.CalculatedFormula="";
						 for (int mol=0;mol<AtomContainerSet2.getAtomContainerCount();mol++) {
							fd.CalculatedFormula+=CDKUtilities.calculateFormula(AtomContainerSet2.getAtomContainer(mol));
							if (mol<AtomContainerSet2.getAtomContainerCount()-1) {
								fd.CalculatedFormula+=".";
							}
						 }
						 
						 
						 String formulaBest=CDKUtilities.calculateFormula(bestMolecule);
						 
						 
						 try {
							 
							 fd.CalculatedMolecularWeight=CDKUtilities.calculateMolecularWeight(formulaBest);

						 } catch (Exception e) {
//							 System.out.println(fd.CalculatedFormula);
							 fd.CalculatedMolecularWeight=-9999;
						 }
//						System.out.println(fd.CalculatedFormula);
						
						
						this.CheckForBadElement(bestMolecule,fd);
						
					} else {
						fd.StructureSource="N/A";
						fd.BadElement="N/A";
						fd.CalculatedFormula="N/A";
						fd.CalculatedMolecularWeight=-9999;
					}
					
					if (UseDefaultCorrectValues) {
					//if have a chemical in a database, make default correct value true:
						if (fd.HaveNIH3DMolFile) fd.NIH3DCorrect="true";
						if (fd.HaveDougDB3DMolFile) fd.DougDB3DCorrect="true";
						if (fd.HaveNIST2DMolFile) fd.NIST2DCorrect="true";
						if (fd.HaveNIST3DMolFile) fd.NIST3DCorrect="true";
						if (fd.HaveIndexNet2DMolFile) fd.IndexNet2DCorrect="true";
						if (fd.HaveChemidplus3DMolFile) fd.Chemidplus3DCorrect="true";	
						if (fd.HaveDSSTox2DMolFile) fd.DSSTox2DCorrect="true";
						if (fd.HaveName2DMolFile) fd.Name2DCorrect="true";
					
						if (!fd.Smiles.equals("missing")) {
							fd.HaveSmiles=true;
							fd.SmilesCorrect="true";
						}
					}
					
					
					fw.write(fd.toStringStructureData2("|"));
					fw.flush();
					

					counter++;
					
					if (counter % 100 == 0) System.out.println(counter);

				} catch (Exception e) {
					e.printStackTrace();
				}

			
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	/**
		 * Loops through list of cas numbers and generates StructureData2 table
		 *
		 */
		public void CreateStructureData2Table(String CASFolder,String CASFileName,String OutputFileName,String OutputFolderName,boolean UseDefaultCorrectValues,boolean Validated) {
			
			DescriptorFactory df=new DescriptorFactory(false);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
			chemicalcompare cc=new chemicalcompare();
//			MFAnalyser mfa=null;
			
			try {
	
				
//				String CASfile=folder+"/"+"CAS Numbers in Chemidplus database.txt";
//				String CASfile=folder+"/"+"CAS Numbers fhm+rat.txt";
				
				
				BufferedReader br = new BufferedReader(new FileReader(CASFolder+"/"+CASFileName));
				
				FileWriter fw=new FileWriter(OutputFolderName+"/"+OutputFileName);
				
				int counter = 0;
				int count1=0;
	
				while (true) {
	//			while (counter<50) {
					try {
	
						String CAS=br.readLine();
						
						if (CAS==null) break;
						
						FileData fd=new FileData();
						
						if (counter==0)  {
							fw.write(fd.getHeaderStructureData2("|"));
							fw.flush();
						}
						
						fd.CAS=CAS;
						fd.Validated=Validated;
						
	//					if (!CAS.equals("477-33-8")) continue;
						
						this.CheckForMolFiles2(fd);
						
						IAtomContainer bestMolecule=this.GetBestMolecule(fd);
						
//						AtomContainerSet AtomContainerSet=this.getAtomContainers2(fd);
						
						String CAS2=CAS;
						while (CAS2.length()<12) {
							CAS2="0"+CAS2;
						}
	
//						AtomContainer bestMolecule=AtomContainerSet.getAtomContainer(0);
						
						int molcount=0;
						
						if (fd.HaveNIH3DMolFile) molcount++;
						if (fd.HaveDougDB3DMolFile) molcount++;
						if (fd.HaveNIST2DMolFile) molcount++;
						if (fd.HaveNIST3DMolFile) molcount++;
						if (fd.HaveIndexNet2DMolFile) molcount++;
						if (fd.HaveChemidplus3DMolFile) molcount++;	
						if (fd.HaveChemidplus2DMolFile) molcount++;
						if (fd.HaveDSSTox2DMolFile) molcount++;
						if (fd.HaveName2DMolFile) molcount++;
						if (fd.HaveOther) molcount++;
					
						if (!fd.Smiles.equals("missing")) molcount++;
						
//						if (molcount==0)
							System.out.println(CAS+"\t"+fd.StructureSource+"\t"+molcount);
						
//						if (CAS.equals("10028-15-6")) {
//							System.out.println(fd.HaveNIST2DMolFile);
//						}
						
						
						if (bestMolecule instanceof IAtomContainer) {
//							fd.StructureSource=(String)bestMolecule.getProperty("Source");
							
							
							
							 AtomContainerSet AtomContainerSet2 = (AtomContainerSet)ConnectivityChecker
								.partitionIntoMolecules(bestMolecule);
							 
							 fd.CalculatedFormula="";
							 for (int mol=0;mol<AtomContainerSet2.getAtomContainerCount();mol++) {
								 IAtomContainer ac=AtomContainerSet2.getAtomContainer(mol);
								 fd.CalculatedFormula+=CDKUtilities.calculateFormula(ac);
								
								if (mol<AtomContainerSet2.getAtomContainerCount()-1) {
									fd.CalculatedFormula+=".";
								}
							 }
							 
							 fd.CalculatedMolecularWeight=CDKUtilities.calculateMolecularWeight(bestMolecule);
							 
							
							
							this.CheckForBadElement(bestMolecule,fd);
							
						} else {
							fd.StructureSource="N/A";
							fd.BadElement="N/A";
							fd.CalculatedFormula="N/A";
							fd.CalculatedMolecularWeight=-9999;
						}
						
						if (UseDefaultCorrectValues) {
						//if have a chemical in a database, make default correct value true:
							if (fd.HaveNIH3DMolFile) fd.NIH3DCorrect="true";
							if (fd.HaveDougDB3DMolFile) fd.DougDB3DCorrect="true";
							if (fd.HaveNIST2DMolFile) fd.NIST2DCorrect="true";
							if (fd.HaveNIST3DMolFile) fd.NIST3DCorrect="true";
							if (fd.HaveIndexNet2DMolFile) fd.IndexNet2DCorrect="true";
							if (fd.HaveChemidplus3DMolFile) fd.Chemidplus3DCorrect="true";	
							if (fd.HaveDSSTox2DMolFile) fd.DSSTox2DCorrect="true";
							if (fd.HaveName2DMolFile) fd.Name2DCorrect="true";
						
							if (!fd.Smiles.equals("missing")) {
								fd.HaveSmiles=true;
								fd.SmilesCorrect="true";
							}
						}
						
						
						fw.write(fd.toStringStructureData2("|"));
						fw.flush();
						
	
						counter++;
						
						if (counter % 100 == 0) System.out.println(counter);
	
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
	
					
				}// end while true;
			
	//			System.out.println(counter);
				
				br.close();
				fw.close();
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}

	/**
		 * Compares for structures that were generated from SMILES using CDK and Marvin View
		 * compares good chemicals in LD50 database
		 */
		public void CompareStructuresForSmiles2() {
			
			DescriptorFactory df=new DescriptorFactory(false);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
			chemicalcompare cc=new chemicalcompare();
			AllRingsFinder arf = new AllRingsFinder();
			arf.setTimeout(60000);
	
	
			try {
	
	//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - CAS revised list.txt";
	//			String folder="ToxPredictor/chemidplus/compare/GoodData-Revised-"+comparemethod;
				
	//			String CASfile="ToxPredictor/chemidplus/compare/Good Data - extra CAS list.txt";
				String folder="ToxPredictor/chemidplus/compare/Compare CAS generated structures for good chemicals";
				
				String structuredatafilepath="ToxPredictor/chemidplus/parse/StructureData.txt";
				
	//			BufferedReader br = new BufferedReader(new FileReader(CASfile));
				String structureFolder="ToxPredictor/chemidplus/structures";
				File structureFolder1=new File(structureFolder+"/Smiles");
				File structureFolder2=new File(structureFolder+"/Smiles_Marvin_withH");			
				
				File ffolder=new File(folder);
				if (!ffolder.exists()) ffolder.mkdir();
				
				File ImageFolder=new File(folder+"/images");
				if (!ImageFolder.exists()) ImageFolder.mkdir();
				
				
				
				FileWriter fw=new FileWriter(folder+"/"+"compareGoodData.html");
				FileWriter fw2=new FileWriter(folder+"/"+"formulacompareGoodData.html");
				FileWriter fw3=new FileWriter(folder+"/"+"formulacompareGoodData.txt");
				
				fw.write("<html><head><title>\r\n");
				fw.write("Compare structures - Good data"+"\r\n");
				fw.write("</title></head>\r\n");
				
				fw2.write("<html><head><title>\r\n");
				fw2.write("Compare formulas- Good data"+"\r\n");
				fw2.write("</title></head>\r\n");
	
				fw2.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
				fw2.write("<tr bgcolor=\"#D3D3D3\">\n");
				fw2.write("\t<th>CAS</th>\n");
				fw2.write("\t<th>Chemidplus Formula</th>\n");
				for (int i=1;i<=9;i++) {
					fw2.write("\t<th colspan=\"2\">Isomorph "+i+"</th>\n");		
				}
				fw2.write("</tr>\n");
				
				fw3.write("CAS\tChemidplus Formula\t");
				for (int i=1;i<=9;i++) {
					fw3.write("Isomorph "+i+"\t\t");		
				}
				fw3.write("\n");
				fw3.flush();
				
				int counter = 0;
				int count1=0;
	
				BufferedReader br=new BufferedReader(new FileReader("ToxPredictor/chemidplus/compare/Good Data - CAS revised list.txt"));
				
				
				while (true) {
					
	//				System.out.println(files1[i].getName());
					if (counter%100==0) System.out.println(counter);
					
					
					String CAS=br.readLine();
					
					if (CAS==null) break;
										
					counter++;
					
					
					FileData fd=new FileData();
					fd.CAS=CAS;
					
					File molfileCDK=new File(structureFolder1.getAbsolutePath()+"/"+CAS+"_Smiles.mol");
					File molfileMarvin=new File(structureFolder2.getAbsolutePath()+"/"+CAS+"_Smiles_Marvin.mol");
	
					if (!molfileMarvin.exists() || !molfileCDK.exists()) {
//						System.out.println(CAS+"-missing");
						continue;
					}
					
					IAtomContainer moleculeCDK=this.LoadChemicalFromMolFile(CAS+"_Smiles",structureFolder1.getAbsolutePath() );
					IAtomContainer moleculeMarvin=this.LoadChemicalFromMolFile(CAS+"_Smiles_Marvin", structureFolder2.getAbsolutePath());
					
					moleculeCDK.setProperty("Source","CDK");
					moleculeMarvin.setProperty("Source","Marvin");
					
					AtomContainerSet AtomContainerSet=new AtomContainerSet();				
					AtomContainerSet.addAtomContainer(moleculeCDK);
					AtomContainerSet.addAtomContainer(moleculeMarvin);
	
					ArrayList MasterList=this.CompareChemicalsInList(AtomContainerSet, "Fingerprinter");
					
					
					if (MasterList.size()>1 || AtomContainerSet.getAtomContainerCount()==1) {				
						count1++;
						this.WriteHTMLForDifference2(folder,MasterList,fw,fd,count1,structuredatafilepath);
						this.WriteHTMLForDifference3(folder,MasterList,fw2,fd,count1,structuredatafilepath);
						this.WriteFormulaTableEntry(MasterList,fw3,fd,count1);
					}
					
					
					
				}// end while true;
			
	//			System.out.println(counter);
				
				fw.write("</html>\r\n");
				fw2.write("</table></html>\r\n");			
				fw3.close();
				br.close();
				
				fw.close();
				fw2.close();
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	
	
	
		
}

class FileData {

	List <String>SystematicName = new ArrayList<>();
	List <String>Synonyms = new ArrayList<>();
	List<CarcinogenicityRecord> ToxicityRecords=new ArrayList<>();
	List<String> Formulas=new ArrayList<>();
	
	
	public String FileName="";
	public String Name = "";
	
	public String CAS = "";
	public String MW = "";
	public String Smiles="";
	
	public boolean Validated=false;
	public String SourceCorrectStructure="";
	
	public String Formula = ""; 
	public String FormulaFromChemidplusWebPage=""; //text formula in chemidplus data base
	
	public String SystematicNameLongest="";
	
	public String CalculatedFormula = ""; // calculated formula from mol file or smiles
	public double CalculatedMolecularWeight;
	//	public String FormulaCheck = "";
//	public String DSSTOX_TestSubstance_Description="";
	public String Description="";
	
//	public String FormulaCheckSource="";
	public String StructureSource="";
	
	
	public String BadElement="false";
		
	public boolean HaveNIH3DMolFile=false;
	public boolean HaveDougDB3DMolFile=false;
	public boolean HaveNIST2DMolFile=false;
	public boolean HaveNIST3DMolFile=false;
	public boolean HaveIndexNet2DMolFile=false;
	public boolean HaveChemidplus3DMolFile=false;
	public boolean HaveChemidplus2DMolFile=false;
	public boolean HaveDSSTox2DMolFile=false;
	public boolean HaveName2DMolFile=false;
	public boolean HaveSmiles=false;
	public boolean HaveOther=false;
	
	public String NIH3DCorrect="";
	public String NIST2DCorrect="";
	public String NIST3DCorrect="";
	public String DougDB3DCorrect="";
	public String IndexNet2DCorrect="";
	public String Chemidplus3DCorrect="";
	public String DSSTox2DCorrect="";
	public String Name2DCorrect="";
	public String SmilesCorrect="";
	
	boolean FileComplete = false;
	
	private static String[] ColNamesToxData = { "CAS", "LD50Op","LD50", "LD50Units","LD50Duration",
			"Effect", "Reference", "ReferenceURL", "MultipleRecords"};
	
	private static String[] ColNamesSynonymData = { "CAS", "Synonym" };		
	private static String[] ColNamesFormulaData = { "CAS", "Formula" };
	
	private static String[] ColNamesSystematicNameData = { "CAS", "SystematicName" };		

	private static String[] ColNamesStructureData = { "CAS", "Name","SystematicNameLongest", "MW", "FormulaFromChemidplusWebPage","BadElement"};
	
//	private static String[] ColNamesStructureData2 = { "CAS", "StructureSource","CalculatedFormula","CalculatedMolecularWeight","BadElement", "HaveNIH3DMolFile",
//			"HaveNIST2DMolFile", "HaveNIST3DMolFile", "HaveDougDB3DMolFile",
//			"HaveIndexNet2DMolFile","HaveChemidplus3DMolFile","HaveDSSTox2DMolFile","HaveName2DMolFile","Smiles"};

//	private static String[] ColNamesStructureData2 = { "CAS", "StructureSource","CalculatedFormula","CalculatedMolecularWeight","BadElement","Validated", 
//		"HaveNIH3DMolFile","NIH3DCorrect",
//		"HaveNIST2DMolFile", "NIST2DCorrect",
//		"HaveNIST3DMolFile","NIST3DCorrect", 
//		"HaveDougDB3DMolFile","DougDB3DCorrect",
//		"HaveIndexNet2DMolFile","IndexNet2DCorrect",
//		"HaveChemidplus3DMolFile","Chemidplus3DCorrect",
//		"HaveDSSTox2DMolFile","DSSTox2DCorrect",
//		"HaveName2DMolFile","Name2DCorrect",
//		"Smiles","SmilesCorrect"
//		};
	
	private static String[] ColNamesStructureData2 = { "CAS",
	"StructureSource","CalculatedFormula","CalculatedMolecularWeight","BadElement",
	"Validated","SourceCorrectStructure",
	"HaveNIH3DMolFile","NIH3DCorrect",
	"HaveNIST2DMolFile","NIST2DCorrect",
	"HaveNIST3DMolFile","NIST3DCorrect",
	"HaveDougDB3DMolFile","DougDB3DCorrect",
	"HaveIndexNet2DMolFile","IndexNet2DCorrect",
	"HaveChemidplus3DMolFile","Chemidplus3DCorrect",
	"HaveDSSTox2DMolFile","DSSTox2DCorrect",
	"HaveName2DMolFile","Name2DCorrect",
	"Smiles","SmilesCorrect"};

	
	
	public static String getHeaderStructureData(String Delimiter) {		
		return ConvertArrayToString(Delimiter,ColNamesStructureData);
	}
	
	public static String getHeaderStructureData2(String Delimiter) {			
		return ConvertArrayToString(Delimiter,ColNamesStructureData2);
	}
	public static String getHeaderSystematicNameData(String Delimiter) {				
		return ConvertArrayToString(Delimiter,ColNamesSystematicNameData);
	}

	public static String getHeaderSynonymData(String Delimiter) {			
		return ConvertArrayToString(Delimiter,ColNamesSynonymData);
	}

	public static String getHeaderFormulaData(String Delimiter) {			
		return ConvertArrayToString(Delimiter,ColNamesFormulaData);
	}

	
	public static String getHeaderToxData(String Delimiter) {
		return ConvertArrayToString(Delimiter,ColNamesToxData);
	}

	public static String ConvertArrayToString (String Delimiter,String [] ColNames) {
		
		String Header="";
		for (int i=0;i<ColNames.length;i++) {
			Header+=ColNames[i];
			if (i<ColNames.length-1) Header+=Delimiter;							
		}
		Header+="\r\n";
		return Header;
		
	}
	
	
	public String toStringToxData(String Delimiter) {
		
		String Line="";
				
		for (int ii=0;ii<ToxicityRecords.size();ii++) {
		
		CarcinogenicityRecord tr=(CarcinogenicityRecord)ToxicityRecords.get(ii);
		
		Line+=CAS + Delimiter + tr.LD50Op+Delimiter+tr.LD50 + Delimiter	+ tr.LD50Units + Delimiter+tr.Duration+Delimiter + tr.Effect + Delimiter
				+ tr.Reference + Delimiter + tr.ReferenceURL+Delimiter;
		
		if (ToxicityRecords.size()>1) Line+="true";							
		else if (ToxicityRecords.size()==1) Line+="false";
		else Line+="error!";
		
		Line+="\r\n";
				
	}

		
		return Line;
			
	}
	
	public String toStringSystematicNameData(String Delimiter) {

		String Line = "";

		for (int ii = 0; ii < this.SystematicName.size(); ii++) {
			Line += CAS + Delimiter+SystematicName.get(ii);
			Line += "\r\n";
		}

		return Line;

	}
	
	public String toStringSynonymData(String Delimiter) {

		String Line = "";

		for (int ii = 0; ii < this.Synonyms.size(); ii++) {
			Line += CAS + Delimiter+Synonyms.get(ii);
			Line += "\r\n";
		}

		return Line;

	}
	
	public String toStringFormulaData(String Delimiter) {

		String Line = "";

		for (int ii = 0; ii < this.Formulas.size(); ii++) {
			Line += CAS + Delimiter+Formulas.get(ii);
			Line += "\r\n";
		}

		return Line;

	}
	
	public String toString(String Delimiter,String [] ColNames) {
		String Line="";
		
		Field myField=null;
		
		DecimalFormat myDF=new DecimalFormat("0.00");
		for (int i=0;i<ColNames.length;i++) {
			try {
				
				myField=this.getClass().getField(ColNames[i]);
				
				String val=null;
				
				String type=myField.getType().getName().toLowerCase();
					
				
				if (type.equals("boolean")) {
					boolean bval=myField.getBoolean(this);					
					val=bval+"";
				} else if (type.equals("double")) {
					val=myDF.format(myField.getDouble(this));	
				} else {
					val=(String)myField.get(this);
				}
				
				
				Line+=val;
				
				if (i<ColNames.length-1) {
					Line+=Delimiter;
				}
				
			} catch (Exception e) {
				System.out.println(myField.getType());
				e.printStackTrace();
			}
		}
		
		Line+="\r\n";
		
		return Line;
	}
	
	
	public String toStringStructureData(String Delimiter) {		
		return this.toString(Delimiter,this.ColNamesStructureData);
	}

	
	public String toStringStructureData2(String Delimiter) {		
		return this.toString(Delimiter,this.ColNamesStructureData2);
	}
} // end FileData class

class CarcinogenicityRecord {
	String LD50 = "";

	String LD50Units = "";
	
	String LD50Op="";

	String Effect = "";

	String Reference = "";

	String ReferenceURL = "";
	
	String Duration="";
		
	
} // end Toxicity Record Class
