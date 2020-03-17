package ToxPredictor.Application.Calculations;

import java.util.ArrayList;

public class IntermediateResults {
	
	double predToxVal;
	double predToxUnc;
	String message;
	ArrayList <Double>predictedToxicities;
	ArrayList <Double>predictedUncertainties;
	
	public IntermediateResults(double predToxVal, double predToxUnc, String message,
			ArrayList<Double> predictedToxicities, ArrayList<Double> predictedUncertainties) {
		this.predToxVal = predToxVal;
		this.predToxUnc = predToxUnc;
		this.message = message;
		this.predictedToxicities = predictedToxicities;
		this.predictedUncertainties = predictedUncertainties;
	}

}
