package vng.paygate.domain.bo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author VuongTM
 */
@XmlRootElement
public class BoBaseResponse extends BoBase {

    protected String groupResponseCode;
    protected String detailResponseCode;
    protected String detailDescription;
    protected String groupDescription;
    private String orderNo;
    private String authSite;
    private String verifyOtpURL;

    public String getAuthSite() {
        return this.authSite;
    }

    public void setAuthSite(String authSite) {
        this.authSite = authSite;
    }

    public String getVerifyOtpURL() {
        return this.verifyOtpURL;
    }

    public void setVerifyOtpURL(String verifyOtpURL) {
        this.verifyOtpURL = verifyOtpURL;
    }

    public BoBaseResponse() {
    }

    public BoBaseResponse(String orderNo) {
        this.orderNo = orderNo;
    }

    public BoBaseResponse(String detailResponseCode, String groupResponseCode) {
        this.detailResponseCode = detailResponseCode;
        this.groupResponseCode = groupResponseCode;
    }

    public BoBaseResponse(String groupResponseCode, String detailResponseCode, String description) {
        this.groupResponseCode = groupResponseCode;
        this.detailResponseCode = detailResponseCode;
        this.detailDescription = description;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    @XmlElement
    public String getDetailDescription() {
        return this.detailDescription;
    }

    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    @XmlElement
    public String getGroupDescription() {
        return this.groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    @XmlElement
    public String getDetailResponseCode() {
        return this.detailResponseCode;
    }

    public void setDetailResponseCode(String detailResponseCode) {
        this.detailResponseCode = detailResponseCode;
    }

    @XmlElement
    public String getGroupResponseCode() {
        return this.groupResponseCode;
    }

    public void setGroupResponseCode(String groupResponseCode) {
        this.groupResponseCode = groupResponseCode;
    }
}
