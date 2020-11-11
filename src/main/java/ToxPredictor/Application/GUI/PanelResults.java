package ToxPredictor.Application.GUI;


import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.LinkedHashMap;
import java.util.List;
import java.awt.image.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import AADashboard.Application.TableGeneratorExcel;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreator;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskCalculations2;
import ToxPredictor.Application.Calculations.WriteBatchResults;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.MyDescriptors.DescriptorData;


import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.Utilities;

public class PanelResults extends JDialog {

	/**
	 * @param args
	 */

//	public boolean Locked=false;
	
	JTable tableMethod = new JTable();
	JTable tableAllMethods = new JTable();
	JTable tableDescriptors = new JTable();

	JTabbedPane jtabbedPane;
	
	JScrollPane scrollPane;
	JScrollPane scrollPane2;
	JScrollPane scrollPane3;
	
//	keyAdapter ka=new keyAdapter();
	actionAdapter aa=new actionAdapter();
	
	public JButton jbSaveToExcel=new JButton();
	public JButton jbSaveToText=new JButton();
	public JButton jbSaveToHTML=new JButton();

	int inset=20;
	
	TESTApplication f;
	
	
	public PanelResults(JFrame owner,String title,boolean modal) {		
		super(owner, title, modal);
		f=(TESTApplication)owner;
		init();
		setUpSimpleControls();
		
	}
	
	private void setUpSimpleControls() {
		
		int width=200;
		
		jbSaveToExcel.setActionCommand("jbSaveToExcel");
		jbSaveToExcel.addActionListener(aa);
		jbSaveToExcel.setSize(width,20);
		jbSaveToExcel.setLocation(inset,scrollPane.getY()+scrollPane.getHeight()+inset);
		jbSaveToExcel.setText("Save to Excel (.xlsx)");
		add(jbSaveToExcel);

		jbSaveToText.setText("Save to text (.csv)");
		jbSaveToText.setActionCommand("jbSaveToText");
		jbSaveToText.addActionListener(aa);
		jbSaveToText.setSize(width,20);
		jbSaveToText.setLocation(jbSaveToExcel.getX()+jbSaveToExcel.getWidth()+inset,scrollPane.getY()+scrollPane.getHeight()+inset);
		add(jbSaveToText);
		
		jbSaveToHTML.setText("Save to web pages (.html)");
		jbSaveToHTML.setActionCommand("jbSaveToHTML");
		jbSaveToHTML.addActionListener(aa);
		jbSaveToHTML.setSize(width,20);
		jbSaveToHTML.setLocation(jbSaveToText.getX()+jbSaveToText.getWidth()+inset,scrollPane.getY()+scrollPane.getHeight()+inset);
		add(jbSaveToHTML);


	}
	
	
	/**
	 * Bases size of scroll pane on the size on the fraMain screen
	 * @param width
	 * @param height
	 */
	public void init() {

		//		double SF=(double)DisplaySize/(double)ImageSize;
//		System.out.println(width+"\t"+height);

		Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();
		int height=(int)(scrnsize.height*0.65);
		int width=(int)(scrnsize.width*0.75);
		
		this.setResizable(false);
		this.setSize(width, height); // need space for controls at bottom
						
		Utilities.CenterFrame(this);
		
		//		Utilities.SetFonts(this.getContentPane());
		Utilities.SetFonts(this);

		//		this.getContentPane().setLayout(null);		
		this.setLayout(null);

		//Create the scroll pane and add the table to it.
		scrollPane = new JScrollPane(tableMethod);
		scrollPane.setLocation(inset, inset); //shift it over a bit
		scrollPane.setSize(width-2*inset, height-6*inset);

		scrollPane2 = new JScrollPane(tableAllMethods);
		scrollPane2.setLocation(scrollPane.getLocation()); //shift it over a bit
		scrollPane2.setSize(scrollPane.getSize());
		
		scrollPane3 = new JScrollPane(tableDescriptors);
		scrollPane3.setLocation(scrollPane.getLocation()); //shift it over a bit
		scrollPane3.setSize(scrollPane.getSize());

		
		jtabbedPane=new JTabbedPane();
		jtabbedPane.setSize(scrollPane.getSize());
		add(jtabbedPane);
		
		tableDescriptors.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				
	}

