package shop.service;

import shop.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

/** Fake gateway for the console demo/tests: the caller decides whether the next payment succeeds. */
public class SimulatedPaymentGateway implements PaymentGateway, GatewaySimulationControl {
    private boolean approveNext = true;

    @Override
    public void setApproveNext(boolean approveNext) {
        this.approveNext = approveNext;
    }

    @Override
    public GatewayResponse charge(String orderId, BigDecimal amount, PaymentMethod method) {
        if (approveNext) {
            return new GatewayResponse(true, amount, "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    "Approved");
        }
        return new GatewayResponse(false, BigDecimal.ZERO, null, "Declined by the gateway");
    }
}
