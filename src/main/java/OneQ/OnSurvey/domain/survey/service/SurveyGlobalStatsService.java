package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;
import OneQ.OnSurvey.domain.survey.repository.SurveyGlobalStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyGlobalStatsService {

    private final StringRedisTemplate redisTemplate;
    private final SurveyGlobalStatsRepository statsRepository;

    @Value("${redis.global-key-prefix.daily-user}")
    private String DAILY_USER_KEY_PREFIX;

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

        String value = redisTemplate.opsForValue().get(DAILY_USER_KEY_PREFIX);
        Integer dailyUserCount = (value != null) ? Integer.parseInt(value) : 0;

        return GlobalStats.of(
            surveyGlobalStats.getTotalDueCount(),
            surveyGlobalStats.getTotalCompletedCount(),
            surveyGlobalStats.getTotalPromotionCount(),
            dailyUserCount
        );
    }
}
