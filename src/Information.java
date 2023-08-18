import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.util.ArrayList;

public class Information extends TemplateObject
{
    double maxX, maxY;
    Player p;

    ArrayList<Particles> particles;
    int titleSize = 60, currentMilestone = 0, milestoneInc = 100;
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
        Font font = new Font("Monospaced", Font.BOLD, titleSize);
        StdDraw.setFont(font);
        String title = (int) Game.score + "";
        StdDraw.text(maxX / 2, maxY / 10, title);

        /*
            Fireworks - when a milestone is hit
         */

        if (atMilestone) for (Particles f : particles) f.draw();

        /*
            Sub text
         */
        font = new Font("Monospaced", Font.BOLD, 12);
        StdDraw.setFont(font);
        StdDraw.textLeft(-Game.maxX * 0.02, Game.maxY * 0.05, "hold right and left arrow keys to move");
        StdDraw.textLeft(-Game.maxX * 0.02, 0, "press q to quit, press r to restart the level");
        StdDraw.textLeft(maxX * 0.85, 0, "high score - " + currentMilestone);
        if (Game.hasMusic) StdDraw.textRight(maxX, maxY, "Music by Tom Pfiefel");
    }

    @Override
    public void update()
    {
        Game.score += 0.1;
        if (!atMilestone && Game.score >= currentMilestone + milestoneInc) new Thread(this::setMilestone).start();

        /*
            Update fireworks
         */
        if (atMilestone) for (Particles prt : particles) prt.update();
    }

    private void setMilestone()
    {
        generateFireworks(); // launch fireworks when reaching milestone
        atMilestone = true;
        titleSize = 100;    // big title
        currentMilestone = currentMilestone + milestoneInc; // Set next milestone
        Game.delay(3 * 1000); // wait a bit, then revert to normal
        atMilestone = false;
        titleSize = 60;
    }

    private void generateFireworks()
    {
        int fireworksNum = 200;
        double radius = 10;
        particles = new ArrayList<>();
        for (int i = 0; i < fireworksNum; i++)
        {
            particles.add(new Particles(maxX / 2, maxY / 10, radius, true));
        }
    }

    @Override
    public void reset()
    {
        currentMilestone = 0;
    }
}
