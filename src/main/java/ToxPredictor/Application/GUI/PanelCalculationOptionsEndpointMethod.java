package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Utilities.Utilities;


public class PanelCalculationOptionsEndpointMethod extends JPanel {
	private JLabel jlEndpoint = new JLabel();
	public JComboBox jcbEndpoint = null;
	private JButton jbAboutEndpoints=new JButton();
	
	private JLabel jlMethod = new JLabel();
	public JComboBox jcbMethod = null;
	private JButton jbAboutQSARMethods=new JButton();
	private JButton jbAboutRelaxFragmentConstraint=new JButton();

	public JCheckBox jcbRelaxFragmentConstraint = new JCheckBox();
	
	String SEPARATOR="SEPARATOR";

	Object gui;
	
	actionAdapter aa =new actionAdapter();	
	
	
	

	public void init(int width,int height,Object gui) {
		this.gui=gui;
		
		this.setSize(width,height);		
		Utilities.SetFonts(this);
		this.setLayout(null);		
		this.SetupSimpleControls();
		
//		TitledBorder border = new TitledBorder("Prediction Options");
//	    border.setTitleJustification(TitledBorder.LEFT);
//	    border.setTitlePosition(TitledBorder.TOP);
//	    this.setBorder(border);
		
		
	}
	
	private void SetupSimpleControls() {

			TESTApplication f=(TESTApplication)gui;
			int heightControls=20;
			int widthText=60;
			int widthComboBox=200;

//			int inset=20;
			int num=4;
			int insetVertical=(this.getHeight()-(num-1)*heightControls)/num;
			int insetHorizontal=20;
			
//			System.out.println("inset for endpoint/method="+inset);

			jlEndpoint.setText("Endpoint:");
			jlEndpoint.setSize(widthText,heightControls);
			jlEndpoint.setLocation(insetHorizontal, insetVertical);
			jlEndpoint.setHorizontalAlignment(SwingConstants.RIGHT);
			
			java.util.Vector vecEndpoints = setUpEndpoints(f);
			jcbEndpoint = new JComboBox(vecEndpoints);
			jcbEndpoint.setRenderer(new ComboBoxRenderer());
			jcbEndpoint.addActionListener(new BlockComboListener(jcbEndpoint));
			
			jcbEndpoint.setLocation(2*insetHorizontal+jlEndpoint.getWidth(), jlEndpoint.getY());
			jcbEndpoint.addActionListener(aa);
			jcbEndpoint.setActionCommand("jcbEndpoint");
			jcbEndpoint.setSize(widthComboBox,heightControls);		
			
			jbAboutEndpoints.setMargin(new Insets(0,0,0,0));
			jbAboutEndpoints.setSize(heightControls, heightControls);
			jbAboutEndpoints.setText("?");
			jbAboutEndpoints.setLocation(jcbEndpoint.getX()+jcbEndpoint.getWidth()+insetHorizontal,jlEndpoint.getY());
			jbAboutEndpoints.setActionCommand("jbAboutEndpoints");
			jbAboutEndpoints.addActionListener(aa);

			jlMethod.setText("Method:");
			jlMethod.setSize(60,heightControls);
			jlMethod.setHorizontalAlignment(SwingConstants.RIGHT);
			jlMethod.setLocation(insetHorizontal, jlEndpoint.getY()+jlEndpoint.getHeight()+insetVertical);
			
			Vector<String>methodsClone=(Vector<String>)f.Methods.clone();//clone it because when remove items from jcbMethod it kills the methods vector!
			jcbMethod=new JComboBox(methodsClone);
			jcbMethod.setSize(widthComboBox,heightControls);
			jcbMethod.setLocation(jlMethod.getX()+jlMethod.getWidth()+insetHorizontal, jlMethod.getY());
			jcbMethod.setSelectedItem(f.defaultMethod);		
			
			jbAboutQSARMethods.setActionCommand("jbAboutQSARMethods");
			jbAboutQSARMethods.addActionListener(aa);
			jbAboutQSARMethods.setMargin(new Insets(0,0,0,0));
			jbAboutQSARMethods.setSize(heightControls, heightControls);
			jbAboutQSARMethods.setText("?");
			jbAboutQSARMethods.setLocation(jcbMethod.getX()+jcbMethod.getWidth()+insetHorizontal,jlMethod.getY());
			

			jcbRelaxFragmentConstraint.setSize(200, 20);
			jcbRelaxFragmentConstraint.setLocation(insetHorizontal, jlMethod.getY()+jlMethod.getHeight()+insetVertical);
			jcbRelaxFragmentConstraint.setText("Relax fragment constraint");
			jcbRelaxFragmentConstraint.setSelected(f.as.isRelaxFragmentConstraint());

			jbAboutRelaxFragmentConstraint.setActionCommand("jbAboutRelaxFragmentConstraint");
			jbAboutRelaxFragmentConstraint.addActionListener(aa);
			jbAboutRelaxFragmentConstraint.setMargin(new Insets(0,0,0,0));
			jbAboutRelaxFragmentConstraint.setSize(heightControls, heightControls);
			jbAboutRelaxFragmentConstraint.setText("?");
			jbAboutRelaxFragmentConstraint.setLocation(jcbRelaxFragmentConstraint.getX()+jcbRelaxFragmentConstraint.getWidth()+insetHorizontal,jcbRelaxFragmentConstraint.getY());
			
			
			
			add(jlEndpoint);
			add(jcbEndpoint);
			add(jbAboutEndpoints);
			add(this.jlMethod);
			add(this.jcbMethod);
			add(this.jbAboutQSARMethods);			
			add(jcbRelaxFragmentConstraint);
			add(jbAboutRelaxFragmentConstraint);

			
			if (f.as.getEndpoint()!=null) {
				jcbEndpoint.setSelectedItem(f.as.getEndpoint());
//				System.out.println(f.as.getEndpoint());
			} else {
				jcbEndpoint.setSelectedItem(TESTConstants.ChoiceFHM_LC50);
			}
			
			if (f.as.getMethod()!=null) {
				jcbMethod.setSelectedItem(f.as.getMethod());
			} else {
				jcbMethod.setSelectedItem(f.defaultMethod);
			}
	}
	
	
	/**
	 * Used to allow having a separator in the endpoints combobox
	 * @author TMARTI02
	 *
	 */
	  class BlockComboListener implements ActionListener {
	    JComboBox combo;

