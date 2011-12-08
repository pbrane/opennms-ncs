/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.ncs;

import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.drools.DroolsCorrelationEngine;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class AnyDependencyRulesTest extends CorrelationRulesTestCase {
	
	@Autowired
	NCSComponentRepository m_repository;
	
	@Autowired
	DistPollerDao m_distPollerDao;
	
	@Autowired
	NodeDao m_nodeDao;

	int m_pe1NodeId;
	
	int m_pe2NodeId;

	@Before
	public void setUp() {
		
		OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
		
		m_distPollerDao.save(distPoller);
		
		
		NetworkBuilder bldr = new NetworkBuilder(distPoller);
		bldr.addNode("PE1").setForeignSource("space").setForeignId("1111-PE1");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
		m_pe1NodeId = bldr.getCurrentNode().getId();
		
		bldr.addNode("PE2").setForeignSource("space").setForeignId("2222-PE2");
		
		m_nodeDao.save(bldr.getCurrentNode());
		
		m_pe2NodeId = bldr.getCurrentNode().getId();
		
		NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
		.setName("CokeP2P")
		.pushComponent("ServiceElement", "NA-ServiceElement", "8765")
			.setName("PE1:SE1")
			.setNodeIdentity("space", "1111-PE1")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnIf")
				.setName("jnxVpnIf")
				.setNodeIdentity("space", "1111-PE1")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfType", "5")
				.setAttribute("jnxVpnIfName", "ge-1/0/2.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:link")
					.setName("link")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-1/0/2")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setNodeIdentity("space", "1111-PE1")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwType", "5")
				.setAttribute("jnxVpnPwName", "ge-1/0/2.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspA-PE1-PE2")
					.setName("lspA-PE1-PE2")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE1-PE2")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspB-PE1-PE2")
					.setName("lspB-PE1-PE2")
					.setNodeIdentity("space", "1111-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE1-PE2")
				.popComponent()
			.popComponent()
		.popComponent()
		.pushComponent("ServiceElement", "NA-ServiceElement", "9876")
			.setName("PE2:SE1")
			.setNodeIdentity("space", "2222-PE2")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnIf")
				.setName("jnxVpnIf")
				.setNodeIdentity("space", "2222-PE2")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfType", "5")
				.setAttribute("jnxVpnIfName", "ge-3/1/4.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:link")
					.setName("link")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-3/1/4")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setNodeIdentity("space", "2222-PE2")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwType", "5")
				.setAttribute("jnxVpnPwName", "ge-3/1/4.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspA-PE2-PE1")
					.setName("lspA-PE2-PE1")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE2-PE1")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspB-PE2-PE1")
					.setName("lspB-PE2-PE1")
					.setNodeIdentity("space", "2222-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE2-PE1")
				.popComponent()
			.popComponent()
		.popComponent()
		.get();
		
		m_repository.save(svc);

	}
    
    @Test
    public void testDependencyRules() throws Exception {
        
        // Get engine
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");
        
        // Antecipate down event
        getAnticipator().reset();
        anticipate(  createComponentImpactedEvent( "ServiceElementComponent", "NA-SvcElemComp", "8765:lspA-PE1-PE2", 17 ) );
        //anticipate(  createComponentImpactedEvent( "ServiceElement", "NA-ServiceElement", "9876", 17 ) );
        //anticipate(  createComponentImpactedEvent( "Service", "NA-Service", "123", 17 ) );
		
		// Generate down event
		Event event = createMplsLspPathDownEvent( m_pe1NodeId, "10.1.1.1", "lspA-PE1-PE2" );
		event.setDbid(17);
		System.err.println("SENDING MplsLspPathDown EVENT!!");
		engine.correlate( event );
		
		// Check down event
		getAnticipator().verifyAnticipated();
		
		/*
		// Antecipate up event
        getAnticipator().reset();
        anticipate(  createComponentResolvedEvent( "ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)", 17 ) );
        anticipate(  createComponentResolvedEvent( "ServiceElement", "NA-ServiceElement", "9876", 17 ) );
        anticipate(  createComponentResolvedEvent( "Service", "NA-Service", "123", 17 ) );
        
        // Generate up event
        event = createVpnPwUpEvent( m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );
        event.setDbid(17);
        System.err.println("SENDING VpnPwUp EVENT!!");
        engine.correlate( event );
        
        // Check up event
        getAnticipator().verifyAnticipated();	
        */
	
    }
    
	

    private Event createVpnPwDownEvent( int nodeid, String ipaddr, String pwtype, String pwname ) {
		
		return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown", "Drools")
				.setNodeid(nodeid)
				.setInterface( addr( ipaddr ) )
				.addParam("jnxVpnPwType", pwtype )
				.addParam("jnxVpnPwName", pwname )
				.getEvent();
	}
    
    private Event createMplsLspPathDownEvent( int nodeid, String ipaddr, String lspname ) {
        
        return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown", "Drools")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
    }
    
    

    private Event createVpnPwUpEvent( int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        return new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp", "Drools")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("jnxVpnPwType", pwtype )
                .addParam("jnxVpnPwName", pwname )
                .getEvent();
    }

    // Currently unused
//    private Event createRootCauseEvent(int symptom, int cause) {
//        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
//    }
	
	private Event createComponentImpactedEvent( String type, String foreignSource, String foreignId, int cause ) {
        
        return new EventBuilder("uei.opennms.org/internal/ncs/componentImpacted", "Drools")
        .addParam("componentType", type )
        .addParam("componentForeignSource", foreignSource )
        .addParam("componentForeignId", foreignId )
        .addParam("CAUSE", cause )
        .getEvent();
    }
	
	private Event createComponentResolvedEvent(String type, String foreignSource, String foreignId, int cause) {
        return new EventBuilder("uei.opennms.org/internal/ncs/componentResolved", "Drools")
        .addParam("componentType", type )
        .addParam("componentForeignSource", foreignSource )
        .addParam("componentForeignId", foreignId )
        .addParam("CAUSE", cause )
        .getEvent();
    }


    public Event createNodeDownEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_DOWN_EVENT_UEI, nodeid);
    }
    
    public Event createNodeUpEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_UP_EVENT_UEI, nodeid);
    }
    
    public Event createNodeLostServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeLostService", nodeid, ipAddr, svcName);
    }
    
    public Event createNodeRegainedServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeRegainedService", nodeid, ipAddr, svcName);
    }
    
    private Event createSvcEvent(String uei, int nodeid, String ipaddr, String svcName)
    {
    	return new EventBuilder(uei, "Drools")
    		.setNodeid(nodeid)
    		.setInterface( addr( ipaddr ) )
    		.setService( svcName )
    		.getEvent();
    		
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    



}