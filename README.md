# 토닥토닥 (Todak-Todak) — Backend

> 하루 두 번, 위로와 응원의 글귀를 이메일로 전달하는 구독 서비스의 백엔드입니다.

## 기술 스택

| 영역 | 기술 |
|------|------|
| Framework | Spring Boot 4.0.4 |
| Language | Java 21 |
| Database | MySQL 8.0 |
| Cache | Redis 7 |
| Message Queue | Kafka 7.6.0 |
| Auth | Magic Link (Passwordless) + JWT |
| Email | Gmail SMTP + Thymeleaf 템플릿 |
| Batch | Spring Batch + Spring Scheduling |
| AI | Claude API (claude-haiku-4-5-20251001) |
| Migration | Flyway |
| Monitoring | Spring Actuator + Prometheus |

---

## 주요 기능

### 이메일 구독 플로우

```
1. 유저가 이메일/이름/카테고리로 구독 신청 (PENDING_VERIFICATION)
2. 이메일 인증 링크 자동 발송
3. 유저가 인증 링크 클릭 → 구독 ACTIVE 전환
4. 매일 07:00 / 19:00 이메일 자동 발송 (Batch + Kafka)
```

### 이메일 발송 아키텍처

```
EmailScheduler (07:00 / 19:00)
    → Spring Batch Job 실행
    → JpaPagingItemReader (ACTIVE 구독자 100명씩)
    → EmailContentProcessor (템플릿 처리)
    → KafkaEmailProducer (ongi.email.send 토픽, 6파티션)
    → KafkaEmailConsumer (Concurrency 3)
    → MailService (실제 이메일 발송)
    → SendHistory 기록
```

### 인증 방식

**Passwordless Magic Link** 방식:

```
1. 유저가 이메일 입력
2. 백엔드가 15분 유효 Magic Link 발송
3. 유저가 이메일 링크 클릭
4. JWT 발급 (유효기간 7일)
5. 이후 모든 보호된 API에 Bearer 토큰 첨부
```

---

## API 엔드포인트

Base URL: `/api/v1`

### 인증 (`/auth`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/auth/magic-link` | 매직 링크 발송 | 불필요 |
| GET | `/auth/magic-link/verify?token=` | 매직 링크 검증 → JWT 발급 | 불필요 |

### 구독자 (`/subscribers`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/subscribers` | 구독 신청 | 불필요 |
| GET | `/subscribers/verify?token=` | 이메일 인증 | 불필요 |
| DELETE | `/subscribers/{email}` | 구독 해지 | 불필요 |
| GET | `/subscribers/me` | 내 정보 조회 | JWT 필수 |
| PATCH | `/subscribers/me/preferences` | 선호 카테고리 변경 | JWT 필수 |

### 글귀 (`/quotes`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/quotes/today` | 오늘의 글귀 (Redis 캐시 12h) | 선택 |
| GET | `/quotes?category=&page=&size=` | 카테고리별 글귀 목록 | 선택 |
| GET | `/quotes/saved` | 내가 저장한 글귀 | JWT 필수 |
| POST | `/quotes/{id}/like` | 좋아요 토글 | JWT 필수 |
| POST | `/quotes/{id}/save` | 저장 토글 | JWT 필수 |
| POST | `/quotes/ai-generate` | AI 글귀 생성 (Claude) | JWT 필수 |

### 어드민 (`/admin`) — ADMIN 역할 필수

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/admin/stats` | 전체 통계 |
| GET | `/admin/subscribers?page=&size=` | 구독자 목록 (페이징) |
| GET | `/admin/send-history?page=&size=` | 발송 이력 (페이징) |

### 모니터링

| 경로 | 설명 |
|------|------|
| `/actuator/health` | 헬스 체크 |
| `/actuator/metrics` | 메트릭스 |
| `/actuator/prometheus` | Prometheus 메트릭 |

---

## 카테고리

| 값 | 한국어 |
|----|--------|
| `COMFORT` | 위로 |
| `CHEER` | 응원 |
| `ENCOURAGE` | 격려 |
| `SUPPORT` | 지지 |
| `CELEBRATE` | 축하 |
| `LOVE` | 사랑 |

---

## 환경변수

<!-- AUTO-GENERATED -->
| 변수 | 필수 | 설명 | 예시 |
|------|------|------|------|
| `DB_URL` | Yes | MySQL JDBC 연결 URL | `jdbc:mysql://localhost:13306/ongi_db` |
| `DB_USERNAME` | Yes | DB 사용자명 | `root` |
| `DB_PASSWORD` | Yes | DB 비밀번호 | `password` |
| `REDIS_URL` | Yes | Redis 연결 URL | `redis://localhost:6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Yes | Kafka 브로커 주소 | `localhost:9092` |
| `GMAIL_USERNAME` | Yes | 발송자 Gmail 주소 | `example@gmail.com` |
| `GMAIL_APP_PASSWORD` | Yes | Gmail 앱 비밀번호 | `xxxx xxxx xxxx xxxx` |
| `JWT_SECRET` | Yes | JWT 서명용 시크릿 키 (256bit 이상) | `your-secret-key` |
| `CLAUDE_API_KEY` | Yes | Claude AI API 키 | `sk-ant-...` |
| `FRONTEND_URL` | Yes | 프론트엔드 URL (CORS, 쉼표로 다수 지정 가능) | `http://localhost:3000` |
<!-- AUTO-GENERATED -->

