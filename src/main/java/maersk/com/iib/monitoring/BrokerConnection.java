package maersk.com.iib.monitoring;

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

    @Value("${application.debug}")
    private boolean _debug;

    @Value("${ibm.iib.node}")
    private String node;
    
    @Value("${ibm.iib.connName}")
    private String connName;
    
    private String hostName;
    private int port;
    
    @Value("${ibm.iib.event.delayInMilliSeconds}")
    private long resetIterations;

    private BrokerConnectionParameters bcon = null;
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

	@Scheduled(fixedDelayString="${ibm.iib.event.delayInMilliSeconds}")
	public void Scheduler() {
		
		if (this._debug) {log.info("IIB Broker stats");}

		try {

			if (this.bcon != null) {
				if (this.bp != null) {
					if (!this.bp.isRunning()) {
						SetProperties();
					}
					GetMetrics();
					
				} else {
					CreateIIBConnection();
					SetProperties();
					this.iibNode.setNodeName(this.node);
					NotRunning();
					
				}
			} else {
				CreateIIBConnection();
				SetProperties();
				this.iibNode.setNodeName(this.node);
				NotRunning();

			}
						
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			log.error("ConfigManagerProxy Not Initialized: " + e.getMessage());
			CreateIIBConnection();
			SetProperties();
			NotRunning();
			
		} catch (Exception e) {
			log.error("Not connected to IIB node: " + e.getMessage());
			CreateIIBConnection();
			SetProperties();
			NotRunning();
		}
		
		
	}

	private void NotRunning() {
		
		log.info("IIB NOT RUNNING");
		
		this.iibNode.NotRunning();
		this.iibExecutionGroups.NotRunning();
		this.iibApplications.NotRunning();
		this.iibMessageFlows.NotRunning();
		
	}
	// Get the metrics
	private void GetMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
	
		GetNodeMetrics();
		GetExecutionGroups();
		GetApplications();
		GetMessageFlows();
		
	}

	
	// Get the IIB node metrics and set the name of the broker
	private void GetNodeMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		
		this.brokerName = this.iibNode.GetNodeName();
		this.iibNode.GetNodeMetrics();
		
		this.iibExecutionGroups.setNodeName(this.brokerName);
		this.iibApplications.setNodeName(this.brokerName);
		this.iibMessageFlows.setNodeName(this.brokerName);
		
	}
	
	// Get the Execution Group metrics (IntegrationServer)
	private void GetExecutionGroups() throws ConfigManagerProxyPropertyNotInitializedException {
		
		this.iibExecutionGroups.SetExecutionMetrics();
		
	}
	
	// Get the Application metrics
	private void GetApplications() throws ConfigManagerProxyPropertyNotInitializedException {
		
		this.iibApplications.GetApplicationMetrics();
		
	}

	// Get the MessageFlow metrics
	private void GetMessageFlows() throws ConfigManagerProxyPropertyNotInitializedException {
		
		this.iibMessageFlows.GetMessageFlowMetrics();
		
	}
	
	
	// Create a connection to the IIB node
	private void CreateIIBConnection() {
		
		GetEnvironmentVariables();
		this.bcon = new IntegrationNodeConnectionParameters(this.hostName, this.port);		
		
	}
	
	
	// create an instance of the broker proxy and set 
	private void SetProperties() {
	
		if (this._debug) {
			log.info("Host: " + this.hostName);
			log.info("Port: " + this.port);
		}
		
		try {
			this.bp = BrokerProxy.getInstance(this.bcon);
			this.iibNode.setBrokerProxy(this.bp);
			//this.iibNode.SetNodeProperties();
			
			this.iibExecutionGroups.setBrokerProxy(this.bp);
			this.iibApplications.setBrokerProxy(this.bp);
			this.iibMessageFlows.setBrokerProxy(this.bp);
			
			if (!this.iibNode.GetNodeName().equals(this.node)) {
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
	private void GetEnvironmentVariables() {
		
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
	
	@PreDestroy
	private void Disconnect() {
		if (this._debug) { log.info("Disconnecting from IIB node"); }

		if (this.bp != null) {
			this.bp.disconnect();
		}
	}

	
}
