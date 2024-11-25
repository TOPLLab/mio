package com.atlassian.sourcemap;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Parsing SourceMap version 3 from JSON
 * <p>
 * https://sourcemaps.info/spec.html
 */
public class SourceMapJson {
    /**
     * Required File version (always the first entry in the object) and must be a positive integer.
     */
    private int version;

    /**
     * An optional source root, useful for relocating source files on a server or removing repeated values
     * in the “sources” entry.  This value is prepended to the individual entries in the “source” field.
     */
    private String sourceRoot;

    /**
     * A list of original sources used by the “mappings” entry.
     */
    private List<String> sources;

    /**
     * An optional list of source content, useful when the “source” can’t be hosted.
     * The contents are listed in the same order as the sources in line 5.
     * “null” may be used if some original sources should be retrieved by name.
     */
    private List<String> sourcesContent;

    /**
     * A list of symbol names used by the “mappings” entry.
     */
    private List<String> names;

    /**
     * A string with the encoded mapping data.
     */
    private String mappings;

    private void ensureSourceContents() {
        if (sourcesContent == null || sourcesContent.size() != sources.size()) {
            sourcesContent = Arrays.asList(new String[sources.size()]);
        }
    }

    private void applyAndVoidRoot() {
        if (sourceRoot != null && !sourceRoot.equals("")) {
            this.sources = sources.stream().map(source -> sourceRoot + source).collect(toList());
        }
        sourceRoot = null;
    }

    public static SourceMapJson parse(String sourceMapData) {
        SourceMapJson sourceMapRoot;

        try {
            sourceMapRoot = new Gson().fromJson(sourceMapData, SourceMapJson.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
        sourceMapRoot.ensureSourceContents();
        sourceMapRoot.applyAndVoidRoot();

        return sourceMapRoot;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSourceRoot() {
        return sourceRoot;
    }

    public void setSourceRoot(String sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getSourcesContent() {
        return sourcesContent;
    }

    public void setSourcesContent(List<String> sourcesContent) {
        this.sourcesContent = sourcesContent;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getMappings() {
        return mappings;
    }

    public void setMappings(String mappings) {
        this.mappings = mappings;
    }

    public static class Builder {

        private final SourceMapJson sourceMapJson = new SourceMapJson();

        public Builder withVersion(int version) {
            sourceMapJson.version = version;
            return this;
        }

        public Builder withSources(List<String> sources) {
            sourceMapJson.sources = sources;
            return this;
        }

        public Builder withSourcesContent(List<String> sourcesContent) {
            sourceMapJson.sourcesContent = sourcesContent;
            return this;
        }

        public Builder withNames(List<String> names) {
            sourceMapJson.names = names;
            return this;
        }

        public Builder withMappings(String mappings) {
            sourceMapJson.mappings = mappings;
            return this;
        }

        public SourceMapJson build() {
            if (sourceMapJson.version == 0) {
                sourceMapJson.version = 3;
            }

            return sourceMapJson;
        }
    }
}
