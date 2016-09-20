/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn._123pay.bank;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.xml.ws.client.ClientTransportException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import vn._123pay.Result;
import vn._123pay.ResultCode;
import vn._123pay.data.json.JSONObject;
import vng.paygate.bank.Abstract.BankAbstract;
import vng.paygate.bank.Abstract.BankFactory;
import vng.paygate.bank.jaxb.adapter.BoBIModuleConfigNew;
import vng.paygate.bank.jaxb.adapter.BoBaseBankNew;
import vng.paygate.config.service.impl.BankServiceImpl;
import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.bo.BoBSOtp;
import vng.paygate.domain.bo.BoBaseResponse;
import vng.paygate.domain.bo.BoMiNotifyResponse;
import vng.paygate.domain.bo.BoNotifyQueryBOrder;
import vng.paygate.domain.bo.BoNotifyQueryRequest;
import vng.paygate.domain.bo.BoOrder;
import vng.paygate.domain.bo.BoProcessPaymentResponse;
import vng.paygate.domain.common.ConstantBS;
import vng.paygate.domain.common.Constants;
import vng.paygate.notify.NotifyHelper;
import vng.paygate.security.ChecksumHelper;
import vng.paygate.security.SecurityHelper;

/**
 *
 * @author Kuti
 */
final class VerifyOTPRequestProcessor extends BaseRequestProcessor {

    private String orderNo;
    private String otp;
    private String numberInput;
    private String responseCode;
    BoBaseResponse boBaseResponse;
    BoProcessPaymentResponse boResponse = new BoProcessPaymentResponse();

    SecurityHelper securityH;
    BankServiceImpl bankService;
    Gson gson = new Gson();

    VerifyOTPRequestProcessor(SecurityHelper security, BankServiceImpl bankservice) {
        securityH = security;
        bankService = bankservice;
        super.setOldVersionCompatibleRequired(true);
    }

    @Override
    protected ResultCode validateAPIParams(JSONObject jsono) {
        orderNo = jsono.getString("orderNo");
        otp = jsono.getString("otp");
        if (otp == null) {
            otp = jsono.getString("authenInfo");
        }
        numberInput = jsono.getString("numberInput");
        String checksum = jsono.getString("checksum");
        Constants.appendParams(super.getLogBuilder(), jsono.toString());

        Constants.appendMessage(super.getLogBuilder(), "validateParams");
        responseCode = validate(orderNo, otp, numberInput, checksum);
        Constants.appendParams(super.getLogBuilder(), responseCode);
        if (!Constants.RESPONSE_CODE_1.equals(responseCode)) {
            boBaseResponse = Service.commonService.getResponse(responseCode);
            if (responseCode.equals(Constants.ERROR_6101)) {
                return ResultCode.DataInputEmtyBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            } else {
                return ResultCode.OrderNoTooLongBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            }
        }

        Constants.appendMessage(super.getLogBuilder(), "Check checksun");
        // RawData
        String rawData = "";
        Map<String, String> treeMap = new TreeMap<String, String>();
        treeMap.put("orderNo", orderNo);
        treeMap.put("otp", otp);
        treeMap.put("numberInput", numberInput);
        for (String item : treeMap.keySet()) {
            if (!StringUtils.isEmpty(item)) {
                rawData += treeMap.get(item);
            }
        }
        boBaseResponse = securityH.VerifyChecksum(Constants.ALGORITHM_SHA1, rawData, checksum, Constants.INTERNAL_MODULE);
        Constants.appendParams(super.getLogBuilder(), boBaseResponse.getDetailResponseCode());
        if (!Constants.RESPONSE_CODE_1.equals(responseCode)) {
            boBaseResponse = Service.commonService.getResponse(boBaseResponse.getDetailResponseCode());
            if (responseCode.equals(Constants.ERROR_5000)) {
                return ResultCode.SystemError.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            }
            return ResultCode.InvalidCheckSumBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
        }
        return ResultCode.Success;
    }

