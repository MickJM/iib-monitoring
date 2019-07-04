package maersk.com.iib.monitoring.messageflows;

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

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.MessageFlowProxy;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import maersk.com.iib.monitoring.IIBBase;

public class MessageFlows extends IIBBase {

    private Map<String,AtomicInteger>iibMessageFlows = new HashMap<String, AtomicInteger>();

    public MessageFlows() {
    	super();
    }
    
	public void GetMessageFlowMetrics() throws ConfigManagerProxyPropertyNotInitializedException {

		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        ResetValues();
        
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
	        
	        Properties p = new Properties();
	        p.setProperty(AttributeConstants.NAME_PROPERTY, egroup.getName().trim());

	        Enumeration<ApplicationProxy> ap = egroup.getApplications(null);
			List<ApplicationProxy> apps = Collections.list(ap);
			for (ApplicationProxy app: apps) {
				
				String appName = app.getName().trim();

			    Enumeration<MessageFlowProxy> mfp = app.getMessageFlows(null);
				List<MessageFlowProxy> msgFlows = Collections.list(mfp);

				for (MessageFlowProxy flow: msgFlows) {
					
					String flowName = flow.getName().trim();
					
					int val = 0;
			        if (flow.isRunEnabled()) {
			        	val = 1;
			        }
			        if (flow.isRunning()) {
			        	val = 2;
			        }

			        SetMetrics(val, egName, appName, flowName);
			        			        
				}				
			}
		}
	}

	private void SetMetrics(int val, String egName, String appName, String flowName) {
		
        String name = egName + "_" + appName + "_" + flowName;
		AtomicInteger mf = iibMessageFlows.get(name);
		if (mf == null) {
			iibMessageFlows.put(name, 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibMessageFlow")
				.toString(),  
				Tags.of("iibNodeName", getNodeName(),
						"integrationServerName", egName,
						"applicationName", appName,
						"messageFlow", flowName),
			new AtomicInteger(val)));
		} else {
			mf.set(val);
		}        
		
	}
	
	public void NotRunning() {
		SetMetricValues(0);
	}
	
	public void ResetValues() {
		SetMetricValues(-1);
	}
	
	
	// Not running, so for any entries in the applications list - set the values to '0' (zero)
	// ... the values will not disappear, since Guages are either 'set' or 'not set'
	private void SetMetricValues(int val) {

		Iterator<Entry<String, AtomicInteger>> listListener = this.iibMessageFlows.entrySet().iterator();
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
