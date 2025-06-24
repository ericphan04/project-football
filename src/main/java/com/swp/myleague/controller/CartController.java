package com.swp.myleague.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.service.saleproductservice.ProductService;

import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = { "/cart" })
public class CartController {

    @Autowired
    ProductService productService;

    @GetMapping(value = { "/", "" })
    public String getCart(Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null)
            cart = new HashMap<>();

        model.addAttribute("cartProducts", cart);
        return "Cart";
    }

    @PostMapping(value = { "", "/" })
    public String addProductToCart(Model model, HttpServletRequest request, @RequestParam("productId") String productId,
            @RequestParam("productAmount") String productAmount,
            @RequestParam("size") String size,
            @RequestParam("url") String url, HttpSession session) {
        Object o = session.getAttribute("cart");

        if (o == null) {
            session.setAttribute("cart", new HashMap<>());
        }
        Map<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            ct = new CartItem();
            ct.setProduct(productService.getById(productId));
        }
        amount = Integer.parseInt(productAmount) + amount;
        ct.setProductAmount(amount);
        cart.put(productId, ct);
        session.setAttribute("cart", cart);
        return "redirect:product";
    }

}
