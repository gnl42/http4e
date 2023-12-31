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
 * 
 * @author Phil Zoio
 */
public class NonMatchingRule implements IPredicateRule {

    public NonMatchingRule() {
    }

    @Override
    public IToken getSuccessToken() {
        return Token.UNDEFINED;
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
        return Token.UNDEFINED;
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner) {
        return Token.UNDEFINED;
    }

}
