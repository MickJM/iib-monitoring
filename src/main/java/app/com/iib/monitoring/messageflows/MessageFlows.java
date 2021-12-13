package app.com.iib.monitoring.messageflows;

/*
 * Copyright 2020
 *
 * Get Message flow metrics
 * 
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.MessageFlowProxy;

import app.com.iib.monitoring.IIBBase;
import io.micrometer.core.instrument.Tags;

public class MessageFlows extends IIBBase {

    protected static final String lookupStatus = IIBPREFIX + "iibMessageFlow";


    public MessageFlows() {
    	super();
    }
    
    /*
     * Get MessageFlow metrics 
     */
	public void getMessageFlowMetrics() throws ConfigManagerProxyPropertyNotInitializedException {

		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        resetValues();
        
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
					
					int val = IIBMONConstants.MSGFLOW_NOT_RUNNING;
			        if (flow.isRunEnabled()) {
			        	val = IIBMONConstants.MSGFLOW_IS_RUN_ENABLED;
			        }
			        if (flow.isRunning()) {
			        	val = IIBMONConstants.MSGFLOW_IS_RUNNING;
			        }
			        setMetricGauge(val, egName, appName, flowName);
				}
				
			}
		}
	}

	/*
	 * Set MessageFlow metric
	 */
	private void setMetricGauge(int val, String egName, String appName, String flowName) throws ConfigManagerProxyPropertyNotInitializedException {

		meterRegistry.gauge(lookupStatus, 
				Tags.of("iibNodeName", getNodeName(),
						"integrationServerName", egName,
						"applicationName", appName,
						"messageFlow", flowName)
				,val);
			
	}
	
	public void notRunning() {
		setMetricValues();
	}
	
	public void resetValues() {
		setMetricValues();
	}
	
	
	/*
	 * Delete the metric
	 */
	private void setMetricValues() {
		DeleteMetricEntry(lookupStatus);
		
	}

}
