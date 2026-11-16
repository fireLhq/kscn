package top.kscn.utils;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.Shape;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * POI 6.0.0 的 DrawShape 会无条件执行两次翻转。
 * 仅替换预览中形状的坐标变换，绘制、母版及组合图形仍由 POI 处理。
 * 上游修复：https://github.com/apache/poi/blob/trunk/poi/src/main/java/org/apache/poi/sl/draw/DrawShape.java
 */
public final class PreviewDrawFactory extends DrawFactory {
    @Override
    public Drawable getDrawable(Shape<?, ?> shape) {
        Drawable renderer = super.getDrawable(shape);
        if (!(shape instanceof PlaceableShape<?, ?> placed) || !(renderer instanceof DrawShape)) return renderer;
        return new Drawable() {
            @Override public void draw(Graphics2D graphics) { renderer.draw(graphics); }
            @Override public void drawContent(Graphics2D graphics) { renderer.drawContent(graphics); }
            @Override public void applyTransform(Graphics2D graphics) { transform(graphics, placed); }
        };
    }

    private static void transform(Graphics2D graphics, PlaceableShape<?, ?> shape) {
        if (graphics == null) return;
        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);
        if (anchor == null) return;
        double x = anchor.getCenterX(), y = anchor.getCenterY();
        AffineTransform flip = new AffineTransform();
        flip.translate(x, y);
        flip.scale(shape.getFlipHorizontal() ? -1 : 1, shape.getFlipVertical() ? -1 : 1);
        flip.translate(-x, -y);
        AffineTransform result = new AffineTransform();
        // 二进制PPT与PPTX的旋转/翻转顺序不同，沿用POI各自的顺序。
        if (shape instanceof HSLFShape) result.concatenate(flip);
        result.rotate(Math.toRadians(shape.getRotation()), x, y);
        if (!(shape instanceof HSLFShape)) result.concatenate(flip);
        graphics.transform(result);
    }
}
