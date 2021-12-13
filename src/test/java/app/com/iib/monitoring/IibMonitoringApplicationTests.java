package app.com.iib.monitoring;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;

import app.com.iib.monitoring.node.IIBNode;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Meter.Id;

import org.springframework.beans.factory.annotation.Autowired;

import app.com.iib.monitoring.IIBBase.IIBMONConstants;
import app.com.iib.monitoring.applications.Applications;
import app.com.iib.monitoring.executiongroup.ExecutionGroup;
import app.com.iib.monitoring.messageflows.MessageFlows;
import app.com.iib.monitoring.node.IIBNode;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { IibMonitoringApplication.class } )
@ActiveProfiles("test")
public class IibMonitoringApplicationTests extends IIBBase {

	static Logger log = LoggerFactory.getLogger(IibMonitoringApplicationTests.class);
	
	@Autowired
	private BrokerConnection brokerconnection;

	@Test
	@Order(1)
	public void contextLoads() throws Exception {
		assertThat(brokerconnection).isNotNull();

		log.info("Thread sleeping for 5 seconds to ensure application thread fully loads ...");
		Thread.sleep(5000);
		assertThat(brokerconnection.IIBNode()).isNotNull();
		
		String brokerName = brokerconnection.IIBNode().getNodeName();
		log.info("IIB Node name : {}", brokerName);

	}

	@Test
	@Order(2)
	public void listBrokerDetails() throws Exception {
		assertThat(brokerconnection).isNotNull();
		
		log.info("Thread sleeping for 12 seconds to ensure application thread fully loads ...");
		Thread.sleep(12000);
		assertThat(brokerconnection.IIBNode()).isNotNull();
		
		String brokerName = brokerconnection.IIBNode().getNodeName();
		log.info("IIB Node name : {}", brokerName);

		List<Meter.Id> filter = meterRegistry.getMeters().stream()
		        .map(Meter::getId)
		        .collect(Collectors.toList());
		
		int iib_apps  = 0;
		int iib_msgFlows  = 0;
		int iib_integrationNodes = 0;
		int iib_nodes = 0;
		int iib_other = 0;
		
		Iterator<Id> list = filter.iterator();
		while (list.hasNext()) {
			Meter.Id id = list.next();
			switch (id.getName()) {
				case "iib:Application":
					iib_apps++;
					break;
				case "iib:iibMessageFlow":
					iib_msgFlows++;
					break;
				case "iib:iibIntegrationServerStatus":
					iib_integrationNodes++;
					break;
				case "iib:iibNodeStatus":
					iib_nodes++;
					break;
					
					
				default:
					iib_other++;
					break;
			}
			List<Tag> tags = id.getTags();
			
			
		}

		log.info("Number of Nodes              : {}", iib_nodes);
		log.info("Number of IntegrationServers : {}", iib_integrationNodes);
		log.info("Number of Applications       : {}", iib_apps);
		log.info("Number of MessageFlows       : {}", iib_msgFlows);

		assertThat(iib_nodes).isGreaterThan(0);
		assertThat(iib_integrationNodes).isGreaterThan(0);
		assertThat(iib_apps).isGreaterThan(0);
		assertThat(iib_msgFlows).isGreaterThan(0);

		

	}
	
}
