package com.swp.myleague.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.admin_request.Request;
import com.swp.myleague.model.entities.admin_request.RequestStatus;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.blog.BlogCategory;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.saleproduct.OrderStatus;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.entities.saleproduct.ProductSize;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.entities.ticket.TicketArea;
import com.swp.myleague.model.entities.ticket.TicketType;
import com.swp.myleague.model.repo.ClubSubscriberRepo;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.RequestService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.service.saleproductservice.OrderService;
import com.swp.myleague.model.service.saleproductservice.ProductService;
import com.swp.myleague.model.service.ticketservice.TicketService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping(value = { "/admin", "/admin/" })
public class AdminController {

    @Autowired
    PlayerService playerService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    MatchService matchService;

    @Autowired
    BlogService blogService;

    @Autowired
    RequestService requestService;

    @Autowired
    EmailService emailService;

    @Autowired
    ClubService clubService;

    @Autowired
    TicketService ticketService;

    @Autowired
    OrderService orderService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ClubSubscriberRepo clubSubscriberRepo;

    @GetMapping("")
    public String getAdminDashboard(Model model, HttpSession session,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "success", required = false) String success) {
        model.addAttribute("users", userService.getUser());
        model.addAttribute("products", productService.getAll());
        
        model.addAttribute("matchFixture", matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) > 0).toList());

        model.addAttribute("tickets", ticketService.getAll());

        List<Match> fixtures = new ArrayList<>();
        if (session.getAttribute("autoFixturesMatch") != null) {
            fixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
            Map<Integer, List<Match>> fixturesByRound = fixtures.stream()
                    .collect(Collectors.groupingBy(
                            match -> Integer.parseInt(match.getMatchDescription().replaceAll("[^0-9]", "")),
                            TreeMap::new,
                            Collectors.toList()));
            model.addAttribute("fixtures", fixtures);
            model.addAttribute("fixturesByRound", fixturesByRound);
            model.addAttribute("hasAutoFixtureSession", true);
        }

        List<Request> requests = requestService.getAll();
        Map<String, List<Request>> requestsByClub = new TreeMap<>();

        for (Request req : requests) {
            String clubName = "Unknown";

            try {
                String[] parts = req.getRequestTitle().split("_");
                if (parts.length < 2)
                    throw new IllegalArgumentException("Invalid format");
                String type = parts[1];

                if ("PLAYER".equals(type)) {
                    Player player = CommonFunc.parse(req.getRequestInfor(), Player.class);
                    clubName = player.getClub().getClubName();
                } else if ("BLOG".equals(type)) {
                    // Blog blog = objectMapper.readValue(req.getRequestInfor(), Blog.class);
                    clubName = parts[3];
                }

            } catch (Exception e) {
                e.printStackTrace();
                clubName = "Invalid";
            }

            requestsByClub.computeIfAbsent(clubName, k -> new ArrayList<>()).add(req);
        }

        model.addAttribute("requestsByClub", requestsByClub);
        model.addAttribute("allClubs", clubService.getAll()); // 👈 thêm dòng này
        // 👇 Add flash attributes explicitly to model (optional but helps)
        if (error != null && !error.isBlank()) {
            if (error.equals("loi:200")) {
                error = "Tổng số vé vượt quá sức chứa sân vận động (" + error.split(":")[1] + ").";
                model.addAttribute("error", error);
            }

        }
        if (success != null && !success.isBlank()) {
            success = "Đã thêm mới vé thành công!";
            model.addAttribute("success", success);
        }

        List<Orders> orders = orderService.getAll();

        // 1. Tổng doanh thu
        double totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED) // chỉ tính đơn thành công
                .mapToDouble(Orders::getOrderTotalMoney)
                .sum();
        model.addAttribute("totalRevenue", totalRevenue);

        // 2. Người dùng mua nhiều nhất (theo tổng tiền đã chi)
        Map<String, Double> userSpending = new HashMap<>();
        for (Orders order : orders) {
            if (order.getOrderStatus() != OrderStatus.COMPLETED)
                continue;
            if (order.getUser() == null)
                continue;
            String email = order.getUser().getEmail();
            userSpending.put(email, userSpending.getOrDefault(email, 0.0) + order.getOrderTotalMoney());
        }

        String topBuyer = userSpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No orders");
        model.addAttribute("topBuyer", topBuyer);

        // 3. Tổng số sản phẩm đã bán (dựa vào orderInfo nếu có nhiều mã sản phẩm)
        int totalProductsSold = 0;
        Map<String, Integer> productCount = new HashMap<>();

        for (Orders order : orders) {
            if (order.getOrderStatus() != OrderStatus.COMPLETED)
                continue;

            // Giả sử orderInfo có định dạng: "Product:UUID,Product:UUID,..."
            String[] parts = order.getOrderInfo().split(",");
            for (String part : parts) {
                if (part.startsWith("Product:")) {
                    String productId = part.split(":")[1].trim();
                    productCount.put(productId, productCount.getOrDefault(productId, 0) + 1);
                    totalProductsSold++;
                }
            }
        }
        model.addAttribute("totalProductsSold", totalProductsSold);

        // 4. Sản phẩm bán chạy nhất (tùy chọn)
        String bestSellerName = productCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    try {
                        Product p = productService.getById(entry.getKey());
                        return p.getProductName();
                    } catch (Exception e) {
                        return "Unknown Product";
                    }
                }).orElse("None");
        model.addAttribute("bestSeller", bestSellerName);

        Map<String, Double> revenueByMonth = new LinkedHashMap<>();

        // Khởi tạo 6 tháng gần nhất
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthLabel = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " "
                    + month.getYear();
            revenueByMonth.put(monthLabel, 0.0);
        }

        // Duyệt qua orders và cộng dồn doanh thu theo tháng
        for (Orders order : orders) {
            if (order.getOrderStatus() != OrderStatus.COMPLETED || order.getOrderDateCreated() == null)
                continue;

            LocalDate orderDate = order.getOrderDateCreated().toLocalDate();
            String monthLabel = orderDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " "
                    + orderDate.getYear();

            if (revenueByMonth.containsKey(monthLabel)) {
                double updated = revenueByMonth.get(monthLabel) + order.getOrderTotalMoney();
                revenueByMonth.put(monthLabel, updated);
            }
        }

        // Gửi dữ liệu qua model
        model.addAttribute("revenueMonths", revenueByMonth.keySet());
        model.addAttribute("revenueValues", revenueByMonth.values());

        List<Match> matches = matchService.getAll();
        for (Match match : matches) {
            int total = ticketService.getTotalTickets(match.getMatchId().toString());
            int sold = ticketService.getSoldTickets(match.getMatchId().toString());
    
            match.setTotalTickets(total); // tạo field nếu cần
            match.setSoldTickets(sold);
        }
        model.addAttribute("matches", matches);

        
        return "AdminDashboard";
    }

    @PostMapping("/requests")
    public String updateRequest(@RequestParam(name = "requestId") String requestId,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "requestTitle", required = false) String requestTitle) {
        Request request = requestService.getById(requestId);
        String emailClub = "";
        List<String> emaList = new ArrayList<>();
        String textForSubscriber = "";
        if (status.equals("CONFIRM")) {
            switch (request.getRequestTitle().split("_")[1]) {
                case "PLAYER":
                    Player player = CommonFunc.parse(request.getRequestInfor(), Player.class);
                    player = playerService.save(player);
                    emailClub = userService.getUserById(player.getClub().getUserId().toString()).getEmail();
                    clubSubscriberRepo.findByClubClubId(player.getClub().getClubId()).stream().forEach(cs -> {
                        emaList.add(cs.getEmail());
                    });
                    textForSubscriber = "YOU HAVE NEW FROM " + player.getClub().getClubName();
                    break;
                case "BLOG":
                    Blog blog;
                    try {

                        blog = objectMapper.readValue(request.getRequestInfor(), Blog.class);
                        blog.setBlogDateCreated(LocalDateTime.now());
                        blog.setClub(clubService.getAll().stream()
                                .filter(c -> c.getClubName().equals(requestTitle.split("_")[3])).toList().get(0));
                        blog = blogService.save(blog);
                        blog.setBlogCategory(BlogCategory.Hotnews);
                        emailClub = userService.getUserById(clubService.getAll().stream()
                                .filter(c -> c.getClubName().equals(requestTitle.split("_")[3])).toList().get(0)
                                .getUserId().toString())
                                .getEmail();
                        clubSubscriberRepo.findByClubClubId(clubService.getAll().stream()
                                .filter(c -> c.getClubName().equals(requestTitle.split("_")[3])).toList().get(0)
                                .getClubId()).stream().forEach(cs -> {
                                    emaList.add(cs.getEmail());
                                });

                        textForSubscriber = "YOU HAVE NEW FROM " + requestTitle.split("_")[3];
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
            for (String email : emaList) {
                emailService.sendMail("chumlu2102@gmail.com", email,
                        "[NEW FROM ]" + requestTitle.split("_")[3],
                        textForSubscriber, null);
            }

        }

        String text = "THIS IS RESULT OF REQUEST:\n" +
                status;

        emailService.sendMail("chumlu2102@gmail.com", emailClub, "[RESULT OF REQUEST ]" + request.getRequestTitle(),
                text, null);

        switch (status.toLowerCase()) {
            case "confirm":
                request.setRequestStatus(RequestStatus.CONFIRM);
                break;
            case "cancel":
                request.setRequestStatus(RequestStatus.CANCEL);
                break;
            default:
                break;
        }

        requestService.save(request);

        return "redirect:/admin";
    }

    @GetMapping("/fixture/create")
    public String getAddFixtures(Model model, @RequestParam(name = "startDate") String startDateStr,
            HttpSession session, @RequestParam(name = "recreate") Boolean isRecreate) {
        List<Match> fixtures = new ArrayList<>();

        if (session.getAttribute("autoFixturesMatch") != null && !isRecreate) {
            fixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        } else {
            LocalDate startDate = LocalDate.parse(startDateStr);
            List<LocalTime> matchSlots = List.of(
                    LocalTime.of(18, 0),
                    LocalTime.of(20, 0));
            fixtures = matchService.autoGenFixturesMatches(startDate, matchSlots);
            session.setAttribute("autoFixturesMatch", fixtures);
            model.addAttribute("hasAutoFixtureSession", true);
        }

        Map<Integer, List<Match>> fixturesByRound = fixtures.stream()
                .collect(Collectors.groupingBy(
                        match -> Integer.parseInt(match.getMatchDescription().replaceAll("[^0-9]", "")),
                        TreeMap::new, // 👉 Tự động sắp xếp theo key tăng dần
                        Collectors.toList()));
        model.addAttribute("fixtures", fixtures);
        model.addAttribute("fixturesByRound", fixturesByRound);
        return "redirect:/admin";
    }

    @PostMapping("/fixture/save-round")
    public String postAddFixtures(@RequestParam(name = "roundNumber") Integer roundNumber, HttpSession session) {
        List<Match> fixtures = new ArrayList<>();

        if (session.getAttribute("autoFixturesMatch") != null) {
            fixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        } else {
            return "redirect:/admin";
        }

        Map<Integer, List<Match>> fixturesByRound = fixtures.stream()
                .collect(Collectors.groupingBy(
                        match -> Integer.parseInt(match.getMatchDescription().replaceAll("[^0-9]", "")),
                        TreeMap::new, // 👉 Tự động sắp xếp theo key tăng dần
                        Collectors.toList()));
        ;
        matchService.saveAuto(fixturesByRound.get(roundNumber));

        return "redirect:/admin";
    }

    @PostMapping("/save-tickets")
    public String postTickets(@RequestParam(name = "matchId") String matchId, @RequestBody List<Ticket> tickets,
            Model model, RedirectAttributes redirectAttributes) {
        Match match = matchService.getById(matchId);
        // Nhóm các ticket giống nhau (ví dụ: theo ticketType và ticketArea)
        int stadiumCapacity = match.getMatchClubStats().get(0).getClub().getClubStadiumCapacity();

        Map<String, List<Ticket>> groupedTickets = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getTicketType() + "_" + t.getTicketArea()));

        // Tạo danh sách mới chứa các ticket đã gộp
        List<Ticket> mergedTickets = new ArrayList<>();
        int i = 1;
        for (Map.Entry<String, List<Ticket>> entry : groupedTickets.entrySet()) {
            List<Ticket> group = entry.getValue();
            if (group.isEmpty())
                continue;

            Ticket base = group.get(0);

            Ticket merged = new Ticket();
            merged.setMatch(match);
            merged.setTicketTitle("Ticket " + i++);
            merged.setTicketType(base.getTicketType());
            merged.setTicketArea(base.getTicketArea());
            merged.setTicketAmount(base.getTicketAmount()); // số lượng gộp lại
            merged.setTicketPrice(base.getTicketPrice());

            mergedTickets.add(merged);
        }
        int totalRequested = mergedTickets.stream().mapToInt(Ticket::getTicketAmount).sum(); // adjust if using merged
        if (totalRequested > stadiumCapacity) {
            return "redirect:/admin?error=loi:" + stadiumCapacity;
        }
        ticketService.saveAllTickets(mergedTickets);
        redirectAttributes.addFlashAttribute("success", "add_success");
        return "redirect:/admin";
    }

    @PostMapping("/addproduct")
    public String addProduct(
            @RequestParam("productName") String name,
            @RequestParam("productDescription") String description,
            @RequestParam("productSize") String size,
            @RequestParam("productPrice") String price,
            @RequestParam("productAmount") String amount,
            @RequestParam("productImage") MultipartFile productImage,
            Principal principal) {

        // String username = principal.getName();
        // User user = userService.findByUsername(username);
        // Club club = clubService.getByUserId(user.getUserId());

        Product product = new Product();
        product.setProductName(name);
        product.setProductDescription(description);
        product.setProductSize(java.util.Arrays.stream(ProductSize.values())
                .filter(s -> s.name().equalsIgnoreCase(size))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid product size: " + size)));
        product.setProductPrice(Double.parseDouble(price));
        product.setProductAmount(Integer.parseInt(amount));

        // Xử lý ảnh sản phẩm
        if (!productImage.isEmpty()) {
            File imageFile = new File("src/main/resources/static/images/Storage-Files" + File.separator
                    + productImage.getOriginalFilename());
            try {
                Files.copy(productImage.getInputStream(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                product.setProductImgPath("/images/Storage-Files/" + productImage.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace(); // hoặc log lỗi
            }
        }

        // Lưu product trực tiếp vào DB
        productService.save(product);

        return "redirect:/admin";
    }

    @PostMapping("/user")
    public String updateBan(@RequestParam(name = "userId") String userId) {
        User user = userService.getUserById(userId);
        user.setIsBan(!user.getIsBan());
        userService.save(user);
        return "redirect:/admin";
    }

    @PostMapping("/matches/update")
    public String updateMatch(@ModelAttribute Match match) {
        matchService.save(match);

        return "redirect:/admin";
    }

    @PostMapping("/tickets/update")
    public String updateTicket(@ModelAttribute TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketDTO.getTicketId());
        ticket.setTicketPrice(ticketDTO.getTicketPrice());
        ticket.setTicketArea(ticketDTO.getTicketArea());
        ticket.setTicketAmount(ticketDTO.getTicketAmount());
        ticket.setTicketTitle(ticketDTO.getTicketTitle());
        ticket.setTicketType(ticketDTO.getTicketType());
        ticket.setMatch(matchService.getById(ticketDTO.getMatchId()));
        ticket = ticketService.save(ticket);
        System.out.println();
        return "redirect:/admin";
    }

    @PostMapping("/product/update")
    public String updateProduct(@ModelAttribute Product product, @RequestParam(name = "newProductImgPath", required = false) String newProductImgPath) {
        if (newProductImgPath != null && !newProductImgPath.isBlank()) {
            product.setProductImgPath(newProductImgPath);
        }
        product.setProductSize(ProductSize.L);
        System.out.println();
        productService.save(product);
        return "redirect:/admin";
    }
    
    

}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TicketDTO  {
    UUID ticketId;
    String ticketTitle;
    Double ticketPrice;
    Integer ticketAmount;
    TicketType ticketType;

    TicketArea ticketArea;

    String matchId;
    
}
