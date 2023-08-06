import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Game
{
    private final Collection<Updatable> US;
    private final Collection<Drawable>  DS;
    private double lastCollision = 0;

    private static final long FPS = 1000 / 60;
    public static final int obstacles = 10, maxX = 800, maxY = 400, obstacleEPS = 20, pEPS = 5, hitReward = 10;
    public static final double G = 0.15, minVY = -10, maxVY = 7, maxVX = 4, VX = 0.2, hitVY = 5;

    public Game()
    {
        US = Collections.synchronizedCollection(new ArrayList<>());
        DS = Collections.synchronizedCollection(new ArrayList<>());

        /*
            Create player
         */
        Player p = new Player(0, maxY / 2.0, 5);
        US.add(p);
        DS.add(p);

        /*
            Create obstacles which reset in position after passing the screen
         */
        int x = (int) (maxX * 0.25);
        int y = maxY / 2;
        for (int i = 0; i < obstacles; i++)
        {
            int w = randInt(50, 60);
            int h = randInt(5, 10);
            double speed = 1.5 + Math.random();
            Obstacle o = new Obstacle(x, y, w / 2.0, h / 2.0, speed);
            US.add(o);
            DS.add(o);

            x = x + randInt(150, 200);
            y = y + randInt(-30, 30);
        }

        /*
            Info object for score, etc
         */
        Information i = new Information(maxX, maxY, p);
        US.add(i);
        DS.add(i);

    }

    private int randInt(int a, int b)
    {
        //
        return (int) (Math.random() * (b - a)) + a;
    }

    public void run()
    {
        StdDraw.setCanvasSize(maxX, maxY);
        StdDraw.setXscale(0, maxX);
        StdDraw.setYscale(0, maxY);
        StdDraw.setPenRadius(0.004);

        while ( isRunning() )
        {
            StdDraw.clear(StdDraw.BLACK);
            iterate();
            StdDraw.show(0);
            delay();
        }
        System.exit(0);
    }

    private boolean isRunning()
    {
        //
        return !StdDraw.isKeyPressed(KeyEvent.VK_Q);
    }

    public static void delay()
    {
        //
        try {Thread.sleep(FPS);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public static void delay(long millis)
    {
        //
        try {Thread.sleep(millis);} catch (InterruptedException e) {e.printStackTrace();}
    }

    private void iterate()
    {
        for (Updatable u : US) u.update();
        for (Drawable  d : DS) d.draw();
        checkCollision();
    }

    private void checkCollision()
    {

        Player p = null;
        ArrayList<Obstacle> arr = new ArrayList<>();

        /*
            Find player and obstacles from updatables
         */
        for (Updatable u : US)
        {
            if (u.getClass() == Player.class)
                p = (Player) u;
            if (u.getClass() == Obstacle.class)
                arr.add((Obstacle) u);
        }

        if (p == null) return;

        /*
            If the player drops below screen - reset score and launch him back
         */
        if (p.y < -maxY * 0.2)
        {
            p.vy = maxY / 40.0 + 2;
            p.score = 0;
        }

        /*
            If the player is above the screen- draw an indicator line
         */
        if (p.y > maxY * 1.05)
        {
            StdDraw.line(p.x, maxY * 1.1, p.x, maxY * 0.9);
        }

        /*
            If the player hits a wall, bounce him back at max X speed and give a little upwards boost
         */
        if (p.x + p.radius < -maxX * 0.05) {p.vx = maxVX; p.vy = Math.min(p.vy + 2, Game.maxVY);}
        if (p.x - p.radius> maxX * 1.05) {p.vx = -maxVX; p.vy = Math.min(p.vy + 2, Game.maxVY);}
        if (System.currentTimeMillis() - lastCollision < 500) return;

        /*
            Check for each obstacle if the player hit them
         */
        for (Obstacle o : arr)
        {
            if (p.x + p.radius + pEPS > o.x - o.halfWidth && p.x - p.radius + pEPS < o.x + o.halfWidth &&
                    p.y + p.radius - pEPS > o.y - o.halfHeight && p.y - p.radius - pEPS < o.y + o.halfHeight)
            {
                p.applyHit();
                o.applyHit();
                lastCollision = System.currentTimeMillis();
                Color temp = o.cl;
                o.cl = p.cl;
                p.cl = temp;
                p.score += hitReward;

                generateDust(o);
                return;
            }
        }
    }

    private void generateDust(Obstacle o)
    {
        new Thread(() -> generateDust_(o)).start();
    }


    private void generateDust_(Obstacle o)
    {
        int fireworksNum = 10;
        double radius = 5;
        ArrayList<Firework> obstacleHitDust = new ArrayList<>();
        for (int i = 0; i < fireworksNum; i++)
        {
            obstacleHitDust.add(new Firework(o.x, o.y, radius, false));
        }

        for (Firework f : obstacleHitDust)
        {
            US.add(f);
            DS.add(f);
        }

        delay(2 * 1000);

        for (Firework f : obstacleHitDust)
        {
            US.remove(f);
            DS.remove(f);
        }
    }
}
