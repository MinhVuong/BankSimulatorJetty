/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.bo;

import java.util.Date;

/**
 *
 * @author Kuti
 */
public class BoOrder extends BoBase{
    private String orderNo;
    private String bankCode;
    private String merchantCode;
    private Integer orderStatus;
    private Integer opAmount;
    private Integer discountAmount;
    private String bankResponseCode;
    private String bankTransactionId;
    private String bankService;
    private Integer merchantResponseCode;
    private String merchantTransactionId;
    private String merchantService;
    private String currency;
    private String description;
    private Date createDate;
    private Date bankServiceDate;
    private Date bankNotifyDate;
    private Date bankQueryDate;
    private Integer bankQueryRetryCount;
    private Integer merchantNotifyStatus;
    private Date merchantNotifyDate;
    private Integer merchantNotifyRetryCount;
    private String clientIP;
    private Integer orderNoSuffix;
    private String custName;
    private String custAddress;
    private String custPhone;
    private String custEmail;
    private String custGender;
    private String custDOB;
    private BoBaseResponse boBaseResponse;
    private String passcode;
    private String[] addInfo;
    private Integer totalAmount;
    private String queryBOrderUrl;
    private Integer merchantAmount;
    private String responseCode;
    private String accountName;
    private String accountID;
    private String emailLogin;
    private String phoneLogin;
    private String loginType;
    private Integer notifyStatus;
    private String cancelURL;
    private String redirectURL;
    private String errorURL;
    private String selectedBank;
    private String bankId;
    private String version;
    private String secretKey;
    private Integer isWaiting;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getOpAmount() {
        return opAmount;
    }

    public void setOpAmount(Integer opAmount) {
        this.opAmount = opAmount;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getBankResponseCode() {
        return bankResponseCode;
    }

    public void setBankResponseCode(String bankResponseCode) {
        this.bankResponseCode = bankResponseCode;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public String getBankService() {
        return bankService;
    }

    public void setBankService(String bankService) {
        this.bankService = bankService;
    }

    public Integer getMerchantResponseCode() {
        return merchantResponseCode;
    }

    public void setMerchantResponseCode(Integer merchantResponseCode) {
        this.merchantResponseCode = merchantResponseCode;
    }

    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    public String getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(String merchantService) {
        this.merchantService = merchantService;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getBankServiceDate() {
        return bankServiceDate;
    }

    public void setBankServiceDate(Date bankServiceDate) {
        this.bankServiceDate = bankServiceDate;
    }

    public Date getBankNotifyDate() {
        return bankNotifyDate;
    }

    public void setBankNotifyDate(Date bankNotifyDate) {
        this.bankNotifyDate = bankNotifyDate;
    }

    public Date getBankQueryDate() {
        return bankQueryDate;
    }

    public void setBankQueryDate(Date bankQueryDate) {
        this.bankQueryDate = bankQueryDate;
    }

    public Integer getBankQueryRetryCount() {
        return bankQueryRetryCount;
    }

    public void setBankQueryRetryCount(Integer bankQueryRetryCount) {
        this.bankQueryRetryCount = bankQueryRetryCount;
    }

    public Integer getMerchantNotifyStatus() {
        return merchantNotifyStatus;
    }

    public void setMerchantNotifyStatus(Integer merchantNotifyStatus) {
        this.merchantNotifyStatus = merchantNotifyStatus;
    }

    public Date getMerchantNotifyDate() {
        return merchantNotifyDate;
    }

    public void setMerchantNotifyDate(Date merchantNotifyDate) {
        this.merchantNotifyDate = merchantNotifyDate;
    }

    public Integer getMerchantNotifyRetryCount() {
        return merchantNotifyRetryCount;
    }

    public void setMerchantNotifyRetryCount(Integer merchantNotifyRetryCount) {
        this.merchantNotifyRetryCount = merchantNotifyRetryCount;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Integer getOrderNoSuffix() {
        return orderNoSuffix;
    }

    public void setOrderNoSuffix(Integer orderNoSuffix) {
        this.orderNoSuffix = orderNoSuffix;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustGender() {
        return custGender;
    }

    public void setCustGender(String custGender) {
        this.custGender = custGender;
    }

    public String getCustDOB() {
        return custDOB;
    }

    public void setCustDOB(String custDOB) {
        this.custDOB = custDOB;
    }

    public BoBaseResponse getBoBaseResponse() {
        return boBaseResponse;
    }

    public void setBoBaseResponse(BoBaseResponse boBaseResponse) {
        this.boBaseResponse = boBaseResponse;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String[] getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String[] addInfo) {
        this.addInfo = addInfo;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getQueryBOrderUrl() {
        return queryBOrderUrl;
    }

    public void setQueryBOrderUrl(String queryBOrderUrl) {
        this.queryBOrderUrl = queryBOrderUrl;
    }

    public Integer getMerchantAmount() {
        return merchantAmount;
    }

    public void setMerchantAmount(Integer merchantAmount) {
        this.merchantAmount = merchantAmount;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getEmailLogin() {
        return emailLogin;
    }

    public void setEmailLogin(String emailLogin) {
        this.emailLogin = emailLogin;
    }

    public String getPhoneLogin() {
        return phoneLogin;
    }

    public void setPhoneLogin(String phoneLogin) {
        this.phoneLogin = phoneLogin;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public Integer getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(Integer notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public String getCancelURL() {
        return cancelURL;
    }

    public void setCancelURL(String cancelURL) {
        this.cancelURL = cancelURL;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getErrorURL() {
        return errorURL;
    }

    public void setErrorURL(String errorURL) {
        this.errorURL = errorURL;
    }

    public String getSelectedBank() {
        return selectedBank;
    }

    public void setSelectedBank(String selectedBank) {
        this.selectedBank = selectedBank;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getIsWaiting() {
        return isWaiting;
    }

    public void setIsWaiting(Integer isWaiting) {
        this.isWaiting = isWaiting;
    }
    
    
}
