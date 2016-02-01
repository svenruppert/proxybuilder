/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package junit.org.rapidpm.module.se.commons.reflection;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class ClassPathScanner {

  public List<String> scannForClasses() {
    final List<String> result = new ArrayList<>();


    return result;
  }

  public ClassLoader[] classLoaders(ClassLoader... classLoaders) {
    if (classLoaders != null && classLoaders.length != 0) {
      return classLoaders;
    } else {
      ClassLoader contextClassLoader = contextClassLoader();
      ClassLoader staticClassLoader = staticClassLoader();
      if (contextClassLoader != null)
        if (staticClassLoader != null && contextClassLoader != staticClassLoader)
          return new ClassLoader[]{contextClassLoader, staticClassLoader};
        else return new ClassLoader[]{contextClassLoader};
      else return new ClassLoader[]{};
    }
  }

  public ClassLoader contextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public ClassLoader staticClassLoader() {
    return ClassPathScanner.class.getClassLoader();
  }

  public Collection<URL> forJavaClassPath() {
    Collection<URL> urls = new ArrayList<>();
    String javaClassPath = System.getProperty("java.class.path");
    if (javaClassPath != null) {
      for (String path : javaClassPath.split(File.pathSeparator)) {
        try {
          urls.add(new File(path).toURI().toURL());
        } catch (Exception e) {
          //Todo logging
        }
      }
    }
    return distinctUrls(urls);
  }

  private Collection<URL> distinctUrls(Collection<URL> urls) {
    Map<String, URL> distinct = new HashMap<>(urls.size());
    for (URL url : urls) {
      distinct.put(url.toExternalForm(), url);
    }
    return distinct.values();
  }


  public String cleanPath(final URL url) {
    String path = url.getPath();
    try {
      path = URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException e) { /**/ }
    if (path.startsWith("jar:")) {
      path = path.substring("jar:".length());
    }
    if (path.startsWith("file:")) {
      path = path.substring("file:".length());
    }
    if (path.endsWith("!/")) {
      path = path.substring(0, path.lastIndexOf("!/")) + "/";
    }
    return path;
  }

  private String resourceName(String name) {
    if (name != null) {
      String resourceName = name.replace(".", "/");
      resourceName = resourceName.replace("\\", "/");
      if (resourceName.startsWith("/")) {
        resourceName = resourceName.substring(1);
      }
      return resourceName;
    }
    return null;
  }

}
