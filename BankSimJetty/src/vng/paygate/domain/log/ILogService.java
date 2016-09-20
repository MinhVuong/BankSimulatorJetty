/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.log;

/**
 *
 * @author Kuti
 */
public abstract interface ILogService {

    public abstract void generateDataMessage(String... paramVarArgs);

    public abstract void initLogMessage(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);

    public abstract void log(String paramString1, String paramString2, String paramString3, String paramString4);

    public abstract void logException(String paramString1, String paramString2, String paramString3, String paramString4, Throwable paramThrowable);

    public abstract void logError(String paramString1, String paramString2, String paramString3, String paramString4);

    public abstract void setFunctionName(String paramString);
}
