/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.openglobes.core.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.ServiceLoader;
import javax.management.ServiceNotFoundException;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class ServiceSelector {

    public static <T> T selectService(Class<T> clazz,
                               String implClass,
                               Collection<File> fileOrDir) throws ServiceNotFoundException {
        var jars = getAllJars(fileOrDir);
        return selectClass(clazz,
                           implClass,
                           jars);
    }

    public static <T> T selectService(Class<T> clazz,
                               String implClass,
                               File... fileOrDir) throws ServiceNotFoundException {
        var jars = getAllJars(Arrays.asList(fileOrDir));
        try {
            var cl = URLClassLoader.newInstance(getURLs(jars));
            return loadObject(ServiceLoader.load(clazz, cl),
                              implClass);
        }
        catch (MalformedURLException ex) {
            throw new ServiceNotFoundException(implClass);
        }
    }

    private static File[] getAllJars(Collection<File> fileOrDir) {
        var files = new HashSet<File>(128);
        fileOrDir.stream().filter(f -> !(f == null)).forEachOrdered(f -> {
            if (f.isFile()) {
                files.add(f);
            }
            else {
                files.addAll(Arrays.asList(getJars(f)));
            }
        });
        File[] jars = new File[files.size()];
        files.toArray(jars);
        return jars;
    }

    private static File[] getJars(File root) {
        Objects.requireNonNull(root);
        var files = root.listFiles((File file) -> {
            var suffix = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            return file.isFile() && suffix.equals("jar");
        });
        return files;
    }

    private static URL[] getURLs(File[] jars) throws MalformedURLException {
        URL[] urls = new URL[jars.length];
        if (jars.length > 0) {
            for (int i = 0; i < jars.length; ++i) {
                urls[i] = jars[i].toURI().toURL();
            }
        }
        return urls;
    }

    private static <T> T loadObject(ServiceLoader<T> sl, String clazz) {
        T obj = null;
        for (var service : sl) {
            var ln = service.getClass().getCanonicalName();
            if (ln.equals(clazz)) {
                obj = service;
                break;
            }
        }
        Objects.requireNonNull(obj, clazz);
        return obj;
    }

    private static <T> T selectClass(Class<T> clazz,
                                     String implClass,
                                     File[] jars) throws ServiceNotFoundException {
        try {
            var cl = URLClassLoader.newInstance(getURLs(jars));
            return loadObject(ServiceLoader.load(clazz, cl),
                              implClass);
        }
        catch (MalformedURLException ex) {
            throw new ServiceNotFoundException(implClass);
        }
    }

    private ServiceSelector() {
    }
}
