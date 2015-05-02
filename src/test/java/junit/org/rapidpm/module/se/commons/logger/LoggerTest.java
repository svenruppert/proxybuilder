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

package junit.org.rapidpm.module.se.commons.logger;

import org.junit.Ignore;
import org.junit.Test;
import org.rapidpm.module.se.commons.logger.Logger;

/**
 * User: Sven Ruppert Date: 18.09.13 Time: 11:05
 */
public class LoggerTest {
    @Test  @Ignore
    public void testLoggerAppender() throws Exception {
        final Logger logger = Logger.getLogger(LoggerTest.class);

        final long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            logger.debug("Eine DebugMeldung -> " + i);
        }
        final long stop = System.nanoTime();
        System.out.println("delts [ms] = " + (stop - start) / 1000 / 1000);
    }
}
