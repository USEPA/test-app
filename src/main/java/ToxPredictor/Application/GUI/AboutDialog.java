/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 12:26:00 -0500 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2007  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sf.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ToxPredictor.Application.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import ToxPredictor.Application.TESTConstants;

/**
 * About dialog for ToxPredictor
 *
 * @author        Todd Martin
 * @cdk.created       1/30/08
 *
 */
public class AboutDialog extends JFrame {

//	public static String Version="5.01";//version of software
	
	/**
	 * 1.0.1 (5/8/08): Changed file structure so that the information in the
	 * hierarchical cluster web pages are not duplicated for each chemical. Each
	 * chemical does however have a web page for each model listing its
	 * descriptor values. This change was made to save hard drive space.
	 * 
	 * 1.0.2 (5/13/08): Changed it so that you only have to load the cluster file
	 * and training set one time (once for either the single chemical or batch
	 * mode)
	 * 
	 * 2.0 (1/22/09):
	 * 
	 * -Permanent training and test set for each toxicity endpoint.
          o The training set included with the software is the same as the training set used in the validation calculations.
          o This allows one to get a better indication of the predictive ability of the models included in the software.
    *  -The program now provides experimental and predicted values for compounds in the test set which are the most similar to the compound being predicted.
          o This will give the user a better measure of confidence in the predicted value if the similar chemicals are predicted accurately.
    *  -Improved batch mode
          o Users can now add chemicals to the batch list
          o Users can now save the batch list as an SDF file
	 * 
	 * 3.0 (4/14/09):
	 * 
	 * -Used random selection to produce training and external test sets 
	 * 	  o Kennard Stone selection gave overly optimistic estimate of predictive ability
	 * -Added BCF endpoint
	 * -Fixed it so the "N/A" method option only shows up for when just doing descriptor calculations
	 * -Add consensus prediction?
	 * 
	 * 3.1 (6/23/09):
	 * -Fixed issue where menus wouldn’t work due to different languages in other countries
	 * 
	 * 3.2 (12/18/09):
	 * -Added reproductive toxicity end point
	 * -Added CAESAR random forest method (for reproductive toxicity endpoint)
	 * 3.3 (7/8/10):
	 * -Daphnia magna LC50 endpoint was added
     * -AMES Mutagenicity endpoint was added
     * -The following changes were made for binary endpoints such as developmental toxicity and AMES mutagenicity:
     * 		o QSAR models now have stricter statistical standards (leave one out concordance = 0.8, sensitivity = 0.5, and specificity = 0.5)
     *		o Model statistics such as concordance, sensitivity, and specificity are now displayed in the results web pages
          
     * 4.0 (6/7/11):
     * -Added the following physical properties:
     * 	    o Boiling point
     *      o Flash point
     *      o Surface tension
     *      o Viscosity
     *      o Density
     *      o Water solubility
     *      o Thermal conductivity
     *      
     * -Made several improvements when loading batch files:
     *		o Batch loading is now done in a threaded fashion so one can see the current chemical being loaded and allow the user to stop loading if necessary
     *		o Program fixes aromatic bonds (bond order = 4) in SDF files
     *		o Program now does a better job of loading smiles with complicated aromatic ring systems
     *		o Program now checks for duplicate ID numbers when loading smiles files
     *		o If there are chemicals with errors during loading they are now displayed at the top of the batch list after loading
     *		o The program can now load SMILES text files with no identifier field- chemicals are assigned arbitrary IDs based on the record number and the current time in milliseconds
     *		o When the training and test sets are loaded from the file menu, the program now automatically sets the endpoint combo box to match the selected data set
     *		o The program disables options from the file menu when batch loading is in progress
     *		o The program now looks up experimental toxicity values (based on the CAS number) for chemicals that could not be loaded correctly from the structure in the batch file
     * -The webpages for the hierarchical method now display the model tables more consistently (i.e. if there's only one valid model it doesnt transpose the table like for the single model method)
     * -Added options menu:
     * 		o The check box for "relax fragment constraint" was moved to options menu
     * 		o Added menu option to change the output directory
     *  
     *  4.0.1 (5/2/11):
     *    	o Fixed bug in loading output folder from xml file
     *    	
     *  4.1 (7/27/12):
     *  	o Fixed bug for saving results files on network drives such as EPA M drive
     *  	o Updated/added endpoints as follows:
     *  		- Updated aquatic toxicity endpoints (LC50, LC50DM) using latest version of Aquire database
     *  		- Expanded IGC50 dataset from 1085 to 1792 by adding additional references from Schultz and coworkers
     *  		- Expanded BCF data set from 600 to about 1000 chemicals using new data from benfenati
     *  		- Added melting point and vapor pressure endpoints
     *  		- Changed boiling point database to EPIPHYS database (expanded data set from 3754 to 5760 chemicals)
     *  		- Expanded viscosity dataset by adding data from Riddick (437 to 551 chemicals)
     *  		- Expanded thermal conductivity data set by adding data from Vargaftik (352 to 442 chemicals) 
     *  		- Updated flash point and density data sets using data on lookchem.com
     *  		- Temperature dependent physical properties are now defined at 25C (since more vapor pressure data is available at 25C than at 20C)
     *  		- Removed some bad outliers from physical property data sets which were not corroborated by other experimental sources online
     *  		- The ability to load physical property datasets were removed from the File menu to avoid copyright issues
     *		o The program autogenerates Molecule ID for chemicals generated from Smiles string based on the formula and the current time in milliseconds (to give a descriptive yet unique ID)	 	
     * 		o Added the ability to copy the smiles of the current structure to the clipboard	
     *  	o Added the ability to load recently analyzed structures via mol files stores in the results folder
     *  	o Added the ability to launch web pages for recent batch runs
     *  	o Added the ability to create a new batch list (i.e. w/o needing to load from a file)
     *  	o The program now searches the structure database for a CAS number 
     *  	  when generated from a smiles string
     *  	o Improved the speed of the parsing of mol files (conversion of aromatic bonds to single and double bonds)
     *  		- This was done by altering AromaticityFixer to try to set aromaticity of rings that aren't attached to other aromatic rings
     *  	o Updated the results web pages as follows:
     *  		- The results graphs for multilinear regression models have been improved so that the points are plotted better (maximum/minimum axis values and increments are set better)
     *  		- Added test chemical structure to main results page for the chemical
     *  		- Added a predicted vs experimental graph for similar chemicals in the test set (more similar chemicals are closer to red color)
     *  		- Added experimental values for similar chemicals in the training set
     *  		- Moved the descriptors for a given model to a separate page to improve loading times
     *  	o The program now gives more fragment information:
     *  		- It tells you if a cluster cant make a prediction because a specific fragment in the test chemical is not present in the chemicals in the cluster
     *  		- The program warns you if the chemical has a fragment which is not accounted for (if fragment molecular weight doesn't equal calculated MW)
     *  		- The program now shows you which fragments were assigned to each atom
     *  	o The program now launches a new instance of fraEditChemical
     *  	  each time a chemical in the batch list is double clicked on- 
     *  	  previously there was a bug where changes to one chemical 
     *        in the batch list could affect another molecule in the list. This is
     *        slower but at least no errors are introduced during editing the molecules
     *  4.2 (4/2016)
     *  	o Added MOA based method for calculating acute fathead minnow toxicity
	 *		o Fixed bug involving selecting the output folder
     *      o Fix inconsistencies in the calculation of the ALOGP descriptor 		 
     *  4.2.1 (7/2016)     *  
     *  	o Corrected bug where FDA method was omitted from the list of method options.
     *  5.1.1 (2021)
     *  	o The interface for T.E.S.T. has been completely redesigned.
	 *		o The structure search feature in T.E.S.T. has been greatly improved
	 *		o The FDA method was removed to speed up calculations (with no loss in prediction accuracy using the Consensus method).
	 *		o Model load times have been significantly reduced.
	 *		o T.E.S.T. can now calculate molecular descriptors for chemicals with a large number of interconnected aromatic rings.
	 *		o T.E.S.T. calculations are no longer terminated if molecular descriptors cannot be generated for a chemical in the batch list.
	 *		o T.E.S.T. can now estimate the toxicity of chemical transformation products via CTS.
	 *		o Batch mode now displays a results screen to track prediction results inside T.E.S.T..
	 *		o T.E.S.T. now has report options to minimize the amount of files created.
     *  5.1.4 (TBA)
     *  	o Updated whim weights.txt using new Todeschini book (table A3), see whim weights v1.3.xlsx. 
     *  		- This affects MATSv and GATSv (and others)
     * 	        <dependency>
             		<groupId>gov.epa.webtest</groupId>
			        <artifactId>SystemData</artifactId>
            		<version>1.3</version>
           		</dependency>
     *  			          	 
	 */
	

