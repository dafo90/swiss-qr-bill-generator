package ch.dafo90.swissqrbillgenerator.model.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ValidationResult {

    private final List<ValidationMessage> validationMessages;

    public ValidationResult(Collection<ValidationMessage> validationMessages) {
        this.validationMessages = new ArrayList<>(validationMessages);
    }

    public ValidationResult() {
        this.validationMessages = new ArrayList<>();
    }

    public void addMessages(ValidationMessage... validationMessages) {
        this.validationMessages.addAll(List.of(validationMessages));
    }

    public void addMessages(Collection<ValidationMessage> validationMessages) {
        this.validationMessages.addAll(validationMessages);
    }

    public boolean isValid() {
        return validationMessages.isEmpty();
    }
}