	    Object currentItem;

	    BlockComboListener(JComboBox combo) {
	      this.combo = combo;
	      combo.setSelectedIndex(0);
	      currentItem = combo.getSelectedItem();
	    }

	    public void actionPerformed(ActionEvent e) {
	      String tempItem = (String) combo.getSelectedItem();
	      if (SEPARATOR.equals(tempItem)) {
	        combo.setSelectedItem(currentItem);
	      } else {
	        currentItem = tempItem;
	      }
	    }
	  }
	
	/**
	 * Used to render the endpoints combobox
	 * @author TMARTI02
	 *
	 */
	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	    JSeparator separator;

	    public ComboBoxRenderer() {
	      setOpaque(true);
	      setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
	      separator = new JSeparator(JSeparator.HORIZONTAL);
	      separator.setBorder(new BottomThickLineBorder());

	    }
	    public Component getListCellRendererComponent(JList list, Object value,
	        int index, boolean isSelected, boolean cellHasFocus) {

	    	String str = (value == null) ? "" : value.toString();
	      if (SEPARATOR.equals(str)) {
	        return separator;
	      }
	      if (isSelected) {
	        setBackground(list.getSelectionBackground());
	        setForeground(list.getSelectionForeground());
	      } else {
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	      }
	      setFont(list.getFont());
	      setText(str);
	      return this;
	    }
	  }
	
	class BottomThickLineBorder extends javax.swing.border.AbstractBorder
	   {
	      private int m_thickness;
	      private Color m_color;

	      BottomThickLineBorder()
	      {
	         this(1, Color.black);
	      }

	      BottomThickLineBorder(int thickness, Color color)
	      {
	         m_thickness = thickness;
	         m_color = color;
	      }

	      public void paintBorder(Component c, Graphics g,
	            int x, int y, int width, int height)
	      {
	         Graphics copy = g.create();
	         if (copy != null)
	         {
	            try
	            {
	               copy.translate(x, y);
	               copy.setColor(m_color);
	               copy.fillRect(0, height - m_thickness, width - 1, height - 1);
	            }
	            finally
	            {
	               copy.dispose();
	            }
	         }
	      }

	      public boolean isBorderOpaque()
	      {
	         return true;
	      }
	      public Insets getBorderInsets(Component c, Insets i)
	      {
	         return new Insets(0, 0, m_thickness, 0);
	      }
	   }
	
	
	private java.util.Vector setUpEndpoints(TESTApplication f) {
		java.util.Vector vecEndpoints = new java.util.Vector();
		
		if (f.includeToxicityEndpoints) {
			for (int i=0;i<f.endPointsToxicity.size();i++) {
				vecEndpoints.add(f.endPointsToxicity.get(i));
			}
			vecEndpoints.add(SEPARATOR);
		}

		if (f.includePhysicalPropertyEndpoints) {
			for (int i=0;i<f.endPointsPhysicalProperty.size();i++) {
				vecEndpoints.add(f.endPointsPhysicalProperty.get(i));
			}
			vecEndpoints.add(SEPARATOR);
		}
		vecEndpoints.add(TESTConstants.ChoiceDescriptors);
		return vecEndpoints;
	}
	
	/**
	 * This method handles when the endpoint is changed via jcbEndpoint.
	 * This method sets the available QSAR methods for each endpoint.
	 * 
	 */
	private void jcbEndpoint_actionPerformed() {
		TESTApplication f=(TESTApplication)gui;
		
		f.getEndpointInfo();


		String oldMethod=(String)jcbMethod.getSelectedItem();
		
		
//		System.out.println(f.isBinaryEndpoint);

		
//		if (f.endpoint.equals(TESTConstants.ChoiceDescriptors)) {
//			jcbCreateDetailedReport.setEnabled(false);
//			jcbCreateReports.setEnabled(false);
//		} else {
//			jcbCreateDetailedReport.setEnabled(true);
//			jcbCreateReports.setEnabled(true);
//		}
		
		TESTApplication ta=(TESTApplication)gui;
		JCheckBox jcbCreateDetailedReport=ta.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports;
		JCheckBox jcbCreateReports=ta.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports;
		
		
		if (f.endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			jcbMethod.removeAllItems();
			jcbMethod.addItem("N/A");
//			jcbCreateDetailedReport.setVisible(false);
			jcbRelaxFragmentConstraint.setEnabled(false);	
			return;
		} else {
//			jcbCreateDetailedReport.setVisible(true);
			
			if (f.isBinaryEndpoint) {
				jcbRelaxFragmentConstraint.setEnabled(false);			
//				if (f.isBinaryEndpoint) jcbRelaxFragmentConstraint.setSelected(true);
			} else {
				jcbRelaxFragmentConstraint.setEnabled(true);
			}
			
			if (!jcbCreateReports.isSelected()) jcbCreateDetailedReport.setSelected(false);
			
		}

		jcbMethod.removeAllItems();

		//add all methods:
		for (int i=0;i<f.Methods.size();i++) {
			jcbMethod.addItem(f.Methods.get(i));
		}

		if (!TESTConstants.haveSingleModelMethod(f.endpoint)) {
			jcbMethod.removeItem(TESTConstants.ChoiceSingleModelMethod);
		}

		if (!TESTConstants.haveGroupContributionMethod(f.endpoint)) {
			jcbMethod.removeItem(TESTConstants.ChoiceGroupContributionMethod);
		}				

		if (f.endpoint.equals(TESTConstants.ChoiceReproTox)) jcbMethod.addItem(TESTConstants.ChoiceRandomForrestCaesar);

		if (f.includeLDA) 
			if (f.endpoint.equals(TESTConstants.ChoiceFHM_LC50)) { 
				jcbMethod.addItem(TESTConstants.ChoiceLDA);
			}


		boolean isOldMethodAvailable=false;

		for (int i=0;i<jcbMethod.getItemCount();i++) {
			String method=(String)jcbMethod.getItemAt(i);
			//		System.out.println(oldMethod+"\t"+method);
			if (oldMethod.equals(method)) {
				isOldMethodAvailable=true;
				break;
			}
		}

		if (isOldMethodAvailable)jcbMethod.setSelectedItem(oldMethod);
		else jcbMethod.setSelectedItem(f.defaultMethod);//set to consensus by default

		



	}
	
	class actionAdapter implements java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			String ac=e.getActionCommand();
			
			TESTApplication f=(TESTApplication)gui;			
			JCheckBox jcbCreateDetailedReport=f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports;
			JCheckBox jcbCreateReports=f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports;

		
			if (e.getActionCommand().equals("jbAboutEndpoints")) {
				f.myfraAbout.setTitle("Endpoints in TEST");
				f.myfraAbout.openURL("About endpoints.html");
				f.myfraAbout.setVisible(true);
			} else if (e.getActionCommand().equals("jcbEndpoint")) {
				jcbEndpoint_actionPerformed();
			} else if (e.getActionCommand().equals("jbAboutQSARMethods")) {
				f.myfraAbout.setTitle("QSAR methods in TEST");
				f.myfraAbout.openURL("About QSAR methods.html");
				f.myfraAbout.setVisible(true);
			} else if (e.getActionCommand().equals("jbAboutRelaxFragmentConstraint")) {
				String message="The fragment constraint, requires that the compounds in the\n"
						      + "model have at least one example of each of the fragments\n"
						      + "contained in the test chemical.\n\n"
						      + "For example, if trying to make a prediction for ethanol,\n"
						      + "the cluster must contain at least one compound with a\n"
						      + "methyl fragment (-CH3 [aliphatic attach]), one compound\n"
						      + "with a methylene fragment (-CH2 [aliphatic attach]), and\n"
						      + "one compound with a hydroxyl fragment (-OH [aliphatic attach])\n\n"
						      + "This constraint is applicable to the hierarchical clustering, \n"
						      + "group contribution, and single model methods for nonbinary endpoints.";
				JOptionPane.showMessageDialog((TESTApplication)gui, message);
				
			}

		}
	}


}
