// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.android.desugar;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import org.objectweb.asm.ClassReader;

class ClassReaderFactory {
  private final ZipFile jar;
  private final CoreLibraryRewriter rewriter;

  public ClassReaderFactory(ZipFile jar, CoreLibraryRewriter rewriter) {
    this.jar = jar;
    this.rewriter = rewriter;
  }

  /**
   * Returns a reader for the given/internal/Class$Name if the class is defined in the wrapped Jar
   * and {@code null} otherwise.  For simplicity this method turns checked into runtime excpetions
   * under the assumption that all classes have already been read once when this method is called.
   */
  @Nullable
  public ClassReader readIfKnown(String internalClassName) {
    ZipEntry entry = jar.getEntry(rewriter.unprefix(internalClassName) + ".class");
    if (entry == null) {
      return null;
    }
    try (InputStream bytecode = jar.getInputStream(entry)) {
      // ClassReader doesn't take ownership and instead eagerly reads the stream's contents
      return rewriter.reader(bytecode);
    } catch (IOException e) {
      // We should've already read through all files in the Jar once at this point, so we don't
      // expect failures reading some files a second time.
      throw new IllegalStateException("Couldn't load " + internalClassName, e);
    }
  }
}
