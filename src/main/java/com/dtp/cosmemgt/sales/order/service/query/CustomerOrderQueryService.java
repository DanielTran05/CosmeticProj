package com.dtp.cosmemgt.sales.order.service.query;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.service.specification.OrderSpecification;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class CustomerOrderQueryService {
    CurrentUserService currentUserService;

    OrderRepository orderRepository;
    OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderId) {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);
        return orderMapper.toOrderDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllMyOrder(Map<String, String> queryParams) {
        User myAccount = currentUserService.getCurrentUser();

        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Order> filterOrderSpec = OrderSpecification.filterOrder(queryParams);
        Specification<Order> customerSpec = (root, query, cb) -> cb.equal(root.get("customer"), myAccount);

        Page<Order> orderPage = orderRepository.findAll(Specification.where(customerSpec).and(filterOrderSpec), pageable);

        return PageResponse.of(orderPage.map(orderMapper::toOrderResponse));
    }

    private Order getValidOwnedOrder(User currentUser, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }
}
