package vn._123pay.bank;

import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.System.exit;
import java.sql.Connection;
import java.sql.SQLException;
import vn._123pay.server.jetty.Application;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.apache.commons.configuration.ConfigurationException;
import vn._123pay.configuration.Config;
import vn._123pay.data.dbms.nonRelational.NRDbClientManager;
import vn._123pay.data.dbms.relational.OracleClient;
import vn._123pay.data.dbms.relational.RDbClient;
import vn._123pay.data.dbms.relational.RDbClientManager;
import vn._123pay.data.dbms.relational.RDbClientName;
import vn._123pay.server.jetty.EmbededServer;
import vn._123pay.util.ExceptionUtil;
import vng.paygate.bank.jaxb.adapter.BoBIModuleConfigNew;
import vng.paygate.config.service.impl.BankServiceImpl;
import vng.paygate.config.service.impl.ConfigServiceImpl;
import vng.paygate.domain.common.CommonService;
import vng.paygate.security.SecurityHelper;

/**
 *
 * @author nghiant6
 */
@Path("/")
@Consumes(MediaType.TEXT_PLAIN)
@Produces("text/plain;charset=utf-8")
public final class Service extends Application {

    static final String NOTIFY_MERCHANT_API = "notifyMerchant";
    static final String VERIFY_CARD = "verifyCard";
    static final String VERIFY_OTP = "verifyOTP";
    public static ConfigServiceImpl configService;
    public static CommonService commonService = new CommonService();
    private static final String MODULE_NAME = System.getProperty("zname");
    private SecurityHelper securityH = new SecurityHelper();
    private BankServiceImpl bankService = new BankServiceImpl();

    public static void main(String[] args) throws SQLException, IOException, FileNotFoundException, ConfigurationException, JAXBException {

        // Init oracle
        String ucpMin = System.getProperty("ucpmin");
        if (ucpMin != null && ucpMin.isEmpty()) {
            ucpMin = null;
        }
        
        RDbClient oracle = new OracleClient(Config.get("oracle-url"),
                Config.get("oracle-uId"),
                Config.get("oracle-uPwd"),
                null,
                ucpMin
        );

        RDbClientManager.Instance.addClient(RDbClientName.Oracle, oracle);
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()) {
            System.err.println("Init oracle success.");
        }

                 
        // Get setting port and start server
        int port = 9020;
        String settingPort = System.getProperty("zport");
        try {
            port = Integer.parseInt(settingPort);
        } catch (NumberFormatException ex) {
            System.err.println("System.getProperty(\"zport\"): "
                    + ExceptionUtil.Instance.getAsString(ex));
        }

        int minThreads = 100;
        String minThreadsValue = System.getProperty("mnthrs");
        if (minThreadsValue != null && !minThreadsValue.isEmpty()){
            minThreads = Integer.parseInt(minThreadsValue);
        }
        
        try {
            port = Integer.parseInt(settingPort);
        } catch (NumberFormatException ex) {
            System.err.println("System.getProperty(\"zport\"): "
                    + ExceptionUtil.Instance.getAsString(ex));
        }

        int maxThreads = 1000;
        String maxThreadsValue = System.getProperty("mxthrs");
        if (maxThreadsValue != null && !maxThreadsValue.isEmpty()){
            maxThreads = Integer.parseInt(maxThreadsValue);
        }
        // Load config of VuongTM
        String fileENV = "/env.properties";
        String fileBankProperties = "/bank.properties";
        String fileXml = "/moduleConfig.xml";
        try{
            configService = new ConfigServiceImpl(fileENV, fileBankProperties, fileXml, BoBIModuleConfigNew.class);
        }catch(Exception e){
            System.err.println("Don't load config Service!!!");    
            exit(1);
        }
        EmbededServer server = new EmbededServer(minThreads, maxThreads)
                .setHttpPort(port)
                .setHandler(Service.class);

        Service service = new Service();
        service.start(server);

        NRDbClientManager.Instance.get("ordercache").disconnect();
        
        System.out.println("Start success !!!");
    }

    @Override
    protected String getModuleName() {
        return Service.MODULE_NAME;
    }

    /**
     *
     * @param data
     * @return
     */
    @POST
    @Path(Service.VERIFY_CARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyCard(String data) {
        return super.execute(data, new VerifyCardRequestProcessor(securityH, bankService));
    }    
    
    /**
     *
     * @param data
     * @return
     */
    @POST
    @Path(Service.VERIFY_OTP)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyOTP(String data) {
        
        return super.execute(data, new VerifyOTPRequestProcessor(securityH, bankService));
    }    
}
