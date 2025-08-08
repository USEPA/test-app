package AADashboard.Application;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
//import gov.epa.api.FlatFileRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;



/**
 * Demo class to create toxicity profile comparison page in html
 * 
 * @author Todd Martin
 *
 */
public class TableGeneratorHTML {

	String[] humanHealthEffectsEndpoints = { Chemical.strAcute_Mammalian_Toxicity, 
			Chemical.strCarcinogenicity,Chemical.strGenotoxicity_Mutagenicity,Chemical.strEndocrine_Disruption,
			Chemical.strReproductive,Chemical.strDevelopmental,
			Chemical.strNeurotoxicity,
			Chemical.strSystemic_Toxicity,
			Chemical.strSkin_Sensitization,Chemical.strSkin_Irritation, Chemical.strEye_Irritation};


	String [] ecotoxEndpoints={Chemical.strAcute_Aquatic_Toxicity,Chemical.strChronic_Aquatic_Toxicity};
	String [] fateEndpoints={Chemical.strPersistence,Chemical.strBioaccumulation};

	public static int width=20;


	


	void generateComparisonTable() {

		String folder="Dashboard Output";
		String filename="comparison.html";

		File Folder=new File(folder);
		if (!Folder.exists()) Folder.mkdir();

		String filepath=folder+"/"+filename;

		String[] humanHealthEffectsEndpoints = { "Acute Mammalian Toxicity", "Carcinogenicity", 
				"Genotoxicity / Mutagenicity","Endocrine Disruption","Reproductive",
				"Developmental","Neurological","Repeated Dose","Skin Sensitization",
				"Eye Irritation","Dermal Irritation" };


		String [] ecotoxEndpoints={"Acute Aquatic Toxicity","Chronic Aquatic Toxicity"};
		String [] fateEndpoints={"Persistence","Bioaccumulation"};


		try {
			FileWriter fw=new FileWriter(filepath);

			Vector<Chemical> chemicals=new Vector<Chemical>();
			chemicals.add(Chemical.createDecaBDE());
			chemicals.add(Chemical.createTPP());
			chemicals.add(Chemical.createRDP());
			chemicals.add(Chemical.createBPADP());

			fw.write("<!DOCTYPE HTML>\r\n");

			fw.write("<html>\r\n");

			fw.write("<head>\r\n");
			fw.write("\t<title>Comparison of alternatives</title>\r\n");


			int width=35;

			fw.write("<style>\r\n");
			fw.write("table { border-collapse:collapse; }\r\n");
			fw.write("td, th {border: 1px black solid; padding: 3px; }\r\n");
			fw.write("th {background-color: lightgray; }\r\n");
			//			fw.write(".example { width:"+width+"px; }\r\n");
			//			fw.write(".example { text-align: center; vertical-align: center; }\r\n");
			fw.write(".example.Text4 span, .example.Text4 { writing-mode: sideways-lr; -webkit-writing-mode: sideways-lr; -ms-writing-mode: sideways-lr; }\r\n");
			fw.write("</style>\r\n");

			fw.write("</head>\r\n\r\n");

			fw.write("<body>\r\n");

			fw.write("<h2>Comparison of alteratives</h2>\r\n");

			fw.write("<table>\r\n");


			fw.write("\t<tr>\r\n");
			fw.write("\t\t<th rowspan=\"2\" width=100px>Structure<br>CAS<br>name</th>\r\n");
			fw.write("\t\t<th colspan=\"11\" width="+width*humanHealthEffectsEndpoints.length+"px>Human Health Effects</th>\r\n");
			fw.write("\t\t<th colspan=\"2\" width="+width*ecotoxEndpoints.length+"px>Ecotoxicity</th>\r\n");
			fw.write("\t\t<th colspan=\"2\" width="+width*fateEndpoints.length+"px>Fate</th>\r\n");
			fw.write("\t</tr>\r\n");

			fw.write("\t<tr>\r\n");
			writeEndpointHeader(humanHealthEffectsEndpoints, fw, width);
			writeEndpointHeader(ecotoxEndpoints, fw, width);
			writeEndpointHeader(fateEndpoints, fw, width);
			fw.write("\t</tr>\r\n");


			for (int i=0;i<chemicals.size();i++) {
				Chemical chemical=chemicals.get(i);
				fw.write("\t<tr>\r\n");

				fw.write("\t\t<td width="+width+"px>"+chemical.CAS+"<br>"+chemical.name+"</td>\r\n");

				//				fw.write("\t\t<td width="+width+"px><img src=\""+chemical.CAS+".png\">"+chemical.CAS+"<br>"+chemical.name+"</td>\r\n");
				//				this.createImage(chemical.CAS, folder);
				//				TaskCalculations.CreateStructureImage(chemical.CAS, folder);

				writeEndpoints(humanHealthEffectsEndpoints, fw, width, chemical);
				writeEndpoints(ecotoxEndpoints, fw, width, chemical);
				writeEndpoints(fateEndpoints, fw, width, chemical);

				fw.write("\t</tr>\r\n");
			}

			fw.write("</table>\r\n");
			fw.write("</body>\r\n\r\n");
			fw.write("</html>\r\n");

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//	void createImage(String CAS,String folder) {
	//		MolFileUtilities m=new MolFileUtilities();
	//		AtomContainer ac=(AtomContainer) m.LoadChemicalFromMolFileInJar("ValidatedStructures2d/"+CAS+".mol");
	//		System.out.println(ac==null);
	//	}

	private void writeEndpointHeader(String[] endpoints, FileWriter fw, int width) throws IOException {
		for (int i=0;i<endpoints.length;i++) {
			fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>"+endpoints[i]+"</th>\r\n");	
		}
	}


	private void writeEndpointHeader2(String[] endpoints, FileWriter fw, int width) throws IOException {
		for (int i=0;i<endpoints.length;i++) {

			if (endpoints[i].equals(Chemical.strAcute_Mammalian_Toxicity)) {
				fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray colspan=3>"+endpoints[i]+"</th>\r\n");
			} else if (endpoints[i].equals(Chemical.strNeurotoxicity) || endpoints[i].equals(Chemical.strSystemic_Toxicity)) {
				fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray colspan=2>"+endpoints[i]+"</th>\r\n");
			} else {
				fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray rowspan=2>"+endpoints[i]+"</th>\r\n");	
			}

		}
	}

	private void writeEndpoints(String[] endpoints, FileWriter fw, int width, Chemical chemical)
			throws NoSuchFieldException, IllegalAccessException, IOException {

		for (int i=0;i<endpoints.length;i++) {
			String var="score"+endpoints[i].replace(" / ", "_").replace(" ","_");
			Field myField = chemical.getClass().getField(var);
			String val=(String)myField.get(chemical);
			fw.write("\t\t<td bgcolor="+getColor(val)+" align=center width="+width+"px><a href=\"\">"+val+"</a></td>\r\n");	
		}
	}

	private void writeEndpointsForGUI(String CAS,FileWriter fw, int width, Chemical chemical,
			String recordFolderPath,boolean writeRecordPages)
					throws NoSuchFieldException, IllegalAccessException, IOException {
		//		JsonArray jaScores=chemical.get("scores").getAsJsonArray();
		ArrayList<Score> scores=chemical.scores;

		//		System.out.println(jaScores.size());
		//		for (int i=0;i<jaScores.size();i++) {
		for (int i=0;i<scores.size();i++) {	
			//			JsonObject joScore=jaScores.get(i).getAsJsonObject();
			Score score=scores.get(i);

			//			String score=joScore.get("final_score").getAsString();
			//			String hazard_name=joScore.get("hazard_name").getAsString();

			String final_score=score.final_score;
			String hazard_name=score.hazard_name;			


			//			System.out.println("here4");

			//			if (joScore.get("records")!=null) {
			if (score.records.size()>0) {	

				//				if (score.final_score==null) {
				//					System.out.println(CAS+"\t"+score.hazard_name);
				//				}

				//				JsonArray records=joScore.get("records").getAsJsonArray();
				//				ArrayList<ScoreRecord> records=score.records;
				//				System.out.println(records.size());
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px><a href=\""+recordFolderPath+"/"+CAS+"_"+hazard_name+".html\">"+final_score+"</a></td>\r\n");
				//				writeRecordPage(CAS, folder, hazard_name,records);

			} else {
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px>"+final_score+"</td>\r\n");
				fw.flush();
			}


			//			System.out.println("here5");

		}
	}

	private void writeEndpoints(String CAS,FileWriter fw, int width, Chemical chemical,
			String recordFolderPath,boolean writeRecordPages)
					throws NoSuchFieldException, IllegalAccessException, IOException {
		//		JsonArray jaScores=chemical.get("scores").getAsJsonArray();
		ArrayList<Score> scores=chemical.scores;

		//		System.out.println(jaScores.size());
		//		for (int i=0;i<jaScores.size();i++) {
		for (int i=0;i<scores.size();i++) {	
			//			JsonObject joScore=jaScores.get(i).getAsJsonObject();
			Score score=scores.get(i);

			//			String score=joScore.get("final_score").getAsString();
			//			String hazard_name=joScore.get("hazard_name").getAsString();

			String final_score=score.final_score;
			String hazard_name=score.hazard_name;			


			//			System.out.println("here4");

			//			if (joScore.get("records")!=null) {
			if (score.records.size()>0) {	

				//				if (score.final_score==null) {
				//					System.out.println(CAS+"\t"+score.hazard_name);
				//				}

				//				JsonArray records=joScore.get("records").getAsJsonArray();
				//				ArrayList<ScoreRecord> records=score.records;
				//				System.out.println(records.size());
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px><a href=\""+recordFolderPath+"/"+CAS+"_"+hazard_name+".html\">"+final_score+"</a></td>\r\n");
				//				writeRecordPage(CAS, folder, hazard_name,records);
				if(writeRecordPages) writeRecordPage2(CAS, score,recordFolderPath);
			} else {
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px>"+final_score+"</td>\r\n");
				fw.flush();
			}


			//			System.out.println("here5");

		}
	}




