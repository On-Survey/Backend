package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Residence {
    ALL("전체"),
    GANGWON("강원"),
    GYEONGGI("경기"),
    GYEONGNAM("경남"),
    GYEONGBUK("경북"),
    GWANGJU("광주"),
    DAEGU("대구"),
    DAEJEON("대전"),
    BUSAN("부산"),
    SEOUL("서울"),
    ULSAN("울산"),
    INCHEON("인천"),
    JEONNAM("전남"),
    JEONBUK("전북"),
    JEJU("제주"),
    CHUNGNAM("충남"),
    CHUNGBUK("충북"),
    SEJONG("세종");

    private final String value;
}
