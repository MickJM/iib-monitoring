package maersk.com.iib.monitoring;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
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

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.IntegrationNodeConnectionParameters;
import com.ibm.broker.config.proxy.MessageFlowProxy;

import io.prometheus.client.CollectorRegistry;
import maersk.com.iib.monitoring.applications.Applications;
import maersk.com.iib.monitoring.executiongroup.ExecutionGroup;
import maersk.com.iib.monitoring.messageflows.MessageFlows;
import maersk.com.iib.monitoring.node.IIBNode;

@Component
public class BrokerConnection {

    private Logger log = LogManager.getLogger(this.getClass());
    
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
		log.info("IIB Broker stats");

		
		try {

			if (this.bcon != null) {
				if (!this.bp.isRunning()) {
					SetProperties();
				}
				GetMetrics();
				
			} else {
				CreateIIBConnection();
				SetProperties();
			}
						
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			log.error("ConfigManagerProxy Not Initialized: " + e.getMessage());
			CreateIIBConnection();
			SetProperties();
			
		} catch (Exception e) {
			log.error("Not connected to IIB node: " + e.getMessage());
			CreateIIBConnection();
			SetProperties();
		}
		
		
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
	
	
	// create an instance of the brokerproxy and set 
	private void SetProperties() {
	
		try {
			this.bp = BrokerProxy.getInstance(this.bcon);
			this.iibNode.setBrokerProxy(this.bp);
			this.iibExecutionGroups.setBrokerProxy(this.bp);
			this.iibApplications.setBrokerProxy(this.bp);
			this.iibMessageFlows.setBrokerProxy(this.bp);
			
		} catch (ConfigManagerProxyLoggedException e) {
			this.bp = null;
		}

	}
	
	// Get the host name and port
	private void GetEnvironmentVariables() {
		
		// Split the host and port number from the connName ... host(port)
		if (!this.connName.equals("")) {
			Pattern pattern = Pattern.compile("^([^()]*)\\(([^()]*)\\)(.*)$");
			Matcher matcher = pattern.matcher(this.connName);	
			if (matcher.matches()) {
				this.hostName = matcher.group(1).trim();
				this.port = Integer.parseInt(matcher.group(2).trim());
			} else {
				log.error("While attempting to connect to IIB node, the connName is invalid ");
				System.exit(1);				
			}
		} else {
			log.error("While attempting to connect to IIB node, the connName is missing  ");
			System.exit(1);
			
		}
	
	}
	
	@PreDestroy
	private void Disconnect() {
        log.info("Disconnecting from IIB node");

		if (this.bp != null) {
			this.bp.disconnect();
		}
	}

	
}
