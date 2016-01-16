package org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sven Ruppert on 13.05.15.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DynamicObjectAdapterBuilder {
}
