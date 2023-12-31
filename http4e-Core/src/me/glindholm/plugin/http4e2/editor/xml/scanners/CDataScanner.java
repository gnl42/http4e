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
package me.glindholm.plugin.http4e2.editor.xml.scanners;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import me.glindholm.plugin.http4e2.editor.xml.ColorManager;
import me.glindholm.plugin.http4e2.editor.xml.IXMLColorConstants;
import me.glindholm.plugin.http4e2.editor.xml.rules.CDataRule;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class CDataScanner extends RuleBasedScanner {

    public IToken ESCAPED_CHAR;
    public IToken CDATA;

    public CDataScanner(final ColorManager colorManager) {

        CDATA = new Token(new TextAttribute(colorManager.getColor(IXMLColorConstants.CDATA)));

        final IRule[] rules = new IRule[2];

        // Add rule to pick up start of c section
        rules[0] = new CDataRule(CDATA, true);
        // Add a rule to pick up end of CDATA sections
        rules[1] = new CDataRule(CDATA, false);

        setRules(rules);
    }

    @Override
    public IToken nextToken() {
        return super.nextToken();
    }
}