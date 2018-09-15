package com.hubtel.payments;

import com.hubtel.payments.Exception.HubtelPaymentException;

import java.security.MessageDigest;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELU on 2/6/2017.
 */

public class SessionConfiguration {

    private String clientid;
    private String accountnumber;
    private String secretkey;
    private String imageurl;
    private String endpointurl;
    protected String posturl = "";
    protected String geturl = "";
    protected String hash = "";

    protected String getHash() {
        String salt = clientid;
        hash = clientid + "|" + secretkey + "|" + System.nanoTime();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes("UTF-8"));
            byte[] bytes = md.digest(hash.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    protected String getRef() {
        String ref = "";

        Random rand = new Random();
        int min = 0;
        int max = 9;
        for(int x = 0; x < 7; x++){
            int randomV =rand.nextInt((max - min) + 1) + min;
            ref += "" + randomV;
        }

        char[] alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int min2 = 0;
        int max2 = 25;
        for(int x = 0; x < 3; x++){
            int randomV =rand.nextInt((max2 - min2) + 1) + min2;
            char c = alphas[randomV];

            ref += "" + c;
        }

        return ref;
    }

    public SessionConfiguration(){
    }

    protected String getEndPointurl(){
        return endpointurl;
    }

    protected String getSecreteKey(){
        return secretkey;
    }

    protected String getClientId(){
        return clientid;
    }

    protected SessionConfiguration setBusinessLogoUrl(String imageUrl){
        imageurl = imageUrl;
        return this;
    }

    protected String getBusinessLogoUrl(){
        if(imageurl == null || imageurl.trim().isEmpty())
            return "https://apps.mobivs.com/client/assets/images/no-biz-image.jpg";

        return imageurl;
    }

    protected String getMerchantAccountNumber(){
        return accountnumber;
    }

    public SessionConfiguration Builder(){
        this.clientid = "";
        this.secretkey = "";
        this.accountnumber = "";
        this.posturl = "";
        this.geturl = "";
        this.imageurl = "";
        this.endpointurl = "";
        return this;
    }

    public SessionConfiguration setClientId(String clientId){
        this.clientid = clientId;
        return this;
    }

    public SessionConfiguration setMerchantAccountNumber(String accountnumber){
        this.accountnumber = accountnumber;
        return this;
    }

    public SessionConfiguration setSecretKey(String secretKey){
        this.secretkey = secretKey;
        return this;
    }

    public SessionConfiguration setEndPointURL(String url){
        this.endpointurl = url;
        return this;
    }

    public SessionConfiguration build() throws HubtelPaymentException {
        if (clientid.trim().length() == 0) {
            throw new HubtelPaymentException("Client Id is not set.");
        }

        if (secretkey.trim().length() == 0) {
            throw new HubtelPaymentException("Secret Key is not set.");
        }

        if (accountnumber.trim().length() == 0) {
            throw new HubtelPaymentException("Merchant AccountNumber is not set.");
        }

        this.posturl = "https://api.hubtel.com/v2/pos/onlinecheckout/items/initiate";
        this.geturl = "https://api.hubtel.com/v1/merchantaccount/merchants/" + accountnumber + "/transactions/status?checkoutId=";

        if(this.endpointurl != null && this.endpointurl.trim().length() > 0) {
            Pattern p = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
            Matcher m = p.matcher(this.endpointurl);
            if (!m.matches()) {
                throw new HubtelPaymentException("invalid endpoint url.");
            }
        }

        return this;
    }
}
