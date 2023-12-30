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
package me.glindholm.plugin.http4e2.editor.xml.scanners;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import me.glindholm.plugin.http4e2.editor.xml.rules.NonMatchingRule;
import me.glindholm.plugin.http4e2.editor.xml.rules.StartTagRule;
import me.glindholm.plugin.http4e2.editor.xml.rules.XMLTextPredicateRule;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class XMLPartitionScanner extends RuleBasedPartitionScanner {

    public final static String XML_DEFAULT = "__xml_default";
    public final static String XML_COMMENT = "__xml_comment";
    public final static String XML_PI = "__xml_pi";
    public final static String XML_DOCTYPE = "__xml_doctype";
    public final static String XML_CDATA = "__xml_cdata";
    public final static String XML_START_TAG = "__xml_start_tag";
    public final static String XML_END_TAG = "__xml_end_tag";
    public final static String XML_TEXT = "__xml_text";

    public XMLPartitionScanner() {

        final IToken xmlComment = new Token(XML_COMMENT);
        final IToken xmlPI = new Token(XML_PI);
        final IToken startTag = new Token(XML_START_TAG);
        final IToken endTag = new Token(XML_END_TAG);
        final IToken docType = new Token(XML_DOCTYPE);
        final IToken text = new Token(XML_TEXT);

        final IPredicateRule[] rules = new IPredicateRule[7];

        rules[0] = new NonMatchingRule();
        rules[1] = new MultiLineRule("<!--", "-->", xmlComment);
        rules[2] = new MultiLineRule("<?", "?>", xmlPI);
        rules[3] = new MultiLineRule("</", ">", endTag);
        rules[4] = new StartTagRule(startTag);
        rules[5] = new MultiLineRule("<!DOCTYPE", ">", docType);
        rules[6] = new XMLTextPredicateRule(text);

        setPredicateRules(rules);
    }
}