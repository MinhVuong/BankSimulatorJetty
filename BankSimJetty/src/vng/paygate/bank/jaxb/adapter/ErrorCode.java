/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.bank.jaxb.adapter;

import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Kuti
 */
@XmlRootElement(name = "ErrorCode")
public class ErrorCode {
    private Map<String, ErrorAPI> codes;

    public ErrorCode() {
    }

    public ErrorCode(Map<String, ErrorAPI> codes) {
        this.codes = codes;
    }

    @XmlJavaTypeAdapter(ErrorCodeAdapter.class)
    @XmlElement(name = "APIs")
    public Map<String, ErrorAPI> getCodes() {
        return codes;
    }

    public void setCodes(Map<String, ErrorAPI> codes) {
        this.codes = codes;
    }
    
}
