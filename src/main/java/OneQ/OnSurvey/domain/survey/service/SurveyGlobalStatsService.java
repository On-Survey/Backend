package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;
import OneQ.OnSurvey.domain.survey.repository.SurveyGlobalStatsRepository;
import OneQ.OnSurvey.global.common.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyGlobalStatsService {

    private final SurveyGlobalStatsRepository statsRepository;

    @Value("${redis.global-key-prefix.daily-user}")
    private String dailyUserKey;

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

        // 24시간 동안 활동한 유저 수 계산
        Long dailyUserCount = RedisUtils.getZSetCount(
            dailyUserKey,
            System.currentTimeMillis() - (24 * 60 * 60 * 1000L),
            Long.MAX_VALUE);
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
        RedisUtils.rangeRemoveFromZSet(
            dailyUserKey,
            0,
            System.currentTimeMillis() - (24 * 60 * 60 * 1000L));
    }
}
