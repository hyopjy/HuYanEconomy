package cn.chahuyun.economy.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.concurrent.ThreadLocalRandom;

public class AdvancedRandomColorTextRenderer {
    // 高性能随机数生成器（线程安全）
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    // 黄金分割点 (φ = (1+√5)/2 ≈ 0.618034)
    private static final float GOLDEN_RATIO = 0.618034f;
    private static float hueSeed = RAND.nextFloat();

    /**
     * 高级随机颜色生成算法（黄金分割色相分布）
     */
    public static Color generateHarmoniousRandomColor() {
        // 使用黄金分割算法生成均匀分布的色相
        hueSeed = (hueSeed + GOLDEN_RATIO) % 1;

        // 固定饱和度和亮度在视觉舒适区间
        float saturation = 0.8f + RAND.nextFloat() * 0.2f; // 80-100%饱和度
        float brightness = 0.7f + RAND.nextFloat() * 0.2f; // 70-90%亮度

        return Color.getHSBColor(hueSeed, saturation, brightness);
    }

    /**
     * 带特效的文字渲染方法
     * @param pen     图形上下文
     * @param text    要渲染的文字
     * @param x       文字起始X坐标
     * @param y       文字基线Y坐标
     * @param effects 特效类型（1=渐变，2=阴影，3=轮廓）
     */
    public static void renderTextWithEffects(Graphics2D pen, String text, int x, int y, int effects) {
        // 保存原始绘图状态
        AffineTransform originalTransform = pen.getTransform();
        Composite originalComposite = pen.getComposite();

        try {
            // === 文字特效处理 ===
            if ((effects & 1) != 0) {
                // 渐变填充特效
                Color color1 = generateHarmoniousRandomColor();
                Color color2 = generateHarmoniousRandomColor();
                GradientPaint gradient = new GradientPaint(
                        x, y, color1,
                        x + pen.getFontMetrics().stringWidth(text), y, color2
                );
                pen.setPaint(gradient);
            } else if ((effects & 2) != 0) {
                // 文字阴影特效
                pen.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                pen.setColor(Color.BLACK);
                pen.drawString(text, x + 2, y + 2);
                pen.setComposite(originalComposite);
            }

            if ((effects & 4) != 0) {
                // 文字描边特效
                pen.setColor(Color.WHITE);
                pen.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                pen.drawString(text, x, y);
            }

            // === 最终文字渲染 ===
            pen.setColor((effects & 1) == 0 ? generateHarmoniousRandomColor() : pen.getColor());
            pen.drawString(text, x, y);
        } finally {
            // 恢复原始绘图状态
            pen.setTransform(originalTransform);
            pen.setComposite(originalComposite);
        }
    }
}