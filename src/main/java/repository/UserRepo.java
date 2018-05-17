package repository;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import entities.User;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class UserRepo {

    public final String secret = "myGeneralSecret";
    //read json file
    public static List<User> readUserFile() throws FileNotFoundException {
        JsonElement usersJson = new JsonParser().parse(new FileReader("src/main/resources/users.json"));
        JsonElement userData = usersJson.getAsJsonObject().get("users");
        List<User> allUsers = new Gson().fromJson(userData, new TypeToken<List<User>>() {
        }.getType());
        return allUsers;
    }
}
