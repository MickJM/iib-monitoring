package maersk.com.iib.monitoring;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Inital version: Base object for other IIB monitoring object
 * 
 * 30/08/2019 Fixed NODE_IS_RUNNING, set to value 1
 *  
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ibm.broker.config.proxy.BrokerProxy;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;

public class IIBBase {

    @Value("${application.debug:false}")
    protected boolean _debug;

	// loggin and prefix
	protected Logger log = LogManager.getLogger(this.getClass());
	protected static final String IIBPREFIX = "iib:";			

	@Autowired
	public MeterRegistry meterRegistry;

	// Node name
	private String nodeName;
	protected void setNodeName(String val) {
		this.nodeName = val;
	}
	protected String getNodeName() {
		return this.nodeName;
	}

	// Queue Manager associated with the IIB node
	private String queueManager;
	protected void setQueueManagerName(String val) {
		this.queueManager = val;
	}
	protected String getQueueManagerName() {
		return this.queueManager;
	}

	// IIB version
	private String iibVersion;
	protected void setIIBVersion(String val) {
		this.iibVersion = val;
	}
	protected String getIIBVersion() {
		return this.iibVersion;
	}
	
	// Broker proxy
    protected BrokerProxy bp = null;
	protected void setBrokerProxy(BrokerProxy value) {
		this.bp = value;
	}

	public interface IIBMONConstants {
		static final int APP_RESET = -1;
		static final int APP_NOT_RUNNING = 0;
		static final int APP_IS_RUN_ENABLED = 1;
		static final int APP_IS_RUNNING = 2;

		static final int INTSERVER_RESET = -1;
		
		static final int INTSERVER_NOT_RUNNING = 0;
		static final int INTSERVER_IS_RUN_ENABLED = 1;
		static final int INTSERVER_IS_RUNNING = 2;

		static final int MSGFLOW_RESET = -1;
		static final int MSGFLOW_NOT_RUNNING = 0;
		static final int MSGFLOW_IS_RUN_ENABLED = 1;
		static final int MSGFLOW_IS_RUNNING = 2;
	
		static final int NODE_RESET = -1;
		static final int NODE_NOT_RUNNING = 0;
		static final int NODE_IS_RUNNING = 1;
		
	}

	//protected void DeleteMetricEntry(MeterRegistry meterRegistry, String lookup) {
	protected void DeleteMetricEntry(String lookup) {
		
		List<Meter.Id> meterIds = null;
		meterIds = this.meterRegistry.getMeters().stream()
		        .map(Meter::getId)
		        .collect(Collectors.toList());
		
		Iterator<Id> list = meterIds.iterator();
		while (list.hasNext()) {
			Meter.Id id = list.next();
			if (id.getName().contains(lookup)) {
				meterRegistry.remove(id);
			}
		}
		
	}
}
