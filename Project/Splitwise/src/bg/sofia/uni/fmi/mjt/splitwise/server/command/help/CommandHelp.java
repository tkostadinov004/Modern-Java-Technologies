package bg.sofia.uni.fmi.mjt.splitwise.server.command.help;

public record CommandHelp(String name, String description, ParameterContainer parameters) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" - ");
        sb.append(description);
        sb.append(System.lineSeparator());

        if (!parameters.isEmpty()) {
            sb.append("Parameters: ");
            sb.append(System.lineSeparator());

            parameters
                    .data()
                    .forEach(param -> sb.append(param).append(System.lineSeparator()));
        }
        return sb.toString();
    }
}
