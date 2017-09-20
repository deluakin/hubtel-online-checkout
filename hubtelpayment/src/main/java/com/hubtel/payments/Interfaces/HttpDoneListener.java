package com.hubtel.payments.Interfaces;

import com.hubtel.payments.Exception.HubtelPaymentException;

/**
 * Created by DELU on 7/23/2016.
 */
public interface HttpDoneListener {
    void onRequestCompleted(String message) throws HubtelPaymentException;
}
