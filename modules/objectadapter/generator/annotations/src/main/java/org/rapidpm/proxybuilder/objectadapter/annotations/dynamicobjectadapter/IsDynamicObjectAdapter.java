package org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by svenruppert on 24.10.15.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface IsDynamicObjectAdapter {
}
