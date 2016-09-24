/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.bank.jaxb.adapter;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Kuti
 */
public class ErrorCodeAdapter extends XmlAdapter<ErrorCodes, Map<String, ErrorAPI>> {

    @Override
    public Map<String, ErrorAPI> unmarshal(ErrorCodes v) throws Exception {
        Map<String, ErrorAPI> map = new HashMap<String, ErrorAPI>();
        for (ErrorAPI config : v.getErrorApis()) {
            map.put(config.getName(), config);
        }
        return map;
    }

    @Override
    public ErrorCodes marshal(Map<String, ErrorAPI> v) throws Exception {
        if (v == null) {
//            System.out.println("Map is null");
            return null;
        }
        ErrorAPI[] configs = new ErrorAPI[v.size()];

        for (int i = 0; i < v.size(); i++) {
            configs[i] = v.get(i + "");
        }
        ErrorCodes res = new ErrorCodes();
        res.setErrorApis(configs);
        return res;
    }

}
