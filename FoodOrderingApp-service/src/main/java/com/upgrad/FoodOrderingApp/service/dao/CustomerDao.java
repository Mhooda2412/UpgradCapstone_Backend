package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        try {
            entityManager.persist(customerEntity);
        } catch (Exception e) {
            System.out.println(".....................Database Error");
            e.printStackTrace();
        }
        return customerEntity;
    }

    public CustomerEntity getUserByContactNumber(String contactNumber) {
        try {
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class).setParameter("contactNumber", contactNumber).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            System.out.println(".....................Database Error");
            e.printStackTrace();
            return null;
        }
    }

    public CustomerEntity updateCustomer(final CustomerEntity updateCustomerEntity) {
        try {
            entityManager.merge(updateCustomerEntity);
        } catch (Exception e) {
            System.out.println(".....................Database Error");
        }
        return updateCustomerEntity;
    }

    public CustomerEntity getCustomerByUuid(final String uuid){
        try{
            return entityManager.createNamedQuery("customerByUuid",CustomerEntity.class).setParameter("uuid",uuid).getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }

}

