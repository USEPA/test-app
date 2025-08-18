package ToxPredictor.Application.Calculations;


import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Utilities.SaveStructureToFile;
import ToxPredictor.Utilities.StructureImageUtil;


/**
 * Needed to pull from sdfs in jar if no dashboard structure is there
 * 
 * @author TMARTI02
 *
 */
public class CreateImageFromTrainingPredictionSDFs {

	
	public void CreateStructureImage(String CAS, String DestFolder,String endpointAbbrev) {

		try {
			
			IAtomContainer ac=getAtomContainer(endpointAbbrev, CAS, "training");
			
			if (ac==null) ac=getAtomContainer(endpointAbbrev, CAS, "prediction");
			
			if (ac==null) return;
			
			SaveStructureToFile.CreateImageFile(ac, CAS, DestFolder);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	public String CreateStructureImage2(String CAS, String DestFolder,String endpointAbbrev) {

		try {
			
			IAtomContainer ac=getAtomContainer(endpointAbbrev, CAS, "training");
			if (ac==null) ac=getAtomContainer(endpointAbbrev, CAS, "prediction");
			if (ac==null) return null;
			
//			SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Isomeric);
//			String smiles=smigen.create(ac);
			return StructureImageUtil.generateImageSrcBase64FromAtomContainer(ac);

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	private IAtomContainer getAtomContainer(String endpointAbbrev,String CAS, String set) {
		
		try {
			java.io.InputStream ins = null;

//			String filepath="LC50DM/LC50DM_training.sdf";
			
			String filepath=WebTEST4.dataFolder +"/"+ endpointAbbrev+"/"+endpointAbbrev+"_"+set+".sdf";

//			System.out.println(filepath);

			ins = CreateImageFromTrainingPredictionSDFs.class.getClassLoader().getResourceAsStream(filepath); 

			try (IteratingSDFReader mr = new IteratingSDFReader(ins,DefaultChemObjectBuilder.getInstance())) {
				while (mr.hasNext()) {
					IAtomContainer ac=mr.next();
					
					if (ac==null) {
						return null;
					}
					
					String CASi=ac.getProperty("CAS");
									
					if (CASi.contentEquals(CAS)) {
//					System.out.println("CAS Match!"+CASi);
						return ac;
					}
				}
			}
			
			return null;			
			
		} catch (Exception ex) {
			System.out.println("CreateImageFromTrainingPredictionSDFs: Error getting AtomContainer for CAS="+CAS+" for "+endpointAbbrev+" for "+set+"set");
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
