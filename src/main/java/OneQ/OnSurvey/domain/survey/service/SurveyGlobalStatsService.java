package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.repository.SurveyGlobalStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyGlobalStatsService {

    private final SurveyGlobalStatsRepository statsRepository;

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
    public SurveyGlobalStats getStats() {
        return statsRepository.findById(1L)
                .orElse(SurveyGlobalStats.init());
    }
}
