package org.rapidpm.proxybuilder.dynamicobjectadapter;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sven on 12.05.15.
 */
public abstract class ExtendedInvocationHandler<T> implements InvocationHandler {

  private Map<MethodIdentifier, Method> adaptedMethods = new HashMap<>();
  private Map<MethodIdentifier, Object> adapters = new HashMap<>();
  private T original;

  public void setOriginal(T original) {
    this.original = original;
  }

  public void addAdapter(Object adapter) {
    final Class<?> adapterClass = adapter.getClass();
    Method[] methods = adapterClass.getDeclaredMethods();
    for (Method m : methods) {
      final MethodIdentifier key = new MethodIdentifier(m);
      adaptedMethods.put(key, m);
      adapters.put(key, adapter);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      final MethodIdentifier key = new MethodIdentifier(method);
      Method other = adaptedMethods.get(key);
      if (other != null) {
        other.setAccessible(true); //Lambdas...
        final Object result = other.invoke(adapters.get(key), args);
        other.setAccessible(false);
        return result;
      } else {
        return method.invoke(original, args);
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

}
