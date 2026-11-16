package top.kscn.service;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import java.io.*;
import java.util.*;
import static top.kscn.service.OfficePreviewCss.*;
import static top.kscn.service.FilePreviewService.*;

final class ExcelStylePreview {
    private ExcelStylePreview() {}
    static OfficeView render(byte[] bytes) throws IOException {
        List<SheetView> result = new ArrayList<>(); int count = 0; boolean truncated = false;
        try (Workbook book = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            DataFormatter formatter = new DataFormatter(Locale.SIMPLIFIED_CHINESE); formatter.setUseCachedValuesForFormulaCells(true);
            for (int index = 0; index < Math.min(20, book.getNumberOfSheets()) && count < 20000; index++) {
                Sheet sheet = book.getSheetAt(index); int columns = 0;
                for (Row row : sheet) columns = Math.max(columns, Math.max(0, row.getLastCellNum()));
                List<CellRangeAddress> regions = sheet.getMergedRegions();
                for (CellRangeAddress region : regions) columns = Math.max(columns, region.getLastColumn() + 1);
                int last = sheet.getPhysicalNumberOfRows() == 0 ? -1 : sheet.getLastRowNum();
                for (CellRangeAddress region : regions) last = Math.max(last, region.getLastRow());
                truncated |= columns > 50 || last >= 500; columns = Math.min(columns, 50);
                int rowCount = Math.min(Math.min(last + 1, 500), columns == 0 ? 500 : (20000 - count) / columns);
                truncated |= rowCount < Math.min(last + 1, 500);
                Map<Integer, int[]> merges = new HashMap<>();
                for (CellRangeAddress region : regions) {
                    if (region.getFirstRow() >= rowCount || region.getFirstColumn() >= columns) continue;
                    int bottom = Math.min(region.getLastRow(), rowCount - 1), right = Math.min(region.getLastColumn(), columns - 1);
                    for (int row = region.getFirstRow(); row <= bottom; row++) for (int column = region.getFirstColumn(); column <= right; column++)
                        merges.put(row * 50 + column, new int[]{region.getFirstRow(), region.getFirstColumn(), bottom - region.getFirstRow() + 1, right - region.getFirstColumn() + 1});
                }
                List<Double> widths = new ArrayList<>(), heights = new ArrayList<>();
                for (int column = 0; column < columns; column++) widths.add(sheet.isColumnHidden(column) ? 0.0 : (double) sheet.getColumnWidthInPixels(column));
                List<List<String>> texts = new ArrayList<>(); List<List<ExcelCell>> rows = new ArrayList<>();
                Map<Short, Map<String, String>> styles = new HashMap<>();
                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    Row row = sheet.getRow(rowIndex); heights.add(row != null && row.getZeroHeight() ? 0 : (double) (row == null ? sheet.getDefaultRowHeightInPoints() : row.getHeightInPoints()));
                    List<String> textRow = new ArrayList<>(); List<ExcelCell> cells = new ArrayList<>();
                    for (int column = 0; column < columns; column++, count++) {
                        Cell cell = row == null ? null : row.getCell(column);
                        CellStyle cellStyle = cell == null ? sheet.getColumnStyle(column) : cell.getCellStyle();
                        if (cellStyle == null && row != null) cellStyle = row.getRowStyle();
                        if (cellStyle == null) cellStyle = book.getCellStyleAt(0);
                        final CellStyle resolved = cellStyle;
                        Map<String, String> style = new LinkedHashMap<>(styles.computeIfAbsent(cellStyle.getIndex(), ignored -> cellStyle(book, resolved, sheet.isDisplayGridlines())));
                        boolean numeric = cell != null && (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC);
                        if (cellStyle.getAlignment() == HorizontalAlignment.GENERAL) style.put("textAlign", numeric ? "right" : "left");
                        if (widths.get(column) == 0) style.put("display", "none");
                        Map<String, String> textStyle = new LinkedHashMap<>();
                        short rotation = cellStyle.getRotation();
                        if (rotation == 255) { textStyle.put("writingMode", "vertical-rl"); textStyle.put("textOrientation", "upright"); }
                        else if (rotation != 0) { textStyle.put("display", "inline-block"); textStyle.put("transform", "rotate(" + (-rotation) + "deg)"); }
                        String text = cell == null ? "" : formatter.formatCellValue(cell);
                        if (text.length() > 4096) { text = text.substring(0, 4096); truncated = true; }
                        int[] merge = merges.get(rowIndex * 50 + column);
                        boolean hidden = merge != null && (rowIndex != merge[0] || column != merge[1]);
                        cells.add(new ExcelCell(text, style, textStyle, merge == null ? 1 : merge[3], merge == null ? 1 : merge[2], hidden)); textRow.add(text);
                    }
                    rows.add(cells); texts.add(textRow);
                }
                result.add(new SheetView(sheet.getSheetName(), texts, rows, widths, heights));
            }
            truncated |= book.getNumberOfSheets() > result.size();
        }
        return new OfficeView("excel", List.of(), result, List.of(), truncated, "已保留单元格格式、合并区域及行列尺寸；公式显示已保存的值。图表、条件格式等复杂效果可能与原文件不同。");
    }

    private static Map<String, String> cellStyle(Workbook book, CellStyle style, boolean gridlines) {
        Map<String, String> css = new LinkedHashMap<>(); Font font = book.getFontAt(style.getFontIndex());
        css.put("fontFamily", font(font.getFontName())); css.put("fontSize", pt(font.getFontHeightInPoints()));
        css.put("fontWeight", font.getBold() ? "700" : "400"); css.put("fontStyle", font.getItalic() ? "italic" : "normal");
        String decoration = (font.getUnderline() == Font.U_NONE ? "" : "underline ") + (font.getStrikeout() ? "line-through" : "");
        css.put("textDecoration", decoration.isBlank() ? "none" : decoration.strip());
        put(css, "color", font instanceof XSSFFont xfont ? xcolor(book, xfont.getXSSFColor()) : indexed(book, font.getColor()));
        if (style.getFillPattern() == FillPatternType.SOLID_FOREGROUND)
            put(css, "backgroundColor", style instanceof XSSFCellStyle xstyle ? xcolor(book, xstyle.getFillForegroundXSSFColor()) : indexed(book, style.getFillForegroundColor()));
        css.put("textAlign", switch (style.getAlignment()) { case CENTER, CENTER_SELECTION -> "center"; case RIGHT -> "right"; case JUSTIFY, DISTRIBUTED -> "justify"; default -> "left"; });
        css.put("verticalAlign", switch (style.getVerticalAlignment()) { case TOP -> "top"; case CENTER, JUSTIFY, DISTRIBUTED -> "middle"; default -> "bottom"; });
        css.put("whiteSpace", style.getWrapText() ? "pre-wrap" : "pre"); css.put("overflowWrap", style.getWrapText() ? "anywhere" : "normal");
        if (style.getIndention() > 0) css.put("paddingLeft", px(style.getIndention() * 12.0));
        for (String side : List.of("Top", "Right", "Bottom", "Left")) {
            BorderStyle border = switch (side) { case "Top" -> style.getBorderTop(); case "Right" -> style.getBorderRight(); case "Bottom" -> style.getBorderBottom(); default -> style.getBorderLeft(); };
            short color = switch (side) { case "Top" -> style.getTopBorderColor(); case "Right" -> style.getRightBorderColor(); case "Bottom" -> style.getBottomBorderColor(); default -> style.getLeftBorderColor(); };
            String rgb = indexed(book, color);
            if (style instanceof XSSFCellStyle xstyle) rgb = xcolor(book, switch (side) { case "Top" -> xstyle.getTopBorderXSSFColor(); case "Right" -> xstyle.getRightBorderXSSFColor(); case "Bottom" -> xstyle.getBottomBorderXSSFColor(); default -> xstyle.getLeftBorderXSSFColor(); });
            css.put("border" + side, border == BorderStyle.NONE ? gridlines && style.getFillPattern() == FillPatternType.NO_FILL ? "1px solid #e4e7ed" : "none" : borderWidth(border) + " " + borderLine(border) + " " + (rgb == null ? "#000000" : rgb));
        }
        return css;
    }
    private static String borderWidth(BorderStyle value) { return switch (value) { case THICK, DOUBLE -> "3px"; case MEDIUM, MEDIUM_DASHED, MEDIUM_DASH_DOT, MEDIUM_DASH_DOT_DOT -> "2px"; case HAIR -> "0.5px"; default -> "1px"; }; }
    private static String borderLine(BorderStyle value) { return switch (value) { case DOUBLE -> "double"; case DOTTED, HAIR -> "dotted"; case DASHED, MEDIUM_DASHED, DASH_DOT, MEDIUM_DASH_DOT, DASH_DOT_DOT, MEDIUM_DASH_DOT_DOT, SLANTED_DASH_DOT -> "dashed"; default -> "solid"; }; }
    private static String indexed(Workbook book, short index) {
        HSSFColor color = book instanceof HSSFWorkbook hssf ? hssf.getCustomPalette().getColor(index) : HSSFColor.getIndexHash().get((int) index);
        if (color == null || index == 64) return null;
        short[] rgb = color.getTriplet(); return String.format(Locale.ROOT, "#%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
    }
    private static String xcolor(Workbook book, XSSFColor color) {
        if (color == null || color.isAuto()) return null;
        if (book instanceof XSSFWorkbook xssf && color.isThemed() && xssf.getTheme() != null) xssf.getTheme().inheritFromThemeAsRequired(color);
        byte[] rgb = color.getRGBWithTint();
        if (rgb == null) return color.isIndexed() ? indexed(book, color.getIndexed()) : null;
        return String.format(Locale.ROOT, "#%02x%02x%02x", rgb[0] & 255, rgb[1] & 255, rgb[2] & 255);
    }
}
