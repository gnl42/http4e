package org.json.me.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * This class manages the syntax coloring and styling data
 */
class SyntaxManager2 {
    // Lazy cache of SyntaxData objects
    private static Map<String, SyntaxData> data = new Hashtable<>();

    /**
     * Gets the syntax data for an extension
     */
    public static synchronized SyntaxData getSyntaxData(final String extension) {
        // Check in cache
        SyntaxData sd = data.get(extension);
        if (sd == null) {
            // Not in cache; load it and put in cache
            sd = loadSyntaxData(extension);
            if (sd != null) {
                data.put(sd.getExtension(), sd);
            }
        }
        return sd;
    }

    /**
     * Loads the syntax data for an extension
     *
     * @param extension the extension to load
     * @return SyntaxData
     */
    private static SyntaxData loadSyntaxData(final String extension) {
        SyntaxData sd = null;
        try {
            final ResourceBundle rb = ResourceBundle.getBundle("examples.ch11." + extension);
            sd = new SyntaxData(extension);
            sd.setComment(rb.getString("comment"));
            sd.setMultiLineCommentStart(rb.getString("multilinecommentstart"));
            sd.setMultiLineCommentEnd(rb.getString("multilinecommentend"));

            // Load the keywords
            final Collection<String> keywords = new ArrayList<>();
            for (final StringTokenizer st = new StringTokenizer(rb.getString("keywords"), " "); st.hasMoreTokens();) {
                keywords.add(st.nextToken());
            }
            sd.setKeywords(keywords);

            // Load the punctuation
            sd.setPunctuation(rb.getString("punctuation"));
        } catch (final MissingResourceException e) {
            // Ignore
        }
        return sd;
    }
}
