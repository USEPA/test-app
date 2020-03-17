package gov.epa.api;

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import ToxPredictor.Application.TESTConstants;

@JsonInclude(Include.NON_NULL)
public class PredictionResult {
	private UUID uuid;
	private String id;
	private Long predictionTime; 
	private String software;
	private String softwareVersion;
	private String condition;
	private String endpoint;
	private String method;
	private String details;
	private ArrayList<PredictedValue> predictions;

	public PredictionResult()
	{
		this.uuid = UUID.randomUUID();
		this.software = TESTConstants.SoftwareTitle;
		this.softwareVersion = TESTConstants.SoftwareVersion;
		predictions = new ArrayList<PredictedValue>();
	}
	
	@JsonProperty
	public UUID getUuid() {
		return uuid;
	}
	@JsonProperty
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@JsonProperty
	public String getId() {
		return id;
	}
	@JsonProperty
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty
	public Long getPredictionTime() {
		return predictionTime;
	}
	@JsonProperty
	public void setPredictionTime(Long predictionTime) {
		this.predictionTime = predictionTime;
	}

	@JsonProperty
	public String getSoftware() {
		return software;
	}
	@JsonProperty
	public void setSoftware(String software) {
		this.software = software;
	}

	@JsonProperty
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	@JsonProperty
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	@JsonProperty
	public String getCondition() {
		return condition;
	}
	@JsonProperty
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	@JsonProperty
	public String getEndpoint() {
		return endpoint;
	}
	@JsonProperty
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@JsonProperty
	public String getMethod() {
		return method;
	}
	@JsonProperty
	public void setMethod(String method) {
		this.method = method;
	}

	@JsonProperty
	public ArrayList<PredictedValue> getPredictions() {
		return predictions;
	}

	@JsonIgnore
	public Integer getPredictionsNumber() {
		return predictions.size();
	}

	@JsonProperty
	public String getDetails() {
		return details;
	}

	@JsonProperty
	public void setDetails(String details) {
		this.details = details;
	}
	
}
