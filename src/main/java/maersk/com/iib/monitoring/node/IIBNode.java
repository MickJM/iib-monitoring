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

	@Autowired
	private CollectorRegistry registry;
		
    //IIB
    private Map<String,AtomicInteger>iibStatusMap = new HashMap<String, AtomicInteger>();
	
	public IIBNode() {
		super();
	}
	
	public String GetNodeName() throws ConfigManagerProxyPropertyNotInitializedException {
		this.nodeName = this.bp.getName().trim();
		return this.nodeName;
	}

	// Get IIB Broker node metrics
	public void GetNodeMetrics() {
	
		int val = 0;		
		try {
			if (this.bp.isRunning()) {
				val = 1;
			}

		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
		}
				
		AtomicInteger i = iibStatusMap.get(this.nodeName);
		if (i == null) {
			iibStatusMap.put(this.nodeName, 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibNodeStatus")
				.toString(),  
				Tags.of("iibNodeName", this.nodeName),
			new AtomicInteger(val)));
		} else {
			i.set(val);
		}        
		
		
		
	}
}
