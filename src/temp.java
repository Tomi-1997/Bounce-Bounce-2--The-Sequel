import edu.princeton.cs.introcs.StdDraw;

public class temp
{
    public static void main(String[] args)
    {
        Player p = new Player(0,0,0);
        System.out.println(p.getClass());
        System.exit(0);

        StdDraw.setScale(0, 500);
        int z = 30;
        for (int i = 0; i <= 500; i = i + z)
        {
            StdDraw.line(0, i, 500, i);
            StdDraw.line(i, 0, i, 500);
        }

        int x = 250, y = 250;
        int r = 10;

        for (int i = x - r; i < x + r; i ++)
        {
            for (int j = y - r; j < y + r; j++)
            {
                if ((i - x) * (i - x) + (j - y) * (j - y) > r) continue;
                StdDraw.point(i, j);
            }
        }

    }
}
