# 👕 FitMe!
> **"오늘도 당신의 스타일을 책임집니다"**
> 사용자의 옷장과 실시간 날씨 데이터를 기반으로 최적의 코디를 제안하고 소통하는 개인 맞춤형 패션 플랫폼

---

## 🔗 프로젝트 링크
- **팀 협업 문서:** [https://www.notion.so/FitMe-51b7885bebca83639cd7819aaf5ba042](#)
- **발표 자료:** [PDF/Slide 링크](#)

---

## 👥 팀원 구성 및 역할
- **김진우:** CD + 인프라 구축, DM(WebSocket), 팔로우 기능
- **김태언:** CD + 인프라 구축, 피드 관리, 날씨 관리(Redis 심화)
- **신제원:** 실시간 알림 시스템 (SSE)
- **이형일:** 인증 관리, 프로필 관리, 날씨 관리(Spring Batch 기반)
- **최현석:** 맞춤형 추천 엔진 개발
- **조성연:** 의상/속성 도메인 설계, LLM 데이터 분석 파이프라인 구축, 미디어 파일 관리 및 성능 최적화, 프론트엔드 UI/UX 개선
---

## 📝 프로젝트 소개
- **목적:** 날씨에 따른 의상 선택의 번거로움을 해결하고, 자신의 옷장을 디지털화하여 관리하는 서비스 제공
- **핵심 기능:** - 구매 링크 기반 의상 정보 자동 등록 (AI 추출)
    - 위치 기반 실시간 날씨 맞춤 코디 추천
    - OOTD 피드 공유 및 사용자 간 팔로우/DM 소통
- **프로젝트 기간:** 2026.03.10 ~ 2026.04.17

---

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security%20(OAuth2)-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge&logo=spring&logoColor=white)

### AI & Data Extraction
![Spring AI](https://img.shields.io/badge/Spring%20AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Grok](https://img.shields.io/badge/LLM%20API%20(Grok)-000000?style=for-the-badge&logo=&logoColor=white)
![Playwright](https://img.shields.io/badge/Playwright-2EAD33?style=for-the-badge&logo=playwright&logoColor=white)

### Database & Message Queue
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)

### Infrastructure & DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![NGINX](https://img.shields.io/badge/NGINX-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Microsoft Azure](https://img.shields.io/badge/Microsoft%20Azure-0089D6?style=for-the-badge&logo=microsoftazure&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
- **CI/CD 파이프라인:** GitHub Actions를 활용하여 빌드 및 배포 자동화 구축
- **안정성 보장:** PR 생성 시 CI에서 테스트를 통과하지 못하면 Merge를 원천 차단하도록 정책 설정
- **Azure 인프라:**
    - **VNet (Virtual Network):** 독립된 사설 네트워크 망을 통한 보안 강화
    - **ACR / 컨테이너 환경:** Docker 기반 이미지 관리 및 배포
    - **Blob Storage:** 의상 및 프로필 미디어 객체 저장소

### Monitoring & Testing
![Spring Actuator](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Jacoco](https://img.shields.io/badge/Jacoco-8A2BE2?style=for-the-badge&logo=eclipse&logoColor=white)
![Codecov](https://img.shields.io/badge/Codecov-F01F7A?style=for-the-badge&logo=codecov&logoColor=white)
- **Coverage Policy:** 단위/통합 테스트 코드 작성 후 Jacoco로 커버리지를 측정하고, Codecov와 연동하여 실시간 모니터링 진행

### API & External Services
![Swagger](https://img.shields.io/badge/Swagger%20UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
- **External API:** OpenWeather API(날씨), Kakao Dev API(소셜 로그인/지도), Google API(소셜 로그인)

### Collaboration Tools
![GitHub](https://img.shields.io/badge/GitHub%20(WBS)-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)

## 💻 팀원별 구현 기능 상세

### 🔹 김태언
- **피드 관리:** OOTD 게시물 업로드 및 조회 기능 개발
- **날씨 관리:** Redis를 이용한 날씨 데이터 조회 성능 최적화

### 🔹 김진우
- **CD + 인프라 구축:** Azure 기반 자동화 배포 환경 구축
- **DM (WebSocket):** 실시간 메시징 시스템 구현
- **팔로우:** 사용자 관계 관리 기능 개발

### 🔹 신제원
- **알림 (SSE):** 실시간 활동 알림 시스템 구현

### 🔹 이형일
- **인증/프로필:** OAuth2 및 JWT 보안, 유저 정보 관리 개발
- **날씨 관리:** Spring Batch 기반 공공 API 데이터 수집 파이프라인 구축

### 🔹 최현석
- **추천 엔진:** 사용자 데이터 기반 개인화 코디 추천 로직 개발

### 🔹 조성연 (의상/속성 관리 및 데이터 최적화)
**비즈니스 로직의 핵심인 의상 도메인을 총괄하며 AI 연동과 시스템 최적화**

#### 1. 도메인 생명주기 통제 (DDD Aggregate)
- **Aggregate 설계:** `Clothes`와 `Attribute`를 루트 애그리거트로 설정하여 하위 엔티티(`SelectableValue`, 조인 테이블)의 생명주기를 완벽히 통제
- **영속성 관리:** JPA의 `orphanRemoval`과 DB의 `@OnDelete(CASCADE)`를 이중 적용하여 데이터 무결성 보장 및 삭제 성능 최적화

#### 2. AI 기반 의상 추출 및 비용 절감
- **LLM 파이프라인:** Playwright 스크래핑과 LLM 분석을 결합하여 상품명, 타입, 상세 속성을 자동 추출
- **캐싱 아키텍처:** URL 정규화(Normalization)를 통해 동일 상품 요청 시 LLM 서버 호출을 생략하는 `CatalogClothes` 캐싱 계층 구현 (응답 속도: **5초 → 50ms**)

#### 3. 미디어 파일 인프라 및 비동기 스케줄링
- **트랜잭션 분리:** 이미지 업로드/삭제 등 무거운 I/O 작업을 메인 비즈니스 트랜잭션에서 분리하여 응답 성능 향상
- **가비지 컬렉션:** Soft Delete 상태의 파일을 `@Scheduled` 기반 백그라운드 스케줄러를 통해 배치로 일괄 물리 삭제 (Azure Blob Storage)

#### 4. 프론트엔드 UI/UX 개선 및 E2E 트러블 슈팅
- **연동 최적화:** AI 분석 대기 시간을 고려한 비동기 흐름 제어 및 `AbortController` 적용
- **CORS 우회:** 외부 이미지 링크를 Blob 데이터로 변환 후 File 객체로 캐스팅하여 기존 파일 업로드 로직과 일관성 유지

---

## 📂 파일 구조
```text

```

## 🌐 구현 홈페이지
- **배포 URL:** [https://fitme-nginx.mangofield-4b56edf3.koreacentral.azurecontainerapps.io](#)

## 📝 프로젝트 회고록 (Deep-Dive)
- **JPA 도메인 생명주기 완벽 통제**
    - 복잡하게 얽힌 5개 이상의 엔티티를 Aggregate로 묶어 데이터 무결성을 확보
- **백엔드 개발자의 E2E 트러블 슈팅**
    - React 환경에서의 비동기 타임아웃 해결 및 이미지 CORS 문제를 File 객체 변환으로 우회
- **보이지 않는 병목, I/O 트랜잭션 분리**
    - 외부 스토리지 I/O를 메인 트랜잭션에서 분리하고 스케줄러를 통해 배치 처리한 성능 최적화
- **비싼 LLM API, 캐싱으로 비용 절감**
    - URL 정규화 로직과 DB 캐싱을 통해 응답 속도를 5초에서 50ms로 단축시킨 아키텍처 개선

---

## 💭 팀 회고 (Retrospective)
- **김태언:**
- **김진우:**
- **신제원:**
- **이형일:**
- **최현석:**
- **조성연:** 단순한 기능 구현을 넘어 '최적의 설계'를 끊임없이 고민했습니다. JPA 도메인 통제부터 I/O 트랜잭션 분리, LLM 비용 최적화를 위한 캐싱, 그리고 Azure 인프라 환경에서의 스크래핑 차단 우회까지. 백엔드 시스템의 다양한 병목을 직접 뚫어내며 **'성능과 비즈니스를 동시에 고려하는 엔지니어링'**의 본질을 배웠습니다. 한계에 부딪힐 때마다 치열하게 머리를 맞대준 팀원들에게 깊이 감사하며, 앞으로도 아키텍처를 깊게 파고들며 집요하게 문제를 해결하는 개발자로 성장하겠습니다.



- **팀 전체 발표 자료:** [FitMe! 프로젝트 발표 Slide 링크](#)
