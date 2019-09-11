package com.mypos.smartsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;

import com.mypos.appmanagment.ISamModuleAidlInterface;
import com.mypos.appmanagment.ISystemAidlInterface;

import java.net.BindException;
import java.util.List;

public class SamModuleManagement {

    private static final String SERVICE_ACTION = "com.mypos.service.SYSTEM";
    private static final String SAM_MODULE = "sam_module";

    private ISystemAidlInterface systemService = null;
    private ISamModuleAidlInterface samCardManagementService = null;

    private boolean isBound = false;
    private static SamModuleManagement instance;

    private OnBindListener mListener = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            systemService = ISystemAidlInterface.Stub.asInterface(iBinder);

            IBinder binder = null;
            try {
                binder = systemService.getManager(SAM_MODULE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (binder != null)
                samCardManagementService = ISamModuleAidlInterface.Stub.asInterface(binder);

            isBound = true;

            if (mListener != null)
                mListener.onBindComplete();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            systemService = null;
            samCardManagementService = null;
            isBound = false;
        }
    };

    private SamModuleManagement(){

    }

    public static SamModuleManagement getInstance() {
        if (instance == null)
            instance = new SamModuleManagement();

        return instance;
    }

    public void bind(Context context, OnBindListener listener) throws Exception {
        if (!isSupported(context))
            throw new Exception("Functionality not supported (probably old version of myPOS OS)");

        if (isBound)
            return;

        mListener = listener;

        Intent intent = new Intent(SERVICE_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setPackage("com.mypos");

        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbind(Context context) {
        if (isBound) {
            context.unbindService(serviceConnection);
            systemService = null;
            samCardManagementService = null;
            isBound = false;
        }

        mListener = null;
    }

    public boolean detect(int slot, long timeOut) throws Exception {
        if (!isBound) {
            throw new BindException("call .bind(context) fist");
        }

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime < timeOut)) {
            try {
                if (samCardManagementService != null) {
                    boolean detect = samCardManagementService.detect(slot);
                    String e = samCardManagementService.getError();

                    if (e != null)
                        throw new Exception(e);

                    return detect;
                }
            }
            catch (IllegalStateException ignored) {
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public byte[] open(int slot, long timeOut) throws Exception {
        if (!isBound) {
            throw new BindException("call .bind(context) fist");
        }

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime < timeOut)) {
            try {
                if (samCardManagementService != null) {
                    byte[] open = samCardManagementService.open(slot);

                    String e = samCardManagementService.getError();

                    if (e != null)
                        throw new Exception(e);

                    return open;
                }
            }
            catch (IllegalStateException ignored) {
            }
            catch (DeadObjectException e) {
                e.printStackTrace();
                return null;
            }
            catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
            finally {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public  void close(int slot, long timeOut) throws Exception {
        if (!isBound) {
            throw new BindException("call .bind(context) fist");
        }

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime < timeOut)) {
            try {
                if (samCardManagementService != null) {
                    samCardManagementService.close(slot);

                    String e = samCardManagementService.getError();

                    if (e != null)
                        throw new Exception(e);
                }
            }
            catch (IllegalStateException ignored) {
            }
            catch (RemoteException e) {
                e.printStackTrace();

            }
            finally {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  byte[] isoCommand(int slot, byte[] apduSend, long timeOut) throws Exception {
        if (!isBound) {
            throw new BindException("call .bind(context) fist");
        }

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime < timeOut)) {
            try {
                if (samCardManagementService != null) {
                    byte[] isoCommand = samCardManagementService.isoCommand(slot, apduSend);

                    String e = samCardManagementService.getError();

                    if (e != null)
                        throw new Exception(e);

                    return isoCommand;
                }
            }
            catch (IllegalStateException ignored) {
            }
            catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
            finally {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public boolean isSupported(Context context) {
        Intent intent = new Intent(SERVICE_ACTION, null);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        List<ResolveInfo> services = context.getPackageManager().queryIntentServices(intent, 0);
        return !services.isEmpty();
    }

}
