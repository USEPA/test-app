package ToxPredictor.Application.GUI;


import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
import ToxPredictor.Application.GUI.Table.MyTableModel;
import ToxPredictor.Application.GUI.Table.MyTableModelAllQSARMethods;
import ToxPredictor.Application.GUI.Table.MyTableModelDescriptors;
import ToxPredictor.Application.GUI.Table.MyTableModelHCD;
import ToxPredictor.Application.GUI.Table.MyTableModelHCD_ScoreRecords;
import ToxPredictor.Application.GUI.Table.MyTableModelLinks;
import ToxPredictor.Application.GUI.Table.MyTableModelMAE;
import ToxPredictor.Application.GUI.Table.MyTableModelSimilarChemical;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Application.model.SimilarChemicals;
import ToxPredictor.MyDescriptors.DescriptorData;


import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.Utilities;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.RecordLink;
import gov.epa.api.Score;

public class PanelResults extends JDialog {

	/**
	 * @param args
	 */

public //	public boolean Locked=false;
	
	JTable tableMethod = new JTable();
	public JTable tableAllMethods = new JTable();
	public JTable tableDescriptors = new JTable();
	JTable tableHCD = new JTable();
	JTable tableHCDScoreRecords = new JTable();
	JTable tableHCDScoreRecords2 = new JTable();
	JTable tableLinks = new JTable();
	
	JTable tableMAE_Training=new JTable();
	JTable tableMAE_Prediction=new JTable();
	
	
	JTable tableSimilarChemicalsTraining = new JTable();
	JTable tableSimilarChemicalsPrediction = new JTable();
	
	public JTabbedPane jtabbedPane;
	
	JScrollPane scrollPaneMethod;
	JScrollPane scrollPaneAllMethods;
	JScrollPane scrollPaneDescriptors;
	
	JScrollPane scrollPaneFinalScores;
	JScrollPane scrollPaneScoreRecords;
	JScrollPane scrollPaneLinks;

	JScrollPane scrollPaneScoreRecords2;
	JScrollPane scrollPaneSimilarChemicalsTraining;
	JScrollPane scrollPaneSimilarChemicalsPrediction;
	JScrollPane scrollPaneMAE_Training;
	JScrollPane scrollPaneMAE_Prediction;
	
//	keyAdapter ka=new keyAdapter();
	actionAdapter aa=new actionAdapter();
	
	public JButton jbSaveToExcel=new JButton();
	public JButton jbSaveToText=new JButton();
//	public JButton jbSaveToHTML=new JButton();
	public JButton jbViewReport=new JButton();

	int inset=20;
	
	TESTApplication f;
	
	JPanel panelTraining=new JPanel();
	JPanel panelPrediction=new JPanel();

	JLabel jlabelTrainingGraph=new JLabel();
	JLabel jlabelPredictionGraph=new JLabel();
	
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
		jbSaveToExcel.setLocation(inset,scrollPaneMethod.getY()+scrollPaneMethod.getHeight()+inset);
		jbSaveToExcel.setText("Save to Excel (.xlsx)");
		add(jbSaveToExcel);

		jbSaveToText.setText("Save to text (.csv)");
		jbSaveToText.setActionCommand("jbSaveToText");
		jbSaveToText.addActionListener(aa);
		jbSaveToText.setSize(width,20);
		jbSaveToText.setLocation(jbSaveToExcel.getX()+jbSaveToExcel.getWidth()+inset,scrollPaneMethod.getY()+scrollPaneMethod.getHeight()+inset);
		add(jbSaveToText);
		
//		jbSaveToHTML.setText("Save to web pages (.html)");
//		jbSaveToHTML.setActionCommand("jbSaveToHTML");
//		jbSaveToHTML.addActionListener(aa);
//		jbSaveToHTML.setSize(width,20);
//		jbSaveToHTML.setLocation(jbSaveToText.getX()+jbSaveToText.getWidth()+inset,scrollPaneMethod.getY()+scrollPaneMethod.getHeight()+inset);
//		add(jbSaveToHTML);