    @Override
    protected <V> Result<V> execute() {
        Constants.appendMessage(super.getLogBuilder(), "Load Order Verify OTP");
        BoBSOtp boBSOTP = bankService.LoadOrderVerifyOTP(orderNo);
        Constants.appendParams(super.getLogBuilder(), boBSOTP.getResponseCode());
        if (!Constants.RESPONSE_CODE_1.equals(boBSOTP.getResponseCode())) {
            // 2 truong hop: 5000  va order status sai
            boBaseResponse = Service.commonService.getResponse(boBSOTP.getResponseCode());
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        }
        BoBIModuleConfigNew boBIModuleConfig = (BoBIModuleConfigNew) Service.configService.getModuleConfig();
        BoBaseBankNew boBank = boBIModuleConfig.getBankCodeMap().get(boBSOTP.getBankCode());
        if (boBank == null) {
            boBank = boBIModuleConfig.getBankCodeMap().get("EIB");
        }

        //update data to response result (in case: neu xay ra exception thi cung co data gui ve)
        buildResponse(boResponse, boBSOTP);

        BoBS boEIB = new BoBS();
        copyBoOrderToBoEIB(boBSOTP, boEIB);

        String keyPath = "path file Key";
        boEIB.setOtp(otp);
        boEIB.setPathKey(keyPath);
        boEIB.setBankService("verifyOTP");
        boEIB.setNumberInput(Integer.valueOf(numberInput).intValue());
        boEIB.setNotifyOrQuery(1);
        Constants.appendMessage(super.getLogBuilder(), "Bank Process OTP");
        BankAbstract bank = BankFactory.createBank(boEIB.getBanksimCode());
        String response = bank.VerifyOTP(super.getLogBuilder(), boEIB);
        Constants.appendParams(super.getLogBuilder(), response);

        Constants.appendMessage(super.getLogBuilder(), "BI Process OTP");
        String status;
        if (ConstantBS.ERROR_VERIFY_OTP_77777777.equals(response)) {
            BoBIModuleConfigNew boBIModuleConfigNew = (BoBIModuleConfigNew) Service.configService.getModuleConfig();
            try {
                Constants.appendMessage(super.getLogBuilder(), "Sleep 7 seconds");

                Thread.sleep(Integer.parseInt(boBIModuleConfigNew.getWaitingTime()));
            } catch (InterruptedException ex) {
                Logger.getLogger(VerifyOTPRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

            Constants.appendMessage(super.getLogBuilder(), "Init Data");
            BoNotifyQueryRequest boNQRequest = new BoNotifyQueryRequest();
            boNQRequest.setOrderNo(boBSOTP.getOrderNo());
            boNQRequest.setBankCode(boBSOTP.getBankCode());

            String rawDataNotify = boBSOTP.getOrderNo();
            String checksum = securityH.CreateCheckSumMerhchant(Constants.ALGORITHM_SHA1, rawDataNotify, Constants.INTERNAL_MODULE);
            boNQRequest.setChecksum(checksum);

            String notifyUrl = boBIModuleConfigNew.getUrlNotify();
            Constants.appendMessage(super.getLogBuilder(), "invokeNotify", notifyUrl + " " + boBSOTP.getBankCode());

            invokeNotify(notifyUrl, boNQRequest);
            boBaseResponse = Service.commonService.getResponse(response);
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        } else {
            if (Constants.ERROR_7222.equals(response)
                    || Constants.ERROR_7231.equals(response)
                    || Constants.ERROR_7211.equals(response)
                    || Constants.ERROR_7232.equals(response)
                    || Constants.ERROR_7213.equals(response)
                    || Constants.ERROR_7300.equals(response)
                    || Constants.ERROR_7234.equals(response)
                    || Constants.ERROR_7201.equals(response)
                    || Constants.ERROR_7230.equals(response)
                    || Constants.ERROR_7255.equals(response)
                    || Constants.ERROR_7302.equals(response)
                    || Constants.ERROR_7007.equals(response)
                    || Constants.ERROR_7235.equals(response)
                    || Constants.ERROR_7202.equals(response)
                    || Constants.ERROR_7233.equals(response)) {
                Constants.appendParams(super.getLogBuilder(), boEIB.getTransactionId(), boEIB.getBankCode(), boEIB.getBankService(),
                        boEIB.getBankResponseCode(), "" + boEIB.getNotifyOrQuery(), "" + boEIB.getNumberInput());
                Constants.appendMessage(super.getLogBuilder(), "updateOtpReinput");
                bankService.UpdateOtpReinput(boEIB);
                if (boEIB.getResponseCode() == null || !Constants.RESPONSE_CODE_1.equals("" + boEIB.getResponseCode().intValue())) {
                    Constants.appendParams(super.getLogBuilder(), "updateOtpReinput is fail");
                    boBaseResponse = Service.commonService.getResponse(Constants.ERROR_5000);
                    return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
                }
                Constants.appendParams(super.getLogBuilder(), "" + boEIB.getResponseCode(), "" + boEIB.getOrderStatus(), boEIB.getMiNotifyUrl(), numberInput);
                if (Integer.valueOf(numberInput).intValue() < 3) {
                    boBaseResponse = Service.commonService.getResponse(response);
                    Constants.appendParams(super.getLogBuilder(), boResponse.getDetailResponseCode(), boResponse.getGroupResponseCode());
                    return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
                } else {
                    boBSOTP.setOrderStatus(boEIB.getOrderStatus().intValue());
                    boBSOTP.setBankResponseCode(boEIB.getBankResponseCode());

                    Constants.appendParams(super.getLogBuilder(), "notifyMerchant");
                    notifyMerchant(boBSOTP, boEIB, boResponse, super.getLogBuilder());

                    boBaseResponse = Service.commonService.getResponse("" + boEIB.getOrderStatus());

                    String payportURL = boBank.getPayportURL() + "?" + ConstantBS.TRANX_REF_PARAM + "=" + boBSOTP.getOrderNo();
                    // Can than cho nay
                    buildResponse(boResponse, boBaseResponse);
                    boResponse.setRedirectURL(payportURL);
                    Constants.appendParams(super.getLogBuilder(), boResponse.getGroupResponseCode(), boResponse.getDetailResponseCode(),
                            boEIB.getOrderStatus() == null ? "" : ("" + boEIB.getOrderStatus()),
                            boEIB.getBankResponseCode(), boResponse.getRedirectURL());
                    return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);
                }

            } else if (Constants.RESPONSE_TIME_OUT.equals(response)) {
                // notify Pending
                status = Constants.NOTIFY_PENDING;
            } else if (Constants.RESPONSE_CODE_1.equals(response) && boEIB.getIsSuccess() == 1) {
                status = Constants.NOTIFY_SUCCESS;
            } else {
                // notify Fail
                status = Constants.NOTIFY_FAIL;
            }

            //update bank notify success/fail
            boolean isUpdatedSuccess = updateNotify(status, boEIB, boBSOTP, boResponse, boBSOTP.getSubbankCode(), super.getLogBuilder());
            if (!isUpdatedSuccess) {
                boBaseResponse = Service.commonService.getResponse(boResponse.getDetailResponseCode());
                Constants.appendParams(super.getLogBuilder(), boResponse.getGroupResponseCode(), boResponse.getDetailResponseCode(),
                        boBSOTP.getOrderStatus() == null ? "" : ("" + boBSOTP.getOrderStatus()),
                        boBSOTP.getBankResponseCode(), "verifyOTP fail");
                return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);

            } else {
                boBaseResponse = Service.commonService.getResponse(Constants.RESPONSE_CODE_1);
                if (status.equals(Constants.NOTIFY_SUCCESS) || status.equals(Constants.NOTIFY_FAIL)) {
                    //success or fail then notify merchant
                    notifyMerchant(boBSOTP, boEIB, boResponse, super.getLogBuilder());
                }
            }
            boBaseResponse = Service.commonService.getResponse(Constants.RESPONSE_CODE_1);
            buildResponse(boResponse, boBSOTP);
            buildResponse(boResponse, boBaseResponse);
            //build payport URL
            String payportURL = boBank.getPayportURL() + "?" + ConstantBS.TRANX_REF_PARAM + "=" + boBSOTP.getOrderNo();
            Constants.appendMessage(super.getLogBuilder(), "UrlRedirect", payportURL);
            boResponse.setRedirectURL(payportURL);

            Constants.appendParams(super.getLogBuilder(), boResponse.getGroupResponseCode(), boResponse.getDetailResponseCode(),
                    boResponse.getOrderStatus() == null ? "" : ("" + boResponse.getOrderStatus()),
                    boResponse.getBankResponseCode(), boResponse.getRedirectURL());
           
            System.out.println("chay chua");
            return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);
        }
    }

    @Override
    protected String getAPIName() {
        return Service.VERIFY_OTP;
    }

    private String validate(String orderNo, String otp, String numberInput, String checksum) {
        if (StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(otp)
                || StringUtils.isEmpty(numberInput) || StringUtils.isEmpty(checksum)) {
            return Constants.ERROR_6101;
        }
        if (orderNo.length() > 17 || !StringUtils.isAlphanumeric(orderNo)
                || !StringUtils.isNumeric(otp) || otp.length() != 8) {
            return Constants.ERROR_6100;
        }

        return Constants.RESPONSE_CODE_1;
    }

    private void buildResponse(BoProcessPaymentResponse boResponse, BoBSOtp boOrder) {
        boResponse.setOrderNo(boOrder.getOrderNo());
        boResponse.setOrderStatus(boOrder.getOrderStatus());
        boResponse.setMerchantTransactionId(boOrder.getMerchantTransactionId());
        boResponse.setTotalAmount(boOrder.getTotalAmount());
        boResponse.setOpAmount(boOrder.getOpAmount());
        boResponse.setBankCode(boOrder.getBankCode());
        boResponse.setMerchantCode(boOrder.getMerchantCode());
        boResponse.setBankResponseCode(boOrder.getBankResponseCode());
        boResponse.setDescription(boOrder.getDescription());
        boResponse.setNotifyStatus(boOrder.getNotifyStatus());
    }

    private void copyBoOrderToBoEIB(BoBSOtp boOrder, BoBS boEIB) {
//        boEIB.setAmount(boOrder.getTotalAmount());
        boEIB.setAmount(boOrder.getOpAmount());
        //Cause TIPSS compare int, not string so we remove 4 first characters in 123Pay transaction id.
        String transId = boOrder.getOrderNo().substring(4);
//        String transId = boOrder.getOrderNo();
        //for test
        if (boOrder.getOrderNoSuffix() != null && boOrder.getOrderNoSuffix().intValue() > 0) {
            transId = transId + "" + boOrder.getOrderNoSuffix();
        }
        boEIB.setTransactionIdSuffix(transId);
        boEIB.setTransactionId(boOrder.getOrderNo());
        boEIB.setMerchantCode(boOrder.getMerchantCode());
        ///
        boEIB.setBankCode(boOrder.getBankCode());
        boEIB.setCustomerId(null);
        boEIB.setCardNo(boOrder.getCardNo());
        String subBankCode = boOrder.getSubbankCode();
        subBankCode = subBankCode.substring(4, subBankCode.length());
        boEIB.setBanksimCode(subBankCode);
    }

    private String processOTP(BoBS boEIB) {
        return Constants.RESPONSE_CODE_1;
    }

    private BoNotifyQueryBOrder invokeNotify(String notifyUrl, BoNotifyQueryRequest boNQRequest) {
        BoNotifyQueryBOrder bo = new BoNotifyQueryBOrder();
        try {
            JsonObject json = new JsonObject();
            json.addProperty("orderNo", boNQRequest.getOrderNo());
            json.addProperty("bankCode", boNQRequest.getBankCode());
            json.addProperty("checksum", boNQRequest.getChecksum());
            NotifyHelper notifyH = new NotifyHelper();
            String result = notifyH.postNotify(notifyUrl, json, "application/json", "application/json");
            if(StringUtils.isEmpty(result)){
                bo.setDetailResponseCode(Constants.ERROR_5000);
                bo.setGroupResponseCode(Constants.ERROR_5000);
            }else{
                bo = gson.fromJson(result, BoNotifyQueryBOrder.class);
            }          
            
        } catch (Exception e) {
            Constants.appendMessage(super.getLogBuilder(), "Fail InvokeNotify");
        }
        return bo;
    }

    private void notifyMerchant(BoBSOtp boOrder, BoBS boEIB, BoProcessPaymentResponse boResponse, StringBuilder logMessage) {

        String rawData = boOrder.getOrderNo();
        String checksumMerchant = securityH.CreateCheckSumMerhchant(Constants.ALGORITHM_SHA1, rawData, Constants.INTERNAL_MODULE);
        Constants.appendParams(super.getLogBuilder(), boOrder.getOrderNo(), checksumMerchant);
        Constants.appendMessage(super.getLogBuilder(), "invokeNotifyMerchant", boEIB.getMiNotifyUrl());
        // check status of notify merchant and order status
        try {
            BoMiNotifyResponse boMiNotify = new BoMiNotifyResponse(Constants.RESPONSE_CODE_1, Constants.RESPONSE_CODE_1);
                    //invokeNotifyMerchant(boEIB.getMiNotifyUrl(), boOrder.getOrderNo(), checksumMerchant);

            if (boMiNotify.getGroupResponseCode().equals(Constants.RESPONSE_CODE_1)
                    && boOrder.getOrderStatus().intValue() == 1) {
                boOrder.setNotifyStatus(1);
            } else {
                boOrder.setNotifyStatus(0);
            }
            Constants.appendParams(logMessage, boMiNotify.getGroupResponseCode(), boMiNotify.getDetailResponseCode(),
                    boOrder.getOrderStatus().intValue() + "", boOrder.getNotifyStatus() + "");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (boOrder.getOrderStatus().intValue() == 1) {
                boResponse.setGroupResponseCode(Constants.RESPONSE_CODE_1);
                boResponse.setDetailResponseCode(Constants.RESPONSE_CODE_1);
                boOrder.setNotifyStatus(0);
            } else {
                boResponse.setGroupResponseCode(Constants.ERROR_5000);
                boResponse.setDetailResponseCode(Constants.ERROR_5001);
            }
            if (ex instanceof ClientTransportException || ex instanceof ConnectException
                    || ex instanceof RemoteException || ex instanceof SocketException
                    || ex instanceof IOException || ex instanceof ClientHandlerException) {
                // connection timeout
                Constants.appendParams(logMessage, Constants.RESPONSE_TIME_OUT, "" + boOrder.getOrderStatus(), "" + boOrder.getNotifyStatus());
            } else {
                // exception
                Constants.appendParams(logMessage, Constants.ERROR_5000, "" + boOrder.getOrderStatus(), "" + boOrder.getNotifyStatus());
            }
        }
    }

    private BoMiNotifyResponse invokeNotifyMerchant(String notifyUrl, String orderNo, String dataSign) {
        System.out.println("URL NOTIFY: " + notifyUrl);
        System.out.println("OrderNo : " + orderNo);
        System.out.println("dataSign : " + dataSign);
        BoMiNotifyResponse bo = new BoMiNotifyResponse(Constants.RESPONSE_CODE_1, Constants.RESPONSE_CODE_1);
        try {
            JsonObject json = new JsonObject();
            json.addProperty("orderNo", orderNo);
            json.addProperty("checksum", dataSign);
            NotifyHelper notifyH = new NotifyHelper();
            String result = notifyH.postNotify(notifyUrl, json, "application/json", "application/json");
            if(StringUtils.isEmpty(result)){
                bo.setDetailResponseCode(Constants.ERROR_5000);
                bo.setGroupResponseCode(Constants.ERROR_5000);
            }else{
                bo = gson.fromJson(result, BoMiNotifyResponse.class);
            }                
            return bo;
        } catch (Throwable e) {
            System.err.println("invokeNotifyMerchant error: " + e.getMessage());
            return new BoMiNotifyResponse(Constants.ERROR_5000, Constants.ERROR_5000);
        }

    }

    private boolean updateNotify(String status, BoBS boEIB, BoBSOtp boOrder, BoProcessPaymentResponse boResponse, String subBankCode, StringBuilder logMessage) {
        boEIB.setNotifyOrQuery(1);
        Constants.appendParams(logMessage, boEIB.getTransactionId(), boEIB.getBankCode(), "" + boEIB.getBankService(), boEIB.getBankResponseCode(),
                "" + boEIB.getNotifyOrQuery());
        if (status.equals(Constants.NOTIFY_PENDING)) {
            Constants.appendMessage(logMessage, "SP_BI_EIB_NOTIFY_PENDING");
            //Update BI notify pending
            String temp = boEIB.getBanksimCode();
            String temp1 = subBankCode.substring(4, subBankCode.length());
            boEIB.setBanksimCode(temp1);
            bankService.updateNotify(boEIB, Constants.NOTIFY_PENDING);
            boEIB.setBanksimCode(temp);

            if (!Constants.RESPONSE_CODE_1.equals("" + boEIB.getResponseCode().intValue())) {
                Constants.appendMessage(logMessage, "" + boEIB.getResponseCode());
//                writeLogError(Constants.ERROR_5000, StringUtils.defaultIfEmpty(Integer.toString(boEIB.getResponseCode()), 
//                Constants.ERROR_5000), logMessage.toString());
                boResponse.setGroupResponseCode(Constants.ERROR_5000);
                boResponse.setDetailResponseCode("" + boEIB.getResponseCode().intValue());
                return false;
            }
            Constants.appendMessage(logMessage, Constants.RESPONSE_CODE_1);
            boResponse.setGroupResponseCode(Constants.RESPONSE_CODE_1);
            boResponse.setDetailResponseCode(Constants.RESPONSE_CODE_1);

            buildResponse(boResponse, boOrder);

            Constants.appendMessage(logMessage, "Result: ");
            Constants.appendParams(logMessage, boResponse.getGroupResponseCode(), boResponse.getDetailResponseCode(),
                    boResponse.getOrderNo(), boResponse.getMerchantTransactionId(),
                    Integer.toString(boResponse.getOrderStatus()), Integer.toString(boResponse.getTotalAmount()),
                    Integer.toString(boResponse.getOpAmount()), boResponse.getBankCode(), boResponse.getMerchantCode(),
                    boResponse.getBankResponseCode(), boResponse.getDescription());
//            writeLogSuccess(Constants.RESPONSE_CODE_1, logMessage.toString());
            return true;
        } else if (status.equals(Constants.NOTIFY_SUCCESS)) {
            Constants.appendMessage(logMessage, "SP_BI_123PAY_NOTIFY_SUCCESS");
            //Update BI notify success
            String temp = boEIB.getBanksimCode();
            String temp1 = subBankCode.substring(4, subBankCode.length());
            boEIB.setBanksimCode(temp1);
            bankService.updateNotify(boEIB, Constants.NOTIFY_SUCCESS);
            boEIB.setBanksimCode(temp);

            if (!Constants.RESPONSE_CODE_1.equals("" + boEIB.getResponseCode().intValue())) {
                Constants.appendParams(logMessage, "" + boEIB.getResponseCode());
//                writeLogError(Constants.ERROR_5000, StringUtils.defaultIfEmpty(Integer.toString(boEIB.getResponseCode()), Constants.ERROR_5000), 
//                    logMessage.toString());
                boResponse.setGroupResponseCode(Constants.ERROR_5000);
                boResponse.setDetailResponseCode("" + boEIB.getResponseCode().intValue());
                return false;
            }
            Constants.appendParams(logMessage, "" + boEIB.getResponseCode(), boEIB.getMiNotifyUrl());
            boEIB.setOrderStatus(Integer.parseInt(Constants.RESPONSE_CODE_1));
        } else {
            Constants.appendMessage(logMessage, "SP_BI_123PAY_NOTIFY_FAIL");
            //Update BI notify fail
            String temp = boEIB.getBanksimCode();
            String temp1 = subBankCode.substring(4, subBankCode.length());
            boEIB.setBanksimCode(temp1);

            bankService.updateNotify(boEIB, Constants.NOTIFY_FAIL);
            boEIB.setBanksimCode(temp);
            if (!Constants.RESPONSE_CODE_1.equals("" + boEIB.getResponseCode().intValue())) {
                Constants.appendParams(logMessage, "" + boEIB.getResponseCode().intValue());
//                writeLogError(Constants.ERROR_5000, StringUtils.defaultIfEmpty(Integer.toString(boEIB.getResponseCode()), Constants.ERROR_5000), 
//                logMessage.toString());
                boResponse.setGroupResponseCode(Constants.ERROR_5000);
                boResponse.setDetailResponseCode("" + boEIB.getResponseCode().intValue());
                return false;
            }
            Constants.appendParams(logMessage, "" + boEIB.getResponseCode(), "" + boEIB.getOrderStatus(), boEIB.getMiNotifyUrl());
//            boResponse.setOrderStatus(Integer.parseInt(boEIB.getOrderStatus()));
        }
        boOrder.setOrderStatus(boEIB.getOrderStatus().intValue());
        boOrder.setBankResponseCode(boEIB.getBankResponseCode());

        return true;
    }
    
    private void buildResponse(BoProcessPaymentResponse boResponse, BoBaseResponse boBase) {
        boResponse.setOrderNo(boBase.getOrderNo());
        boResponse.setGroupResponseCode(boBase.getGroupResponseCode());
        boResponse.setDetailResponseCode(boBase.getDetailResponseCode());
        boResponse.setGroupDescription(boBase.getGroupDescription());
        boResponse.setDetailDescription(boBase.getDetailDescription());
    }
}
