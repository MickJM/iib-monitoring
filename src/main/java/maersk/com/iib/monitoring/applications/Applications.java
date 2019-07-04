package maersk.com.iib.monitoring.applications;

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

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import maersk.com.iib.monitoring.IIBBase;

public class Applications extends IIBBase {

    private Map<String,AtomicInteger>iibApplications = new HashMap<String, AtomicInteger>();
    
    public Applications() {
    	super();
    }
    
	public void GetApplicationMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
	
		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        ResetMetrics();
		
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
			log.info("Applications: egName:" + egName);

	        Properties p = new Properties();
	        p.setProperty(AttributeConstants.NAME_PROPERTY, egName);

	        Enumeration<ApplicationProxy> ap = egroup.getApplications(null);
			List<ApplicationProxy> apps = Collections.list(ap);
			for (ApplicationProxy app: apps) {
				
				String appName = app.getName().trim();
				log.info("Applications: appName:" + appName);
				log.info("Applications: status :" + app.isRunning());

				int val = 0;
		        if (app.isRunEnabled()) {
		        	val = 1;
		        }
		        if (app.isRunning()) {
		        	val = 2;
		        }
		        SetMetric(val, appName, egName);
		        		        
			}
		}
	}
	
	private void SetMetric(int val, String appName, String egName) {
		
		log.info("Applications: SetMetrics");
		
		String name = appName + "_" + egName;
		AtomicInteger a = iibApplications.get(name);
		if (a == null) {
			iibApplications.put(name, 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibApplicationStatus")
				.toString(),  
				Tags.of("iibNodeName", getNodeName(),
						"integrationServerName", egName,
						"applicationName", appName),
			new AtomicInteger(val)));
		} else {
			a.set(val);
		}        
		
	}
	
	public void NotRunning() {
		SetMetricValues(0);
	}
	
	public void ResetMetrics() {
		SetMetricValues(-1);
	}
	
	
	// Not running, so for any entries in the applications list - set the values to '0' (zero)
	// ... the values will not disappear, since Guages are either 'set' or 'not set'
	public void SetMetricValues(int val) {

		Iterator<Entry<String, AtomicInteger>> listListener = this.iibApplications.entrySet().iterator();
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
