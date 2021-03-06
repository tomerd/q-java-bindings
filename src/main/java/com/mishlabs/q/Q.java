package com.mishlabs.q;

import java.util.Date;
import java.util.Map;

public class Q
{
    private native String native_version();

    private native long native_connect(String config);

    private native void native_disconnect(long q);

    private native String native_post(long q, String queue, String uid, String data, long run_at);

    private native boolean native_reschedule(long q, String uid, long run_at);

    private native boolean native_cancel(long q, String uid);

    private native void native_worker(long q, String queue, NativeWorker worker);

    private native void native_observer(long q, String queue, NativeObserver observer);

    private native void native_drop(long q);

    static
    {
        // IMPORTANT: make sure to set java.library.path to where qlib is installed (normally /usr/local/lib)
        // otherwise Java will not be able to load the q library.
        System.loadLibrary("q-1.0");
    }

    private long pq = 0;

    public Q()
    {
    }

    public String getVersion()
    {
        return this.native_version();
    }

    public boolean connect() throws Exception
    {
        return this.connect(null);
    }

    public boolean connect(String config) throws Exception
    {
        if (0 != pq) throw new IllegalStateException("Q already connected");
        pq = this.native_connect(config);
        return (0 != pq);
    }

    public void disconnect() throws Exception
    {
        if (0 == pq) return;
        this.native_disconnect(pq);
        pq = 0;
    }

    public String post(String queue, String data) throws Exception
    {
        return this.post(queue, null, data);
    }

    public String post(String queue, String uid, String data) throws Exception
    {
        return this.postAt(queue, uid, data, null);
    }

    public String postAt(String queue, String data, Date run_at) throws Exception
    {
        return this.postAt(queue, null, data, run_at);
    }

    public String postAt(String queue, String uid, String data, Date run_at) throws Exception
    {
        return this.postAt(queue, uid, data, null != run_at ? run_at.getTime() : 0);
    }

    public String postAt(String queue, String data, long run_at) throws Exception
    {
        return this.postAt(queue, null, data, run_at);
    }

    public String postAt(String queue, String uid, String data, long run_at) throws Exception
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        if (null == queue) throw new IllegalArgumentException();
        if (null == data) throw new IllegalArgumentException();
        return this.native_post(pq, queue, uid, data, run_at/1000);
    }

    public boolean reschedule(String uid, long run_at) throws Exception
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        if (null == uid) throw new IllegalArgumentException();
        return this.native_reschedule(pq, uid, run_at/1000);
    }

    public boolean cancel(String uid) throws Exception
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        if (null == uid) throw new IllegalArgumentException();
        return this.native_cancel(pq, uid);
    }

    public void worker(String queue, final Worker worker) throws Exception
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        if (null == queue) throw new IllegalArgumentException();
        if (null == worker) throw new IllegalArgumentException();
        this.native_worker(pq, queue, new NativeWorker()
        {
            public void perform(String data)
            {
                worker.perform(data);
            }
        });
    }

    public void observer(String queue, final Observer observer) throws Exception
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        if (null == queue) throw new IllegalArgumentException();
        if (null == observer) throw new IllegalArgumentException();
        this.native_observer(pq, queue, new NativeObserver()
        {
            public void perform(String data)
            {
                observer.perform(data);
            }
        });
    }

    // careful, dropes all queues!
    public void drop()
    {
        if (0 == pq) throw new IllegalStateException("Q disconnected");
        this.native_drop(pq);
    }

}
