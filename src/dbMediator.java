import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;


public class dbMediator
{
    private static String getAuth()
    {
        return "mongodb+srv://BB2_User:J1BzrYun6ngGGPDS@cluster0.lkwjn6f.mongodb.net/?retryWrites=true&w=majority";
    }

    public static void updatePlayerPeriodically(dbObject dbo)
    {
        String auth = getAuth();
        if (auth.length() == 0) return;

        dbObject highScorerAtLaunch = getHighScorer();
        if (highScorerAtLaunch != null)
        {
            dbo.setUsername(highScorerAtLaunch.getUsername());
            dbo.setScore(highScorerAtLaunch.getScore());
        }

        MongoClientSettings settings = getMongoSettings();

        // Client Connection
        try (MongoClient mongoClient = MongoClients.create(settings))
        {
            MongoDatabase database = mongoClient.getDatabase("BB2");
            MongoCollection<Document> collection = database.getCollection("Highscore");

            ChangeStreamIterable<Document> changeStream = collection.watch();
            changeStream.forEach(
                    update ->
                    {
                        Document d = update.getFullDocument();
                        if (d == null) return;

                        String name = d.getString("username");
                        int score = Integer.parseInt(d.getString("score"));

                        dbo.setUsername(name);
                        dbo.setScore(score);
                    }
            );

        }
    }

    private static MongoClientSettings getMongoSettings()
    {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(getAuth()))
                .serverApi(serverApi)
                .build();
    }

    public static void setHighScorer(String username, int score)
    {
        String auth = getAuth();
        if (auth.length() == 0) return;
        MongoClientSettings settings = getMongoSettings();

        // Client Connection
        try (MongoClient mongoClient = MongoClients.create(settings))
        {
            MongoDatabase database = mongoClient.getDatabase("BB2");
            MongoCollection<Document> collection = database.getCollection("Highscore");

            for (Document d : collection.find())
            {
                Document newD = new Document(d);
                newD.replace("username", username);
                newD.replace("score", score+"");

                collection.replaceOne(d, newD);
                break;
            }

        }
    }

    public static dbObject getHighScorer()
    {
        String auth = getAuth();
        if (auth.length() == 0) return null;
        MongoClientSettings settings = getMongoSettings();

        // Client Connection
        try (MongoClient mongoClient = MongoClients.create(settings))
        {
            MongoDatabase database = mongoClient.getDatabase("BB2");
            MongoCollection<Document> collection = database.getCollection("Highscore");

            for (Document d : collection.find())
            {
                String name = d.getString("username");
                int score = Integer.parseInt(d.getString("score"));
                return new dbObject(name, score);
            }
        }
        return null;
    }

    public static void main(String[] args)
    {
        dbObject res = getHighScorer();
        System.out.println(res);

        setHighScorer("tomi", 0);

        res = getHighScorer();
        System.out.println(res);
    }
}

