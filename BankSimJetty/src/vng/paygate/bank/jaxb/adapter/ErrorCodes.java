/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.bank.jaxb.adapter;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Kuti
 */
public class ErrorCodes {
    private ErrorAPI[] errorApis;

    @XmlElement(name="API")
    public ErrorAPI[] getErrorApis() {
        return errorApis;
    }

    public void setErrorApis(ErrorAPI[] errorApis) {
        this.errorApis = errorApis;
    }
    
}
