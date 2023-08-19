import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class Particles extends TemplateObject
{
    double x, y, vx, vy;
    Color cl;
    int lifetime;
    public Particles(double x, double y, double radius, boolean circular)
    {
        this.x = x;
        this.y = y;

        do
        {
            vx = Math.random() * 2 * radius - radius;
            vy = Math.random() * radius; // Half a circle spread

            if (circular) // 360 spread
                vy = Math.random() * 2 * radius - radius;

        } while(vx * vx + vy * vy > radius);
        lifetime = (int) (Math.random() * 150) + 50;
    }

    public Particles(double x, double y, double radius, boolean circular, Color cl)
    {
        this(x, y, radius, circular);
        this.cl = cl;
    }

    @Override
    public void draw()
    {
        //
        if (cl != null) StdDraw.setPenColor(cl);
        if (lifetime > 0) StdDraw.point(x, y);
    }

    @Override
    public void update()
    {
        //
        x = x + vx;
        y = y + vy;

        vy = vy - Game.getGravity() * 0.05;
        vx = vx - Game.getGravity() * 0.01;
        lifetime--;
    }
}