`.env` 파일 생성 (docker-compose 사용 시):

```env
DB_URL=jdbc:mysql://localhost:13306/ongi_db
DB_USERNAME=root
DB_PASSWORD=password
REDIS_URL=redis://localhost:6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
GMAIL_USERNAME=example@gmail.com
GMAIL_APP_PASSWORD=xxxx xxxx xxxx xxxx
JWT_SECRET=your-256-bit-secret-key
CLAUDE_API_KEY=sk-ant-...
FRONTEND_URL=http://localhost:3000
```

---

## 스크립트

<!-- AUTO-GENERATED -->
| 커맨드 | 설명 |
|--------|------|
| `./gradlew bootRun` | 개발 서버 시작 |
| `./gradlew build` | 프로덕션 빌드 (JAR 생성) |
| `./gradlew test` | 테스트 실행 |
| `./gradlew clean` | 빌드 결과물 삭제 |
| `./gradlew clean build` | 클린 빌드 |
<!-- AUTO-GENERATED -->

---

## 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose

### 로컬 개발 환경 실행

```bash
# 1. 인프라 (MySQL, Redis, Kafka, Zookeeper) 실행
docker-compose up -d

# 2. 환경변수 설정
cp .env.example .env  # .env.example이 없다면 위의 환경변수 참고하여 직접 작성

# 3. 서버 실행
./gradlew bootRun
```

서버: [http://localhost:8080](http://localhost:8080) / 헬스 체크: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Docker Compose 인프라 구성

| 서비스 | 이미지 | 포트 |
|--------|--------|------|
| MySQL | mysql:8.0 | 13306:3306 |
| Redis | redis:7-alpine | 6379:6379 |
| Zookeeper | confluentinc/cp-zookeeper:7.6.0 | 2181 |
| Kafka | confluentinc/cp-kafka:7.6.0 | 9092:9092 |

---

## 프로젝트 구조

```
src/main/java/com/ongi/
├── domain/                          # 비즈니스 도메인
│   ├── admin/                       # 관리자
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   ├── auth/                        # 인증 (Magic Link, JWT)
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/                  # MagicLinkToken
│   │   ├── repository/
│   │   └── service/
│   ├── quote/                       # 글귀
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/                  # Quote, QuoteLike, QuoteSave
│   │   ├── repository/
│   │   └── service/
│   └── subscriber/                  # 구독자
│       ├── controller/
│       ├── dto/
│       ├── entity/                  # Subscriber, SendHistory
│       ├── repository/
│       └── service/
├── infra/                           # 인프라 계층
│   ├── claude/                      # Claude AI 클라이언트
│   ├── config/                      # SecurityConfig, WebConfig 등
│   ├── jwt/                         # JWT 생성/검증
│   ├── kafka/                       # Kafka 설정, Consumer
│   ├── mail/                        # MailService (비동기)
│   ├── redis/                       # Redis 캐시 설정
│   └── security/                    # JwtAuthFilter, AdminAuthFilter
├── batch/                           # 이메일 배치
│   ├── config/                      # EmailSendJobConfig
│   ├── dto/                         # EmailMessageDto
│   ├── processor/                   # EmailContentProcessor
│   ├── scheduler/                   # EmailScheduler (07:00 / 19:00)
│   └── writer/                      # KafkaEmailProducer
└── global/                          # 전역 공통
    ├── exception/                   # GlobalExceptionHandler
    ├── response/                    # ApiResponse<T>
    └── util/                        # TokenGenerator
```

---

## 데이터베이스 스키마

Flyway 마이그레이션으로 자동 관리 (`src/main/resources/db/migration/`)

| 파일 | 내용 |
|------|------|
| V1__init.sql | 초기 스키마 (전체 테이블) |
| V2__seed_quotes.sql | 카테고리별 초기 글귀 데이터 (6종 × 20건) |
| V3__add_categories.sql | 카테고리 관련 변경 |
| V4__add_magic_link.sql | tb_magic_link_token 테이블 |
| V5__add_quote_interactions.sql | tb_quote_like, tb_quote_save 테이블 |
| V7__add_admin_indexes.sql | 어드민 쿼리 인덱스 |
| V8__add_subscriber_role.sql | Subscriber role 컬럼 추가 |

**주요 테이블**

| 테이블 | 설명 |
|--------|------|
| `tb_quote` | 글귀 (MANUAL / AI 생성) |
| `tb_subscriber` | 구독자 (USER / ADMIN 역할) |
| `tb_send_history` | 이메일 발송 이력 |
| `tb_quote_like` | 글귀 좋아요 |
| `tb_quote_save` | 글귀 저장 |
| `tb_magic_link_token` | 매직 링크 토큰 |

---

## 이메일 템플릿

`src/main/resources/templates/email/`

| 파일 | 발송 시점 |
|------|----------|
| `morning-email.html` | 매일 07:00 |
| `evening-email.html` | 매일 19:00 |
| `verify-email.html` | 구독 신청 시 |
| `magic-link-email.html` | 로그인 요청 시 |

---

## 관련 레포지토리

- **Frontend**: [ongi-fe](https://github.com/) — Next.js 15 + TypeScript + Tailwind CSS
