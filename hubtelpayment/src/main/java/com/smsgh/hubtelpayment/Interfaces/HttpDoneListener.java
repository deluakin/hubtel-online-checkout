package com.smsgh.hubtelpayment.Interfaces;

import com.smsgh.hubtelpayment.Exception.MPowerPaymentException;

/**
 * Created by DELU on 7/23/2016.
 */
public interface HttpDoneListener {
    void onRequestCompleted(String message) throws MPowerPaymentException;
}
