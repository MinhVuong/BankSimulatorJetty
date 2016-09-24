/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import vng.paygate.bank.jaxb.adapter.ErrorAPI;
import vng.paygate.bank.jaxb.adapter.ErrorCode;

/**
 *
 * @author Kuti
 */
public class ErrorCodeHelper {

    private ErrorCode errorCodes;

    public ErrorCodeHelper(String nameFile) {
        try {
            File file = new File(nameFile);
            if (file == null) {
                errorCodes = null;
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(ErrorCode.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Reader reader = new FileReader(file);
            errorCodes = (ErrorCode) unmarshaller.unmarshal(reader);

        } catch (Exception e) {
            System.out.println("ErrorCodeHelper exception: " + e.getMessage());
            this.errorCodes = null;
        }

    }

    public ErrorCode getErrorCodes() {
        return errorCodes;
    }
    /**
     * 
     * @param code      Value error code need check in List
     * @param nameApi   Name of API
     * @param category  Status pending or fail: 1 is pending and 2 is fail.
     * @return 
     */
    public boolean CheckErrorCode(String code, String nameApi, int category){
        ErrorAPI eapi = this.errorCodes.getCodes().get(nameApi);
        if(eapi == null)
            return false;
        switch(category){
            case 1:{    // Pending
                return eapi.getPendings().contains(code);
            }
            case 2:{    // Fail
                return eapi.getFails().contains(code);
            }
        }
        return false;
    }

}
