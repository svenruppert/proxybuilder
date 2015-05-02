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

package junit.org.rapidpm.module.se.commons;

import org.rapidpm.module.se.commons.proxy.type.virtual.ProxyGenerator;

import static org.rapidpm.module.se.commons.proxy.type.virtual.Concurrency.NO_DUPLICATES;

/**
 * Created by ts40 on 19.02.14.
 */
public class FieldGetter {

    public static void main(String[] args) {

        DemoClassA demoClassA = new DemoClassA();
        demoClassA.demoClassB = new DemoClassB();
        demoClassA.demoClassB.value = "TestValue";

        //could get an NPE
//        final String value = demoClassA.getDemoClassB().getValue();

        final DemoClassA make = ProxyGenerator.make(DemoClassA.class, DemoClassA.class, NO_DUPLICATES);
        make.demoClassB = new DemoClassB();
        final DemoClassB demoClassB = make.getDemoClassB();
        final String value = demoClassB.getValue();
        System.out.println("value = " + value);





    }

//    private static String getValue(DemoClassA demoClassA) {
//        String demoClassBValue;
//        if (demoClassA != null) {
//            final DemoClassB demoClassB = demoClassA.getDemoClassB();
//            if (demoClassB != null) {
//                return demoClassB.getValue();
//            }
//        }
//        return null;
//    }

//    private static Optional<String> getValueOptional(DemoClassA demoClassA) {
//        String demoClassBValue;
//        if (demoClassA != null) {
//            final DemoClassB demoClassB = demoClassA.getDemoClassB();
//            if (demoClassB != null) {
//                return new Optional<>(demoClassB.getValue());
//            }
//        }
//        return new Optional<>(null);
//    }
//    private static <T> Optional<T> getValueOptional(T value) {
//        if (value != null) {
//            return new Optional<>(value);
//        } else {
//            return new Optional<>(null);
//        }
//    }

    public static class Optional<T> {

        T value;

        public Optional(T value) {
            this.value = value;
        }

        public Boolean isPresent() {
            return value != null;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

}
