/*
 * Copyright [2013] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
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

package org.rapidpm.module.se.commons.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

import java.io.Serializable;

/**
 * Created by Sven Ruppert on 01.08.13.
 */
public class Logger implements Serializable {


    private Class<?> declaringClass;
    private final org.apache.log4j.Logger logger;


    public Logger(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
        this.logger = org.apache.log4j.Logger.getLogger(declaringClass.getName());
    }


    public static Logger getLogger(Class<?> declaringClass) {
        new Logger(Logger.class).warn("Class with static Logger definition " + declaringClass);
        return new Logger(declaringClass);
    }


    public void debug(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.DEBUG, message, t);
    }

    public void debug(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.DEBUG, message, null);
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void trace(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.TRACE, message, t);
    }

    public void trace(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.TRACE, message, null);
    }

    public void error(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.ERROR, message, null);
    }

    public void error(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.ERROR, message, t);
    }

    public void fatal(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.FATAL, message, null);
    }

    public void fatal(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.FATAL, message, t);
    }

    public void info(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.INFO, message, null);
    }

    public void info(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.INFO, message, t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isEnabledFor(Priority level) {
        return logger.isEnabledFor(level);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void warn(Object message) {
        logger.log(Logger.class.getCanonicalName(), Level.WARN, message, null);
    }

    public void warn(Object message, Throwable t) {
        logger.log(Logger.class.getCanonicalName(), Level.WARN, message, t);
    }
}
