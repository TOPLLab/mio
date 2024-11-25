package com.atlassian.sourcemap;

/**
 * Mapping of position from generated file to source file.
 */
public interface Mapping
{
    int getGeneratedLine();

    void setGeneratedLine(int newLine);

    int getGeneratedColumn();

    int getSourceLine();

    int getSourceColumn();

    String getSourceFileName();

    void setSourceFileName(String sourceFileName);

    String getSourceSymbolName();
}
