import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends TemplateObject
{
    double x, y, radius, vx, vy, maxFallSpeed, maxJump;
    public Color cl;

    public Player(double x, double y, double radius, double minVY, double maxVY)
    {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.vx = 2;
        this.vy = 5;
        this.maxFallSpeed = minVY;
        this.maxJump = maxVY;
        cl = Color.WHITE;
    }

    @Override
    public void draw()
    {
        StdDraw.setPenColor(cl);

        /*
            Draw a deformed circle based on current velocity
         */
        double vxNorm = Math.abs(vx / 5.5);
        double vyNorm = Math.abs( -vy / maxFallSpeed * 2.25);

        if (vxNorm < 1) vxNorm = 1;
        if (vyNorm < 1) vyNorm = 1;

        StdDraw.filledEllipse(x, y, radius * vxNorm, radius * vyNorm);
    }

    @Override
    public void update()
    {
        x = x + vx;
        y = y + vy;

        vy = Math.max(vy - Game.getGravity(), maxFallSpeed);

        /*
            Changing direction has more effect on velocity up to a certain limit.
         */
        double strength = Game.VX;
        if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT))
        {
            if (vx > Game.maxVX) return; // For cases when speed is more than max after hitting a launch pad
            if (vx < 0) strength = strength * 2; // Pressing right but current direction is left
            vx = Math.min(Game.maxVX, vx + strength);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT))
        {
            if (vx < -Game.maxVX) return; // For cases when speed is more than max after hitting a launch pad
            if (vx > 0) strength = strength * 2; // Pressing left but current direction is right
            vx = Math.max(-Game.maxVX, vx - strength);
        }
    }

    @Override
    public void reset()
    {
        //
    }

    public void collide(TemplateObject to)
    {
        if (to.getClass() == Obstacle.class) collide( (Obstacle) to);
    }

    public void collide(Obstacle o)
    {
        /*
            Hit obstacle, flip y velocity between a defined limit
         */
        vy = -vy;
        if (vy < Game.hitVY) vy = Game.hitVY;
        if (vy > maxJump) vy = maxJump;
    }

    public void launch(double vx_, double vy_)
    {
        vy = vy_;
        vx = vx_;

        for (int i = 0; i < 2; i++) update();
    }

    public boolean isIn(Obstacle o, double pEPS)
    {
        return x + this.radius + pEPS > o.x - o.halfWidth && x - radius - pEPS < o.x + o.halfWidth &&
                y + this.radius > o.y - o.halfHeight && y - radius < o.y + o.halfHeight;
    }

}
