package com.swp.myleague.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.repo.MatchEventRepo;
import com.swp.myleague.model.repo.MatchRepo;
import com.swp.myleague.utils.openai_matchevent.OpenAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreMatchScenarioScheduler {

    private final MatchRepo matchRepo;
    private final MatchEventRepo eventRepo; // nếu muốn lưu MatchEvent
    private final OpenAiService openAiService;

    /**
     * Chạy mỗi phút. Cron "0 * * * * *" = giây 0 của mỗi phút.
     * Điều chỉnh fixedRate/cron theo nhu cầu.
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void generateScenarioForUpcomingMatches() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime threshold = now.plusMinutes(15); // “sắp bắt đầu” = 15ph tới

        List<Match> upcoming = matchRepo.findByMatchStartTimeBetweenAndMatchEventsIsEmpty(now, threshold);

        for (Match match : upcoming) {
            try {
                String prompt = buildPrompt(match);
                String script = openAiService.askChatGPT(prompt);

                // Ví dụ: “00:00:Tiếng còi khai cuộc\n05:30:Cú sút đầu tiên...”
                List<MatchEvent> events = parseScript(script, match);
                eventRepo.saveAll(events); // hoặc gửi qua WebSocket/Kafka

                log.info("Generated scenario for match {}", match.getMatchTitle());
            } catch (Exception ex) {
                log.error("Failed to create scenario for match {}", match.getMatchId(), ex);
            }
        }
    }

    /** Tạo prompt hướng dẫn ChatGPT */
    private String buildPrompt(Match m) {
        return """
                Bạn là bình luận viên bóng đá. Hãy tạo kịch bản diễn biến 90 phút trận đấu ở mức KHỞI ĐẦU
                (những sự kiện quan trọng dự kiến, không quá chi tiết) cho trận:
                - Tiêu đề: %s
                - Thời gian bắt đầu: %s (GMT+7)
                - Yêu cầu: trả về MỘT chuỗi duy nhất, mỗi dòng dạng "MM:SS:Nội dung",
                  ví dụ "00:00:Trọng tài thổi còi khai cuộc".
                """.formatted(m.getMatchTitle(),
                m.getMatchStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private List<MatchEvent> parseScript(String script, Match match) {

        List<MatchEvent> events = new ArrayList<>();

        String[] lines = script.split("\\R"); // Tách từng dòng
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            String[] parts = line.split(":", 3);
            if (parts.length < 3) {
                System.err.println("Sai định dạng: " + line);
                continue;
            }

            try {
                int minutes = Integer.parseInt(parts[0].trim());
                // int seconds = Integer.parseInt(parts[1].trim());
                String content = parts[2].trim();

                // LocalDateTime eventTime = match.getMatchStartTime()
                //         .plusMinutes(minutes)
                //         .plusSeconds(seconds);

                MatchEvent event = new MatchEvent();
                event.setMatch(match);
                event.setMatchEventMinute(minutes);
                event.setMatchEventTitle(content);

                events.add(event);
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý dòng: " + line);
                e.printStackTrace();
            }
        }

        return events;
    }
}
