/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.bank.jaxb.adapter;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Kuti
 */
@XmlRootElement
public class ErrorAPI {
    private String name;
    private List<String> pendings;
    private List<String> fails;

    public ErrorAPI() {
    }

    public ErrorAPI(String name, List<String> pendings, List<String> fails) {
        this.name = name;
        this.pendings = pendings;
        this.fails = fails;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "StatusPeding")
    @XmlElement(name = "Code")
    public List<String> getPendings() {
        return pendings;
    }

    public void setPendings(List<String> pendings) {
        this.pendings = pendings;
    }

    @XmlElementWrapper(name = "StatusFail")
    @XmlElement(name = "Code")
    public List<String> getFails() {
        return fails;
    }

    public void setFails(List<String> fails) {
        this.fails = fails;
    }
    
}
