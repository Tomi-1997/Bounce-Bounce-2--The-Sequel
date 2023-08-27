import edu.princeton.cs.introcs.StdDraw;

import java.util.ArrayList;

public class Information extends TemplateObject
{
    double maxX, maxY;
    Player p;

    ArrayList<Particles> particles;
    int titleSize = 60, textSize = 12, currentMilestone = 0, milestoneInc = 100;
    boolean atMilestone = false;

    public Information(double maxX, double maxY, Player p)
    {
        this.maxX = maxX;
        this.maxY = maxY;
        this.p = p;
    }

    @Override
    public void draw()
    {
        StdDraw.setPenColor(p.cl);

        /*
            Title
         */
        Game.getInstance().setFontSize(titleSize);
        String title = (int) Game.getInstance().getScore() + "";
        StdDraw.text(maxX / 2, maxY / 10, title);

        /*
            Confetti - when a milestone is hit
         */
        if (atMilestone) for (Particles f : particles) f.draw();

        /*
            Sub text
         */
        Game.getInstance().setFontSize(textSize);
        StdDraw.textLeft(-maxX * 0.02, maxY * 0.05, "hold right and left arrow keys to move");
        StdDraw.textLeft(-maxX * 0.02, 0, "press q to quit, press r to restart the level");
        StdDraw.textLeft(maxX * 0.85, 0, "high score - " + currentMilestone);
        if (Game.getInstance().hasMusic()) StdDraw.textRight(maxX, maxY, "Music by Tom Pfeifel");
    }

    @Override
    public void update()
    {
        /*
            Not in middle of celebration AND passed latest milestone
         */
        if (!atMilestone && Game.getInstance().getScore() >= currentMilestone + milestoneInc)
            new Thread(this::setMilestone).start();

        /*
            Update confetti
         */
        if (atMilestone) for (Particles prt : particles) prt.update();
    }

    private void setMilestone()
    {
        generateConfetti(); // launch fireworks when reaching milestone
        atMilestone = true;
        titleSize *= 2;    // big title
        currentMilestone = currentMilestone + milestoneInc; // Set next milestone
        Game.getInstance().delay(4 * 1000); // wait a bit, then revert to normal
        atMilestone = false;
        titleSize /= 2;
    }

    private void generateConfetti()
    {
        int fireworksNum = (int) (Math.min(maxX, maxY) / 2);
        double radius = Math.min(maxX, maxY) / 20;
        particles = new ArrayList<>();
        for (int i = 0; i < fireworksNum; i++)
        {
            particles.add(new Particles(maxX / 2, maxY / 10, radius, true, Game.getRandColor()));
        }
    }

    @Override
    public void reset()
    {
        //
        currentMilestone = 0;
    }
}
