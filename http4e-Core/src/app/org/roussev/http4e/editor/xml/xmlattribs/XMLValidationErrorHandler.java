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
package org.roussev.http4e.editor.xml.xmlattribs;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class XMLValidationErrorHandler extends DefaultHandler {

    private final List<XMLValidationError> errorList = new ArrayList<>();
    private Locator locator;

    public XMLValidationErrorHandler() {
    }

    @Override
    public void error(final SAXParseException e) throws SAXException {

        handleError(e, false);

    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
    }

    private void handleError(final SAXParseException e, final boolean isFatal) {
        final XMLValidationError validationError = nextError(e, isFatal);
        errorList.add(validationError);
        // System.err.println(validationError.toString());

    }

    protected XMLValidationError nextError(final SAXParseException e, final boolean isFatal) {
        final String errorMessage = e.getMessage();

        final int lineNumber = locator.getLineNumber();
        final int columnNumber = locator.getColumnNumber();

        log(this, (isFatal ? "FATAL " : "Non-Fatal") + "Error on line " + lineNumber + ", column " + columnNumber + ": " + errorMessage);

        final XMLValidationError validationError = new XMLValidationError();
        validationError.setLineNumber(lineNumber);
        validationError.setColumnNumber(columnNumber);
        validationError.setErrorMessage(errorMessage);
        return validationError;
    }

    private void log(final XMLValidationErrorHandler handler, final String string) {
    }

    @Override
    public void fatalError(final SAXParseException e) throws SAXException {
        handleError(e, true);
    }

    public List<XMLValidationError> getErrorList() {
        return errorList;
    }

}
