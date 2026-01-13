package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;
import OneQ.OnSurvey.domain.survey.repository.SurveyGlobalStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyGlobalStatsService {

    private final StringRedisTemplate redisTemplate;
    private final SurveyGlobalStatsRepository statsRepository;

    @Value("${redis.global-key-prefix.daily-user}")
    private String dailyUserKeyPrefix;

    private SurveyGlobalStats getOrInit() {
        return statsRepository.findById(1L)
                .orElseGet(() -> statsRepository.save(SurveyGlobalStats.init()));
    }

    public void addDueCount(long delta) {
        SurveyGlobalStats stats = getOrInit();
        stats.increaseDueCount(delta);
    }

    public void addCompletedCount(long delta) {
        SurveyGlobalStats stats = getOrInit();
        stats.increaseCompletedCount(delta);
    }

    public void addPromotionCount(long delta) {
        SurveyGlobalStats stats = getOrInit();
        stats.increasePromotionCount(delta);
    }

    @Transactional(readOnly = true)
    public GlobalStats getStats() {
        SurveyGlobalStats surveyGlobalStats = statsRepository.findById(1L)
            .orElse(SurveyGlobalStats.init());

        Long dailyUserCount = redisTemplate.opsForZSet().zCard(dailyUserKeyPrefix);
        return GlobalStats.of(
            surveyGlobalStats.getTotalDueCount(),
            surveyGlobalStats.getTotalCompletedCount(),
            surveyGlobalStats.getTotalPromotionCount(),
            dailyUserCount
        );
    }

    @Scheduled(fixedRate = 3600000) // 매 시간 실행
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void removeOldDailyUsers() {
        long dailyRange = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);
        redisTemplate.opsForZSet().removeRangeByScore(dailyUserKeyPrefix, 0, dailyRange);
    }
}
