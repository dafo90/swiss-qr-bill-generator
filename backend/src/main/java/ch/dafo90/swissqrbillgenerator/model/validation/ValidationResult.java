package ch.dafo90.swissqrbillgenerator.model.validation;

import ch.dafo90.swissqrbillgenerator.model.Base64Image;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ValidationResult {

    private final List<ValidationMessage> validationMessages;
    private final Base64Image logo;

    public ValidationResult(Base64Image logo, Collection<ValidationMessage> validationMessages) {
        this.logo = logo;
        this.validationMessages = new ArrayList<>(validationMessages);
    }

    public ValidationResult() {
        this.logo = Base64Image.empty();
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
