import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;


public class dbMediator
{
    private static String getAuth()
    {
        // DB User with limited permission to only find() and update() document in a certain cluster
        // Expiring soon, probably expired already
        return "mongodb+srv://BB2_User:J1BzrYun6ngGGPDS@cluster0.lkwjn6f.mongodb.net/?retryWrites=true&w=majority";
    }

    public static void updatePlayerPeriodically(dbObject dbo)
    {
        dbObject highScorerAtLaunch = getHighScorer();
        if (highScorerAtLaunch == null) return;

        dbo.setUsername(highScorerAtLaunch.getUsername());
        dbo.setScore(highScorerAtLaunch.getScore());
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
        catch(Exception e)
        {
            System.out.println("No database access");
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
        catch(Exception e)
        {
            System.out.println("No database access");
        }
        return null;
    }

    public static void main(String[] args)
    {
//        testInsert();
//        dbObject res = getHighScorer();
//        System.out.println(res);
//        setHighScorer("tomi", 50);
//        res = getHighScorer();
//        System.out.println(res);
    }

    public static void testInsert()
    {
        // Client Connection
        try (MongoClient mongoClient = MongoClients.create(getMongoSettings()))
        {
            MongoDatabase database = mongoClient.getDatabase("BB2");
            MongoCollection<Document> collection = database.getCollection("Highscore");

            Document d = new Document();
            d.put("k1", "v1");
            d.put("k2", "v2");
            collection.insertOne(d);
        }
        catch (Exception e)
        {
            System.out.println("DB Access limited, as intended");
        }
    }
}

