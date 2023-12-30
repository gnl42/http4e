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
package me.glindholm.plugin.http4e2.httpclient.core.client.view.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class HContentAssistProcessor implements IContentAssistProcessor {

    private final Tracker keyTracker;
    private final int processorType;
    public final static int HEADER_PROCESSOR = 0;
    public final static int PARAM_PROCESSOR = 1;

    public HContentAssistProcessor(final int processorType, final Tracker keyTracker) {
        this.keyTracker = keyTracker;
        this.processorType = processorType;
    }

    private boolean isHeaderProcessor() {
        return processorType == HContentAssistProcessor.HEADER_PROCESSOR;
    }

    //////////////////////////////////
    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {

        final IDocument doc = viewer.getDocument();
        // Point selectedRange = viewer.getSelectedRange();
        // if (selectedRange.y > 0) { do something with selection }
//    ITypedRegion partitionType = doc.getPartition(offset);
        final List<ICompletionProposal> proposalsList = new ArrayList<>();
        try {
            final int offsetDelim = DocumentUtils.getDelimOffset(doc, offset);
            final String lineText = DocumentUtils.getLineText(doc, offset);
//         DocumentUtils.dumpOffset(offset, offsetDelim, doc);

            if (DocumentUtils.isInValueType(offset, offsetDelim) /* partitionType.getType().equals(HPartitionScanner.PROPERTY_VALUE) || */) {
                final String delim_to_cursor = DocumentUtils.delim_to_cursor(doc, offsetDelim + 1, offset);
                computeValueProposals(doc, delim_to_cursor, lineText, offset, proposalsList);
            } else {
                final String nl_to_cursor = DocumentUtils.nl_to_cursor(doc, offset);
                computeKeyProposals(doc, nl_to_cursor, lineText, offset, proposalsList);
            }

        } catch (final BadLocationException e) {
            ExceptionHandler.warn("computeCompletionProposals: " + e);
        }

        final ICompletionProposal[] proposals = new ICompletionProposal[proposalsList.size()];
        proposalsList.toArray(proposals);
        return proposals;
    }

    /**
     * qualifier, partially entered text. offset, current cursor position. propList, result list.
     */
    private void computeKeyProposals(final IDocument doc, final String qualifier, final String lineText, final int offset,
            final List<ICompletionProposal> proposalsList) {
        AssistUtils.doTemplateProposals(doc, offset, qualifier, proposalsList, isHeaderProcessor());

        final String keyWord = AssistUtils.getKeyWordToOffset(doc, offset);
        AssistUtils.doTrackProposals(keyTracker, keyWord, offset, proposalsList);

        if (isHeaderProcessor()) {
            AssistUtils.doHttpHeadersProposals(offset, qualifier, proposalsList);
        }
    }

    //////////////////////
    private void computeValueProposals(final IDocument doc, final String qualifier, final String lineText, final int offset,
            final List<ICompletionProposal> propList) {
        final int qlen = qualifier.length();

        final String key = DocumentUtils.getKeyFromLine(lineText);
        final String val = DocumentUtils.getValueFromLine(lineText);
        final int replacementOffset = offset - qlen;

        final Tracker childTracker = keyTracker.getChildTracker(key);
        int offsetDelim;
        try {
            offsetDelim = DocumentUtils.getDelimOffset(doc, offset);
            final String keyWord = DocumentUtils.delim_to_cursor(doc, offsetDelim + 1, offset);
            AssistUtils.doTrackProposals(childTracker, keyWord, offset, propList);
        } catch (final BadLocationException e) {
            ExceptionHandler.handle(e);
        }

        if (isHeaderProcessor()) {
            AssistUtils.doHttpH_ValuesProposals(key, offset, qualifier, propList);
        }
    }

    @Override
    public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int documentOffset) {
        // viewer.getSelectedRange();
        // if (selectedRange.y > 0) Text is selected. Create a context information array.
        // ContextInformation[] contextInfos = new
        // ContextInformation[STYLELABELS.length];
        // // Create one context information item for each style
        // for (int i = 0; i < STYLELABELS.length; i++)
        // contextInfos[i] = new ContextInformation("<" + STYLETAGS[i] + ">",
        // STYLELABELS[i] + " Style");
        // return contextInfos;
        // }
        return new ContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[0];
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new ContextInformationValidator(this);
    }

    // private final static String[] STYLETAGS = new String[] { "b2", "i2",
    // "code2", "strong2" };
    // private final static String[] STYLELABELS = new String[] { "bold 1",
    // "italic 1", "code 1", "strong 1" };
    //
    // /**
    // * @param selectedText -
    // * currently selected text in document
    // * @param selectedRange -
    // * the selection range in document
    // * @param propList -
    // * the result list
    // */
    // private void computeStyleProposals( String selectedText, Point
    // selectedRange, List propList){
    // for (int i = 0; i < STYLETAGS.length; i++) {
    // String tag = STYLETAGS[i];
    // // Compute replacement text
    // String replacement = "<m" + tag + ">" + selectedText + "</m" + tag + ">";
    // // Derive cursor position
    // int cursor = tag.length() + 2;
    // IContextInformation contextInfo = new ContextInformation(null,
    // STYLELABELS[i] + " Style");
    // CompletionProposal proposal = new CompletionProposal(replacement,
    // selectedRange.x, selectedRange.y, cursor, null, STYLELABELS[i],
    // contextInfo, replacement);
    // propList.add(proposal);
    // }
    // }
    //
    //
    // private String lastIndent( IDocument doc, int offset){
    // try {
    // int start = offset - 1;
    // while (start >= 0 && doc.getChar(start) != '\n') {
    // start--;
    // }
    // int end = start;
    // while (end < offset && Character.isSpaceChar(doc.getChar(end))) {
    // end++;
    // }
    // return doc.get(start + 1, end - start - 1);
    // } catch (BadLocationException e) {
    // System.err.println("lastIndent: " + e);
    // }
    // return "";
    // }
    //
    //
    // private String getQualifier( IDocument doc, int offset){
    // // Use string buffer to collect characters
    // StringBuilder buf = new StringBuilder();
    // while (true) {
    // try {
    // // Read character backwards
    // char c = doc.getChar(--offset);
    // // This was not the start of a tag
    // if (c == '>' || Character.isWhitespace(c)) {
    // // no open tag was found.
    // return "";
    // }
    // // Collect character
    // buf.append(c);
    // // Start of tag. Return qualifier
    // if (c == '<')
    // return buf.reverse().toString();
    // } catch (BadLocationException e) {
    // // Document start reached, no tag found
    // return "";
    // }
    // }
    // }
    // // Proposal part before cursor
    // private final static String[] STRUCTTAGS1 = new String[] { "Connection",
    // "<A SRC=\"", "<TABLE>", "<TR>", "<TD>" };
    // // Proposal part after cursor
    // private final static String[] STRUCTTAGS2 = new String[] { "", "\"></A>",
    // "</TABLE>", "</TR>", "</TD>" };

}
