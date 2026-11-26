package shop.repository;

import shop.model.Order;
import shop.model.OrderStatus;

import java.util.List;

public interface OrderRepository extends BaseRepository<Order> {
    List<Order> findByUserId(String userId);

    List<Order> findByStatus(OrderStatus status);
}
