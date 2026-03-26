package fr.app.application.utils;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyUtils {
    private static VolleyUtils instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleyUtils(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyUtils getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyUtils(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    // This is the specific method your Activity is looking for
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}