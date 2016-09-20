package vng.paygate.bank.jaxb.adapter;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author VuongTM
 */
public class SubBanks {
    private SubBank[] subBanks;

    @XmlElement(name="subBank")
    public SubBank[] getSubBanks() {
        return subBanks;
    }

    public void setSubBanks(SubBank[] subBanks) {
        this.subBanks = subBanks;
    }
}
