package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.openscience.cdk.AtomContainer;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.TESTPredictedValue;

public class WriteBatchResults {

	String del = "|";// delimiter for text file
	
	//TODO- make following not global:
	FileWriter fwBatchHTML = null;	
	FileWriter fwBatchTXT = null;
	FileWriter fwBatchConsensusTXT = null;

	File txtFile=null;
	File txtFileAll=null;

	
	void setUpFileWriters(File fileOutputFolder,ArrayList<String> methods,File htmlFile,int runNumber,String endpoint,String method) {

		String textFileNameMethod;
		
		String textFileNameAll;
		
		DescriptorFactory df=new DescriptorFactory(false);

		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			textFileNameMethod = "batch_Descriptors" + "_" + runNumber + ".txt";
		} else {
			textFileNameMethod = "Batch_" + endpoint.replace(" ", "_") + "_" + method + "_" + runNumber + ".txt";
		}

		if (method.equals(TESTConstants.ChoiceConsensus)) {
			textFileNameAll = "Batch_" + endpoint.replace(" ", "_") + "_all_methods_" + runNumber + ".txt";
		} else {
			textFileNameAll = "N/A";
		}

		
		txtFile = new File(fileOutputFolder.getAbsolutePath() +File.separator+"ToxRuns"+ File.separator + textFileNameMethod);
		txtFileAll = new File(fileOutputFolder.getAbsolutePath()+File.separator+"ToxRuns" + File.separator + textFileNameAll);

		
		
