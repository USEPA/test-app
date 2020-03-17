import ToxPredictor.Application.TESTConstants;

public class TESTParams {
	String inputFilePath;
	int iEndpoint;
	int iMethod;
	
	TESTParams(String fp, int e, int m) {
		inputFilePath = fp;
		iEndpoint = e;
		iMethod = m;
	}

	
	//We have 18 endpoints:
	static int [] endpoints= {
			TESTConstants.numChoiceFHM_LC50,
			TESTConstants.numChoiceDM_LC50,
			TESTConstants.numChoiceTP_IGC50,
			TESTConstants.numChoiceRat_LD50,
			// TESTConstants.ChoiceGA_EC50,
			TESTConstants.numChoiceBCF,
			TESTConstants.numChoiceReproTox,
			TESTConstants.numChoiceMutagenicity,
			TESTConstants.numChoiceER_Binary,
			TESTConstants.numChoiceER_LogRBA,
			
			TESTConstants.numChoiceBoilingPoint,
			TESTConstants.numChoiceVaporPressure,
			TESTConstants.numChoiceMeltingPoint,
			TESTConstants.numChoiceFlashPoint,
			TESTConstants.numChoiceDensity,
			TESTConstants.numChoiceSurfaceTension,
			TESTConstants.numChoiceThermalConductivity,
			TESTConstants.numChoiceViscosity,
			TESTConstants.numChoiceWaterSolubility};
			
	/**
	 * I created this method so that it's easier to add more endpoints
	 * @param method
	 * @return
	 */
	static TESTParams[] createMatrix(int method) {
		TESTParams[] tp=new TESTParams[endpoints.length];
		for (int i=0;i<endpoints.length;i++) {
			int endpointNumi=endpoints[i];
			String endpoint=TESTConstants.getEndpoint(endpointNumi);
			String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			String fp="data/"+endpointAbbrev+"/"+endpointAbbrev;
			TESTParams tpi=new TESTParams(fp,endpointNumi,method);
			tp[i]=tpi;
		}
		return tp;
	}
}
