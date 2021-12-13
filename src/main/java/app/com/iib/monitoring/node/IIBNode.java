package app.com.iib.monitoring.node;

/*
 * Copyright 2020
 *
 * Get Node metrics
 * 
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;

import app.com.iib.monitoring.IIBBase;
import io.micrometer.core.instrument.Tags;

@Component
public class IIBNode extends IIBBase {
    
    protected static final String lookupStatus = IIBPREFIX + "iibNodeStatus";
	
	public IIBNode() {
		super();
	}
		
	// Get IIB node name
	public String getIIBNodeName() throws ConfigManagerProxyPropertyNotInitializedException {
		setNodeName(this.bp.getName().trim());		
		return getNodeName();
	}

	// Get IIB Broker node metrics
	public void getNodeMetrics() {
	
		int val = IIBMONConstants.NODE_NOT_RUNNING;		
		try {
			if (this.bp.isRunning()) {
				val = IIBMONConstants.NODE_IS_RUNNING;
			}

		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
		} catch (NullPointerException e) {
		}
		resetMetric();
		setNodeMetrics(val);
	}
	
	// IIB isn't running, so set to '0'
	public void notRunning() {	
		setNodeMetrics(IIBMONConstants.NODE_NOT_RUNNING);
		
	}
	
	// Set the IIB node metric
	private void setNodeMetrics(int val) {
		
		meterRegistry.gauge(lookupStatus, 
				Tags.of("iibNodeName", getNodeName())
				,val);

	}
	
	private void resetMetric() {
		DeleteMetricEntry(lookupStatus);

	}
}
