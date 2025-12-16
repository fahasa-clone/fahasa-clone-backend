package vn.clone.fahasa_backend.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.clone.fahasa_backend.service.payment.VnPayService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VnPayService vnPayService;

    @GetMapping("/api/vnpay")
    public ResponseEntity<String> getVnPayPaymentUrl(HttpServletRequest request,
                                                     @RequestParam("orderId") int orderId,
                                                     @RequestParam("amount") long amount) {
        String ip = vnPayService.getIpAddress(request);
        String paymentUrl = vnPayService.generatePaymentUrl(orderId, amount, ip);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/api/payment/vnpay/return")
    public ResponseEntity<String> handleVnPayReturnUrl(HttpServletRequest request,
                                                       @RequestParam("vnp_ResponseCode") String responseCode,
                                                       @RequestParam("vnp_TxnRef") int orderId) {
        if (!vnPayService.checkSecureHash(request)) {
            return ResponseEntity.badRequest().body("Invalid secure hash");
        }
        return null;
    }

    @GetMapping("/IPN")
    public ResponseEntity<IpnHandlerResponse> handleVnPayIPN(HttpServletRequest request,
                                                             @RequestParam("vnp_ResponseCode") String responseCode,
                                                             @RequestParam("vnp_TxnRef") int orderId,
                                                             @RequestParam("vnp_Amount") int amount) {
        IpnHandlerResponse handlerResponse = new IpnHandlerResponse();

        // 1. Check checksum
        if (!vnPayService.checkSecureHash(request)) {
            log.info("Invalid Checksum");

            handlerResponse.setResponseCode("97");
            handlerResponse.setMessage("Invalid Checksum");

            return ResponseEntity.ok(handlerResponse);
        }

        // 2. Check order's existence
        if (orderId == 0) {
            log.info("Order not Found");

            handlerResponse.setResponseCode("01");
            handlerResponse.setMessage("Order not Found");

            return ResponseEntity.ok(handlerResponse);
        }

        // 3. Check amount
        if (amount == 0) {
            log.info("Invalid Amount");

            handlerResponse.setResponseCode("04");
            handlerResponse.setMessage("Invalid Amount");

            return ResponseEntity.ok(handlerResponse);
        }

        // 4. Check order status
        if (false) {
            log.info("Order already confirmed");

            handlerResponse.setResponseCode("02");
            handlerResponse.setMessage("Order already confirmed");

            return ResponseEntity.ok(handlerResponse);
        }

        // 5. Update order status if transaction is successful
        if (responseCode.equals("00")) {
            log.info("Update order status");
        }

        // 6. Confirm order
        log.info("Confirm Success");

        handlerResponse.setResponseCode("00");
        handlerResponse.setMessage("Confirm Success");

        return ResponseEntity.ok(handlerResponse);
    }

    @Setter
    public static class IpnHandlerResponse {

        @JsonProperty("RspCode")
        private String responseCode;

        @JsonProperty("Message")
        private String message;
    }
}
