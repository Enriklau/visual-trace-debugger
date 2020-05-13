// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.IDEACoverageRunner;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.TraceData;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class IDEATraceCoverageRunner extends IDEACoverageRunner {
    private static final Logger LOG = Logger.getInstance(IDEATraceCoverageRunner.class);

    @Override
    public void appendCoverageArgument(final String sessionDataFilePath,
                                       final String[] patterns,
                                       final String[] excludePatterns,
                                       final SimpleJavaParameters javaParameters,
                                       final boolean collectLineInfo,
                                       final boolean isSampling,
                                       @Nullable String sourceMapPath) {
        StringBuilder argument = new StringBuilder("-javaagent:");
        String agentPath = handleSpacesInAgentPath(PathUtil.getJarPathForClass(TraceData.class));
        if (agentPath == null) return;
        argument.append(agentPath);
        argument.append("=");
        try {
            final File tempFile = createTempFile();
            tempFile.deleteOnExit();
            write2file(tempFile, sessionDataFilePath);
            write2file(tempFile, String.valueOf(collectLineInfo));
            write2file(tempFile, Boolean.FALSE.toString()); //append unloaded
            write2file(tempFile, Boolean.FALSE.toString());//merge with existing
            write2file(tempFile, String.valueOf(isSampling));
            if (sourceMapPath != null) {
                write2file(tempFile, Boolean.TRUE.toString());
                write2file(tempFile, sourceMapPath);
            }
            if (patterns != null) {
                writePatterns(tempFile, patterns);
            }
            if (excludePatterns != null) {
                write2file(tempFile, "-exclude");
                writePatterns(tempFile, excludePatterns);
            }
            argument.append(tempFile.getCanonicalPath());
        } catch (IOException e) {
            LOG.info("Coverage was not enabled", e);
            return;
        }

        javaParameters.getVMParametersList().add(argument.toString());
    }

    private static void writePatterns(File tempFile, String[] patterns) throws IOException {
        for (String coveragePattern : patterns) {
            coveragePattern = coveragePattern.replace("$", "\\$").replace(".", "\\.").replaceAll("\\*", ".*");
            if (!coveragePattern.endsWith(".*")) { //include inner classes
                coveragePattern += "(\\$.*)*";
            }
            write2file(tempFile, coveragePattern);
        }
    }


    @Override
    @NotNull
    public String getPresentableName() {
        return "IntelliJ Trace IDEA";
    }

    @Override
    @NotNull
    public String getId() {
        return "traceidea";
    }

    @Override
    @NotNull
    public String getDataFileExtension() {
        return "itc";
    }

    @Override
    public boolean isCoverageByTestApplicable() {
        return true;
    }
}