package com.hubtel.payments;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.hubtel.payments.Class.HTTPRequest;
import com.hubtel.payments.Exception.HubtelPaymentException;
import com.hubtel.payments.Class.PaymentItem;
import com.hubtel.payments.Interfaces.HttpDoneListener;
import com.hubtel.payments.Interfaces.OnPaymentResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELU on 2/6/2017.
 */


public class HubtelCheckout {
    private double amount = 0.00;
    private String description = "";
    private SessionConfiguration config;
    private List<PaymentItem> paymentItemList = new ArrayList<>();
    private Context context;
    private OnPaymentResponse paymentResponse;
    private Dialog d;
    private TextView txtloading;
    boolean done = false;
    boolean paymentpageloaded = false;

    /**
     *
     * @param configuration
     */
    public HubtelCheckout(SessionConfiguration configuration){
        this.config = configuration;
    }

    /**
     *
     * @param configuration
     * @param paymentItemList
     */
    public HubtelCheckout(SessionConfiguration configuration, List<PaymentItem> paymentItemList){
        this.config = configuration;
        this.paymentItemList = paymentItemList;
    }

    /**
     *
     * @param p
     */
    public HubtelCheckout addPaymentItem(PaymentItem p){
        paymentItemList.add(p);
        return this;
    }

    public HubtelCheckout setPaymentDetails(double amount, String description){
        this.amount = amount;
        this.description = description;
        return this;
    }

    /**
     *
     * @param context
     * @throws HubtelPaymentException
     */
    public void Pay(Context context) throws HubtelPaymentException {
        this.context = context;

        if(config.posturl.trim().length() == 0){
            throw new HubtelPaymentException("Make sure the build() method of the SessionConfiguration class is called");
        }

        if(config.geturl.trim().length() == 0){
            throw new HubtelPaymentException("Make sure the build() method of the SessionConfiguration class is called");
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
            public void onRequestCompleted(String message) throws HubtelPaymentException {
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
            throw new HubtelPaymentException(e.getMessage());
        }
    }

    /**
     *
     * @param paymentResponse
     */
    public void setOnPaymentCallback(OnPaymentResponse paymentResponse){
        this.paymentResponse = paymentResponse;
    }

    private void SendResponseToEndPoint(String endpointurl) throws HubtelPaymentException {
        try {
            HTTPRequest r = new HTTPRequest(context, new HttpDoneListener() {
                @Override
                public void onRequestCompleted(String message) throws HubtelPaymentException {

                }
            });
            HashMap<String, String> nameValuePairs = new HashMap<String, String>();
            nameValuePairs.put("url", endpointurl);
            nameValuePairs.put("data", "");
            Object[] params = new Object[1];
            params[0] = nameValuePairs;

            r.execute(params);
        } catch (Exception e) {
            throw new HubtelPaymentException(e.getMessage());
        }
    }

    private void CompletePaymentStatusCheck(String message, String token) throws HubtelPaymentException {
        try {
            if(d != null){
                d.dismiss();
            }
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.get("ResponseCode") != null && jsonObject.get("ResponseCode").toString().equalsIgnoreCase("0000")){
                JSONArray dataArr = (JSONArray) jsonObject.get("Data");
                JSONObject data = (JSONObject) dataArr.get(0);
                JSONArray transCycle = (JSONArray) data.get("TransactionCycle");
                JSONObject mostRecent = (JSONObject) transCycle.get(0);
                String status = mostRecent.getString("Status");
                String endpointurl = config.getEndPointurl();
                String clientRef = data.get("ClientReference").toString();
                if(endpointurl != null && endpointurl.trim().length() > 0){
                    String param = (endpointurl.indexOf("?") > 0) ? "" : "?";
                    param += "&status=" + status + "&token=" + token + "&tranxid=" + clientRef;
                    endpointurl = endpointurl + param;
                    SendResponseToEndPoint(endpointurl);
                }
                switch (status){
                    case "Success":
                        paymentResponse.onSuccessful(token);
                        break;
                    default:
                        String response_text = jsonObject.get("response_text").toString();
                        paymentResponse.onFailed(token, response_text);
                        break;
                }
            }else{
                paymentResponse.onFailed(token, "");
            }
        }catch (Exception ex){
            throw new HubtelPaymentException(ex.getMessage());
        }
    }

