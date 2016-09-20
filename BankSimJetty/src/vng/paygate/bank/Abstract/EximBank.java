package vng.paygate.bank.Abstract;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import vng.paygate.bank.jaxb.adapter.BoBaseBankNew;
import vng.paygate.bank.jaxb.adapter.BoCardInfo;
import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.common.ConstantBS;
import vng.paygate.domain.common.Constants;

/**
 *
 * @author VuongTM
 */
public class EximBank extends BankAbstract{

    @Override
    public String checkAllowCardInfo(StringBuilder logBuilder, String cardHolderName, String cardNo, BoBaseBankNew boBaseBank, String subBankCodeConfig) {
        Constants.appendMessage(logBuilder, "checkAllowCardInfo");
        List<BoCardInfo> lstBoCardInfo = boBaseBank.getSubBanks().get(subBankCodeConfig).getBoCardInfo();
        if (lstBoCardInfo.size() > 0) {

            for (BoCardInfo boCardInfo : lstBoCardInfo) {

                if (boCardInfo.getCardHolderName().equals(cardHolderName.toUpperCase().trim())
                        && boCardInfo.getCardNo().equals(cardNo.trim())) {

                    // check error card
                    if (boCardInfo.getBankCode() != null) {
                        Constants.appendMessage(logBuilder, "boCardInfo.getBankCode()" + boCardInfo.getBankCode());
                        if(boCardInfo.getBankCode().equals("0"))
                            return "1";
                        else if(boCardInfo.getBankCode().equals("1")){
                            return "0";
                        }else {
                            return boCardInfo.getBankCode();
                        }
                    }
                    Constants.appendMessage(logBuilder, "return:" + Constants.RESPONSE_CODE_1);
                    return Constants.RESPONSE_CODE_1;
                }
            }
        }
        Constants.appendMessage(logBuilder, "Can not find this card.");
        return ConstantBS.ERROR_VERIFY_CARD_10;
    }

    @Override
    public String VerifyOTP(StringBuilder logMessage, BoBS boEIB) {
        if (StringUtils.isEmpty(boEIB.getOtp()) || boEIB.getOtp().length() != 8) {
            return Constants.ERROR_5000;
        }
        if (boEIB.getOtp().equals(ConstantBS.ERROR_VERIFY_OTP_77777777)) {
            return boEIB.getOtp();
        }
        String otp = boEIB.getOtp().substring(2);
        System.out.println("OTP substring(2): " + otp);
        String verifyResult = null;
        String result = null;
        if (otp.equals(ConstantBS.ERROR_VERIFY_888888) || otp.equals(ConstantBS.ERROR_VERIFY_999999)) {
            verifyResult = "-" + otp;
        } else {
            if (boEIB.getOtp().substring(4, 6).equals(ConstantBS.INVOKE_BANK_SUCCESS)) {
                verifyResult = Constants.RESPONSE_CODE_1;
                result = boEIB.getOtp().substring(6);
            } else {
                verifyResult = Constants.RESPONSE_CODE_1;
                result = ConstantBS.ERROR_VERIFY_OTP_8;
            }
        }
        System.out.println("OTP substring(4, 6): " + boEIB.getOtp().substring(4, 6));
        System.out.println("verifyResult: " + verifyResult);
        System.out.println("result: " + result);

        Constants.appendMessage(logMessage, verifyResult, result, otp);
        if (verifyResult == null) {
            return Constants.ERROR_5000;
        } else {
            if (Constants.INVALID_TRANX_RESULT.equalsIgnoreCase(verifyResult)) {
                boEIB.setBankResponseCode(Constants.INVALID_TRANX_RESULT);
                boEIB.setIsSuccess(-1);
                return Constants.ERROR_5000;
            } else if (Constants.RESPONSE_TIME_OUT.equals(verifyResult)) { // If can't invoke EIB, do not invoke reversal
                boEIB.setBankResponseCode(Constants.INVALID_TRANX_RESULT);
                return Constants.INVALID_TRANX_RESULT;

            } else if (Constants.RESPONSE_CODE_1.equals(verifyResult)) {
                if (result == null) {
                    boEIB.setBankResponseCode(Constants.INVALID_TRANX_RESULT);
                    boEIB.setIsSuccess(-1);
                    return Constants.ERROR_5000;
                }
                // verify OTP fail
                if (result.equals(ConstantBS.ERROR_VERIFY_OTP_1)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_1);
                    return Constants.ERROR_7230;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_2)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_2);
                    return Constants.ERROR_7231;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_3)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_3);
                    return Constants.ERROR_7302;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_4)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_4);
                    return Constants.ERROR_7255;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_5)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_5);
                    return Constants.ERROR_7211;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_7)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_7);
                    return Constants.ERROR_7302;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_8)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_8);
                    return Constants.ERROR_7222;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_12)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_12);
                    return Constants.ERROR_7007;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_13)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_13);
                    return Constants.ERROR_7302;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_14)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_14);
                    return Constants.ERROR_7232;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_41)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_41);
                    return Constants.ERROR_7234;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_43)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_43);
                    return Constants.ERROR_7235;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_51)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_51);
                    return Constants.ERROR_7201;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_54)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_54);
                    return Constants.ERROR_7233;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_61)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_61);
                    return Constants.ERROR_7202;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_91)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_91);
//                    boEIB.setIsSuccess(-1);
                    return Constants.RESPONSE_TIME_OUT;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_96)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_96);
                    return Constants.ERROR_7300;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_20)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_20);
                    return Constants.ERROR_7213;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_21)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_21);
                    return Constants.ERROR_7213;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_58)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_58);
                    return Constants.ERROR_7213;
                }else if (result.equals(ConstantBS.ERROR_VERIFY_OTP_25)) {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_OTP_25);
                    return Constants.ERROR_7300;
                } else if (result.equals(ConstantBS.INVOKE_BANK_SUCCESS)) {
                    boEIB.setBankResponseCode(ConstantBS.INVOKE_BANK_SUCCESS);
                    boEIB.setIsSuccess(1);
                } else {
                    boEIB.setBankResponseCode(result);          // OTP khong co thi set thanh sai OTP
                    boEIB.setIsSuccess(-1);
                    return Constants.ERROR_7222;
                }
            }
        }
        return Constants.RESPONSE_CODE_1;
    }
    
}
