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

## 요약
| 섹션       | 핵심 내용                                      |
|----------|--------------------------------------------|
| 레포지토리 개요 | Java  21 + Spring  Boot  3 기반 백엔드 서버       |
| 주요 도메인   | 설문 생성, 관리, 참여 / 사용자 및 결제 관리                |
| 구조       | Bounded Context를 기반으로 한 도메인 분리             |
| 배포       | Dockerfile, docker‑compose, GitHub Actions |