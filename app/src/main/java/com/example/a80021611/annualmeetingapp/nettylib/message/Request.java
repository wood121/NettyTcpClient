package com.example.a80021611.annualmeetingapp.nettylib.message;

import java.io.IOException;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/14 9:34<br>
 * 版本： v2.0<br>
 */
public interface Request {
    Request setCallback(ResponseListener callback);

    ResponseListener getCallback();
    
    byte[] getSendMsgByte() throws IOException;
}
