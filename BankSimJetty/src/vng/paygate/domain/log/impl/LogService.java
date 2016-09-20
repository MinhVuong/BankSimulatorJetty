/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.log.impl;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import vng.paygate.domain.common.Constants;
import vng.paygate.domain.log.ILogService;

/**
 *
 * @author Kuti
 */
public class LogService implements ILogService {

    protected static final Logger logger = LoggerFactory.getLogger("InfoAppender");
    protected static final Logger logScribe = LoggerFactory.getLogger("ScribeAppender");
    private String[] logValues = new String[11];
    private StringBuilder data = new StringBuilder();

    @Override
    public void generateDataMessage(String... params) {
        for (String param : params) {
            this.data.append(StringUtils.defaultIfBlank(param, "")).append(Constants.TAB);
        }
    }

    @Override
    public void initLogMessage(String startDate, String serverId, String moduleName, String functionName, String productId, String callerIP, String userIp) {
        this.logValues[1] = startDate;
        this.logValues[2] = serverId;
        this.logValues[3] = moduleName;
        this.logValues[4] = functionName;
        this.logValues[5] = productId;
        this.logValues[8] = callerIP;
        this.logValues[9] = userIp;
    }

    private String loadLogFormat(String endDate, String returnCode, String errorCode, String localData) {
        this.logValues[0] = endDate;
        this.logValues[6] = returnCode;
        this.logValues[7] = errorCode;
        if ((this.data == null) || (this.data.length() == 0)) {
            this.logValues[10] = localData.replaceFirst("\t", "");
        } else {
            this.logValues[10] = (Constants.TAB + this.data.toString() + localData);
        }
        ResourceBundle rb = ResourceBundle.getBundle("config/environment");
        String logMessage = rb.getString("123pay.log.format");
        return logMessage;
    }

    @Override
    public void log(String endDate, String returnCode, String errorCode, String data) {
        String logMessage = loadLogFormat(endDate, returnCode, errorCode, data);
        if (!StringUtils.isBlank(logMessage)) {
            logMessage = MessageFormat.format(logMessage, (Object[]) this.logValues);

            MDC.put("logScribe", "123pay_succ");
            logScribe.info(logMessage);
            MDC.remove("logScribe");
        }
    }

    @Override
    public void logException(String endDate, String returnCode, String errorCode, String data, Throwable exception) {
        String logMessage = loadLogFormat(endDate, returnCode, errorCode, data);
        if (!StringUtils.isBlank(logMessage)) {
            logMessage = MessageFormat.format(logMessage, (Object[]) this.logValues);

            MDC.put("logScribe", "123pay_excep");
            logMessage = logMessage + " " + ExceptionUtils.getStackTrace(exception).replace("\t", " ").replace("\n", " ").replace("\r", "");
            logScribe.error(logMessage);
            MDC.remove("logScribe");
        }
    }

    @Override
    public void logError(String endDate, String returnCode, String errorCode, String data) {
        String logMessage = loadLogFormat(endDate, returnCode, errorCode, data);
        if (!StringUtils.isBlank(logMessage)) {
            logMessage = MessageFormat.format(logMessage, (Object[]) this.logValues);

            MDC.put("logScribe", "123pay_err");
            logScribe.error(logMessage);
            MDC.remove("logScribe");
        }
    }

    @Override
    public void setFunctionName(String funcName) {
        if (this.logValues != null) {
            this.logValues[4] = funcName;
        }
    }

}
