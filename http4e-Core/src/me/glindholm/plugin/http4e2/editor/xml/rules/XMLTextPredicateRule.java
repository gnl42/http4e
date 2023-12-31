/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
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
package me.glindholm.plugin.http4e2.editor.xml.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Extra rule which will return specified token if sequence of characters matches
 * 
 * @author Phil Zoio
 */
public class XMLTextPredicateRule implements IPredicateRule {

    private final IToken token;
    private int charsRead;
    private boolean whiteSpaceOnly;
    boolean inCdata;

    public XMLTextPredicateRule(final IToken text) {
        token = text;
    }

    @Override
    public IToken getSuccessToken() {
        return token;
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
        return evaluate(scanner);
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner) {

        reinit();

        int c = 0;

        // carry on reading until we find a bad char
        // int chars = 0;
        while (isOK(c = read(scanner), scanner)) {
            // add character to buffer
            if (c == ICharacterScanner.EOF) {
                return Token.UNDEFINED;
            }

            whiteSpaceOnly = whiteSpaceOnly && Character.isWhitespace((char) c);
        }

        unread(scanner);

        // if we have only read whitespace characters, go back to where evaluation
        // started and return undefined token
        if (whiteSpaceOnly) {
            rewind(scanner, charsRead);
            return Token.UNDEFINED;
        }

        return token;

    }

    private boolean isOK(final int cc, final ICharacterScanner scanner) {

        char c = (char) cc;

        if (!inCdata) {
            if (c == '<') {

                int cdataCharsRead = 0;

                for (int i = 0; i < "![CDATA[".length(); i++) {
                    // whiteSpaceOnly = false;

                    c = (char) read(scanner);
                    cdataCharsRead++;

                    if (c != "![CDATA[".charAt(i)) {

                        // we don't have a match - wind back only the cdata characters
                        rewind(scanner, cdataCharsRead);
                        inCdata = false;
                        return false;
                    }
                }

                inCdata = true;
                return true;

                // return false;
            }
        } else if (c == ']') {

            for (int i = 0; i < "]>".length(); i++) {

                c = (char) read(scanner);

                if (c != "]>".charAt(i)) {
                    // we're still in the CData section, so just continue
                    // processing
                    return true;
                }
            }

            // we found all the matching characters at the end of the CData
            // section, so break out of this
            inCdata = false;

            // we're still in XML text
            return true;

        }

        return true;

    }

    private void rewind(final ICharacterScanner scanner, int theCharsRead) {
        while (theCharsRead > 0) {
            theCharsRead--;
            unread(scanner);
        }
    }

    private void unread(final ICharacterScanner scanner) {
        scanner.unread();
        charsRead--;
    }

    private int read(final ICharacterScanner scanner) {
        final int c = scanner.read();
        charsRead++;
        return c;
    }

    private void reinit() {
        charsRead = 0;
        whiteSpaceOnly = true;
    }

}