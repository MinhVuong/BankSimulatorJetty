package vng.paygate.bank.jaxb.adapter;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author VuongTM
 */
public class BankCodeNewAdapter extends XmlAdapter<BankCodeNews, Map<String,BoBaseBankNew> >{

    @Override
    public Map<String, BoBaseBankNew> unmarshal(BankCodeNews v) throws Exception {
        Map<String, BoBaseBankNew> map = new HashMap<String, BoBaseBankNew>();
        for (BoBaseBankNew config : v.getBanks()) {
            map.put(config.getBankCode(), config);
        }
        return map;
    }

    @Override
    public BankCodeNews marshal(Map<String, BoBaseBankNew> v) throws Exception {
        if (v == null) {
//            System.out.println("Map is null");
            return null;
        }
        BoBaseBankNew[] configs = new BoBaseBankNew[v.size()];

        for (int i = 0; i < v.size(); i++) {
            configs[i] = v.get(i + "");
        }
        BankCodeNews res = new BankCodeNews();
        res.setBanks(configs);
        return res;
    }
    
}
