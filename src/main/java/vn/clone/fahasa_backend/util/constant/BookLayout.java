package vn.clone.fahasa_backend.util.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BookLayout {
    HARDCOVER("hardcover"), PAPERBACK("paperback"), CARDS("cards");

    private final String value;
}