		jbViewReport.setText("View HTML report");
		jbViewReport.setActionCommand("jbViewReport");
		jbViewReport.addActionListener(aa);
		jbViewReport.setSize(width,20);
//		jbViewReport.setLocation(jbSaveToHTML.getLocation());
		jbViewReport.setLocation(jbSaveToText.getX()+jbSaveToText.getWidth()+inset,scrollPaneMethod.getY()+scrollPaneMethod.getHeight()+inset);
		add(jbViewReport);

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
		int height=(int)(scrnsize.height*0.85);
		int width=(int)(scrnsize.width*0.85);
		
		this.setResizable(false);
		this.setSize(width, height); // need space for controls at bottom
						
		Utilities.CenterFrame(this);
		
//		this.setLocation(this.getX(), f.getY());//align with top of main program
		
		//		Utilities.SetFonts(this.getContentPane());
		Utilities.SetFonts(this);

		//		this.getContentPane().setLayout(null);		
		this.setLayout(null);

		//Create the scroll pane and add the table to it.
		scrollPaneMethod = new JScrollPane(tableMethod);
		scrollPaneMethod.setLocation(inset, inset); //shift it over a bit
		scrollPaneMethod.setSize(width-2*inset, height-6*inset);

		scrollPaneAllMethods = new JScrollPane(tableAllMethods);
		scrollPaneAllMethods.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneAllMethods.setSize(scrollPaneMethod.getSize());
		
		scrollPaneDescriptors = new JScrollPane(tableDescriptors);
		scrollPaneDescriptors.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneDescriptors.setSize(scrollPaneMethod.getSize());

		scrollPaneFinalScores = new JScrollPane(tableHCD);
		scrollPaneFinalScores.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneFinalScores.setSize(scrollPaneMethod.getSize());

		scrollPaneLinks = new JScrollPane(tableLinks);
		scrollPaneLinks.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneLinks.setSize(scrollPaneMethod.getSize());

		
		tableHCDScoreRecords.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableHCDScoreRecords2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		scrollPaneScoreRecords = new JScrollPane(tableHCDScoreRecords);
		scrollPaneScoreRecords.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneScoreRecords.setSize(scrollPaneMethod.getSize());

		scrollPaneScoreRecords2 = new JScrollPane(tableHCDScoreRecords2);
		scrollPaneScoreRecords2.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneScoreRecords2.setSize(scrollPaneMethod.getSize());

		
		scrollPaneSimilarChemicalsTraining = new JScrollPane(tableSimilarChemicalsTraining);
		scrollPaneSimilarChemicalsTraining.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneSimilarChemicalsTraining.setSize((int)(scrollPaneMethod.getWidth()*0.5), scrollPaneMethod.getHeight()-30);

		scrollPaneSimilarChemicalsPrediction = new JScrollPane(tableSimilarChemicalsPrediction);
		scrollPaneSimilarChemicalsPrediction.setLocation(scrollPaneMethod.getLocation()); //shift it over a bit
		scrollPaneSimilarChemicalsPrediction.setSize((int)(scrollPaneMethod.getWidth()*0.5), scrollPaneMethod.getHeight()-30);
		
		
		jtabbedPane=new JTabbedPane();
		jtabbedPane.setSize(scrollPaneMethod.getSize());
		add(jtabbedPane);

		int heightMAE_Table=125;
		int heightPlot=scrollPaneSimilarChemicalsTraining.getHeight()-heightMAE_Table;
		
		panelTraining.setLayout(null);
		panelTraining.add(scrollPaneSimilarChemicalsTraining);
		scrollPaneSimilarChemicalsTraining.setLocation(0, 0);
		jlabelTrainingGraph.setSize(heightPlot-50,heightPlot-50);
		panelTraining.add(jlabelTrainingGraph);
		jlabelTrainingGraph.setLocation(scrollPaneSimilarChemicalsTraining.getWidth(),0);
		
