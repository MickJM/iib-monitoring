package maersk.com.iib.monitoring.executiongroup;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
	
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
	        int val = 0;
	        if (egroup.isRunEnabled()) {
	        	val = 1;
	        }
	        if (egroup.isRunning()) {
	        	val = 2;
	        }
	        
			AtomicInteger i = iibExecutionGroupMap.get(egName);
			if (i == null) {
				iibExecutionGroupMap.put(egName, 
					Metrics.gauge(new StringBuilder()
					.append(IIBPREFIX)
					.append("iibIntegrationServerStatus")
					.toString(),  
					Tags.of("iibNodeName", this.nodeName,
							"integrationServerName", egroup.getName().trim()),
				new AtomicInteger(val)));
			} else {
				i.set(val);
			}        
	        
	        //Properties p = new Properties();
	        //p.setProperty(AttributeConstants.NAME_PROPERTY, egroup.getName().trim());
		}	
	}
	
}
