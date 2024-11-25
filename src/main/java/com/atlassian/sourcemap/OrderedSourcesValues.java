package com.atlassian.sourcemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;

/**
 *@since 2.0.0
 *
 * Helper class to ensure we have a unique set of "values", while allowing to keep pointing to the same index even for
 * values that no longer exist within the final value list.
 * This is required as joining multiple source maps might require remapping them.
 */
public class OrderedSourcesValues {
    private final List<String> values = new ArrayList<>();
    private final Map<String, Integer> indexLookup = new HashMap<>();
    private int nextIndex;

    void add(String value) {
        // do not add things that already exist
        if (value != null && values.contains(value)) {
            return;
        }
        values.add(value);
        indexLookup.put(value, nextIndex);
        nextIndex += 1;
    }

    void replaceAt(int index, String value) {
        values.set(index, value);
        indexLookup.put(value, index);
    }

    boolean hasValue(String value) {
        return indexLookup.containsKey(value);
    }

    String getValueAtIndex(int index) {
        return values.get(index);
    }

    Integer getIndex(String value) {
        return indexLookup.get(value);
    }

    List<String> getValues() {
        return unmodifiableList(values);
    }
}