		JPanel panelMAE_Training = new JPanel();
		panelMAE_Training.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Entire set vs. similar chemicals", TitledBorder.LEFT,
				TitledBorder.TOP));
		panelMAE_Training.setSize(heightPlot,heightMAE_Table);
		panelMAE_Training.setLocation(jlabelTrainingGraph.getX(),scrollPaneSimilarChemicalsTraining.getHeight()-panelMAE_Training.getHeight()-30);
		scrollPaneMAE_Training=new JScrollPane(tableMAE_Training);
		scrollPaneMAE_Training.setPreferredSize(new Dimension(panelMAE_Training.getWidth()-10,panelMAE_Training.getHeight()-30));
		panelTraining.add(panelMAE_Training);
		panelMAE_Training.add(scrollPaneMAE_Training);
				
		
		panelPrediction.setLayout(null);
		panelPrediction.add(scrollPaneSimilarChemicalsPrediction);
		scrollPaneSimilarChemicalsPrediction.setLocation(scrollPaneSimilarChemicalsTraining.getLocation());
		panelPrediction.add(jlabelPredictionGraph);
		jlabelPredictionGraph.setSize(jlabelTrainingGraph.getSize());
		jlabelPredictionGraph.setLocation(jlabelTrainingGraph.getLocation());

		JPanel panelMAE_Prediction = new JPanel();
		panelMAE_Prediction.setBorder(panelMAE_Training.getBorder());
		panelMAE_Prediction.setSize(panelMAE_Training.getSize());
		panelMAE_Prediction.setLocation(panelMAE_Training.getLocation());
		scrollPaneMAE_Prediction=new JScrollPane(tableMAE_Prediction);
		scrollPaneMAE_Prediction.setPreferredSize(scrollPaneMAE_Training.getPreferredSize());
		panelPrediction.add(panelMAE_Prediction);
		panelMAE_Prediction.add(scrollPaneMAE_Prediction);

		
		tableDescriptors.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tableHCD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableHCDScoreRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableHCDScoreRecords2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableLinks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		tableMethod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableAllMethods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableDescriptors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void initTableModel(String endpoint,String method,boolean isBatch) {
				
		jtabbedPane.removeAll();
		jtabbedPane.add("Results", scrollPaneMethod);

		String [] colNames=MyTableModel.getColNames(endpoint, method);
		MyTableModel tableModel=new MyTableModel(colNames);
		tableModel.setupTable(tableMethod);

		if(method.contentEquals(TESTConstants.ChoiceConsensus)) {
			jtabbedPane.add("Individual methods", scrollPaneAllMethods);		
			String [] colNames2=MyTableModelAllQSARMethods.getColumnNames(endpoint, method);
			MyTableModelAllQSARMethods tableModelAll=new MyTableModelAllQSARMethods(colNames2);
			tableModelAll.setupTable(tableAllMethods);			
		} 
		
		if(!isBatch) {
			String [] colNames2=MyTableModelSimilarChemical.getColumnNames();//TODO add units

			jtabbedPane.add("Predictions for similar test chemicals", panelPrediction);
			MyTableModelSimilarChemical tableModelSC_test=new MyTableModelSimilarChemical(colNames2);
			tableModelSC_test.setupTable(tableSimilarChemicalsPrediction);

			MyTableModelMAE tableModelMAE_Prediction=new MyTableModelMAE(MyTableModelMAE.getColumnNames());
			tableModelMAE_Prediction.setupTable(tableMAE_Prediction);
			
			jtabbedPane.add("Predictions for similar training chemicals", panelTraining);
			MyTableModelSimilarChemical tableModelSC_training=new MyTableModelSimilarChemical(colNames2);
			tableModelSC_training.setupTable(tableSimilarChemicalsTraining);			

			MyTableModelMAE tableModelMAE_Training=new MyTableModelMAE(MyTableModelMAE.getColumnNames());
			tableModelMAE_Training.setupTable(tableMAE_Training);

		
		}

	}
	
	public void initTableModelDescriptors() {
		
		jtabbedPane.removeAll();

		jtabbedPane.add("Descriptors", scrollPaneDescriptors);		
		String [] colNamesDescriptors=MyTableModelDescriptors.getColumnNames();
		
		//Use invokeLater because so many columns you get error because it takes too long:
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	MyTableModelDescriptors tableModelDescriptors=new MyTableModelDescriptors(colNamesDescriptors);
	    		tableModelDescriptors.setupTable(tableDescriptors);
	        }
	    });

	}
	
	public void initTableModelHCD() {
		jtabbedPane.removeAll();
		jtabbedPane.add("Final Scores", scrollPaneFinalScores);				
		jtabbedPane.add("Score Records", scrollPaneScoreRecords);	
		
		if (tableHCDScoreRecords.getModel() instanceof DefaultTableModel) {
			MyTableModelHCD_ScoreRecords tableModelSR=new MyTableModelHCD_ScoreRecords();
			tableModelSR.setupTable(tableHCDScoreRecords);

			tableHCDScoreRecords.addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        if (evt.getClickCount()==2) {
			        	int row = tableHCDScoreRecords.rowAtPoint(evt.getPoint());			        
			        	String link=tableModelSR.getLink(row);
//			        	System.out.println("link="+link);
			        	if(link==null || link.trim().isEmpty()) return;
			        	URI uri=URI.create(link);
			        	MyBrowserLauncher.launch(uri);
			        }
			    }
			});
		} else {
			MyTableModelHCD_ScoreRecords model=(MyTableModelHCD_ScoreRecords) tableHCDScoreRecords.getModel();
			model.clear();
		}
		
		
		if (TESTApplication.forMDH) {
			jtabbedPane.add("Links", scrollPaneLinks);
			
			if (tableLinks.getModel() instanceof DefaultTableModel) {
				MyTableModelLinks modelLinks=new MyTableModelLinks();
				modelLinks.setupTable(tableLinks);
				
				tableLinks.addMouseListener(new java.awt.event.MouseAdapter() {
				    @Override
				    public void mouseClicked(java.awt.event.MouseEvent evt) {
				        if (evt.getClickCount()==2) {
				        	
				        	int row = tableLinks.rowAtPoint(evt.getPoint());			        
//				        	System.out.println(modelLinks.getRowCount()+"\t"+row);
				        	
				        	RecordLink rl=modelLinks.getLink(row);
//				        	System.out.println(rl.URL);
				        	URI uri=URI.create(rl.URL);
				        	MyBrowserLauncher.launch(uri);
				        }
				    }
				});
			} else {
				MyTableModelLinks modelLinks=(MyTableModelLinks) tableLinks.getModel();
				modelLinks.clear();
			}
		}
		
		if (tableHCD.getModel() instanceof DefaultTableModel) {
			MyTableModelHCD modelHCD=new MyTableModelHCD();
			modelHCD.setupTable(tableHCD);
			tableHCD.getTableHeader().setPreferredSize(new Dimension(scrollPaneFinalScores.getWidth(), 250));
			
			tableHCD.addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = tableHCD.rowAtPoint(evt.getPoint());
			        int col = tableHCD.columnAtPoint(evt.getPoint());
			        		        
			        if (col>=2) {
			        	if (modelHCD.getChemical(row)==null) return;
			        	
				        Chemical chemical=modelHCD.getChemical(row);
				        String scoreName=modelHCD.getColumnName(col).trim();
//			        	System.out.println(jtabbedPane.getComponentCount());			        	
			        	Score score=chemical.getScore(scoreName);			        	
			        	if (score.records.size()==0) return;
			        	
			        	if (tableHCDScoreRecords2.getModel() instanceof DefaultTableModel) {
//				        	if(jtabbedPane.getComponentCount()==4)
				    		jtabbedPane.add("Score Records "+scoreName+" "+chemical.CAS, scrollPaneScoreRecords2);				
				    		MyTableModelHCD_ScoreRecords tableModelSR2=new MyTableModelHCD_ScoreRecords();
				    		tableModelSR2.setupTable(tableHCDScoreRecords2);
				        	
				        	for (int i=0;i<score.records.size();i++) {
				        		tableModelSR2.addScoreRecord(score.records.get(i));
				        	}
				        	jtabbedPane.setSelectedIndex(jtabbedPane.getComponentCount()-1);
				        	
				    		tableHCDScoreRecords2.addMouseListener(new java.awt.event.MouseAdapter() {
				    		    @Override
				    		    public void mouseClicked(java.awt.event.MouseEvent evt) {
				    		        if (evt.getClickCount()==2) {
				    		        	int row = tableHCDScoreRecords2.rowAtPoint(evt.getPoint());			        
				    		        	String link=tableModelSR2.getLink(row);
//				    		        	System.out.println("link="+link);
				    		        	if(link==null || link.trim().isEmpty()) return;
				    		        	URI uri=URI.create(link);
				    		        	MyBrowserLauncher.launch(uri);
				    		        }
				    		    }
				    		});			        		
			        	} else {
			        		MyTableModelHCD_ScoreRecords tableModelSR2=(MyTableModelHCD_ScoreRecords) tableHCDScoreRecords2.getModel();
							tableModelSR2.clear();
							jtabbedPane.add("Score Records "+scoreName+" "+chemical.CAS, scrollPaneScoreRecords2);				
				        	for (int i=0;i<score.records.size();i++) {
				        		tableModelSR2.addScoreRecord(score.records.get(i));
				        	}
				        	jtabbedPane.setSelectedIndex(jtabbedPane.getComponentCount()-1);
			        	}
			        	
			        }
			    }
			});
		} else {
			MyTableModelHCD modelHCD=(MyTableModelHCD) tableHCD.getModel();
			modelHCD.clear();
		}
		
		
	}

	
	private void saveToExcel() {
		
		String filename=null;
		String tabName0=jtabbedPane.getTitleAt(0);

		if (tabName0.contentEquals("Descriptors")) {
			filename = "Descriptors.xlsx";				
		} else if (tabName0.contentEquals("Final Scores")) {
			filename = "HCD Results.xlsx";
		} else if (tabName0.contentEquals("Results")) {
			filename = f.endpoint.replace(" ", "_") + "_" + f.method + ".xlsx";
		} else {
			JOptionPane.showMessageDialog(f, "Invalid selection");
			return;
		}
		
		String filepath=f.as.getOutputFolderPath()+File.separator+filename;		
		File of=new File(filepath);
		
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
			
			XSSFWorkbook workbook = new XSSFWorkbook();
			
			if (tabName0.contentEquals("Descriptors")) {				
				writeResultsToExcel(workbook,tableDescriptors.getModel(),"Descriptors");
			} else if (tabName0.contentEquals("Final Scores")) {
				MyTableModelHCD model=(MyTableModelHCD)tableHCD.getModel();
				Chemicals chemicals=model.getChemicals();				

				TableGeneratorExcel tgExcel = new TableGeneratorExcel();
				tgExcel.writeFinalScoresToWorkbookSimple(chemicals, workbook);
		        tgExcel.writeScoreRecordsToWorkbook(chemicals,workbook);
		        
		        MyTableModelLinks modelLinks=(MyTableModelLinks)tableLinks.getModel();
		        tgExcel.writeLinksSheet(RecordLink.fieldNames,"Links", workbook, modelLinks.getRecords());
		        
			} else if (tabName0.contentEquals("Results")) {
				filename = f.endpoint.replace(" ", "_") + "_" + f.method + ".xlsx";
				writeResultsToExcel(workbook,tableMethod.getModel(),f.method);
				if (f.method.contentEquals(TESTConstants.ChoiceConsensus))
					writeResultsToExcel(workbook,tableAllMethods.getModel(),"Individual methods");
			} else {
				JOptionPane.showMessageDialog(f, "Invalid selection");
				return;
			}

			writeExcelFile(inFile, workbook);//write to file
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
	
	
	
	public void writeExcelFile(File inFile,XSSFWorkbook workbook) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(new File(inFile.getAbsolutePath()));
		workbook.write(out);
		out.close();
	}


	
	private void writeResultsToExcel (XSSFWorkbook workbook,TableModel tableModel,String tabName) {
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
	
	
	public void writeResultsToText (File file,TableModel model) {
		try {
			
			FileWriter fw=new FileWriter(file);
			
			String header="";
			for (int col=0;col<model.getColumnCount();col++) {				
				String colName=model.getColumnName(col);
				colName=colName.replace("\n", "_");
				header+="\""+colName+"\"";				
				if (col<model.getColumnCount()-1) header+=",";
				
			}
			fw.write(header+"\r\n");
			fw.flush();
									
//			System.out.println("rowCount="+tableModel.getRowCount());
						
			for (int row=0;row<model.getRowCount();row++) {								
				String Line="";
				
				long t1=System.currentTimeMillis();
				for (int col=0;col<model.getColumnCount();col++) {
					String val=model.getValueAt(row, col)+"";
					Line+="\""+val+"\"";

					if (col<model.getColumnCount()-1) Line+=",";
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
							imagePath=PredictToxicityWebPageCreator.webImagePathByCID + tpv.gsid;//TODO fix to use CID
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
	
	
	public void setDefaultCursorAllTables() {		
		Cursor c=Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);		
		tableMethod.getTableHeader().setCursor(c);
		tableAllMethods.getTableHeader().setCursor(c);
		tableDescriptors.getTableHeader().setCursor(c);
		tableHCD.getTableHeader().setCursor(c);
		tableHCDScoreRecords.getTableHeader().setCursor(c);
		tableHCDScoreRecords2.getTableHeader().setCursor(c);
		tableLinks.getTableHeader().setCursor(c);
		tableMAE_Training.getTableHeader().setCursor(c);
		tableMAE_Prediction.getTableHeader().setCursor(c);
		tableSimilarChemicalsTraining.getTableHeader().setCursor(c);
		tableSimilarChemicalsPrediction.getTableHeader().setCursor(c);
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
			} else if (ac.equals("jbViewReport")) {
				viewReport();
			}
		}

	}

	
	private void viewReport() {

		String CAS=f.panelSingleStructureDatabaseSearch.jtfCAS.getText();
		String endpoint=(String)f.panelCalculationOptions.panelOptionsEndpointMethod.jcbEndpoint.getSelectedItem();
		String method=(String)f.panelCalculationOptions.panelOptionsEndpointMethod.jcbMethod.getSelectedItem();
		
		
		File f1=new File(f.as.getOutputFolderPath());
		File f2=new File(f1.getAbsolutePath()+File.separator+"ToxRuns");
		File f3=new File(f2.getAbsolutePath()+File.separator+"ToxRun_"+CAS);
		
		File f5=null;
		
		if (!endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
			File f4=new File(f3.getAbsolutePath()+File.separator+endpoint);
			String filename=endpoint+"_"+method+"_"+CAS+".html";
			filename=filename.replace(" ", "_");
			f5=new File(f4.getAbsolutePath()+File.separator+filename);
		} else {
			File f4=new File(f3.getAbsolutePath()+File.separator+"StructureData");
			String filename="DescriptorData_"+CAS+".html";
			filename=filename.replace(" ", "_");
			f5=new File(f4.getAbsolutePath()+File.separator+filename);
		}
		
		if (!f5.exists())  {
			JOptionPane.showMessageDialog(f, f5.getAbsolutePath()+" does not exist");
			return;
		}
		
		try {
			MyBrowserLauncher.launch(f5.toURI());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		String tabNameSelected=jtabbedPane.getTitleAt(jtabbedPane.getSelectedIndex());
		
		TableModel model=null;
		
//		if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
//			writeResultsToText(inFile,(MyTableModelDescriptors)this.tableDescriptors.getModel());
//		} else {
//			if (jtabbedPane.getSelectedIndex()==0) {
//				writeResultsToText(inFile,(MyTableModel)this.tableMethod.getModel());	
//			} else if (jtabbedPane.getSelectedIndex()==1) {
//				writeResultsToText(inFile,(MyTableModelAllQSARMethods)this.tableAllMethods.getModel());
//			}
////				if (f.method.contentEquals(TESTConstants.ChoiceConsensus))
////					writeResultsToExcel(workbook,(MyTableModelAllQSARMethods)this.table2.getModel(),"Individual methods");
//		}
				
		
		if (tabNameSelected.contentEquals("Descriptors")) {
			filename = "Descriptors.csv";
			model=tableDescriptors.getModel();			
		} else if (tabNameSelected.contentEquals("Final Scores")) {
			filename = "HCD Final Scores.csv";
			model=tableHCD.getModel();
		} else if (tabNameSelected.contentEquals("Score Records")) {
			filename = tabNameSelected+".csv";
			model=tableHCDScoreRecords.getModel();
		} else if (tabNameSelected.contains("Score Records")) {
			filename = tabNameSelected+".csv";
			model=tableHCDScoreRecords2.getModel();
		} else if (tabNameSelected.contentEquals("Results")) {
			filename = f.endpoint.replace(" ", "_") + "_" + f.method + ".csv";
			model=tableMethod.getModel();
		} else if (tabNameSelected.contentEquals("Individual methods")) {
			filename = f.endpoint.replace(" ", "_") + "_AllMethods.csv";
			model=tableAllMethods.getModel();
		} else if (tabNameSelected.contains("Predictions for similar training chemicals")) {
			filename = tabNameSelected+".csv";
			model=tableSimilarChemicalsTraining.getModel();
		} else if (tabNameSelected.contains("Predictions for similar test chemicals")) {
			filename = tabNameSelected+".csv";
			model=tableSimilarChemicalsPrediction.getModel();
		} else if (tabNameSelected.contains("Links")) {
			filename = tabNameSelected+".csv";
			model=tableLinks.getModel();

		} else {
			JOptionPane.showMessageDialog(f, "Invalid selection");
			return;
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
			writeResultsToText(inFile,model);
			this.setCursor(Utilities.defaultCursor);
			            
//            JOptionPane.showMessageDialog(f,
//            		"The file was saved successfully at\n"+ inFile.getAbsolutePath());
            		
           MyBrowserLauncher.launch(inFile.toURI());


		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}
		
	}
	
	public List<Component> getAllComponents(Container container) {
	    Component[] components = container.getComponents();
	    List <Component> result = new ArrayList<Component>();
	    for (Component component : components) {
	        result.add(component);
	        if (component instanceof Container) {
	            result.addAll(getAllComponents((Container) component));
	        }
	    }
	    return result;
	}

//	public void writeToTextFile(File inFile,String endpoint) {
//		if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
//			writeResultsToText(inFile,(MyTableModelDescriptors)this.tableDescriptors.getModel());
//		} else {
//			if (jtabbedPane.getSelectedIndex()==0) {
//				writeResultsToText(inFile,(MyTableModel)this.tableMethod.getModel());	
//			} else if (jtabbedPane.getSelectedIndex()==1) {
//				writeResultsToText(inFile,(MyTableModelAllQSARMethods)this.tableAllMethods.getModel());
//			}
////				if (f.method.contentEquals(TESTConstants.ChoiceConsensus))
////					writeResultsToExcel(workbook,(MyTableModelAllQSARMethods)this.table2.getModel(),"Individual methods");
//		}
//	}

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
	public void addChemical(Chemical chemical) {
		MyTableModelHCD tableModel=(MyTableModelHCD)tableHCD.getModel();
		tableModel.addChemical(chemical);
		
		MyTableModelHCD_ScoreRecords tableModelScoreRecords=(MyTableModelHCD_ScoreRecords)tableHCDScoreRecords.getModel();
		
		for (int i=0;i<chemical.scores.size();i++) {
			Score scorei=chemical.scores.get(i);
			for (int j=0;j<scorei.records.size();j++) {
				tableModelScoreRecords.addScoreRecord(scorei.records.get(j));		
			}
		}
		
		if (TESTApplication.forMDH) {
			MyTableModelLinks tableModelLinks=(MyTableModelLinks)tableLinks.getModel();
			for (int i=0;i<chemical.links.size();i++) {
				tableModelLinks.addLink(chemical.links.get(i));		
			}
		}
		
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
	 * @param newPredictions
	 */
	public void addResultsSimilarChemicals(TESTPredictedValue tpv,String set) {
		MyTableModelSimilarChemical tableModel=null;
		MyTableModelMAE tableModelMAE=null;
		Vector<SimilarChemical> vecSimilarChemicals=null;
		JLabel labelGraph=null;

		String chart=null;
		SimilarChemicals similarChemicals=null;
		
		if (set.equals("Training")) {
			tableModel=(MyTableModelSimilarChemical)tableSimilarChemicalsTraining.getModel();
			tableModelMAE=(MyTableModelMAE)tableMAE_Training.getModel();
			similarChemicals=tpv.predictionResults.getSimilarChemicals().get(1);
			labelGraph=jlabelTrainingGraph;
		} else if (set.equals("Prediction")) { 
			tableModel=(MyTableModelSimilarChemical)tableSimilarChemicalsPrediction.getModel();
			tableModelMAE=(MyTableModelMAE)tableMAE_Prediction.getModel();
			similarChemicals=tpv.predictionResults.getSimilarChemicals().get(0);
			labelGraph=jlabelPredictionGraph;
		}
		
		vecSimilarChemicals=similarChemicals.getSimilarChemicalsList();
		
		for (SimilarChemical similarChemical:vecSimilarChemicals) {
			tableModel.addPrediction(similarChemical);
		}
		tableModel.updateRowHeights();
		
		
		if (similarChemicals.getExternalPredChart()==null) {
			labelGraph.setIcon(null);
			tableModelMAE.addResult("No similar chemicals",-9999);
			return;
		}

		chart=similarChemicals.getExternalPredChart().getExternalPredChartImageSrc();

		tableModelMAE.addResult("Entire set",similarChemicals.getExternalPredChart().getMAEEntireTestSet());
		tableModelMAE.addResult("Similarity >= 0.5",similarChemicals.getExternalPredChart().getMAE());

		try {
//			 System.out.println(chart);
			 byte[] imageByte = org.apache.commons.codec.binary.Base64.decodeBase64(chart.substring(chart.indexOf(","),chart.length()).trim()); 
			 ByteArrayInputStream inputStream = new ByteArrayInputStream(imageByte);                         
			 BufferedImage bufImage = ImageIO.read(inputStream);
			 			 
//			 ImageIcon icon = new ImageIcon(bufImage);
			 
			 int size=jlabelTrainingGraph.getHeight();
			 
			Image newimg = bufImage.getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			ImageIcon icon = new ImageIcon(newimg);  // transform it back			 
			labelGraph.setIcon(icon);
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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


	

