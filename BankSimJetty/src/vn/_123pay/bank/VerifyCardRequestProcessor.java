package vn._123pay.bank;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.xml.ws.client.ClientTransportException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.apache.commons.lang.StringUtils;
import vn._123pay.Result;
import vn._123pay.ResultCode;
import vn._123pay.data.json.JSONObject;
import vng.paygate.bank.Abstract.BankAbstract;
import vng.paygate.bank.Abstract.BankFactory;
import vng.paygate.bank.jaxb.adapter.BoBIModuleConfigNew;
import vng.paygate.bank.jaxb.adapter.BoBaseBankNew;
import vng.paygate.config.service.impl.BankServiceImpl;
import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.bo.BoBaseResponse;
import vng.paygate.domain.bo.BoMiNotifyResponse;
import vng.paygate.domain.bo.BoOrderNew;
import vng.paygate.domain.bo.BoProcessPaymentResponse;
import vng.paygate.domain.common.CommonService;
import vng.paygate.domain.common.ConstantBS;
import vng.paygate.domain.common.Constants;
import vng.paygate.domain.common.DateUtils;
import vng.paygate.domain.common.RC4;
import vng.paygate.domain.log.impl.LogService;
import vng.paygate.exception.TechniqueException;
import vng.paygate.notify.NotifyHelper;
import vng.paygate.security.ChecksumHelper;
import vng.paygate.security.SecurityHelper;

/**
 *
 * @author nghiant
 */
final class VerifyCardRequestProcessor extends BaseRequestProcessor {

    private String orderNo;
    private String cardInfo;
    private String clientIp;
    private String checksum;
    BoBaseResponse boBaseResponse;
    BoProcessPaymentResponse boResponse = new BoProcessPaymentResponse();
    SecurityHelper securityH;
    BankServiceImpl bankService;
    BoBS boEIB = new BoBS();
    Gson gson = new Gson();

    /**
     *
     */
    VerifyCardRequestProcessor(SecurityHelper security, BankServiceImpl bankservice) {
        this.bankService = bankservice;
        this.securityH = security;
        super.setOldVersionCompatibleRequired(true);
    }

    /**
     *
     * @param apiParams
     * @return
     */
    @Override
    protected ResultCode validateAPIParams(JSONObject apiParams) {
        System.out.println("validateAPIParams");

        orderNo = apiParams.getString("orderNo");
        cardInfo = apiParams.getString("cardInfo");
        clientIp = StringUtils.defaultIfEmpty(apiParams.getString("clientIp"), "");
        checksum = apiParams.getString("checksum");

        Constants.appendParams(super.getLogBuilder(), apiParams.toString());

        boEIB.setClientIp(clientIp);

        Constants.appendMessage(super.getLogBuilder(), "validateParams");
        //validate required paramters from MI
        String responseCode = validate(orderNo, cardInfo, checksum);
        Constants.appendParams(super.getLogBuilder(), responseCode);
        if (!Constants.RESPONSE_CODE_1.equals(responseCode)) {
            //return list of response code,123Pay transaction id, url input card
            boBaseResponse = Service.commonService.getResponse(responseCode);
            if (responseCode.equals(Constants.ERROR_6101)) {
                return ResultCode.DataInputEmtyBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            } else {
                return ResultCode.OrderNoTooLongBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            }
        }
        //Check IP Client.
        Constants.appendMessage(super.getLogBuilder(), "validateCheckSum");
        // Check CheckSum
        // Create RawData.  Nen dua vao Map roi lam lai
        String rawData = "";
//        rawData += cardInfo;
//        rawData += clientIp;
//        rawData += orderNo;
        Map<String, String> treeMap = new TreeMap<String, String>();
        treeMap.put("orderNo", orderNo);
        treeMap.put("cardInfo", cardInfo);
        treeMap.put("clientIp", clientIp);
        for (String item : treeMap.keySet()) {
            if (!StringUtils.isEmpty(item)) {
                rawData += treeMap.get(item);
            }
        }

        boBaseResponse = securityH.VerifyChecksum(Constants.ALGORITHM_SHA1, rawData, checksum, Constants.INTERNAL_MODULE);
        if (!Constants.RESPONSE_CODE_1.equals(boBaseResponse.getDetailResponseCode())) {
            boBaseResponse = Service.commonService.getResponse(boBaseResponse.getDetailResponseCode());
            if (responseCode.equals(Constants.ERROR_5000)) {
                return ResultCode.SystemError.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
            }
            return ResultCode.InvalidCheckSumBankSim.setGroupResponseCode(Integer.parseInt(boBaseResponse.getGroupResponseCode()));
        }
        Constants.appendParams(super.getLogBuilder(), boBaseResponse.getDetailResponseCode());

        return ResultCode.Success;
    }

