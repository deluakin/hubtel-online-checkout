package com.hubtel.payments.Interfaces;

/**
 * Created by DELU on 7/23/2016.
 */


public interface OnPaymentResponse {
    void onFailed(String token, String reason);
    void onCancelled(String token);
    void onSuccessful(String token);
}
