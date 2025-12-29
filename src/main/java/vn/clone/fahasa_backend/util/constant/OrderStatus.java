package vn.clone.fahasa_backend.util.constant;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {

    PENDING("pending"),
    AWAITING_PAYMENT("awaiting_payment"),
    CONFIRMED("confirmed"),
    PROCESSING("processing"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("cancelled"),
    RETURNED("returned");

    private final String value;

    public List<OrderStatus> nextValidStates() {
        return switch (this) {
            case PENDING, AWAITING_PAYMENT -> List.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> List.of(PROCESSING, CANCELLED);
            case PROCESSING -> List.of(SHIPPED);
            case SHIPPED -> List.of(DELIVERED);
            case DELIVERED -> List.of(RETURNED);
            case CANCELLED, RETURNED -> List.of(); // Trạng thái cuối, không thể chuyển đổi
        };
    }

    public boolean canTransitionTo(OrderStatus nextState) {
        return nextValidStates().contains(nextState);
    }

}