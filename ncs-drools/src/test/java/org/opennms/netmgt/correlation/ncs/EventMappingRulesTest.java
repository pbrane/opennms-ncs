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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import org.springframework.test.annotation.DirtiesContext;

public class EventMappingRulesTest extends CorrelationRulesTestCase {
	
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
    @DirtiesContext
    public void testMapPwDown() throws Exception {
		
		Event event = createVpnPwDownEvent(17, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );

		testEventMapping(event, ComponentDownEvent.class, "ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)");

    }


	@Test
    @DirtiesContext
    public void testMapPwUp() throws Exception {

		Event event = createVpnPwUpEvent(27, m_pe2NodeId, "10.1.1.1", "5", "ge-3/1/4.50" );

		testEventMapping(event, ComponentUpEvent.class, "ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)");

    }
    
	@Test
    @DirtiesContext
    public void testMapMplsLspPathDown() throws Exception {
		
		Event event = createMplsLspPathDownEvent(37, m_pe2NodeId, "10.1.1.1", "lspA-PE2-PE1");

		testEventMapping(event, ComponentDownEvent.class, "ServiceElementComponent", "NA-SvcElemComp", "9876:lspA-PE2-PE1");

	}
    
	@Test
    @DirtiesContext
    public void testMapMplsLspPathUp() throws Exception {
		
		Event event = createMplsLspPathUpEvent(37, m_pe2NodeId, "10.1.1.1", "lspA-PE2-PE1");

		testEventMapping(event, ComponentUpEvent.class, "ServiceElementComponent", "NA-SvcElemComp", "9876:lspA-PE2-PE1");
    }
    
	private void testEventMapping(Event event, Class<? extends ComponentEvent> componentEventClass,	String componentType, String componentForeignSource, String componentForeignId) {
		// Get engine
        DroolsCorrelationEngine engine = findEngineByName("eventMappingRules");
        
        assertEquals("Expected nothing but got " + engine.getMemoryObjects(), 0, engine.getMemorySize());
        
		engine.correlate( event );
		
		List<Object> memObjects = engine.getMemoryObjects();

		assertEquals("Unexpected size of workingMemory " + memObjects, 1, memObjects.size());

		Object eventObj = memObjects.get(0);

		assertTrue( componentEventClass.isInstance(eventObj) );
		assertTrue( eventObj instanceof ComponentEvent );
		
		ComponentEvent c = (ComponentEvent) eventObj;
		
		assertSame(event, c.getEvent());
		
		Component component = c.getComponent();
		assertEquals(componentType, component.getType());
		assertEquals(componentForeignSource, component.getForeignSource());
		assertEquals(componentForeignId, component.getForeignId());
	}
    

    // dependencies must be loaded when needed by propagation rules
    // loaded deps needed by multiple events should not load more than once
    // deps no longer needed by one event should remain loaded if need by others
    // deps no longer needed by any event should be unloaded

    // propagate outages to 'dependsOn' parents
    // propagate outages to 'dependsOnAny' parents when ALL children are down
    // resolve outages in 'dependsOn' parents when dependsOn children are resolved
    // resolve outage in 'dependsOnAny' parents when ANY child is resolved

    // map various events to outages and resolutions
    // ignore duplicate cause events
    // ignore duplicate resolution events
    
    private Event createMplsLspPathDownEvent( int dbId, int nodeid, String ipaddr, String lspname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
        
        event.setDbid(dbId);
		return event;
    }
    
    private Event createMplsLspPathUpEvent( int dbId, int nodeid, String ipaddr, String lspname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp", "Drools")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("mplsLspName", lspname )
                .getEvent();
        event.setDbid(dbId);
		return event;
    }


    private Event createVpnPwDownEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
		
		Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown", "Test")
				.setNodeid(nodeid)
				.setInterface( addr( ipaddr ) )
				.addParam("jnxVpnPwType", pwtype )
				.addParam("jnxVpnPwName", pwname )
				.getEvent();
		event.setDbid(dbId);
		return event;
	}

    private Event createVpnPwUpEvent( int dbId, int nodeid, String ipaddr, String pwtype, String pwname ) {
        
        Event event = new EventBuilder("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp", "Test")
                .setNodeid(nodeid)
                .setInterface( addr( ipaddr ) )
                .addParam("jnxVpnPwType", pwtype )
                .addParam("jnxVpnPwName", pwname )
                .getEvent();
        event.setDbid(dbId);
		return event;
    }


}