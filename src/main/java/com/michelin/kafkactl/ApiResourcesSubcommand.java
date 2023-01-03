package com.michelin.kafkactl;

import com.michelin.kafkactl.services.ApiResourcesService;
import com.michelin.kafkactl.services.LoginService;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "api-resources", description = "Print the supported API resources on the server")
public class ApiResourcesSubcommand implements Callable<Integer> {
    /**
     * API resources service
     */
    @Inject
    public ApiResourcesService apiResourcesService;

    /**
     * Login service
     */
    @Inject
    public LoginService loginService;

    /**
     * Current command
     */
    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    /**
     * Run the "api-resources" command
     * @return The command return code
     */
    @Override
    public Integer call() {
        boolean authenticated = loginService.doAuthenticate();
        if (!authenticated) {
            throw new CommandLine.ParameterException(commandSpec.commandLine(), "Login failed");
        }

        CommandLine.Help.TextTable tt = CommandLine.Help.TextTable.forColumns(
                CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO),
                new CommandLine.Help.Column(30, 2, CommandLine.Help.Column.Overflow.SPAN),
                new CommandLine.Help.Column(30, 2, CommandLine.Help.Column.Overflow.SPAN),
                new CommandLine.Help.Column(30, 2, CommandLine.Help.Column.Overflow.SPAN));
        tt.addRowValues("KIND", "NAMES", "NAMESPACED");

        apiResourcesService.getListResourceDefinition().forEach(rd ->
                tt.addRowValues(rd.getKind(), String.join(",", rd.getNames()), String.valueOf(rd.isNamespaced())));

        System.out.println(tt);
        return 0;
    }
}