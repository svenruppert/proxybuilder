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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

/**
 *
 */
public class JarInputFile implements Vfs.File {
  private final ZipEntry entry;
  private final JarInputDir jarInputDir;
  private final long fromIndex;
  private final long endIndex;

  public JarInputFile(ZipEntry entry, JarInputDir jarInputDir, long cursor, long nextCursor) {
    this.entry = entry;
    this.jarInputDir = jarInputDir;
    fromIndex = cursor;
    endIndex = nextCursor;
  }

  public String getName() {
    String name = entry.getName();
    return name.substring(name.lastIndexOf("/") + 1);
  }

  public String getRelativePath() {
    return entry.getName();
  }

  public InputStream openInputStream() throws IOException {
    return new InputStream() {
      @Override
      public int read() throws IOException {
        if (jarInputDir.cursor >= fromIndex && jarInputDir.cursor <= endIndex) {
          int read = jarInputDir.jarInputStream.read();
          jarInputDir.cursor++;
          return read;
        } else {
          return -1;
        }
      }
    };
  }
}
