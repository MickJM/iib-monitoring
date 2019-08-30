package maersk.com.iib.monitoring.executiongroup;

/*
 * Get the 'execution group' (Integration Server) metrics from an IIB node
 * 
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import maersk.com.iib.monitoring.IIBBase;

public class ExecutionGroup extends IIBBase {

    private Map<String,AtomicInteger>iibExecutionGroupMap = new HashMap<String, AtomicInteger>();

	public ExecutionGroup() {
		super();
	}

	public void getExecutionMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		
		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        resetValues();
	
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
	        int val = IIBMONConstants.INTSERVER_NOT_RUNNING;
	        if (egroup.isRunEnabled()) {
	        	val = IIBMONConstants.INTSERVER_IS_RUN_ENABLED;
	        }
	        if (egroup.isRunning()) {
	        	val = IIBMONConstants.INTSERVER_IS_RUNNING;
	        }
	        setMetric(val, egName);
	        
		}
		
	}
	
	
	private void setMetric(int val, String egName) throws ConfigManagerProxyPropertyNotInitializedException {
				
		AtomicInteger i = iibExecutionGroupMap.get(egName);
		if (i == null) {
			iibExecutionGroupMap.put(egName, 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibIntegrationServerStatus")
				.toString(),  
				Tags.of("iibNodeName", getNodeName(),
						"integrationServerName", egName),
			new AtomicInteger(val)));
		} else {
			i.set(val);
		}        
	        
	}

	public void notRunning() {
		setMetricsValue(IIBMONConstants.APP_NOT_RUNNING);
	}
	
	public void resetValues() {
		setMetricsValue(IIBMONConstants.INTSERVER_RESET);
	}
	
	// Not running, so for any entries in the execution group list - set the values to '0' (zero)
	// ... the values will not disappear, since Guages are either 'set' or 'not set'
	public void setMetricsValue(int val) {

		Iterator<Entry<String, AtomicInteger>> listListener = this.iibExecutionGroupMap.entrySet().iterator();
		while (listListener.hasNext()) {
	        Map.Entry pair = (Map.Entry)listListener.next();
	        String key = (String) pair.getKey();
	        try {
				AtomicInteger i = (AtomicInteger) pair.getValue();
				if (i != null) {
					i.set(val);
				}
	        } catch (Exception e) {
	        }
		}		
	}
	
}