		try {
								
			if (WebTEST4.generateWebpages) {
				fwBatchHTML = new FileWriter(htmlFile);
				WriteOverallHtmlHeader(fwBatchHTML, textFileNameMethod, textFileNameAll,endpoint,method);
			}
					
			fwBatchTXT = new FileWriter(txtFile);
						

			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				df.WriteCSVHeader(fwBatchTXT, del);
			} else {
				WriteOverallTextHeader(fwBatchTXT, del,endpoint,method);

				if (method.equals(TESTConstants.ChoiceConsensus)) {
					fwBatchConsensusTXT = new FileWriter(txtFileAll);
					WriteOverallTextHeaderAllMethods(fwBatchConsensusTXT, del, methods,endpoint);
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	private void writeHTMLResults(AtomContainer ac, FileWriter fwBatchHTML, int index, String CAS,
			TESTPredictedValue tpv, String error, String endpoint, String method, String f2, String imagePath,
			String filename) throws IOException {
		fwBatchHTML.write("<tr>\n");
		fwBatchHTML.write("<td>" + index + "</td>\n");// #
		
		if (WebTEST4.createReports)
			write_TD_CAS(ac, fwBatchHTML, CAS, error, f2, imagePath, filename);
		else
			write_TD_CAS(ac, fwBatchHTML, CAS, error, f2, imagePath);
		
		if (tpv.predictionResults!=null) {
			PredictionResultsPrimaryTable p=tpv.predictionResults.getPredictionResultsPrimaryTable();
		
			if (TESTConstants.isLogMolar(endpoint)) {
				fwBatchHTML.write("<td>" + p.getExpToxValue() + "</td>\n");
				fwBatchHTML.write("<td>" + p.getPredToxValue() + "</td>\n");
			}
		
			fwBatchHTML.write("<td>" + p.getExpToxValMass() + "</td>\n");
			fwBatchHTML.write("<td>" + p.getPredToxValMass() + "</td>\n");
			
			if (method.equals(TESTConstants.ChoiceLDA)) {
				if (p.getExpMOA().equals("")) {
					fwBatchHTML.write("<td align=\"center\">N/A</td>\n");
				} else {
					fwBatchHTML.write("<td align=\"center\">" + p.getExpMOA() + "</td>\n");
				}

				if (p.getPredMOA().equals("") || Double.parseDouble(p.getMaxScoreMOA()) < 0.5) {
					fwBatchHTML.write("<td align=\"center\">N/A</td>\n");
				} else {
					fwBatchHTML.write("<td align=\"center\">" + p.getPredMOA() + "</td>\n");
				}

			}
		
		} else {
			if (TESTConstants.isLogMolar(endpoint)) {
				if (tpv.expValLogMolar.isNaN()) {
					fwBatchHTML.write("<td>N/A</td>\n");
				} else {
					fwBatchHTML.write("<td>" + tpv.expValLogMolar + "</td>\n");
				}
				fwBatchHTML.write("<td>N/A</td>\n");
			}
		
			
			fwBatchHTML.write("<td>"+tpv.expValMass+"</td>\n");
			fwBatchHTML.write("<td>N/A</td>\n");
			
			if (method.equals(TESTConstants.ChoiceLDA)) {
				fwBatchHTML.write("<td align=\"center\">N/A</td>\n");
				fwBatchHTML.write("<td align=\"center\">N/A</td>\n");
			}
			
		}


		fwBatchHTML.write("</tr>\n");
		fwBatchHTML.flush();
	}
	
	private void write_TD_CAS(AtomContainer ac, FileWriter fwBatchHTML, String CAS, String error, String f2,
			String imagePath) throws IOException {
		
		if (error.equals("")) {
			if (ac.getProperty("Parent") != null) {
				fwBatchHTML.write("<td>" + CAS + "<br><br>"
						+ "Breakdown product of " + ac.getProperty("Parent") + "<br>"+
						"Accumulation = "+ac.getProperty("Accumulation")+ "</td>\n");
			} else {
				fwBatchHTML.write("<td>" + CAS + "</td>\n");
			}
			
//				fwBatchHTML.write("<td><a href=\"" + f3 + "/structure.png" + "\"><img src=\"" + f3 + "/" + "structure.png" + "\" height=150 border=0></a></td>\n");
			fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+getStructureImageHtmlDetails(imagePath)+"></a></td>\n");
		} else {
			if (ac.getProperty("Parent")!=null) {
				fwBatchHTML.write("<td>" + CAS + "<br>"+"(Breakdown product of "+ac.getProperty("Parent")+")"+"</td>\n");					
			} else {
				fwBatchHTML.write("<td>" + CAS + "</td>\n");	
			}

			if (ac.getProperty("gsid") == null || !WebTEST4.dashboardStructuresAvailable) {
				fwBatchHTML.write("<td>Error: " + error + "</td>\n");					
			} else {
				fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+getStructureImageHtmlDetails(imagePath)+"></a>"+"<br>Error: " + error + "</td>\n");
			}

		}
	}

	
	public static String getStructureImageHtmlDetails(String imagePath) {
//		return "border=0 width=200";
//		System.out.println(imagePath);
		
		if (imagePath.contains("comptox.epa.gov/dashboard")) return "border=0 height=150";
		else return "border=0";
	}
	
