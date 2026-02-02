# StartHub Server 🚀

스타트업의 시작부터 성장까지, 한 곳에서 도와주는 플랫폼의 백엔드 서버입니다.
BMC(Business Model Canvas), 경쟁사 분석, 그리고 정부 부처에서 제공하는 다양한 사업 공고 정보를 제공합니다.

---

## 주요 기능

### 📋 BMC (Business Model Canvas)
- AI 기반 BMC 작성 지원
- 질문-응답 방식의 대화형 BMC 생성
- BMC 수정 및 관리

### 📊 경쟁사 분석
- AI를 활용한 경쟁사 분석 리포트 생성
- 강점/약점 분석 및 글로벌 전략 제안

### 📢 정부 지원사업 공고
- K-Startup, 기업마당(BizInfo) 공고 자동 스크래핑
- 첨부파일(HWP, PDF) 자동 다운로드 및 PDF 변환
- AI 기반 맞춤 공고 추천
- 자연어 검색 지원

### 💬 AI 챗봇
- 사용자 컨텍스트 기반 맞춤형 상담
- RAG 기반 정확한 정보 제공

### 🔔 알림
- Firebase Cloud Messaging 푸시 알림
- 공고 마감 알림

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Kotlin 1.9 |
| **Framework** | Spring Boot 3.5 |
| **Database** | MySQL, Redis |
| **ORM** | Spring Data JPA |
| **Security** | Spring Security, OAuth2, JWT |
| **AI** | Spring AI (OpenAI, Anthropic) |
| **Cloud Storage** | Google Cloud Storage |
| **Push Notification** | Firebase Cloud Messaging |
| **Documentation** | Swagger / OpenAPI 3.0 |
| **Scraping** | Jsoup |
| **Document Processing** | Apache PDFBox, Apache POI, HWPLib, LibreOffice |

---

## 아키텍처

```
src/main/kotlin/com/jininsadaecheonmyeong/starthubserver/
├── application/           # UseCase, Service
├── domain/               # Entity, Repository, Exception, Enum
├── presentation/         # Controller, DTO, Docs
├── infrastructure/       # Scheduler, Scraping, Conversion
└── global/              # Security, Config, Common
```

---

## 스크린샷

<img width="1440" alt="메인 페이지" src="https://github.com/user-attachments/assets/9290eaff-985c-4f08-8d31-a3db084fbc02" />

<img width="1439" alt="공고 목록" src="https://github.com/user-attachments/assets/2ce04c9f-5eb4-4c8a-b73e-6f8e9f094c19" />

<img width="1920" alt="공고 상세" src="https://github.com/user-attachments/assets/660d4ca8-3b3c-4290-9231-ec52b790a0c0" />

<img width="1440" alt="BMC 작성" src="https://github.com/user-attachments/assets/96e3233d-320f-4713-9f6d-3414c9352b01" />

<img width="1920" alt="BMC 결과" src="https://github.com/user-attachments/assets/cc15357a-d432-47dc-b752-084f32715b61" />

<img width="1440" alt="경쟁사 분석" src="https://github.com/user-attachments/assets/7510431d-83ab-442a-8f19-7b2529380a5e" />

<img width="1440" alt="AI 챗봇" src="https://github.com/user-attachments/assets/48691500-4161-4c42-b6a5-361be895d12e" />

<img width="1440" alt="맞춤 추천" src="https://github.com/user-attachments/assets/a3bb5019-0517-4efc-b885-83be74b17925" />

<img width="1440" alt="일정 관리" src="https://github.com/user-attachments/assets/0a131ebe-2487-428c-94fc-10ac9512bea4" />

<img width="1440" alt="마이페이지" src="https://github.com/user-attachments/assets/6c850fd4-d659-4dfc-9841-28697d0f042b" />
