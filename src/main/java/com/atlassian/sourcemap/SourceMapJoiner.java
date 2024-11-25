package com.atlassian.sourcemap;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins source maps. It expects that files joined with newlines.
 */
public class SourceMapJoiner {
    static class ReadSourceMapWithOffset {
        ReadableSourceMap sourceMap;
        int linesCount;

        public ReadSourceMapWithOffset(ReadableSourceMap sourceMap, int linesCount) {
            this.sourceMap = sourceMap;
            this.linesCount = linesCount;
        }
    }

    List<ReadSourceMapWithOffset> sourceMaps = new ArrayList<>();

    /**
     * Create joined source map by joining multiple source maps, each of it additionally could have the offset.
     * @param sourceMap source map to add.
     * @param length number of lines of added source map.
     */
    public void add(ReadableSourceMap sourceMap, int length) { add(sourceMap, length, 0); }

    /**
     * Create joined source map by joining multiple source maps, each of it additionally could have the offset.
     * @param sourceMap source map to add.
     * @param length number of lines of added source map.
     * @param offset offset of added source map (note - the offset is inside of its content, not outside).
     */
    public void add(ReadableSourceMap sourceMap, int length, int offset) {
        sourceMap.addOffset(offset);
        sourceMaps.add(new ReadSourceMapWithOffset(sourceMap, length));
    }

    /**
     * Joins added source maps.
     * @return joined source map.
     */
    public WritableSourceMap join() {
        WritableSourceMap joinedMap = new WritableSourceMapImpl.Builder().empty().build();
        int lineOffset = 0;
        for (ReadSourceMapWithOffset sourceMapWithOffset : sourceMaps) {
            final int currentLineOffset = lineOffset;
            lineOffset += sourceMapWithOffset.linesCount;

            ReadableSourceMap sourceMap = sourceMapWithOffset.sourceMap;
            if (sourceMap == null) {
                continue;
            }

            joinedMap.addSourcesAndContents(sourceMap.getSources(), sourceMap.getSourcesContent());
            sourceMap.addOffset(currentLineOffset);
            sourceMap.eachMapping(joinedMap::addMapping);
        }
        return joinedMap;
    }
}
