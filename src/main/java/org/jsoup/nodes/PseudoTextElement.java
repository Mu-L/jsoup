package org.jsoup.nodes;

import org.jsoup.internal.QuietAppendable;
import org.jsoup.parser.Tag;

/**
 * Represents a {@link TextNode} as an {@link Element}, to enable text nodes to be selected with
 * the {@link org.jsoup.select.Selector} {@code :matchText} syntax.
 * @deprecated use {@link Element#selectNodes(String, Class)} instead, with selector of <code>::textnode</code> and class <code>TextNode</code>.
 */
@Deprecated
public class PseudoTextElement extends Element {

    public PseudoTextElement(Tag tag, String baseUri, Attributes attributes) {
        super(tag, baseUri, attributes);
    }

    @Override
    void outerHtmlHead(QuietAppendable accum, Document.OutputSettings out) {
    }

    @Override
    void outerHtmlTail(QuietAppendable accum, Document.OutputSettings out) {
    }
}
