package bg.sofia.uni.fmi.mjt.splitwise.server.command.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterContainer {
    private List<ParameterHelp> data;

    public ParameterContainer() {
        this.data = new ArrayList<>();
    }

    public List<ParameterHelp> data() {
        return Collections.unmodifiableList(data);
    }

    boolean isEmpty() {
        return data.isEmpty();
    }

    public void addParameter(String name, String description, boolean isOptional) {
        if (!data.isEmpty() && data.getLast() instanceof VariableParameterHelp) {
            throw new IllegalArgumentException("You've already added a variable argument, therefore you cannot add any new arguments");
        }
        data.add(new ParameterHelp(name, description, isOptional));
    }

    public void addVariableParameter(String name, String description, int minAmount, boolean isOptional) {
        if (!data.isEmpty() && data.getLast() instanceof VariableParameterHelp) {
            throw new IllegalArgumentException("You've already added a variable argument, therefore you cannot add any new arguments");
        }
        data.add(new VariableParameterHelp(name, description, minAmount, isOptional));
    }
}
