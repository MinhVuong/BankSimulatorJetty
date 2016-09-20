package vng.paygate.bank.Abstract;

import vng.paygate.bank.jaxb.adapter.BoBaseBankNew;
import vng.paygate.domain.bo.BoBS;

/**
 *
 * @author VuongTM
 */
public abstract class BankAbstract {
    public abstract String checkAllowCardInfo(StringBuilder logBuilder, String cardHolderName, String cardNo, BoBaseBankNew boBaseBank, String subBankCodeConfig);
    public abstract String VerifyOTP(StringBuilder logMessage, BoBS boEIB);
}
