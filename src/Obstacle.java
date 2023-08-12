import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;

public class Obstacle extends TemplateObject
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

        /*
            Generate a random colour, brighten if needed.
         */
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);

        int temp;

        /*
            Blue component of a colour is much higher than red - swap values
         */
        if (b > r + 100)
        {
            temp = b;
            b = r;
            r = temp;
        }

        /*
            Blue component of a colour is much higher than green - swap values
         */
        if (b > g + 100)
        {
            temp = b;
            b = g;
            g = temp;
        }

        while (r + g + b < 200)
        {
            r = r + 50;
            g = g + 50;
            b = b + 15;
        }
        cl = new Color(r, g, b);

        /*
            Slowly float upwards and downwards. Switch direction after X amount of time
         */
        dir = Math.random() > 0.5 ? 1 : -1;
        new Thread(this::sway).start();
    }

    private void sway()
    {
        while (true)
        {
            dir = dir * -1;
            Game.delay(45 * 1000);
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
        if (x + 2 * halfWidth + Game.obstacleEPS < 0)
        {
            x = Game.maxX * (Math.random() * 0.3 + 1.2);
            y = y + Math.random() * 10 - 5;
        }

        /*
            Go left, float up/down
         */
        double currSpeed = speed + Game.speedMultiplier * Game.score;
        if (currSpeed > Game.maxSpeed) currSpeed = Game.maxSpeed;
        x = x - currSpeed;
        y = y + 0.05 * dir;

        /*
            Update width based on score
         */
        halfWidth = halfWidthStart - Game.speedMultiplier * Game.score;
        if (halfWidth < halfWidthStart / 2) halfWidth = halfWidthStart / 2;
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
            Game.delay();
            str = str * 0.9; // slow down
        }

        str = -str;
        while(y < startY * 0.95)
        {
            y = y + str; // go up
            Game.delay();
            str = str + str_inc; // accelerate
        }
    }

    @Override
    public void collide(TemplateObject to)
    {
        //
        new Thread(this::recoil).start();
    }


    @Override
    public void reset()
    {
        isReset = true;
    }
}
