package app.com.iib.monitoring.applications;

/*
 * Copyright 2019
 *
 * Get application metrics
 * 
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

import app.com.iib.monitoring.IIBBase;
import io.micrometer.core.instrument.Tags;
import io.prometheus.client.CollectorRegistry;

public class Applications extends IIBBase {
	
	@Autowired
	public CollectorRegistry registry;
	
	protected static final String lookupStatus = IIBPREFIX + "iibApplicationStatus";

    private static final int APP_RESET = 0;
    
    public Applications() {
    	super();
    }
    
    // For each execution group, 
	public void getApplicationMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
	
		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        resetMetrics();
		
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
			if (Debug()) { log.info("Applications: egName:" + egName); }

	        Properties p = new Properties();
	        p.setProperty(AttributeConstants.NAME_PROPERTY, egName);

	        Enumeration<ApplicationProxy> ap = egroup.getApplications(null);
			List<ApplicationProxy> apps = Collections.list(ap);
			for (ApplicationProxy app: apps) {
				
				String appName = app.getName().trim();
				if (Debug()) { 
					log.info("Applications: appName:" + appName);
					log.info("Applications: status :" + app.isRunning());
				}
				
				int val = IIBMONConstants.APP_NOT_RUNNING;
		        if (app.isRunEnabled()) {
		        	val = IIBMONConstants.APP_IS_RUN_ENABLED;
		        }
		        if (app.isRunning()) {
		        	val = IIBMONConstants.APP_IS_RUNNING;
		        }
		        setGaugeMap(val, appName, egName);
		        
			}
		}
	}

	/*
	 * Sett application gauge
	 */
	private void setGaugeMap(int val, String appName, String egName) throws ConfigManagerProxyPropertyNotInitializedException {

		String nodeName = getNodeName();

		//meterRegistry.gauge("iib:Application", 10.0);
		meterRegistry.gauge("iib:Application", 
				Tags.of("iibNodeName", getNodeName(),
				"integrationServerName", egName,
				"applicationName",appName)
				,val);
		
	}
	
	
	public void notRunning() {
		setMetricValues();
	}
	
	private void resetMetrics() {
		setMetricValues();
	}
	
	/*
	 * Not running
	 */
	public void setMetricValues() {
		DeleteMetricEntry(lookupStatus);
				
	}

}
