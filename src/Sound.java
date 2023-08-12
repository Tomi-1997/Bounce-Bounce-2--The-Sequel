import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Sound extends TemplateObject
{
    ArrayList<String> hitSoundFName;
    public Sound(String bgSound, String hitSound)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {

        /*
            Starting background music
         */
        URL url = this.getClass().getClassLoader().getResource(bgSound);
        assert url != null;
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

        /*
            Load hit sound to play when a player hits an obstacle
         */
        hitSoundFName = new ArrayList<>();
        for (int i = 1; i <= Game.beepFiles; i++)
            load(hitSound + i + ".wav");

    }

    private void load(String s)
    {
        hitSoundFName.add(s);
    }

    @Override
    public void collide(TemplateObject to)
    {
        if (Math.random() > Game.beepProb) return;
        int index = Game.randInt(0, hitSoundFName.size() - 1);
        String s = hitSoundFName.get(index);
        try
        {
            URL url = this.getClass().getClassLoader().getResource(s);
            assert url != null;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
