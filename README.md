
# 👕 FitMe!

![GitHub Actions Workflow Status](https://github.com/FitMe-FitMeUp/FitMe/actions/workflows/cd-deploy.yml/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/FitMe-FitMeUp/fitme/branch/main/graph/badge.svg?)](https://codecov.io/gh/FitMe-FitMeUp/fitme)

> **"오늘도 당신의 스타일을 책임집니다"**
> 사용자의 옷장과 실시간 날씨 데이터를 기반으로 최적의 코디를 제안하고 소통하는 개인 맞춤형 패션 플랫폼

---

## 🔗 프로젝트 링크
- **배포 URL:** https://fitme-nginx.mangofield-4b56edf3.koreacentral.azurecontainerapps.io/
- **팀 협업 문서:** https://www.notion.so/FitMe-51b7885bebca83639cd7819aaf5ba042
- **발표 자료:** [Fitme 발표](https://github.com/user-attachments/files/26814162/Fitme.pdf.pdf)
---

## 👥 팀원 구성 및 역할
- **김태언(팀장):** CI/CD 파이프라인 구축 + 인프라 구축, 피드 관리(게시물, 댓글, 좋아요 도메인 전반 설계 및 구현)
- **김진우:** CI/CD 파이프라인 구축 + 인프라 구축, DM(WebSocket), 팔로우 기능
- **신제원:** 실시간 알림 시스템 (SSE)
- **이형일:** 인증 관리, 프로필 관리, 날씨 관리(Spring Batch 기반)
- **최현석:** 맞춤형 추천 엔진 개발
- **조성연:** 의상/속성 도메인 설계, LLM 데이터 분석 파이프라인 구축, 미디어 파일 관리 및 성능 최적화, 프론트엔드 UI/UX 개선
---

## 📝 프로젝트 소개
- **목적:** 사용자의 옷장을 디지털화하고, 실시간 날씨와 AI 분석을 기반으로 코디를 추천하며, 피드, 댓글, 좋아요, DM, 실시간 알림 기능을 통해 사용자 간 스타일을 공유하고 소통할 수 있는 플랫폼 제공
- **핵심 기능:**
    - 구매 링크 기반 의상 정보 자동 등록 (AI 추출)
    - 위치 기반 실시간 날씨를 활용한 맞춤 코디 추천
    - OOTD 피드 공유, 댓글, 좋아요, 팔로우, DM 기능을 통한 사용자 간 소통
    - SSE 기반 실시간 알림 시스템 제공
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
    - **네트워크:** VNet, NAT Gateway, Nginx (Reverse Proxy 및 트래픽 라우팅, Outbound IP 고정 및 외부 API whitelist 대응)
    - **컨테이너 환경:** ACR, Azure Container Apps (컨테이너 기반 서비스 배포 및 운영)
    - **데이터 저장소:** PostgreSQL, Blob Storage (미디어 파일 저장)
    - **캐시:** Azure Cache for Redis (조회 성능 최적화)
    - **메시징:** Kafka / Event Hub (비동기 이벤트 처리)
    - **검색:** Elasticsearch (VM 기반 직접 운영)

### Monitoring & Testing
![Spring Actuator](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Jacoco](https://img.shields.io/badge/Jacoco-8A2BE2?style=for-the-badge&logo=eclipse&logoColor=white)
![Codecov](https://img.shields.io/badge/Codecov-F01F7A?style=for-the-badge&logo=codecov&logoColor=white)
- **모니터링:** Spring Actuator를 통해 애플리케이션 상태 및 메트릭을 수집하고, Prometheus로 수집된 데이터를 Grafana를 통해 시각화하여 운영 상태를 모니터링
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

### 🔹 김태언(팀장)
- **피드 관리:** 피드 게시물 생성, 조회, 좋아요, 댓글 기능 개발
- **검색 기능 구현:** 사용자 입력(오타, 부분 검색 등)을 고려한 피드 검색 기능 개발
- **조회 기능 최적화:** 대량 데이터 조회를 고려한 댓글 및 피드 목록 조회 구조 개선
- **인프라 설계 및 운영:** Azure 기반 클라우드 인프라 및 로컬 Docker Compose 환경 구성

### 트러블슈팅

**피드 도메인을 중심으로 검색 품질, 조회 성능, 동시성, 인프라 안정성 개선**

#### 1. 좋아요 동시성 처리

- DB atomic update 적용으로 race condition 방지 및 데이터 정합성 확보
- 낙관적/비관적 락 대신 단일 update 쿼리 방식으로 고빈도 좋아요 요청 대응

#### 2. 부분 검색 지원

- Elasticsearch ngram analyzer 적용으로 content 중간 문자열 검색 지원
- Nori, fuzziness와 결합하여 오타·부분 검색·단어 순서 변경 대응

#### 3. 목록 조회 성능 개선

- 피드 목록 조회 시 feedId 기반 batch query(IN 조회) 적용으로 하위 데이터 일괄 조회
- Map 기반 메모리 그룹핑으로 DTO 조립 및 N+1 문제 완화

#### 4. 조회 구조 개선

- FeedQueryService/Repository 분리로 조회 로직과 도메인 로직 책임 분리
- Projection 기반 조회 후 조립 구조 적용으로 유지보수성 및 확장성 개선

#### 5. 외부 API 연동 안정화

- Azure NAT Gateway 적용으로 outbound IP 고정 및 whitelist 기반 외부 API 호출 문제 해결
- 내부 서비스와 외부 노출 대상 분리로 운영 환경 네트워크 안정성 개선

### 🔹 김진우
- **CD + 인프라 구축:** Azure 기반 자동화 배포 환경 구축
- **DM (WebSocket):** 실시간 메시징 시스템 구현
- **팔로우:** 사용자 관계 관리 기능 개발

### 트러블슈팅
- 담당자가 추가
  
### 🔹 신제원
- **알림 (SSE):** 실시간 활동 알림 시스템 구현

### 트러블슈팅
- 담당자가 추가

### 🔹 이형일
- **인증/프로필:** OAuth2 및 JWT 보안, 유저 정보 관리 개발
- **날씨 관리:** Spring Batch 기반 공공 API 데이터 수집 파이프라인 구축

### 트러블슈팅
**핵심 사용자 흐름(사용자, 프로필, 날씨 조회)을 책임지고, 로컬 중심 구현을 운영 환경으로 확장 가능한 구조로 정리**

#### 1. 인증/인가 코어 안정화 (JWT 무효화 정책 고도화)
- JWT 기반 인증에 jti 블랙리스트와 사용자 단위 revokeAllBefore(cutoff)를 함께 적용해, 단일 토큰 폐기와 전체 세션 강제 무효화를 모두 지원.
- refresh 시간 불일치 이슈(iat vs cutoff)를 정리해 간헐적 401 발생 케이스를 해결.

#### 2. 임시 비밀번호 재설정 플로우 구축 (트랜잭션/비동기 분리)
- 비밀번호 찾기에서 임시 비밀번호 검증, 새 비밀번호 설정, 토큰 재발급까지 한 사이클을 서비스 정책과 일치시키도록 구현.
- 메일 발송은 AFTER_COMMIT 이벤트 리스너로 분리해 데이터 정합성을 확보했고, 실패 시 재시도 큐(지수 백오프/최대 횟수)로 복구 가능한 구조를 적용.

#### 3. 저장소 전환 설계 (In-Memory → Redis)
- 임시 비밀번호 저장소, 토큰 블랙리스트, 메일 재시도 큐를 조건부 빈 전략으로 구현해 로컬(In-Memory)과 운영(Redis) 전환이 가능하도록 설계. 
- 단일 인스턴스에서 벗어나 멀티 인스턴스/재기동 상황에서도 상태 일관성이 유지되도록 구성.

#### 4. OAuth2 로그인 실연동 및 런타임 정책 정리
- Google/Kakao OAuth2 연동에서 Redirect URI, 쿠키, 프록시 헤더, 베이스 URL 계산 불일치 이슈를 정리.
- AppRuntimePolicy를 도입해 환경별 URL/secure 판단/CORS 정책을 중앙화.
- 성공·실패 핸들러의 리다이렉트 흐름을 정렬해 로컬/배포 간 동작 차이를 축소.

#### 5. 날씨 조회 신뢰성 개선
- 날씨 성공 + 빈 배열 상황에서 fallback 수집 후 즉시 재조회 경로를 추가해 초기 미노출 문제를 완화. 
- 일별 집계 로직을 정리해 평균/최저/최고가 동일하게 보이던 왜곡 케이스를 개선.
- 수집/매핑 책임 분리로 유지보수성과 테스트 용이성을 높임.

### 🔹 최현석
- **추천 엔진:** 사용자 데이터 기반 개인화 코디 추천 로직 개발

### 트러블슈팅
- 담당자가 추가
### 🔹 조성연 
- **옷 관리:** 의상 등록, 조회 및 사용자 옷장 데이터 관리 기능 구현
    - 구매 링크 기반 상품 정보 자동 추출 기능 구현 (스크래핑 + LLM)
    - 이미지 업로드 및 삭제 기능 구현
- **옷 속성 관리:** 의상 속성 및 선택값 구조 설계 및 관리 기능 구현

### 트러블슈팅

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

## 📊 ERD

<img width="4310" height="1782" alt="FitMe" src="https://github.com/user-attachments/assets/a8c2b535-5b55-43cb-ad94-b5e8c918b0a9" />

ERD 상세 보기: [erdcloud 링크](https://www.erdcloud.com/d/WNbv4ftDp9Kgq7MFb)

### ERD 핵심 도메인 설명

- **Feed**
  - 사용자 생성 콘텐츠(OOTD)의 중심 테이블
  - 좋아요/댓글/검색 기능이 모두 연결되는 핵심 도메인

- **User**
  - 인증 및 사용자 식별을 담당하는 기본 엔티티
  - 피드, 팔로우, 좋아요 등 모든 도메인의 기준이 되는 주체

- **Clothes**
  - 의상 정보 및 속성을 관리하는 도메인
  - LLM 기반 데이터 추출 및 추천 기능과 직접 연결

- **Weather**
  - 지역 및 시간 기반 날씨 데이터를 관리
  - 코디 추천 로직의 핵심 입력 데이터로 활용
---

## 📂 파일 구조
### 애플리케이션 구조

```text

com.ootd.fitme

├── domain

│   ├── feed (피드, 댓글, 좋아요)

│   ├── clothes (의상, 속성, 카탈로그)

│   ├── user (유저, 프로필, 팔로우)

│   ├── notification (알림, SSE/Kafka)

│   ├── directmessage (DM)

│   ├── recommendation (코디 추천)

│   └── weather (날씨, 지역, 배치)

│

├── global

│   ├── config (Security, Redis, Kafka, ES 등)

│   ├── security (JWT, OAuth2)

│   ├── exception

│   └── interceptor

│

├── infrastructure

│   ├── ai (LLM 추출)

│   ├── scraper (Playwright, Jsoup)

│   ├── external (Kakao, Google, Weather API)

│   ├── realtime (WebSocket)

│   └── storage (이미지, 로그)

│

└── FitmeApplication.java
```

### 운영 및 배포 구조

```text

project-root
├── Dockerfile / Dockerfile.nginx / Dockerfile.elasticsearch
├── compose.yml
├── monitoring
│   ├── grafana
│   └── prometheus
├── nginx
│   ├── nginx.conf.local
│   └── nginx.conf.prod
└── src
    ├── main
    └── test
```



---

## 개인 개발 문서(Report)
- **김태언:** https://innovative-sunshine-4ce.notion.site/Fitme-34d86ebf7b7f80ae9148f3123b37c7ca
- **김진우:**
- **신제원:**
- **이형일:** https://www.notion.so/347f4865b83680039418e77a12900f3e?v=28af4865b83681b49704000c3c73b2b5&source=copy_link
- **최현석:**
- **조성연:** [개발 리포트](https://m-ywork-story.tistory.com/entry/%EA%B0%9C%EB%B0%9C-%EB%A6%AC%ED%8F%AC%ED%8A%B8-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EB%A5%BC-%EB%A7%88%EC%B9%98%EB%A9%B0-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98%EB%B6%80%ED%84%B0-E2E-%ED%8C%8C%EC%9D%B4%ED%94%84%EB%9D%BC%EC%9D%B8%EA%B9%8C%EC%A7%80%EC%9D%98-%EA%B8%B0%EB%A1%9D) | [프로젝트 회고](https://m-ywork-story.tistory.com/entry/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%ED%9A%8C%EA%B3%A0-FitMe)
