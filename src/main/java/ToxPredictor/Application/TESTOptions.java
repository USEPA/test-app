package ToxPredictor.Application;

import java.util.Hashtable;
import java.util.Vector;

import ToxPredictor.Utilities.chemicalcompare;

public class TESTOptions {
	public String defaultMethod = TESTConstants.ChoiceConsensus;
	
	public static Vector<String> endPointsToxicity=null; //defined later in setUpChoices()
	public static Vector<String> endPointsPhysicalProperty=null; //defined later in setUpChoices()
	public static Hashtable<String,String> htAbbrevChoice=new Hashtable<String,String>();
	public static Vector<String>Methods=new Vector<String>();
	
	public static String compareMethod=chemicalcompare.methodHybrid;//fast and accurate

	public static boolean includeToxicityEndpoints=true;
	public static boolean includePhysicalPropertyEndpoints=true;
	public static boolean includeFDA=false;

	
	public static void setUpChoices() {
		int i=0;
		
//		int numEndpoints=1;
//		if (includeToxicityEndpoints) numEndpoints+=7;
//		if (includePhysicalPropertyEndpoints) numEndpoints+=9;
		
		endPointsToxicity=new Vector<String>();
		endPointsPhysicalProperty=new Vector<String>();
		
		Methods.add(TESTConstants.ChoiceConsensus);
		Methods.add(TESTConstants.ChoiceHierarchicalMethod);
		Methods.add(TESTConstants.ChoiceSingleModelMethod);
		Methods.add(TESTConstants.ChoiceGroupContributionMethod);
		if (includeFDA) Methods.add(TESTConstants.ChoiceFDAMethod);
		Methods.add(TESTConstants.ChoiceNearestNeighborMethod);

		
		if (includeToxicityEndpoints) {
			//*add endpoint* 03
			endPointsToxicity.add(TESTConstants.ChoiceFHM_LC50);
			endPointsToxicity.add(TESTConstants.ChoiceDM_LC50);
			endPointsToxicity.add(TESTConstants.ChoiceTP_IGC50);				
			endPointsToxicity.add(TESTConstants.ChoiceRat_LD50);
			endPointsToxicity.add(TESTConstants.ChoiceBCF);
			endPointsToxicity.add(TESTConstants.ChoiceReproTox);
			endPointsToxicity.add(TESTConstants.ChoiceMutagenicity);
//			endPointsToxicity.add(ChoiceEstrogenReceptor);
//			endPointsToxicity.add(ChoiceEstrogenReceptorRelativeBindingAffinity);

			//*add endpoint* 04
			htAbbrevChoice.put(TESTConstants.ChoiceFHM_LC50, TESTConstants.abbrevChoiceFHM_LC50);
			htAbbrevChoice.put(TESTConstants.ChoiceDM_LC50, TESTConstants.abbrevChoiceDM_LC50);
			htAbbrevChoice.put(TESTConstants.ChoiceTP_IGC50, TESTConstants.abbrevChoiceTP_IGC50);				
			htAbbrevChoice.put(TESTConstants.ChoiceRat_LD50, TESTConstants.abbrevChoiceRat_LD50);
			htAbbrevChoice.put(TESTConstants.ChoiceBCF, TESTConstants.abbrevChoiceBCF);
			htAbbrevChoice.put(TESTConstants.ChoiceReproTox, TESTConstants.abbrevChoiceReproTox);		
			htAbbrevChoice.put(TESTConstants.ChoiceMutagenicity, TESTConstants.abbrevChoiceMutagenicity);
//			htAbbrevChoice.put(ChoiceEstrogenReceptor,abbrevChoiceER_Binary);
//			htAbbrevChoice.put(ChoiceEstrogenReceptorRelativeBindingAffinity,abbrevChoiceER_LogRBA);
		}
		
		if (includePhysicalPropertyEndpoints) {
			endPointsPhysicalProperty.add(TESTConstants.ChoiceBoilingPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceVaporPressure);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceMeltingPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceFlashPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceDensity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceSurfaceTension);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceThermalConductivity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceViscosity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceWaterSolubility);
			
			htAbbrevChoice.put(TESTConstants.ChoiceBoilingPoint, TESTConstants.abbrevChoiceBoilingPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceVaporPressure, TESTConstants.abbrevChoiceVaporPressure);
			htAbbrevChoice.put(TESTConstants.ChoiceMeltingPoint, TESTConstants.abbrevChoiceMeltingPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceFlashPoint, TESTConstants.abbrevChoiceFlashPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceDensity, TESTConstants.abbrevChoiceDensity);
			htAbbrevChoice.put(TESTConstants.ChoiceSurfaceTension, TESTConstants.abbrevChoiceSurfaceTension);
			htAbbrevChoice.put(TESTConstants.ChoiceThermalConductivity, TESTConstants.abbrevChoiceThermalConductivity);
			htAbbrevChoice.put(TESTConstants.ChoiceViscosity, TESTConstants.abbrevChoiceViscosity);
			htAbbrevChoice.put(TESTConstants.ChoiceWaterSolubility, TESTConstants.abbrevChoiceWaterSolubility);
		}
		
//		EndPoints[i++]=ChoiceDescriptors;
	}
}
