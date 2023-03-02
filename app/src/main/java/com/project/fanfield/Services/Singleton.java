package com.project.fanfield.Services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Singleton {
    public static Context sContext;
    public static Singleton singleton;
    RequestQueue requestQueue;

    private Singleton(Context sContext){
        this.sContext = sContext;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(sContext);
        return requestQueue;
    }

    public static synchronized Singleton getInstance(Context context){
        if(singleton==null){
            sContext = context;
            singleton = new Singleton(context);
        }
        return singleton;
    }

    public<T> void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }
}

