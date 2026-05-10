package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;
import OneQ.OnSurvey.domain.survey.repository.SurveyGlobalStatsRepository;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SurveyGlobalStatsServiceTest {

    @Mock
    private SurveyGlobalStatsRepository statsRepository;

    @Mock
    private RedisAgent redisAgent;

    @InjectMocks
    private SurveyGlobalStatsService surveyGlobalStatsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(surveyGlobalStatsService, "dailyUserKey", "daily:user:");
    }

    private SurveyGlobalStats buildStats(long due, long completed, long promotion) {
        return SurveyGlobalStats.builder()
                .id(1L)
                .totalDueCount(due)
                .totalCompletedCount(completed)
                .totalPromotionCount(promotion)
                .build();
    }

    @Test
    @DisplayName("addDueCount - 기존 stats에 delta만큼 증가")
    void addDueCount_increasesExistingStats() {
        SurveyGlobalStats stats = buildStats(1000L, 500L, 200L);
        given(statsRepository.findById(1L)).willReturn(Optional.of(stats));

        surveyGlobalStatsService.addDueCount(100L);

        assertThat(stats.getTotalDueCount()).isEqualTo(1100L);
    }

    @Test
    @DisplayName("addDueCount - stats 없으면 init 후 증가")
    void addDueCount_noStats_initAndIncreases() {
        SurveyGlobalStats newStats = SurveyGlobalStats.init();
        given(statsRepository.findById(1L)).willReturn(Optional.empty());
        given(statsRepository.save(any(SurveyGlobalStats.class))).willReturn(newStats);

        surveyGlobalStatsService.addDueCount(50L);

        assertThat(newStats.getTotalDueCount()).isEqualTo(1050L);
    }

    @Test
    @DisplayName("addCompletedCount - 기존 stats에 delta만큼 증가")
    void addCompletedCount_increasesExistingStats() {
        SurveyGlobalStats stats = buildStats(1000L, 500L, 200L);
        given(statsRepository.findById(1L)).willReturn(Optional.of(stats));

        surveyGlobalStatsService.addCompletedCount(30L);

        assertThat(stats.getTotalCompletedCount()).isEqualTo(530L);
    }

    @Test
    @DisplayName("addPromotionCount - 기존 stats에 delta만큼 증가")
    void addPromotionCount_increasesExistingStats() {
        SurveyGlobalStats stats = buildStats(1000L, 500L, 200L);
        given(statsRepository.findById(1L)).willReturn(Optional.of(stats));

        surveyGlobalStatsService.addPromotionCount(10L);

        assertThat(stats.getTotalPromotionCount()).isEqualTo(210L);
    }

    @Test
    @DisplayName("getStats - stats 존재 시 dailyUserCount와 함께 반환")
    void getStats_existingStats_returnsCorrectValues() {
        SurveyGlobalStats stats = buildStats(2000L, 800L, 400L);
        given(statsRepository.findById(1L)).willReturn(Optional.of(stats));
        given(redisAgent.getZSetCount(any(), anyLong(), anyLong())).willReturn(42L);

        GlobalStats result = surveyGlobalStatsService.getStats();

        assertThat(result.totalDueCount()).isEqualTo(2000L);
        assertThat(result.totalCompletedCount()).isEqualTo(800L);
        assertThat(result.totalPromotionCount()).isEqualTo(400L);
        assertThat(result.dailyUserCount()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getStats - stats 없으면 init 기본값 사용")
    void getStats_noStats_usesInitDefaults() {
        given(statsRepository.findById(1L)).willReturn(Optional.empty());
        given(redisAgent.getZSetCount(any(), anyLong(), anyLong())).willReturn(0L);

        GlobalStats result = surveyGlobalStatsService.getStats();

        assertThat(result.totalDueCount()).isEqualTo(1000L);
        assertThat(result.totalCompletedCount()).isEqualTo(1000L);
        assertThat(result.totalPromotionCount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("removeOldDailyUsers - redisAgent.rangeRemoveFromZSet 호출")
    void removeOldDailyUsers_callsRedisRangeRemove() {
        surveyGlobalStatsService.removeOldDailyUsers();

        verify(redisAgent).rangeRemoveFromZSet(any(), anyLong(), anyLong());
    }
}