	/** Displays the About Dialog for JChemPaint.  */
	public AboutDialog() {
		super("About T.E.S.T.");		
		doInit();
	}

	/**  Description of the Method */
	public void doInit() {
		String s1 = "T.E.S.T. Version "+TESTConstants.SoftwareVersion+"\n";
		s1 += "A program to estimate toxicity from molecular structure.\n";
		String s2 = "Written by Todd Martin, Paul Harten, Raghuraman Venkatapathy, and Douglas Young.\n";
		s2 += "See the user's guide for more information.";

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(Color.white);

		JLabel label1 = new JLabel();

		try {
//			JCPPropertyHandler jcpph = JCPPropertyHandler.getInstance();
//			URL url = jcpph.getResource("jcplogo" + JCPAction.imageSuffix);
						
			URL url=this.getClass().getClassLoader().getResource("EPA logo.jpg");
			
			ImageIcon icon = new ImageIcon(url);
			//ImageIcon icon = new ImageIcon(../resources/);
			label1 = new JLabel(icon);
		} catch (Exception exception) {
		}
		
		
		try {
			URL url=this.getClass().getClassLoader().getResource("epa_logo_icon.jpg");
			this.setIconImage(new ImageIcon(url).getImage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		label1.setBackground(Color.white);

		Border lb = BorderFactory.createLineBorder(Color.white, 5);
		JTextArea jtf1 = new JTextArea(s1);
		
//		System.out.println(jtf1.getFont().getName());
//		System.out.println(jtf1.getFont().getSize());
		
		Font myFont = new java.awt.Font("Dialog", Font.BOLD, 12);
		jtf1.setFont(myFont);
		jtf1.setBorder(lb);
		jtf1.setEditable(false);
		JTextArea jtf2 = new JTextArea(s2);
		jtf2.setEditable(false);
		jtf2.setBorder(lb);		
		getContentPane().add("Center", label1);
		getContentPane().add("North", jtf1);
		getContentPane().add("South", jtf2);
		pack();
		
		ToxPredictor.Utilities.Utilities.CenterFrame(this); //applet-omit
		
		setVisible(false);
	}
}

