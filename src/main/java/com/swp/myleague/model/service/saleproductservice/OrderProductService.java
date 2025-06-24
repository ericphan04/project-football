package com.swp.myleague.model.service.saleproductservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.saleproduct.OrderProduct;
import com.swp.myleague.model.repo.OrderProductRepo;

@Service
public class OrderProductService implements IService<OrderProduct> {

    @Autowired
    private OrderProductRepo orderProductRepo;

    @Override
    public List<OrderProduct> getAll() {
        return orderProductRepo.findAll();
    }

    @Override
    public OrderProduct getById(String id) {
        return orderProductRepo.findById(CommonFunc.convertStringToUUID(id)).orElseThrow();
    }

    @Override
    public OrderProduct save(OrderProduct e) {
        return orderProductRepo.save(e);
    }

    @Override
    public OrderProduct delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
