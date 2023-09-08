import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;

public class Obstacle extends TemplateObject implements Collidable
{
    double x, y, halfWidthStart, halfWidth, halfHeight, speed;
    Color cl;
    int dir;
    public Obstacle(double x, double y, double halfWidth, double halfHeight, double speed)
    {
        this.x = x;
        this.y = y;
        this.halfWidth = halfWidth;
        this.halfWidthStart = halfWidth;
        this.halfHeight = halfHeight;
        this.speed = speed;

        cl = Game.getRandColor();

        /*
            Slowly float upwards and downwards. Switch direction after X amount of time
         */
        dir = Math.random() > 0.5 ? 1 : -1;
        new Thread(this::sway).start();
    }

    private void sway()
    {
        while (!isReset())
        {
            dir = dir * -1;
            Game.getInstance().delay(20 * 1000);
        }
    }

    @Override
    public void draw()
    {
        StdDraw.setPenColor(cl);
        StdDraw.filledRectangle(x, y, halfWidth, halfHeight);
    }

    @Override
    public void update()
    {
        /*
            {x plus stuff < 0} -> It went off-screen to the left. Reset position to around the start with some random
            increments. Move height a bit.
         */
        if (x + 2 * halfWidth + Game.getInstance().getMaxX() * 0.1 < 0)
        {
            x = Game.getInstance().getMaxX() * (Math.random() * 0.3 + 1.2);
            y = y + Math.random() * 10 - 5;
        }

        /*
            Go left (Speed is based on the current score), float up/down.
         */
        double currSpeed = speed + Game.getInstance().getSpeedMultiplier() * Game.getInstance().getScore();
        if (currSpeed > Game.getInstance().getMaxSpeed()) currSpeed = Game.getInstance().getMaxSpeed();
        x = x - currSpeed;
        y = y + 0.1 * dir;

        /*
            Update width based on score
         */
        halfWidth = halfWidthStart - Game.getInstance().getSpeedMultiplier() * Game.getInstance().getScore() * 1.5;
        halfWidth = Math.max(halfWidthStart / 5, halfWidth);
    }

    public void recoil()
    {
        /*
            Hit by player, drop abruptly and slowly stabilize. Then accelerate to the previous location.
         */
        double startY = y;
        double str = 5; // hit strength
        double str_inc = 0.1; // brakes / gas power
        int duration = 20;

        for (int i = 0; i < duration; i++)
        {
            y = y - str; // go down
            Game.getInstance().delay();
            str = str * 0.9; // slow down
        }

        /*
            About to disappear, don't go back up
         */
        if (isReset()) return;
        str = -str;
        while(y < startY * 1.01)
        {
            y = y + str; // go up
            Game.getInstance().delay();
            str = str + str_inc; // accelerate
        }
    }

    /**
     * Performs the default recoil pattern, recoil down a bit, go back up. (No x move)
     * @param to object collided with
     */
    @Override
    public void collide(TemplateObject to)
    {
        new Thread(this::recoil).start();
    }


    /**
     * Performs recoil based on current velocity of object which the collision has occurred with.
     * @param to object collided with
     * @param vx X velocity
     * @param vy Y velocity
     */
    public void collide(TemplateObject to, double vx, double vy)
    {
        new Thread(
                () ->
                {
                    double strY = vy * 1.5; // make a higher drop more significant
                    double strX = vx < 0 ? vx * 0.25 : vx * 1.5; // against the flow - higher influence

                    /*
                        Go down + sideways
                     */
                    double ySum = 0;
                    for (int i = 0; i < 20; i++)
                    {
                        x = x + strX;
                        y = y - strY;

                        ySum = ySum + strY;
                        strY = strY * 0.8;
                        strX = strX * 0.8;
                        Game.getInstance().delay();
                    }

                    /*
                        Marked reset, don't go back up- will disappear promptly.
                     */
                    if (isReset()) return;

                    /*
                        Go back up
                     */

                    Game.getInstance().delay(100);
                    strY = 0.1; // start rising slowly
                    double backUpStr = 0.05; // increase speed gradually
                    while (ySum >= 0)
                    {
                        y = y + strY;
                        ySum = ySum - strY;
                        strY += backUpStr;
                        Game.getInstance().delay();
                    }
                }
        ).start();
    }

    @Override
    public void reset()
    {
        isReset = true;
    }
}
