package gov.epa.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PredictedValue {
	private String id;
	private String smiles;
	
	private String expValMolarLog;
	private String expValMass; 
	private Boolean expActive;

	private String predValMolarLog; 
	private String predValMass;
	private Boolean predActive;

	private String molarLogUnits;
	private String massUnits;

	private String message;
	private String error;
	private String errorCode;
	
	private String endpoint;
	private String method;
	
	private String dtxsid;
	private String casrn;
	private String gsid;
	private String preferredName;
	private String inChICode;
	private String inChIKey;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	

	public String getExpValMolarLog() {
		return expValMolarLog;
	}

	public void setExpValMolarLog(String val) {
		this.expValMolarLog = val;
	}


	public String getPredValMolarLog() {
		return predValMolarLog;
	}

	public void setPredValMolarLog(String val) {
		this.predValMolarLog = val;
	}


	public String getPredValMass() {
		return predValMass;
	}

	public void setPredValMass(String val) {
		this.predValMass = val;
	}


	public String getExpValMass() {
		return expValMass;
	}

	public void setExpValMass(String expValueMass) {
		this.expValMass = expValueMass;
	}
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	

	public Boolean getExpActive() {
		return expActive;
	}

	public void setExpActive(Boolean expActive) {
		this.expActive = expActive;
	}
	

	public Boolean getPredActive() {
		return predActive;
	}

	public void setPredActive(Boolean predActive) {
		this.predActive = predActive;
	}
	

	public String getMassUnits() {
		return massUnits;
	}

	public void setMassUnits(String massUnits) {
		this.massUnits = massUnits;
	}
	
	public String getMolarLogUnits() {
		return molarLogUnits;
	}

	public void setMolarLogUnits(String molarLogUnits) {
		this.molarLogUnits = molarLogUnits;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getDtxsid() {
		return dtxsid;
	}
	public void setDtxsid(String dtxsid) {
		this.dtxsid = dtxsid;
	}
	public String getCasrn() {
		return casrn;
	}
	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}
	public String getGsid() {
		return gsid;
	}
	public void setGsid(String gsid) {
		this.gsid = gsid;
	}
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public String getInChICode() {
		return inChICode;
	}
	public void setInChICode(String inChICode) {
		this.inChICode = inChICode;
	}
	public String getInChIKey() {
		return inChIKey;
	}
	public void setInChIKey(String inChIKey) {
		this.inChIKey = inChIKey;
	}
	
	
	
	
}
