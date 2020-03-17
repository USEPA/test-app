package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Utilities.Utilities;

public class PanelOutputOptions extends JPanel {
	private JLabel jlOutputFolder=new JLabel("Select output folder:");
	public JTextArea jtfOutputFolder=new JTextArea("");
	public JFileChooser chooser = new JFileChooser();
	JButton jbBrowse=new JButton("Browse...");
	
//	private JLabel jlReportType=new JLabel("Select report type:");
	
	public JCheckBox jcbCreateReports = new JCheckBox();
	public JCheckBox jcbCreateDetailedReports = new JCheckBox();
	JButton jbAboutCreateReports=new JButton();
	JButton jbViewPanelResults=new JButton();

//	public Point locCreateDetailedReport;
	
	Object gui;
	
	actionAdapter aa =new actionAdapter();

	void init(int width,int height,Object gui) {
		this.gui=gui;

		this.setSize(width,height);
//		this.setBorder(BorderFactory.createLineBorder(Color.black));
//		double SF=(double)DisplaySize/(double)ImageSize;
//		this.setResizable(false);		
				
//		TitledBorder border = new TitledBorder("Output Options");
//	    border.setTitleJustification(TitledBorder.LEFT);
//	    border.setTitlePosition(TitledBorder.TOP);
//	    this.setBorder(border);

			
		Utilities.SetFonts(this);
		this.setLayout(null);
		
		this.SetupSimpleControls();
				
//		Border blackline = BorderFactory.createLineBorder(Color.black);
//		this.setBorder(blackline);
	    
	    this.setVisible(true);
	    
		
	}
	
	
	private void setOutputFolder(TESTApplication f) {
		if (f.as.getOutputFolderPath()!=null && new File(f.as.getOutputFolderPath()).exists())
			jtfOutputFolder.setText(f.as.getOutputFolderPath());
		else {
//			jbBrowse.doClick();
//			String of=chooser.getCurrentDirectory().getAbsolutePath();			
//			if (!of.contains("MyToxicity")) {
//				of+=File.separator+"MyToxicity";
//			}			
////			System.out.println(of);
//			File OF=new File(of);
//			OF.mkdirs();
//
//			jtfOutputFolder.setText(of);
		}
	}
	
	private void SetupSimpleControls() {

		TESTApplication f=(TESTApplication)gui;
		int heightControls=20;
		int widthText=60;
		int widthComboBox=175;
		
		int insetHorizontal=20;
		int insetVertical=(int)((this.getHeight()-4*heightControls)/4.0);
		
		jlOutputFolder.setSize(widthComboBox,20);
		jlOutputFolder.setLocation(insetHorizontal,insetVertical);

		
		jbBrowse.setSize(100,2*heightControls);
		jbBrowse.setLocation(getWidth()-jbBrowse.getWidth()-insetHorizontal,jlOutputFolder.getY()+jlOutputFolder.getHeight()+insetVertical);
		jbBrowse.addActionListener(aa);
		jbBrowse.setActionCommand("jbBrowse");
		
		jtfOutputFolder.setSize(getWidth()-3*insetHorizontal-jbBrowse.getWidth(),heightControls*2);
		jtfOutputFolder.setLocation(insetHorizontal,jlOutputFolder.getY()+jlOutputFolder.getHeight()+insetVertical);
		jtfOutputFolder.setEditable(false);
		jtfOutputFolder.setLineWrap(true);
		jtfOutputFolder.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
						
//		jlReportType.setSize(150,20);
//		jlReportType.setLocation(insetHorizontal,jtfOutputFolder.getY()+jtfOutputFolder.getHeight()+insetVertical);		
//		Vector<String>reportOptions=new Vector<>();
//		reportOptions.add(TESTConstants.strReportTypeSummary);
//		reportOptions.add(TESTConstants.strReportTypeWeb);
//		reportOptions.add(TESTConstants.strReportTypeDetailedWeb);
//		reportOptions.add(TESTConstants.strReportTypeExcel);
		
		jcbCreateReports.setText("Create reports");
		jcbCreateReports.setSize(150, 20);
		jcbCreateReports.setLocation(insetHorizontal, jtfOutputFolder.getY()+jtfOutputFolder.getHeight()+insetVertical);
		jcbCreateReports.setSelected(f.as.isCreateReport());		
		jcbCreateReports.setActionCommand("jcbCreateReports");
		jcbCreateReports.addActionListener(aa);
		
		jcbCreateDetailedReports.setSize(widthComboBox, 20);
		jcbCreateDetailedReports.setLocation(jcbCreateReports.getX(), jcbCreateReports.getY());
		jcbCreateDetailedReports.setText("Create detailed reports");
		jcbCreateDetailedReports.setSelected(f.as.isCreateDetailedReport());
		jcbCreateDetailedReports.setActionCommand("jcbCreateDetailedReports");
		jcbCreateDetailedReports.addActionListener(aa);

		jbAboutCreateReports.setActionCommand("jbAboutCreateReports");
		jbAboutCreateReports.addActionListener(aa);
		jbAboutCreateReports.setMargin(new Insets(0,0,0,0));
		jbAboutCreateReports.setSize(heightControls, heightControls);
		jbAboutCreateReports.setText("?");
		jbAboutCreateReports.setLocation(jcbCreateDetailedReports.getX()+jcbCreateDetailedReports.getWidth()+insetHorizontal,jcbCreateReports.getY());

		jbViewPanelResults.setActionCommand("jbViewPanelResults");
		jbViewPanelResults.addActionListener(aa);
		jbViewPanelResults.setMargin(new Insets(0,0,0,0));
		jbViewPanelResults.setSize(100,heightControls);
		jbViewPanelResults.setText("View results");
		jbViewPanelResults.setLocation(getWidth()-jbViewPanelResults.getWidth()-insetHorizontal,jbAboutCreateReports.getY());
		jbViewPanelResults.setEnabled(false);
		
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//messes things up!!!
		chooser.setDialogTitle("Select folder where all results files will be located");
		chooser.setApproveButtonText("Select");

		setOutputFolder(f);
		add(jlOutputFolder);
//		add(jlReportType);
		add(jtfOutputFolder);
		add(jbBrowse);
		add(jcbCreateReports);
		add(jcbCreateDetailedReports);
		add(jbAboutCreateReports);
		add(jbViewPanelResults);


	}
	
