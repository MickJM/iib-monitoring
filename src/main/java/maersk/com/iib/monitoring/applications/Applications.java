package maersk.com.iib.monitoring.applications;

/*
 * Get the 'application' metrics from an IIB node
 * 
 */

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.AttributeConstants;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Meter.Id;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import maersk.com.iib.monitoring.IIBBase;

public class Applications extends IIBBase {
	
	@Autowired
	public CollectorRegistry registry;
	
	protected static final String lookupStatus = IIBPREFIX + "iibApplicationStatus";

    private static final int APP_RESET = 0;
	//private Map<String,AtomicInteger>iibApplications = new HashMap<String, AtomicInteger>();
	//private Map<String,Gauge>iibApplicationsGauge = new HashMap<String, Gauge>();
    
    public Applications() {
    	super();
    }
    
    // For each execution group, 
	public void getApplicationMetrics() throws ConfigManagerProxyPropertyNotInitializedException {
	
		Enumeration<ExecutionGroupProxy> egroups = this.bp.getExecutionGroups(null);
		List<ExecutionGroupProxy> egps = Collections.list(egroups);
        resetMetrics();
		
		for (ExecutionGroupProxy egroup: egps) {

			String egName = egroup.getName().trim();
			if (this._debug) { log.info("Applications: egName:" + egName); }

	        Properties p = new Properties();
	        p.setProperty(AttributeConstants.NAME_PROPERTY, egName);

	        Enumeration<ApplicationProxy> ap = egroup.getApplications(null);
			List<ApplicationProxy> apps = Collections.list(ap);
			for (ApplicationProxy app: apps) {
				
				String appName = app.getName().trim();
				if (this._debug) { 
					log.info("Applications: appName:" + appName);
					log.info("Applications: status :" + app.isRunning());
				}
				
				int val = IIBMONConstants.APP_NOT_RUNNING;
		        if (app.isRunEnabled()) {
		        	val = IIBMONConstants.APP_IS_RUN_ENABLED;
		        }
		        if (app.isRunning()) {
		        	val = IIBMONConstants.APP_IS_RUNNING;
		        }
		        //setMetric(val, appName, egName);
		        setGaugeMap(val, appName, egName);
		        
			}
		}
	}
	
	private void setGaugeMap(int val, String appName, String egName) throws ConfigManagerProxyPropertyNotInitializedException {

		String nodeName = getNodeName();

		/*
		//String name = appName + "_" + egName;
		String key = new StringBuilder()
				.append(IIBPREFIX)
				.append("iibApplicationStatus")
				.append(nodeName)
				.append(egName)
				.append(appName)
				.toString();
		String name = new StringBuilder()
				.append(IIBPREFIX)
				.append("iibApplicationStatus")
				.toString();
				
		String[] labels = {"iibNodeName", "integrationServerName", "applicationName"};
		
		String[] values = {nodeName, egName, appName};

		/*
		MultiGauge m = MultiGauge.builder("statuses")
			.tag("job", "dirty")
	        .description("Testing")
	        .register(meterRegistry);
		
		*/
		
		//meterRegistry.gauge("iib:Application", 10.0);
		meterRegistry.gauge("iib:Application", 
				Tags.of("iibNodeName", getNodeName(),
				"integrationServerName", egName,
				"applicationName",appName)
				,val);
		
		
		/*
		if (!iibApplicationsGauge.containsKey(key)) {
			iibApplicationsGauge.put(name, 

					
					Gauge.build()						
						.help(new StringBuilder()
								.append(IIBPREFIX)
								.append("iibApplicationStatus")
								.toString()) 
						.name(name)
						
						.labelNames(labels)
			//			.create());
						.register(registry));		
		}
		*/
		//Gauge g = iibApplicationsGauge.get(name);
		//g.labels(values).set(val);

	
		
	}
	
	private void setMetric(int val, String appName, String egName) {
		
		/*
		String name = appName + "_" + egName;
		AtomicInteger a = iibApplications.get(name);
		if (a == null) {
			iibApplications.put(name, 
				Metrics.gauge(new StringBuilder()
				.append(IIBPREFIX)
				.append("iibApplicationStatus")
				.toString(),  
				Tags.of("iibNodeName", getNodeName(),
						"integrationServerName", egName,
						"applicationName", appName),
			new AtomicInteger(val)));
		} else {
			a.set(val);
		}        
		*/
		
	}
	
	public void notRunning() {
		setMetricValues(IIBMONConstants.APP_NOT_RUNNING);
	}
	
	public void resetMetrics() {
		setMetricValues(IIBMONConstants.APP_RESET);
	}
	
	
	// Not running, so for any entries in the applications list - set the values to '0' (zero) or -1
	// ... the values will not disappear, since Guages are either 'set' or 'not set'
	// '0'  - IIB applications are not running
	// '-1' - IIB application metrics are reset
	//
	public void setMetricValues(int val) {

		DeleteMetricEntry(lookupStatus);
		
		/*
		Iterator<Entry<String, AtomicInteger>> listListener = this.iibApplications.entrySet().iterator();
		while (listListener.hasNext()) {
	        Map.Entry pair = (Map.Entry)listListener.next();
	        String key = (String) pair.getKey();
	        try {
				AtomicInteger i = (AtomicInteger) pair.getValue();
				if (i != null) {
					i.set(val);
				}
	        } catch (Exception e) {
	        }
		}
		*/
		
		
	}

}
