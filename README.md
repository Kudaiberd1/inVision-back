# InVision — backend

Spring Boot API for an admissions-style flow: applicants submit CV and essay (PDF) plus an intro video; an external **AI evaluator** scores documents and can run a **chatbot interview**; reviewers use a **JWT-protected dashboard** to browse candidates, open documents (TeX preview + PDF URL), and update application status.

## Stack

- Java **21**, Spring Boot **4.x**
- PostgreSQL, Spring Data JPA
- Spring Security + **JWT**
- AWS S3 (upload + public URLs; dashboard re-downloads PDFs for text extraction)
- Apache PDFBox (PDF → text → LaTeX `verbatim` snippet for dashboard)
- springdoc-openapi (Swagger UI)
- `RestTemplate` → external Python AI service

## Prerequisites

- JDK 21
- PostgreSQL (create a database, e.g. `invision`)
- AWS credentials with S3 access to your bucket
- Running **AI service** (default base URL `http://127.0.0.1:8000`) exposing at least:
  - `POST /evaluate/pdf` (multipart: `file`, `mode` = `cv` | `essay`, `user_id`)
  - `POST /interview/start` and `POST /interview/{sessionId}/reply` (proxied as-is)

## Configuration

Copy or edit `src/main/resources/application.yaml`. Prefer **environment variables** or a local `application-local.yaml` (gitignored) for secrets.

| Area | Properties / env (examples) |
|------|-----------------------------|
| Database | `spring.datasource.url`, `username`, `password` |
| JWT | `jwt.secret`, `jwt.expiration` (ms) |
| AWS S3 | `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`; `aws.s3.bucket-name`, `aws.region` |
| AI | `ai.evaluator.base-url` (no trailing slash logic is handled in code) |
| Upload limits | `spring.servlet.multipart.max-file-size`, `max-request-size` |

## Run

```bash
./gradlew bootRun
```

Default servlet port is **8080** unless overridden.

## API documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Dashboard endpoints are annotated with bearer auth; use **Authorize** in Swagger with `Bearer <token>` after login.

## Security model

| Access | Paths |
|--------|--------|
| **No JWT** | `/api/auth/**`, `/api/forms`, `/api/forms/**`, `/api/interview/**`, `/swagger-ui/**`, `/v3/api-docs/**` |
| **JWT required** | `/api/dashboard/**` and any other non-listed routes |

Login: `POST /api/auth/login` with JSON `{ "username": "<email>", "password": "<password>" }` → `{ "token": "<jwt>" }`.  
Send `Authorization: Bearer <token>` for dashboard calls.

## Main endpoints

### Applicant

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/forms` | Multipart submit (201, body `{"id": <formId>}`) |

**Multipart parts** (field names): `fullName`, `email`, `phone` (optional), `dateOfBirth` (`MM/dd/yyyy`), `city`, `schoolUniversity`, `gpa`, `fieldOfStudy`, `cv` (PDF ≤ 5 MB), `motivationEssay` (PDF ≤ 5 MB), `introductionVideo` (MP4 ≤ 50 MB).

Flow: validate files → call AI twice (CV + essay) → upload to S3 → persist `Form`, `CVReview`, `EssayReview` in one transaction.

### Interview (proxied to AI)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/interview/start` | Body: `candidateId`, `candidateStage` (normalized to `school` / `university` / `unknown`) |
| `POST` | `/api/interview/{sessionId}/reply` | Forwards to AI; when the interview completes, result is stored in `interview_results` |

### Dashboard (JWT)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/dashboard/candidates` | List candidates that have both CV and essay reviews |
| `GET` | `/api/dashboard/candidates/{id}/cv-review` | `cvFullText` (TeX), `cvPdfUrl`, `cvReview` |
| `GET` | `/api/dashboard/candidates/{id}/essay-review` | `essayFullText` (TeX), `essayPdfUrl`, `essayReview` |
| `GET` | `/api/dashboard/candidates/{id}/chatbot-analysis` | From stored interview JSON (linked by `candidate_id` / email heuristics) |
| `PATCH` | `/api/dashboard/candidates/{id}/status` | JSON `{ "status": "PENDING" \| "ACCEPTED" \| "REJECTED" }` → 204 |

## Project layout (high level)

- `controller/` — REST entry points (`Auth`, `Form`, `Interview`, `Dashboard`, …)
- `service/` — business logic (`FormService`, `AISummarizeService`, `S3Service`, `DashboardService`, …)
- `entity/` — JPA models (`Form`, `CVReview`, `EssayReview`, `InterviewResult`, `User`, …)
- `repository/` — Spring Data repositories
- `config/` — Security, JWT, OpenAPI, AWS S3 client
- `security/` — JWT filter and helpers

## Notes for frontend

- **CV/essay panels**: use `*FullText` with a TeX-capable renderer or strip the `verbatim` wrapper and show plain text in `<pre>`; use `*PdfUrl` for embed / new tab / download.
- **S3 PDF in browser**: may require correct bucket CORS or a backend proxy if you hit cross-origin issues.
- **Interview ↔ candidate**: dashboard matches `InterviewResult` to a form by candidate id string or email; aligning `candidateId` with your applicant identity improves linking.

## Build & test

```bash
./gradlew build
```

---

Hackathon / MVP focus: **`POST /api/forms`** + AI integration + S3 + PostgreSQL, plus **`/api/dashboard/*`** for reviewers with JWT.
