package maersk.com.iib.monitoring.applications;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
		
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
	        
	        Properties p = new Properties();
	        p.setProperty(AttributeConstants.NAME_PROPERTY, egroup.getName().trim());

	        Enumeration<ApplicationProxy> ap = egroup.getApplications(null);
			List<ApplicationProxy> apps = Collections.list(ap);
			for (ApplicationProxy app: apps) {
				
				String appName = app.getName().trim();

				int val = 0;
		        if (app.isRunEnabled()) {
		        	val = 1;
		        }
		        if (app.isRunning()) {
		        	val = 2;
		        }
		      
				AtomicInteger a = iibApplications.get(app.getName().trim());
				if (a == null) {
					iibApplications.put(appName, 
						Metrics.gauge(new StringBuilder()
						.append(IIBPREFIX)
						.append("iibApplicationStatus")
						.toString(),  
						Tags.of("iibNodeName", this.nodeName,
								"integrationServerName", egroup.getName().trim(),
								"applicationName", app.getName().trim()),
					new AtomicInteger(val)));
				} else {
					a.set(val);
				}        
		        
			}
			
		}
		
	}
}
