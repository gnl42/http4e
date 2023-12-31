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
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

/**
 * 
 * @author Phil Zoio
 */
public class StartTagRule extends MultiLineRule {

    public StartTagRule(final IToken token) {
        this(token, false);
    }

    protected StartTagRule(final IToken token, final boolean endAsWell) {
        super("<", endAsWell ? "/>" : ">", token);
    }

    @Override
    protected boolean sequenceDetected(final ICharacterScanner scanner, final char[] sequence, final boolean eofAllowed) {
        final int c = scanner.read();
        if (sequence[0] == '<') {
            if (c == '?' || c == '!') {
                scanner.unread();
                // comment - abort
                return false;
            }
        } else if (sequence[0] == '>') {
            scanner.unread();
        }
        return super.sequenceDetected(scanner, sequence, eofAllowed);
    }
}