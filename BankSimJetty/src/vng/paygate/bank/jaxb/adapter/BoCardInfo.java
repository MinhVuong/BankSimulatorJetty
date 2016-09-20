package vng.paygate.bank.jaxb.adapter;

/**
 *
 * @author VuongTM
 */
public class BoCardInfo {
    private String cardHolderName;
    private String cardNo;
    private String prefixCardNo;
    private String bankCode;

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getPrefixCardNo() {
        return prefixCardNo;
    }

    public void setPrefixCardNo(String prefixCardNo) {
        this.prefixCardNo = prefixCardNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
}