	public void initTableModel(String endpoint,String method) {
				
		jtabbedPane.removeAll();

		
		jtabbedPane.add("Results", scrollPane);

		String [] colNames=MyTableModel.getColNames(endpoint, method);
		MyTableModel tableModel=new MyTableModel(colNames);
		tableModel.setupTable(tableMethod);

		if(method.contentEquals(TESTConstants.ChoiceConsensus)) {
			jtabbedPane.add("Individual methods", scrollPane2);		
			String [] colNames2=MyTableModelAllQSARMethods.getColumnNames(endpoint, method);
			MyTableModelAllQSARMethods tableModelAll=new MyTableModelAllQSARMethods(colNames2);
			tableModelAll.setupTable(tableAllMethods);			
		} 

	}
	
	public void initTableModelDescriptors() {
		
		jtabbedPane.removeAll();

		jtabbedPane.add("Descriptors", scrollPane3);		
		String [] colNamesDescriptors=MyTableModelDescriptors.getColumnNames();
		MyTableModelDescriptors tableModelDescriptors=new MyTableModelDescriptors(colNamesDescriptors);
		tableModelDescriptors.setupTable(tableDescriptors);

//		table3.getColumnModel().getColumn(0).setPreferredWidth(25);


	}
	
	
	
	private void saveToExcel() {
		
		String filename = "Batch_" + f.endpoint.replace(" ", "_") + "_" + f.method + ".xlsx";
		
		if (f.endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			filename = "Batch_Descriptors.xlsx";
		}
		
		String filepath=f.as.getOutputFolderPath()+File.separator+filename;
		
		File of=new File(filepath);
		
//		System.out.println(filepath);
		
//		f.chooserExcel.setCurrentDirectory(of);
		f.chooserExcel.setSelectedFile(of);
		
		int returnVal = f.chooserExcel.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserExcel.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".xlsx";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"\""+inFile.getName()+"\" exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}
		
        if (TaskCalculations2.isExcelOpen(inFile.getAbsolutePath())) {
        	JOptionPane.showMessageDialog(f, "Please close "+inFile.getAbsolutePath());        	
        	return;
        }		        	


		try {

			this.setCursor(Utilities.waitCursor);
			writeToExcelFile(inFile,f.endpoint,f.method);
			this.setCursor(Utilities.defaultCursor);
			
//            JOptionPane.showMessageDialog(f,
//            		"The file was saved successfully at\n"+ inFile.getAbsolutePath());
            		
           MyBrowserLauncher.launch(inFile.toURI());


		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}
	}