	class actionAdapter implements java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			String ac=e.getActionCommand();
			
			TESTApplication f=(TESTApplication)gui;

			if (e.getActionCommand().equals("jbBrowse")) {
				jbBrowse_actionPerformed(e);
			} else if (e.getActionCommand().equals("jcbCreateReports")) {
			} else 	if (e.getActionCommand().equals("jcbCreateDetailedReports")) {

			} else if (e.getActionCommand().contentEquals("jbViewPanelResults")) {
				TESTApplication ta=(TESTApplication)gui;
				ta.panelResults.setVisible(true);
			} else if (e.getActionCommand().contentEquals("jbAboutCreateReports")) {
				
				String message="";
				
				if (f.panelBatch.isVisible()) {
					message="Select this option to be able to save reports (slower)\n\n";				
				} else {
					message="Select this option to create detailed reports (creates additional web pages)\n\n";
				}
				JOptionPane.showMessageDialog((TESTApplication)gui, message,"Report option",JOptionPane.INFORMATION_MESSAGE);
				
			}


		}
	}
	
	private void jbBrowse_actionPerformed(ActionEvent e) {
		TESTApplication f=(TESTApplication)gui;

		try {
			chooser.setCurrentDirectory(new File(jtfOutputFolder.getText()));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		int returnVal = chooser.showSaveDialog(f);
		
//		System.out.println(returnVal);

		if (returnVal != JFileChooser.APPROVE_OPTION) return;
		if (chooser.getSelectedFile().getAbsolutePath()==null) return;


		String outputFolderPath=chooser.getSelectedFile().getAbsolutePath();
		
		if (!outputFolderPath.contains("MyToxicity")) {
			outputFolderPath+=File.separator+"MyToxicity";
		}
		
//		System.out.println(outputFolderPath);

		File inFile = new File(outputFolderPath);
		
		if (!inFile.exists()) {
			int Result = JOptionPane.showConfirmDialog(f, "\""+outputFolderPath+"\""						
					+ " does not exist do you wish to create it?",
					"Create folder?", JOptionPane.OK_CANCEL_OPTION);
			
			if (Result == JOptionPane.OK_OPTION) {
				inFile.mkdirs();
			} else {
				return;
			}
		} 

		jtfOutputFolder.setText(outputFolderPath);
		f.as.setOutputFolderPath(outputFolderPath);
		f.as.saveSettingsToFile();

		
	}

}
