package vn.clone.fahasa_backend.annotation.validator;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import vn.clone.fahasa_backend.annotation.MinForList;

public class MinForListValidator implements ConstraintValidator<MinForList, List<Integer>> {

    int min;

    @Override
    public void initialize(MinForList annotation) {
        min = annotation.value();
    }

    @Override
    public boolean isValid(List<Integer> list, ConstraintValidatorContext context) {
        if (list.isEmpty()) {
            return false;
        }

        // Check if all elements are >= min
        return list.stream()
                   .allMatch(value -> value != null && value >= this.min);
    }
}
