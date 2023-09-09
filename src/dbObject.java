public class dbObject
{
    private final String username;
    private final int score;

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

    public String toString()
    {
        return "Username - " + username + ", score - " + score;
    }

}
