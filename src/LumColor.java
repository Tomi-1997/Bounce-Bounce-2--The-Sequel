import java.awt.*;

public class LumColor {
    private Color color;
    public LumColor(){
        generateLuminance();
    }
    public Color getColor(){
        return color;
    }

    private void setColor(double r, double g, double b){
        int r8bit = (int)(r *255);
        int g8bit = (int)(g *255);
        int b8bit = (int)(b *255);
        color = new Color(r8bit,g8bit,b8bit);
    }
    private double generateChannel()
    {
        /*
         * Generate a random colour
         * For the sRGB colorspace, the relative luminance of a color is defined as L = 0.2126 * R + 0.7152 * G + 0.0722 * B where R, G and B are defined as:
         * <p>
         * if RsRGB <= 0.03928 then R = RsRGB/12.92 else R = ((RsRGB+0.055)/1.055) ^ 2.4
         * if GsRGB <= 0.03928 then G = GsRGB/12.92 else G = ((GsRGB+0.055)/1.055) ^ 2.4
         * if BsRGB <= 0.03928 then B = BsRGB/12.92 else B = ((BsRGB+0.055)/1.055) ^ 2.4
         * <p>
         * RsRGB, GsRGB, and BsRGB are defined as:
         * RsRGB = R8bit/255
         * GsRGB = G8bit/255
         * BsRGB = B8bit/255
         */
        double c = Math.random();
        if (c <= 0.03928)
            return c / 12.92;
        else return Math.pow( (c + 0.055) / 1.055, 2.4 );
    }

    private void generateLuminance() {
        double r = 0, g = 0, b = 0;
        double ratio = 1;
        while (ratio > 0.14285) { // 1/7 - most strict standard
            r = generateChannel();
            g = generateChannel();
            b = generateChannel();
            ratio = calcLuminanceRatio(r, g, b);
        }
        setColor(r, g, b);
    }
    private double calcLuminanceRatio(double r, double g, double b){
        double black_luminance = 0;
        double luminance = r * 0.2126 + g * 0.7152 + b * 0.0722;
        return (black_luminance + 0.05) / (luminance + 0.05);
    }
}
