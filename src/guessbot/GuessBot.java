package guessbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GuessBot {
    private final String CLIENT_ID = "a18d5b29f2ffd08";
    private final String CLIENT_SECRET = "befea6099cdecfba29820866a4cfcc3643134f41";
    private final String OAUTH_URL = "https://api.imgur.com/oauth2/";
    private final String COMMENT_URL = "https://api.imgur.com/3/comment";
    private final String GALLERY_URL = "https://api.imgur.com/3/gallery/user/rising";
    private final String IMG_URL = "http://i.imgur.com/";
    private final String TEST_HASH = "zUNbcbu";
    private String access;
    private String refresh;
    private Calendar expiration;
    
    public static void main(String[] args) {
        GuessBot gBot = new GuessBot();
        gBot.run();
    }

    public void run() {
        readTokens();
        if(needRefresh()) {
            refreshTokens();
            readTokens();
        }
        guessGallery();
    }
    public void guessGallery() {
        try {
            HttpGet httpGet = new HttpGet(GALLERY_URL); 
            httpGet.addHeader("Authorization","Bearer "+access);
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity result = response.getEntity();
            InputStream out = result.getContent();
            Scanner scanner = new Scanner(out);
            String responseBody = scanner.useDelimiter("\\A").next();
            scanner.close();
            System.out.println(responseBody);
            JsonParser jParser = new JsonParser();
            JsonArray galleryJson = jParser.parse(responseBody).getAsJsonObject().get("data").getAsJsonArray();
            int i = 0;
            for(JsonElement jE: galleryJson) {
                JsonObject jO = jE.getAsJsonObject(); 
                if (!jO.get("is_album").getAsBoolean()) {
                    comment(jO.get("id").getAsString(), "I think I see "+GoogleRIS.getGuess(jO.get("link").getAsString()));
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void printGallery(){
        try {
            HttpGet httpGet = new HttpGet(GALLERY_URL); 
            httpGet.addHeader("Authorization","Bearer "+access);
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity result = response.getEntity();
            InputStream out = result.getContent();
            Scanner scanner = new Scanner(out);
            String responseBody = scanner.useDelimiter("\\A").next();
            scanner.close();
            System.out.println(responseBody);
            JsonParser jParser = new JsonParser();
            JsonArray galleryJson = jParser.parse(responseBody).getAsJsonObject().get("data").getAsJsonArray();
            int i = 0;
            for(JsonElement jE: galleryJson) {
                JsonObject jO = jE.getAsJsonObject(); 
                System.out.println(i+": "+jO.get("link").getAsString()+" "+jO.get("is_album").getAsString());
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void comment(String id, String c) {
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("image_id", id)
                    .addTextBody("comment", c).build();
            HttpPost httpPost = new HttpPost(COMMENT_URL);
            httpPost.setEntity(entity);  
            httpPost.addHeader("Authorization","Bearer "+access);
            HttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readTokens() {
        try {
            BufferedReader tokensFile = new BufferedReader(new FileReader("tokens.txt"));
            String onRecord = tokensFile.readLine();
            expiration = Calendar.getInstance();
            expiration.setTimeInMillis((Long.parseLong(tokensFile.readLine())));
//            System.out.println("On record: "+onRecord);
            tokensFile.close();
            JsonObject record = new JsonParser().parse(onRecord).getAsJsonObject();
            access = record.get("access_token").getAsString();
            refresh = record.get("refresh_token").getAsString();
//            System.out.println("access: "+access);
//            System.out.println("refresh: "+refresh);
            System.out.println("tokens expire on " + expiration.getTime());
            System.out.println("right now it is "+Calendar.getInstance().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean needRefresh() {
        return Calendar.getInstance().after(expiration);
    }
    private void refreshTokens() {
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("refresh_token", refresh)
                    .addTextBody("client_id", CLIENT_ID)
                    .addTextBody("client_secret", CLIENT_SECRET)
                    .addTextBody("grant_type", "refresh_token").build();
            HttpPost httpPost = new HttpPost(OAUTH_URL+"token");
            httpPost.setEntity(entity);  
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity result = response.getEntity();
            InputStream out = result.getContent();
            Scanner scanner = new Scanner(out);
            String responseBody = scanner.useDelimiter("\\A").next();
            scanner.close();
            BufferedWriter tokenWriter = new BufferedWriter(new FileWriter("tokens.txt"));
            Calendar refreshedExpiration = Calendar.getInstance();
            refreshedExpiration.add(Calendar.SECOND, new JsonParser().parse(responseBody)
                    .getAsJsonObject().get("expires_in").getAsInt());
            tokenWriter.write(responseBody+"\n"+refreshedExpiration.getTimeInMillis());
            tokenWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
