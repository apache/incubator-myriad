/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mesos;

public class MesosNativeLibrary {
    /**
     * Represent a 'libmesos' version with Major, Minor, and Patch versions. We
     * use a class here to make it easier to do version compatibility checking.
     * For example:
     * <pre>
     * {@code
     * static Version BugFixVersion = new Version(0, 22, 1);
     * public static void myFunction() {
     *   if (version().compareTo(BugFixVersion) >= 0) {
     *     // New behavior with bug fix.
     *   } else {
     *     // Old behavior for backwards compatibility.
     *   }
     * }
     * }
     * </pre>
     */
    public static class Version implements Comparable<Version> {
        public Version(long major, long minor, long patch) {
            if (major < 0) {
                throw new IllegalArgumentException(
                        "Major version must not be negative");
            }

            if (minor < 0) {
                throw new IllegalArgumentException(
                        "Minor version must not be negative");
            }

            if (patch < 0) {
                throw new IllegalArgumentException(
                        "Patch version must not be negative");
            }

            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        public Version(long major, long minor) {
            this(major, minor, 0);
        }

        public Version(long major) {
            this(major, 0, 0);
        }

        public boolean equals(Version other) {
            return other != null &&
                    major == other.major &&
                    minor == other.minor &&
                    patch == other.patch;
        }

        /**
         * Compare this version to an 'other' one. The comparison is done
         * lexicographically. This returns -1 if this version is 'lesser' than the
         * other, 0 if they are equivalent, and 1 if this version is 'greater'.
         */
        @Override
        public int compareTo(Version other) {
            if (other == null) {
                throw new IllegalArgumentException("other Version must not be null");
            }

            if (major < other.major) {
                return -1;
            } else if (major > other.major) {
                return 1;
            }

            if (minor < other.minor) {
                return -1;
            } else if (minor > other.minor) {
                return 1;
            }

            if (patch < other.patch) {
                return -1;
            } else if (patch > other.patch) {
                return 1;
            }

            return 0;
        }

        /**
         * A helper that is easier to use than 'compareTo', this returns
         * true if 'this' version is strictly 'less than', not 'less than
         * or equal to' the 'other' version.
         */
        public boolean before(Version other) {
            return this.compareTo(other) < 0;
        }

        /**
         * A helper that is easier to use than 'compareTo', this returns
         * true if 'this' version is strictly 'greater than', not 'greater
         * than or equal to' the 'other' version.
         */
        public boolean after(Version other) {
            return this.compareTo(other) > 0;
        }

        public final long major;
        public final long minor;
        public final long patch;
    }

    /**
     * Attempts to load the native library (if it was not previously loaded)
     * from the given path. If the path is null 'java.library.path' is used to
     * load the library.
     */
    public static synchronized void load(String path) {
        // Our JNI library will actually set 'loaded' to true once it is
        // loaded, that way the library can get loaded by a user via
        // 'System.load' in the event that they want to specify an
        // absolute path and we won't try and reload the library ourselves
        // (which would probably fail because 'java.library.path' might
        // not be set).
        if (loaded) {
            return;
        }

        // In some circumstances, such as when sandboxed class loaders are used,
        // the current thread's context class loader will not be able to see
        // MesosNativeLibrary (even when executing this code!).
        // We therefore, temporarily swap the thread's context class loader with
        // the class loader that loaded this class, for the duration of the native
        // library load.
        ClassLoader contextClassLoader =
                Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                MesosNativeLibrary.class.getClassLoader());
        try {
            if (path != null) {
                System.load(path);
            } else {
                // TODO(tillt): Change the default fallback to JNI specific library
                // once libmesos has been split.
                System.loadLibrary("mesos");
            }
        } catch (UnsatisfiedLinkError error) {
            System.err.println("Failed to load native Mesos library from " +
                    (path != null ? path : System.getProperty("java.library.path")));
            throw error;
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static void load() {
        // Try to get the JNI specific library path from the environment.
        String path = System.getenv("MESOS_NATIVE_JAVA_LIBRARY");

        // As a fallback, use deprecated environment variable to extract that path.
        if (path == null) {
            path = System.getenv("MESOS_NATIVE_LIBRARY");
            if (path != null) {
                System.out.println("Warning: MESOS_NATIVE_LIBRARY is deprecated, " +
                        "use MESOS_NATIVE_JAVA_LIBRARY instead. Future releases will " +
                        "not support JNI bindings via MESOS_NATIVE_LIBRARY.");
            }
        }

        load(path);
    }

    /**
     * Returns the version of the native loaded library, or throws a
     * runtime exception if the library is not loaded. This was
     * introduced in MESOS 0.22.1. Any version prior to that will be
     * 0.0.0. This means you should not make version specific decision
     * before the 0.22.1 version boundary. For example, if you found a
     * bug that was fixed in 0.19.0, you will *not* be able to perform
     * the following check correctly:
     *
     *   if (version().before(new Version(0, 19, 0))) {
     *     ...
     *   }
     *
     * This predicate will return true for all versions up until 0.22.1.
     */
    public static synchronized Version version() {
        // Since we allow 'load' to be called with a parameter, we can not load on
        // behalf of the user here. Instead, we throw an exception if the library
        // has not been loaded.
        if (!loaded) {
            throw new RuntimeException("'libmesos' not loaded");
        }

        if (version == null) {
            // Try to load the libmesos version identifier. If we get an
            // 'UnsatisfiedLinkError' then this means we are loading a 'libmesos' with
            // a version prior to 0.22.1, which is when the 'MAJOR', 'MINOR', and
            // 'PATCH' version identifiers were introduced.
            try {
                version = _version();
            } catch (UnsatisfiedLinkError error) {
                System.err.println(
                        "WARNING: using an old version of 'libmesos'" +
                                " without proper version information: " + error.getMessage());

                // If we're using a version of 'libmesos' less than 0.22.1, then we set
                // the version to 0.0.0.
                version = new Version(0, 0, 0);
            }
        }

        return version;
    }

    public static final String VERSION = "1.5.0";

    private static Version version = null;

    private static boolean loaded = false;

    /**
     * Native implementation of 'libmesos' version identifier function.
     */
    private static native Version _version();
}
