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

package org.rapidpm.proxybuilder.type.staticruntime.virtual;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class VirtualProxySourceGenerator {

  protected final Class subject;
  protected final Class realSubject;
  private final String proxy;
  private final CreationStrategy type;
  private CharSequence charSequence;

  public VirtualProxySourceGenerator(Class subject, Class realSubject, CreationStrategy type) {
    this.subject = subject;
    this.realSubject = realSubject;
    this.type = type;
    this.proxy = makeProxyName(subject, type);

  }

  private static String makeProxyName(Class subject, CreationStrategy type) {
    return "$$_" + subject.getName().replace('.', '_') +
        "Proxy_" + Integer.toHexString(System.identityHashCode(
        subject.getClassLoader())) + "_" + type;
  }

  public String getProxyName() {
    return proxy;
  }

  public CharSequence getCharSequence() {
    if (charSequence == null) {
      StringWriter sw = new StringWriter();
      generateProxyClass(new PrintWriter(sw)); //write it down
      charSequence = sw.getBuffer();
    }
    //System.out.println("charSequence = " + charSequence.toString());
    return charSequence;
  }

  private void generateProxyClass(PrintWriter out) {
    addClassDefinition(out);
    addProxyBody(out);
    out.close();
  }

  private void addClassDefinition(PrintWriter out) {
    addImports(out);
    out.printf("public class %s %s %s {%n",
        proxy, getInheritanceType(subject), subject.getName());
  }

  private void addProxyBody(PrintWriter out) {
    addRealSubjectCreation(out, subject.getName(), realSubject.getName());
    addProxiedMethods(out);
    out.println("}");
  }

  protected void addImports(PrintWriter out) {

  }

  private String getInheritanceType(Class subject) {
    return subject.isInterface() ? "implements" : "extends";
  }

  protected abstract void addRealSubjectCreation(PrintWriter out, String name, String realName);

  private void addProxiedMethods(PrintWriter out) {
    for (Method m : subject.getMethods()) {
      addProxiedMethod(out, m);
    }
    addToStringIfInterface(out);
  }

  private void addProxiedMethod(PrintWriter out, Method m) {
    if (Modifier.isFinal(m.getModifiers())) return;
    addMethodSignature(out, m);
    addMethodBody(out, m);   //NPE da val ger getter gefuellt wird

    final Class<?> returnType = m.getReturnType();
    if (returnType == void.class) out.printf(");%n }%n");
    else {
      out.printf(");%n");  //end of orig method.. start proxy additional stuff
      final boolean aFinal = Modifier.isFinal(returnType.getModifiers());
      if (!returnType.isPrimitive() && !returnType.isArray() && !aFinal) {
        final String typeName = returnType.getTypeName();
        final String proxyGenerator = StaticProxyGenerator.class.getCanonicalName();
        final String concurrency = CreationStrategy.class.getCanonicalName();
        out.printf(" if (val == null) { System.out.println(\" val == null for method  + " + m.getName() + "\");} %n");
        out.printf(typeName + " proxyObj = " + proxyGenerator + ".make(" + typeName + ".class, " + typeName + ".class, " + concurrency + "." + type.toString() + "); %n");

        if (type.equals(CreationStrategy.OnExistingObject)) {
          out.printf("try { %n");
          out.printf("    proxyObj.getClass().getDeclaredField(\"realSubject\").set(proxyObj, val);  %n");
          out.printf("} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {  %n");
          out.printf("    e.printStackTrace(); %n");
          out.printf("}  %n");
        }
        out.printf(" return proxyObj; %n");
      } else {
        out.printf(" return val; %n");
      }
      out.printf("%n}%n");
//            out.printf(" return val; }%n");
    }
  }

  private void addToStringIfInterface(PrintWriter out) {
    if (subject.isInterface()) {
      out.println();
      out.println(" public String toString() {");
      out.println(" if(realSubject() == null ) return \"NullObjectHolder in \" + this.getClass() ;");
      out.println(" return realSubject().toString();");
      out.println(" }");
    }
  }

  private void addMethodSignature(PrintWriter out, Method m) {
    out.printf("%n public %s", Util.prettyPrint(m.getReturnType()));
    out.printf(" %s(", m.getName());
    addParameterList(out, m);
    out.printf(") {%n ");
  }

  private void addMethodBody(PrintWriter out, Method m) {
//        addReturnKeyword(out, m);
    addMethodBodyDelegatingToRealSubject(out, m);
  }

  private void addParameterList(PrintWriter out, Method m) {
    Class<?>[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      String next = i == types.length - 1 ? "" : ", ";
      out.printf("%s p%d%s", Util.prettyPrint(types[i]), i, next);
    }
  }

  private void addMethodBodyDelegatingToRealSubject(PrintWriter out, Method m) {
    //hole result
    final Class<?> returnType = m.getReturnType();
    if (returnType == void.class) out.printf("realSubject().%s(", m.getName());
    else if (m.getName().equals("toString")) {
      out.println("String val;");
      out.println(" if(realSubject() == null ) val = \"NullObjectHolder in \" + this.getClass() ; ");
      out.printf(" else val = realSubject().%s(", m.getName());
    } else if (m.getName().startsWith("get") && !returnType.isPrimitive()) {
      String name;
      if (returnType.isArray()) name = returnType.getSimpleName();
      else name = returnType.getName();
      out.println(name + " val;");
      out.println(" if(realSubject() == null ) val = null ; ");
      out.printf(" else val = realSubject().%s(", m.getName());   //NPE
    } else {
      String name;
      if (returnType.isArray()) name = returnType.getSimpleName();
      else name = returnType.getName();
      out.println(name + " val;");
      out.printf("val = realSubject().%s(", m.getName());   //NPE

    }
    addMethodCall(out, m);
  }

  private void addMethodCall(PrintWriter out, Method m) {
    Class<?>[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      String next = i == types.length - 1 ? "" : ", ";
      out.printf("p%d%s", i, next);
    }
  }
}