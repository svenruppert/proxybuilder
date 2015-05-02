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

package org.rapidpm.module.se.commons.reflections.vfs;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/*
 * An implementation of {@link org.rapidpm.module.se.commons.reflections.vfs.Vfs.Dir} for directory {@link java.io.File}.
 */
public class SystemDir implements Vfs.Dir {
  private final File file;

  public SystemDir(File file) {
    if (file != null && (!file.isDirectory() || !file.canRead())) {
      throw new RuntimeException("cannot use dir " + file);
    }

    this.file = file;
  }

  @Override
  public String toString() {
    return getPath();
  }

  public String getPath() {
    if (file == null) {
      return "/NO-SUCH-DIRECTORY/";
    }
    return file.getPath().replace("\\", "/");
  }

  public Iterable<Vfs.File> getFiles() {
    if (file == null || !file.exists()) {
      return Collections.emptyList();
    }
    return new Iterable<Vfs.File>() {
      public Iterator<Vfs.File> iterator() {
        return new AbstractIterator<Vfs.File>() {
          final Stack<File> stack = new Stack<File>();

          {
            stack.addAll(listFiles(file));
          }

          protected Vfs.File computeNext() {
            while (!stack.isEmpty()) {
              final File file = stack.pop();
              if (file.isDirectory()) {
                stack.addAll(listFiles(file));
              } else {
                return new SystemFile(SystemDir.this, file);
              }
            }

            return endOfData();
          }
        };
      }
    };
  }

  private static List<File> listFiles(final File file) {
    File[] files = file.listFiles();

    if (files != null)
      return Lists.newArrayList(files);
    else
      return Lists.newArrayList();
  }

  public void close() {
  }
}
