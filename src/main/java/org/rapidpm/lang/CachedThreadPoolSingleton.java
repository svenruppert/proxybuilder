package org.rapidpm.lang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Sven Ruppert
 */
public class CachedThreadPoolSingleton {

    public final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private static CachedThreadPoolSingleton ourInstance = new CachedThreadPoolSingleton();

    public static CachedThreadPoolSingleton getInstance() {
        return ourInstance;
    }

    private CachedThreadPoolSingleton() {
    }
}