    private String GeneratePostData(){
        String data = "";
        try {
            JSONObject jparent = new JSONObject();
            JSONObject cart = new JSONObject();
            JSONArray items = new JSONArray();
            if(this.paymentItemList.size() > 0){
                for(PaymentItem p : this.paymentItemList){
                    JSONObject item = new JSONObject();
                    item.put("name", p.name);
                    item.put("quantity", p.qty);
                    item.put("unitPrice", p.unit_p);

                    items.put(item);
                }
                cart.put("items", items);
            }else{
                JSONObject item = new JSONObject();
                item.put("name", description);
                item.put("quantity", 1);
                item.put("unitPrice", amount);

                items.put(item);
                cart.put("items", items);
            }

            cart.put("totalAmount", this.amount);
            cart.put("description", this.description);

            cart.put("callbackUrl", "https://apps.mobivs.com/invitasio/index.php/hubtel/" + getAppName());
            cart.put("returnUrl", "https://apps.mobivs.com/mpower_continue");
            cart.put("cancellationUrl", "https://apps.mobivs.com/mpower_cancel");

            cart.put("merchantBusinessLogoUrl", config.getBusinessLogoUrl());
            cart.put("clientReference", config.getRef());
            cart.put("merchantAccountNumber", config.getMerchantAccountNumber());

            data = cart.toString();

        }catch (Exception ex){

        }

        return data;
    }

    private void CompletePaymentTransaction(final String token) throws HubtelPaymentException {
        //get transaction status from the mpower api
        try {
            HTTPRequest r = new HTTPRequest(context, new HttpDoneListener() {
                @Override
                public void onRequestCompleted(String message) throws HubtelPaymentException {
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
            throw new HubtelPaymentException(e.getMessage());
        }
    }

    private void UserCancelledTransaction(){
        if(d != null){
            d.dismiss();
        }
        this.paymentResponse.onCancelled();
    }

    private String getQueryStringPart(String key, String str_url) throws HubtelPaymentException {
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
            throw new HubtelPaymentException(ex.getMessage());
        }
        return "";
    }

    private String getAppName(){
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appname = (stringId == 0) ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);

        return appname;
    }

    private void ContinuePayment(String message) throws HubtelPaymentException {
        try{
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.get("status") != null && jsonObject.get("status").toString().equalsIgnoreCase("Success")){
                JSONObject data = new JSONObject(jsonObject.get("data").toString());
                String url = data.getString("checkoutUrl");
                final WebView webview = (WebView) d.findViewById(R.id.mpower_browser);
                webview.setWebChromeClient(new WebChromeClient());
                webview.getSettings().setJavaScriptEnabled(true);
                webview.getSettings().setDomStorageEnabled(true);

                webview.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        String web_url = view.getUrl();
                        if(web_url.contains("mpower_continue") && web_url.contains("checkoutid")){
                            if(done){
                                return;
                            }
                            done = true;
                            view.stopLoading();
                            webview.setVisibility(View.GONE);
                            txtloading.setText("Completing payment...");
                            try {
                                String token = getQueryStringPart("checkoutid", web_url);
                                CompletePaymentTransaction(token);
                            } catch (HubtelPaymentException e) {
                                e.printStackTrace();
                            }
                            return;
                        }else if(web_url.contains("mpower_cancel")){
                            if(done){
                                return;
                            }
                            done = true;
                            view.stopLoading();
                            webview.setVisibility(View.GONE);
                            txtloading.setText("Cancelling payment...");
                            try {
                                String token = getQueryStringPart("checkoutid", web_url);
                                UserCancelledTransaction();
                            } catch (HubtelPaymentException e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        if(progress >= 95) {
                            webview.setVisibility(View.VISIBLE);
                            d.findViewById(R.id.processing).setVisibility(View.GONE);
                            txtloading.setText("Processing...");
                        }else{
                            webview.setVisibility(View.GONE);
                            d.findViewById(R.id.processing).setVisibility(View.VISIBLE);
                        }
                    }
                });
                webview.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        d.dismiss();
                        try {
                            throw new HubtelPaymentException("Oh no! " + description);
                        } catch (HubtelPaymentException e) {
                            e.printStackTrace();
                        }
                    }
                });
                webview.loadUrl(url);
            }else{
                d.dismiss();
                throw new HubtelPaymentException("Payment gateway could not be initialized");
            }
        }catch (Exception ex){
            d.dismiss();
            throw new HubtelPaymentException(ex.getMessage());
        }
    }

}
