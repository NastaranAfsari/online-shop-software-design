package shop.service;

/**
 * Lets the console demo decide whether the next simulated payment is approved.
 * The CLI depends on this abstraction instead of the concrete SimulatedPaymentGateway (DIP).
 */
public interface GatewaySimulationControl {
    void setApproveNext(boolean approveNext);
}
