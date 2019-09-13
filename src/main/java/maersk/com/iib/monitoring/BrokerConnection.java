package maersk.com.iib.monitoring;

/*
 * Connect to IIB brokers for Metrics
 * 
 * 30/08/2019 IBM advised that Node was not being set correctly.
 *            Ensured Node Metrics was correctly invoked
 *            
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.IntegrationNodeConnectionParameters;

import io.prometheus.client.CollectorRegistry;
import maersk.com.iib.monitoring.applications.Applications;
import maersk.com.iib.monitoring.executiongroup.ExecutionGroup;
import maersk.com.iib.monitoring.messageflows.MessageFlows;
import maersk.com.iib.monitoring.node.IIBNode;

@Component
public class BrokerConnection {

    private Logger log = LogManager.getLogger(this.getClass());

    @Value("${application.debug:false}")
    private boolean _debug;
    @Value("${ibm.iib.node}")
    private String node;
    
    @Value("${ibm.iib.connName}")
    private String connName;   
    private String hostName;
    private int port;
    

    @Value("${ibm.iib.user:}")
    private String userid;
    @Value("${ibm.iib.password:}")
    private String password;
    @Value("${ibm.iib.useSSL:false}")
    private boolean useSSL;

    @Value("${ibm.iib.event.delayInMilliSeconds}")
    private long resetIterations;

    private BrokerConnectionParameters bconn = null;
    private BrokerProxy bp = null;
	
    private String brokerName;
    
    @Autowired
    public IIBNode iibNode;
    @Autowired
    public ExecutionGroup iibExecutionGroups;
    @Autowired
    public Applications iibApplications;
    @Autowired
    public MessageFlows iibMessageFlows;
    
    @Bean
    public IIBNode createNode() {
    	return new IIBNode();
    }
    @Bean
    public ExecutionGroup createExecutionGroups() {
    	return new ExecutionGroup();
    }
    @Bean
    public Applications createApplications() {
    	return new Applications();
    }
    @Bean
    public MessageFlows createMessageFlows() {
    	return new MessageFlows();
    }
    
    @Autowired
    public CollectorRegistry registry;
    
	private BrokerConnection() {
	}

	// Every 'x' seconds, get the metrics
	@Scheduled(fixedDelayString="${ibm.iib.event.delayInMilliSeconds}")
	public void scheduler() {
		
		if (this._debug) {log.info("IIB Broker stats");}

		try {

			if (this.bconn != null) {
				if (this.bp != null) {
					if (!this.bp.isRunning()) {
						setProperties();
					}
					getMetrics();
					
				} else {
					createIIBConnection();
					setProperties();
					this.iibNode.setNodeName(this.node);
					brokerNotRunning();
					
				}
			} else {
				createIIBConnection();
				setProperties();
				this.iibNode.setNodeName(this.node);
				brokerNotRunning();

			}
						
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			log.error("ConfigManagerProxy Not Initialized: " + e.getMessage());
			createIIBConnection();
			setProperties();
			brokerNotRunning();
			
		} catch (Exception e) {
			log.error("Not connected to IIB node: " + e.getMessage());
			createIIBConnection();
			setProperties();
			brokerNotRunning();
		}
		
		
	}

	/*
	 * Indicate the the broker is not running
	 */
	private void brokerNotRunning() {	
		this.iibNode.notRunning();
		this.iibExecutionGroups.notRunning();
		this.iibApplications.notRunning();
		this.iibMessageFlows.notRunning();
		
	}
	
	// Get the metrics
	private void getMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		getNodeMetrics();
		getExecutionGroups();
		getApplications();
		getMessageFlows();
		
	}

	
	// Get the IIB node metrics and set the name of the broker
	private void getNodeMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		this.brokerName = this.iibNode.getNodeName();
		this.iibNode.getIIBNodeName();
		this.iibNode.getNodeMetrics();
		
		this.iibExecutionGroups.setNodeName(this.brokerName);
		this.iibApplications.setNodeName(this.brokerName);
		this.iibMessageFlows.setNodeName(this.brokerName);
		
	}
	
	// Get the Execution Group metrics (IntegrationServer)
	private void getExecutionGroups() throws ConfigManagerProxyPropertyNotInitializedException {
		this.iibExecutionGroups.getExecutionMetrics();
		
	}
	
	// Get the Application metrics
	private void getApplications() throws ConfigManagerProxyPropertyNotInitializedException {
		this.iibApplications.getApplicationMetrics();
		
	}

	// Get the MessageFlow metrics
	private void getMessageFlows() throws ConfigManagerProxyPropertyNotInitializedException {
		this.iibMessageFlows.getMessageFlowMetrics();
		
	}
	
	
	// Create a connection to the IIB node
	private void createIIBConnection() {
		setConnectionDetails();
		boolean useCredentials = validateCredentials();
		
		/*
		 * if we are using credential, then connect with them, together if the connection is over SSL 
		 */
		if (useCredentials) {
			this.bconn = new IntegrationNodeConnectionParameters(this.hostName, this.port, this.userid, this.password, this.useSSL);		
			
		} else {
			this.bconn = new IntegrationNodeConnectionParameters(this.hostName, this.port);		
		}
	}
	
	
	// create an instance of the broker proxy and set 
	private void setProperties() {
		if (this._debug) {
			log.info("Host: " + this.hostName);
			log.info("Port: " + this.port);
		}
		
		try {
			this.bp = BrokerProxy.getInstance(this.bconn);
			this.iibNode.setBrokerProxy(this.bp);
			//this.iibNode.SetNodeProperties();
			
			this.iibExecutionGroups.setBrokerProxy(this.bp);
			this.iibApplications.setBrokerProxy(this.bp);
			this.iibMessageFlows.setBrokerProxy(this.bp);
			
			if (!this.iibNode.getIIBNodeName().equals(this.node)) {
				log.error("IIB Node name mismatch"); 
				System.exit(3);
			}
		} catch (ConfigManagerProxyLoggedException e) {
			this.bp = null;
			this.iibNode.setNodeName(this.node);
			
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			log.error("Unknown IIB node: " + e.getMessage());
			System.exit(4);
			
		}

	}
	
	// Get the host name and port
	private void setConnectionDetails() {
		
		// If null or blank ... error
		if (!(this.node != null && !this.node.isEmpty())) {
			log.error("IIB Node name is missing or is null.");
			System.exit(1);
		}
		
		// Split the host and port number from the connName ... host(port)
		if (!this.connName.equals("")) {
			Pattern pattern = Pattern.compile("^([^()]*)\\(([^()]*)\\)(.*)$");
			Matcher matcher = pattern.matcher(this.connName);	
			if (matcher.matches()) {
				this.hostName = matcher.group(1).trim();
				this.port = Integer.parseInt(matcher.group(2).trim());
			} else {
				log.error("While attempting to connect to IIB node, the connName is invalid ");
				System.exit(2);				
			}
		} else {
			log.error("While attempting to connect to IIB node, the connName is missing  ");
			System.exit(2);
			
		}
	
	}

	// Get the host name and port
	private boolean validateCredentials() {
		boolean ret = false;
	
		if ((!StringUtils.isEmpty(this.userid)) && (!StringUtils.isEmpty(this.password))) {
			ret = true;
		}
		
		return ret;
		
	}
	
	
	@PreDestroy
	private void disconnect() {
		if (this._debug) { log.info("Disconnecting from IIB node"); }

		if (this.bp != null) {
			this.bp.disconnect();
		}
	}

	
}
