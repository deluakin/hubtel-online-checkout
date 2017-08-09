package com.smsgh.hubtelpayment;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.smsgh.hubtelpayment.Class.HTTPRequest;
import com.smsgh.hubtelpayment.Exception.MPowerPaymentException;
import com.smsgh.hubtelpayment.Class.PaymentItem;
import com.smsgh.hubtelpayment.Interfaces.HttpDoneListener;
import com.smsgh.hubtelpayment.Interfaces.OnPaymentResponse;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELU on 2/6/2017.
 */


public class MpowerPayments {
    private double amount = 0.00;
    private String description = "";
    private SessionConfiguration config;
    private List<PaymentItem> paymentItemList = new ArrayList<>();
    Context context;
    OnPaymentResponse paymentResponse;
    Dialog d;
    TextView txtloading;
    boolean done = false;
    boolean paymentpageloaded = false;

    /**
     *
     * @param configuration
     */
    public MpowerPayments(SessionConfiguration configuration){
        this.config = configuration;
    }

    /**
     *
     * @param configuration
     * @param paymentItemList
     */
    public MpowerPayments(SessionConfiguration configuration, List<PaymentItem> paymentItemList){
        this.config = configuration;
        this.paymentItemList = paymentItemList;
    }

    /**
     *
     * @param p
     */
    public MpowerPayments addPaymentItem(PaymentItem p){
        paymentItemList.add(p);
        return this;
    }

    public MpowerPayments setPaymentDetails(double amount, String description){
        this.amount = amount;
        this.description = description;
        return this;
    }

    /**
     *
     * @param context
     * @throws MPowerPaymentException
     */
    public void Pay(Context context) throws MPowerPaymentException{
        this.context = context;
        if(config.posturl.trim().length() == 0){
            throw new MPowerPaymentException("Make sure the build() method of the SessionConfiguration class is called");
        }

        if(config.geturl.trim().length() == 0){
            throw new MPowerPaymentException("Make sure the build() method of the SessionConfiguration class is called");
        }


        d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.mpower_init_payment);
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        d.setCanceledOnTouchOutside(false);
        txtloading = (TextView) d.findViewById(R.id.txtprocessing);
        d.show();