	public void writeToExcelFile(File inFile,String endpoint,String method) throws FileNotFoundException, IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			writeResultsToExcel(workbook,(MyTableModelDescriptors)this.tableDescriptors.getModel(),"Descriptors");
		} else {
			writeResultsToExcel(workbook,(MyTableModel)this.tableMethod.getModel(),method);
			
			if (method.contentEquals(TESTConstants.ChoiceConsensus))
				writeResultsToExcel(workbook,(MyTableModelAllQSARMethods)this.tableAllMethods.getModel(),"Individual methods");
		}
		
		FileOutputStream out = new FileOutputStream(new File(inFile.getAbsolutePath()));
		workbook.write(out);
		out.close();
	}
	
	private void writeResultsToExcel (XSSFWorkbook workbook,AbstractTableModel tableModel,String tabName) {
		try {
			
			XSSFSheet sheet = workbook.createSheet(tabName);
						

//			XSSFCellStyle styleBold=TableGenerator.getStyleBold(workbook);
			XSSFCellStyle styleBoldWrap=TableGeneratorExcel.getStyleBoldWrap(workbook);
			
			XSSFRow row0 = sheet.createRow(0);
			
			if (!tabName.contentEquals("Descriptors"))
				row0.setHeightInPoints(35);
			
			for (int col=0;col<tableModel.getColumnCount();col++) {
				XSSFCell cellj=row0.createCell(col);
				cellj.setCellValue(tableModel.getColumnName(col));
				cellj.setCellStyle(styleBoldWrap);				
			}
			
			
			for (int row=0;row<tableModel.getRowCount();row++) {
				
				XSSFRow rowi = sheet.createRow(row+1);
				
				for (int col=0;col<tableModel.getColumnCount();col++) {
					XSSFCell cellj=rowi.createCell(col);
					String val=tableModel.getValueAt(row, col)+"";
					cellj.setCellValue(val);
				}
								
			}
			for (int col=0;col<tableModel.getColumnCount();col++) {
				sheet.autoSizeColumn(col);	
			}
						
			sheet.createFreezePane( 0, 1, 0, 1 );
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	void writeResultsToText (File file,AbstractTableModel tableModel) {
		try {
			
			FileWriter fw=new FileWriter(file);
			
			String header="";
			for (int col=0;col<tableModel.getColumnCount();col++) {				
				String colName=tableModel.getColumnName(col);
				colName=colName.replace("\n", "_");
				header+="\""+colName+"\"";				
				if (col<tableModel.getColumnCount()-1) header+=",";
				
			}
			fw.write(header+"\r\n");
			fw.flush();
									
//			System.out.println("rowCount="+tableModel.getRowCount());
						
			for (int row=0;row<tableModel.getRowCount();row++) {								
				String Line="";
				
				long t1=System.currentTimeMillis();
				for (int col=0;col<tableModel.getColumnCount();col++) {
					String val=tableModel.getValueAt(row, col)+"";
					Line+="\""+val+"\"";

					if (col<tableModel.getColumnCount()-1) Line+=",";
				}
				long t2=System.currentTimeMillis();
//				System.out.println("row="+row+" "+(t2-t1)+"ms");
				
				fw.write(Line+"\r\n");
				fw.flush();								
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeResultsToHTML (File file,AbstractTableModel tableModel,Vector<TESTPredictedValue>vecTPV) {
		try {
			
			PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();
			
			FileWriter fw=new FileWriter(file);
						
			writeHeader(fw);

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw.write("<tr bgcolor=\"#D3D3D3\">\r\n");
			for (int col=0;col<tableModel.getColumnCount();col++) {
				fw.write("<th>"+tableModel.getColumnName(col)+"</th>\r\n");
				
				if (tableModel.getColumnName(col).contentEquals("ID")) {
					fw.write("<th>Structure</th>\r\n");
				}
				
			}
			fw.write("</tr>\r\n");
									
			for (int row=0;row<tableModel.getRowCount();row++) {				
				fw.write("<tr>\r\n");
				for (int col=0;col<tableModel.getColumnCount();col++) {
					String val=tableModel.getValueAt(row, col)+"";
					
					
					if (tableModel.getColumnName(col).contentEquals("ID")) {
						
						TESTPredictedValue tpv=vecTPV.get(row);
						
						
						if (tpv.predictionResults!=null) {

							String outputFolder=tpv.predictionResults.getReportBase();
							
//							System.out.println("outputFolder="+outputFolder);

							String outputFileName=WebTEST4.getResultFileNameNoExtension(tpv.endpoint, tpv.method, tpv.id)+".html";
							String outputFilePath=outputFolder+File.separator+outputFileName;
							htmlCreator.writeResultsWebPages(tpv.predictionResults, outputFilePath);
							fw.write("<td><a href=\""+outputFilePath+"\">"+val+"</a></td>");
														
//							outputFileName=WebTEST4.getResultFileNameNoExtension(tpv.endpoint, tpv.method, tpv.id)+".json";
//							outputFilePath=outputFolder+File.separator+outputFileName;
//							WebTEST4.writeJSON(outputFilePath, tpv.predictionResults);
							
						} else {
							fw.write("<td>"+val+"</td>");
						}
						
						String imagePath="";
						
						if (tpv.gsid == null || !WebTEST4.dashboardStructuresAvailable) {
							
							if (tpv.predictionResults!=null) {
								File folderReportBase=new File(tpv.predictionResults.getReportBase());

								SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

								AtomContainer m = null;
								try {
									// m=sp.parseSmiles(Smiles);
									m = (AtomContainer) sp.parseSmiles(tpv.smiles);
									
									String imageFolder=folderReportBase.getParentFile().getAbsolutePath()+File.separator+"StructureData";
									imagePath=imageFolder+File.separator+"structure.png";

									ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "structure", imageFolder);

								} catch (Exception ex) {
									System.out.println(ex.getMessage());									
								}
							}
						} else {
							imagePath=PredictToxicityWebPageCreator.webPath + tpv.gsid;
						}
						fw.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+WriteBatchResults.getStructureImageHtmlDetails(imagePath)+"></a></td>\n");
					} else {
						fw.write("<td>"+val+"</td>");
					}
					
				}
				fw.write("</tr>\r\n");
				
				fw.flush();								
			}
			
			fw.write("</table></body></html>\r\n");
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeDescriptorResultsToHTML (File file,MyTableModelDescriptors tableModel) {
		try {
			
			Vector<LinkedHashMap<String,String>>vecTPV=tableModel.getPredictions();
			
			Vector<String>descNames=DescriptorData.getDescriptorNames();
			
			PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();
			
			FileWriter fw=new FileWriter(file);
						
			writeHeader(fw);

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw.write("<tr bgcolor=\"#D3D3D3\">\r\n");
			for (int col=0;col<tableModel.getColumnCount();col++) {
				fw.write("<th>"+tableModel.getColumnName(col)+"</th>\r\n");
				
//				if (tableModel.getColumnName(col).contentEquals("ID")) {
//					fw.write("<th>Structure</th>\r\n");
//				}
				
			}
			fw.write("</tr>\r\n");
									
			for (int row=0;row<tableModel.getRowCount();row++) {				
				fw.write("<tr>\r\n");
				for (int col=0;col<tableModel.getColumnCount();col++) {
					String val=tableModel.getValueAt(row, col)+"";
					
					
					if (tableModel.getColumnName(col).contentEquals("ID")) {
						
						LinkedHashMap<String,String> dd=vecTPV.get(row);
						
						String folder=file.getParentFile().getAbsolutePath();

						fw.write("<td>"+val+"</td>");
											
// TODO add images- need way to access gsid which isnt stored in hashmap right now						
//						String imagePath="";
//						
//						if (dd.get("gsid") == null || !WebTEST4.dashboardStructuresAvailable) {
//							
//							if (dd.get("Error").isEmpty()) {
//
//								File folderReportBase=file.getParentFile();
//								
//								SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//
//								AtomContainer m = null;
//								try {
//									// m=sp.parseSmiles(Smiles);
//									m = (AtomContainer) sp.parseSmiles(dd.get("SMILES"));
//									
//									String imageFolder=folderReportBase.getAbsolutePath()+File.separator+"StructureData"+File.separator+"ToxRuns"+File.separator+"ToxRun_"+dd.get("CAS");
//									imagePath=imageFolder+File.separator+"structure.png";
//
////									System.out.println(imagePath);
//									
//									ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "structure", imageFolder);
//
//								} catch (Exception ex) {
//									System.out.println(ex.getMessage());									
//								}
//							}
//						} else {
//							imagePath=PredictToxicityWebPageCreator.webPath + dd.get("gsid");
//						}
//						fw.write("<td><a href=\"" + imagePath + "\"><img src=\"" + imagePath + "\" "+WriteBatchResults.getStructureImageHtmlDetails(imagePath)+"></a></td>\n");
					} else {
						fw.write("<td>"+val+"</td>");
					}
					
				}
				fw.write("</tr>\r\n");
				
				fw.flush();								
			}
			
			fw.write("</table></body></html>\r\n");
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void writeHeader(FileWriter fw) throws IOException {
		fw.write("<!DOCTYPE HTML><html><head><title>\r\n");

		// System.out.println(endpoint.equals(TESTConstants.ChoiceDescriptors));

		if (!f.endpoint.equals(TESTConstants.ChoiceDescriptors))
			fw.write("Batch predictions for " + f.method + " method for " + f.endpoint + "\r\n");
		else {
			fw.write("Descriptors\r\n");
		}
		fw.write("</title></head><body>\r\n");

		
		if (!f.endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			fw.write("<h2>Batch predictions for " + f.method + " method for " + f.endpoint + "</h2>\r\n");
		} else {
			fw.write("<h2>Descriptors</h2>\r\n");
		}
	}
	
	
	
	
	class actionAdapter implements java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {

			String ac=e.getActionCommand();

			if (ac.equals("jbSaveToExcel")) {
				saveToExcel();
			} else if (ac.equals("jbSaveToText")) {
				saveToText();
			} else if (ac.equals("jbSaveToHTML")) {
				saveToHTML();
			}
		}
	}
	
	
	
	//	public void SetParentFrame(	Object gui) {
	//		this.gui=gui;
	//		
	//	}
	
	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param newPredictions
	 */
	public void addPredictionAllMethods(TESTPredictedValue newPrediction) {
		MyTableModelAllQSARMethods tableModel=(MyTableModelAllQSARMethods)tableAllMethods.getModel();
		tableModel.addPrediction(newPrediction);
	}
	
	private void saveToHTML() {
		String filename=null;
		
		if (f.endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			filename = "Batch_Descriptors.html";
		} else {
			if (jtabbedPane.getSelectedIndex()==0) {
				filename = "Batch_" + f.endpoint.replace(" ", "_") + "_" + f.method + ".html";
			} else if (jtabbedPane.getSelectedIndex()==1) {
				filename = "Batch_" + f.endpoint.replace(" ", "_") + "_AllMethods.html";
			}
		}
		
		String filepath=f.as.getOutputFolderPath()+File.separator+filename;
		
		File of=new File(filepath);
		
//		System.out.println(filepath);
		
//		f.chooserExcel.setCurrentDirectory(of);
		
		JFileChooser chooser=f.chooserHTML;
		
		chooser.setSelectedFile(of);
		
		int returnVal = chooser.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = chooser.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".xlsx";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"\""+inFile.getName()+"\" exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}
		
        if (TaskCalculations2.isExcelOpen(inFile.getAbsolutePath())) {
        	JOptionPane.showMessageDialog(f, "Please close "+inFile.getAbsolutePath());        	
        	return;
        }		        	


		try {
			
			this.setCursor(Utilities.waitCursor);
			writeToHTMLFile(inFile,f.endpoint);
			this.setCursor(Utilities.defaultCursor);
            
//            JOptionPane.showMessageDialog(f,
//            		"The file was saved successfully at\n"+ inFile.getAbsolutePath());
            		
           MyBrowserLauncher.launch(inFile.toURI());


		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}
		
	}

	public void writeToHTMLFile(File inFile,String endpoint) {
				
		if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {				
			writeDescriptorResultsToHTML(inFile,(MyTableModelDescriptors)this.tableDescriptors.getModel());
		} else {
			
//			System.out.println("selectedIndex="+jtabbedPane.getSelectedIndex());
						
			if (jtabbedPane.getSelectedIndex()==0) {
				writeResultsToHTML(inFile,(MyTableModel)this.tableMethod.getModel(),getPredictions());	
			} else if (jtabbedPane.getSelectedIndex()==1) {
				writeResultsToHTML(inFile,(MyTableModelAllQSARMethods)this.tableAllMethods.getModel(),getPredictions());
			}
		}
	}

	private void saveToText() {
		
		String filename=null;
		
		if (f.endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			filename = "Batch_Descriptors.csv";
		} else {
			if (jtabbedPane.getSelectedIndex()==0) {
				filename = "Batch_" + f.endpoint.replace(" ", "_") + "_" + f.method + ".csv";
			} else if (jtabbedPane.getSelectedIndex()==1) {
				filename = "Batch_" + f.endpoint.replace(" ", "_") + "_AllMethods.csv";
			}
		}
		
		String filepath=f.as.getOutputFolderPath()+File.separator+filename;
		
		File of=new File(filepath);
		
//		System.out.println(filepath);
		
//		f.chooserExcel.setCurrentDirectory(of);
		f.chooserCSV.setSelectedFile(of);
		
		int returnVal = f.chooserCSV.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserCSV.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".xlsx";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"\""+inFile.getName()+"\" exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}
		
        if (TaskCalculations2.isExcelOpen(inFile.getAbsolutePath())) {
        	JOptionPane.showMessageDialog(f, "Please close "+inFile.getAbsolutePath());        	
        	return;
        }		        	


		try {
			
			
			this.setCursor(Utilities.waitCursor);
			writeToTextFile(inFile,f.endpoint);
			this.setCursor(Utilities.defaultCursor);
			            
//            JOptionPane.showMessageDialog(f,
//            		"The file was saved successfully at\n"+ inFile.getAbsolutePath());
            		
           MyBrowserLauncher.launch(inFile.toURI());


		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}
		
	}

	public void writeToTextFile(File inFile,String endpoint) {
		if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			writeResultsToText(inFile,(MyTableModelDescriptors)this.tableDescriptors.getModel());
		} else {
			if (jtabbedPane.getSelectedIndex()==0) {
				writeResultsToText(inFile,(MyTableModel)this.tableMethod.getModel());	
			} else if (jtabbedPane.getSelectedIndex()==1) {
				writeResultsToText(inFile,(MyTableModelAllQSARMethods)this.tableAllMethods.getModel());
			}
//				if (f.method.contentEquals(TESTConstants.ChoiceConsensus))
//					writeResultsToExcel(workbook,(MyTableModelAllQSARMethods)this.table2.getModel(),"Individual methods");
		}
	}

	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param newPredictions
	 */
	public void addPrediction(DescriptorData dd) {
		MyTableModelDescriptors tableModel=(MyTableModelDescriptors)tableDescriptors.getModel();
		tableModel.addPrediction(dd);
	}

	
	
	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param newPredictions
	 */
	public void addPrediction(TESTPredictedValue tpv) {
		MyTableModel tableModel=(MyTableModel)tableMethod.getModel();
		tableModel.addPrediction(tpv);
	}

	
	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param moleculeSet
	 */
	public Vector<TESTPredictedValue> getPredictions() {
		MyTableModel tableModel=(MyTableModel)tableMethod.getModel();
		return tableModel.getPredictions();
	}
	
//	public void showCell(int row, int column,JTable table) {
//		Rectangle rect = table.getCellRect(row, column, true);
//		scrollRectToVisible(rect);
//	    table.clearSelection();
//	    table.setRowSelectionInterval(row, row);
//	    
//	}

	//	 This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(java.awt.Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	class windowAdapter extends java.awt.event.WindowAdapter {

		public void windowClosing(java.awt.event.WindowEvent e) {
			setVisible(false);
		}
	}

	
//	/**
//	 * Used to detect if the delete key is clicked (triggers delete event) or if
//	 * enter key is pressed (searchs by CAS in molecule id text box)
//	 */
//	class keyAdapter extends java.awt.event.KeyAdapter {
//
//		public void keyReleased(KeyEvent e) {
//			Object o = e.getSource();
//
//			
//		}
//	}
//	class mouseAdapter implements java.awt.event.MouseListener {
//
//		public void mouseClicked(MouseEvent e) {
//			if (e.getClickCount()==2) {
//				
//			}
//			
//		}
//
//		public void mouseEntered(MouseEvent e) {
//		}
//
//		public void mouseExited(MouseEvent e) {
//		}
//
//		public void mousePressed(MouseEvent e) {
//		}
//
//		public void mouseReleased(MouseEvent e) {
//		}
//
//	}

	
	
	

} // end overall class


	

