package com.ssmdemo.service.impl;

import com.ssmdemo.dao.CustomerMapper;
import com.ssmdemo.model.Customer;
import com.ssmdemo.model.CustomerExample;
import com.ssmdemo.service.inf.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CustomerService implements ICustomerService {


    @Autowired
    CustomerMapper customerMapper;

    public int insert(Customer customer) {
        return customerMapper.insert(customer);
    }

    public int delete(int id) {
        return 0;
    }

    public int update(Customer customer) {
        return 0;
    }

    public List<Customer> findAll() {
        CustomerExample example = new CustomerExample();
        example.createCriteria().andNameIsNotNull();
        return customerMapper.selectByExample(example);
    }

    public List<Customer> findByName(String name) {
        CustomerExample example = new CustomerExample();
        example.createCriteria().andNameIsNotNull();
        return customerMapper.selectByExample(example);
    }
}
