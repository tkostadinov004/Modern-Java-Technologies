package bg.sofia.uni.fmi.mjt.splitwise.server.command.help;

public class VariableParameterHelp extends ParameterHelp {
    private int minAmount;

    public VariableParameterHelp(String name, String description, int minAmount, boolean isOptional) {
        super(name, description, isOptional);
        this.minAmount = minAmount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < minAmount; i++) {
            sb.append("\t* ")
                    .append(super.isOptional() ? "OPTIONAL: " : "")
                    .append(super.name())
                    .append(i + 1)
                    .append(" - ")
                    .append(super.description())
                    .append(System.lineSeparator());
        }
        sb.append("...").append(System.lineSeparator());
        sb.append(super.isOptional() ? "OPTIONAL: " : "")
                .append(super.name())
                .append("N - ")
                .append(super.description());

        return sb.toString().stripTrailing();
    }
}
