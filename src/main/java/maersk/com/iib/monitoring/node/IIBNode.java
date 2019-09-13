package maersk.com.iib.monitoring.node;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import maersk.com.iib.monitoring.IIBBase;

@Component
public class IIBNode extends IIBBase {

    //IIB status
    private Map<String,AtomicInteger>iibStatusMap = new HashMap<String, AtomicInteger>();
    
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

		
		/*
		AtomicInteger i = iibStatusMap.get(getNodeName());
		if (i == null) {
			iibStatusMap.put(getNodeName(), 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibNodeStatus")
				.toString(),  
				Tags.of("iibNodeName", getNodeName()),
			new AtomicInteger(val)));
		} else {
			i.set(val);
		}        
		*/
	}
	
	private void resetMetric() {
		DeleteMetricEntry(lookupStatus);

	}
}
