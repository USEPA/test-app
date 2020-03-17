package ToxPredictor.Utilities;

import ToxPredictor.Application.model.PredictionResults;

public class TESTPredictedValue {

	public Integer index;
	public String query;
	
	public String id;
	public String extId;

	public String endpoint;
	public String method;

	public String smiles;

	public Double expValLogMolar = Double.NaN;
	public Double expValMass = Double.NaN;
	public Boolean expActive;
	
	public Double predValLogMolar = Double.NaN;
	public Double predValMass = Double.NaN;
	public Boolean predActive;
	
	public String dtxsid;
	public String casrn;
	public String gsid;
	public String preferredName;
	public String inChICode;
	public String inChIKey;

	public String reportPath;
	
	public PredictionResults predictionResults;
	
	/**
	 * Non-numerical results of prediction (if any)
	 */
	public String message;
	
	/**
	 * Error generated during prediction (if any)
	 */
	public String error;
	public String errorCode;

	public TESTPredictedValue(String id, String endpoint, String method) {
		this.id = id;
		this.endpoint = endpoint;
		this.method = method;
	}

	public TESTPredictedValue() {
		// TODO Auto-generated constructor stub
	}

	public String toString() {
		return String.format(
				"ID: %s; Endpoint: %s; Method: %s; SMILES: %s; ExpValLogMolar: %f; ExpActive: %b; PredValLogMolar: %f; PredActive: %b; Message: %s; Error: %s",
				id, endpoint, method, smiles, expValLogMolar, expActive, predValLogMolar, predActive, message, error);
	}
}
