package maersk.com.iib.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.broker.config.proxy.BrokerProxy;

public class IIBBase {

	// loggin and prefix
	protected Logger log = LogManager.getLogger(this.getClass());
	protected static final String IIBPREFIX = "iib:";			

	// Node name
	protected String nodeName;
	protected void setNodeName(String val) {
		this.nodeName = val;
	}
	
	// Broker proxy
    protected BrokerProxy bp = null;
	protected void setBrokerProxy(BrokerProxy value) {
		this.bp = value;
	}

}
