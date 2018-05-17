package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import entities.User;
import org.json.XML;
import org.json.simple.JSONObject;
import repository.UserRepo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserServices {
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
            "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";

    public static UserRepo repo;

    public UserServices(UserRepo repo){
        this.repo = repo;
    }


    // returns a list of all users
    public List<User> getAllUsers() throws FileNotFoundException {
      return repo.readUserFile();
//        return allUsers ;
    }

    //encodes password received and compares with password from json file
    public boolean validatePassword(String userName, String password) throws NoSuchAlgorithmException, FileNotFoundException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.getEncoder().encodeToString(hash);
        User user = findUser(userName);
        System.out.println(encoded);
        return encoded.equals(user.getPassword_hash());
    }

    //find user by user name
    public User findUser(String userName) throws FileNotFoundException {
        List<User> allUsers = repo.readUserFile();
        List<User> result = allUsers.stream()
                .filter(item -> Objects.equals(item.getUser_name(), userName))
                .collect(Collectors.toList());
        return result.get(0);
    }

    //creates body from json file and encrypts it
    public String getEncryptedBody(String userName) throws FileNotFoundException, NoSuchAlgorithmException {
        User user = findUser(userName);
        JSONObject body = new JSONObject();
        body.put("user_name", user.getUser_name());
        body.put("milk", user.milk);
        body.put("bread", user.bread);
        body.put("butter", user.butter);

        return Base64.getEncoder().encodeToString(body.toString().getBytes(StandardCharsets.UTF_8));

    }

    public String getEnclosedUserName(String userName){
        return Base64.getEncoder().encodeToString(userName.getBytes(StandardCharsets.UTF_8));
    }

    //encrypts with HMAC the body plus the secret
    public String encriptHMAC(String userName) throws FileNotFoundException, NoSuchAlgorithmException {
        String key = getEncryptedBody(userName).concat(repo.secret);
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return new String(HexBin.encode(sha256_HMAC.doFinal(key.getBytes("UTF-8"))));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //validates the token received from header
    public boolean validateTokenForBuy(String[] encryptedItems, JsonObject decryptedBody) throws FileNotFoundException, NoSuchAlgorithmException {

        String userName = decryptedBody.get("user_name").getAsString();
        String encodedSecret = encryptedItems[1];

        return validateEncryptedSecret(encodedSecret, userName);
    }

    //compares the HMAC encrypted received with the existent
    public boolean validateEncryptedSecret(String encodedSecret, String userName) throws FileNotFoundException, NoSuchAlgorithmException {
        return encodedSecret.equals(encriptHMAC(userName));
    }

    //verifies the user is allowed for complete the buy
    public String validateBuy(String token, JsonObject body) throws FileNotFoundException, NoSuchAlgorithmException {
        String[] encryptedItems = token.split("\\.");
        JsonObject decryptedBody = decodeBody(encryptedItems[0]);

        if(validateTokenForBuy(encryptedItems, decryptedBody)){
            List<String> itemsCannotBuy = getItemsCannotBuy(body, decryptedBody.get("user_name").getAsString());
            if(!itemsCannotBuy.isEmpty()){
                return itemsCannotBuy.toString();
            } else {
                return "Success";
            }
        } else {
            return "Unauthorized";
        }
    }

    //verifies what items the user is allowed to buy
    public List<String> getItemsCannotBuy(JsonObject body, String userName) throws FileNotFoundException {
        User userFromDB = findUser(userName);
        List<String> itemsCannotBuy = new ArrayList<>();

        if(body.has("milk") && body.get("milk").getAsInt()>0){
            if(!userFromDB.isMilk()){
                itemsCannotBuy.add("milk");
            }
        }

        if(body.has("bread") && body.get("bread").getAsInt()>0) {
            if(!userFromDB.isBread()){
                itemsCannotBuy.add("bread");
            }
        }

        if(body.has("butter") && body.get("butter").getAsInt()>0) {
            if(!userFromDB.isButter()){
                itemsCannotBuy.add("butter");
            }
        }
        return itemsCannotBuy;
    }

    //get JsonObject from encoded Body
    public static JsonObject decodeBody(String encryptedBody){
        byte[] decoded = Base64.getDecoder().decode(encryptedBody);
        JsonObject body = new JsonParser().parse(new String(decoded)).getAsJsonObject();
        return body;
    }

    //gets UserName from token encoded user name
    public static String decodeUserName(String encodedUserName){
        byte[] decoded = Base64.getDecoder().decode(encodedUserName);
        String userName = (new String(decoded));
        return userName;
    }

    //validates conversion token, which is formed by userName.encryptedHMAC
    public boolean validConversionToken(String token) throws FileNotFoundException, NoSuchAlgorithmException {
        String[] encryptedItems = token.split("\\.");
        String userName = decodeUserName(encryptedItems[0]);

        String encodedSecret = encryptedItems[1];

        return validateEncryptedSecret(encodedSecret, userName) ;
    }

    //converts xml body to Json
    public static String xmlToJson(String xml){
//        try {
            org.json.JSONObject xmlJSONObj = XML.toJSONObject(xml);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            return jsonPrettyPrintString;
//        } catch (JSONException je) {
//            System.out.println(je);
//            return je.getMessage();
//            throw new JSONException("Error converting xml");
//        }
        //panic handler - cuando es una excepcion distinta a la que estoy cachando, regrese 500
    }

}