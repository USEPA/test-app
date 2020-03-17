package ToxPredictor.misc;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import ToxPredictor.Utilities.CDKUtilities;

public class CreateSCIHUB_Files_ER_Paper {

	void createSmilesFile() {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST5\\ToxPredictor\\DescriptorTextTablesToxCast\\ER_Cerapp Data Files";
		String sdfFilePath=folder+"\\ER_CERAPP_overall.sdf";
		
		MolFileUtilities m=new MolFileUtilities();
		
		AtomContainerSet acs=m.LoadFromSDF(sdfFilePath);
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			
			ac = (AtomContainer) AtomContainerManipulator.removeHydrogens(ac);// remove
			
			String CAS=ac.getProperty("CAS");
			
			String SMILES=null;
			try {
				SMILES = CDKUtilities.generateSmiles(ac);
			} catch (Exception e) {
			
			}
			
			System.out.println(CAS+"\t"+SMILES);
			
		}
		
		
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CreateSCIHUB_Files_ER_Paper c=new CreateSCIHUB_Files_ER_Paper();
		c.createSmilesFile();
	}

}
