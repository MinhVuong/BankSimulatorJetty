/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.bo;

/**
 *
 * @author CPU01661-local       Khong Can dung Toi
 */
public class SPResponseVerifyCard {
    private int responseCode;
    private int orderStatus;
    private String miNotifyUrl;
    private String description;

    public SPResponseVerifyCard() {
    }

    public SPResponseVerifyCard(int responseCode, int orderStatus, String miNotifyUrl, String description) {
        this.responseCode = responseCode;
        this.orderStatus = orderStatus;
        this.miNotifyUrl = miNotifyUrl;
        this.description = description;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getMiNotifyUrl() {
        return miNotifyUrl;
    }

    public void setMiNotifyUrl(String miNotifyUrl) {
        this.miNotifyUrl = miNotifyUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
