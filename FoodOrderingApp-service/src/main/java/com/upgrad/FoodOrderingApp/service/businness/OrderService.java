package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDao;
import com.upgrad.FoodOrderingApp.service.dao.OrdersDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrdersEntity;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    CouponDao couponDao;

    @Autowired
    CustomerDao customerDao;

    @Autowired
    OrdersDao ordersDao;

    @Autowired
    OrderItemDao orderItemDao;

    public CouponEntity getCouponByCouponName(String couponName) throws CouponNotFoundException {
        if (couponName == null || couponName == "") {
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }
        CouponEntity couponEntity = couponDao.getCouponByCouponName(couponName);
        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }
        return couponEntity;
    }

       /* This method is to get Orders By Customers.Takes the customerUuid  and returns the list of OrdersEntity .
        If error throws exception with error code and error message.
        */
    public List<OrdersEntity>getOrdersByCustomers(String customerUuid){
        CustomerEntity customerEntity = customerDao.getCustomerByUuid(customerUuid);
        return ordersDao.getOrdersByCustomers(customerEntity);
    }

    /* This method is to get Order Items By Order.Takes the ordersEntity  and returns the list of OrderItemEntity .
    If error throws exception with error code and error message.
    */
    public List<OrderItemEntity> getOrderItemsByOrder(OrdersEntity ordersEntity) {

        //Calls getOrderItemsByOrder of orderItemDao to return list of OrderItemEntity corresponding to the order.
        List<OrderItemEntity> orderItemEntities = orderItemDao.getOrderItemsByOrder(ordersEntity);
        return orderItemEntities;
    }
}
