
package vng.paygate.config.service.impl;

import com.google.gson.Gson;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang.StringUtils;
import vn._123pay.Result;
import vn._123pay.ResultCode;
import vn._123pay.data.dbms.relational.RDbClient;
import vn._123pay.data.dbms.relational.RDbClientManager;
import vn._123pay.data.dbms.relational.RDbClientName;
import vng.paygate.config.service.IBankService;
import vng.paygate.domain.bo.BoBS;
import vng.paygate.domain.bo.BoBSOtp;
import vng.paygate.domain.bo.BoOrderNew;
import vng.paygate.domain.bo.SPResponseVerifyCard;
import vng.paygate.domain.common.Constants;

/**
 *
 * @author VuongTM
 */
public class BankServiceImpl implements IBankService {

    @Override
    public BoOrderNew LoadOrderVerifyCard(String orderNo) {
        StringBuilder responseCode = new StringBuilder();
        BoOrderNew boOrder = LoadOrderVCard(orderNo, responseCode);
        if(StringUtils.isEmpty(responseCode.toString())){
            boOrder.setResponseCode(Constants.ERROR_5000);
        }else{
            boOrder.setResponseCode(responseCode.toString());
        }
        return boOrder;
    }

    private BoOrderNew LoadOrderVCard(String orderNo, StringBuilder responseCode) {
        String sp = "{call SP_BI_GET_ORDER_V_CARD(?,?,?)}";
        BoOrderNew boOrderNew = new BoOrderNew();
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()) {
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, orderNo);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(2, OracleTypes.CURSOR);

            cs.executeUpdate();
            responseCode.append(cs.getInt(3));
            System.out.println("responseCode: " + responseCode);
            
            Gson gson = new Gson();
            if (responseCode.toString().equals("1")) {
                ResultSet rs = (ResultSet) cs.getObject(2);
                while (rs.next()) {
                    String merchantCode = rs.getString("MERCHANT_CODE");
                    int totalAmount = rs.getInt("TOTAL_AMOUNT");
                    int opAmount = rs.getInt("OP_AMOUNT");
                    String description = rs.getString("ORDER_DESCRIPTION");
                    Date createDate = rs.getDate("CREATED_DATE");
                    int orderStatus = rs.getInt("ORDER_STATUS");
                    String custName = rs.getString("CUSTOMER_NAME");
                    String custAddress = rs.getString("CUSTOMER_ADDRESS");
                    String custPhone = rs.getString("CUSTOMER_PHONE");
                    String accountName = rs.getString("ACCOUNT_LOGIN");
                    String bankCode = rs.getString("BANK_CODE");
                    String subBankCode = rs.getString("SUB_BANK");
                    //                BoOrderNew boOrderNew = new BoOrderNew();
                    boOrderNew.setMerchantCode(merchantCode);
                    boOrderNew.setTotalAmount(totalAmount);
                    boOrderNew.setOpAmount(opAmount);
                    boOrderNew.setDescription(description);
                    boOrderNew.setCreateDate(createDate);
                    boOrderNew.setOrderNo(orderNo);
                    boOrderNew.setOrderStatus(orderStatus);
                    boOrderNew.setCustName(custName);
                    boOrderNew.setCustAddress(custAddress);
                    boOrderNew.setCustPhone(custPhone);
                    boOrderNew.setAccountName(accountName);
                    boOrderNew.setBankCode(bankCode);
                    boOrderNew.setSubbankCode(subBankCode);
                }
                System.out.println("BoOrderNew: " + gson.toJson(boOrderNew));
            } 
            return boOrderNew;
            // Tra ve 3 trang thai:
            // 1: thanh cong
            // -3: order da thuc hien order verifyCard
            // 5000: loi he thong

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return boOrderNew;
        }
    }

    @Override
    public String VerifyCard(BoBS boBS) {
        return CallSPVerifyCard(boBS);
    }

    private String CallSPVerifyCard(BoBS boBS) {
        String sp = "{call SP_BI_BANKSIM_VERIFY_CARD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()) {
            SPResponseVerifyCard spResult = new SPResponseVerifyCard();
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, boBS.getTransactionId());
            cs.setString(2, boBS.getBankCode());
            cs.setString(3, boBS.getCardNo6First());
            cs.setString(4, boBS.getCardNo4Last());
            cs.setString(5, boBS.getCardNo());
            cs.setString(6, boBS.getCardHash());
            cs.setString(7, boBS.getCardHolderName());
            cs.setDate(8, new java.sql.Date(boBS.getExpireDate().getTime()));
            cs.setString(9, boBS.getClientIp());
            cs.setString(10, boBS.getBankResponseCode());
            cs.setInt(11, boBS.getIsSuccess());
            cs.setInt(12, boBS.getiOrderStatus());
            Gson gson = new Gson();
            
            cs.registerOutParameter(13, Types.INTEGER);
            cs.registerOutParameter(14, Types.INTEGER);
            cs.registerOutParameter(15, Types.VARCHAR);
            cs.registerOutParameter(16, Types.VARCHAR);
            
            cs.executeUpdate();
            boBS.setResponseCode(cs.getInt(13));
            boBS.setOrderStatus(cs.getInt(14));
            boBS.setMiNotifyUrl(cs.getString(15));
            boBS.setDescription(cs.getString(16));
            System.out.println("db: " + gson.toJson(boBS));
            if(boBS.getIsSuccess() == 1){
                return String.valueOf(cs.getInt(13));
            }else{
                return String.valueOf(cs.getInt(14));
            }
