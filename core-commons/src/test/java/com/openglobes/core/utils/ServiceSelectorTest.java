/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ServiceSelectorTest {

    @Test
    public void testSelector() throws IOException,
                                      InterruptedException,
                                      ClassNotFoundException {
        File root = new File("target/sample");
        var r = prepareJar(root);
        var loader = ServiceSelector.getClassLoader(r);
        Class<?> cls = Class.forName("test.IEcho",
                                     false,
                                     loader);
        assertDoesNotThrow(() -> {
            ServiceSelector.selectService(cls,
                                          "test.Echo",
                                          loader);
        },
                           "Service loader failed.");
    }

    private File prepareJar(File root) throws IOException,
                                              InterruptedException {
        prepareClasses(root);
        prepareMetaInf(root);
        return createJar(root);
    }

    private File createJar(File root) throws IOException,
                                             InterruptedException {
        var j = new File(root.getParentFile().getAbsolutePath() + "/echo-1.0.jar");
        var cmd = "jar cf \"" + j.getAbsolutePath() + "\" -C \"" + root.getAbsolutePath() + "\" .";
        cmd = cmd.replace('\\', '/');
        Process pr = Runtime.getRuntime().exec(cmd);
        pr.waitFor();
        if (pr.exitValue() != 0) {
            throw new IOException("jar command line returns " + pr.exitValue());
        }
        return j;
    }

    private void prepareMetaInf(File root) throws IOException {
        var metaInf = new File(root, "META-INF/services/test.IEcho");
        metaInf.getParentFile().mkdirs();
        Files.write(metaInf.toPath(),
                    "test.Echo\n".getBytes(StandardCharsets.UTF_8));
    }

    private void prepareClasses(File root) throws IOException {

        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null,
                     null,
                     null,
                     prepareSources(root));
    }

    private void writeJavaSources(String source, File java) throws IOException {
        java.getParentFile().mkdirs();
        Files.write(java.toPath(), source.getBytes(StandardCharsets.UTF_8));
    }

    private String[] prepareSources(File root) throws IOException {
        // Prepare source somehow.
        String interfaceSource = "package test;\n"
                                 + "public interface IEcho {\n"
                                 + "    void echo();\n"
                                 + "}";
        String implSource = "package test;\n"
                            + "public class Echo implements IEcho {\n"
                            + "    public Echo() {}\n"
                            + "    public void echo() {\n"
                            + "        System.out.println(\"Hi, Echo!\");\n"
                            + "    }\n"
                            + "}";

        // Save source in .java file.
        File interfaceFile = new File(root, "test/IEcho.java");
        File implFile = new File(root, "test/Echo.java");
        writeJavaSources(interfaceSource,
                         interfaceFile);
        writeJavaSources(implSource,
                         implFile);

        return new String[]{
            interfaceFile.getPath(),
            implFile.getPath()
        };
    }
}
