/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rapidpm.proxybuilder.type.staticruntime.generator;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Collections.singletonList;

public class Generator {


  private static final Method DEFINE_CLASS_METHOD;
  private static final JavaCompiler JAVA_COMPILER;

  static {
    try {
      DEFINE_CLASS_METHOD = Proxy.class.getDeclaredMethod("defineClass0", ClassLoader.class, String.class, byte[].class, int.class, int.class);
      DEFINE_CLASS_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
    JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();
    if (JAVA_COMPILER == null) {
      throw new UnsupportedOperationException(
          "Cannot find java compiler! " +
              "Probably only JRE installed.");
    }
  }

  private Generator() {
  }

  public static Class make(ClassLoader loader, String className, CharSequence javaSource) {
    GeneratedClassFile gcf = new GeneratedClassFile();
    DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
    boolean result = compile(className, javaSource, gcf, dc);
    return processResults(loader, javaSource, gcf, dc, result);
  }

  private static boolean compile(String className, CharSequence javaSource, GeneratedClassFile gcf, DiagnosticCollector<JavaFileObject> dc) {
    final GeneratedJavaSourceFile gjsf = new GeneratedJavaSourceFile(className, javaSource);
    final StandardJavaFileManager standardFileManager = JAVA_COMPILER.getStandardFileManager(dc, null, null);

    final GeneratingJavaFileManager fileManager = new GeneratingJavaFileManager(standardFileManager, gcf);

    CompilationTask task = JAVA_COMPILER.getTask(null, fileManager, dc, null, null, singletonList(gjsf));

    return task.call();
  }

  private static Class processResults(ClassLoader loader, CharSequence javaSource,
                                      GeneratedClassFile gcf, DiagnosticCollector<?> dc, boolean result) {
    if (result) {
      return createClass(loader, gcf);
    } else {
// use your logging system of choice here
      System.err.println("Compile failed:");
      System.err.println(javaSource);
      dc.getDiagnostics().forEach(System.err::println);
      throw new IllegalArgumentException("Could not create proxy - compile failed");
    }
  }

  //go go to the classloader
  private static Class createClass(ClassLoader loader, GeneratedClassFile gcf) {
    try {
      byte[] data = gcf.getClassAsBytes();
      return (Class) DEFINE_CLASS_METHOD.invoke(null, loader, null, data, 0, data.length);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Proxy problem", e);
    }
  }
}