        HTTPRequest r = new HTTPRequest(context, new HttpDoneListener() {
            @Override
            public void onRequestCompleted(String message) throws MPowerPaymentException {
                ContinuePayment(message);
            }
        });
        HashMap<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("url", config.posturl);
        nameValuePairs.put("clientid", config.getClientId());
        nameValuePairs.put("secretkey", config.getSecreteKey());
        nameValuePairs.put("data", GeneratePostData());
        Object[] params = new Object[1];
        params[0] = nameValuePairs;
        try {
            r.execute(params);
        } catch (Exception e) {
            throw new MPowerPaymentException(e.getMessage());
        }
    }

    /**
     *
     * @param paymentResponse
     */
    public void setOnPaymentCallback(OnPaymentResponse paymentResponse){
        this.paymentResponse = paymentResponse;
    }

    private void SendResponseToEndPoint(String endpointurl) throws MPowerPaymentException{
        try {
            HTTPRequest r = new HTTPRequest(context, new HttpDoneListener() {
                @Override
                public void onRequestCompleted(String message) throws MPowerPaymentException {

                }
            });
            HashMap<String, String> nameValuePairs = new HashMap<String, String>();
            nameValuePairs.put("url", endpointurl);
            nameValuePairs.put("data", "");
            Object[] params = new Object[1];
            params[0] = nameValuePairs;

            r.execute(params);
        } catch (Exception e) {
            throw new MPowerPaymentException(e.getMessage());
        }
    }

    private void CompletePaymentStatusCheck(String message, String token) throws MPowerPaymentException{
        try {
            d.dismiss();
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.get("response_code") != null && jsonObject.get("response_code").toString().equalsIgnoreCase("00")){
                String status = jsonObject.get("status").toString();
                String endpointurl = config.getEndPointurl();
                if(endpointurl != null && endpointurl.trim().length() > 0){
                    String param = (endpointurl.indexOf("?") > 0) ? "" : "?";
                    param += "&status=" + status + "&token=" + token + "&tranxid=" + config.hash;
                    endpointurl = endpointurl + param;
                    SendResponseToEndPoint(endpointurl);
                }
                switch (status){
                    case "completed":
                        JSONObject custom_data = new JSONObject(jsonObject.get("custom_data").toString());
                        if(config.hash.equalsIgnoreCase(custom_data.get("tranx_id").toString())) {
                            paymentResponse.onSuccessful(token);
                        }
                        break;
                    case "cancelled ":
                        UserCancelledTransaction(token);
                        break;
                    case "pending":
                        String response_text = jsonObject.get("response_text").toString();
                        paymentResponse.onFailed(token, response_text);
                        break;
                    default:
                        break;
                }
            }else{

            }
        }catch (Exception ex){
            throw new MPowerPaymentException(ex.getMessage());
        }
    }

    private String GeneratePostData(){
        String data = "";
        try {
            JSONObject jparent = new JSONObject();
            JSONObject jobject = new JSONObject();
            if(this.paymentItemList.size() > 0){
                int x = 0;
                JSONObject p_obj2 = new JSONObject();
                for(PaymentItem p : this.paymentItemList){
                    JSONObject p_obj = new JSONObject();
                    p_obj.put("name", p.name);
                    p_obj.put("quantity", p.qty);
                    p_obj.put("unit_price", p.unit_p);
                    p_obj.put("total_price", p.total_p);
                    p_obj.put("description", p.description);
                    p_obj2.put("item_" + x, p_obj);
                    x++;
                }
                jobject.put("items", p_obj2);
            }

            jobject.put("total_amount", this.amount);
            jobject.put("description", this.description);
            jparent.put("invoice", jobject);

            jobject = new JSONObject();
            jobject.put("name", getAppName());
            jparent.put("store", jobject);

            jobject = new JSONObject();
            jobject.put("return_url", "http://txtconnect.co/v2/app/mpower_continue.php");
            jobject.put("cancel_url", "http://txtconnect.co/v2/app/mpower_cancel.php");
            jparent.put("actions", jobject);

            jobject = new JSONObject();
            jobject.put("tranx_id", config.getHash());
            jparent.put("custom_data", jobject);

            data = jparent.toString();

        }catch (Exception ex){

        }

        return data;
    }

    private void CompletePaymentTransaction(final String token) throws MPowerPaymentException {
        //get transaction status from the mpower api
        try {
            HTTPRequest r = new HTTPRequest(context, new HttpDoneListener() {
                @Override
                public void onRequestCompleted(String message) throws MPowerPaymentException {
                    CompletePaymentStatusCheck(message, token);
                }
            });
            HashMap<String, String> nameValuePairs = new HashMap<String, String>();
            nameValuePairs.put("url", config.geturl + token);
            nameValuePairs.put("clientid", config.getClientId());
            nameValuePairs.put("secretkey", config.getSecreteKey());
            nameValuePairs.put("data", "");
            Object[] params = new Object[1];
            params[0] = nameValuePairs;

            r.execute(params);
        } catch (Exception e) {
            throw new MPowerPaymentException(e.getMessage());
        }
    }

    private void UserCancelledTransaction(String token){
        this.paymentResponse.onCancelled(token);
    }

    private String getQueryStringPart(String key, String str_url) throws MPowerPaymentException {
        try {
            URI uri = new URI(str_url);
            String[] query_data = uri.getQuery().split("&");
            for (int x = 0; x < query_data.length; x++) {
                if (query_data[x].indexOf(key + "=") == 0) {
                    String[] data = query_data[x].split("=");
                    return data[1];
                }
            }
        }catch (Exception ex){
            throw new MPowerPaymentException(ex.getMessage());
        }
        return "";
    }

    private String getAppName(){
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appname = (stringId == 0) ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);

        return appname;
    }

    private void ContinuePayment(String message) throws MPowerPaymentException{
        try{
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.get("response_code") != null && jsonObject.get("response_code").toString().equalsIgnoreCase("00")){
                String url = jsonObject.get("response_text").toString();
                WebView webview = (WebView) d.findViewById(R.id.mpower_browser);
                webview.setWebChromeClient(new WebChromeClient());
                webview.getSettings().setJavaScriptEnabled(true);
                webview.getSettings().setDomStorageEnabled(true);
                txtloading.setText("Loading...");

                webview.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        String web_url = view.getUrl();
                        if(web_url.contains("mpower_continue.php?token")){
                            if(done){
                                return;
                            }
                            done = true;
                            view.stopLoading();
                            d.cancel();
                            d.dismiss();
                            txtloading.setText("Completing payment...");
                            try {
                                String token = getQueryStringPart("token", web_url);
                                CompletePaymentTransaction(token);
                            } catch (MPowerPaymentException e) {
                                e.printStackTrace();
                            }
                            return;
                        }else if(web_url.contains("mpower_cancel.php")){
                            if(done){
                                return;
                            }
                            done = true;
                            view.stopLoading();
                            d.cancel();
                            d.dismiss();
                            txtloading.setText("Cancelling payment...");
                            try {
                                String token = "";
                                if(web_url.contains("mpower_cancel.php?token")) {
                                    token = getQueryStringPart("token", web_url);
                                }
                                UserCancelledTransaction(token);
                            } catch (MPowerPaymentException e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        if(progress >= 30 && progress < 95) {
                            txtloading.setText("Almost ready...");
                        } else if(progress >= 95) {
                            d.findViewById(R.id.processing).setVisibility(View.GONE);
                            txtloading.setText("Processing...");
                        }else{
                            d.findViewById(R.id.processing).setVisibility(View.VISIBLE);
                        }
                    }
                });
                webview.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        d.dismiss();
                        try {
                            throw new MPowerPaymentException("Oh no! " + description);
                        } catch (MPowerPaymentException e) {
                            e.printStackTrace();
                        }
                    }
                });
                webview.loadUrl(url);
            }else{
                d.dismiss();
                throw new MPowerPaymentException(jsonObject.get("response_text").toString());
            }
        }catch (Exception ex){
            d.dismiss();
            throw new MPowerPaymentException(ex.getMessage());
        }
    }

}
