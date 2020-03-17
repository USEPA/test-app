package ToxPredictor.Application.GUI;


	//file: CanisMinor.java
	import java.awt.*;
	import java.awt.event.*;
	import java.net.*;
	import javax.swing.*;
	import javax.swing.event.*;


	public class DialogAboutQSARMethods extends JDialog {
	
	  protected JEditorPane mEditorPane;
	  

	  public DialogAboutQSARMethods() {
		createGUI();

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		//Hide the dialog if escape key is pressed:
		rootPane.registerKeyboardAction(actionListener, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	  protected void createGUI() {
	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());

	    mEditorPane = new JEditorPane(  );
	    mEditorPane.setEditable(false);
	    content.add(new JScrollPane(mEditorPane), BorderLayout.CENTER);



		// Get the current screen size
		Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();

		int height=(int)(scrnsize.height*0.75);
//		int width=(int)(scrnsize.width*0.75);
		int width=(int)(height);
//		if (width<650) width=650;

	    
	    setSize(width, height);
	    
	    ToxPredictor.Utilities.Utilities.CenterFrame(this);
	    
	    setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
//	    setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
	    
	  }

	  ActionListener actionListener = new ActionListener() {
		  public void actionPerformed(ActionEvent actionEvent) {
		     setVisible(false);
		  }
		};
		
//		protected JRootPane createRootPane() {
//			  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
//			  JRootPane rootPane = new JRootPane();
//			  rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
//			  return rootPane;
//			}

		
	/**
	 * Opens URL from file in jar file 
	 */
	  public void openURL(String fileName) {
	    try {
	    	URL myURL=this.getClass().getClassLoader().getResource(fileName);
	        mEditorPane.setPage(myURL);	      
	    }
	    catch (Exception e) {
	      System.out.println("Couldn't open " + fileName + ":" + e);
	    }
	  }

	  class LinkActivator implements HyperlinkListener {
	    public void hyperlinkUpdate(HyperlinkEvent he) {
	      HyperlinkEvent.EventType type = he.getEventType(  );
	      if (type == HyperlinkEvent.EventType.ACTIVATED)
	        openURL(he.getURL().toExternalForm(  ));
	    }
	  }

	  
	  
	  public static void main(String[] args) {

	    DialogAboutQSARMethods f=new DialogAboutQSARMethods();
	    f.setVisible( true );
	    f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    
	  }
	}
