package bg.sofia.uni.fmi.mjt.splitwise.server.command.help;

public class ParameterHelp {
    private String name;
    private String description;
    private boolean isOptional;

    public ParameterHelp(String name, String description, boolean isOptional) {
        this.name = name;
        this.description = description;
        this.isOptional = isOptional;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t* ");
        if (isOptional) {
            sb.append("OPTIONAL: ");
        }
        sb.append(name);
        sb.append(" - ");
        sb.append(description);
        return sb.toString().stripTrailing();
    }
}
