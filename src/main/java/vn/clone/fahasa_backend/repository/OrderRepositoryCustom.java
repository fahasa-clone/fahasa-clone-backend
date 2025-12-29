package vn.clone.fahasa_backend.repository;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.*;
import vn.clone.fahasa_backend.domain.Order;
import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.domain.response.OrderSummaryDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<OrderSummaryDTO> findAllOrderSummary(Account account, OrderStatus status, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderSummaryDTO> query = cb.createQuery(OrderSummaryDTO.class);
        Root<Order> root = query.from(Order.class);

        query.select(cb.construct(OrderSummaryDTO.class,
                                  root.get("publicId"),
                                  root.get("orderReference"),
                                  root.get("createdAt"),
                                  root.get("updatedAt"),
                                  root.get("currentStatus"),
                                  root.get("totalQuantity"),
                                  root.get("grandTotal")));


        Specification<Order> spec = SpecificationsBuilder.hasAccountId(account.getId());
        if (status != null) {
            Specification<Order> isStatus = SpecificationsBuilder.isStatus(status);
            spec = spec.and(isStatus);
        }
        query.where(spec.toPredicate(root, query, cb));

        jakarta.persistence.criteria.Order order = cb.desc(root.get("id"));
        query.orderBy(order);

        TypedQuery<OrderSummaryDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<OrderSummaryDTO> results = typedQuery.getResultList();
        Long total = countQuery(spec);

        return new PageImpl<>(results, pageable, total);
    }

    private long countQuery(Specification<Order> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> order = countQuery.from(Order.class);
        countQuery.select(cb.count(order));
        countQuery.where(specification.toPredicate(order, countQuery, cb));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public Order findFullOrderDetailById(UUID orderId) {
        try {
            Order orderAndState = findOrderStatesByOrderId(orderId);
            Order orderAndDetail = findOrderDetailsByOrderId(orderId);
            orderAndState.setOrderDetails(orderAndDetail.getOrderDetails());
            return orderAndState;
        } catch (NoResultException e) {
            throw new BadRequestException("Order not found!");
        }
    }

    private Order findOrderStatesByOrderId(UUID orderId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = criteriaBuilder.createQuery(Order.class);
        Root<Order> order = query.from(Order.class);
        order.fetch("orderStates", JoinType.LEFT);
        Predicate predicate = criteriaBuilder.equal(order.get("publicId"), orderId);
        query.select(order).where(predicate);
        return entityManager.createQuery(query).getSingleResult();
    }

    private Order findOrderDetailsByOrderId(UUID orderId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = criteriaBuilder.createQuery(Order.class);
        Root<Order> order = query.from(Order.class);
        Fetch<Order, OrderDetail> orderDetail = order.fetch("orderDetails", JoinType.LEFT);
        orderDetail.fetch("book", JoinType.LEFT);
        Predicate predicate = criteriaBuilder.equal(order.get("publicId"), orderId);
        query.select(order).where(predicate);
        return entityManager.createQuery(query).getSingleResult();
    }
}
