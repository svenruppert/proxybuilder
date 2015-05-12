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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Waiting at CMD for pressing Q.
 * <p>
 * Created by Sven Ruppert on 15.02.14.
 */
public class WaitForQ {

  private List<ShutDownAction> shutDownActions = new ArrayList<>();

  public void addShutDownAction(ShutDownAction action) {
    shutDownActions.add(action);
  }

  public void waitForQ() {
    final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    final Thread t = new Thread(() -> {
      System.out.println("press Q THEN ENTER to terminate");
      int quit = 0;
      while (true) {
        try {
          Thread.sleep(10000);
          String msg = null;
          while (true) {
            try {
              msg = in.readLine();
            } catch (Exception ignored) {
            }
            if (msg != null && msg.equals("Q")) {
              quit = 1;
            }
            if (quit == 1) break;
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        break;
      }
      //start ShutDownActions
      shutDownActions.forEach(ShutDownAction::execute);
    });
    t.start();
  }

  @FunctionalInterface
  public interface ShutDownAction {
    void execute();
  }
}
