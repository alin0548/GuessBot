package guessbot;
import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GoogleRIS {
    private final static String UP_URL = "https://www.google.com/searchbyimage/upload";
    private final static String URL_URL = "https://www.google.com/searchbyimage?&image_url=";
    public static String getGuess(String filePath, String fileName) {
        String keyUrl = "";
        try {
            HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("encoded_image", new File(filePath), ContentType.create("application/octet-stream"), fileName).build();
            HttpPost httpPost = new HttpPost(UP_URL);
            httpPost.setEntity(entity);  
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity result = response.getEntity();
            InputStream out = result.getContent();
            Scanner scanner = new Scanner(out);
            String responseBody = scanner.useDelimiter("\\A").next();
            Document doc = Jsoup.parse(responseBody);
            keyUrl = doc.getElementsByAttribute("HREF").attr("HREF");
            Document doc2 = Jsoup.connect(keyUrl).get();
            return doc2.getElementsByClass("_gUb").text();
            //System.out.println(doc2.html());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("keyUrl" + keyUrl);
            e.printStackTrace();
            return "";
        }
    }
    public static String getGuess(String imgURL) {
        String keyUrl = "";
        try {
            Document doc2 = Jsoup.connect(URL_URL+imgURL).get();
            return doc2.getElementsByClass("_gUb").text();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("keyUrl" + keyUrl);
            e.printStackTrace();
            return "";
        }
    }
}
