package vn._123pay.bank;

import java.security.PublicKey;
import vn._123pay.ResultCode;
import vn._123pay.configuration.Config;
import vn._123pay.data.json.JSONObject;
import vn._123pay.security.CMS;
import vn._123pay.security.DSA;
import vn._123pay.security.MDA;
import vn._123pay.security.SecurityUtil;
import vn._123pay.server.jetty.JSONStringRequestProcessor;

/**
 *
 * @author nghiant
 */
abstract class BaseRequestProcessor extends JSONStringRequestProcessor {

    private Long requestTime;

    private boolean isSignatureRequired;
    private boolean oldVerCompatibleRequired;
    private String caller;

    /**
     *
     * @return
     */
    @Override
    protected final String getCaller() {
        return this.caller;
    }

    /**
     *
     * @param value
     */
    protected final void setIsSignatureRequired(boolean value) {
        this.isSignatureRequired = value;
    }

    /**
     *
     * @param value
     */
    protected final void setOldVersionCompatibleRequired(boolean value) {
        this.oldVerCompatibleRequired = value;
    }

    @Override
    protected final ResultCode validateRequestData() {

        JSONObject requestData = super.getRequestData();

        if (this.oldVerCompatibleRequired) {
            return this.validateAPIParams(requestData);
        }

        try {
            String time = requestData.getString("time");
            super.getLogBuilder().append("time=")
                    .append(time)
                    .append(';');
            if (time == null || time.isEmpty()) {
                return ResultCode.InvalidInput;
            }

            this.requestTime = Long.parseLong(time);
        } catch (ClassCastException ex) {
            try {
                this.requestTime = requestData.getLong("time");
                if (this.requestTime == null) {
                    super.getLogBuilder().append("time=null;");

                    return ResultCode.InvalidInput;
                }
            } catch (ClassCastException ccex) {
                super.getLogBuilder().append("Invalid time format;");

                return ResultCode.InvalidInput;
            }
        } catch (NumberFormatException ex) {
            return ResultCode.InvalidInput;
        }

        this.caller = requestData.getString("caller");
        super.getLogBuilder().append("caller=")
                .append(this.caller)
                .append(';');

        if (this.caller == null || this.caller.isEmpty()) {
            return ResultCode.InvalidInput;
        }

        ResultCode validateCallerRetCode = this.validateCaller();
        if (validateCallerRetCode != ResultCode.Success) {
            return validateCallerRetCode;
        }

        JSONObject apiParams;
        try {
            apiParams = requestData.getObject("data");
            if (apiParams == null) {
                super.getLogBuilder().append("data=null;");

                return ResultCode.InvalidInput;
            }
        } catch (ClassCastException ex) {
            super.getLogBuilder().append("data must be a JSONObject;");

            return ResultCode.InvalidInput;
        }

        ResultCode validateChecksumResult
                = this.validateChecksum(requestData.getString("chksum"),
                        Long.toString(this.requestTime) + '|' + this.caller + '|'
                );

        if (ResultCode.Success != validateChecksumResult) {
            return validateChecksumResult;
        }

        return this.validateAPIParams(apiParams);
    }

    private ResultCode validateCaller() {
        String apisPermission = Config.get(this.caller);
        if (apisPermission == null) {
            return ResultCode.Forbidden;
        }

        if (!apisPermission.contains(this.getAPIName() + ";")) {
            return ResultCode.Forbidden;
        }

        return ResultCode.Success;
    }

    private ResultCode validateChecksum(String checksum, String rawData) {
        StringBuilder logBuilder = super.getLogBuilder();

        if (null == checksum || checksum.isEmpty()) {
            logBuilder.append("chksum=")
                    .append(checksum)
                    .append(';');

            return ResultCode.InvalidInput;
        }

        String key = this.caller + "-secretKey";
        String secretKey = Config.get(key);
        if (secretKey == null || secretKey.isEmpty()) {
            logBuilder.append("Missing ")
                    .append(key)
                    .append(" config;");

            return ResultCode.InvalidInput;
        }

        rawData += this.getDataSection();

        String genChecksum
                = SecurityUtil.Instance.generateChecksum(rawData
                        + secretKey,
                        MDA.SHA256);

        if (!checksum.equals(genChecksum)) {
            logBuilder.append("Invalid chksum;");

            return ResultCode.InvalidInput;
        }

        return ResultCode.Success;
    }

    private String getDataSection() {
        String originalString = super.getOriginalString();

        int startIndex = originalString.indexOf("\"data\"");
        startIndex = originalString.indexOf("{", startIndex);

        int endIndex = originalString.indexOf("}", startIndex + 1);
        String subString = originalString.substring(startIndex, endIndex + 1);

        int subObjectsCount = 0;
        int subStartIndex = 0;
        do {
            subStartIndex = subString.indexOf("{", subStartIndex + 1);
            if (subStartIndex == -1) {
                break;
            }

            subObjectsCount++;
        } while (true);

        if (subObjectsCount > 0) {
            for (int i = 0; i < subObjectsCount; i++) {
                endIndex = originalString.indexOf("}", endIndex + 1);
            }

            subString = originalString.substring(startIndex, endIndex + 1);
        }

        return subString;
    }
}
