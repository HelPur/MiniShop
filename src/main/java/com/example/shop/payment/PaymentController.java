package com.example.shop.payment;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.CurrentUserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<PaymentRecord> pay(@RequestBody PaymentService.PayRequest request) {
        return ApiResponse.ok(paymentService.createPayment(CurrentUserContext.getRequired().getId(), request));
    }

    @PostMapping("/callback")
    public ApiResponse<PaymentRecord> callback(@RequestBody PaymentService.CallbackRequest request) {
        return ApiResponse.ok(paymentService.callback(request));
    }
}
