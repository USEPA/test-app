package ToxPredictor.Application.GUI;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import ToxPredictor.Utilities.*;

public class PanelCalculationOptions extends JPanel {

	
	PanelCalculationOptionsEndpointMethod panelOptionsEndpointMethod=new PanelCalculationOptionsEndpointMethod();
	PanelCTSOptions panelCTSOptions=new PanelCTSOptions();
	PanelOutputOptions panelOutputOptions=new PanelOutputOptions();
	
//	public static final boolean runCTS = false;//TODO move to checkbox
//	keyAdapter ka=new keyAdapter();
	
	Object gui;
	
    public static void main(String[] args) {
   	 	PanelCalculationOptions f=new PanelCalculationOptions();
    	f.setVisible(true);
	}

	void init(int width,int height,Object gui) {
		this.gui=gui;
		
		this.setSize(width, height);	
		Utilities.SetFonts(this);
		this.setLayout(null);
		
		this.SetUpPanels();

		TitledBorder border = new TitledBorder("Calculation Options");
	    border.setTitleJustification(TitledBorder.LEFT);
	    border.setTitlePosition(TitledBorder.TOP);
	    this.setBorder(border);
	    this.setVisible(true);
		
	}

	
			
	private void SetUpPanels() {
				
		int inset=10;
//		if (this.getHeight()<400) inset=10;
		
//		System.out.println(this.getHeight());
		
		double frac=0.50;
		
		int heightCTS=30;
				
		
		panelOptionsEndpointMethod.init(getWidth()-2*inset, (int)((this.getHeight()-heightCTS)*frac-3*inset),gui);
		panelOptionsEndpointMethod.setLocation(inset, 2*inset);		
		this.add(panelOptionsEndpointMethod);
				
//		panelCTSOptions.init(getWidth()-2*inset, (int)(this.getHeight()*frac-3*inset),gui);
//		panelCTSOptions.setLocation(inset,panelOptionsEndpointMethod.getY()+panelOptionsEndpointMethod.getHeight()+inset);		
//		this.add(panelCTSOptions);

		panelCTSOptions.init(getWidth()-2*inset, heightCTS,gui);
		panelCTSOptions.setLocation(inset,panelOptionsEndpointMethod.getY()+panelOptionsEndpointMethod.getHeight()+inset);		
		this.add(panelCTSOptions);

		
		panelOutputOptions.init(getWidth()-2*inset, panelOptionsEndpointMethod.getHeight(), gui);
		panelOutputOptions.setLocation(inset,panelCTSOptions.getY()+panelCTSOptions.getHeight()+inset);
		this.add(panelOutputOptions);
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
//			if (e.getKeyCode() != e.VK_ENTER)return;
//
//		}
//	}    
    
	
}
