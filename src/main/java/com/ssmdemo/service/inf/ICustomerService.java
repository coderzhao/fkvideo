package com.ssmdemo.service.inf;

import com.ssmdemo.model.Customer;

import java.util.List;

public interface ICustomerService {
    int insert(Customer customer);
    int delete(int id);
    int update(Customer customer);
    List<Customer> findAll();
    List<Customer> findByName(String name);
}
