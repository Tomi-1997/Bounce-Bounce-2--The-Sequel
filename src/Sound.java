import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Sound extends TemplateObject
{
    String hitSoundFName;
    public Sound(String bgSound, String hitSound)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        hitSoundFName = hitSound;
        /*
            Starting background music
         */
        URL url = this.getClass().getClassLoader().getResource(bgSound);
        assert (url != null);

        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void beep()
    {
        /*
            Randomly abort (don't make sound)
         */
        if (Math.random() > Game.getInstance().getBeepProb()) return;

        /*
            Choose random sound file
         */
        int index = Game.getInstance().randInt(1, Game.getInstance().getBeepFiles());
        try
        {
            URL url = this.getClass().getClassLoader().getResource(hitSoundFName + index + ".wav");
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
