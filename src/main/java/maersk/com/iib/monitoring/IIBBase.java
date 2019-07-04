package maersk.com.iib.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.broker.config.proxy.BrokerProxy;

public class IIBBase {

	// loggin and prefix
	protected Logger log = LogManager.getLogger(this.getClass());
	protected static final String IIBPREFIX = "iib:";			

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

}
