package com.mobeon.masp.execution_engine.mock;

import com.mobeon.masp.util.Ignore;
import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.DynamicMock;

import java.lang.reflect.*;
import java.util.ArrayList;

/**
 *
 */
public abstract class DelegatingMock extends Mock {
    private Object proxy;
    private ArrayList<Object> delegateList = new ArrayList<Object>();
    private static Method isSameMethod = findMethod(DelegatingProxy.class,"isSame",new Class<?>[]{});

    private static final class Result {
        private final Object result;

        public Result(Object result) {
            this.result = result;
        }
    }

    private void init() {
        Class<?> aClass = getMockedType();
        ClassLoader loader = aClass.getClassLoader();
        final Object mockProxy = super.proxy();

        InvocationHandler handler = new InvocationHandler() {

            public Object invoke(Object proxy, Method invokedMethod, Object[] args) throws Throwable {
                if(invokedMethod.equals(isSameMethod)) {
                    return isSame();
                }
                Result result = null;
                for (Object object : delegateList) {
                    Method implementedMethod = findDeclaredMethod(object, invokedMethod);
                    Method accessibleMethod = findAccessibleMethod(implementedMethod, object.getClass());
                    if (accessibleMethod != null) {
                        MockAction annotation = implementedMethod.getAnnotation(MockAction.class);
                        MockAction.Action actions;
                        if (annotation == null) {
                            actions = MockAction.Action.MOCK;
                        } else {
                            actions = annotation.value();
                        }
                        switch (actions) {
                            case DELEGATE:
                                result = doDefaultAction(object, accessibleMethod, invokedMethod, args);
                                break;
                            case MOCK:
                                result = doMockAction(invokedMethod, args);
                                break;
                        }
                        break;
                    }
                }
                if (result != null)
                    return result.result;
                else {
                    //Always call mock if no dummies found
                    result = doMockAction(invokedMethod, args);
                    if(result != null)
                        return result.result;
                    else {
                        //If all else fails, call all Objects methods on the mockProxy object
                        Method m = Object.class.getMethod(invokedMethod.getName(),invokedMethod.getParameterTypes());
                        return m.invoke(mockProxy,args);
                    }
                }
            }

            private Result doMockAction(Method invokedMethod, Object[] args) throws Throwable {
                Method method = findMethod(mockProxy, invokedMethod);
                return callProxiedMethod(mockProxy, method, args);
            }

            private Result doDefaultAction(Object self, Method method, Method invokedMethod, Object[] args) throws Throwable {
                Result result = callProxiedMethod(self, method, args);
                if (result == null) {
                    method = findMethod(mockProxy, invokedMethod);
                    result = callProxiedMethod(mockProxy, method, args);
                }
                return result;
            }

        };
        proxy = Proxy.newProxyInstance(loader, new Class<?>[]{aClass,DelegatingProxy.class}, handler);

    }

    public void addDelegate(Object delegate) {
        delegateList.add(0,delegate);
    }

    public boolean isSameObject(Object that) {
        return delegateList.contains(that) || proxy == that || super.proxy() == that;
    }

    public Constraint isSame() {
        return new Constraint() {
            public boolean eval(Object object) {
                return isSameObject(object);
            }

            public StringBuffer describeTo(StringBuffer stringBuffer) {
                return stringBuffer.append("isSameAs(").append(delegateList).append(")");
            }
        };
    }

    private Result callProxiedMethod(Object self, Method method, Object[] args) throws Throwable {
        if (method == null)
            return null;
        Object result;
        try {
            result = method.invoke(self, args);
            return new Result(result);
        } catch (IllegalAccessException iae) {
            System.err.println("Unexpected: " + iae);
            Ignore.illegaleAccessException(iae);
        } catch (InvocationTargetException ite) {
            throw fixupStacktrace(ite);
        }
        return null;
    }

    private Throwable fixupStacktrace(InvocationTargetException ite) {
        int stackOffset = 6;
        Throwable t = ite.getCause();
        t = t.fillInStackTrace();
        StackTraceElement[] frames = t.getStackTrace();
        StackTraceElement[] newFrames = new StackTraceElement[frames.length - stackOffset];
        System.arraycopy(frames, stackOffset, newFrames, 0, newFrames.length);
        t.setStackTrace(newFrames);
        return t;
    }


    private Method findAccessibleMethod(Method method, Class base) {
        if (method == null)
            return null;
        Class<?> superClass = base.getSuperclass();
        if (!isAccessible(base, method))
            do {
                Method superMethod = null;
                try {
                    superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    Ignore.noSuchMethodException(nsme);
                }
                if (superMethod == null) {
                    return null;
                } else if (isAccessible(superClass, superMethod)) {
                    return superMethod;
                } else {
                    method = superMethod;
                }
                superClass = superClass.getSuperclass();
            } while (true);
        else
            return method;
    }

    private boolean isAccessible(Class base, Method method) {
        int classModifiers = base.getModifiers();
        int methodModifiers = method.getModifiers();
        return (Modifier.isPublic(classModifiers)) && (Modifier.isPublic(methodModifiers));
    }


    private Method findDeclaredMethod(Object implementation, Method method) {
        try {
            return implementation.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException nsme) {
            Ignore.noSuchMethodException(nsme);
        }
        return null;
    }

    private static Method findMethod(Object implementation, Method method) {
        try {
            return implementation.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException nsme) {
            Ignore.noSuchMethodException(nsme);
        }
        return null;
    }

    private static Method findMethod(Class cls, String methodName,Class<?>[] types) {
        try {
            return cls.getMethod(methodName, types);
        } catch (NoSuchMethodException nsme) {
            Ignore.noSuchMethodException(nsme);
        }
        return null;
    }

    public DelegatingMock(Class aClass) {
        super(aClass);
        init();
    }

    public DelegatingMock(Class aClass, String string) {
        super(aClass, string);
        init();
    }

    public DelegatingMock(DynamicMock dynamicMock) {
        super(dynamicMock);
        init();
    }


    public Object proxy() {
        return proxy;
    }

}
