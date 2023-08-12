import edu.princeton.cs.introcs.StdDraw;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class Game
{
    public Game()
    {
        TO = Collections.synchronizedCollection(new ArrayList<>());
        createPlayer();
        createObstacles((int) (maxX * 0.25), maxY / 2);
        createInformation();
        createSound("Minibit.wav", "beep");
        createLaunchPads();
    }


    private final Collection<TemplateObject> TO;
    private Player p;
    private Sound s;
    private LaunchPad l, r;

    private static final long FPS = 1000 / 60;
    public static final int obstacles = 10;
    public static final int maxX = 800;
    public static final int maxY = 400;
    public static final int obstacleEPS = 50;
    public static final int pEPS = 10; // Space of error when deciding if the player hit an obstacle or not
    public static final int hitReward = 10;

    public static double G = 0.15;
    public static final double minVY = -10;
    public static final double maxVY = 7;
    public static final double maxVX = 4;
    public static final double VX = 0.2;
    public static final double hitVY = 5;
    final double baseSpeed = 1.5;
    public static double speedMultiplier = 0.005;
    public static final double maxSpeed = 7;
    public static double score = 0;
    private double lastCollision = 0;
    public static final double beepProb = 0.2;
    public static final int beepFiles = 5;
    public static boolean hasMusic = true;
    private boolean restartAvailable = true;
    private boolean isResetting = false;

    private void createInformation()
    {
        Information i = new Information(maxX, maxY, this.p);
        TO.add(i);
    }

    private void createObstacles(int x, int y)
    {
        for (int i = 0; i < obstacles; i++)
        {
            int w = randInt(50, 60);
            int h = randInt(5, 10);
            double speed = baseSpeed + Math.random();
            Obstacle o = new Obstacle(x, y, w / 2.0, h / 2.0, speed);
            TO.add(o);

            x = x + randInt(150, 200);
            y = y + randInt(-30, 30);
        }
    }

    private void createPlayer()
    {
        Player p = new Player(0, maxY / 2.0, 5);
        TO.add(p);
        this.p = p;
    }

    private void createSound(String bgSound, String hitSound)
    {
        try
        {
            s = new Sound(bgSound, hitSound);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            hasMusic = false;
        }
    }

    private void createLaunchPads()
    {
        l = new LaunchPad(p, maxX * 0.025, maxY * 0.25, maxY * 0.1);
        r = new LaunchPad(p, maxX * 0.975, maxY * 0.25, maxY * 0.1);

        TO.add(l);
        TO.add(r);
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

    private void iterate()
    {
        synchronized (TO) { for (Updatable u : TO) u.update(); }
        synchronized (TO) { for (Drawable  d : TO) d.draw(); }
        checkCollision();
    }

    private void restart()
    {
        restartAvailable = false;
        /*
            Print text to user indicating a reset is near
         */
        int duration = 1000;
        int dots = 3;
        StringBuilder text = new StringBuilder("Restarting");
        for (int i = 0; i <= dots; i++)
        {
            final String lambdaText = text.toString();
            TemplateObject d = new TemplateObject()
            {
                @Override
                public void update() {}
                @Override
                public void reset() {}
                @Override
                public void onPress() {}
                @Override
                public void draw() { StdDraw.textLeft(maxX / 2.3, maxY / 1.5, lambdaText); }
            };

            add(d);
            delay(duration / dots);
            rm(d);
            TO.remove(d);
            text.append(".");
        }

        /*
            Perform reset() for each object
         */

        for (TemplateObject to : TO)
            to.reset();

        int dropDuration = maxY / 2;
        createObstacles((int) (maxX * 0.4), (int) (maxY * 1.5));

        for (int i = 0; i < dropDuration; i++)
        {
            synchronized (TO)
            {
                for (TemplateObject u : TO)
                {
                    if (u.getClass() != Obstacle.class) continue;
                    Obstacle o = (Obstacle) u;
                    o.y = o.y - 2;
                }
            }
            delay(10);
        }


        delay(1000);

        TO.removeIf(TemplateObject::isReset);
        restartAvailable = true;
    }

    private boolean isRunning()
    {
        //
        if (restartAvailable && StdDraw.isKeyPressed(KeyEvent.VK_R)) { new Thread(this::restart).start(); }
        return !StdDraw.isKeyPressed(KeyEvent.VK_Q);
    }

    private void checkCollision()
    {
        /*
            Find obstacles from updatable \ drawables
         */
        ArrayList<Obstacle> arr = new ArrayList<>();
        synchronized (TO)
        {
            for (TemplateObject to : TO)
            {
                if (to.getClass() == Obstacle.class) arr.add((Obstacle) to);
            }
        }

        /*
            If the player drops below screen - reset score and launch him back
         */
        if (p.y < -maxY * 0.2)
        {
            p.vy = maxY / 40.0 + 2;
            resetScore();
        }

        /*
            If the player is above the screen- draw an indicator line
         */
        if (p.y > maxY * 1.05)
        {
            StdDraw.setPenColor(p.cl);
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
            if (p.x + p.radius + pEPS > o.x - o.halfWidth && p.x - p.radius - pEPS < o.x + o.halfWidth &&
                    p.y + p.radius > o.y - o.halfHeight && p.y - p.radius < o.y + o.halfHeight)
            {
                p.collide(o);
                o.collide(p);
                lastCollision = System.currentTimeMillis();
                Color temp = o.cl;
                o.cl = p.cl;
                p.cl = temp;
                score += hitReward;
                s.collide(null);
                generateDust(o);
                break;
            }
        }

        /*
            Check if the player hit a launch pad
         */
        if (between(p.x, p.y, l.x1, l.y1, l.x2, l.y2))
            l.collide(p);

        if (between(p.x, p.y, r.x1, r.y1, r.x2, r.y2))
            r.collide(p);
    }

    private boolean between(double x, double y, double x1, double y1, double x2, double y2)
    {
        double dtToX1 = (x - x1) * (x - x1) + (y - y1) * (y - y1);
        dtToX1 = Math.sqrt(dtToX1);

        double dtToX2 = (x - x2) * (x - x2) + (y - y2) * (y - y2);
        dtToX2 = Math.sqrt(dtToX2);

        double dtOverall = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        dtOverall = Math.sqrt(dtOverall);

        double dtEPS = 10;

        return dtToX1 + dtToX2 - dtOverall < dtEPS;
    }

    private void resetScore()
    {
        if (isResetting) return;

        long delayTime = 20;
        delayTime = Math.max(( long) (25 * delayTime / (score + 1)), 1);
        long finalDelayTime = delayTime;


        new Thread( () ->
        {
            isResetting = true;
            double scoreAtFall = score;
            double sub = scoreAtFall / 200 + 1;
            while (scoreAtFall > 0)
            {scoreAtFall = Math.max(scoreAtFall - sub, 0); score = Math.max(score - sub, 0); delay(finalDelayTime);}
            isResetting = false;
        } ).start();
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

    private void generateDust(Obstacle o)
    {
        new Thread(() -> generateDust_(o)).start();
    }


    private void generateDust_(Obstacle o)
    {
        int fireworksNum = 10;
        double radius = 5;
        ArrayList<TemplateObject> obstacleHitDust = new ArrayList<>();
        for (int i = 0; i < fireworksNum; i++)
        {
            obstacleHitDust.add(new Firework(o.x, o.y, radius, false, o.cl));
        }

        addAll(obstacleHitDust);
        delay(2 * 1000);
        rmAll(obstacleHitDust);

    }

    private void rm(TemplateObject d)
    {
        synchronized (TO) {
            TO.remove(d);
        }
    }

    private void add(TemplateObject d)
    {
        synchronized (TO) {
            TO.add(d);
        }
    }

    private void rmAll(ArrayList<TemplateObject> obstacleHitDust)
    {
        //
        synchronized (TO) {TO.removeAll(obstacleHitDust); }
    }

    private void addAll(ArrayList<TemplateObject> obstacleHitDust)
    {
        //
        synchronized (TO) {TO.addAll(obstacleHitDust); }
    }

    public static int randInt(int a, int b)
    {
        //
        return (int) (Math.random() * (b - a)) + a;
    }

}
