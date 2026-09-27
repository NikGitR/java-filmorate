package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator
        implements ConstraintValidator<ValidReleaseDate, LocalDate> {

    private static final LocalDate FIRST_FILM_DATE =
            LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(
            LocalDate releaseDate,
            ConstraintValidatorContext context
    ) {
        return releaseDate == null
                || !releaseDate.isBefore(FIRST_FILM_DATE);
    }
}
