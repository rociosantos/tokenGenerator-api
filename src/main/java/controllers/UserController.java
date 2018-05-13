package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.User;
import exceptions.UnauthorizedException;
import services.UserServices;

import static spark.Spark.*;
import static util.JsonUtil.json;

public class UserController {
    public UserController(final UserServices userService) {
        get("/users", (req, res) ->
             userService.getAllUsers(), json());
// more routes

        after((req, res) -> {
            res.type("application/json");
        });

        post("/get_token", (request, response) -> {
            User user = new Gson().fromJson(request.body(), User.class);
            boolean validated = userService.validatePassword(user.getUser_name(), user.getPassword_hash());
            if (!validated) {
                throw new UnauthorizedException(user.getUser_name());
            }
            String token = userService.getEncryptedBody(user.getUser_name()).concat(".").concat(userService.encriptHMAC(user.getUser_name()));
            response.body("{\"token\": \"" + token + "\"}");
            return true;
        });

        post("/buy", (request, response) -> {
            String token = request.headers("token");
            JsonObject body = new JsonParser().parse(new String(request.body())).getAsJsonObject();

            String validated = userService.validateBuy(token, body);
           if (validated.equals("Unauthorized")) {
                throw new UnauthorizedException(body.get("user_name").getAsString());
            } else if(validated.equals("Success")){
                response.status(200);
                response.body("{\"message\":\"Buy has been completed successfully\"}");
            } else {
                response.status(403);
                response.body("{\"message\":\"User is not authorized to buy" + validated + " items\"}");
           }
           return true;
        });

        exception(UnauthorizedException.class, (exception, request, response)  -> {
            response.status(401);
            response.body("{\"message\":\"Unauthorized\"}");
        });
    }
}

