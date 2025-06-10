package com.swp.myleague.model.entities.saleproduct;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Product {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID productId;

    private String productName;
    private String productDescription;

    @Enumerated(EnumType.STRING)
    private ProductSize productSize;

    private Double productPrice;
    private Integer productAmount;
    private String productImgPath;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderProduct> orderProducts;

}
