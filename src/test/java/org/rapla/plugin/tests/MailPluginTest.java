/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.plugin.tests;

import java.util.Locale;

import org.rapla.MockMailer;
import org.rapla.ServletTestBase;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.logger.Logger;
import org.rapla.plugin.mail.MailToUserInterface;
import org.rapla.plugin.mail.server.MailInterface;
import org.rapla.plugin.mail.server.RaplaMailToUserOnLocalhost;
import org.rapla.server.ServerService;
import org.rapla.server.internal.ServerServiceImpl;

/** listens for allocation changes */
public class MailPluginTest extends ServletTestBase {
    ServerServiceImpl raplaServer;

    ClientFacade facade1;
    Locale locale;


    public MailPluginTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
       
       // start the server
        //ServerServiceContainer container = getContainer().lookupDeprecated(ServerServiceContainer.class, getStorageName());
        raplaServer = this.getContainer();
        // start the client service
        facade1 =  getContainer().lookupDeprecated(ClientFacade.class, "remote-facade");
        facade1.login("homer","duffs".toCharArray());
        locale = Locale.getDefault();
    }
    
    protected String getStorageName() {
        return "storage-file";
    }
    
    protected void tearDown() throws Exception {
        facade1.logout();
        super.tearDown();
    }
    
    public void test() throws Exception 
    {
        MockMailer mailMock = (MockMailer) raplaServer.lookupDeprecated(MailInterface.class, null);
        final ClientFacade facade = getContext().lookup(ClientFacade.class);
        Logger logger = getContext().lookup(Logger.class);
    	MailToUserInterface mail = new RaplaMailToUserOnLocalhost(mailMock, facade, logger);
        mail.sendMail( "homer","Subject", "MyBody");

        Thread.sleep( 1000);

        assertNotNull( mailMock.getMailBody() );
   
    }
    
 
}

