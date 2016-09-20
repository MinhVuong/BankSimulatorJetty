package vn._123pay.bank;

//package vn._123pay.merchant;
//
//import java.util.concurrent.TimeoutException;
//import vn._123pay.Result;
//import vn._123pay.ResultCode;
//import vn._123pay.configuration.Config;
//import vn._123pay.data.json.JSONObject;
//import vn._123pay.payment.CardStatus;
//import vn._123pay.payment.FieldName;
//import vn._123pay.payment.Order;
//import vn._123pay.payment.OrderStatus;
//import vn._123pay.payment.Orders;
//import vn._123pay.payment.bank.Cybersource;
//import vn._123pay.payment.util.DefaultCardEncryptor;
//import vn._123pay.service.cardstore.CardStoreClient;
//import vn._123pay.service.payment.PaymentGWClient;
//
///**
// *
// * @author nghiant
// */
//final class PayByTokenRequestProcessor extends BaseRequestProcessor {
//
//    private String orderNo;
//    private String tokenNo;
//
//    /**
//     *
//     */
//    PayByTokenRequestProcessor() {
//        super.setIsSignatureRequired(true);
//    }
//
//    /**
//     *
//     * @param apiParams
//     * @return
//     */
//    @Override
//    protected ResultCode validateAPIParams(JSONObject apiParams) {
//        StringBuilder logBuilder = super.getLogBuilder();
//
//        this.orderNo = apiParams.getString(Order.NUMBER_FIELD_NAME);
//        logBuilder.append(Order.NUMBER_FIELD_NAME)
//                .append('=')
//                .append(this.orderNo)
//                .append(';');
//
//        if (this.orderNo == null || this.orderNo.isEmpty()) {
//            return ResultCode.InvalidInput;
//        }
//
//        this.tokenNo = apiParams.getString("token");
//        logBuilder.append("token=")
//                .append(this.tokenNo)
//                .append(';');
//
//        if (this.tokenNo == null || this.tokenNo.isEmpty()) {
//            return ResultCode.InvalidInput;
//        }
//
//        String merchantCode = super.getCaller();
//        if (!"ZALOPAY".equals(merchantCode) && !"TRUSTCARDVN".equals(merchantCode)) {
//            try {
//                this.tokenNo = new DefaultCardEncryptor(Config.get(merchantCode + "-card-encryptKey"),
//                        Config.get(merchantCode + "-card-encryptIV")).decrypt(this.tokenNo);
//                logBuilder.append("rawToken=")
//                        .append(this.tokenNo)
//                        .append(';');
//
//                if (this.tokenNo == null || tokenNo.isEmpty()) {
//                    return ResultCode.InvalidInput;
//                }
//            } catch (Throwable ex) {
//                logBuilder.append("Invalid tokenNo (")
//                        .append(ex.getMessage())
//                        .append(");");
//
//                return ResultCode.InvalidInput;
//            }
//        }
//
//        Order order = Orders.getByOrderNo(orderNo,
//                logBuilder);
//        if (order == null) {
//            return ResultCode.InvalidInput;
//        }
//
//        JSONObject getTokenRet = CardStoreClient.Instance.getToken(this.tokenNo,
//                merchantCode,
//                logBuilder);
//
//        if (getTokenRet == null) {
//            return ResultCode.InvalidInput;
//        }
//
//        int getTokenRetCode = getTokenRet.getInt("retCode");
//        if (!ResultCode.Success.equals(getTokenRetCode)) {
//            if (-10 == getTokenRetCode) { // token not found
//                return ResultCode.InvalidInput;
//            }
//
//            return ResultCode.SystemError;
//        }
//
//        int cardStatus;
//        JSONObject tokenInfo = getTokenRet.getObject("data");
//        int tokenStatus = tokenInfo.getInt("pubStat");
//        logBuilder.append("pubStat=")
//                .append(Integer.toString(tokenStatus))
//                .append(';');
//
//        switch (tokenStatus) {
//            case CardStatus.ACTIVE: // public active -> check private
//                cardStatus = tokenInfo.getInt("priStat");
//                logBuilder.append("priStat=")
//                        .append(Integer.toString(cardStatus))
//                        .append(';');
//
//                if (cardStatus == CardStatus.EXPIRES) {
//                    return ResultCode.TokenExpired;
//                }
//
//                break;
//
//            case CardStatus.EXPIRES:
//                return ResultCode.TokenExpired;
//
//            default:
//                return ResultCode.InvalidInput;
//        }
//
//        int orderStatus = order.getStatus();
//        switch (orderStatus) {
//            case OrderStatus.NEW:
//                String tokenRealBankCode = tokenInfo.getString(FieldName.BANK);
//                String subBankCode;
//                switch (tokenRealBankCode) {
//                    case Cybersource.CODE:
//                        subBankCode = Cybersource.SUB_CODE;
//                        break;
//                    default:
//                        if (Service.getVTB2BankCode().equals(tokenRealBankCode)) {
//                            if (cardStatus == CardStatus.NEW) {
//                                String manualTokenMappingList = Config.get("manual-mapping-list");
//                                if (manualTokenMappingList == null || manualTokenMappingList.isEmpty()) {
//                                    subBankCode = Config.get("vtb2-sbc");
//                                } else {
//                                    try {
//                                        JSONObject manualMappingList = JSONObject.fromString(manualTokenMappingList);
//                                        String bankToken = manualMappingList.getString(this.tokenNo);
//                                        if (bankToken == null || bankToken.isEmpty()) {
//                                            subBankCode = Config.get("vtb2-sbc");
//                                        } else {
//                                            logBuilder.append("Active manual mapping;");
//
//                                            subBankCode = Config.get("vtb-sbc");
//                                        }
//                                    } catch (Throwable ex) {
//                                        logBuilder.append("Invalid manualMappingList;");
//
//                                        subBankCode = Config.get("vtb-sbc");
//                                    }
//                                }
//                            } else {
//                                subBankCode = Config.get("vtb2-sbc");
//                            }
//
//                            logBuilder.append("subBank=")
//                                    .append(subBankCode)
//                                    .append(';');
//
//                            break;
//                        }
//
//                        //WARN: maybe not correct
//                        subBankCode = new StringBuilder("123P")
//                                .append(tokenRealBankCode)
//                                .toString();
//
//                        break;
//                }
//
//                Result<String> updateBankRet = order.updateBankCode(subBankCode,
//                        logBuilder
//                );
//
//                ResultCode updateBankCode = updateBankRet.getCode();
//                if (ResultCode.Success != updateBankCode) {
//                    return updateBankCode;
//                }
//
//                Result<JSONObject> verifyPaymentRet
//                        = order.verifyPayment(Config.get("internal-secretKey"),
//                                logBuilder);
//                ResultCode verifyPaymentRetCode = verifyPaymentRet.getCode();
//                if (ResultCode.Success != verifyPaymentRetCode) {
//                    return verifyPaymentRetCode;
//                }
//
//                order.saveRedirectURLForIB(tokenRealBankCode,
//                        verifyPaymentRet.getData().getString("redirectURL"),
//                        super.getLogBuilder()
//                );
//
//                break;
//
//            case OrderStatus.VERIFIED_PAYMENT:
//            case OrderStatus.VERIFIED_CARD:
//                break;
//
//            case OrderStatus.BANK_PROCESSING:
//                subBankCode = order.getSubBankCode();
//                if (Config.get("ib-list").contains(new StringBuilder().append(';')
//                        .append(subBankCode)
//                        .append(';')
//                        .toString())) {
//                    break;
//                }
//
//            default:
//                logBuilder.append("Invalid orderStatus(")
//                        .append(Integer.toString(orderStatus))
//                        .append(");");
//
//                return ResultCode.BusinessRuleViolation;
//        }
//
//        return ResultCode.Success;
//    }
//
//    /**
//     *
//     * @param <V>
//     * @return
//     */
//    @Override
//    protected Result<JSONObject> execute() {
//        StringBuilder logBuilder = super.getLogBuilder();
//
//        try {
//            JSONObject payRet
//                    = PaymentGWClient.Instance.payByToken(this.orderNo,
//                            this.tokenNo,
//                            logBuilder
//                    );
//
//            int payRetCode = payRet.getInt("retCode");
//            if (!ResultCode.Success.equals(payRetCode)) {
//                logBuilder.append("payByToken return ")
//                        .append(Integer.toString(payRetCode))
//                        .append(';');
//
//                if (3 == payRetCode) {
//                    return new Result<>(ResultCode.RedirectRequired,
//                            payRet.getObject("data"),
//                            null
//                    );
//                }
//
//                if (4 == payRetCode) {
//                    return new Result<>(ResultCode.RequiredData);
//                }
//
//                return new Result<>(Integer.toString(payRetCode),
//                        payRet.getObject("data"),
//                        null
//                );
//            }
//
//            return new Result<>(ResultCode.Success,
//                    payRet.getObject("data"),
//                    null
//            );
//        } catch (TimeoutException ex) {
//            logBuilder.append("payByToken timeout;");
//
//            return new Result<>(ResultCode.Timeout);
//        }
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    protected String getAPIName() {
//        return Service.PAY_BY_TOKEN_API;
//    }
//}
