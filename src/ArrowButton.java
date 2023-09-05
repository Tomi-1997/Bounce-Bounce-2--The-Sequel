import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class ArrowButton extends TemplateObject
{
    private int keyNum;
    private boolean isPressed = false, isLeft;
    public ArrowButton(int keyEvent, boolean left)
    {
        this.keyNum = keyEvent;
        this.isLeft = left;
    }

    @Override
    public void update()
    {
        isPressed = StdDraw.isKeyPressed(keyNum);
    }

    @Override
    public void draw()
    {
        int maxX = Game.getInstance().getMaxX();
        double xOffset = 0.05;
        double radius = maxX * 0.025;
        double x = isLeft ? maxX * xOffset : maxX * (1 - xOffset);
        double y = Game.getInstance().getMaxY() * 0.05;

        if (isPressed) StdDraw.filledSquare(x, y, radius); else StdDraw.square(x, y, radius);

        /*
                    x2                      x2
            x1          <-- Left Right -->         x1
                    x2                      x2
         */

        xOffset = maxX * 0.01;
        double yOffset = xOffset;

        double x1 = isLeft? x - xOffset : x + xOffset;
        double x2 = isLeft? x + xOffset : x - xOffset;
        Color oldCl = StdDraw.getPenColor();

        if (isPressed) StdDraw.setPenColor(Color.BLACK);
        StdDraw.line(x1, y, x2, y + yOffset);
        StdDraw.line(x1, y, x2, y - yOffset);
        StdDraw.setPenColor(oldCl);
    }

    public boolean isPressed()
    {
        return isPressed;
    }
}
