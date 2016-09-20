/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.bo;

/**
 *
 * @author Kuti
 */
public class BoBSOtp extends BoOrder{
    private String cardNo;
    private String miNotifyUrl;
    private String subbankCode;
    
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMiNotifyUrl() {
        return miNotifyUrl;
    }

    public void setMiNotifyUrl(String miNotifyUrl) {
        this.miNotifyUrl = miNotifyUrl;
    }

    public String getSubbankCode() {
        return subbankCode;
    }

    public void setSubbankCode(String subbankCode) {
        this.subbankCode = subbankCode;
    }
}
