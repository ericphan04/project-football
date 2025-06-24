package com.swp.myleague.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.saleproductservice.ProductService;
import com.swp.myleague.model.service.ticketservice.TicketService;
import com.swp.myleague.utils.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(value = { "/payment" })
public class PaymentController {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Autowired ProductService productService;

    @Autowired TicketService ticketService;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {

        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null)
            cart = new HashMap<>();

        model.addAttribute("cartProducts", cart);
        return "Checkout";
    }

    @GetMapping("/create-payment")
    public String createPayment(HttpServletRequest req, @RequestParam("amount") Double amount, @RequestParam("orderInfo") String orderInfo) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = req.getRemoteAddr();
        String vnp_OrderInfo = "Thanh toan don hang: " + vnp_TxnRef + ":" + orderInfo;
        String vnp_TransactionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100))); // VNPay yêu cầu x100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_TransactionDate);

        // B1: Sort params by key
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String name : fieldNames) {
            String value = vnp_Params.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append('=').append(URLEncoder.encode(value, "UTF-8"));
                query.append(name).append('=').append(URLEncoder.encode(value, "UTF-8"));
                if (!name.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        String paymentUrl = vnp_PayUrl + "?" + query.toString();

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/vnpay-return")
    public String paymentReturn(HttpServletRequest request, Principal principal) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (!paramName.equals("vnp_SecureHash")) {
                params.put(paramName, paramValue);
            }
        }

        try {
            if (params.get("vnp_ResponseCode").equals("00")) {
                String username = principal.getName();
                User user = userService.findByUsername(username);
                String subject = "THANH TOAN MYLEAGUE: ";
                switch (params.get("vnp_OrderInfo").split(":")[2]) {
                    case "Product":
                        subject += "BUY PRODUCT";
                        Product product = productService.getById(params.get("vnp_OrderInfo").split(":")[3]);
                        product.setProductAmount(product.getProductAmount() - 1);
                        productService.save(product);
                        break;
                    case "Ticket": 
                        subject += "BUY TICKET";
                        Ticket ticket = ticketService.getById(params.get("vnp_OrderInfo").split(":")[3]);
                        ticket.setTicketAmount(ticket.getTicketAmount() - 1);
                        ticketService.save(ticket);
                        break;
                    default:
                        break;
                }
                emailService.sendMail("chumlu2102@gmail.com", user.getEmail(), subject, "MaQR");
                return "PaymentSuccess";
            }
        } catch (Exception e) {
            return new String("Không thể thực hiện thanh toán");
        }
        return "PaymentFailure";

    }

}
