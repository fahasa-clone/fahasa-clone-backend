package vn.clone.fahasa_backend.util.constant;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShippingMethod {
    STANDARD("standard"),
    EXPRESS("express");

    private final String value;
}