    /**
     *
     * @param <V>
     * @return
     */
    @Override
    protected Result<String> execute() {
        //query order from database
        Constants.appendMessage(super.getLogBuilder(), "loadOrderVerifyCard");
        BoOrderNew boOrder = bankService.LoadOrderVerifyCard(orderNo);

        Constants.appendParams(super.getLogBuilder(), boOrder.getResponseCode());
        if (!Constants.RESPONSE_CODE_1.equals(boOrder.getResponseCode())) {
            if (boOrder.getResponseCode().equals("-3")) {     // Da thanh toan
                boBaseResponse = Service.commonService.getResponse(Constants.ERROR_7200);
            } else if (boOrder.getResponseCode().equals(Constants.ERROR_5000)) {
                boBaseResponse = Service.commonService.getResponse(boOrder.getResponseCode());
            } else {         // Da VerifyCard
                boBaseResponse = Service.commonService.getResponse(Constants.ERROR_7200);
            }
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        }
        Constants.appendMessage(super.getLogBuilder(), "loadConfig");
        BoBIModuleConfigNew boBIConfig = (BoBIModuleConfigNew) Service.configService.getModuleConfig();
        BoBaseBankNew boBank = boBIConfig.getBankCodeMap().get(boOrder.getBankCode());
        if (boBank == null) {
            boBank = boBIConfig.getBankCodeMap().get("EIB");
        }
        Constants.appendMessage(super.getLogBuilder(), "verifyEncryptedMessage");
        String responseCode = verifyEncryptedMessage(cardInfo, boEIB, boBank.getRc4SecretKey(), super.getLogBuilder());
//        boBaseResponse = Service.commonService.getResponse(responseCode);
        Constants.appendParams(super.getLogBuilder(), responseCode);
        if (!Constants.RESPONSE_CODE_1.equals(responseCode)) {
            boBaseResponse = Service.commonService.getResponse(responseCode);
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        }

        //copy boOrder to boEIB
        copyBoOrderToBoEIB(boOrder, boEIB);
        Constants.appendMessage(super.getLogBuilder(), "processCardInfo", boEIB.getCardNo());
        responseCode = processCardInfo(boEIB, boBank);
        boBaseResponse = Service.commonService.getResponse(responseCode);
        if (!responseCode.equals("1")
                && boOrder.getOrderNoSuffix() != null
                && boOrder.getOrderNoSuffix().intValue() >= (Integer.parseInt(boBank.getMaxInput()) - 1)) {

            Constants.appendParams(super.getLogBuilder(), Constants.ERROR_7221, "Can't input incorrect card " + boBank.getMaxInput() + " times");
            boBaseResponse = Service.commonService.getResponse(Constants.ERROR_7221);
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        }

        if (!responseCode.equals("1")) {
            if (boEIB.getIsSuccess() == -2 && (Constants.ERROR_7232.equals(responseCode)
                    || Constants.ERROR_7231.equals(responseCode)
                    || Constants.ERROR_7211.equals(responseCode)
                    || Constants.ERROR_7212.equals(responseCode)
                    || Constants.ERROR_7201.equals(responseCode)
                    || Constants.ERROR_7300.equals(responseCode)
                    || Constants.RESPONSE_TIME_OUT.equals(responseCode))) {
                Constants.appendParams(super.getLogBuilder(), responseCode, "Invalid card info/Allow re-input card info");
                boBaseResponse = Service.commonService.getResponse(responseCode);
                return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
            } else if (Constants.ERROR_7302.equals(responseCode)
                    || Constants.ERROR_7007.equals(responseCode)
                    || Constants.ERROR_7100.equals(responseCode)
                    || Constants.ERROR_7221.equals(responseCode)
                    || Constants.INVALID_TRANX_RESULT.equals(responseCode)) {
                // return error code for show match message => notify merchant => return payport url
                boBaseResponse = Service.commonService.getResponse(responseCode);
                notifyMerchant(boEIB, boBaseResponse, boOrder);
                boBaseResponse = Service.commonService.getResponse(boBaseResponse.getDetailResponseCode());
                //build response result
                buildResponse(boResponse, boOrder);
                buildResponse(boResponse, boBaseResponse);
                //build payport URL
                String payportURL = boBank.getPayportURL() + "?" + ConstantBS.TRANX_REF_PARAM + "=" + boOrder.getOrderNo();
                Constants.appendMessage(super.getLogBuilder(), "RedirectURL:" + payportURL);
                boResponse.setRedirectURL(payportURL);
                //update order status
                Constants.appendMessage(super.getLogBuilder(), "Result");
                Constants.appendParams(super.getLogBuilder(), boResponse.getGroupResponseCode(), boResponse.getDetailResponseCode(),
                        boResponse.getOrderNo(), boResponse.getMerchantTransactionId(),
                        Integer.toString(boResponse.getOrderStatus()),
                        Integer.toString(boResponse.getNotifyStatus()),
                        Integer.toString(boResponse.getTotalAmount()),
                        Integer.toString(boResponse.getOpAmount()), boResponse.getBankCode(),
                        boResponse.getMerchantCode(), boResponse.getBankResponseCode(),
                        boResponse.getDescription(),
                        boResponse.getRedirectURL());

                return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);
            } else {
                buildResponse(boResponse, boBaseResponse);
                //build payport URL
                String payportURL = boBank.getPayportURL() + "?" + ConstantBS.TRANX_REF_PARAM + "=" + boOrder.getOrderNo();
                Constants.appendMessage(super.getLogBuilder(), "RedirectURL:" + payportURL);
                boResponse.setRedirectURL(payportURL);
                Constants.appendParams(super.getLogBuilder(), boResponse.getDetailResponseCode(), "Verify Card fail / Internal Error",
                        boResponse.getRedirectURL());
                return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);
            }
        }
        // Set congif result success
        boBaseResponse = Service.commonService.getResponse(responseCode);
        buildResponse(boResponse, boBaseResponse);
        boResponse.setOrderNo(orderNo);
        if (boEIB.getOrderStatus() == 20) {
            boResponse.setAuthSite("BANK");
            boResponse.setVerifyOtpURL("https://sandbox2.123pay.vn/otp.php?id=" + boResponse.getOrderNo());
        }
        if (boEIB.getOrderStatus() != 20 && boEIB.getOrderStatus() != 11) {
            boResponse.setDetailResponseCode(String.valueOf(boEIB.getOrderStatus()));
        }

        String checksumReturn = buildChecksum(boResponse);
        if (checksumReturn == null
                || StringUtils.isEmpty(checksumReturn)) {
            boBaseResponse = Service.commonService.getResponse(responseCode);
            Constants.appendParams(super.getLogBuilder(), boBaseResponse.getDetailResponseCode(), boBaseResponse.getDetailDescription());
            return new Result<>("", gson.toJson(boBaseResponse), null).setIsOldFashion(true);
        }
        boResponse.setChecksum(checksumReturn);
        Constants.appendParams(super.getLogBuilder(), boResponse.getDetailResponseCode(), "Verify card success");

        return new Result<>("", gson.toJson(boResponse), null).setIsOldFashion(true);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getAPIName() {
        return Service.VERIFY_CARD;
    }

    private String validate(String orderNo, String cardInfo, String checksum) {
        if (StringUtils.isEmpty(orderNo)
                || StringUtils.isEmpty(cardInfo)
                || StringUtils.isEmpty(checksum)) {
            return Constants.ERROR_6101;
        }

        if (orderNo.length() > 17) {
            return Constants.ERROR_6100;
        }

        return Constants.RESPONSE_CODE_1;
    }

    private String verifyEncryptedMessage(String encryptData, BoBS boEIB, String secrectKeyRC4, StringBuilder logBuilder) {
        SecretKey secretKey = RC4.generateKey(secrectKeyRC4);
        String cardInfo;

        String params[];
        if (secretKey != null) {
            try {
                cardInfo = RC4.decrypt(encryptData, secretKey);
            } catch (TechniqueException ex) {
                return Constants.ERROR_5000;
            }

            Constants.appendMessage(logBuilder, "Card information: " + cardInfo);
            if (cardInfo == null || cardInfo.equals("")) {
                Constants.appendMessage(logBuilder, "Card information invalid: " + cardInfo);
                return Constants.ERROR_6007;
            }
            if (cardInfo.contains("=")) {
                params = StringUtils.split(cardInfo, ConstantBS.SEPARATOR_EIB);
            } else {
                params = StringUtils.split(cardInfo, ConstantBS.SEPARATOR_EIB1);
            }
            if (params != null) {
                String cardNo = "";
                String cardHolderName = "";
                if (params.length == 2 || params.length == 3) {
                    cardNo = params[0];
                    cardHolderName = params[1];
                } else if (params.length == 4) {
                    cardNo = params[0];
                    cardHolderName = params[2];
                }
                if (cardNo.equals("") || cardHolderName.equals("")) {
                    Constants.appendMessage(logBuilder, "Invalid card no/card holder name");
                    return Constants.ERROR_6007;
                }

                if (!StringUtils.isNumeric(cardNo)) {
                    Constants.appendMessage(logBuilder, "Card no must be numeric");
                    return Constants.ERROR_6007;
                }

                if (!StringUtils.isAlphanumericSpace(cardHolderName) || !StringUtils.isAsciiPrintable(cardHolderName)) {
                    Constants.appendMessage(logBuilder, "Card holder name must be ascii alphabet numeric space");
                    return Constants.ERROR_6007;
                }
                cardNo = cardNo.trim();
                boEIB.setCardNo(cardNo);
                boEIB.setCardNo6First(cardNo.substring(0, 6));
                boEIB.setCardNo4Last(cardNo.substring(cardNo.length() - 4, cardNo.length()));
                boEIB.setCardHash(ChecksumHelper.generateChecksum(ChecksumHelper.ALGORITHM_SHA1, cardNo));
                boEIB.setCardHolderName(cardHolderName.trim());
                boEIB.setExpireDate(DateUtils.getDate(ConstantBS.DEFAULT_EXPIRED_DATE_FULL));

                return Constants.RESPONSE_CODE_1;
            } else {
                Constants.appendMessage(logBuilder, "Number of parameter in encrypt message wrong : " + params.length);
                return Constants.ERROR_6007;
            }
        }
        return Constants.ERROR_5000;
    }

    private void copyBoOrderToBoEIB(BoOrderNew boOrder, BoBS boEIB) {
//        boEIB.setAmount(boOrder.getTotalAmount());
        boEIB.setAmount(boOrder.getOpAmount());
        //Cause TIPSS compare int, not string so we remove 4 first characters in 123Pay transaction id.
        String transId = boOrder.getOrderNo().substring(4);
//        String transId = boOrder.getOrderNo();
        //for test
        if (boOrder.getOrderNoSuffix() != null && boOrder.getOrderNoSuffix().intValue() > 0) {
            transId = transId + "" + boOrder.getOrderNoSuffix();
        }
        boEIB.setTransactionIdSuffix(transId);
        boEIB.setTransactionId(boOrder.getOrderNo());
        boEIB.setMerchantCode(boOrder.getMerchantCode());
        boEIB.setExpireDate(boOrder.getCreateDate());
        boEIB.setBankCode(boOrder.getBankCode());
        boEIB.setCustomerId(null);
        String subBankCode = boOrder.getSubbankCode();
        subBankCode = subBankCode.substring(4, subBankCode.length());
        boEIB.setBanksimCode(subBankCode);
    }

    private String processCardInfo(BoBS boEIB, BoBaseBankNew boBank) {
        try {
            Constants.appendMessage(super.getLogBuilder(), "processCardInfo");
            if (boEIB.getCardNo().length() != 16) {
                return Constants.ERROR_6100;
            }
            String cardNo = boEIB.getCardNo();
            String cardHolderName = boEIB.getCardHolderName();
            String checkPrefix = boEIB.getCardNo().substring(10, 16);
            String checkPrefixCardNo = Constants.RESPONSE_CODE_1;
            Date expiredDate = boEIB.getExpireDate();
            String cashHash = boEIB.getCardHash();

            removeCardInfo(boEIB);

            Constants.appendMessage(super.getLogBuilder(), "checkAllowCardInfo");
            BankAbstract bank = BankFactory.createBank(boEIB.getBanksimCode());

            String checkAllowCardInfo = bank.checkAllowCardInfo(super.getLogBuilder(), cardHolderName, cardNo, boBank, boEIB.getBanksimCode());
            Constants.appendMessage(super.getLogBuilder(), checkAllowCardInfo);
            if (checkAllowCardInfo.equals("1")) {
                boEIB.setBankResponseCode(Constants.RESPONSE_CODE_1);
                boEIB.setIsSuccess(1);
                Constants.appendMessage(super.getLogBuilder(), Constants.RESPONSE_CODE_1);
                boEIB.setCardNo(cardNo);
                boEIB.setCardHolderName(cardHolderName);
                boEIB.setExpireDate(expiredDate);
                boEIB.setCardHash(cashHash);
            } else {
                if (!ConstantBS.ERROR_VERIFY_CARD_10.equals(checkAllowCardInfo)) {
                    boEIB.setBankResponseCode(checkAllowCardInfo);
                } else {
                    boEIB.setBankResponseCode(ConstantBS.ERROR_VERIFY_CARD_10);
                }
                Constants.appendMessage(super.getLogBuilder(), "set -2 line 282");
                boEIB.setIsSuccess(-2);
                boEIB.setExpireDate(expiredDate);
            }
            Constants.appendMessage(super.getLogBuilder(), "SP_BI_BANKSIM_VERIFY_CARD");
            String bankCode = boEIB.getBankCode();
            int iOrderStatus = 11;
            if (boEIB.getIsSuccess() == 1) {
                if (boBank.getSubBanks().get(boEIB.getBanksimCode()).getIsInputOTP().equals("1")) {
                    iOrderStatus = 20;
                }
            } else {
                try {
                    iOrderStatus = Integer.parseInt(boEIB.getCardNo4Last());
                } catch (Exception ex) {
                    iOrderStatus = 6100;
                }
            }
            boEIB.setiOrderStatus(iOrderStatus);

            Constants.appendMessage(super.getLogBuilder(), "boEIB.getiOrderStatus:" + boEIB.getiOrderStatus() + "Last:" + boEIB.getCardNo4Last());
            Constants.appendMessage(super.getLogBuilder(), "boEIB.getIsSuccess:" + boEIB.getIsSuccess());
            // Call BankService SP_BI_BANKSIM_VERIFY_CARD
            boEIB.setBankCode(boEIB.getBanksimCode());
            
            String responseCode = bankService.VerifyCard(boEIB);
            boEIB.setBankCode(bankCode);
            System.out.println("boIEB after db: " + gson.toJson(boEIB));

            if (!responseCode.equals("1")) {
                Constants.appendParams(super.getLogBuilder(), responseCode == null ? "null" : "" + responseCode);
                Constants.appendMessage(super.getLogBuilder(), "processCardInfo");
                Constants.appendMessage(super.getLogBuilder(), responseCode);
                return responseCode;
            }
            Constants.appendParams(super.getLogBuilder(), "getResponseCode: " + responseCode, "getOrderStatus:" + boEIB.getOrderStatus().intValue(), boEIB.getMiNotifyUrl());

            Constants.appendMessage(super.getLogBuilder(), "processCardInfo");
            Constants.appendParams(super.getLogBuilder(), responseCode);
            return responseCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            Constants.appendMessage(super.getLogBuilder(), "processCardInfo");
            Constants.appendParams(super.getLogBuilder(), Constants.ERROR_5000, ex.toString());
            return Constants.ERROR_5000;
        }
    }

    private void removeCardInfo(BoBS boEIB) {
        Constants.appendMessage(super.getLogBuilder(), "removeCardInfo");
        boEIB.setCardNo(null);
        boEIB.setCardHolderName(null);
        boEIB.setExpireDate(null);
        boEIB.setCardHash(null);
        Constants.appendMessage(super.getLogBuilder(), Constants.RESPONSE_CODE_1);
    }

    private void notifyMerchant(BoBS boEIB, BoBaseResponse boResponse, BoOrderNew boOrder) {
        String rawData;
        /*
         * call notify to merchant to inform status of order
         */
        rawData = boEIB.getTransactionId();

        String checksumMerchant = securityH.CreateCheckSumMerhchant(Constants.ALGORITHM_SHA1, rawData, Constants.INTERNAL_MODULE);
        Constants.appendParams(super.getLogBuilder(), boEIB.getMiNotifyUrl(), boEIB.getTransactionId(), checksumMerchant);
        Constants.appendMessage(super.getLogBuilder(), "invokeNotifyMerchant");
        // check status of notify merchant and order status
        BoMiNotifyResponse boMiNotify;
        try {
            boMiNotify = invokeNotifyMerchant(boEIB.getMiNotifyUrl(), boEIB.getTransactionId(), checksumMerchant);
            boOrder.setNotifyStatus(0);
            Constants.appendParams(super.getLogBuilder(), boMiNotify.getGroupResponseCode(), boMiNotify.getDetailResponseCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            boResponse.setGroupResponseCode(Constants.ERROR_5000);
            boResponse.setDetailResponseCode(Constants.ERROR_5001);
            Constants.appendParams(super.getLogBuilder(), Constants.ERROR_5000);
            if (ex instanceof ClientTransportException || ex instanceof ConnectException
                    || ex instanceof RemoteException || ex instanceof SocketException
                    || ex instanceof IOException || ex instanceof ClientHandlerException) {
                // connection timeout
                Constants.appendParams(super.getLogBuilder(), Constants.RESPONSE_TIME_OUT);
            } else {
                // exception
                Constants.appendParams(super.getLogBuilder(), Constants.ERROR_5000);
            }
        }
    }

    private BoMiNotifyResponse invokeNotifyMerchant(String notifyUrl, String orderNo, String dataSign) throws Exception {
        BoMiNotifyResponse bo = new BoMiNotifyResponse(Constants.RESPONSE_CODE_1, Constants.RESPONSE_CODE_1);
        JsonObject json = new JsonObject();
        json.addProperty("orderNo", orderNo);
        json.addProperty("checksum", dataSign);
        NotifyHelper notifyH = new NotifyHelper();
        String result = notifyH.postNotify(notifyUrl, json, "application/json", "application/json");
        if (StringUtils.isEmpty(result)) {
            bo.setDetailResponseCode(Constants.ERROR_5000);
            bo.setGroupResponseCode(Constants.ERROR_5000);
        } else {
            bo = gson.fromJson(result, BoMiNotifyResponse.class);
        }

        return bo;
    }

    private void buildResponse(BoProcessPaymentResponse boResponse, BoOrderNew boOrder) {
        boResponse.setOrderNo(boOrder.getOrderNo());
        boResponse.setOrderStatus(boOrder.getOrderStatus());
        boResponse.setMerchantTransactionId(boOrder.getMerchantTransactionId());
        boResponse.setTotalAmount(boOrder.getTotalAmount());
        boResponse.setOpAmount(boOrder.getOpAmount());
        boResponse.setBankCode(boOrder.getBankCode());
        boResponse.setMerchantCode(boOrder.getMerchantCode());
        boResponse.setBankResponseCode(boOrder.getBankResponseCode());
        boResponse.setDescription(boOrder.getDescription());
        boResponse.setNotifyStatus(boOrder.getNotifyStatus() == null ? 0 : boOrder.getNotifyStatus());
    }

    private void buildResponse(BoProcessPaymentResponse boResponse, BoBaseResponse boBase) {
        boResponse.setOrderNo(boBase.getOrderNo());
        boResponse.setGroupResponseCode(boBase.getGroupResponseCode());
        boResponse.setDetailResponseCode(boBase.getDetailResponseCode());
        boResponse.setGroupDescription(boBase.getGroupDescription());
        boResponse.setDetailDescription(boBase.getDetailDescription());
    }

    private String buildChecksum(BoProcessPaymentResponse boResponse) {
        ObjectMapper m = new ObjectMapper();
        Map<String, String> props = m.convertValue(boResponse, TreeMap.class);
        props.remove("checksum");
        String rawData = "";
        for (String item : props.keySet()) {
            rawData += props.get(item);
        }

//        appendParams(logMessage, rawData);
        Constants.appendMessage(super.getLogBuilder(), "createSignature");
        //Just pass raw data to verify. ISignature try to load key by its self
        String checksum = securityH.CreateCheckSumMerhchant(Constants.ALGORITHM_SHA1, rawData, Constants.INTERNAL_MODULE);
        return checksum;
    }
}
