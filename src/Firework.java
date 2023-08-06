import edu.princeton.cs.introcs.StdDraw;
public class Firework implements Updatable, Drawable
{
    double x, y, vx, vy;
    int lifetime;
    public Firework(double x, double y, double radius, boolean circular)
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

    @Override
    public void draw()
    {
        //
        if (lifetime > 0) StdDraw.point(x, y);
    }

    @Override
    public void update()
    {
        //
        x = x + vx;
        y = y + vy;

        vy = vy - Game.G * 0.05;
        vx = vx - Game.G * 0.01;
        lifetime--;
    }
}
