package com.example.a80021611.annualmeetingapp.netty4android.connection;

import java.util.ArrayList;
import java.util.List;

/**
 * connect status
 */
public class ConnectionManager {
    public static ConnectionManager mInstance = new ConnectionManager();
    private List<ConnectionListener> mConnectionListeners = new ArrayList<ConnectionListener>();

    private ConnectionManager() {

    }

    public interface ConnectionListener {
        void onConnectionStatusChange(int status);
    }

    public static ConnectionManager getInstance() {
        return mInstance;
    }

    public void registerListener(ConnectionListener listener) {
        mConnectionListeners.add(listener);
    }

    public void unregisterListener(ConnectionListener listener) {
        mConnectionListeners.remove(listener);
    }

    public void dispatch(int status) {
        for (ConnectionListener listener : mConnectionListeners) {
            listener.onConnectionStatusChange(status);
        }
    }
}
