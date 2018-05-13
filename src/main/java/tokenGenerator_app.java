import controllers.UserController;
import services.UserServices;

public class tokenGenerator_app {
    public static void main(String[] args) {
        //lista de objetos user, mandarselos al constructor
        //inyeccion de dependencias
        //concurrencia - lock
        new UserController(new UserServices());
    }
}
