package com.akindelu.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hubtel.payments.Class.Environment;
import com.hubtel.payments.Exception.HubtelPaymentException;
import com.hubtel.payments.Interfaces.OnPaymentResponse;
import com.hubtel.payments.HubtelCheckout;
import com.hubtel.payments.SessionConfiguration;

public class MainActivity extends AppCompatActivity {

    TextView paymentstatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paymentstatus = (TextView) findViewById(R.id.paymentstatus);
    }

    public void initPayment(View v){
        try {
            paymentstatus.setText("");
            SessionConfiguration sessionConfiguration = new SessionConfiguration()
                    .Builder().setClientId("igeirlub")
                    .setSecretKey("jjksrpzl")
                    .setEnvironment(Environment.LIVE_MODE)
                    .build();
            HubtelCheckout hubtelPayments = new HubtelCheckout(sessionConfiguration);
            hubtelPayments.setPaymentDetails(0.1, "This is a demo payment");
            hubtelPayments.Pay(this);
            hubtelPayments.setOnPaymentCallback(new OnPaymentResponse() {
                @Override
                public void onFailed(String token, String reason) {
                    paymentstatus.setText("payment failed: \nReason: " + reason);
                }

                @Override
                public void onCancelled(String token) {
                    paymentstatus.setText("payment was cancelled.");
                }

                @Override
                public void onSuccessful(String token) {
                    paymentstatus.setText("payment was successful. \nToken: " + token);
                }
            });
        }catch (HubtelPaymentException ex){
            Log.e("test", ex.getMessage());
        }
    }
}