//            if(!boBS.getBankResponseCode().equals("1")){
//                return String.valueOf(cs.getInt(14));
//            }else{
//                return String.valueOf(cs.getInt(13));
//            }
        } catch (Exception e) {
            boBS.setResponseCode(5000);
            return Constants.ERROR_5000;
        }
    }
    
    @Override
    public BoBSOtp LoadOrderVerifyOTP(String orderNo) {
        StringBuilder responseCode = new StringBuilder();
        BoBSOtp boBSOtp = CallSPLoadOrderVerifyOTP(orderNo, responseCode);
        if((boBSOtp == null) || 
                (StringUtils.isEmpty(responseCode.toString()))){
            boBSOtp = new BoBSOtp();
            boBSOtp.setResponseCode(Constants.ERROR_5000);
        }else
            boBSOtp.setResponseCode(responseCode.toString());

        return boBSOtp;
    }
    
    private BoBSOtp CallSPLoadOrderVerifyOTP(String orderNo, StringBuilder responseCode){
        String sp = "{call SP_BI_BS_GET_ORDER_INFO_V_OTP(?,?,?)}";
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()){
            
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, orderNo);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            
            cs.executeUpdate();
            responseCode.append(cs.getInt(3));
            System.out.println("responseCode: " + responseCode);
            Gson gson = new Gson();
            if(responseCode.toString().equals("1")){
                ResultSet rs = (ResultSet) cs.getObject(2);
                while(rs.next()){
                    String merchantCode = rs.getString("MERCHANT_CODE");
                    int totalAmount = rs.getInt("TOTAL_AMOUNT");
                    int opAmount = rs.getInt("OP_AMOUNT");
                    String description = rs.getString("ORDER_DESCRIPTION");
                    Date createDate = rs.getDate("CREATED_DATE");
                    String custName = rs.getString("CUSTOMER_NAME");
                    String custAddress = rs.getString("CUSTOMER_ADDRESS");
                    String custPhone = rs.getString("CUSTOMER_PHONE");
                    String accountName = rs.getString("ACCOUNT_LOGIN");
                    int orderNoSuffix = rs.getInt("ORDER_NO_SUFFIX");
                    int orderStatus = rs.getInt("ORDER_STATUS");
                    String bankCode = rs.getString("BANK_CODE");
                    String merchantTransactionId = rs.getString("M_TRANSACTIONID");
                    String cardNo = rs.getString("CARD_NO");
                    String subBankCode = rs.getString("SUB_BANK");
                    BoBSOtp boBSOTP = new BoBSOtp();
                    boBSOTP.setOrderNo(orderNo);
                    boBSOTP.setTotalAmount(totalAmount);
                    boBSOTP.setOpAmount(opAmount);
                    boBSOTP.setDescription(description);
                    boBSOTP.setCreateDate(createDate);
                    boBSOTP.setCustName(custName);
                    boBSOTP.setCustAddress(custAddress);
                    boBSOTP.setCustPhone(custPhone);
                    boBSOTP.setAccountName(accountName);
                    boBSOTP.setOrderNoSuffix(orderNoSuffix);
                    boBSOTP.setOrderStatus(orderStatus);
                    boBSOTP.setBankCode(bankCode);
                    boBSOTP.setMerchantTransactionId(merchantTransactionId);
                    boBSOTP.setCardNo(cardNo);
                    boBSOTP.setSubbankCode(subBankCode);
                    System.out.println("BoBSOTP: " + gson.toJson(boBSOTP));
                    return boBSOTP;
                }
                
            }else{
                return null;
            }
            return null;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    @Override
    public void UpdateOtpReinput(BoBS boEIB) {
        CallSPUpdateOTPReInput(boEIB);
    }
    
    private void CallSPUpdateOTPReInput(BoBS boEIB){
        String sp = "{call SP_BI_BS_123PAY_OTP_RE_INPUT(?,?,?,?,?,?,?,?,?,?)}";
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()){
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, boEIB.getTransactionId());
            cs.setString(2, boEIB.getBanksimCode());
            cs.setString(3, boEIB.getBankCode());
            cs.setString(4, boEIB.getBankService());
            cs.setString(5, boEIB.getBankResponseCode());
            cs.setInt(6, boEIB.getNotifyOrQuery());
            cs.setInt(7, boEIB.getNumberInput());
            cs.registerOutParameter(8, Types.INTEGER);
            cs.registerOutParameter(9, Types.INTEGER);
            cs.registerOutParameter(10, Types.VARCHAR);
            
            cs.executeUpdate();
            boEIB.setResponseCode(cs.getInt(8));
            boEIB.setOrderStatus(cs.getInt(9));
            boEIB.setMiNotifyUrl(cs.getString(10));
            
        }catch (Exception e) {
            System.err.println(e.getMessage());
            boEIB.setResponseCode(Integer.parseInt(Constants.ERROR_5000));
        }
    }

    @Override
    public void updateNotify(BoBS boEIB, String notifyStatus) {
        if (notifyStatus.equals(Constants.NOTIFY_PENDING)) {
            CallSPUpdateNotifyPending(boEIB);
        } else if (notifyStatus.equals(Constants.NOTIFY_SUCCESS)) {
            CallSPUpdateNotifySuccess(boEIB);
        } else if (notifyStatus.equals(Constants.NOTIFY_FAIL)) {
            CallSPUpdateNotifyFail(boEIB);
        } else {
            boEIB.setResponseCode(Integer.valueOf(Constants.ERROR_5000));
        }
    }
    
    private void CallSPUpdateNotifyPending(BoBS boEIB){
        String sp = "{call SP_BI_EIB_NOTIFY_PENDING(?,?,?,?,?,?,?)}";
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()){
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, boEIB.getTransactionId());
            cs.setString(2, boEIB.getBankCode());
            cs.setString(3, boEIB.getBankService());
            cs.setString(4, boEIB.getBankResponseCode());
            
            cs.registerOutParameter(5, Types.INTEGER);
            cs.registerOutParameter(6, Types.INTEGER);
            cs.registerOutParameter(7, Types.VARCHAR);
            
            cs.executeUpdate();
            
            boEIB.setNotifyOrQuery(cs.getInt(5));
            boEIB.setResponseCode(cs.getInt(6));
            boEIB.setMiNotifyUrl(cs.getString(7));
            
        }catch (Exception e) {
            System.err.println(e.getMessage());
            boEIB.setResponseCode(Integer.parseInt(Constants.ERROR_5000));
        }
    }
    private void CallSPUpdateNotifySuccess(BoBS boEIB){
        String sp = "{call SP_BI_NOTIFY_SUCCESS_B_TRANSID(?,?,?,?,?,?,?,?)}";
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()){
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, boEIB.getTransactionId());
            cs.setString(2, boEIB.getBankCode());
            cs.setString(3, boEIB.getBankService());
            cs.setString(4, boEIB.getBankResponseCode());
            cs.setString(5, boEIB.getRefNo());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.registerOutParameter(7, Types.INTEGER);
            cs.registerOutParameter(8, Types.VARCHAR);
            
            cs.executeUpdate();
            
            boEIB.setNotifyOrQuery(cs.getInt(6));
            boEIB.setResponseCode(cs.getInt(7));
            boEIB.setMiNotifyUrl(cs.getString(8));
            
        }catch (Exception e) {
            System.err.println(e.getMessage());
            boEIB.setResponseCode(Integer.parseInt(Constants.ERROR_5000));
        }
    }
    private void CallSPUpdateNotifyFail(BoBS boEIB){
        String sp = "{call SP_BI_123PAY_NOTIFY_FAIL(?,?,?,?,?,?,?,?)}";
        try (Connection conn = RDbClientManager.Instance.get(RDbClientName.Oracle).getConnection()){
            CallableStatement cs = conn.prepareCall(sp);
            cs.setString(1, boEIB.getTransactionId());
            cs.setString(2, boEIB.getBankCode());
            cs.setString(3, boEIB.getBankService());
            cs.setString(4, boEIB.getBankResponseCode());
            cs.setInt(5, boEIB.getNotifyOrQuery());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.registerOutParameter(7, Types.INTEGER);
            cs.registerOutParameter(8, Types.VARCHAR);
            
            cs.executeUpdate();
            
            boEIB.setResponseCode(cs.getInt(6));
            boEIB.setOrderStatus(cs.getInt(7));
            boEIB.setMiNotifyUrl(cs.getString(8));
            
        }catch (Exception e) {
            System.err.println(e.getMessage());
            boEIB.setResponseCode(Integer.parseInt(Constants.ERROR_5000));
        }
    }
}
