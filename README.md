# Welcome to the Project ProxyBuilder

[![Join the chat at https://gitter.im/RapidPM/proxybuilder](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/RapidPM/proxybuilder?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/RapidPM/proxybuilder.svg?branch=develop)](https://travis-ci.org/RapidPM/proxybuilder)

branch:
+ master:
[![Coverage Status - master](https://coveralls.io/repos/RapidPM/proxybuilder/badge.svg?branch=master)](https://coveralls.io/r/RapidPM/proxybuilder?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/55a3ab9532393900210005cc/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55a3ab9532393900210005cc)


+ develop:
[![Coverage Status - develop](https://coveralls.io/repos/RapidPM/proxybuilder/badge.svg?branch=develop)](https://coveralls.io/r/RapidPM/proxybuilder?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/55a3ab9a32393900170005be/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55a3ab9a32393900170005be)


# ProxyBuilder
With the proxy builder you can build proxies with different functions. There are two main versions available.
The first one is the static virtual proxy, the second one ist based on the dynamic proxies.


## based on static compiled Proxies
### Virtual Proxy
TBD

## based on dynamic proxies
The main class is the VirtualProxyBuilder. With this you can create virtual proxies with different
functionalities.


### Virtual Proxy
```java
final DemoLogic original = new DemoLogic();
final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .build();
```

### Security Proxy
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



### Metrics Proxy

```java
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addMetrics()
        .build();

```



# DynamicObjectAdapterBuilder