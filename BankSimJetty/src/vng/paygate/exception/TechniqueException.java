/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.exception;

/**
 *
 * @author Kuti
 */
public class TechniqueException extends Exception {

    private static final long serialVersionUID = -8950873012354363164L;

    public TechniqueException() {
    }

    public TechniqueException(String msg) {
        super(msg);
    }

    public TechniqueException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
