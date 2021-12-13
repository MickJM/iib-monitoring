package app.com.iib.monitoring;

/*
 * IIB Monitoring API
 *
 * Developed to monitor IIB within SMQ
 * This API can only monitor IIB v10, due to the way IBM have created their APIs
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

import app.com.iib.monitoring.applications.Applications;
import app.com.iib.monitoring.executiongroup.ExecutionGroup;
import app.com.iib.monitoring.messageflows.MessageFlows;
import app.com.iib.monitoring.node.IIBNode;

//import com.ibm.integration.admin.proxy.*;

import io.prometheus.client.CollectorRegistry;

@Component
public class BrokerConnection {

    private Logger log = LogManager.getLogger(this.getClass());

    @Value("${application.debug:false}")
    private boolean _debug;
    public boolean Debug() { return this._debug; }
    @Value("${ibm.iib.node}")
    private String node;
    public String Nodename() { return this.node; }
    
    @Value("${ibm.iib.connName}")
    private String connName;
    public String Connname() { return this.connName; }
    
    private String hostName;
    public String Hostname() { return this.hostName; }
    public void Hostname(String v) { this.hostName = v; }

    private int port;
    public int Port() { return this.port; }
    public void Port(int v) { this.port = v; }
    

    @Value("${ibm.iib.user:}")
    private String userid;
    public String Username() { return this.userid; }
    @Value("${ibm.iib.password:}")
    private String password;
    public String Password() { return this.password; }
    
    @Value("${ibm.iib.useSSL:false}")
    private boolean useSSL;
    public boolean UsingSSL() { return this.useSSL; }
    
    @Value("${ibm.iib.event.delayInMilliSeconds}")
    private long resetIterations;

    private BrokerConnectionParameters bconn = null;
	public BrokerConnectionParameters BConnection() { return this.bconn; }
	public void BConnection( BrokerConnectionParameters v ) { this.bconn = v; }

    private BrokerProxy bp = null;
	public BrokerProxy BProxy() { return this.bp; }
	public void BProxy( BrokerProxy v ) { this.bp = v; }
	
   // private String brokerName;
    
    @Autowired
    private IIBNode iibNode;
    public IIBNode IIBNode() { return this.iibNode; }
    
    @Autowired
    private ExecutionGroup iibExecutionGroups;
    public ExecutionGroup Groups() {return this.iibExecutionGroups;}

    @Autowired
    private Applications iibApplications;
    public Applications Apps() {return this.iibApplications;}

    @Autowired
    private MessageFlows iibMessageFlows;
    public MessageFlows MsgFlows() {return this.iibMessageFlows;}
    
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
		
		if (Debug()) {log.info("IIB Broker stats");}

		try {

			if (BConnection() != null) {
				if (BProxy() != null) {
					if (!BProxy().isRunning()) {
						setProperties();
					}
					getMetrics();
					
				} else {
					createIIBConnection();
					setProperties();
					createNode().setNodeName(Nodename());
					brokerNotRunning();
					
				}
			} else {
				createIIBConnection();
				setProperties();
				createNode().setNodeName(Nodename());
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
	 * Not running, so remove the metrics
	 */
	private void brokerNotRunning() {	
		if (IIBNode() != null) {
			IIBNode().notRunning();			
		}
		
		if (Groups() != null) {
			Groups().notRunning();
		}
		
		if (Apps() != null) {
			Apps().notRunning();
		}
		
		if (MsgFlows() != null) {
			MsgFlows().notRunning();
		}
	}
	
	// Get the metrics
	public void getMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		getNodeMetrics();
		getExecutionGroups();
		getApplications();
		getMessageFlows();
		
	}

	
	// Get the IIB node metrics and set the name of the broker
	private void getNodeMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
		String brokerName = IIBNode().getNodeName();
		IIBNode().getIIBNodeName();
		IIBNode().getNodeMetrics();
		
		Groups().setNodeName(brokerName);
		Apps().setNodeName(brokerName);
		MsgFlows().setNodeName(brokerName);
		
	}
	
	// Get the Execution Group metrics (IntegrationServer)
	private void getExecutionGroups() throws ConfigManagerProxyPropertyNotInitializedException {
		if (Groups() != null) {
			Groups().getExecutionMetrics();
		}
	}
	
	// Get the Application metrics
	private void getApplications() throws ConfigManagerProxyPropertyNotInitializedException {
		if (Apps() != null) {
			Apps().getApplicationMetrics();
		}
	}

	// Get the MessageFlow metrics
	private void getMessageFlows() throws ConfigManagerProxyPropertyNotInitializedException {
		if (MsgFlows() != null) {
			MsgFlows().getMessageFlowMetrics();
		}
	}
	
	
	// Create a connection to the IIB node
	public void createIIBConnection() {
		setConnectionDetails();
		boolean useCredentials = validateCredentials();
				
		/*
		 * if we are using credential, then connect with them, together if the connection is over SSL 
		 */
		if (Debug()) {
			log.info("Using credentials : {}", useCredentials);
			log.info("Username          : {}", Username());
			log.info("Password          : {}", Password() );
			log.info("SSL               : {}", UsingSSL());

		}
		if (useCredentials) {
			BConnection( new IntegrationNodeConnectionParameters(Hostname(), Port(), Username(), Password(), UsingSSL()));		
			
		} else {
			BConnection( new IntegrationNodeConnectionParameters(Hostname(), Port()));		
		}
	}
	
	/*
	 * ACE connection
	 */
	/*
	private void createACEConnection() throws IntegrationAdminException {
		
		IntegrationServerProxy isp = new IntegrationServerProxy("localhost", 7601, "aceadmin", "Passw0rd", false);

		//IntegrationServerProxy isp = new IntegrationServerProxy()
		//IntegrationServerProxy isp = new IntegrationServerProxy("IS01", 
		//		"c:\\Users\\mickm\\OneDrive\\Documents\\Development\\ACE\\workspaces\\IS01");

		//IntegrationServerProxy isp = new IntegrationServerProxy("localhost", 4417);
		//, "aceadmin", "Password", false);

		//Enumeration<ApplicationProxy> eg = isp.getAllApplications(false);
		
		List<ApplicationProxy> egps = isp.getAllApplications(true);
		
		for (ApplicationProxy egroup: egps) {

			String egName = egroup.getName().trim();
			log.info("Name = " + egName);
			
		}
		
		int i = 0;
	}
	*/
	
	// create an instance of the broker proxy and set 
	private void setProperties() {
		if (Debug()) {
			log.info("Host: " + Hostname());
			log.info("Port: " + Port());
		}
		
		try {
			BProxy(BrokerProxy.getInstance(BConnection()));
			IIBNode().setBrokerProxy(BProxy());			
			Groups().setBrokerProxy(BProxy());
			Apps().setBrokerProxy(BProxy());
			MsgFlows().setBrokerProxy(BProxy());
			
			if (!IIBNode().getIIBNodeName().equals(Nodename())) {
				log.error("IIB Node name mismatch"); 
				System.exit(3);
			}
		} catch (ConfigManagerProxyLoggedException e) {
			if (Debug()) {
				log.error("Error: " + e.getMessage());
			}
			BProxy(null);
			IIBNode().setNodeName(Nodename());
			
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			if (Debug()) {
				log.info("Unknown IIB node: " + e.getMessage());
			}
			System.exit(4);
			
		}

	}
	
	// Get the host name and port
	private void setConnectionDetails() {
		
		// If null or blank ... error
		if (!(Nodename() != null && !Nodename().isEmpty())) {
			log.error("IIB Node name is missing or is null.");
			System.exit(1);
		}
		
		// Split the host and port number from the connName ... host(port)
		if (!Connname().equals("")) {
			Pattern pattern = Pattern.compile("^([^()]*)\\(([^()]*)\\)(.*)$");
			Matcher matcher = pattern.matcher(Connname());	
			if (matcher.matches()) {
				Hostname(matcher.group(1).trim());
				Port(Integer.parseInt(matcher.group(2).trim()));
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
		if ((!StringUtils.isEmpty(Username())) && (!StringUtils.isEmpty(Password()))) {
			ret = true;
		}
		return ret;
	}
	
	@PreDestroy
	private void disconnect() {
		if (Debug()) { log.info("Disconnecting from IIB node"); }
		if (BProxy() != null) {
			BProxy().disconnect();
		}
	}

	
}
