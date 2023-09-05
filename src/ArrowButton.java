import edu.princeton.cs.introcs.StdDraw;
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
    }

    public boolean isPressed()
    {
        return isPressed;
    }
}
