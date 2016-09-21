/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.lang.reflect.ParameterizedType;
import vn._123pay.bank.Service;
import vng.paygate.domain.bo.BoBase;
import vng.paygate.domain.bo.BoBaseResponse;
import vng.paygate.domain.bo.BoProcessPaymentResponse;

/**
 *
 * @author CPU01661-local
 */
public class Constants {
    public static final String LOG_SEPARATOR = "\t";
    public static final String SEPARATOR = "|";
    public static final String PARAMS_SEPARATOR = ";";
    public static final String ERROR_6101 = "6101";         // Tham số null.
    public static final String ERROR_6100 = "6100";         // Tham số truyền vào không đúng.
    public static final String ERROR_6007 = "6007";         // CardInfor NULL or EMPTY.
    public static final String RESPONSE_CODE_1 = "1";       // Thành công.
    public static final String ERROR_5000 = "5000";         // Lỗi hệ thống.
    public static final String ERROR_5001 = "5001";         // Lỗi hệ thống.
    public static final String ERROR_7221 = "7221";
    public static final String ERROR_7232 = "7232";
    public static final String ERROR_7231 = "7231";
    public static final String ERROR_7211 = "7211";
    public static final String ERROR_7212 = "7212";
    public static final String ERROR_7201 = "7201";
    public static final String ERROR_7300 = "7300";
    public static final String ERROR_7302 = "7302";
    public static final String ERROR_7007 = "7007";
    public static final String ERROR_7100 = "7100";
    public static final String ERROR_7200 = "7200";
    public static final String ERROR_7202 = "7202";
    public static final String ERROR_7213 = "7213";
    public static final String ERROR_7222 = "7222";
    public static final String ERROR_7299 = "7299";
    public static final String ERROR_7230 = "7230";
    public static final String ERROR_7233 = "7233";
    public static final String ERROR_7234 = "7234";
    public static final String ERROR_7235 = "7235";
    public static final String ERROR_7255 = "7255";
    public static final String ERROR_7402 = "7402";
    
    public static final String INVALID_TRANX_RESULT = "-888888";
    public static final String RESPONSE_TIME_OUT = "-999999";
    
    
    
    public static final String ALGORITHM_SHA1 = "SHA-1";
    public static final String INTERNAL_MODULE = "INTERNAL";
    public static final String FE_MODULE = "FRONTEND";
    public static final String TAB = "  ";
    public static final String NOTIFY_PENDING = "notify pending";
    public static final String NOTIFY_SUCCESS = "notify success";
    public static final String NOTIFY_FAIL = "notify fail";
    
    
    public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * append log message with parameters and returns
     *
     * @param logMessage
     * @param messages
     */
    public static void appendParams(StringBuilder logMessage, String... messages) {
        if (messages == null) {
            return;
        }
        for (int i = 0; i < messages.length; i++) {
            String mess = messages[i];
            logMessage.append(Constants.PARAMS_SEPARATOR).append(mess);
        }
        logMessage.append(Constants.LOG_SEPARATOR);
    }
    
    public static void appendMessage(StringBuilder logMessage, String... messages) {
        for (String mess : messages) {
            logMessage.append(Constants.LOG_SEPARATOR).append(mess);
        }
    }
    
    public BoBaseResponse getResponse(String responseCode) throws InstantiationException, IllegalAccessException {
        BoBaseResponse boResponse = Service.configService.getModuleConfig().getResponseCodeMap().get(responseCode);
        if (boResponse == null) {
            boResponse = Service.configService.getModuleConfig().getResponseCodeMap().get(Constants.ERROR_5000);
        }

        BoBaseResponse result = new BoBaseResponse();
        result.setDetailDescription(boResponse.getDetailDescription());
        result.setDetailResponseCode(boResponse.getDetailResponseCode());
        result.setGroupDescription(boResponse.getGroupDescription());
        result.setGroupResponseCode(boResponse.getGroupResponseCode());
        result.setAuthSite(boResponse.getAuthSite());
        result.setVerifyOtpURL(boResponse.getVerifyOtpURL());
        result.setOrderNo(boResponse.getOrderNo());
        return result;
    }
}
