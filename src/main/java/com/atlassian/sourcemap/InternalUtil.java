package com.atlassian.sourcemap;

import java.io.InputStream;
import java.io.Reader;

/**
 * Code based on Google Closure Compiler https://code.google.com/p/closure-compiler
 */
class InternalUtil
{

    static String join(Iterable<String> list, String delimiter)
    {
        StringBuilder buff = new StringBuilder();
        boolean isFirst = true;
        for (String s : list) {
            if (isFirst) isFirst = false;
            else buff.append(delimiter);
            buff.append(s);
        }
        return buff.toString();
    }

    /**
     * Convert InputStream to String.
     */
    public static String toString(InputStream stream) {
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Convert Reader to String.
     */
    public static String toString(Reader reader) {
        java.util.Scanner s = new java.util.Scanner(reader).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
