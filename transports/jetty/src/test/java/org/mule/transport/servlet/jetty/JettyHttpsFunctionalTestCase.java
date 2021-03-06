/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.servlet.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.module.client.MuleClient;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.functional.HttpFunctionalTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.runners.Parameterized.Parameters;

public class JettyHttpsFunctionalTestCase extends HttpFunctionalTestCase
{

    public JettyHttpsFunctionalTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "jetty-https-functional-test-service.xml"},
            {ConfigVariant.FLOW, "jetty-https-functional-test-flow.xml"}
        });
    }      
    
    @Override
    public void testSend() throws Exception
    {
        final FlowConstruct testSedaService = muleContext.getRegistry().lookupFlowConstruct("testComponent");
        FunctionalTestComponent testComponent = (FunctionalTestComponent) getComponent(testSedaService);
        assertNotNull(testComponent);

        final AtomicBoolean callbackMade = new AtomicBoolean(false);
        EventCallback callback = new EventCallback()
        {
            public void eventReceived(MuleEventContext context, Object component) throws Exception
            {
                assertTrue(callbackMade.compareAndSet(false, true));
                MuleMessage msg = context.getMessage();
                assertEquals(TEST_MESSAGE, msg.getPayloadAsString());
            }
        };

        testComponent.setEventCallback(callback);

        MuleClient client = new MuleClient(muleContext);
        Map<String, String> props = new HashMap<String, String>();
        props.put(HttpConstants.HEADER_CONTENT_TYPE, "text/plain;charset=UTF-8");
        MuleMessage result = client.send("clientEndpoint", TEST_MESSAGE, props);
        assertNotNull(result);
        assertEquals(TEST_MESSAGE + " Received", result.getPayloadAsString());
        assertTrue("Callback never fired", callbackMade.get());
    }
}
