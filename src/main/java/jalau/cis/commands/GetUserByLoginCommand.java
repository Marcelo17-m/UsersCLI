package jalau.cis.commands;

import jalau.cis.models.User;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "-getByLogin",
    description = "Get user by login"
)
public class GetUserByLoginCommand extends Command implements Callable<Integer> {

    @CommandLine.Option(
        names = {"-login"},
        required = true,
        description = "User login"
    )
    private String login;

    @Override
    public Integer call() throws Exception {
        try {
            // VALIDACIÓN (del Swagger)
            if (login == null || login.isBlank()) {
                System.out.println("Login cannot be empty");
                return 1;
            }

            User user = getUsersService().getUserByLogin(login);

            // MANEJO DE 404 (deL Swagger)
            if (user == null) {
                System.out.printf("User with login '%s' not found\n", login);
            } else {
                System.out.println("User found:");
                System.out.println(user);
            }

            return 0;

        } catch (Exception ex) {
            System.out.printf("Internal error: %s\n", ex.getMessage());
            throw ex;
        }
    }
}