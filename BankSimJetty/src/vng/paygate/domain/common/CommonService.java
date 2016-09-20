/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.lang.reflect.ParameterizedType;
import vn._123pay.bank.Service;
import vng.paygate.bank.jaxb.adapter.BoBIModuleConfigNew;
import vng.paygate.domain.bo.BoBaseResponse;

/**
 *
 * @author VuongTM
 */
public class CommonService{

    public BoBaseResponse getResponse(String responseCode){
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
