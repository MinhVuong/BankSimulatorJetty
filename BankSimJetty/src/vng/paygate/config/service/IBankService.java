/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.config.service;

import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.bo.BoBSOtp;
import vng.paygate.domain.bo.BoOrderNew;
import vng.paygate.domain.bo.SPResponseVerifyCard;
/**
 *
 * @author Kuti
 */
public interface IBankService {
    public BoOrderNew LoadOrderVerifyCard(String orderNo);
    
    public String VerifyCard(BoBS boBS);
    
    public BoBSOtp LoadOrderVerifyOTP(String orderNO);
    
    public void UpdateOtpReinput(BoBS boEIB);
    
    public void updateNotify(BoBS boEIB, String notifyStatus);
}
