package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.User;
import exceptions.UnauthorizedException;
import org.json.JSONException;
import services.UserServices;

import static spark.Spark.*;
import static util.JsonUtil.json;

public class UserController {
    public UserController(final UserServices userService) {
        get("/users", (req, res) ->
             userService.getAllUsers(), json());
// more routes

        get("/users/:userName", ((request, response) ->
        userService.findUser(request.params(":userName"))), json());

        after((req, res) -> {
            res.type("application/json");
        });

        post("/get_token", (request, response) -> {
            User user = new Gson().fromJson(request.body(), User.class);
            boolean validated = userService.validatePassword(user.getUser_name(), user.getPassword_hash());
            if (!validated) {
                response.status(401);
                response.body("{\"message\":\"Unauthorized\"}");
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

        post("/convert_get_token", (request, response) -> {
            User user = new Gson().fromJson(request.body(), User.class);
            boolean validated = userService.validatePassword(user.getUser_name(), user.getPassword_hash());
            if (!validated) {
                throw new UnauthorizedException(user.getUser_name());
            }
            String token = userService.getEnclosedUserName(user.getUser_name()).concat(".").concat(userService.encriptHMAC(user.getUser_name()));
            response.body("{\"token\": \"" + token + "\"}");
            return true;
        });

        post("/convert", (request, response) -> {
            String token = request.headers("token");
            String body = request.body();
            boolean validContentType = request.contentType().equals("application/xml");

            if (validContentType) {
                if (userService.validConversionToken(token)) {
                    try{
                        String convertedBody= userService.xmlToJson(body);
                        response.status(200);
                        response.body(convertedBody);
                    } catch (JSONException je){
                        System.out.println(je);
                        response.status(400);
                        response.body("{\"message\":\"Invalid xml\"}");
                    }

                } else {
                    response.status(401);
                    response.body("{\"message\":\"Unauthorized\"}");
                }
            } else {
                response.status(406);
                response.body("{\"message\":\"Content type is not accepted\"}");
            }
            //regresar la excepción del error al convertir el xml
            return true;
        });

        exception(UnauthorizedException.class, (exception, request, response)  -> {
            response.status(401);
            response.body("{\"message\":\"Unauthorized\"}");
        });
    }
}

