package gov.epa.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ToxPredictor.Database.DSSToxRecord;

public class LookupResult {
    
    private List<DSSToxRecord> result;

    @JsonProperty
    public List<DSSToxRecord> getResult() {
        return result;
    }

    @JsonProperty
    public void setResult(List<DSSToxRecord> result) {
        this.result = result;
    }
    
    @JsonProperty
    public int getRecordCount() {
        return result == null ? 0 : result.size();
    }

    @Override
    public String toString() {
        return String.format("LookupResponse [result=%s]", result);
    }
}
