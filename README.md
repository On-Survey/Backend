# ONSURVEY BACKEND REPOSITORY

---

## 1. 프로젝트 개요
| 항목 | 내용                                                         |
|------|------------------------------------------------------------|
| **프로젝트명** | *ON SURVEY*                                                |
| **주요 기술** | Java 21, Spring Boot 3, Spring JPA, QueryDSL, Redis, MySQL | 

---

## 2. 주요 DOMAIN
| DOMAIN        | 설명         |
|---------------|------------|
| FORM          | 설문 생성 및 수정 |
| MEMBER        | 사용자 정보 관리  |
| MANAGEMENT    | 설문 관리 및 집계 |
| PARTICIPATION | 설문 참여      |
| PAYMENT       | 결제         |
| 멱등성 & 분산 락 | PromotionGrant + Redis 락으로 중복 지급 방지 |

---

## 3. 프로젝트 구조 (수정 중)
```
Backend/
├─ src/
│  └─ main/
│     ├─ java/OneQ/OnSurvey/
│     │  ├─ domain/
│     │  │  ├─ form/                        # 설문 생성 및 수정
│     │  │  │  ├─ api/  
│     │  │  │  │  ├─ FormController
│     │  │  │  │  └─ dto/
│     │  │  │  │     ├─ request/
│     │  │  │  │     ├─ response/
│     │  │  │  │     └─ DefaultSurveyDto
│     │  │  │  ├─ application/
│     │  │  │  │  ├─ SurveyService
│     │  │  │  │  └─ QuestionService
│     │  │  │  ├─ domain/
│     │  │  │  │  ├─  model/                # 도메인 엔티티 (POJO)
│     │  │  │  │  │  ├─ Survey
│     │  │  │  │  │  └─ Question
│     │  │  │  │  └─  repository/
│     │  │  │  │     ├─ SurveyRepository
│     │  │  │  │     └─ QuestionRepository
│     │  │  │  └─ infra/
│     │  │  │     ├─ entity/                # 영속성 엔티티 (@Entity)
│     │  │  │     │  ├─ SurveyEntity
│     │  │  │     │  ├─ QuestionEntity
│     │  │  │     │  └─ ScreeningEntity
│     │  │  │     ├─ mapper/                # POJO <-> JpaEntity 컨버터
│     │  │  │     └─ jpa/
│     │  │  │        └─ SurveyJpaRepository
│     │  │  ├─ management/                  # 설문 관리 및 집계
│     │  │  ├─ member/                      # 사용자 관리
│     │  │  └─ participation/               # 설문 참여
│     │  └─ global/                         # 공유 설정
│     │     ├─ annotation/
│     │     ├─ auth/
│     │     ├─ config/
│     │     ├─ entity/
│     │     ├─ exception/
│     │     ├─ handler/
│     │     ├─ infra/
│     │     ├─ response/
│     │     └─ util/
│     └─ resources/
│        └─ application.yml                 # 환경 설정
├─ build.gradle
├─ Dockerfile
├─ docker-compose.yml
└─ README.md
```

---

## 4. 배포·운영
### Docker 이미지
```bash
# JAR 파일 빌드 (Gradle)
./gradlew bootJar

# 이미지 생성
docker build -t yourrepo/yourproject:latest .
```

---

## 4. 테스트
| 테스트 종류 | 실행 명령 | 비고 |
|------------|----------|------|
| 단위 테스트 | `./gradlew test` | JUnit  5 + Mockito |
| 통합 테스트 | `./gradlew integrationTest` (프로젝트에 정의) | Testcontainers 로 실제 DB 구동 |
| 커버리지 보고서 | `./gradlew jacocoTestReport` | `build/reports/jacoco/test/html/index.html` 확인 |

---

## 5. 멱등성 & 분산 락 (Promotion)

토스 프로모션 포인트 지급은 다음 조합으로 중복 지급을 방지합니다.

- DB 유니크 제약 + 낙관적 락 기반 멱등 처리
- Redis 기반 분산 락
- 토스 API 재시도 및 결과 폴링
- 포인트 지급 여부 플래그

---

### 5.1 PromotionGrant 기반 멱등 처리

- 엔티티: `promotion_grant`
- 유니크 제약 (1 유저 · 1 설문 · 1 코드당 1건)
    - `user_key`, `survey_id`, `promotion_code`

---

## 요약
| 섹션       | 핵심 내용                                      |
|----------|--------------------------------------------|
| 레포지토리 개요 | Java  21 + Spring  Boot  3 기반 백엔드 서버       |
| 주요 도메인   | 설문 생성, 관리, 참여 / 사용자 및 결제 관리                |
| 구조       | Bounded Context를 기반으로 한 도메인 분리             |
| 배포       | Dockerfile, docker‑compose, GitHub Actions |