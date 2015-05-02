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
import org.apache.commons.vfs2.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 */
public interface CommonsVfs2UrlType {

  public static class Dir implements Vfs.Dir {
    private final FileObject file;

    public Dir(FileObject file) {
      this.file = file;
    }

    public String getPath() {
      try {
        return file.getURL().getPath();
      } catch (FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    public Iterable<Vfs.File> getFiles() {
      return new Iterable<Vfs.File>() {
        public Iterator<Vfs.File> iterator() {
          return new FileAbstractIterator();
        }
      };
    }

    public void close() {
      try {
        file.close();
      } catch (FileSystemException e) {
        //todo log
      }
    }

    private class FileAbstractIterator extends AbstractIterator<Vfs.File> {
      final Stack<FileObject> stack = new Stack<FileObject>();

      {
        listDir(file);
      }

      protected Vfs.File computeNext() {
        while (!stack.isEmpty()) {
          final FileObject file = stack.pop();
          try {
            if (isDir(file)) listDir(file);
            else return getFile(file);
          } catch (FileSystemException e) {
            throw new RuntimeException(e);
          }
        }

        return endOfData();
      }

      private boolean isDir(FileObject file) throws FileSystemException {
        return file.getType() == FileType.FOLDER;
      }

      private boolean listDir(FileObject file) {
        return stack.addAll(listFiles(file));
      }

      private File getFile(FileObject file) {
        return new File(Dir.this.file, file);
      }

      protected List<FileObject> listFiles(final FileObject file) {
        try {
          FileObject[] files = file.getType().hasChildren() ? file.getChildren() : null;
          return files != null ? Arrays.asList(files) : new ArrayList<FileObject>();
        } catch (FileSystemException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static class File implements Vfs.File {
    private final FileObject root;
    private final FileObject file;

    public File(FileObject root, FileObject file) {
      this.root = root;
      this.file = file;
    }

    public String getName() {
      return file.getName().getBaseName();
    }

    public String getRelativePath() {
      String filepath = file.getName().getPath().replace("\\", "/");
      if (filepath.startsWith(root.getName().getPath())) {
        return filepath.substring(root.getName().getPath().length() + 1);
      }

      return null; //should not get here
    }

    public InputStream openInputStream() throws IOException {
      return file.getContent().getInputStream();
    }
  }
}
