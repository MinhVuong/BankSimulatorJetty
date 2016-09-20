/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.bank.Abstract;

/**
 *
 * @author CPU01661-local
 */
public class BankFactory {
    public static BankAbstract createBank(String nameBank){
        if(nameBank.equals("EIB"))
            return new EximBank();
        else if(nameBank.equals("VTB"))
            return new VietinBank();
        return null;
    }
}
