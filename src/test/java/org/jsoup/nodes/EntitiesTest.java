package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import static org.jsoup.nodes.Document.OutputSettings;
import static org.jsoup.nodes.Entities.EscapeMode.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntitiesTest {
    @Test public void escape() {
        // escape is maximal (as in the escapes cover use in both text and attributes; vs Element.html() which checks if attribute or text and minimises escapes
        String text = "Hello &<> Å å π 新 there ¾ © » ' \"";
        String escapedAscii = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(base));
        String escapedAsciiFull = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(extended));
        String escapedAsciiXhtml = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(xhtml));
        String escapedUtfFull = Entities.escape(text, new OutputSettings().charset("UTF-8").escapeMode(extended));
        String escapedUtfMin = Entities.escape(text, new OutputSettings().charset("UTF-8").escapeMode(xhtml));

        assertEquals("Hello &amp;&lt;&gt; &Aring; &aring; &#x3c0; &#x65b0; there &frac34; &copy; &raquo; &apos; &quot;", escapedAscii);
        assertEquals("Hello &amp;&lt;&gt; &angst; &aring; &pi; &#x65b0; there &frac34; &copy; &raquo; &apos; &quot;", escapedAsciiFull);
        assertEquals("Hello &amp;&lt;&gt; &#xc5; &#xe5; &#x3c0; &#x65b0; there &#xbe; &#xa9; &#xbb; &#x27; &quot;", escapedAsciiXhtml);
        assertEquals("Hello &amp;&lt;&gt; Å å π 新 there ¾ © » &apos; &quot;", escapedUtfFull);
        assertEquals("Hello &amp;&lt;&gt; Å å π 新 there ¾ © » &#x27; &quot;", escapedUtfMin);
        // odd that it's defined as aring in base but angst in full

        // round trip
        assertEquals(text, Entities.unescape(escapedAscii));
        assertEquals(text, Entities.unescape(escapedAsciiFull));
        assertEquals(text, Entities.unescape(escapedAsciiXhtml));
        assertEquals(text, Entities.unescape(escapedUtfFull));
        assertEquals(text, Entities.unescape(escapedUtfMin));
    }

    @Test public void escapeDefaults() {
        String text = "Hello &<> Å å π 新 there ¾ © » ' \"";
        String escaped = Entities.escape(text);
        assertEquals("Hello &amp;&lt;&gt; Å å π 新 there ¾ © » &apos; &quot;", escaped);
    }

    @Test public void escapedSupplementary() {
        String text = "\uD835\uDD59";
        String escapedAscii = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(base));
        assertEquals("&#x1d559;", escapedAscii);
        String escapedAsciiFull = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(extended));
        assertEquals("&hopf;", escapedAsciiFull);
        String escapedUtf= Entities.escape(text, new OutputSettings().charset("UTF-8").escapeMode(extended));
        assertEquals(text, escapedUtf);
    }

    @Test public void unescapeMultiChars() {
        String text = "&NestedGreaterGreater; &nGg; &nGt; &nGtv; &Gt; &gg;"; // gg is not combo, but 8811 could conflict with NestedGreaterGreater or others
        String un = "≫ ⋙̸ ≫⃒ ≫̸ ≫ ≫";
        assertEquals(un, Entities.unescape(text));
        String escaped = Entities.escape(un, new OutputSettings().charset("ascii").escapeMode(extended));
        assertEquals("&Gt; &Gg;&#x338; &Gt;&#x20d2; &Gt;&#x338; &Gt; &Gt;", escaped);
        assertEquals(un, Entities.unescape(escaped));
    }

    @Test public void xhtml() {
        assertEquals(38, xhtml.codepointForName("amp"));
        assertEquals(62, xhtml.codepointForName("gt"));
        assertEquals(60, xhtml.codepointForName("lt"));
        assertEquals(34, xhtml.codepointForName("quot"));

        assertEquals("amp", xhtml.nameForCodepoint(38));
        assertEquals("gt", xhtml.nameForCodepoint(62));
        assertEquals("lt", xhtml.nameForCodepoint(60));
        assertEquals("quot", xhtml.nameForCodepoint(34));
    }

    @Test public void getByName() {
        assertEquals("≫⃒", Entities.getByName("nGt"));
        assertEquals("fj", Entities.getByName("fjlig"));
        assertEquals("≫", Entities.getByName("gg"));
        assertEquals("©", Entities.getByName("copy"));
    }

    @Test public void escapeSupplementaryCharacter() {
        String text = new String(Character.toChars(135361));
        String escapedAscii = Entities.escape(text, new OutputSettings().charset("ascii").escapeMode(base));
        assertEquals("&#x210c1;", escapedAscii);
        String escapedUtf = Entities.escape(text, new OutputSettings().charset("UTF-8").escapeMode(base));
        assertEquals(text, escapedUtf);
    }

    @Test public void notMissingMultis() {
        String text = "&nparsl;";
        String un = "\u2AFD\u20E5";
        assertEquals(un, Entities.unescape(text));
    }

    @Test public void notMissingSupplementals() {
        String text = "&npolint; &qfr;";
        String un = "⨔ \uD835\uDD2E"; // 𝔮
        assertEquals(un, Entities.unescape(text));
    }

    @Test public void unescape() {
        String text = "Hello &AElig; &amp;&LT&gt; &reg &angst; &angst &#960; &#960 &#x65B0; there &! &frac34; &copy; &COPY;";
        assertEquals("Hello Æ &<> ® Å &angst π π 新 there &! ¾ © ©", Entities.unescape(text));

        assertEquals("&0987654321; &unknown", Entities.unescape("&0987654321; &unknown"));
    }

    @Test public void strictUnescape() { // for attributes, enforce strict unescaping (must look like &#xxx; , not just &#xxx)
        String text = "Hello &amp= &amp;";
        assertEquals("Hello &amp= &", Entities.unescape(text, true));
        assertEquals("Hello &= &", Entities.unescape(text));
        assertEquals("Hello &= &", Entities.unescape(text, false));
    }

    @Test public void prefixMatch() {
        // https://github.com/jhy/jsoup/issues/2207
        // example from https://html.spec.whatwg.org/multipage/parsing.html#character-reference-state
        String text = "I'm &notit; I tell you. I'm &notin; I tell you.";
        assertEquals("I'm ¬it; I tell you. I'm ∉ I tell you.", Entities.unescape(text, false));
        assertEquals("I'm &notit; I tell you. I'm ∉ I tell you.", Entities.unescape(text, true)); // not for attributes
    }

    @Test public void caseSensitive() {
        String unescaped = "Ü ü & &";
        assertEquals("&Uuml; &uuml; &amp; &amp;",
                Entities.escape(unescaped, new OutputSettings().charset("ascii").escapeMode(extended)));

        String escaped = "&Uuml; &uuml; &amp; &AMP";
        assertEquals("Ü ü & &", Entities.unescape(escaped));
    }

    @Test public void quoteReplacements() {
        String escaped = "&#92; &#36;";
        String unescaped = "\\ $";

        assertEquals(unescaped, Entities.unescape(escaped));
    }

    @Test public void letterDigitEntities() {
        String html = "<p>&sup1;&sup2;&sup3;&frac14;&frac12;&frac34;</p>";
        Document doc = Jsoup.parse(html);
        doc.outputSettings().charset("ascii");
        Element p = doc.select("p").first();
        assertEquals("&sup1;&sup2;&sup3;&frac14;&frac12;&frac34;", p.html());
        assertEquals("¹²³¼½¾", p.text());
        doc.outputSettings().charset("UTF-8");
        assertEquals("¹²³¼½¾", p.html());
    }

    @Test public void noSpuriousDecodes() {
        String string = "http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2";
        assertEquals(string, Entities.unescape(string));
    }

    @Test public void alwaysEscapeLtAndGtInAttributeValues() {
        // https://github.com/jhy/jsoup/issues/2337

        String docHtml = "<a title='<p>One</p>'>One</a>";
        Document doc = Jsoup.parse(docHtml);
        Element element = doc.select("a").first();

        doc.outputSettings().escapeMode(base);
        assertEquals("<a title=\"&lt;p&gt;One&lt;/p&gt;\">One</a>", element.outerHtml());

        doc.outputSettings().escapeMode(xhtml);
        assertEquals("<a title=\"&lt;p&gt;One&lt;/p&gt;\">One</a>", element.outerHtml());
    }

    @Test public void controlCharactersAreEscaped() {
        // https://github.com/jhy/jsoup/issues/1556
        // escape in HTML for legibility; remove from xml
        String input = "<a foo=\"&#x1b;esc&#x7;bell\">Text &#x1b; &#x7;</a>";
        Document doc = Jsoup.parse(input);
        assertEquals(input, doc.body().html());

        Document xml = Jsoup.parse(input, "", Parser.xmlParser());
        assertEquals("<a foo=\"escbell\">Text  </a>", xml.html());
    }
    
    @Test public void escapeByClonedOutputSettings() {
        OutputSettings outputSettings = new OutputSettings();
        String text = "Hello &<> Å å π 新 there ¾ © »";
        OutputSettings clone1 = outputSettings.clone();
        OutputSettings clone2 = outputSettings.clone();

        String escaped1 = assertDoesNotThrow(() -> Entities.escape(text, clone1));
        String escaped2 = assertDoesNotThrow(() -> Entities.escape(text, clone2));
        assertEquals(escaped1, escaped2);
    }

    @Test void parseHtmlEncodedEmojiMultipoint() {
        String emoji = Parser.unescapeEntities("&#55357;&#56495;", false); // 💯
        assertEquals("\uD83D\uDCAF", emoji);
    }

    @Test void parseHtmlEncodedEmoji() {
        String emoji = Parser.unescapeEntities("&#128175;", false); // 💯
        assertEquals("\uD83D\uDCAF", emoji);
    }
}
