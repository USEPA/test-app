package ToxPredictor.Application;

import java.util.HashSet;
import java.util.Set;

public enum WebReportType {
    JSON, 
    HTML, 
    PDF;
    
    public static Set<WebReportType> getAll() {
        HashSet<WebReportType> result = new HashSet<>();
        result.add(WebReportType.JSON);
        result.add(WebReportType.HTML);
        result.add(WebReportType.PDF);
        return result;
    };
    
    public static Set<WebReportType> getNone() {
        return new HashSet<>();
    };
    
}
