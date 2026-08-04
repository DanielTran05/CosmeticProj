package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.request.ShippingOrderCreationRequest;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.entity.OrderShipping;
import com.dtp.cosmemgt.sales.order.repository.OrderShippingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class OrderShippingService {
    OrderShippingRepository orderShippingRepository;

    public void createOrderShipping(Order order, User user, OrderCreationRequest request) {
        log.info("Creating shipping info for order [{}] and user [{}]", order.getId(), user.getId());

        OrderShipping odShipping = OrderShipping.builder()
                .order(order)
                .shippingProvider("DVVC")
                .trackingNumber("DEMO_TRACKINGNO")
                .build();

        if(request.getShippingOrderCreationRequest() != null){
            ShippingOrderCreationRequest shippingOrderCreationRequest = request.getShippingOrderCreationRequest();
            odShipping.setReceiverAddress(shippingOrderCreationRequest.getReceiverAddress());
            odShipping.setReceiverName(shippingOrderCreationRequest.getReceiverName());
            odShipping.setReceiverPhone(shippingOrderCreationRequest.getReceiverPhone());
        }else {
            odShipping.setReceiverAddress(user.getAddress());
            odShipping.setReceiverName(user.getFullName());
            odShipping.setReceiverPhone(user.getPhoneNum());
        }

        orderShippingRepository.save(odShipping);
        order.setOrderShipping(odShipping);
    }
}
