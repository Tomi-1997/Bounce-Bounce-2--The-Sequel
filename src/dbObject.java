public class dbObject
{
    private String username;
    private int score;

    public dbObject(String username, int score)
    {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public void setUsername(String username)
    {
        if (username.length() == 0) return;
        this.username = username;
    }

    public void setScore(int score)
    {
        if (score < 0) return;
        this.score = score;
    };

    public String toString()
    {
        return "Username - " + username + ", score - " + score;
    }

}
