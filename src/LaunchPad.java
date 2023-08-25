import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class LaunchPad extends TemplateObject
{
    double x1;
    double y1;
    double x2;
    double y2;
    int direction;
    boolean collideAble = true;

    final double catapultPart = 0.65; // Hitting above this number * (y2 + y1) will act as a catapult

    public LaunchPad(double x, double y, double length, double maxX)
    {
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
        /*
            Upper section - where hitting will catapult you
         */
        if (collideAble) StdDraw.setPenColor(Color.WHITE); else StdDraw.setPenColor(Color.darkGray);
        StdDraw.line(x1, y1, x1 - (x1 - x2) * catapultPart, y1 - (y1 - y2) * catapultPart);

        /*
            Lower section - hitting this will thrust the player upwards.
         */
        if (collideAble) StdDraw.setPenColor(Color.RED); else StdDraw.setPenColor(Color.darkGray);
        StdDraw.line(x2, y2, x2 - (x2 - x1) * (1 - catapultPart), y2 - (y2 - y1) * (1 - catapultPart));
    }

    public void collide(TemplateObject to)
    {
        if (!collideAble) return;

        /*
            Just collided with player, check area of collision and spring forward
         */

        Player toP = (Player) to;
        boolean hitUpper = toP.y > (y2 + y1) * catapultPart;
        double vx, vy;
        if (hitUpper)
        {
            vx = Game.getInstance().getMaxVX() * 2.25 * direction;
            vy = Game.getInstance().getMaxVY() * 1;
        }

        else
        {
            vx = Game.getInstance().getMaxVX() * 1.25 * direction;
            vy = Game.getInstance().getMaxVY() * 1.25;
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
                y2 = y2 + Game.getInstance().getMaxY() * 0.05;
                Game.getInstance().delay(overallWait);

                double xInc = (x2 - oldX) / unavailableDuration;
                double yInc = (y2 - oldY) / unavailableDuration;
                for (int i = 0; i < unavailableDuration; i++)
                {
                    x2 = x2 - xInc;
                    y2 = y2 - yInc;
                    Game.getInstance().delay(betweenWait);
                }

                x2 = oldX;
                y2 = oldY;
            }

            else
            {
                oldX = x1;
                oldY = y1;

                x1 = x1 + Game.getInstance().getMaxX() * 0.025 * direction;
                y1 += (y2 - y1) / 3;
                Game.getInstance().delay(overallWait);

                double xInc = (x1 - oldX) / unavailableDuration;
                double yInc = (y1 - oldY) / unavailableDuration;
                for (int i = 0; i < unavailableDuration; i++)
                {
                    x1 = x1 - xInc;
                    y1 = y1 - yInc;
                    Game.getInstance().delay(betweenWait);
                }

                x1 = oldX;
                y1 = oldY;
            }

            collideAble = true;
        }).start();
    }
}
