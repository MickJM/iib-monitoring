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

    @Value("${application.debug}")
    private boolean _debug;

	//@Autowired
	//private CollectorRegistry registry;
		
    //IIB
    private Map<String,AtomicInteger>iibStatusMap = new HashMap<String, AtomicInteger>();
	
	public IIBNode() {
		super();
	}
	
	// Set IIB node properties for;
	//   queue manager
	//   IIB version
	//public void SetNodeProperties() throws ConfigManagerProxyPropertyNotInitializedException {
		//setQueueManagerName(this.bp.getQueueManagerName().trim());
		//String x = Integer.toString(this.bp.getBrokerVersion());
		//setIIBVersion(x); 
	//}
	
	// Get IIB node name
	public String GetNodeName() throws ConfigManagerProxyPropertyNotInitializedException {
		setNodeName(this.bp.getName().trim());		
		return getNodeName();
	}

	// Get IIB Broker node metrics
	public void GetNodeMetrics() {
	
		int val = 0;		
		try {
			if (this.bp.isRunning()) {
				val = 1;
			}

		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
		} catch (NullPointerException e) {
		}
		
		SetNodeMetrics(val);
	}
	
	// IIB isn't running, so set to '0'
	public void NotRunning() {
	
		SetNodeMetrics(0);
		
	}
	
	// Set the IIB node metric
	private void SetNodeMetrics(int val) {
		
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
		
	}
}
