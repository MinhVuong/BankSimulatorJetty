package vng.paygate.bank.jaxb.adapter;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author VuongTM
 */
@XmlRootElement
public class SubBank {
    private String subBankCode;
    private String isInputOTP;
    private List<BoCardInfo> boCardInfo;
    public SubBank() {
    }
    @XmlElement
    public String getSubBankCode() {
        return subBankCode;
    }
    @XmlElementWrapper(name = "cardInfos")
    @XmlElement(name = "cardInfo")
    public List<BoCardInfo> getBoCardInfo() {
        return boCardInfo;
    }
    @XmlElement
    public String getIsInputOTP() {
        return isInputOTP;
    }

    public void setIsInputOTP(String isInputOTP) {
        this.isInputOTP = isInputOTP;
    }
    
    public void setSubBankCode(String subBankCode) {
        this.subBankCode = subBankCode;
    }

    public void setBoCardInfo(List<BoCardInfo> boCardInfo) {
        this.boCardInfo = boCardInfo;
    }
}
