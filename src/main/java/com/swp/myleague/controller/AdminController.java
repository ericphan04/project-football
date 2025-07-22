package com.swp.myleague.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.model.entities.admin_request.Request;
import com.swp.myleague.model.entities.admin_request.RequestStatus;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.RequestService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.service.saleproductservice.ProductService;
import com.swp.myleague.model.service.ticketservice.TicketService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("")
    public String getAdminDashboard(Model model, HttpSession session) {
        model.addAttribute("users", userService.getUser());
        model.addAttribute("products", productService.getAll());
        model.addAttribute("matches", matchService.getAll());

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
                    Blog blog = CommonFunc.parse(req.getRequestInfor(), Blog.class);
                    clubName = blog.getClub().getClubName();
                }

            } catch (Exception e) {
                e.printStackTrace();
                clubName = "Invalid";
            }

            requestsByClub.computeIfAbsent(clubName, k -> new ArrayList<>()).add(req);
        }

        model.addAttribute("requestsByClub", requestsByClub);
        model.addAttribute("allClubs", clubService.getAll()); // 👈 thêm dòng này

        return "AdminDashboard";
    }

    @PostMapping("/requests")
    public String updateRequest(@RequestParam(name = "requestId") String requestId,
            @RequestParam(name = "status") String status) {
        Request request = requestService.getById(requestId);
        String emailClub = "";
        if (status.equals("CONFIRM")) {
            switch (request.getRequestTitle().split("_")[1]) {
                case "PLAYER":
                    Player player = CommonFunc.parse(request.getRequestInfor(), Player.class);
                    player = playerService.save(player);
                    emailClub = userService.getUserById(player.getClub().getUserId().toString()).getEmail();
                    break;
                case "BLOG":
                    Blog blog = CommonFunc.parse(request.getRequestInfor(), Blog.class);
                    blog = blogService.save(blog);
                    emailClub = userService.getUserById(blog.getClub().getUserId().toString()).getEmail();
                    break;
                default:
                    break;
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
            Model model) {
        Match match = matchService.getById(matchId);
        // Nhóm các ticket giống nhau (ví dụ: theo ticketType và ticketArea)
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
            merged.setTicketAmount(group.size()); // số lượng gộp lại
            merged.setTicketPrice(base.getTicketPrice());

            mergedTickets.add(merged);
        }
        ticketService.saveAllTickets(mergedTickets);
        return "redirect:/admin";
    }

}
