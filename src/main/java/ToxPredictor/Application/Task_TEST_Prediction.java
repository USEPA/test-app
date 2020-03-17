package ToxPredictor.Application;

import java.util.List;
import java.util.Set;

import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Utilities.TESTPredictedValue;

public class Task_TEST_Prediction implements Runnable {

//	SwingWorker worker;

	AtomContainerSet acs=null;
	String endpoint=null;
	String method=null;
	Set<WebReportType> wrt=null;
	
	public void init(AtomContainerSet acs,String endpoint,String method,Set<WebReportType> wrt) throws Exception {

        try {
        	this.acs=acs;
        	this.endpoint=endpoint;
        	this.method=method;
        	this.wrt=wrt;
        	
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	@Override
	public void run() {

		try {
			List<TESTPredictedValue> listTPV = WebTEST3.go(acs,
					new CalculationParameters(null, null, endpoint, method, wrt));

    	} catch (Exception ex){
    		ex.printStackTrace();
    	}

	}
	
	

	


}
