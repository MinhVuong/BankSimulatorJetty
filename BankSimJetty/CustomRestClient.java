package vng.paygate.domain.common;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.util.Map;
import vng.paygate.domain.bo.BoBaseResponse;

/**
 *
 * @author VuongTM
 */
public class CustomRestClient {

    private Client client;
    private WebResource webResource;
    private Gson gson = new Gson();

    public CustomRestClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
//        clientConfig.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE);
        this.client = Client.create(clientConfig);
    }

    public void setWebResource(String path) {
        this.webResource = this.client.resource(path);
    }

    public BoBaseResponse post(Map params, Class clazz)
            throws Exception {
        ClientResponse response = (ClientResponse) ((WebResource.Builder) this.webResource.type("application/json").accept(new String[]{"application/json"})).post(ClientResponse.class, params);
        String output = (String) response.getEntity(String.class);
        BoBaseResponse returnObject = (BoBaseResponse) this.gson.fromJson(output, clazz);

        response.close();
        this.client.destroy();
        return returnObject;
    }
}
