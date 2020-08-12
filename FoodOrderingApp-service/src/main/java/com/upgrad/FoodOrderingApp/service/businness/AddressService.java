package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private StateDao stateDao;

    @Autowired
    private AddressDao addressDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity) throws SaveAddressException {

        if (addressEntity.getPincode() == null || !(addressEntity.getPincode().matches("^\\d{6,6}$"))) {
            throw new SaveAddressException("SAR-002", "Invalid pincode.");
        }

        if (addressEntity.getFlatBuilNo() == null ||
                addressEntity.getFlatBuilNo().isEmpty() ||
                addressEntity.getLocality() == null ||
                addressEntity.getLocality().isEmpty() ||
                addressEntity.getCity() == null ||
                addressEntity.getCity().isEmpty()) {
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setAddress(addressEntity);
        customerAddressEntity.setCustomer(customerEntity);

        try {
            return addressDao.saveAddress(customerAddressEntity);
        } catch (Exception e) {
            throw new SaveAddressException("ADR-000", "Unknown database error while saving Address");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        return addressDao.deleteAddress(addressEntity);
    }

    public AddressEntity getAddressByUUID(String uuid, CustomerEntity customerEntity) throws AuthorizationFailedException, AddressNotFoundException {
        CustomerAddressEntity customerAddressEntity = addressDao.getAddressByUUID(uuid, customerEntity.getUuid());
        if (customerAddressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        } else if (customerAddressEntity.getCustomer() != customerEntity) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        } else {
            return customerAddressEntity.getAddress();
        }
    }

    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {

        List<CustomerAddressEntity> listCustomerAddressEntity = addressDao.getAllCustomerAddress(customerEntity);
        List<AddressEntity> listAddressEntity = new ArrayList<>();
        for (CustomerAddressEntity ca : listCustomerAddressEntity) {
            listAddressEntity.add(ca.getAddress());
        }

        return listAddressEntity;
    }

    public List<StateEntity> getAllStates() {
        return stateDao.getAllState();
    }

    public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
        try {
            return stateDao.getStateByUuid(uuid);
        } catch (Exception e) {
            throw new AddressNotFoundException("ANF-002", "No state by this id.");
        }
    }
}
