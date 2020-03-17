package gov.epa.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResult {
    
    private List<NcctRecord> result;

    @JsonProperty
    public List<NcctRecord> getResult() {
        return result;
    }

    @JsonProperty
    public void setResult(List<NcctRecord> result) {
        this.result = result;
    }
    
    @JsonProperty
    public int getRecordCount() {
        return result == null ? 0 : result.size();
    }

    @Override
    public String toString() {
        return String.format("SearchResponse [result=%s]", result);
    }
    

}