	public static void WriteOverallHtmlHeader(FileWriter fw, String endpoint,String method) throws Exception {

		fw.write("<!DOCTYPE HTML><html><head><title>\r\n");

		// System.out.println(endpoint.equals(TESTConstants.ChoiceDescriptors));

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors))
			fw.write("Batch predictions for " + method + " method for " + endpoint + "\r\n");
		else {
			fw.write("Descriptors\r\n");
		}
		fw.write("</title></head><body>\r\n");

		
		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			fw.write("<h2>Batch predictions for " + method + " method for " + endpoint + "</h2>\r\n");
		} else {
			fw.write("<h2>Descriptors</h2>\r\n");
		}

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>#</th>\n");
		fw.write("<th>ID</th>\n");
		fw.write("<th>Structure</th>\n");

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			String massunits = TESTConstants.getMassUnits(endpoint);
			String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

			if (!TESTConstants.isBinary(endpoint)) {
				if (TESTConstants.isLogMolar(endpoint)) {
					fw.write("<th>Experimental Value<br>" + molarlogunits + "</th>\n");
					fw.write("<th>Predicted Value<br>" + molarlogunits + "</th>\n");
				}
				fw.write("<th>Experimental Value<br>" + massunits + "</th>\n");
				fw.write("<th>Predicted Value<br>" + massunits + "</th>\n");

				if (method.equals(TESTConstants.ChoiceLDA)) {
					fw.write("<th>Experimental MOA<br></th>\n");
					fw.write("<th>Predicted MOA<br></th>\n");
				}

			} else {
				fw.write("<th>Experimental Value</th>\n");
				fw.write("<th>Predicted Value</th>\n");
				fw.write("<th>Experimental Result</th>\n");
				fw.write("<th>Predicted Result</th>\n");

			}

		} else {
			// fw.write("<th>Error</th>\n");
		}

		fw.write("</tr>\n");

	}
	
	
	private void WriteOverallHtmlHeader(FileWriter fw, String textFileNameMethod, String textFileNameAll,String endpoint,String method) throws Exception {

		fw.write("<!DOCTYPE HTML><html><head><title>\r\n");

		// System.out.println(endpoint.equals(TESTConstants.ChoiceDescriptors));

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors))
			fw.write("Batch predictions for " + method + " method for " + endpoint + "\r\n");
		else {
			fw.write("Descriptors\r\n");
		}
		fw.write("</title></head><body>\r\n");

		String strDel = "\"" + del + "\"";
		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			fw.write("<h2>Batch predictions for " + method + " method for " + endpoint + "</h2>\r\n");
			fw.write("<h3><a href=\"" + textFileNameMethod + "\">" + "Text file (" + method + " method)" + "</a></h3>\r\n");

			if (method.equals(TESTConstants.ChoiceConsensus)) {
				fw.write("<h3><a href=\"" + textFileNameAll + "\">" + "Text file (All methods)" + "</a></h3>\r\n");
			}
		} else {
			fw.write("<h2>Descriptors (");

			// System.out.println("strDel="+strDel);

			if (del.equals("\t")) {
				fw.write("<a href=\"" + textFileNameMethod + "\">tab delimited descriptor file</a>)</h2>\r\n");
			} else {
				fw.write("<a href=\"" + textFileNameMethod + "\">" + strDel + " delimited descriptor file</a>)</h2>\r\n");
			}

		}

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>#</th>\n");
		fw.write("<th>ID</th>\n");
		fw.write("<th>Structure</th>\n");

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			String massunits = TESTConstants.getMassUnits(endpoint);
			String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

			if (!TESTConstants.isBinary(endpoint)) {
				if (TESTConstants.isLogMolar(endpoint)) {
					fw.write("<th>Experimental Value<br>" + molarlogunits + "</th>\n");
					fw.write("<th>Predicted Value<br>" + molarlogunits + "</th>\n");
				}
				fw.write("<th>Experimental Value<br>" + massunits + "</th>\n");
				fw.write("<th>Predicted Value<br>" + massunits + "</th>\n");

				if (method.equals(TESTConstants.ChoiceLDA)) {
					fw.write("<th>Experimental MOA<br></th>\n");
					fw.write("<th>Predicted MOA<br></th>\n");
				}

			} else {
				fw.write("<th>Experimental Value</th>\n");
				fw.write("<th>Predicted Value</th>\n");
				fw.write("<th>Experimental Result</th>\n");
				fw.write("<th>Predicted Result</th>\n");

			}

		} else {
			// fw.write("<th>Error</th>\n");
		}

		// fw.write("<th>Experimental Value</th>\n");
		// fw.write("<th>Predicted Value</th>\n");
		fw.write("</tr>\n");

	}
	private void write_TD_CAS(AtomContainer ac, FileWriter fwBatchHTML, String CAS, String error, String f2,
				String imagePath, String filename) throws IOException {
			
			if (error.equals("")) {
				if (ac.getProperty("Parent") != null) {
					fwBatchHTML.write("<td><a href=\"" + f2 + "/" + filename + "\">" + CAS + "</a>" + "<br><br>"
							+ "Breakdown product of " + ac.getProperty("Parent") + "<br>"+
							"Accumulation = "+ac.getProperty("Accumulation")+ "</td>\n");
				} else {
					fwBatchHTML.write("<td><a href=\"" + f2 + "/" + filename + "\">" + CAS + "</a></td>\n");
				}
				
	//				fwBatchHTML.write("<td><a href=\"" + f3 + "/structure.png" + "\"><img src=\"" + f3 + "/" + "structure.png" + "\" height=150 border=0></a></td>\n");
	//			fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" border=0 width=200></a></td>\n");
				fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+getStructureImageHtmlDetails(imagePath)+"></a></td>\n");
			} else {
				if (ac.getProperty("Parent")!=null) {
					fwBatchHTML.write("<td>" + CAS + "<br>"+"(Breakdown product of "+ac.getProperty("Parent")+")"+"</td>\n");					
				} else {
					fwBatchHTML.write("<td>" + CAS + "</td>\n");	
				}
	
				if (ac.getProperty("gsid") == null || !WebTEST4.dashboardStructuresAvailable) {
					fwBatchHTML.write("<td>Error: " + error + "</td>\n");					
				} else {
					fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+getStructureImageHtmlDetails(imagePath)+"></a>"+"<br>Error: " + error + "</td>\n");
	//				fwBatchHTML.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" width=200 border=0></a>"+"<br>Error: " + error + "</td>\n");
	
				}
	
			}
		}

	private void writeCenteredTD(FileWriter fw,String value) throws Exception {
		fw.write("<td align=\"center\">"+value+"</td>\n");
	}

	private void WriteDescriptorResultsForChemicalToWebPage(FileWriter fw, int index, String CAS, String error) {
		String filename = "DescriptorData_"+CAS+".html";
	
		try {
			fw.write("<tr>\n");
	
			fw.write("<td>" + index + "</td>\n");// #
	
			if (error.equals("")) {
				String folder = "ToxRun_" + CAS + "/StructureData";
	
				fw.write("<td><a href=\"" + folder + "/" + filename + "\">" + CAS + "</a></td>\n"); // ID
																									// col
	
				fw.write("<td><a href=\"" + folder + "/structure.png" + "\"><img src=\"" + folder + "/structure.png" + "\" "+getStructureImageHtmlDetails(folder + "/structure.png")+"></a></td>\n");// structure
																																								// col
	
				// fw.write("<td><br></td>\n");//error col
			} else {
				fw.write("<td>" + CAS + "</td>\n"); // ID col
				fw.write("<td>" + "Error: " + error + "</td>\n");
				// fw.write("<td><br></td>\n");//error col
			}
	
			fw.write("</tr>\n");
	
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	//	private void writeTextResultsBinary(AtomContainer ac, int index, String CAS, String query,
	//			TESTPredictedValue tpv, String d, String endpoint, String method) {
	//
	//		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
	//		
	//
	//		try {	
	//
	//			
	//			String smilesRan=ac.getProperty("SmilesRan");
	//			
	//			fwBatchTXT.write(index + d + CAS + d+query+d+smilesRan+d);
	//			
	//
	//			if (tpv.expValLogMolar.isNaN()) {
	//				fwBatchTXT.write("N/A" + d);
	//			} else {
	//				fwBatchTXT.write(d2.format(tpv.expValLogMolar) + d);
	//			}
	//
	//			if (tpv.predValLogMolar.isNaN()) {
	//				fwBatchTXT.write("N/A" + d);
	//			} else {
	//				fwBatchTXT.write(d2.format(tpv.predValLogMolar) + d);
	//			}
	//
	//			// System.out.println(ExpToxVal+"\t"+PredToxVal);
	//
	//			if (tpv.expValLogMolar.isNaN()) {
	//				fwBatchTXT.write("N/A" + d);
	//			} else {
	//				if (tpv.expValLogMolar < 0.5) {
	//
	//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
	//						fwBatchTXT.write("Developmental NON-toxicant" + d); // mass
	//
	//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
	//						fwBatchTXT.write("Mutagenicity Negative" + d); // mass
	//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
	//						fwBatchTXT.write("Does NOT bind to estrogen receptor" + d); // mass
	//					}
	//				} else {
	//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
	//						fwBatchTXT.write("Developmental toxicant" + d); // mass
	//
	//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
	//						fwBatchTXT.write("Mutagenicity Positive" + d); // mass
	//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
	//						fwBatchTXT.write("Binds to estrogen receptor" + d); // mass
	//					}
	//
	//				}
	//			}
	//
	//			if (tpv.predValLogMolar.isNaN()) {
	//				fwBatchTXT.write("N/A"+d);
	//			} else {
	//
	//				if (tpv.predValLogMolar < 0.5) {
	//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
	//						fwBatchTXT.write("Developmental NON-toxicant"+d); // mass
	//
	//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
	//						fwBatchTXT.write("Mutagenicity Negative"+d); // mass
	//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
	//						fwBatchTXT.write("Does NOT bind to estrogen receptor" + d); // mass
	//					}
	//				} else {
	//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
	//						fwBatchTXT.write("Developmental toxicant"+d); // mass
	//
	//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
	//						fwBatchTXT.write("Mutagenicity Positive"+d); // mass
	//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
	//						fwBatchTXT.write("Binds to estrogen receptor" + d); // mass
	//					}
	//				}
	//			}
	//
	//			fwBatchTXT.write(ac.getProperty("Error")+"");
	//			fwBatchTXT.write("\r\n");
	//			fwBatchTXT.flush();
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//
	//	}
	
		private void writeHTMLResultsBinary(AtomContainer ac, FileWriter fwBatchHTML2, int index, String CAS,
				TESTPredictedValue tpv, String error, String endpoint, String method, String f2, String imagePath,
				String filename) {
	
	
			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
	
			try {
				fwBatchHTML.write("<tr>\n");
				fwBatchHTML.write("<td>" + index + "</td>\n");// #
	
				if (WebTEST4.createReports)
					write_TD_CAS(ac, fwBatchHTML, CAS, error, f2, imagePath, filename);
				else
					write_TD_CAS(ac, fwBatchHTML, CAS, error, f2, imagePath);
	
	
				if (tpv.expValLogMolar.isNaN()) {
					writeCenteredTD(fwBatchHTML, "N/A");
				} else {
					writeCenteredTD(fwBatchHTML, d2.format(tpv.expValLogMolar));
				}
	
				if (tpv.predValLogMolar.isNaN()) {
					writeCenteredTD(fwBatchHTML, "N/A");
				} else {
					writeCenteredTD(fwBatchHTML, d2.format(tpv.predValLogMolar));
				}
	
				// System.out.println(ExpToxVal+"\t"+PredToxVal);
	
				if (tpv.expValLogMolar.isNaN()) {
					fwBatchHTML.write("<td align=\"center\">N/A</td>\n");// mass
				} else {
					if (tpv.expValLogMolar < 0.5) {
	
						if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
							fwBatchHTML.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); // mass
	
						} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
							fwBatchHTML.write("<td align=\"center\">Mutagenicity Negative</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
							fwBatchHTML.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass
						}
					} else {
						if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
							fwBatchHTML.write("<td align=\"center\">Developmental toxicant</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
							fwBatchHTML.write("<td align=\"center\">Mutagenicity Positive</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
							fwBatchHTML.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); // mass
						}
	
					}
				}
	
				if (tpv.predValLogMolar.isNaN()) {
					fwBatchHTML.write("<td align=\"center\">N/A</td>\n"); // mass units
				} else {
					if (tpv.predValLogMolar < 0.5) {
						if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
							fwBatchHTML.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); // mass
	
						} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
							fwBatchHTML.write("<td align=\"center\">Mutagenicity Negative</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
							fwBatchHTML.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass
						}
					} else {
						if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
							fwBatchHTML.write("<td align=\"center\">Developmental toxicant</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
							fwBatchHTML.write("<td align=\"center\">Mutagenicity Positive</td>\n"); // mass
						} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
							fwBatchHTML.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); // mass
						}
					}
				}
	
				fwBatchHTML.write("</tr>\n");
				fwBatchHTML.flush();
	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	
		}

	private void WriteToxicityResultsForChemical(AtomContainer ac,FileWriter fwBatchHTML, FileWriter fwBatchTXT, 
				int index, String CAS, String query,TESTPredictedValue tpv, String del, String error,String endpoint,String method,boolean createExcelReport) {
			
			
			try {
	
	
				String f = "ToxRun_" + CAS;
				String f2 = f + "/" + endpoint;
				String f3 = f + "/StructureData";
	
				String imagePath="";
				
				if (ac.getProperty("gsid") == null || !WebTEST4.dashboardStructuresAvailable) {
					imagePath=f3 + "/structure.png";
				} else {
					imagePath=PredictToxicityWebPageCreator.webImagePathByCID + ac.getProperty("gsid");//TODO fix to use CID
				}
				
				
				String fileNameNoExtension=WebTEST4.getResultFileNameNoExtension(endpoint, method, CAS);
				String filename=fileNameNoExtension+".html";
	
	
				if(!createExcelReport) {			
					writeHTMLResults(ac, fwBatchHTML, index, CAS, tpv, error, endpoint, method, f2, imagePath, filename);
				}
				writeTextResults(ac, index, CAS, query, tpv, del, endpoint, method);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	
	private void writeTextResults(AtomContainer ac, int index, String CAS, String query,
			TESTPredictedValue tpv, String del, String endpoint, String method) throws IOException {


		String smilesRan=ac.getProperty("SmilesRan");

		fwBatchTXT.write(index + del + CAS + del+query+del+smilesRan+del);


		// ********************************************************
		// Molar values


		if (tpv.predictionResults!=null) {
			PredictionResultsPrimaryTable p=tpv.predictionResults.getPredictionResultsPrimaryTable();

			if (TESTConstants.isLogMolar(endpoint)) {
				fwBatchTXT.write(p.getExpToxValue() + del);
				fwBatchTXT.write(p.getPredToxValue() + del);
			}

			fwBatchTXT.write(p.getExpToxValMass() + del);
			fwBatchTXT.write(p.getPredToxValMass() + del);

			if (method.equals(TESTConstants.ChoiceLDA)) {

				if (p.getExpMOA().equals("")) {
					fwBatchTXT.write("N/A" + del);
				} else {
					fwBatchTXT.write(p.getExpMOA() + del);
				}

				if (p.getPredMOA().equals("") || Double.parseDouble(p.getMaxScoreMOA()) < 0.5) {
					fwBatchTXT.write("N/A"+del);
				} else {
					fwBatchTXT.write(p.getPredMOA()+del);
				}

			}

		} else {
			if (TESTConstants.isLogMolar(endpoint)) {
				if (tpv.expValLogMolar.isNaN()) {
					fwBatchTXT.write("N/A"+ del);
				} else {
					fwBatchTXT.write(tpv.expValLogMolar + del);
				}
			}

			
			fwBatchTXT.write(tpv.expValMass+ del);
			fwBatchTXT.write("N/A"+ del);

			if (method.equals(TESTConstants.ChoiceLDA)) {
				fwBatchTXT.write("N/A" + del);
				fwBatchTXT.write("N/A"+del);
			}

		}


		fwBatchTXT.write(ac.getProperty("Error")+"");
		fwBatchTXT.write("\r\n");
		fwBatchTXT.flush();
	}

	private void WriteToxResultsForChemicalBinary(AtomContainer ac,FileWriter fwBatchHTML, FileWriter fwBatchTEXT, int index, String CAS,String query,TESTPredictedValue tpv, String d, String error,String endpoint,String method) {
	
			try {
	
				String f = "ToxRun_" + CAS;
				String f2 = f + "/" + endpoint;
				String f3 = f + "/StructureData";
	
				String imagePath="";
				
				if (ac.getProperty("gsid") == null || !WebTEST4.dashboardStructuresAvailable) {
					imagePath=f3 + "/structure.png";
				} else {
					imagePath=PredictToxicityWebPageCreator.webImagePathByCID + ac.getProperty("gsid");//TODO fix to use CID
				}
	//			String filename = "PredictionResults";
	//			filename += method.replaceAll(" ", "");
	//			filename += ".html";
				
				String fileNameNoExtension=WebTEST4.getResultFileNameNoExtension(endpoint, method, CAS);
				String filename=fileNameNoExtension+".html";
					
//				if(!createExcelReport) {			
//					writeHTMLResultsBinary(ac, fwBatchHTML, index, CAS, tpv, error, endpoint, method, f2, imagePath, filename);
//				}
	
	//			writeTextResultsBinary(ac, index, CAS, query, tpv, del, endpoint, method);
	
	
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		}

	public static void WriteOverallTextHeader(FileWriter fw, String d,String endpoint,String method) throws Exception {
	
		fw.write("#" + d + "ID" + d+"Query"+d+"SmilesRan"+d);
	
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);
	
		if (!TESTConstants.isBinary(endpoint)) {
			if (TESTConstants.isLogMolar(endpoint)) {
				fw.write("Exp_Value:" + molarlogunits + d);
				fw.write("Pred_Value:" + molarlogunits + d);
			}
	
			fw.write("Exp_Value:" + massunits + d);
			fw.write("Pred_Value:" + massunits + d);
	
			if (method.equals(TESTConstants.ChoiceLDA)) {
				fw.write("Experimental MOA" + d);
				fw.write("Predicted MOA" + d);
			}
	
		} else {
			fw.write("Exp_Value" + d);
			fw.write("Pred_Value" + d);
			fw.write("Exp_Result" + d);
			fw.write("Pred_Result"+d);
		}
		fw.write("Error");
		fw.write("\r\n");
	
	}

	public static void WriteOverallTextHeaderAllMethods(FileWriter fw, String d, ArrayList<String> methods,String endpoint) throws Exception {
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);
	
		String units;
	
		if (!TESTConstants.isBinary(endpoint)) {
			if (TESTConstants.isLogMolar(endpoint)) {
				units = molarlogunits;
			} else {
				units = massunits;
			}
		} else {
			units = "";
		}
	
		if (!units.equals("")) {
			fw.write("Note: All values in " + units + "\r\n");
		}
	
		fw.write("#" + d + "ID" + d +"Query"+ d +"SmilesRan"+ d+"Exp" + d);
	
		for (int i = 0; i < methods.size(); i++) {
			fw.write("Pred_" + methods.get(i)+d);
		}
		fw.write("Error");		
		fw.write("\r\n");
	
	}

	public static void WriteToxicityResultsForChemicalAllMethods(FileWriter fw3, int index, String CAS, String query,List<TESTPredictedValue>listTPV, String del,String error,String endpoint,AtomContainer ac) {
		try {
	
			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
	
			TESTPredictedValue tpvConsensus=listTPV.get(0);
			
			String smilesRan=ac.getProperty("SmilesRan");
			
			fw3.write(index + del + CAS + del+query+del+smilesRan+del);
			
			if (TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
				if (tpvConsensus.expValLogMolar.isNaN()) fw3.write("N/A"+del);
				else fw3.write(d2.format(tpvConsensus.expValLogMolar)+del);
			} else {
				if (tpvConsensus.expValMass.isNaN()) fw3.write("N/A"+del);
				else fw3.write(tpvConsensus.expValMass+del);
			}
	
			for (int i=1;i<listTPV.size();i++) {
				
				TESTPredictedValue tpv=listTPV.get(i);
				
				if (!Strings.isEmpty(tpv.error)) continue;
				
				if (TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
					if (tpv.predValLogMolar.isNaN()) fw3.write("N/A"+del);
					else {
						fw3.write(d2.format(tpv.predValLogMolar)+del);
					}
				} else {
					if (tpv.predValMass.isNaN()) fw3.write("N/A"+del);
					else fw3.write(tpv.predValMass+del);
				}
			}
			
			
			if (!Strings.isEmpty(error)) {
				List<String>methods=TaskCalculations2.getMethods(endpoint);
				for (int i=0;i<methods.size();i++) {
					fw3.write("N/A");
					fw3.write(del);
				}
				
			} else {
				if (TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
					if (tpvConsensus.predValLogMolar.isNaN()) {
						fw3.write("N/A"+del);
					} else fw3.write(d2.format(tpvConsensus.predValLogMolar)+del);
				} else {
					if (tpvConsensus.predValMass.isNaN()) {
						fw3.write("N/A"+del);
					}	else fw3.write(tpvConsensus.predValMass+del);
				}
			}
			fw3.write(error);
			fw3.write("\r\n");
			fw3.flush();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
