package ToxPredictor.Application.Calculations;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import ToxPredictor.Utilities.SaveStructureToFile;


/**
 * Needed to pull from sdfs in jar if no dashboard structure is there
 * 
 * @author TMARTI02
 *
 */
public class CreateImageFromTrainingPredictionSDFs {

	
	public void CreateStructureImage(String CAS, String DestFolder,String endpointAbbrev) {

		try {
			
			AtomContainer ac=getAtomContainer(endpointAbbrev, CAS, "training");
			
			if (ac==null) ac=getAtomContainer(endpointAbbrev, CAS, "prediction");
			
			if (ac==null) return;
			
			SaveStructureToFile.CreateImageFile(ac, CAS, DestFolder);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	private AtomContainer getAtomContainer(String endpointAbbrev,String CAS, String set) {
		
		try {
			java.io.InputStream ins = null;

//			String filepath="LC50DM/LC50DM_training.sdf";
			String filepath=endpointAbbrev+"/"+endpointAbbrev+"_"+set+".sdf";
//			System.out.println(filepath);

			ins = CreateImageFromTrainingPredictionSDFs.class.getClassLoader().getResourceAsStream(filepath); 

			IteratingSDFReader mr = new IteratingSDFReader(ins,DefaultChemObjectBuilder.getInstance());
			
			while (mr.hasNext()) {
				AtomContainer ac=(AtomContainer)mr.next();
				
				if (ac==null) return null;
				
				String CASi=ac.getProperty("CAS");
								
				if (CASi.contentEquals(CAS)) {
//					System.out.println("CAS Match!"+CASi);
					return ac;
				}
			}
			return null;			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public void CreateStructureImageLDA(String CASi, String strImageFolder, String abbrevEndpoint) {
//		 TODO Auto-generated method stub
		System.out.println("Need structure image for LDA for "+CASi);
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}


}