	private void writeEndpoints(String CAS,FileWriter fw, int width, Chemical chemical,String folder,
			String recordFolderName,boolean writeRecordPages)
					throws NoSuchFieldException, IllegalAccessException, IOException {
		//		JsonArray jaScores=chemical.get("scores").getAsJsonArray();
		ArrayList<Score> scores=chemical.scores;

		//		System.out.println(jaScores.size());
		//		for (int i=0;i<jaScores.size();i++) {
		for (int i=0;i<scores.size();i++) {	
			//			JsonObject joScore=jaScores.get(i).getAsJsonObject();
			Score score=scores.get(i);

			String final_score=score.final_score;
			String hazard_name=score.hazard_name;			
			if (score.records.size()>0) {	

				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px><a href=\""+recordFolderName+"/"+CAS+"_"+hazard_name+".html\">"+final_score+"</a></td>\r\n");
				if(writeRecordPages) writeRecordPage2(CAS, score,folder+"/"+recordFolderName);
			} else {
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px>"+final_score+"</td>\r\n");
				fw.flush();
			}
		}
	}

	//	private void writeRecordPage(String CAS, String folder, String hazard_name,JsonArray records) {
	private void writeRecordPage(String CAS, String folder, String hazard_name,ArrayList<ScoreRecord> records) {	
		try {
			Path directory = Paths.get(folder + "/AA dashboard record pages");
			if (!Files.exists(directory)) {
				Files.createDirectories(directory);
			}

			FileWriter fw = new FileWriter(directory.toString() + File.separator + CAS + "_" + hazard_name + ".html");

			fw.write("<!DOCTYPE HTML>\r\n");

			fw.write("<html>\r\n");

			fw.write("<head>\r\n");
			fw.write("\t<title>Records for "+hazard_name.replace("_"," ")+ " for "+CAS+"</title>\r\n");
			fw.write("</head>\r\n\r\n");

			fw.write("<body>\r\n");

			fw.write("<h2>Records for "+hazard_name.replace("_"," ")+ " for "+CAS+"</h2>\r\n");

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			//			String [] fields= {"Source","Score","Classification","Hazard Statement","Rationale","Route","Note"};

			fw.write("<tr style=\"background-color: lightgray\">\n");

			for (int i=0;i<ScoreRecord.displayFieldNames.length;i++) {
				fw.write("\t\t<th>"+ScoreRecord.displayFieldNames[i]+"</th>\r\n");
			}
			fw.write("\t</tr>\r\n");



			for (int j=0;j<records.size();j++) {
				fw.write("\t<tr>\r\n");

				//				JsonObject record=records.get(j).getAsJsonObject();
				ScoreRecord record=records.get(j);

				for (int i=0;i<ScoreRecord.actualFieldNames.length;i++) {
					String fieldName=ScoreRecord.actualFieldNames[i];

					//					System.out.println(CAS+"\t"+fieldName+"\t"+record.get(fieldName));


					Field myField = record.getClass().getField(fieldName);

					if (myField.get(record)==null) {
						fw.write("\t<td><BR></td>\r\n");
					} else {
						String val=(String)myField.get(record);

						//						if (fieldName.equals("source")) {
						//							if (val.equals(ScoreRecord.sourceDenmark)) {
						//								System.out.println(CAS+"\t"+record.category);
						//							}
						//						}


						if (fieldName.equals("score")) {
							fw.write("\t\t<td bgcolor="+getColor(val)+" align=center>"+val+"</td>\r\n");

						} else if (fieldName.equals("source")) {
							fw.write("\t\t<td bgcolor="+getColor(val)+" align=center>"+val);

							fw.write("<BR>("+record.getListType()+")");

							fw.write("</td>\r\n");

						} else {
							fw.write("\t<td>"+val+"</td>\r\n");	
						}
					}


					//					if (record.get(fieldName).isJsonNull()) {
					//						fw.write("\t<td><BR></td>\r\n");
					//					} else {
					//
					//						String val=record.get(fieldName).getAsString();
					//						
					//						if (fieldName.equals("score")) {
					//							fw.write("\t\t<td bgcolor="+getColor(val)+" align=center>"+val+"</td>\r\n");
					//						} else {
					//							fw.write("\t<td>"+val+"</td>\r\n");	
					//						}
					//					}


				}
				fw.write("\t</tr>\r\n");
			}


			fw.write("</table>\r\n");
			fw.write("</body>\r\n\r\n");
			fw.write("</html>\r\n");

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void writeRecordPage2(String CAS, Score score,String recordFolderPath) {	
		try {
			Path directory = Paths.get(recordFolderPath);
			if (!Files.exists(directory)) {
				Files.createDirectories(directory);
			}

			FileWriter fw = new FileWriter(directory.toString() + File.separator + score.hazard_name+"_"+CAS + ".html");

			//			System.out.println(directory.toString() + File.separator + CAS + "_" + score.hazard_name + ".html");

			fw.write("<!DOCTYPE HTML>\r\n");

			fw.write("<html>\r\n");

			fw.write("<head>\r\n");
			fw.write("\t<title>Records for "+score.hazard_name.replace("_"," ")+ " for "+CAS+"</title>\r\n");
			fw.write("</head>\r\n\r\n");

			fw.write("<body>\r\n");

			fw.write("<h2>Records for "+score.hazard_name.replace("_"," ")+ " for "+CAS+"</h2>\r\n");

			//			createFinalScoreTable(score, fw);
			createSourcesTable(score, fw);
			fw.write("</body>\r\n\r\n");
			fw.write("</html>\r\n");

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	public static String formatDoubleValue(double dose) {
		DecimalFormat df = new DecimalFormat("0.00");
		DecimalFormat df2 = new DecimalFormat("0");
		DecimalFormat dfSci = new DecimalFormat("0.00E00");

		double doseRoundDown = Math.floor(dose);

		double percentDifference = Math.abs(doseRoundDown - dose) / dose * 100.0;

		if (dose < 0.01) {
			return dfSci.format(dose);
		} else {
			if (percentDifference > 0.1) {
				return df.format(dose);
			} else {
				return df2.format(dose);
			}
		}

	}

	private void createSourcesTable(Score score, FileWriter fw)
			throws IOException, NoSuchFieldException, IllegalAccessException {


		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		if (AADashboard.finalScoreScheme.equals(AADashboard.finalScoreSchemeTrumping)) {
			fw.write("<caption>Score records sorted by Authority (descending), Score (descending), and Source (ascending)</caption>\n");
		} else if (AADashboard.finalScoreScheme.equals(AADashboard.finalScoreSchemeWeightedAverage)) {
			fw.write("<caption>Score records sorted by Source (ascending)</caption>\n");
		}

		//			String [] fields= {"Source","Score","Classification","Hazard Statement","Rationale","Route","Note"};

		fw.write("<tr style=\"background-color: lightgray\">\n");

		for (int i=0;i<ScoreRecord.displayFieldNames.length;i++) {
			fw.write("\t\t<th>"+ScoreRecord.displayFieldNames[i]+"</th>\r\n");
			if (i==0) {
				fw.write("\t\t<th>Authority</th>\r\n");
			}
		}
		fw.write("\t</tr>\r\n");


		boolean selectedSource=false;//variable to determine whether the selected source has been highlighted yet

		boolean allNA=true;//all sources have score of NA

		for (int j=0;j<score.records.size();j++) {
			ScoreRecord record=score.records.get(j);
			if (!record.score.equals(ScoreRecord.scoreNA)) {
				allNA=false;
				break;
			}
		}

		//		System.out.println(score.hazard_name+"\t"+allNA);

		for (int j=0;j<score.records.size();j++) {

			ScoreRecord record=score.records.get(j);

			if (AADashboard.finalScoreScheme.equals(AADashboard.finalScoreSchemeTrumping)) {
				if (!selectedSource && (!record.score.equals(ScoreRecord.scoreNA) || allNA)) {
					fw.write("\t<tr bgcolor=lightblue>\r\n");//highlight recommended source in blue
					selectedSource=true;
				} else {
					fw.write("\t<tr>\r\n");	
				}
			} else {
				fw.write("\t<tr>\r\n");
			}

			//				JsonObject record=records.get(j).getAsJsonObject();


			for (int i=0;i<ScoreRecord.actualFieldNames.length;i++) {
				String fieldName=ScoreRecord.actualFieldNames[i];

				//					System.out.println(CAS+"\t"+fieldName+"\t"+record.get(fieldName));


				Field myField = record.getClass().getField(fieldName);

				if (myField.get(record)==null) {
					fw.write("\t<td><BR></td>\r\n");
				} else {
					String val="";
					
					if (myField.getType().toString().contains("Double")) {
						DecimalFormat df=new DecimalFormat("0.000");
						val=formatDoubleValue((Double)myField.get(record));
												
					} else {
						val=(String)myField.get(record);						
					}
					
//					System.out.println(myField.getType());

					

					if (fieldName.equals("score")) {
						fw.write("\t\t<td bgcolor="+getColor(val)+" align=center>"+val+"</td>\r\n");
						//						} else if (fieldName.equals("source")) {
						//							fw.write("\t\t<td align=center>"+val);
						//							fw.write("<BR>("+ScoreRecord.getListType(val)+")");
						//							fw.write("</td>\r\n");
					 					

					} else if (fieldName.contentEquals("valueMass")) {
						
						if (record.valueMassOperator==null || record.valueMassOperator.contentEquals("=")) {
							fw.write("\t<td>"+val+" "+record.valueMassUnits+"</td>\r\n");
						} else {
							fw.write("\t<td>"+record.valueMassOperator+" "+val+" "+record.valueMassUnits+"</td>\r\n");							
						}
						

					
					} else if (fieldName.contentEquals("duration")) {
						fw.write("\t<td>"+val+" "+record.durationUnits+"</td>\r\n");
					
					} else if (fieldName.contentEquals("source")) {
						
						if (record.source.contentEquals(ScoreRecord.sourceJapan)) {
							
							String []urls=record.url.replace(" Revised by ", "").split(";");
							
							fw.write("\t<td>Japan:<br>");								
							fw.write("<a href=\""+urls[0]+"\">New Classification</a>");
							
							for (int ii=1;ii<urls.length;ii++) {
								fw.write("<br><a href=\""+urls[ii]+"\">Revision "+ii+"</a>");
							}
							
							fw.write("</td>\r\n");
							
						} else if (record.url!=null && !record.url.isEmpty() && !record.source.contentEquals(ScoreRecord.sourceToxVal)) {
							fw.write("\t<td><a href=\""+record.url+"\">"+val+"</a></td>\r\n");
						} else {
							fw.write("\t<td>"+val+"</td>\r\n");
						}
					
					} else if (fieldName.contentEquals("sourceOriginal")) {

						if (record.url!=null && record.url.length()>1)
							fw.write("\t<td><a href=\""+record.url+"\">"+val+"</a></td>\r\n");
						else
							fw.write("\t<td>"+val+"</td>\r\n");

					
					} else {
						fw.write("\t<td>"+val+"</td>\r\n");	
					}

				}

				if (i==0) {//add list type td
					//					System.out.println(record.source+"\t"+record.getListType());
					fw.write("\t\t<td>"+record.getListType()+"</td>\r\n");
				}


			}
			fw.write("\t</tr>\r\n");
		}


		fw.write("</table>\r\n");
	}

//	private void createFinalScoreTable(Score score, FileWriter fw) throws IOException {
//		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
//
//		fw.write("<tr>\n");
//		fw.write("\t<td bgcolor=lightgrey>Final Score</td>\r\n");	
//		fw.write("\t<td bgcolor=\""+getColor(score.final_score)+"\" align=center>"+score.final_score+"</td>\r\n");
//		fw.write("</tr>\n");
//
//		if (score.final_score_source!=null) {
//
//			fw.write("<tr>\n");
//			fw.write("\t<td bgcolor=lightgrey>Final Score Source</td>\r\n");	
//			fw.write("\t<td>"+score.final_score_source+"</td>\r\n");
//			fw.write("</tr>\n");
//
//			fw.write("<tr>\n");
//			fw.write("\t<td bgcolor=lightgrey>Final Score List Type</td>\r\n");	
//			fw.write("\t<td>"+score.final_score_authority+"</td>\r\n");
//			fw.write("</tr>\n");
//		}
//
//		fw.write("</table><p>\r\n");
//	}


	String getColor(String val) {
		String color="";

		if (val.equals("L")) {
			color="lightgreen";
		} else if (val.equals("M")) {
			color="yellow";
		} else if (val.equals("H")) {
			color="orange";
		} else if (val.equals("VH")) {
			color="red";
		} else {
			color="lightgray";
		}
		return color;
	}


	void writeStyle(FileWriter fw) throws Exception {
		int width=100;

		fw.write("<style>\r\n");
		fw.write("table { border-collapse:collapse; }\r\n");
		fw.write("td, th {border: 1px black solid; padding: 3px; }\r\n");
		fw.write("th {background-color: lightgray; }\r\n");
		//			fw.write(".example { width:"+width+"px; }\r\n");
		//			fw.write(".example { text-align: center; vertical-align: center; }\r\n");


		//			String rotate=".verticalTableHeader {\r\n" + 
		//					"    text-align:left;\r\n"+
		//					"    white-space:nowrap;\r\n" + 
		//					"    g-origin:50% 50%;\r\n" + 
		//					"    -webkit-transform: rotate(90deg);\r\n" + 
		//					"    -moz-transform: rotate(90deg);\r\n" + 
		//					"    -ms-transform: rotate(90deg);\r\n" + 
		//					"    -o-transform: rotate(90deg);\r\n" + 
		//					"    transform: rotate(90deg);\r\n" + 
		//					"    \r\n" + 
		//					"}\r\n";
		//			fw.write(rotate);


		//		String rotate=".verticalTableHeader {\r\n" + 
		//				"			  /* Something you can count on */\r\n" + 
		//				"			  height: 140px;\r\n" + 
		//				"			  white-space: nowrap;\r\n" + 
		//				"			}\r\n" + 
		//				"\r\n" + 
		//				"			th.rotate > div {\r\n" + 
		//				"			  transform: \r\n" + 
		//				"			    /* Magic Numbers */\r\n" + 
		//				"			    translate(25px, 51px)\r\n" + 
		//				"			    /* 45 is really 360 - 45 */\r\n" + 
		//				"			    rotate(315deg);\r\n" + 
		//				"			  width: 30px;\r\n" + 
		//				"			}\r\n" + 
		//				"			th.rotate > div > span {\r\n" + 
		//				"			  border-bottom: 1px solid #ccc;\r\n" + 
		//				"			  padding: 5px 10px;\r\n" + 
		//				"			}";

		//		String rotate=".verticalTableHeader\r\n" + 
		//				"            {\r\n" + 
		//				"                text-align: center;\r\n" + 
		//				"                vertical-align: middle;\r\n" + 
		//				"                width: 20px;\r\n" + 
		//				"                margin: 0px;\r\n" + 
		//				"                padding: 0px;\r\n" + 
		//				"                padding-left: 3px;\r\n" + 
		//				"                padding-right: 3px;\r\n" + 
		//				"                padding-top: 10px;\r\n" + 
		//				"                white-space: nowrap;\r\n" + 
		//				"                -webkit-transform: rotate(-90deg); \r\n" + 
		//				"                -moz-transform: rotate(-90deg);                 \r\n" + 
		//				"            };";
		//		
		//		
		//		
		//		fw.write(rotate+"/r/n");



		fw.write("</style>\r\n");

		//		String style="<style>"
		//				+ ".verticalTableHeader {\r\n" + 
		//				"    text-align:left;\r\n"+
		//				"    white-space:nowrap;\r\n" + 
		//				"    g-origin:50% 50%;\r\n" + 
		//				"    -webkit-transform: rotate(90deg);\r\n" + 
		//				"    -moz-transform: rotate(90deg);\r\n" + 
		//				"    -ms-transform: rotate(90deg);\r\n" + 
		//				"    -o-transform: rotate(90deg);\r\n" + 
		//				"    transform: rotate(90deg);\r\n" + 
		//				"    \r\n" + 
		//				"}\r\n" + 
		//				".verticalTableHeader p {\r\n" + 
		//				"    margin:0 -100% ;\r\n" + 
		//				"    display:inline-block;\r\n" + 
		//				"}\r\n" + 
		//				".verticalTableHeader p:before{\r\n" + 
		//				"    content:'';\r\n" + 
		//				"    width:0;\r\n" + 
		//				"    padding-top:110%;/* takes width as reference, + 10% for faking some extra padding */\r\n" + 
		//				"    display:inline-block;\r\n" + 
		//				"    vertical-align:middle;\r\n" + 
		//				"}\r\n" + 
		//				"table {\r\n" + 
		//				"    text-align:center;\r\n" + 
		//				"    table-layout : fixed;\r\n" + 
		//				"    width:150px\r\n" + 
		//				"}\r\n" + 
		//				"</style>\r\n";
		//		fw.write(style+"\r\n");

	}


	//	void generateComparisonTableFromJSONFile(String jsonFilePath,String outputFolder,String outputFileName) {
	//			
	//			
	//			String[] humanHealthEffectsEndpoints = { "Acute Mammalian Toxicity", "Carcinogenicity", 
	//					"Genotoxicity / Mutagenicity","Endocrine Disruption","Reproductive",
	//					"Developmental","Neurological","Repeated Dose","Skin Sensitization","Skin Irritation",
	//					"Eye Irritation" };
	//	
	//			
	//			String [] ecotoxEndpoints={"Acute Aquatic Toxicity","Chronic Aquatic Toxicity"};
	//			String [] fateEndpoints={"Persistence","Bioaccumulation"};
	//			
	//			
	//			try {
	//				FileWriter fw=new FileWriter(outputFolder+"/"+outputFileName);
	//				
	////				Gson gson = new Gson();
	////				String jsonFilePath=folder+"/AA dashboard json testing file.json";
	////				JsonReader reader = new JsonReader(new FileReader(jsonFilePath));
	////				
	////				
	////				JsonObject jo = gson.fromJson(reader, JsonObject.class);
	////				JsonArray chemicals=(JsonArray)jo.get("chemicals");
	//				
	////				String jsonFilePath=folder+"/AA dashboard json testing file.json";
	////				String jsonFilePath=folder+"/Records from NITE.json";
	////				String jsonFilePath=folder+"/flame retardant records.json";
	//				
	//				
	//				Chemicals chemicals=Chemicals.loadFromJSON(jsonFilePath);
	//				
	////				JsonElement element=chemicals.toJsonElement();
	////				JsonObject object=new JsonObject();
	////				object.add("chemicals", element);
	////				System.out.println(object.toString());
	//				
	////				System.out.println(chemicals.size());
	//				
	////				if (true) return;
	//				
	//				fw.write("<!DOCTYPE HTML>\r\n");
	//				
	//				fw.write("<html>\r\n");
	//				
	//				fw.write("<head>\r\n");
	//				fw.write("\t<title>Comparison of alternatives</title>\r\n");
	//				
	//	
	//				int width=20;
	//	
	//				this.writeStyle(fw);
	//				
	//	
	//				fw.write("</head>\r\n\r\n");
	//				
	//				fw.write("<body>\r\n");
	//				
	//				fw.write("<h2>Comparison of alteratives</h2>\r\n");
	//				
	//				fw.write("<table>\r\n");
	//				
	//				
	//				fw.write("\t<tr>\r\n");
	//				fw.write("\t\t<th rowspan=\"2\" width=100px>Structure<br>CAS<br>name</th>\r\n");
	//				fw.write("\t\t<th colspan=\"11\" width="+width*humanHealthEffectsEndpoints.length+"px>Human Health Effects</th>\r\n");
	//				fw.write("\t\t<th colspan=\"2\" width="+width*ecotoxEndpoints.length+"px>Ecotoxicity</th>\r\n");
	//				fw.write("\t\t<th colspan=\"2\" width="+width*fateEndpoints.length+"px>Fate</th>\r\n");
	//				fw.write("\t</tr>\r\n");
	//				
	////				fw.write("\t<tr height=500>\r\n");
	////				fw.write("\t<tr height=200>\r\n");
	//				fw.write("\t<tr>\r\n");
	//				writeEndpointHeader(humanHealthEffectsEndpoints, fw, width);
	//				writeEndpointHeader(ecotoxEndpoints, fw, width);
	//				writeEndpointHeader(fateEndpoints, fw, width);
	//				fw.write("\t</tr>\r\n");
	//	
	//	
	//				for (int i=0;i<chemicals.size();i++) {
	////					JsonObject chemical=(JsonObject)chemicals.get(i);
	//					
	//					Chemical chemical=chemicals.get(i);
	//
	//					fw.write("\t<tr>\r\n");
	//	
	////					String CAS=chemical.get("CAS").getAsString();
	////					String Name=chemical.get("Name").getAsString();
	//					
	//					fw.write("\t\t<td width="+width+"px>"+chemical.CAS+"<br>"+chemical.name+"</td>\r\n");
	//	
	////					fw.write("\t\t<td width="+width+"px><img src=\""+CAS+".png\">"+CAS+"<br>"+Name+"</td>\r\n");
	////					this.createImage(CAS, folder);
	////					TaskCalculations.CreateStructureImage(chemical.CAS, folder);
	//					
	//					writeEndpoints(chemical.CAS,fw, width, chemical,outputFolder);
	//					
	//					
	//					fw.write("\t</tr>\r\n");
	//				}
	//				
	//				fw.write("</table>\r\n");
	//				fw.write("</body>\r\n\r\n");
	//				fw.write("</html>\r\n");
	//				
	//				fw.close();
	//				System.out.println("done");
	//			} catch (Exception ex) {
	//				ex.printStackTrace();
	//			}
	//		}
	


	


	
	





	
	

	

	/**
	 * This version has Acute Mammalian Toxicity broken out by route
	 * 
	 * @param jsonFilePath
	 * @param outputFolder
	 * @param outputFileName
	 */
	public void generateComparisonTableFromJSONFile2(Chemicals chemicals,String outputFolder,String outputFileName,String recordFolderName,boolean writeRecordPages) {

		String[] humanHealthEffectsEndpoints = { Chemical.strAcute_Mammalian_Toxicity, 
				Chemical.strCarcinogenicity,Chemical.strGenotoxicity_Mutagenicity,Chemical.strEndocrine_Disruption,
				Chemical.strReproductive,Chemical.strDevelopmental,
				Chemical.strNeurotoxicity,
				Chemical.strSystemic_Toxicity,
				Chemical.strSkin_Sensitization,Chemical.strSkin_Irritation, Chemical.strEye_Irritation};


		String [] ecotoxEndpoints={Chemical.strAcute_Aquatic_Toxicity,Chemical.strChronic_Aquatic_Toxicity};
		String [] fateEndpoints={Chemical.strPersistence,Chemical.strBioaccumulation};

		int width=20;


		try {
			FileWriter fw=new FileWriter(outputFolder+"/"+outputFileName);
			fw.write("<!DOCTYPE HTML>\r\n");
			fw.write("<html>\r\n");
			fw.write("<head>\r\n");
			fw.write("\t<title>Comparison of alternatives</title>\r\n");
			this.writeStyle(fw);

			fw.write("</head>\r\n\r\n");

			fw.write("<body>\r\n");

			fw.write("<h2>Comparison of alteratives</h2>\r\n");
			
			fw.write("<table>\r\n");

			writeAAHeader(humanHealthEffectsEndpoints, ecotoxEndpoints, fateEndpoints, width, fw);

			//			System.out.println("here2");

			for (int i=0;i<chemicals.size();i++) {
				//				JsonObject chemical=(JsonObject)chemicals.get(i);

				Chemical chemical=chemicals.get(i);

				fw.write("\t<tr>\r\n");
				//				String CAS=chemical.get("CAS").getAsString();
				//				String Name=chemical.get("Name").getAsString();
				//				fw.write("\t\t<td width="+width+"px>"+chemical.CAS+"<br>"+chemical.name+"</td>\r\n");
				fw.write("\t\t<td width="+width+"px>"+chemical.CAS+"</td>\r\n");
				//				fw.write("\t\t<td width="+width+"px><img src=\""+CAS+".png\">"+CAS+"<br>"+Name+"</td>\r\n");
				//				this.createImage(CAS, folder);
				//				TaskCalculations.CreateStructureImage(chemical.CAS, folder);
				writeEndpoints(chemical.CAS,fw, width, chemical,outputFolder,recordFolderName,writeRecordPages);
				fw.write("\t</tr>\r\n");
			}

			//			System.out.println("here3");
			fw.write("</table>\r\n");
			fw.write("</body>\r\n\r\n");
			fw.write("</html>\r\n");

			fw.close();
			System.out.println("done");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This version has Acute Mammalian Toxicity broken out by route
	 * 
	 * @param jsonFilePath
	 * @param outputFolder
	 * @param outputFileName
	 */
	public void writeHeaderForGUI(FileWriter fw) throws Exception {

		fw.write("<!DOCTYPE HTML>\r\n");
		fw.write("<html>\r\n");
		fw.write("<head>\r\n");
		fw.write("\t<title>Hazard profiles for selected chemicals</title>\r\n");
		this.writeStyle(fw);

		fw.write("</head>\r\n\r\n");

		fw.write("<body>\r\n");

		fw.write("<h2>Comparison of hazard profiles</h2>\r\n");
		
		fw.write("<p><em>Note: the scores below do not represent final hazard determinations and "
				+ "do not imply endorsement from EPA as a safe or unsafe product and "
				+ "should not be used for regulatory purposes.</em></p>\r\n");

		//		fw.write("<p><a href=\""+excelFileName+"\">Results Excel File</a></p>\n");

		fw.write("<table>\r\n");

		writeAAHeader(humanHealthEffectsEndpoints, ecotoxEndpoints, fateEndpoints, width, fw);

	}

	public void WriteRowInComparisonTable(FileWriter fw, AtomContainer ac,Chemical chemical,String relativePathRecordFolder) throws Exception {
		fw.write("\t<tr>\r\n");
		fw.write("\t\t<td align=center width="+width+"px>");

		String imgPath=relativePathRecordFolder+"\\structure_"+chemical.CAS+".png";

		fw.write("<a href=\""+imgPath+"\"><img src=\""+imgPath+"\"></a><br>");

		fw.write(chemical.CAS+"<br>");
		
		if (chemical.name!=null) fw.write(chemical.name);

		if (ac.getProperty("Parent")!=null) 
			fw.write("<br>(Breakdown product of "+ac.getProperty("Parent")+")");

		fw.write("</td>\r\n");

		for (int i=0;i<chemical.getScores().size();i++) {	
			Score score=chemical.getScores().get(i);
			String final_score=score.final_score;
			String hazard_name=score.hazard_name;			
			
			if (score.records.size()>0) {
				if (final_score.contentEquals("N/A")) final_score="I";			
				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px><a href=\""+relativePathRecordFolder+"/"+hazard_name+"_"+chemical.CAS+".html\">"+final_score+"</a></td>\r\n");
			} else {
//				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px>"+final_score+"</td>\r\n");
				fw.write("\t\t<td bgcolor=white align=center width="+width+"px><br></td>\r\n");
				fw.flush();
			}
		}

		fw.write("\t</tr>\r\n");
	}


	public void WriteRestOfComparisonTable(FileWriter fw)  throws Exception {
		fw.write("</table>\r\n");
		fw.write("</body>\r\n\r\n");
		fw.write("</html>\r\n");
		fw.flush();
		fw.close();
	}

	private void writeAAHeader(String[] humanHealthEffectsEndpoints, String[] ecotoxEndpoints, String[] fateEndpoints,
			int width, FileWriter fw) throws IOException {
		fw.write("\t<tr>\r\n");

		//			fw.write("\t\t<th rowspan=\"3\" width=100px>Structure<br>CAS<br>name</th>\r\n");
		fw.write("\t\t<th rowspan=\"3\" width=100px>Chemical</th>\r\n");

		fw.write("\t\t<th colspan=\""+(humanHealthEffectsEndpoints.length+4)+"\" width="+width*(humanHealthEffectsEndpoints.length+4)+"px>Human Health Effects</th>\r\n");
		fw.write("\t\t<th colspan=\"2\" width="+width*ecotoxEndpoints.length+"px>Ecotoxicity</th>\r\n");
		fw.write("\t\t<th colspan=\"2\" width="+width*fateEndpoints.length+"px>Fate</th>\r\n");
		fw.write("\t</tr>\r\n");

		//			fw.write("\t<tr height=500>\r\n");
		//			fw.write("\t<tr height=200>\r\n");
		fw.write("\t<tr>\r\n");
		writeEndpointHeader2(humanHealthEffectsEndpoints, fw, width);
		writeEndpointHeader2(ecotoxEndpoints, fw, width);
		writeEndpointHeader2(fateEndpoints, fw, width);
		fw.write("\t</tr>\r\n");

		fw.write("\t<tr>\r\n");

		//Write routes for acute mammalian toxicity in header:
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Oral</th>\r\n");
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Inhalation</th>\r\n");
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Dermal</th>\r\n");

		//Write exposure duration for neurotoxicity in header:
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Repeat Exposure</th>\r\n");
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Single Exposure</th>\r\n");

		//Write exposure duration for systemic organic toxicity in header:
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Repeat Exposure</th>\r\n");
		fw.write("\t\t<th width="+width+"px class=\"verticalTableHeader\" bgcolor=lightgray>Single Exposure</th>\r\n");

		fw.write("\t</tr>\r\n");
	}

	public static void main(String[] args) throws Exception {
		//		TableGeneratorHTML t=new TableGeneratorHTML();
		//		String outputFolder="AA dashboard/Output/realtime";
		//		String jsonFilePath = outputFolder + "/flame retardant records.json";
		//		String outputFileName="flame retardant records.html";
		//		t.generateComparisonTableFromJSONFile2(jsonFilePath, outputFolder, outputFileName,"AA dashboard record pages");


		//		
	}



}
