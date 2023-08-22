import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    private static final double G = 0.15;
    private final int obstacles = 10, maxX = 800, maxY = 400, hitReward = 10;

    private final double minVY = -10, maxVY = 7;
    public static final double maxVX = 4;
    public static final double VX = 0.2;
    public static final double hitVY = 5;
    final double baseSpeed = 1.5;
    public static double speedMultiplier = 0.004;
    public static final double maxSpeed = 6;
    public static double score = 0;
    private double lastCollision = 0;
    private double penR = 0.004;
    public static final double beepProb = 0.2;

    public static final int beepFiles = 5;
    public static boolean hasMusic = true;
    private boolean restartAvailable = true, regenAvailable = true;
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
            int w = randInt(maxX / 15, maxX / 10);
            int h = randInt(maxY / 100, maxY / 50);
            double speed = baseSpeed + Math.random();
            Obstacle o = new Obstacle(x, y, w / 2.0, h / 2.0, speed, maxX);
            TO.add(o);

            x = x + randInt(150, 200);
            y = y + randInt(-30, 30);
        }
    }

    private void createPlayer()
    {
        int playerRadius = Math.min(maxX, maxY) / 80;
        Player p = new Player(0, maxY / 2.0, playerRadius, minVY, maxVY);
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
        l = new LaunchPad(maxX * 0.025, maxY * 0.25, maxY * 0.1, maxX, maxY, maxVY, minVY);
        r = new LaunchPad(maxX * 0.975, maxY * 0.25, maxY * 0.1, maxX, maxY, maxVY, minVY);

        TO.add(l);
        TO.add(r);
    }

    public void run()
    {
        StdDraw.setCanvasSize(maxX, maxY);
        StdDraw.setXscale(0, maxX);
        StdDraw.setYscale(0, maxY);
        StdDraw.setPenRadius(penR);

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
        checkRegeneration();
    }

    private void checkRegeneration()
    {
        if (!regenAvailable) return;
        new Thread(this::checkRegeneration_).start();
    }

    private void checkRegeneration_()
    {
        regenAvailable = false;
        int delaySec = randInt(10, 30);
        delay(delaySec * 1000L);

        /*
            Countdown to regen
         */
        int duration = 1000;
        for (int i = 3; i >= 1; i--)
        {
            final String lambdaText = Integer.toString(i);
            TemplateObject d = new TemplateObject()
            {
                @Override
                public void update() {}
                @Override
                public void reset() {}
                @Override
                public void onPress() {}
                @Override
                public void draw()
                {
                    StdDraw.setPenColor(Color.white);
                    setSize(56);
                    StdDraw.text(maxX / 2.0, maxY / 2.0, lambdaText);
                }
            };

            add(d);
            delay(duration);
            rm(d);
            TO.remove(d);
        }

        /*
            Mark obstacles for removal
         */
        for (TemplateObject to : TO)
            if (to.getClass() == Obstacle.class) to.reset();

        int dropStrength = 1;
        createObstacles((int) (maxX * 0.4), (int) (maxY * 1.5));

        /*
            Slowly drop all obstacles
         */

        for (int i = 0; i < maxY; i++)
        {
            synchronized (TO)
            {
                for (TemplateObject u : TO)
                {
                    if (u.getClass() != Obstacle.class) continue;
                    Obstacle o = (Obstacle) u;
                    o.y = o.y - dropStrength;
                }
            }
            delay(10);
        }


        delay(2000);

        /*
            Remove old obstacles
         */
        TO.removeIf(TemplateObject::isReset);
        regenAvailable = true;
    }

    private void setSize(int titleSize)
    {
        Font font = new Font("Monospaced", Font.BOLD, titleSize);
        StdDraw.setFont(font);
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

        resetScore();

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
        if (p.y > maxY * 1.1)
        {
            StdDraw.setPenColor(p.cl);

            double currentRad = p.y - maxY;

            currentRad = 1 / currentRad;

            currentRad = Math.min(penR * 5, currentRad);
            currentRad = Math.max(penR, currentRad);
            StdDraw.setPenRadius(currentRad);
            double offset = maxX * 0.01;
            StdDraw.line(p.x, maxY * 1.04, p.x + offset , maxY);
            StdDraw.line(p.x, maxY * 1.04, p.x - offset, maxY);

            StdDraw.setPenRadius(penR);
        }

        /*
            If the player hits a wall, bounce him back at max X speed and give a little upwards boost
         */
        if (p.x + p.radius < -maxX * 0.05) {p.vx = maxVX; p.vy = Math.min(p.vy + 3, maxVY);}
        if (p.x - p.radius > maxX * 1.05) {p.vx = -maxVX; p.vy = Math.min(p.vy + 3, maxVY);}
        if (System.currentTimeMillis() - lastCollision < 500) return;

        /*
            Check for each obstacle if the player hit them
         */
        double pEPS = p.radius;
        for (Obstacle o : arr)
        {
            if (p.isIn(o, pEPS))
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
        { l.collide(p); lastCollision = System.currentTimeMillis(); }

        if (between(p.x, p.y, r.x1, r.y1, r.x2, r.y2))
        { r.collide(p); lastCollision = System.currentTimeMillis(); }
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
        delayTime = Math.max(( long) (50 * delayTime / (score + 1)), 1);
        if (delayTime > 20) delayTime = 20;
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
            obstacleHitDust.add(new Particles(o.x, o.y, radius, false, o.cl));
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

    public static double getGravity()
    {
        return G;
    }

}
