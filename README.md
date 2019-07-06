# Welcome to ProxyBuilder
# Welcome to ProxyBuilder

Here we will describe the ProxyBuilder 
from [github.com/ProxyBuilder/proxybuilder](https://github.com/ProxyBuilder/proxybuilder)

## What are Proxies ?
If you want to have an overview of the Proxy-Pattern itself I can recommend
the following sources.

[JavaOne 2015 Tutorial - Proxy DeepDive](https://de.slideshare.net/svenruppert/proxy-deepdive-javaone20151027001)

## What goal we want to reach?
This project was born, because I had to work a lot with old huge projects. The only thing I could rely on, is the pure JDK.
So I started playing with different Design Patterns and figured out, that Proxies are one of the most powerful pattern group for me.
During the time I was writing the german Book ***"Dynamic Proxies"*** with [Dr. Heinz Kabutz](http://www.javaspecialists.eu/) I started to write examples. Step by step the examples are more generic and the ***ProxyBuilder*** - project was born. 

## Some Examples
To have an idea what you could do with the ***ProxyBuilder*** I will show here some examples. The more detailed 
information you can find in the special sections of this website. This project is based on ***Java8***.

### Some Examples with DynamicProxies
Here are some examples based on the ***DynamicProxy***. But we have created ***generated static*** versions too...
#### Virtual Proxy
```java
final DemoLogic original = new DemoLogic();
final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .build();
```

#### Security Proxy
```java
final DemoLogic original = new DemoLogic();
final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
```


```java
final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> false)
        .build();
```

#### Metrics Proxy
```java
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addMetrics()
        .build();

```

## How to bootstrap your project?
If you want to be as near as possible at the actual development version, you could use this. If you need a more stable version
change the version numbers to the last stable one.

If you want to start with the ***DynamicProxyBuilder*** creating for example ***VirtualProxies*** bundled with some other things
you need the following dependency in your pom.xml:

```xml
    <dependency>
      <groupId>org.rapidpm.proxybuilder</groupId>
      <artifactId>rapidpm-proxybuilder-modules-dynamic</artifactId>
      <version>${rapidpm.version}</version>
    </dependency>
```

The ***ProxyBuilder*** includes the ***Kotlin*** runtime libs and ***Metrics*** from Dropwizard. 

### VirtualProxy with one PreAction
This will be a ***VirtualProxy*** with a ***PreAction***. You can add as many ***PreActions*** as you need. 
Every ***PreAction*** will be executed before every method invocation in the order the ***PreAction*** was added.

```java
public class ProxyDemoV001 {

  public static void main(String[] args) {
  
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, new ServiceImpl())
        .addIPreAction((original, method, args1) 
                        -> System.out.println(" PreAction = " + System.nanoTime()))
        .build();
  
    System.out.println("proxy created " + System.nanoTime());
    System.out.println("s = " + service.doWork("Go.."));
  }

  public interface Service {
    String doWork(String str);
  }

  public static class ServiceImpl implements Service {
    public ServiceImpl() {
      System.out.println(" ServiceImpl => constructor... " + System.nanoTime());
    }

    @Override
    public String doWork(final String str) {
      return str + " orig..";
    }
  }

}

```

### VirtualProxy with Metrics

```java
public class ProxyDemoV002 {

  public static void main(String[] args) {
    RapidPMMetricsRegistry.getInstance().startConsoleReporter();
    
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, new ServiceImpl())
        .addMetrics()
        .build();
        
    System.out.println("proxy created " + System.nanoTime());
    final long count = IntStream.range(0, 10_000_000)
        .boxed()
        .map(i -> service.doWork("Go.." + i))
        .count();
    System.out.println("s = " + service.doWork("Go.."));
    System.out.println("count = " + count);
  }


  public interface Service {
    String doWork(String str);
  }

  public static class ServiceImpl implements Service {
    public ServiceImpl() {
      System.out.println(" ServiceImpl => constructor... " + System.nanoTime());
    }
    @Override
    public String doWork(final String str) {
      return str + " orig..";
    }
  }
}
```



To bootstrap your project with the latest SNAPSHOT you need to add the SNAPSHOT-repository that is available at maven - central.  
Here our ***TeamCity*** will push regularly the binaries. 
If you are using maven you could add the following to your ***settings.xml*** to get the snapshots that are available at maven-central. 

```xml
   <profile>
      <id>allow-snapshots</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>snapshots-repo</id>
          <url>https://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
    </profile>
``` 

# Static Proxies
Compared to the DynamicProxies we are now generating static proxies. This could be done with 
***AnnotationProcessing*** during the compile process or at runtime.

## Generated
Using ***AnnotationProcessing*** to generate the Proxies will give you the possibility to get 
proxies without the overhead of Reflection. On the other side you will generate maybe a lot of code, that must be compiled.

## Runtime Generated
Compared to the way of using ***AnnotationProcessing*** this one will create and compile at runtime the static proxies. This
is possible if you have access to the Compiler (tools.jar) during runtime and will mostly lead you to use Unsafe to put your new classes to the SystemClassLoader.

## Static Generated VirtualProxy
With the Annotation ***@StaticVirtualProxy*** you can generate Static - VirtualProxies during the clean compile process. This Annotation have a parameter called ***strategy***. The Strategy is used to define the right time to call the ***InstanceFactory***
See [DynamicProxies - CreationStrategies](/dynamicproxy/#creationstrategies)


## Static Generated MetricsProxy
With the static ***MetricsProxy*** we will get generated MetricsProxies that are using Dropwizard-Metrics to measure the 
usage of the methods. The generated static MetricsProxies are generating Methods for all declared Methods in the inheritance including ***hashCode()*** and ***equals()***. You can use the Annotation  ***@StaticMetricsProxy*** in combination with interfaces and classes.

There is one difference between the generated classes based on interfaces and classes. If you annotate an interface you will only get Metrics for the declared Methods, not for ***hashCode*** and ***equals*** if it is not explicitly declared. But for annotated classes you will get the Metrics for methods from Object, too.

```java
@StaticMetricsProxy
public interface Service {
  String doWork(String txt);

  String doMoreWorkA(String txt);

  String doMoreWorkB(String txt);

  String doMoreWorkC(String txt);

  String doMoreWorkD(String txt);
}
```

You can use the MetricsProxy easily by creating an instance and setting the Delegator. After this
every Methodcall will be counted by DropwizardMetrics. For every Method, you will get a separate Histogram, named with the full Classname and Methodname. In my case you will get a Histogram with the name
***org.rapidpm.demo.proxybuilder.staticproxy.v002.Service.doMoreWorkC***


```java
    final ServiceStaticMetricsProxy proxy = new ServiceStaticMetricsProxy();
    proxy.withDelegator(new ServiceImpl());

    final Service service = proxy;

    RapidPMMetricsRegistry.getInstance().startConsoleReporter();
    try (final IntStream intStream = IntStream.range(0, 10_000_000)) {
      intStream
          .onClose(() -> out.println("Stream will be closed now..."))

          .forEach(i -> service.doMoreWorkC("aaahhhhhh " + i));
    }

    RapidPMMetricsRegistry
        .getInstance()
        .getMetrics()
        .getHistograms()
        .forEach((s, histogram) -> {
          out.println("s = " + s);
          out.println("histogram - get999thPercentile= " + histogram.getSnapshot().get999thPercentile());
        });
```

## Static Generated LoggingProxy
Quite often I could find source code like the following.
```java
public void doWork(String str){
    logger.debug("doWork -> " + str);
    //some work....
}
```

The target is a logging of the methodcalls and the values. OK, we don´t want to discuss why or why not. But if you have to do it, you could
now use the ***LoggingProxy***. The Logging is implemented with ***slf4j***. Every method call will be logged with 
the methodname, the name of the params and the param values itself. With this information you could find the corresponding source code very easy.

Based on the following definition of a method...
```java
    public <T extends List> T unwrapList(final T type, final String str);
``` 
you will get an implementation like the follwoing.

```java
  public <T extends List> T unwrapList(final T type, final String str) {
    if (logger.isInfoEnabled()) {
      logger.info("delegator.unwrapList(type, str) values - " + type + " - " + str);
    }
    T result = delegator.unwrapList(type, str);
    return result;
  }
```

This implementation will asume, that the values are using a proper ***toString()*** implementation itself.

If you want to generate a StaticLoggingProxy, please add the Annotation ***@StaticLoggingProxy*** to the target class or interface.

Here you will get the full example.
```java
@StaticLoggingProxy
public interface MyLoggingInterface {
  <T extends List> T unwrapList(T type, String str);
}

//generated code
@Generated(
    value = "StaticLoggingProxyAnnotationProcessor",
    date = "2016-05-09T14:40:56.22",
    comments = "www.proxybuilder.org"
)
@IsGeneratedProxy
@IsLoggingProxy
public class MyLoggingInterfaceStaticLoggingProxy implements MyLoggingInterface {
  private static final Logger logger = getLogger(MyLoggingInterface.class);

  private MyLoggingInterface delegator;

  public MyLoggingInterfaceStaticLoggingProxy withDelegator(final MyLoggingInterface delegator) {
    this.delegator = delegator;
    return this;
  }

  public <T extends List> T unwrapList(final T type, final String str) {
    if(logger.isInfoEnabled()) {
      logger.info("delegator.unwrapList(type, str) values - " + type + " - " + str);
    }
    T result = delegator.unwrapList(type, str);
    return result;
  }
}

// demo code usage
public class MainV008 {
  public static void main(String[] args) {
    final MyLoggingInterface demo
        = new MyLoggingInterfaceStaticLoggingProxy()
        .withDelegator(new LoggerExample());
    final List<Integer> list = demo.unwrapList(asList(1,2,3,4), "AEAEA");
  }

  public static class LoggerExample implements MyLoggingInterface {
    @Override
    public <T extends List> T unwrapList(final T type, final String str) {
      return null;
    }
  }
}
```

The logging output will be ```delegator.unwrapList(type, str) values - [1, 2, 3, 4] - AEAEA```


## Static Runtime VirtualProxy
partly implemented until now.. stay tuned

## Static Runtime MetricsProxy
partly implemented until now.. stay tuned

#DynamicProxy

Since jdk1.3 the DynamicProxy is part of the JDK. 
The official documentation/API-Doc for JDK8 you can find 
[here](http://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Proxy.html)

To create Proxies based on the DynamicProxy you have to add the following dependency to your project.
'''xml
 <dependency>
      <groupId>org.rapidpm.proxybuilder</groupId>
      <artifactId>rapidpm-proxybuilder-modules-dynamic</artifactId>
      <version>${rapidpm.version}</version>
    </dependency>
'''

If you are working with this kind of Proxies, you need always an interface. If you don´t have an interface in your code and you could not create one, you have to use the ***StaticVirtualProxies***.

##VirtualProxy

#### pure VirtualProxy
```java
final DemoLogic original = new DemoLogic();
final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .build();
```
With this you can generate at runtime a ***VirtualProxy*** based on the ***DynamicProxy***. This you can use beginning from Java 1.3 if you need it. (You have to backport the code by yourself, but we would like to add this as legacy module) The Sourcelevel we are using in this project is Java8.

#### Security Proxy
```java
final DemoLogic original = new DemoLogic();
final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
```
This is a SecureVirtalProxy. The SecurityRules are invoked before the VirtualProxy is activated. This means, the real Subject will be created after the first time all SecurityRules are OK. 

```java
final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> false)
        .build();
```

#### Metrics Proxy

If you need the possibility to get Metrics out of your application, you could create a MetricsProxy. In this example we are creating a pure ***MetricsProxy*** you could do the following:

```java
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, new InnerDemoClass())
        .addMetrics()
        .build();

```
 You could combine this with a ***VirtualProxy*** to get a ***VirtualMetricsProxy***:

```java
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, ServiceImpl.class, CreationStrategy.SOME_DUPLICATES)
        .addMetrics()
        .build();

```

## PreAction -- PostAction
Sometimes you want to have the possibility to do something before or after a Methodinvocation. For this we have the 
***PreAction*** and ***PostAction***. The Actions are executed in the order they are added to the Proxy.

```java
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, new ServiceImpl())
        .addIPreAction((original, method, args1) -> out.println("001 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("002 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("003 = " + method.getName()))
        .build();
```

A ***VirtualProxy*** with ***PreActions*** will lead to the execution of all ***PreActions*** before the real Subject will be created. 

```java
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, ServiceImpl.class, CreationStrategy.SOME_DUPLICATES)
        .addIPreAction((original, method, args1) -> out.println("001 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("002 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("003 = " + method.getName()))
        .build();
```

If you combine it with a ***SecurityVirtualProxy*** with ***PreActions*** the Security-Rule will be invoked first.

```java
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, ServiceImpl.class, CreationStrategy.SOME_DUPLICATES)
        .addIPreAction((original, method, args1) -> out.println("001 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("002 = " + method.getName()))
        .addIPreAction((original, method, args1) -> out.println("003 = " + method.getName()))
        .addSecurityRule(() -> {
          out.println("sec 001");
          return true;
        })
        .build();
```

## CreationStrategies
With the ***CreationStrategies*** you can choose what will be the right way of synchronization for you if you are using ***VirtualProxies***. You can implement some other by yourself, if needed. With ***CreationStrategies*** you can do different things. Not only the creation of one Element is possible, you can e.g. create a pool of instances used randomly or you can do something like ***MethodScoped***. 

### MethodScoped
This ***CreationStrategy*** will create for every method invocation a new instance.

```java
public class ServiceStrategyFactoryMethodScoped<T> implements ServiceStrategyFactory<T> {

  @Override
  public synchronized T realSubject(ServiceFactory<T> factory) {
    return factory.createInstance();
  }
}
```


### NONE
If you choose nothing or ***CreationStrategy.NONE*** you will get the ***NotThreadSafe*** version.

```java
public class ServiceStrategyFactoryNotThreadSafe<T> implements ServiceStrategyFactory<T> {

  private T service;

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }

}
```

### SomeDuplicates

```java
public class ServiceStrategyFactorySomeDuplicates<T> implements ServiceStrategyFactory<T> {
  private final AtomicReference<T> ref = new AtomicReference<>();

  @Override
  public T realSubject(ServiceFactory<T> factory) {

    T service = ref.get();
    if (service == null) {
      service = factory.createInstance();
      if (!ref.compareAndSet(null, service)) {
        service = ref.get();
      }
    }
    return service;
  }
}
```

### Synchronized

```java
public class ServiceStrategyFactorySynchronized<T> implements ServiceStrategyFactory<T> {

  private T service;

  @Override
  public synchronized T realSubject(ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }
}
```

### NoDuplicates

```java
public class ServiceStrategyFactoryNoDuplicates<T> implements ServiceStrategyFactory<T> {

  private final Lock initializationLock = new ReentrantLock();
  private volatile T realSubject;

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    T result = realSubject;
    if (result == null) {
      initializationLock.lock();
      try {
        result = realSubject;
        if (result == null) {
          result = realSubject = factory.createInstance();
        }
      } finally {
        initializationLock.unlock();
      }
    }
    return result;
  }
}
```

# Object Adapter
In this section we want to describe the ObjectAdapter Pattern that is realized in this module.
We can use a dynamic and a static version of the ***ObjectAdapter*** Pattern. The dynamic version is based on the DynamicProxy from jdk1.3 and the static version is purely generated via Annotation Processing. 

## DynamicObjectAdapter
If you want to use the ***DynamicObjectAdapter*** you need at leaset one Interface you can cast to. To create the ***DynamicObjectAdapter*** you can use the Annotation ***@DynamicObjectAdapterBuilder***. This will create via Annotation Processing the ***DynamicObjectAdapter*** and the corresponding ***Builder*** that you can use for convenience and type safety. Let´s assume you have the following interface ***Service***:


```java
@DynamicObjectAdapterBuilder
public interface Service {
  String doWork(String txt);
  String doMoreWorkA(String txt);
  String doMoreWorkB(String txt);
  String doMoreWorkC(String txt);
  String doMoreWorkD(String txt);
}
``` 

With the Annotation we are generating the corresponding parts, used for the typesafe generated DynamicObjectAdapterBuilder.
You will get a FunctionalInterface for every Method declared in your interface, and an typed InvocationHandler and the Builder itself. 

To use the ***DynamicObjectAdapter*** you can do the following:

```java
    final Service service = ServiceAdapterBuilder
        .newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWork(new ServiceMethodDoWork() {
          @Override
          public String doWork(final String txt) {
            return "mocked";
          }
        })
        .withDoMoreWorkC(new ServiceMethodDoMoreWorkC() {
          @Override
          public String doMoreWorkC(final String txt) {
            return "mocked again";
          }
        })
        .buildForTarget(Service.class);

```

Also you can use the Lambda-Expressions for this. So your code will be shorter.
```java
    final Service serviceJDK8 = ServiceAdapterBuilder
        .newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWork(txt -> "mocked")
        .withDoMoreWorkC(txt -> "mocked again")
        .buildForTarget(Service.class);
``` 

You can use this for mocking too, if you want. For this you could adapt the methods you need and set the original to null.

```java
    final Service serviceJDK8Mock = ServiceAdapterBuilder
        .newBuilder()
        .setOriginal(null)
        .withDoWork(txt -> "mocked")
        .withDoMoreWorkC(txt -> "mocked again")
        .buildForTarget(Service.class);
```

If you can not use AnnotationProcessing, you are able to use the DynamicObjectAdapter itself.  

```java
    final ExtendedInvocationHandler<Service> extendedInvocationHandler
        = new ExtendedInvocationHandler<Service>() { };

    extendedInvocationHandler.addAdapter(new Object() {
      public String doMoreWorkB(String txt) {
        return "mocked";
      }
    });

    final AdapterBuilder<Service> adapterBuilder = new AdapterBuilder<Service>() {
      @Override
      protected ExtendedInvocationHandler<Service> getInvocationHandler() {
        return extendedInvocationHandler;
      }
    };

    final Service service = adapterBuilder.buildForTarget(Service.class);
```

Or if you want to write it even more compact..

```java
final Service service = new AdapterBuilder<Service>() {
      protected ExtendedInvocationHandler<Service> getInvocationHandler() {
        return new ExtendedInvocationHandler<Service>() {
          {
            addAdapter(new Object() {
              public String doMoreWorkB(String txt) {
                return "mocked";
              }
            });
          }
        };
      }
    }
    .buildForTarget(Service.class);
```

## StaticObjectAdapter
If you want, you can use the static ObjectAdapter nearly in the same way. But here you will get no Builder. You only will get the functional interfaces and the Adapter itself. Add the Annotation ***@StaticObjectAdapter*** to an Interface or Class.

```java
@StaticObjectAdapter
public interface Service {
  String doWork(String txt);
  String doMoreWorkA(String txt);
  String doMoreWorkB(String txt);
  String doMoreWorkC(String txt);
  String doMoreWorkD(String txt);
}
```

After a ***mvn clean compile*** you will get the Functionalinterfaces and the StaticObjectAdapter itself.


```java
final Service service = new ServiceStaticObjectAdapter()
        .withService(new ServiceImpl())
        .withServiceMethodDoMoreWorkC(new ServiceMethodDoMoreWorkC() {
          @Override
          public String doMoreWorkC(final String txt) {
            return "mocked";
          }
        })
        .withServiceMethodDoMoreWorkD(new ServiceMethodDoMoreWorkD() {
          @Override
          public String doMoreWorkD(final String txt) {
            return "mocked";
          }
        });

```

And here again, you can write it with short Lambda-syntax:

```java
final Service service = new ServiceStaticObjectAdapter()
        .withService(new ServiceImpl())
        .withServiceMethodDoMoreWorkC(txt -> "mocked")
        .withServiceMethodDoMoreWorkD(txt -> "mocked");
```

