package com.swp.myleague.model.service.saleproductservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.repo.ProductRepo;

@Service
public class ProductService implements IService<Product> {

    @Autowired
    private ProductRepo productRepo;

    @Override
    public List<Product> getAll() {
        return productRepo.findAll();
    }

    @Override
    public Product getById(String id) {
        return productRepo.findById(CommonFunc.convertStringToUUID(id)).orElseThrow();
    }

    @Override
    public Product save(Product e) {
        return productRepo.save(e);
    }

    @Override
    public Product delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
