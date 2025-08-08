package ToxPredictor.Application.GUI;



import java.awt.Insets;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import ToxPredictor.Utilities.*;



public class PanelCTSOptions extends JPanel {

//	public ButtonGroup bg=new ButtonGroup();
	
//	public JRadioButton jrbHydrolysis=new JRadioButton();
//	public JRadioButton jrbAbioticReduction = new JRadioButton();
//	public JRadioButton jrbHumanMetabolism = new JRadioButton();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5339837204647126348L;

	public JComboBox<String> jcbRoute=null;
	
	public JCheckBox jcbRunCTS=new JCheckBox();
	
	public JButton jbHelpCTS=new JButton("?");
	
	ActionAdapter aa =new ActionAdapter();	
	
	Object gui;
	int inset=20;
	
	MessageWithLink messageWithLink=new MessageWithLink("The <a href=\"https://qed.epacdx.net/cts/\">Chemical Transformation Simulator (CTS)</a> is a "
			+ "web-based tool<br>"
			+ "for predicting environmental and biological transformation pathways<br>"
			+ "and physicochemical properties of organic chemicals");

		
	private void SetupSimpleControls() {
		
		int radialwidth=150;
		int textHeight=20;
		
//		int vspacing=15;
//		int vspacing=(this.getHeight()-4*textHeight-20)/5;
		
//		if (vspacing>20) vspacing=20;
		
//		System.out.println("vspacing="+vspacing);
		
		jcbRunCTS.setText("Run CTS");
		jcbRunCTS.setSize(100, textHeight);
		jcbRunCTS.setLocation(20, 0);
		jcbRunCTS.setActionCommand("jcbRunCTS");
		jcbRunCTS.addActionListener(aa);
		
		Vector<String>routes=new Vector<>();
		
		routes.add("Hydrolysis");
		routes.add("Abiotic Reduction");
		routes.add("Human Metabolism");
		
		jcbRoute=new JComboBox<>(routes);		
		jcbRoute.setSize(radialwidth, textHeight);
		jcbRoute.setLocation(jcbRunCTS.getX()+jcbRunCTS.getWidth()+20, (int)jcbRunCTS.getY());
		jcbRoute.setEnabled(false);
				
		
		jbHelpCTS.setActionCommand("jbHelpCTS");
		jbHelpCTS.addActionListener(aa);
		jbHelpCTS.setMargin(new Insets(0,0,0,0));
		jbHelpCTS.setSize(20, 20);
		jbHelpCTS.setText("?");
		jbHelpCTS.setLocation(jcbRoute.getX()+jcbRoute.getWidth()+20,jcbRoute.getY());

		
		
//		jrbHydrolysis.setText("Hydrolysis");
//		jrbHydrolysis.setSize(radialwidth, textHeight);
//		jrbHydrolysis.setLocation(40, (int)jcbRunCTS.getLocation().getY()+textHeight+vspacing);
//		jrbHydrolysis.setEnabled(false);
//
//		jrbAbioticReduction.setText("Abiotic Reduction");
//		jrbAbioticReduction.setSize(radialwidth, textHeight);
//		jrbAbioticReduction.setLocation(40, (int)jrbHydrolysis.getLocation().getY()+textHeight+vspacing);
//		jrbAbioticReduction.setEnabled(false);
//		
//		jrbHumanMetabolism.setText("Human Metabolism");
//		jrbHumanMetabolism.setSize(radialwidth, textHeight);
//		jrbHumanMetabolism.setLocation(40, (int)jrbAbioticReduction.getLocation().getY()+textHeight+vspacing);
//		jrbHumanMetabolism.setEnabled(false);
//		
//		add(this.jrbHydrolysis);
//		add(this.jrbAbioticReduction);
//		add(this.jrbHumanMetabolism);
		
		add(this.jcbRoute);
		add(jcbRunCTS);
		add(jbHelpCTS);
		
		this.jcbRoute.setSelectedItem("Hydrolysis");			
		

		
	}
	
	void init(int width,int height,Object gui) {
		this.gui=gui;
		
		this.setSize(width,height);
//		this.setBorder(BorderFactory.createLineBorder(Color.black));
		
//		TitledBorder border = new TitledBorder("Chemical Transformation Simulator (CTS) Options");
//	    border.setTitleJustification(TitledBorder.LEFT);
//	    border.setTitlePosition(TitledBorder.TOP);
//	    this.setBorder(border);

		if (gui instanceof TESTApplication) {
			this.setSize(width, height);	
		}
		
		Utilities.SetFonts(this);
//		this.getContentPane().setLayout(null);

		this.setLayout(null);
				
		this.SetupSimpleControls();
		
		
//		bg.add(jrbHydrolysis);
//		bg.add(jrbAbioticReduction);
//		bg.add(jrbHumanMetabolism);
		
		
		
		this.setVisible(false);
		
	}

			
	
	class ActionAdapter implements java.awt.event.ActionListener {
		  public void actionPerformed(ActionEvent e) {
			  String ac=e.getActionCommand();
			  
				if (ac.equals("jcbRunCTS")) {
					jcbRunCTS_clicked();
				} else if (ac.contentEquals("jbHelpCTS")) {
					JOptionPane.showMessageDialog((TESTApplication)gui, messageWithLink,"CTS", JOptionPane.INFORMATION_MESSAGE);
				}
		  }

		private void jcbRunCTS_clicked() {
			// TODO Auto-generated method stub
			jcbRoute.setEnabled(jcbRunCTS.isSelected());
			
//			jrbHydrolysis.setEnabled(jcbRunCTS.isSelected());
//			jrbAbioticReduction.setEnabled(jcbRunCTS.isSelected());
//			jrbHumanMetabolism.setEnabled(jcbRunCTS.isSelected());
		}
  }

}
