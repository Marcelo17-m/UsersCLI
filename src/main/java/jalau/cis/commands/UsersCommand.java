package jalau.cis.commands;

import jalau.cis.services.ServicesFacade;
import picocli.CommandLine;


import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "users",
    description = "CRUD on a Users DB",
    subcommands = {
        ReadUsersCommand.class,
        DeleteUserCommand.class,
        CreateUserCommand.class,
        UpdateUserCommand.class,
        GetUserByLoginCommand.class  //dice unknown type
    }
)
public class UsersCommand implements Callable<Integer> {

    @CommandLine.Option(
        description = "Configuration File (xml)",
        required = true,
        names = {"-config"}
    )
    private String configuration;

    @Override
    public Integer call() throws Exception {
        System.out.println("Running User CRUD...");
        System.out.printf("Loading configuration from [%s]\n", configuration);

        ServicesFacade.getInstance().init(configuration);
        return 0;
    }
}