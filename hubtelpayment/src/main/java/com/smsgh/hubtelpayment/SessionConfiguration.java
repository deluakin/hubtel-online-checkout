package com.smsgh.hubtelpayment;

import com.smsgh.hubtelpayment.Exception.MPowerPaymentException;
import com.smsgh.hubtelpayment.Class.Environment;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELU on 2/6/2017.
 */

public class SessionConfiguration {

    private String clientid;
    private String secretkey;
    private String endpointurl;
    private int mode = Environment.TEST_MODE;
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

    public SessionConfiguration Builder(){
        this.clientid = "";
        this.secretkey = "";
        this.mode = Environment.TEST_MODE;
        this.posturl = "";
        this.geturl = "";
        this.endpointurl = "";
        return this;
    }

    public SessionConfiguration setClientId(String clientId){
        this.clientid = clientId;
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

    public SessionConfiguration setEnvironment(int environment){
        this.mode = environment;
        return this;
    }

    public SessionConfiguration build() throws MPowerPaymentException {
        if (mode != Environment.TEST_MODE && mode != Environment.LIVE_MODE) {
            throw new MPowerPaymentException("Invalid payment environment.");
        }

        if (clientid.trim().length() == 0) {
            throw new MPowerPaymentException("Client Id is not set.");
        }

        if (secretkey.trim().length() == 0) {
            throw new MPowerPaymentException("Secret Key is not set.");
        }

        if (mode == Environment.TEST_MODE) {
            this.posturl = "https://app.mpowerpayments.com/sandbox-api/v1/checkout-invoice/create";
            this.geturl = "https://app.mpowerpayments.com/sandbox-api/v1/checkout-invoice/confirm/";
        }

        if (mode == Environment.LIVE_MODE) {
            this.posturl = "https://api.hubtel.com/v1/merchantaccount/onlinecheckout/invoice/create";
            this.geturl = "https://api.hubtel.com/v1/merchantaccount/onlinecheckout/invoice/status/";
        }

        if(this.endpointurl != null && this.endpointurl.trim().length() > 0) {
            Pattern p = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
            Matcher m = p.matcher(this.endpointurl);
            if (!m.matches()) {
                throw new MPowerPaymentException("invalid endpoint url.");
            }
        }

        return this;
    }
}
