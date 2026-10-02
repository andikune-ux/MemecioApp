package com.memecio.app.plugin;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

/**
 * Helper untuk panggil method `suspend` Kotlin dari Java via reflection.
 * C-2b-2.
 */
public class CoroutineHelper {

    private static final String TAG = "CoroutineHelper";
    private static final long DEFAULT_TIMEOUT_MS = 30000;

    public static class Result {
        public boolean success;
        public Object data;
        public Throwable error;
    }

    /** Panggil method suspend Kotlin. args = argumen sebelum Continuation. */
    public static Result callSuspend(Object target, String methodName, Object... args) {
        Result result = new Result();

        try {
            Method found = null;
            for (Method m : target.getClass().getMethods()) {
                if (!m.getName().equals(methodName)) continue;
                Class<?>[] ptypes = m.getParameterTypes();
                if (ptypes.length != args.length + 1) continue;
                if (!ptypes[ptypes.length - 1].getName().equals("kotlin.coroutines.Continuation")) continue;

                boolean match = true;
                for (int i = 0; i < args.length; i++) {
                    Class<?> p = ptypes[i];
                    Object a = args[i];
                    if (a == null) {
                        if (p.isPrimitive()) { match = false; break; }
                        continue;
                    }
                    if (p.isAssignableFrom(a.getClass())) continue;
                    if (p == int.class && a instanceof Integer) continue;
                    if (p == long.class && a instanceof Long) continue;
                    if (p == boolean.class && a instanceof Boolean) continue;
                    match = false; break;
                }
                if (match) { found = m; break; }
            }

            if (found == null) {
                result.error = new NoSuchMethodException(
                    "Method " + methodName + "(" + args.length + " arg + Continuation) tidak ditemukan");
                return result;
            }

            Log.d(TAG, "Invoke: " + found.getDeclaringClass().getSimpleName() + "." + found.getName());

            final Object[] holder = new Object[1];
            final Throwable[] errHolder = new Throwable[1];
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicBoolean done = new AtomicBoolean(false);

            Continuation<Object> cont = new Continuation<Object>() {
                @Override public CoroutineContext getContext() { return EmptyCoroutineContext.INSTANCE; }

                @Override public void resumeWith(Object o) {
                    if (!done.compareAndSet(false, true)) return;
                    try {
                        String cn = (o == null) ? "null" : o.getClass().getName();
                        Log.d(TAG, "Continuation resumed, class=" + cn);
                        if (o == null) {
                            holder[0] = null;
                        } else if (cn.equals("kotlin.Result$Failure")) {
                            try {
                                Field f = o.getClass().getDeclaredField("exception");
                                f.setAccessible(true);
                                errHolder[0] = (Throwable) f.get(o);
                            } catch (Throwable t) {
                                errHolder[0] = new RuntimeException("Kotlin Result.Failure (unknown)");
                            }
                        } else if (cn.startsWith("kotlin.Result$Success")) {
                            try {
                                Field f = o.getClass().getDeclaredField("value");
                                f.setAccessible(true);
                                holder[0] = f.get(o);
                            } catch (Throwable t) {
                                holder[0] = o;
                            }
                        } else {
                            holder[0] = o;
                        }
                    } catch (Throwable t) {
                        errHolder[0] = t;
                    } finally {
                        latch.countDown();
                    }
                }
            };

            Object[] invokeArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, invokeArgs, 0, args.length);
            invokeArgs[args.length] = cont;

            Object ret = found.invoke(target, invokeArgs);

            if (!isSuspended(ret) && done.compareAndSet(false, true)) {
                holder[0] = ret;
                latch.countDown();
            }

            if (!latch.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                result.error = new RuntimeException("Timeout menunggu coroutine ("
                    + (DEFAULT_TIMEOUT_MS / 1000) + "s)");
                return result;
            }

            if (errHolder[0] != null) { result.error = errHolder[0]; return result; }
            result.success = true;
            result.data = holder[0];
            return result;

        } catch (Throwable t) {
            result.error = t;
            return result;
        }
    }

    private static boolean isSuspended(Object ret) {
        if (ret == null) return false;
        try {
            Class<?> c = Class.forName("kotlin.coroutines.intrinsics.IntrinsicsKt");
            Method m = c.getMethod("getCOROUTINE_SUSPENDED");
            Object suspended = m.invoke(null);
            return ret == suspended;
        } catch (Throwable t) {
            return false;
        }
    }
}
