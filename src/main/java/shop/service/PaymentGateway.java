package shop.service;

import shop.model.PaymentMethod;

import java.math.BigDecimal;

/** Port to the external payment provider. */
public interface PaymentGateway {
    GatewayResponse charge(String orderId, BigDecimal amount, PaymentMethod method);
}
