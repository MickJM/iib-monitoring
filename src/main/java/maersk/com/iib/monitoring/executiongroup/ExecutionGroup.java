package maersk.com.iib.monitoring.executiongroup;

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

	public void SetExecutionMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		
		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        ResetValues();
	
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
	        int val = 0;
	        if (egroup.isRunEnabled()) {
	        	val = 1;
	        }
	        if (egroup.isRunning()) {
	        	val = 2;
	        }
	        SetMetric(val, egName);
	        
		}
		
	}
	
	
	private void SetMetric(int val, String egName) throws ConfigManagerProxyPropertyNotInitializedException {
				
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

	public void NotRunning() {
		SetMetricsValue(0);
	}
	
	public void ResetValues() {
		SetMetricsValue(-1);
	}
	
	// Not running, so for any entries in the execution group list - set the values to '0' (zero)
	// ... the values will not disappear, since Guages are either 'set' or 'not set'
	public void SetMetricsValue(int val) {

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
