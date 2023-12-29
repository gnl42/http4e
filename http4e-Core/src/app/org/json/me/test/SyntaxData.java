package org.json.me.test;

import java.util.Collection;

/**
 * This class contains information for syntax coloring and styling for an extension
 */
public class SyntaxData {

    public String extension;
    public Collection<String> keywords;
    public String punctuation;
    public String comment;
    public String multiLineCommentStart;
    public String multiLineCommentEnd;

    public SyntaxData(final String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(final Collection<String> keywords) {
        this.keywords = keywords;
    }

    public String getMultiLineCommentEnd() {
        return multiLineCommentEnd;
    }

    public void setMultiLineCommentEnd(final String multiLineCommentEnd) {
        this.multiLineCommentEnd = multiLineCommentEnd;
    }

    public String getMultiLineCommentStart() {
        return multiLineCommentStart;
    }

    public void setMultiLineCommentStart(final String multiLineCommentStart) {
        this.multiLineCommentStart = multiLineCommentStart;
    }

    public String getPunctuation() {
        return punctuation;
    }

    public void setPunctuation(final String punctuation) {
        this.punctuation = punctuation;
    }
}