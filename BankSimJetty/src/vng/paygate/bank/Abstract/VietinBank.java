
package vng.paygate.bank.Abstract;

import java.util.List;
import vng.paygate.bank.jaxb.adapter.BoBaseBankNew;
import vng.paygate.bank.jaxb.adapter.BoCardInfo;
import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.common.ConstantBS;
import vng.paygate.domain.common.Constants;

/**
 *
 * @author VuongTM
 */
public class VietinBank extends BankAbstract{

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
        return "1";
    }
    
}
