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
package me.glindholm.plugin.http4e2.editor.xml.format;

import org.eclipse.jface.text.formatter.IFormattingStrategy;

/**
 * 
 * @author Phil Zoio
 */
public class DefaultFormattingStrategy implements IFormattingStrategy {

    protected static final String lineSeparator = System.lineSeparator();

    public DefaultFormattingStrategy() {
    }

    @Override
    public void formatterStarts(final String initialIndentation) {
    }

    @Override
    public String format(final String content, final boolean isLineStart, final String indentation, final int[] positions) {
        return "";
    }

    @Override
    public void formatterStops() {
    }

}
