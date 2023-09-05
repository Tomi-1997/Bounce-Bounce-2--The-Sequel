import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class Game
{
    //TODO 
    // finish player map collisions functions
    // move collision functions to entities

    private static Game myGame;
    private Game()
    {
        TO = Collections.synchronizedCollection(new ArrayList<>());
        createPlayer();
        createObstacles((int) (maxX * 0.25), maxY / 2);
        createInformation();
        createSound();
        createLaunchPads();
    }

    public static synchronized Game getInstance()
    {
        if (myGame == null)
            myGame = new Game();

        return myGame;
    }

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
            Obstacle o = new Obstacle(x, y, w / 2.0, h / 2.0, speed);
            TO.add(o);

            x = x + randInt(150, 200);
            y = y + randInt(-30, 30);
        }
    }

    private void createPlayer()
    {
        int playerRadius = Math.min(maxX, maxY) / 80;
        Player p = new Player(0, maxY / 2.0, playerRadius);
        TO.add(p);
        this.p = p;
    }

    private void createSound()
    {
        try
        {
            sound = new Sound("Minibit.wav", "beep");
        }
        catch (Exception e)
        {
            sound = new TemplateObject();
            e.printStackTrace();
            hasMusic = false;
        }
    }

    private void createLaunchPads()
    {
        l = new LaunchPad(maxX * 0.025, maxY * 0.25, maxY * 0.1, maxX);
        r = new LaunchPad(maxX * 0.975, maxY * 0.25, maxY * 0.1, maxX);

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
        score = score + scorePassiveGain;
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
        for (int i = 5; i >= 1; i--)
        {
            final String lambdaText = Integer.toString(i);
            int finalI = i;
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
                    setFontSize(100 - finalI * 10);
                    StdDraw.text(maxX / 2.0, maxY / 2.0, lambdaText);
                }
            };

            addFor(d, duration);
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

    private void addFor(TemplateObject d, int duration)
    {
        add(d);
        delay(duration);
        rm(d);
    }

    public void setFontSize(int titleSize)
    {
        Font font = new Font("Monospaced", Font.BOLD, titleSize);
        StdDraw.setFont(font);
    }

    private boolean isRunning()
    {
        //
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

        if (p.checkBelow()) resetScore();
        p.checkAbove();
        p.checkSides();
//
//        synchronized (TO) { for (TemplateObject to : TO) collide(p, to); }

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

                score += scoreHitReward;
                sound.collide(null);
                generateDust(o);

                hitObstacle = true;
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
        new Thread( () ->
        {
            /*
                Mark at reset, stop passive score gain
             */
            double oldGain = scorePassiveGain;
            isResetting = true;
            scorePassiveGain = 0;
            hitObstacle = false;

            /*
                Slowly begin to subtract from score, accelerate abruptly after a few iterations
             */
            for (int i = 1; score >= 0 && !hitObstacle ; i++)
            {
                long delayTime = (long)(Math.max(1, 100 - i * Math.sqrt(i)));
                delay(delayTime);
                score--;
            }

            /*
                If didn't hit obstacle - Set score = 0, it might be negative, set passive gain positive again
             */
            if (!hitObstacle) score = 0;
            scorePassiveGain = oldGain;
            isResetting = false;
        }).start();
    }

    public void delay()
    {
        //
        try {Thread.sleep(FPS);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public void delay(long millis)
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
        delay(3 * 1000);
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

    public int randInt(int a, int b)
    {
        //
        return (int) (Math.random() * (b - a)) + a;
    }

    public double getGravity()
    {
        return G;
    }

    public int getMaxX() {
        return maxX;
    }

    public double getMinVY() {
        return minVY;
    }

    public double getMaxVY() {
        return maxVY;
    }

    public double getMaxVX() {
        return maxVX;
    }

    public double getVX() {
        return VX;
    }

    public double getHitVY() {
        return hitVY;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getScore() {
        return score;
    }

    public double getBeepProb() {
        return beepProb;
    }

    public int getBeepFiles() {
        return beepFiles;
    }

    public boolean hasMusic() {
        return hasMusic;
    }

    public double getPenR()
    {
        return penR;
    }

    private final Collection<TemplateObject> TO;
    private Player p;
    private TemplateObject sound;
    private LaunchPad l, r;

    private final long FPS = 1000 / 60;
    private final double G = 0.15;
    private final int obstacles = 10, maxX = 800, maxY = 400;

    private final double minVY = -10, maxVY = 7;
    private final double maxVX = 4;
    private final double VX = 0.2;
    private final double hitVY = 5;
    final double baseSpeed = 1.5;
    private double speedMultiplier = 0.005;
    private final double maxSpeed = 5;
    private double lastCollision = 0;
    private double penR = 0.005;
    private final double beepProb = 0.2;

    private double score = 0;
    private double scorePassiveGain = 0.1;
    private final int scoreHitReward = 10;

    private final int beepFiles = 5;
    private boolean hasMusic = true;
    private boolean regenAvailable = true;
    private boolean isResetting = false;
    private boolean hitObstacle = false;


    public int getMaxY()
    {
        return maxY;
    }

    public void collide(Player p, TemplateObject to)
    {}

    public static Color getRandColor()
    {
        /*
            Generate a random colour
         */
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);

        /*
            If needed, brighten
         */
        while (r + g + b < 200)
        {
            r = r + 20;
            g = g + 20;
            b = b + 5;
        }
        return new Color(r, g, b);
    }
}
