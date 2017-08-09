package com.akindelu.hubtelpayment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.smsgh.hubtelpayment.Class.Environment;
import com.smsgh.hubtelpayment.Exception.MPowerPaymentException;
import com.smsgh.hubtelpayment.Interfaces.OnPaymentResponse;
import com.smsgh.hubtelpayment.MpowerPayments;
import com.smsgh.hubtelpayment.SessionConfiguration;

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
                    .Builder().setClientId(getString(R.string.mpower_masterkey))
                    .setSecretKey(getString(R.string.mpower_privatekey))
                    .setEnvironment(Environment.LIVE_MODE)
                    .build();
            MpowerPayments mpowerPayments = new MpowerPayments(sessionConfiguration);
            mpowerPayments.setPaymentDetails(0.1, "This is a demo payment");
            mpowerPayments.Pay(this);
            mpowerPayments.setOnPaymentCallback(new OnPaymentResponse() {
                @Override
                public void onFailed(String token, String reason) {
                    paymentstatus.setText("payment failed: " + reason);
                }

                @Override
                public void onCancelled(String token) {
                    paymentstatus.setText("payment was cancelled. \nToken: " + token);
                }

                @Override
                public void onSuccessful(String token) {
                    paymentstatus.setText("payment was successful. \nToken: " + token);
                }
            });
        }catch (MPowerPaymentException ex){
            Log.e("test", ex.getMessage());
        }
    }
}
