package app.com.iib.monitoring.executiongroup;

/*
 * Copyright 2019
 *
 * Get integration server metrics
 * 
 */


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

import app.com.iib.monitoring.IIBBase;
import io.micrometer.core.instrument.Tags;

public class ExecutionGroup extends IIBBase {

	protected static final String lookupStatus = IIBPREFIX + "iibIntegrationServerStatus";

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
	        setMetricGauge(val, egName);
		}
		
	}

	/*
	 * Set IntegrationServerName guage
	 */
	private void setMetricGauge(int val, String egName) throws ConfigManagerProxyPropertyNotInitializedException {

		meterRegistry.gauge("iib:iibIntegrationServerStatus", 
				Tags.of("iibNodeName", getNodeName(),
				"integrationServerName", egName)
				,val);
			
	}
	
	public void notRunning() {
		setMetricsValue();
	}
	
	public void resetValues() {
		setMetricsValue();
	}
	
	/*
	 * Delete the metrics
	 */
	public void setMetricsValue() {
		DeleteMetricEntry(lookupStatus);

	}
	
}
