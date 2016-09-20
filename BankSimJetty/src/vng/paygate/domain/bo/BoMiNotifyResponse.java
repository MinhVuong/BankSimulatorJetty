package vng.paygate.domain.bo;

/**
 *
 * @author VuongTM
 */
public class BoMiNotifyResponse extends BoBaseResponse {

    private String checkSum;

    public BoMiNotifyResponse() {
    }
    

    public BoMiNotifyResponse(String detailResponseCode, String groupResponseCode) {
        super(detailResponseCode, groupResponseCode);
    }

    public String getCheckSum() {
        return this.checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
