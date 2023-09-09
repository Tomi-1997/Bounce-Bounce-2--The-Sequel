import static com.mongodb.client.model.Filters.eq;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.io.File;
import java.util.Scanner;

public class dbMediator
{
    private static String getAuth()
    {
        try
        {
            File f = new File("dbAuth.txt");
            Scanner myReader = new Scanner(f);
            return myReader.nextLine();

        }
        catch(Exception e)
        {
            return "";
        }
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
            }

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

        setHighScorer("sonya", 250);

        res = getHighScorer();
        System.out.println(res);
    }
}

