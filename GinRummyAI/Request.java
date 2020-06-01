import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Request {

  String destination;


  public Request(String destination) {
    this.destination = destination;
  }


  public static String getParamsString(Map<String, String> params)
    throws UnsupportedEncodingException{
      StringBuilder result = new StringBuilder();

      for (Map.Entry<String, String> entry : params.entrySet()) {
        result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
        result.append("=");
        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        result.append("&");
      }

      String resultString = result.toString();
      return resultString.length() > 0
        ? resultString.substring(0, resultString.length() - 1)
        : resultString;
  }


  public double sendFeatures(String featuresStr) throws IOException {
    HashMap<String, String> values = new HashMap<>();
    values.put("features", featuresStr);

    URL url = new URL(destination);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");


    con.setDoOutput(true);
    DataOutputStream out = new DataOutputStream(con.getOutputStream());
    out.writeBytes(getParamsString(values));
    out.flush();
    out.close();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    double retVal = Double.parseDouble(in.readLine());
    in.close();

    return retVal;
  }





}
