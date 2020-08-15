package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrdersEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@CrossOrigin(allowedHeaders = "*", origins = "*", exposedHeaders = ("access-token"))
@RestController("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @Autowired
    CustomerService customerService;


    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET,path = "/coupon/{coupon_name}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(@RequestHeader(value = "authorization") final String authorization ,@PathVariable(value = "coupon_name") final String couponName) throws AuthorizationFailedException, CouponNotFoundException {
        String[] accessToken = authorization.split("Bearer ");
        if(accessToken.length<2){
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in");
        }


        CustomerEntity customerEntity = customerService.getCustomer(accessToken[1]);
        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .couponName(couponEntity.getCouponName())
                .id(UUID.fromString(couponEntity.getUuid()))
                .percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);


    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET,path = "",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse>getPastOrderOfUser(@RequestHeader(value = "authorization")final String authorization) throws AuthorizationFailedException {
        String[] accessToken = authorization.split("Bearer ");
        if(accessToken.length<2){
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in");
        }
        CustomerEntity customerEntity = customerService.getCustomer(accessToken[1]);
        //Calls getOrdersByCustomers of orderService to get all the past orders of the customer.
        List<OrdersEntity> ordersEntities =  orderService.getOrdersByCustomers(customerEntity.getUuid());

        //Creating List of OrderList
        List<OrderList> orderLists = new LinkedList<>();

        if(ordersEntities != null){  //Checking if orderentities is null if yes them empty list is returned
            for(OrdersEntity ordersEntity : ordersEntities){
                //Calls getOrderItemsByOrder by order of orderService get all the items ordered in past by orders.
                List<OrderItemEntity> orderItemEntities = orderService.getOrderItemsByOrder(ordersEntity);
                //Creating ItemQuantitiesResponse List
                List<ItemQuantityResponse> itemQuantityResponseList = new LinkedList<>();
                orderItemEntities.forEach(orderItemEntity -> {
                    //Creating new ItemQuantityResponseItem
                    ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                            .itemName(orderItemEntity.getItem().getitemName())
                            .itemPrice(orderItemEntity.getItem().getPrice())
                            .id(UUID.fromString(orderItemEntity.getItem().getUuid()))
                            .type(ItemQuantityResponseItem.TypeEnum.valueOf(orderItemEntity.getItem().getType().getValue()));
                    //Creating ItemQuantityResponse which will be added to the list
                    ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                            .item(itemQuantityResponseItem)
                            .quantity(orderItemEntity.getQuantity())
                            .price(orderItemEntity.getPrice());

                    itemQuantityResponseList.add(itemQuantityResponse);
                });
                //Creating OrderListAddressState to add in the address
                OrderListAddressState orderListAddressState = new OrderListAddressState()
                        .id(UUID.fromString(ordersEntity.getAddress().getState().getUuid()))
                        .stateName(ordersEntity.getAddress().getState().getStateName());

                //Creating OrderListAddress to add address to the orderList
                OrderListAddress orderListAddress = new OrderListAddress()
                        .id(UUID.fromString(ordersEntity.getAddress().getUuid()))
                        .flatBuildingName(ordersEntity.getAddress().getFlatBuilNo())
                        .locality(ordersEntity.getAddress().getLocality())
                        .city(ordersEntity.getAddress().getCity())
                        .pincode(ordersEntity.getAddress().getPincode())
                        .state(orderListAddressState);
                //Creating OrderListCoupon to add Coupon to the orderList
                OrderListCoupon orderListCoupon = new OrderListCoupon()
                        .couponName(ordersEntity.getCoupon().getCouponName())
                        .id(UUID.fromString(ordersEntity.getCoupon().getUuid()))
                        .percent(ordersEntity.getCoupon().getPercent());

                //Creating OrderListCustomer to add Customer to the orderList
                OrderListCustomer orderListCustomer = new OrderListCustomer()
                        .id(UUID.fromString(ordersEntity.getCustomer().getUuid()))
                        .firstName(ordersEntity.getCustomer().getFirstName())
                        .lastName(ordersEntity.getCustomer().getLastName())
                        .emailAddress(ordersEntity.getCustomer().getEmail())
                        .contactNumber(ordersEntity.getCustomer().getContactNumber());

                //Creating OrderListPayment to add Payment to the orderList
                OrderListPayment orderListPayment = new OrderListPayment()
                        .id(UUID.fromString(ordersEntity.getPayment().getUuid()))
                        .paymentName(ordersEntity.getPayment().getPaymentName());


                //Craeting orderList to add all the above info and then add it orderLists to finally add it to CustomerOrderResponse
                OrderList orderList = new OrderList()
                        .id(UUID.fromString(ordersEntity.getUuid()))
                        .itemQuantities(itemQuantityResponseList)
                        .address(orderListAddress)
                        .bill(BigDecimal.valueOf(ordersEntity.getBill()))
                        .date(String.valueOf(ordersEntity.getDate()))
                        .discount(BigDecimal.valueOf(ordersEntity.getDiscount()))
                        .coupon(orderListCoupon)
                        .customer(orderListCustomer)
                        .payment(orderListPayment);
                orderLists.add(orderList);

            }
            CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse()
                    .orders(orderLists);
            return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse,HttpStatus.OK);
        }else {
            return new ResponseEntity<CustomerOrderResponse>(new CustomerOrderResponse(),HttpStatus.OK);//If no order created by customer empty array is returned.
        }
    }
}
