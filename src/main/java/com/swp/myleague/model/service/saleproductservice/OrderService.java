package com.swp.myleague.model.service.saleproductservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.saleproduct.OrderProduct;
import com.swp.myleague.model.entities.saleproduct.OrderStatus;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.repo.OrderProductRepo;
import com.swp.myleague.model.repo.OrderRepo;
import com.swp.myleague.model.repo.ProductRepo;

@Service
public class OrderService implements IService<Orders> {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;

    @Override
    public List<Orders> getAll() {
        return orderRepo.findAll();
    }

    @Override
    public Orders getById(String id) {
        return orderRepo.findById(CommonFunc.convertStringToUUID(id)).orElseThrow();
    }

    @Override
    public Orders save(Orders e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Transactional
    public Orders save(List<OrderProduct> orderProducts) {

        boolean hasOutOfStock = orderProducts.stream()
                .anyMatch(op -> op.getProduct().getProductAmount() < op.getOrderProductAmount());

        if (hasOutOfStock) {
            throw new RuntimeException("Có sản phẩm không đủ hàng trong kho.");
        }

        Orders o = new Orders();
        o.setUser(null);
        o.setOrderStatus(OrderStatus.PENDING);
        o.setOrderProducts(orderProducts);
        o.setOrderTotalMoney(orderProducts.stream()
                .mapToDouble(op -> op.getOrderProductAmount() * op.getProduct().getProductPrice()).sum());

        orderProducts.forEach(op -> {
            Product p = op.getProduct();
            productRepo.decreaseStock(p.getProductId(), op.getOrderProductAmount());
            orderProductRepo.save(op);
            productRepo.save(p);
        });

        return orderRepo.save(o);
    }

    @Override
    public Orders delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
