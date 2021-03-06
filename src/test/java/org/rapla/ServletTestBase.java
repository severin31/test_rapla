package org.rapla;

import junit.framework.TestCase;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.server.MainServlet;
import org.rapla.server.internal.ServerServiceImpl;

import java.io.File;

@SuppressWarnings("restriction")
public abstract class ServletTestBase extends TestCase
{
	MainServlet mainServlet;
    //private Server jettyServer;
    public static String TEST_SRC_FOLDER_NAME="test-src";
    public static String WAR_SRC_FOLDER_NAME="war";
    protected int port = 8052;
    
    final public static String WEBAPP_FOLDER_NAME = RaplaTestCase.TEST_FOLDER_NAME  + "/webapp";
    final public static String WEBAPP_INF_FOLDER_NAME = WEBAPP_FOLDER_NAME + "/WEB-INF";
  
    public ServletTestBase( String name )
    {
        super( name );
        new File("temp").mkdir();
        File testFolder =new File(RaplaTestCase.TEST_FOLDER_NAME);
        testFolder.mkdir();
    }

    protected void setUp() throws Exception
    {
//        File webappFolder = new File(WEBAPP_FOLDER_NAME);
//		IOUtil.deleteAll( webappFolder );
//        new File(WEBAPP_INF_FOLDER_NAME).mkdirs();
//        
//        IOUtil.copy( TEST_SRC_FOLDER_NAME + "/test.xconf", WEBAPP_INF_FOLDER_NAME + "/raplaserver.xconf" );
//        //IOUtil.copy( "test-src/test.xlog", WEBAPP_INF_FOLDER_NAME + "/raplaserver.xlog" );
//        IOUtil.copy( TEST_SRC_FOLDER_NAME + "/testdefault.xml", WEBAPP_INF_FOLDER_NAME + "/test.xml" );
//        IOUtil.copy( WAR_SRC_FOLDER_NAME + "/WEB-INF/web.xml", WEBAPP_INF_FOLDER_NAME + "/web.xml" );
//        
//        
//        jettyServer =new Server(port);
//        WebAppContext context = new WebAppContext( jettyServer,"rapla","/" );
//        context.setResourceBase(  webappFolder.getAbsolutePath() );
//        context.setMaxFormContentSize(64000000);
//        //MainServlet.serverContainerHint=getStorageName();
//        
//      //  context.addServlet( new ServletHolder(mainServlet), "/*" );
//        jettyServer.start();
//        Handler[] childHandlers = context.getChildHandlersByClass(ServletHandler.class);
//        ServletHolder servlet = ((ServletHandler)childHandlers[0]).getServlet("RaplaServer");
//        mainServlet = (MainServlet) servlet.getServlet();
//        URL server = new URL("http://127.0.0.1:8052/rapla/ping");
//        HttpURLConnection connection = (HttpURLConnection)server.openConnection();
//        int timeout = 10000;
//        int interval = 200;
//        for ( int i=0;i<timeout / interval;i++)
//        {
//            try
//            {
//                connection.connect();
//            } 
//            catch (ConnectException ex) {
//                Thread.sleep(interval);
//            }
//        }
    }
    
    protected RaplaContext getContext()
    {
        throw new IllegalStateException();
        //return mainServlet.getContext();
    }
    
    protected ServerServiceImpl getContainer()
    {
        throw new IllegalStateException();
        //return mainServlet.getContainer();
    }


	protected <T> T getService(Class<T> role) throws RaplaException {
	    return getContext().lookup( role);
	}

	protected RaplaLocale getRaplaLocale() throws Exception {
        return getContext().lookup(RaplaLocale.class);
    }

    
    protected void tearDown() throws Exception
    {
        //jettyServer.stop();
        super.tearDown();
    }
    
    protected String getStorageName() {
        return "storage-file";
    }
 }
