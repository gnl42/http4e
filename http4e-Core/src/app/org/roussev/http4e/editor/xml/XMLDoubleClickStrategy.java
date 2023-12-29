/*
 *  Copyright 2017 Eclipse HttpClient (http4e) http://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.roussev.http4e.editor.xml;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * 
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class XMLDoubleClickStrategy implements ITextDoubleClickStrategy {

    protected ITextViewer fText;

    @Override
    public void doubleClicked(final ITextViewer part) {
        final int pos = part.getSelectedRange().x;

        if (pos < 0) {
            return;
        }

        fText = part;

        if (!selectComment(pos)) {
            selectWord(pos);
        }
    }

    protected boolean selectComment(final int caretPos) {
        final IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {
            int pos = caretPos;
            char c = ' ';

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (c == '\\') {
                    pos -= 2;
                    continue;
                }
                if (c == Character.LINE_SEPARATOR || c == '\"') {
                    break;
                }
                --pos;
            }

            if (c != '\"') {
                return false;
            }

            startPos = pos;

            pos = caretPos;
            final int length = doc.getLength();
            c = ' ';

            while (pos < length) {
                c = doc.getChar(pos);
                if (c == Character.LINE_SEPARATOR || c == '\"') {
                    break;
                }
                ++pos;
            }
            if (c != '\"') {
                return false;
            }

            endPos = pos;

            final int offset = startPos + 1;
            final int len = endPos - offset;
            fText.setSelectedRange(offset, len);
            return true;
        } catch (final BadLocationException x) {
        }

        return false;
    }

    protected boolean selectWord(final int caretPos) {

        final IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {

            int pos = caretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            startPos = pos;

            pos = caretPos;
            final int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
            return true;

        } catch (final BadLocationException x) {
        }

        return false;
    }

    private void selectRange(final int startPos, final int stopPos) {
        final int offset = startPos + 1;
        final int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }

}