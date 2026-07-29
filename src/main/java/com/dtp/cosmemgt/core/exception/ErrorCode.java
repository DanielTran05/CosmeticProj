package com.dtp.cosmemgt.core.exception;

import lombok.Getter;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1009, "Invalid request", HttpStatus.BAD_REQUEST),

    //cate
    CATEGORY_EXISTED(2001, "Category existed", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(2002, "Category not existed", HttpStatus.NOT_FOUND),

    //supplier
    SUPPLIER_EXISTED(3001, "Supplier existed", HttpStatus.BAD_REQUEST),
    SUPPLIER_NOT_EXISTED(3002, "Supplier not existed", HttpStatus.NOT_FOUND),

    //pricing rule
    PRICING_RULE_EXISTED(4001, "Pricing rule existed", HttpStatus.BAD_REQUEST),
    PRICING_RULE_NOT_EXISTED(4002, "Pricing rule not existed", HttpStatus.NOT_FOUND),

    //product
    PRODUCT_EXISTED(5001, "Product existed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(5002, "Product not existed", HttpStatus.NOT_FOUND),

    //UOM
    UOM_EXISTED(6001, "UOM existed", HttpStatus.BAD_REQUEST),
    UOM_NOT_EXISTED(6002, "UOM not existed", HttpStatus.NOT_FOUND),

    //product variant
    PRODUCT_VARIANT_EXISTED(7001, "Product variant existed", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANT_NOT_EXISTED(7002, "Product variant not existed", HttpStatus.NOT_FOUND),
    PRODUCT_OR_UOM_NOT_EXISTED(7003, "Product or UOM not existed", HttpStatus.NOT_FOUND),

    //order
    ORDER_DO_NOT_BELONG(8001, "This order is not belong to you", HttpStatus.BAD_REQUEST),
    ONLY_ONE_REVIEW_FOR_CUS_PV(8002, "You can only review once for a product", HttpStatus.BAD_REQUEST),
    HAS_NOT_USED_YET(8003, "You have not used this product", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(8004, "Order is not found", HttpStatus.NOT_FOUND),
    CAN_NOT_CANCEL_ORDER(8005, "Can not cancel order", HttpStatus.BAD_REQUEST),
    CAN_NOT_RETURN_ORDER(8006, "Can not return order", HttpStatus.BAD_REQUEST),
    CAN_NOT_EXPORT_ORDER(8007, "Can not export order", HttpStatus.BAD_REQUEST),

    //warehouse order
    CAN_NOT_CONFIRM_DELIVERED(8100, "Warehouse can not confirm un-delivered order", HttpStatus.BAD_REQUEST),

    //review
    REVIEW_NOT_EXISTED(9001, "Review not existed", HttpStatus.BAD_REQUEST),
    REVIEW_EXISTED(9002, "Review existed", HttpStatus.BAD_REQUEST),
    USER_HAS_NOT_REVIEWED_THIS_PRODUCT(9003, "User has not review this product", HttpStatus.BAD_REQUEST),

    //inventory transaction
    OUT_OF_STOCK(9100, "Out of stock", HttpStatus.BAD_REQUEST),

    //inventory batch
    INVENTORY_BATCH_NOT_EXISTED(9200, "Batch is not existed", HttpStatus.BAD_REQUEST),
    INVALID_ADJUSTMENT_QTY(9201, "Invalid adjustment quantity", HttpStatus.BAD_REQUEST),



    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
