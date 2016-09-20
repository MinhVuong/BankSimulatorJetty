package vng.paygate.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import vn._123pay.bank.Service;
import vng.paygate.bank.jaxb.adapter.BoBIModuleConfigNew;
import vng.paygate.domain.bo.BoBaseResponse;
import vng.paygate.domain.common.Constants;

/**
 *
 * @author VuongTM
 */
public class SecurityHelper {
    public String secrectKey;
    public String privateKey;
    public String publicKey;

    public SecurityHelper() {
    }
    
    public BoBaseResponse VerifyChecksum(String nameAlgorithm, String rawData, String checksum, String categorySecrectKey){
        BoBaseResponse boBaseResponse = new BoBaseResponse();
        try{
            BoBIModuleConfigNew boBIConfig = (BoBIModuleConfigNew)Service.configService.getModuleConfig();
            String secrectKey = "b";
            if(categorySecrectKey.equals(Constants.INTERNAL_MODULE)){
                secrectKey = boBIConfig.getSecretKey();
            }else
                secrectKey = boBIConfig.getFrontEndSecretKey();
            rawData += secrectKey;
            String myCheckSum =  CreateCheckSum(nameAlgorithm, rawData);
            if(myCheckSum.equals(checksum)){
                boBaseResponse.setDetailResponseCode("1");
            }else{
                boBaseResponse.setDetailResponseCode("6004");
            }
        }catch(Exception e){
            boBaseResponse.setDetailResponseCode("5000");
        }
        return boBaseResponse;
    }
    
    
    
    private String CreateCheckSum(String nameChecksum, String rawData){
//        Gson gson = new Gson();
//        System.out.println("Data Cheksum: " + jObj.toString());
        try{
            System.out.println("RawData: " + rawData);
            byte[] encrypt = rawData.getBytes("UTF-8");
            String result = "";
            if(nameChecksum.equals("chksum-SHA-256")){
                result = SHA256(encrypt);
            }else if(nameChecksum.equals("chksum-SHA-512")){
                result = SHA512(encrypt);
            }else if(nameChecksum.equals("SHA-1")){
                result = SHA1(encrypt);
            }else if(nameChecksum.equals("chksum-MD5")){
                result = MD5(encrypt);
            }
            return result;
        } catch(Exception e){
            System.err.println(e.getMessage());
            return "";
        }
    }
    
    private String SHA256(byte[] str) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(str);
        byte byteData[] = md.digest();
        
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private String SHA512(byte[] str) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(str);
        byte byteData[] = md.digest();
        
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private String SHA1(byte[] str) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(str);
        byte byteData[] = md.digest();
        
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
    
    private  String MD5(byte[] str) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str);
        byte byteData[] = md.digest();
        
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void setSecrectKey(String secrectKey) {
        this.secrectKey = secrectKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String CreateCheckSumMerhchant(String nameAlgorithm, String rawData, String categorySecrectKey){
        try {
            BoBIModuleConfigNew boBIConfig = (BoBIModuleConfigNew)Service.configService.getModuleConfig();
            String secrectKey = "b";
            if(categorySecrectKey.equals(Constants.INTERNAL_MODULE)){
                secrectKey = boBIConfig.getSecretKey();
            }else
                secrectKey = boBIConfig.getFrontEndSecretKey();
            rawData += secrectKey;
            String myCheckSum =  CreateCheckSum(nameAlgorithm, rawData);
            return myCheckSum;
        } catch (Exception e) {
            return "";
        }
    }
}
