package com.atlassian.sourcemap;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.function.Consumer;

public class ReadableSourceMapImpl implements ReadableSourceMap {
    private final SourceMapConsumer sourcemapConsumer;
    private int offset;

    /**
     * Parse source map - the "offset" param here is a slight leakage as a source-map itself should not have
     * any offset per se. However if multiple source-maps are joined they need an offset calculated for them which
     * this param facilitates.
     * @param sourceMap source map content.
     * @param offset the line offset for any mapping within this sourcemap.
     */
    private ReadableSourceMapImpl(String sourceMap, int offset) {
        this.offset = offset;
        sourcemapConsumer = new SourceMapConsumer(sourceMap);
    }

    /**
     * Parse source map.
     * @param sourceMap source map content.
     * @return an instance of ReadableSourceMap
     */
    public static ReadableSourceMap fromSourceWithOffset(final String sourceMap, final int offset) {
        return new ReadableSourceMapImpl(sourceMap, offset);
    }

    /**
     * Parse source map.
     * @param sourceMap source map content.
     * @return an instance of ReadableSourceMap
     */
    public static ReadableSourceMap fromSource(String sourceMap) {
        return new ReadableSourceMapImpl(sourceMap, 0);
    }

    /**
     * Parse source map.
     * @param sourceMap source map content.
     * @return an instance of ReadableSourceMap
     */
    public static ReadableSourceMap fromSource(InputStream sourceMap) {
        return new ReadableSourceMapImpl(InternalUtil.toString(sourceMap), 0);
    }

    /**
     * Parse source map.
     * @param sourceMap source map content.
     * @return an instance of ReadableSourceMap
     */
    public static ReadableSourceMap fromSource(Reader sourceMap) {
        return new ReadableSourceMapImpl(InternalUtil.toString(sourceMap), 0);
    }

    @Override
    public List<String> getSources() {
        return sourcemapConsumer.getSourceFileNames();
    }

    @Override
    public List<String> getSourcesContent() {
        return sourcemapConsumer.getSourcesContent();
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void addOffset(int offset) {
        this.offset += offset;
    }

    @Override
    public List<String> getNames() {
        return sourcemapConsumer.getSourceSymbolNames();
    }

    private void ensureOffset() {
        if (offset > 0) {
            sourcemapConsumer.recalculateWithOffset(offset);
        }

        offset = 0;
    }

    @Override
    public Mapping getMapping(int lineNumber, int column) {
        ensureOffset();
        return sourcemapConsumer.getMapping(lineNumber - offset, column);
    }

    @Override
    public void eachMapping(Consumer<Mapping> callback) {
        ensureOffset();
        sourcemapConsumer.eachMapping(callback);
    }
}
