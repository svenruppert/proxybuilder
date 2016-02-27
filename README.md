# Welcome to ProxyBuilder

Here we will describe the ProxyBuilder from [github.com/ProxyBuilder/proxybuilder](https://github.com/ProxyBuilder/proxybuilder)

## What are Proxies ?
If you want to have an overview of the Proxy-Pattern itself I can recommend
the following sources.

<iframe src="//de.slideshare.net/slideshow/embed_code/key/qOl1Sz53XkmKTh" width="595" height="485" frameborder="0" marginwidth="0" marginheight="0" scrolling="no" style="border:1px solid #CCC; border-width:1px; margin-bottom:5px; max-width: 100%;" allowfullscreen> </iframe> <div style="margin-bottom:5px"> <strong> <a href="//de.slideshare.net/svenruppert/proxy-deepdive-javaone20151027001" title="JavaOne 2015 Tutorial - Proxy DeepDive" target="_blank">JavaOne 2015 Tutorial - Proxy DeepDive</a> </strong> from <strong><a target="_blank" href="//de.slideshare.net/svenruppert">Sven Ruppert</a></strong> </div>

## What goal we want to reach?
This project was born, because I had to work a lot with old hughe projects. The only thing I could relay on, is the pure JDK.
So I started playing with different DesignPatterns and figured out, that Proxies are one of the most powerfull patterngroup for me.
During the time I was writing the german Book ***"Dynamic Proxies"*** with [Dr. Heinz Kabutz](http://www.javaspecialists.eu/) I started to write examples. Step by step the examples are more generic and the ***ProxyBuilder*** - project was born. 

## Some Examples
To have an ideaw hat you could do with the ***ProxyBuilder*** I will show here some examples. The more detailed 
informations you can find in the special sections of this website. This project is based on ***Java8***.

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
you need the following dep in your pom.xml.

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
Every ***PreAction*** will be executed before every method invokation in the order the ***PreAction*** was added.

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
Here our ***TeamCity*** will push regularly the binaryies. 
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
