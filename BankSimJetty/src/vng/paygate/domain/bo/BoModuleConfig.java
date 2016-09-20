package vng.paygate.domain.bo;

import java.util.Map;
import java.util.Properties;

/**
 *
 * @author VuongTM
 */
public class BoModuleConfig {

    private String env;
    private Properties properties;
    private Map<String, BoBaseResponse> responseCodeMap;

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Map<String, BoBaseResponse> getResponseCodeMap() {
        return this.responseCodeMap;
    }

    public void setResponseCodeMap(Map<String, BoBaseResponse> responseCodeMap) {
        this.responseCodeMap = responseCodeMap;
    }

    public String getEnv() {
        return this.env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
