package com.example.a80021611.annualmeetingapp.netty4android.message;

import java.io.IOException;

public interface ResponseListener {
    void onSuccess(Request resopnseRequest) throws IOException;

    void onFail(int errCode);
}
