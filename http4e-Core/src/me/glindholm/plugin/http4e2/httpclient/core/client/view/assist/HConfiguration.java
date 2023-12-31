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

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultInformationControl.IInformationPresenter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import me.glindholm.plugin.http4e2.httpclient.core.misc.LazyObjects;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class HConfiguration extends SourceViewerConfiguration {

    private ITextDoubleClickStrategy doubleClickStrategy;
    private RuleBasedScanner defaultScanner;
    private RuleBasedScanner valueScanner;
    private RuleBasedScanner commentScanner;
    private ContentAssistant assistant;
    private final int processorType;

    public HConfiguration(final int processorType) {
        this.processorType = processorType;
    }

    @Override
    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE, HPartitionScanner.COMMENT, HPartitionScanner.PROPERTY_VALUE,
                /* HPartitionScanner.PROPERTY_KEY, */ };
    }

    /**
     * @Override
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
        if (doubleClickStrategy == null) {
            doubleClickStrategy = new HDoubleClickStrategy();
        }
        return doubleClickStrategy;
    }

    /**
     * @Override
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        final PresentationReconciler rr = new PresentationReconciler();
        DefaultDamagerRepairer dr;

        dr = new DefaultDamagerRepairer(getCommentScanner());
        rr.setDamager(dr, HPartitionScanner.COMMENT);
        rr.setRepairer(dr, HPartitionScanner.COMMENT);

        dr = new DefaultDamagerRepairer(getValueScanner());
        rr.setDamager(dr, HPartitionScanner.PROPERTY_VALUE);
        rr.setRepairer(dr, HPartitionScanner.PROPERTY_VALUE);

        dr = new DefaultDamagerRepairer(getDefaultScanner());
        rr.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        rr.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

//      NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(ResourceUtils.getColor(Styles.COMMENT)));
//      rr.setDamager(ndr, HPartitionScanner.COMMENT);
//      rr.setRepairer(ndr, HPartitionScanner.COMMENT);

        return rr;
    }

    /**
     * @Override
     */
    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        if (assistant != null) {
            return assistant;
        }
        assistant = new ContentAssistant();
        final IContentAssistProcessor processor = new HContentAssistProcessor(processorType, LazyObjects.getHeaderTracker());
        // Set this processor for each supported content type
        assistant.setContentAssistProcessor(processor, HPartitionScanner.PROPERTY_VALUE);
        assistant.setContentAssistProcessor(processor, HPartitionScanner.PROPERTIES_PARTITIONING);
        assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        // Set factory for information controller
        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(200);
        assistant.setProposalSelectorBackground(ResourceUtils.getColor(Styles.CONTENT_ASSIST));

        return assistant;
    }

    /**
     * @Override
     */
    @Override
    public IInformationControlCreator getInformationControlCreator(final ISourceViewer sourceViewer) {
        return parent -> new DefaultInformationControl(parent, getInformationPresenter());
    }

    /**
     * @Override
     */
    @Override
    public IAnnotationHover getAnnotationHover(final ISourceViewer sourceViewer) {
        return new MyAnnotationHover();
    }

    /**
     * @Override
     */
    @Override
    public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
        return new MyTextHover();
    }

    // ===============================================
    private RuleBasedScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = new HCommentScanner();
            commentScanner.setDefaultReturnToken(new Token(new TextAttribute(ResourceUtils.getColor(Styles.PROC_INSTR))));
        }
        return commentScanner;
    }

    private RuleBasedScanner getValueScanner() {
        if (valueScanner == null) {
            valueScanner = new HValueScanner();
            valueScanner.setDefaultReturnToken(new Token(new TextAttribute(ResourceUtils.getColor(Styles.WHITE_RGB_TEXT))));
        }
        return valueScanner;
    }

    private RuleBasedScanner getDefaultScanner() {
        if (defaultScanner == null) {
            defaultScanner = new HDefaultScanner();
            defaultScanner.setDefaultReturnToken(new Token(new TextAttribute(ResourceUtils.getColor(Styles.WHITE_RGB_TEXT))));
        }
        return defaultScanner;
    }

    private static IInformationPresenter getInformationPresenter() {
        final String _3_2_PRESENTER = "org.eclipse.jface.internal.text.link.contentassist.HTMLTextPresenter";
        final String _3_3_PRESENTER = "org.eclipse.jface.internal.text.html.HTMLTextPresenter";
        try {
            try {
                return (IInformationPresenter) Class.forName(_3_2_PRESENTER).newInstance();

            } catch (final java.lang.NoClassDefFoundError e) {
                return (IInformationPresenter) Class.forName(_3_3_PRESENTER).newInstance();
            }
        } catch (final Exception e) {
//         ExceptionHandler.handle(e);
        }
        return null;
    }

// // The presenter instance for the information window
//   private static final DefaultInformationControl.IInformationPresenter presenter =
//      new DefaultInformationControl.IInformationPresenter() {
//      public String updatePresentation(
//         Display display,
//         String infoText,
//         TextPresentation presentation,
//         int maxWidth,
//         int maxHeight) {
//         System.out.println("IInformationPresenter(), infoText=" + infoText);
//         System.out.println("IInformationPresenter(), presentation=" + presentation);
//         System.out.println("IInformationPresenter(), maxWidth=" + maxWidth);
//         System.out.println("IInformationPresenter(), maxHeight=" + maxHeight);
//         int start = -1;
//         // Loop over all characters of information text
//         for (int i = 0; i < infoText.length(); i++) {
//            switch (infoText.charAt(i)) {
//               case '<' :
//                  // Remember start of tag
//                  start = i;
//                  break;
//               case '>' :
//                  if (start >= 0) {
//                     // We have found a tag and create a new style range
//                     StyleRange range =
//                        new StyleRange(
//                           start,
//                           i - start + 1,
//                           null,
//                           null,
//                           SWT.BOLD);
//                     // Add this style range to the presentation
//                     presentation.addStyleRange(range);
//                     // Reset tag start indicator
//                     start = -1;
//                  }
//                  break;
//            }
//         }
//         // Return the information text
//         return infoText;
//      }
//   };

}