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

package org.rapidpm.module.se.commons;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ts40 on 18.02.14.
 */
public class FieldSetter {


    //JDK7
    public <T, S> boolean setFieldIfNotNull(T target, String propertyName ,S source ){
        if (source != null){
            try {
                Field declaredField = target.getClass().getDeclaredField(propertyName);
                if ( ! declaredField.isAccessible()){
                    declaredField.setAccessible(true);
                    declaredField.set(target , source);
                    declaredField.setAccessible(false);
                } else {
                    declaredField.set(target , source);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public <T, S> boolean useMethodIfNotNull(T target, String methodName ,S source ){
        if (source != null){
            try {
                Method declaredMethod = target.getClass().getDeclaredMethod(methodName, source.getClass());
                if ( ! declaredMethod.isAccessible()){
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(target, source);
                    declaredMethod.setAccessible(false);
                } else {
                    declaredMethod.invoke(target , source);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else{
            return false;
        }
    }





}
