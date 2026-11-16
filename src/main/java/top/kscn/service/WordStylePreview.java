package top.kscn.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import static top.kscn.service.FilePreviewService.*;
import static top.kscn.service.OfficePreviewCss.*;

final class WordStylePreview {
    private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private WordStylePreview() {}
    private static class Context {
        XWPFDocument document; String defaultStyle = "Normal"; int characters, blocks, pictures; long imageBytes; boolean truncated;
        final Map<String, Integer> numbers = new HashMap<>();
        String text(String value) {
            if (value == null) return "";
            int length = Math.min(value.length(), Math.min(4096, Math.max(0, 500000 - characters)));
            truncated |= length < value.length(); characters += length; return value.substring(0, length);
        }
        boolean block() { if (++blocks <= 2000) return true; truncated = true; return false; }
        String image(String extension, byte[] data) {
            extension = extension.toLowerCase(Locale.ROOT);
            if (++pictures > 30 || (imageBytes += data.length) > 8 * 1024 * 1024 || data.length > 3 * 1024 * 1024 || !Set.of("png", "jpg", "jpeg", "gif", "bmp").contains(extension)) { truncated = true; return null; }
            return "data:image/" + (extension.equals("jpg") ? "jpeg" : extension) + ";base64," + Base64.getEncoder().encodeToString(data);
        }
    }
    static OfficeView render(FilePropertiesService.PreviewSource source) throws IOException {
        Context context = new Context(); List<Block> blocks; Map<String, String> page = page(11906, 16838, 1440, 1440, 1440, 1440);
        if (FilePreviewService.extension(source.name()).equals("docx")) {
            try (var document = new XWPFDocument(new ByteArrayInputStream(source.bytes()))) {
                context.document = document;
                if (document.getStyles() != null) for (XWPFStyle style : document.getStyles().getStyles()) {
                    Node node = style.getCTStyle().getDomNode();
                    if ("paragraph".equals(attribute(node, "type")) && Set.of("1", "true", "on").contains(attribute(node, "default"))) context.defaultStyle = style.getStyleId();
                }
                Node section = child(document.getDocument().getBody().getDomNode(), "sectPr");
                Node size = child(section, "pgSz"), margins = child(section, "pgMar");
                page = page(number(size, "w", 11906), number(size, "h", 16838), number(margins, "top", 1440), number(margins, "right", 1440), number(margins, "bottom", 1440), number(margins, "left", 1440));
                blocks = body(document.getBodyElements(), context, 0);
            }
        } else {
            try (var document = new HWPFDocument(new ByteArrayInputStream(source.bytes()))) {
                Range range = document.getRange(); blocks = new ArrayList<>();
                if (range.numSections() > 0) {
                    Section section = range.getSection(0);
                    page = page(section.getPageWidth(), section.getPageHeight(), section.getMarginTop(), section.getMarginRight(), section.getMarginBottom(), section.getMarginLeft());
                }
                for (int index = 0; index < range.numParagraphs() && context.blocks < 2000; index++) {
                    Paragraph paragraph = range.getParagraph(index);
                    if (paragraph.isInTable()) {
                        Table table = range.getTable(paragraph); blocks.add(legacyTable(table, document, context));
                        while (index + 1 < range.numParagraphs() && range.getParagraph(index + 1).getStartOffset() < table.getEndOffset()) index++;
                    } else blocks.add(legacyParagraph(paragraph, document, context));
                }
                context.truncated |= context.blocks >= 2000;
            }
        }
        return new OfficeView("word", blocks, List.of(), List.of(), context.truncated,
                "已保留字体、段落、表格和正文图片；复杂浮动对象、页眉页脚及精确分页可能与原文件不同。", page);
    }
    private static Map<String, String> page(double width, double height, double top, double right, double bottom, double left) {
        Map<String, String> css = new LinkedHashMap<>(); css.put("width", pt(width / 20)); css.put("minHeight", pt(height / 20));
        css.put("padding", pt(top / 20) + " " + pt(right / 20) + " " + pt(bottom / 20) + " " + pt(left / 20));
        css.put("fontFamily", "'Calibri','Arial',sans-serif"); css.put("fontSize", "11pt"); css.put("lineHeight", "1.15"); return css;
    }
    private static List<Block> body(List<IBodyElement> elements, Context context, int depth) {
        List<Block> result = new ArrayList<>();
        if (depth > 8) { context.truncated = true; return result; }
        for (IBodyElement element : elements) {
            if (!context.block()) break;
            if (element instanceof XWPFParagraph paragraph) result.add(paragraph(paragraph, context));
            else if (element instanceof XWPFTable table) result.add(table(table, context, depth));
        }
        return result;
    }
    private static List<Node> styleChain(Context context, String id) {
        LinkedList<Node> result = new LinkedList<>(); Set<String> seen = new HashSet<>();
        XWPFStyles styles = context.document.getStyles();
        while (styles != null && id != null && !id.isBlank() && seen.add(id) && seen.size() < 20) {
            XWPFStyle style = styles.getStyle(id); if (style == null) break;
            Node node = style.getCTStyle().getDomNode(); result.addFirst(node); id = value(child(node, "basedOn"));
        }
        return result;
    }
    private static Block paragraph(XWPFParagraph paragraph, Context context) {
        Map<String, String> css = new LinkedHashMap<>(), font = new LinkedHashMap<>();
        css.put("margin", "0 0 8pt");
        XWPFStyles styles = context.document.getStyles();
        if (styles != null) {
            Node defaults = child(styles.getCtStyles().getDomNode(), "docDefaults");
            runStyle(child(child(defaults, "rPrDefault"), "rPr"), font);
            paragraphStyle(child(child(defaults, "pPrDefault"), "pPr"), css);
            for (Node style : styleChain(context, context.defaultStyle)) { paragraphStyle(child(style, "pPr"), css); runStyle(child(style, "rPr"), font); }
        }
        for (Node style : styleChain(context, paragraph.getStyle())) { paragraphStyle(child(style, "pPr"), css); runStyle(child(style, "rPr"), font); }
        Node pPr = child(paragraph.getCTP().getDomNode(), "pPr"); paragraphStyle(pPr, css); css.putAll(font);
        List<Inline> runs = new ArrayList<>();
        if (paragraph.getNumID() != null) {
            String key = paragraph.getNumID() + ":" + paragraph.getNumIlvl(); int number = context.numbers.merge(key, 1, Integer::sum);
            String prefix = "bullet".equals(paragraph.getNumFmt()) ? "• " : number + ". ";
            runs.add(new Inline(prefix, null, Map.of()));
        }
        StringBuilder plain = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            Map<String, String> runCss = new LinkedHashMap<>(); Node rPr = child(run.getCTR().getDomNode(), "rPr");
            for (Node style : styleChain(context, value(child(rPr, "rStyle")))) runStyle(child(style, "rPr"), runCss);
            runStyle(rPr, runCss);
            String text = context.text(run.text()); plain.append(text);
            if (!text.isEmpty()) runs.add(new Inline(text, null, runCss));
            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                XWPFPictureData data = picture.getPictureData(); if (data == null) continue;
                String image = context.image(data.suggestFileExtension(), data.getData());
                if (image != null) {
                    Map<String, String> imageCss = new LinkedHashMap<>(); Node extent = descendant(picture.getCTPicture().getDomNode(), "ext");
                    double width = number(extent, "cx", 0), height = number(extent, "cy", 0);
                    if (width > 0) imageCss.put("width", pt(width / 12700));
                    if (height > 0) imageCss.put("height", pt(height / 12700));
                    runs.add(new Inline("", image, imageCss));
                }
            }
        }
        return new Block("paragraph", plain.toString(), null, null, css, runs, List.of());
    }

    private static class CellBuilder {
        List<Block> blocks; Map<String, String> style; int colspan = 1, rowspan = 1; boolean hidden;
        WordCell build() { return new WordCell(blocks, colspan, rowspan, hidden, style); }
    }
    private static Block table(XWPFTable table, Context context, int depth) {
        List<List<CellBuilder>> builders = new ArrayList<>(); Map<Integer, CellBuilder> vertical = new HashMap<>();
        Node properties = child(table.getCTTbl().getDomNode(), "tblPr"); Map<String, String> css = new LinkedHashMap<>();
        css.put("width", "100%"); css.put("borderCollapse", "collapse");
        width(child(properties, "tblW"), css);
        String alignment = value(child(properties, "jc"));
        if ("center".equals(alignment)) { css.put("marginLeft", "auto"); css.put("marginRight", "auto"); }
        if ("right".equals(alignment)) css.put("marginLeft", "auto");
        for (XWPFTableRow row : table.getRows()) {
            if (builders.size() >= 200) { context.truncated = true; break; }
            List<CellBuilder> cells = new ArrayList<>(); int column = 0;
            for (XWPFTableCell cell : row.getTableCells()) {
                if (cells.size() >= 50) { context.truncated = true; break; }
                CellBuilder current = new CellBuilder(); Node tcPr = child(cell.getCTTc().getDomNode(), "tcPr");
                current.colspan = (int) Math.max(1, Math.min(50, number(child(tcPr, "gridSpan"), "val", 1)));
                Node merge = child(tcPr, "vMerge"); boolean continuation = merge != null && !"restart".equals(value(merge));
                if (continuation && vertical.containsKey(column)) { current.hidden = true; vertical.get(column).rowspan++; current.blocks = List.of(); }
                else {
                    current.blocks = body(cell.getBodyElements(), context, depth + 1);
                    for (int offset = 0; offset < current.colspan; offset++) { if (merge == null) vertical.remove(column + offset); else vertical.put(column + offset, current); }
                }
                current.style = new LinkedHashMap<>(); current.style.put("padding", "3pt 5pt");
                Node borders = child(properties, "tblBorders");
                for (String side : List.of("top", "right", "bottom", "left")) {
                    boolean edge = switch (side) { case "top" -> builders.isEmpty(); case "bottom" -> builders.size() == table.getNumberOfRows() - 1; case "left" -> column == 0; default -> cells.size() == row.getTableCells().size() - 1; };
                    Node border = child(borders, edge ? side : side.equals("top") || side.equals("bottom") ? "insideH" : "insideV");
                    if (border != null) current.style.put("border" + Character.toUpperCase(side.charAt(0)) + side.substring(1), borderValue(border));
                }
                borderStyles(child(tcPr, "tcBorders"), current.style);
                put(current.style, "backgroundColor", color(attribute(child(tcPr, "shd"), "fill")));
                String verticalAlignment = value(child(tcPr, "vAlign")); current.style.put("verticalAlign", "center".equals(verticalAlignment) ? "middle" : "bottom".equals(verticalAlignment) ? "bottom" : "top");
                width(child(tcPr, "tcW"), current.style);
                Node margins = child(tcPr, "tcMar");
                for (String side : List.of("top", "right", "bottom", "left")) {
                    Node margin = child(margins, side); if (margin != null) current.style.put("padding" + Character.toUpperCase(side.charAt(0)) + side.substring(1), pt(number(margin, "w", 0) / 20));
                }
                Node height = child(child(row.getCtRow().getDomNode(), "trPr"), "trHeight");
                if (height != null) current.style.put("height", pt(number(height, "val", 0) / 20));
                cells.add(current); column += current.colspan;
            }
            builders.add(cells);
        }
        List<List<WordCell>> cells = builders.stream().map(row -> row.stream().map(CellBuilder::build).toList()).toList();
        List<List<String>> rows = cells.stream().map(row -> row.stream().map(cell -> cell.blocks().stream().map(block -> block.text() == null ? "" : block.text()).reduce("", (a, b) -> a + b)).toList()).toList();
        return new Block("table", null, rows, null, css, List.of(), cells);
    }
    private static void width(Node node, Map<String, String> css) {
        if (node == null) return; String type = attribute(node, "type"); double width = number(node, "w", 0);
        if (width > 0 && "dxa".equals(type)) css.put("width", pt(width / 20));
        else if (width > 0 && "pct".equals(type)) css.put("width", OfficePreviewCss.number(width / 50) + "%");
    }
    private static void paragraphStyle(Node pPr, Map<String, String> css) {
        if (pPr == null) return;
        String align = value(child(pPr, "jc"));
        if (Set.of("left", "right", "center", "both", "distribute", "start", "end").contains(align)) css.put("textAlign", Set.of("both", "distribute").contains(align) ? "justify" : align);
        Node spacing = child(pPr, "spacing"), indent = child(pPr, "ind");
        setTwips(spacing, "before", "marginTop", css); setTwips(spacing, "after", "marginBottom", css);
        if (!attribute(spacing, "line").isEmpty()) css.put("lineHeight", "auto".equals(attribute(spacing, "lineRule")) || attribute(spacing, "lineRule").isEmpty() ? OfficePreviewCss.number(number(spacing, "line", 240) / 240) : pt(number(spacing, "line", 240) / 20));
        setTwips(indent, "left", "marginLeft", css); setTwips(indent, "right", "marginRight", css);
        setTwips(indent, "firstLine", "textIndent", css);
        if (!attribute(indent, "hanging").isEmpty()) css.put("textIndent", pt(-number(indent, "hanging", 0) / 20));
        if (child(pPr, "pageBreakBefore") != null && enabled(child(pPr, "pageBreakBefore"))) css.put("breakBefore", "page");
    }
    private static void setTwips(Node node, String name, String property, Map<String, String> css) { if (!attribute(node, name).isEmpty()) css.put(property, pt(number(node, name, 0) / 20)); }
    private static void runStyle(Node rPr, Map<String, String> css) {
        if (rPr == null) return;
        Node fonts = child(rPr, "rFonts"); String family = attribute(fonts, "eastAsia"); if (family.isEmpty()) family = attribute(fonts, "ascii");
        if (!family.isEmpty()) css.put("fontFamily", font(family));
        Node size = child(rPr, "sz"); if (size != null) css.put("fontSize", pt(number(size, "val", 22) / 2));
        Node bold = child(rPr, "b"), italic = child(rPr, "i");
        if (bold != null) css.put("fontWeight", enabled(bold) ? "700" : "400");
        if (italic != null) css.put("fontStyle", enabled(italic) ? "italic" : "normal");
        put(css, "color", color(value(child(rPr, "color"))));
        put(css, "backgroundColor", color(attribute(child(rPr, "shd"), "fill")));
        String highlight = value(child(rPr, "highlight"));
        if (Set.of("yellow", "green", "cyan", "magenta", "blue", "red", "black", "white").contains(highlight)) css.put("backgroundColor", highlight);
        Node underline = child(rPr, "u"), strike = child(rPr, "strike");
        if (underline != null || strike != null) {
            String decoration = (underline != null && !"none".equals(value(underline)) ? "underline " : "") + (strike != null && enabled(strike) ? "line-through" : "");
            css.put("textDecoration", decoration.isBlank() ? "none" : decoration.strip());
        }
        String position = value(child(rPr, "vertAlign"));
        if (position.equals("superscript") || position.equals("subscript")) { css.put("verticalAlign", position.equals("superscript") ? "super" : "sub"); css.put("fontSize", "0.75em"); }
        if (child(rPr, "caps") != null) css.put("textTransform", enabled(child(rPr, "caps")) ? "uppercase" : "none");
        if (child(rPr, "smallCaps") != null) css.put("fontVariant", enabled(child(rPr, "smallCaps")) ? "small-caps" : "normal");
    }
    private static void borderStyles(Node borders, Map<String, String> css) {
        for (String side : List.of("top", "right", "bottom", "left")) {
            Node border = child(borders, side); if (border == null) border = child(borders, side.equals("top") || side.equals("bottom") ? "insideH" : "insideV");
            if (border == null) continue;
            css.put("border" + Character.toUpperCase(side.charAt(0)) + side.substring(1), borderValue(border));
        }
    }
    private static String borderValue(Node border) {
        String type = value(border), rgb = color(attribute(border, "color"));
        return Set.of("nil", "none").contains(type) ? "none" : pt(number(border, "sz", 4) / 8) + " " + (type.equals("double") ? "double" : type.equals("dotted") ? "dotted" : type.toLowerCase(Locale.ROOT).contains("dash") ? "dashed" : "solid") + " " + (rgb == null ? "#000000" : rgb);
    }
    private static Block legacyParagraph(Paragraph paragraph, HWPFDocument document, Context context) {
        context.block(); Map<String, String> css = new LinkedHashMap<>();
        css.put("textAlign", switch (paragraph.getJustification()) { case 1 -> "center"; case 2 -> "right"; case 3 -> "justify"; default -> "left"; });
        css.put("marginTop", pt(paragraph.getSpacingBefore() / 20.0)); css.put("marginBottom", pt(paragraph.getSpacingAfter() / 20.0));
        css.put("marginLeft", pt(paragraph.getIndentFromLeft() / 20.0)); css.put("marginRight", pt(paragraph.getIndentFromRight() / 20.0)); css.put("textIndent", pt(paragraph.getFirstLineIndent() / 20.0));
        List<Inline> runs = new ArrayList<>(); StringBuilder plain = new StringBuilder();
        for (int index = 0; index < paragraph.numCharacterRuns(); index++) {
            CharacterRun run = paragraph.getCharacterRun(index); Map<String, String> style = new LinkedHashMap<>();
            style.put("fontFamily", font(run.getFontName())); style.put("fontSize", pt(run.getFontSize() / 2.0));
            style.put("fontWeight", run.isBold() ? "700" : "400"); style.put("fontStyle", run.isItalic() ? "italic" : "normal");
            if (run.getUnderlineCode() != 0 || run.isStrikeThrough()) style.put("textDecoration", (run.getUnderlineCode() != 0 ? "underline " : "") + (run.isStrikeThrough() ? "line-through" : ""));
            int rgb = run.getIco24(); if (rgb >= 0 && rgb <= 0xffffff) style.put("color", String.format(Locale.ROOT, "#%02x%02x%02x", rgb & 255, rgb >> 8 & 255, rgb >> 16 & 255));
            if (document.getPicturesTable().hasPicture(run)) {
                Picture picture = document.getPicturesTable().extractPicture(run, false);
                if (picture != null) { String image = context.image(picture.suggestFileExtension(), picture.getContent()); if (image != null) runs.add(new Inline("", image, Map.of())); }
            }
            String text = context.text(run.text().replace("\r", "").replace("\u0007", "").replace("\u0001", "")); plain.append(text);
            if (!text.isEmpty()) runs.add(new Inline(text, null, style));
        }
        return new Block("paragraph", plain.toString(), null, null, css, runs, List.of());
    }
    private static Block legacyTable(Table table, HWPFDocument document, Context context) {
        List<List<WordCell>> cells = new ArrayList<>(); List<List<String>> texts = new ArrayList<>();
        for (int index = 0; index < Math.min(200, table.numRows()) && context.blocks < 2000; index++) {
            TableRow row = table.getRow(index); List<WordCell> line = new ArrayList<>(); List<String> textLine = new ArrayList<>();
            for (int column = 0; column < Math.min(50, row.numCells()); column++) {
                TableCell cell = row.getCell(column); List<Block> blocks = new ArrayList<>();
                for (int paragraph = 0; paragraph < cell.numParagraphs() && context.blocks < 2000; paragraph++) blocks.add(legacyParagraph(cell.getParagraph(paragraph), document, context));
                line.add(new WordCell(blocks, 1, 1, false, Map.of("border", "0.5pt solid #808080", "padding", "3pt 5pt")));
                textLine.add(blocks.stream().map(Block::text).reduce("", (a, b) -> a + b));
            }
            cells.add(line); texts.add(textLine); context.truncated |= row.numCells() > 50;
        }
        context.truncated |= table.numRows() > 200;
        return new Block("table", null, texts, null, Map.of("width", "100%", "borderCollapse", "collapse"), List.of(), cells);
    }
    private static Node child(Node node, String name) {
        if (node == null) return null;
        for (Node current = node.getFirstChild(); current != null; current = current.getNextSibling()) if (name.equals(current.getLocalName())) return current;
        return null;
    }
    private static Node descendant(Node node, String name) {
        if (node == null) return null; if (name.equals(node.getLocalName())) return node;
        for (Node current = node.getFirstChild(); current != null; current = current.getNextSibling()) { Node found = descendant(current, name); if (found != null) return found; }
        return null;
    }
    private static String attribute(Node node, String name) {
        if (!(node instanceof Element element)) return "";
        String result = element.getAttributeNS(W, name); return result.isEmpty() ? element.getAttribute(name) : result;
    }
    private static String value(Node node) { return attribute(node, "val"); }
    private static double number(Node node, String attribute, double fallback) { try { return Double.parseDouble(attribute(node, attribute)); } catch (NumberFormatException exception) { return fallback; } }
    private static boolean enabled(Node node) { return !Set.of("false", "0", "off", "none").contains(value(node)); }
}
