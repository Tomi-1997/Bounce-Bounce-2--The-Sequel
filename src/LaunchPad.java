import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class LaunchPad extends TemplateObject
{
    double x1, y1, x2, y2, maxX, maxY, maxVY, minVY;
    int direction;
    boolean collideAble = true;
    public LaunchPad(double x, double y, double length, double maxX, double maxY, double maxVY, double minVY)
    {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxVY = maxVY;
        this.minVY = minVY;
        if (x > maxX * 0.5)
        {
            x1 = x - length;
            y1 = y - length;
            x2 = x + length;
            direction = - 1;
        }
        else
        {
            x1 = x + length;
            y1 = y - length;
            x2 = x - length;
            direction = 1;
        }
        y2 = y + length;
    }

    public void draw()
    {
        if (collideAble) StdDraw.setPenColor(Color.WHITE); else StdDraw.setPenColor(Color.darkGray);
        StdDraw.line(x1, y1, x2, y2);
    }

    public void collide(TemplateObject to)
    {
        if (!collideAble) return;

        Player toP = (Player) to;
        boolean hitUpper = toP.y > (y2 + y1) * 0.65;
        double vx, vy;
        if (hitUpper)
        {
            vx = Game.maxVX * 2.25 * direction;
            vy = maxVY * 1;
        }

        else
        {
            vx = Game.maxVX * 1.25 * direction;
            vy = maxVY * 1.25;
        }

        toP.launch(vx, vy);

        new Thread( () ->
        {
            collideAble = false;
            double oldX, oldY;

            long overallWait = 1000;
            long betweenWait = 10;
            long unavailableDuration = 200;

            if (hitUpper)
            {
                oldX = x2;
                oldY = y2;

                x2 = (x1 + x2) * 0.5;
                y2 = y2 + maxY * 0.05;
                Game.delay(overallWait);

                double xInc = (x2 - oldX) / unavailableDuration;
                double yInc = (y2 - oldY) / unavailableDuration;
                for (int i = 0; i < unavailableDuration; i++)
                {
                    x2 = x2 - xInc;
                    y2 = y2 - yInc;
                    Game.delay(betweenWait);
                }

                x2 = oldX;
                y2 = oldY;
            }

            else
            {
                oldX = x1;
                oldY = y1;

                x1 = x1 + maxX * 0.025 * direction;
                y1 += (y2 - y1) / 3;
                Game.delay(overallWait);

                double xInc = (x1 - oldX) / unavailableDuration;
                double yInc = (y1 - oldY) / unavailableDuration;
                for (int i = 0; i < unavailableDuration; i++)
                {
                    x1 = x1 - xInc;
                    y1 = y1 - yInc;
                    Game.delay(betweenWait);
                }

                x1 = oldX;
                y1 = oldY;
            }

            collideAble = true;
        }).start();
    }
}
