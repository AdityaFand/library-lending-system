# Library Lending System

A backend (REST API) for a small library. A **librarian** adds books and lends them to members. **Members** search books, reserve books that are taken, and see their own loans and fines.

- **Swagger UI:** http://localhost:8000/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8000/v3/api-docs

---

## 1. Tech stack

| Part | Used |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Security, Validation) |
| Database | MySQL 8 (hosted on Aiven) |
| Login | JWT tokens (jjwt) with BCrypt passwords |
| API docs | Swagger / OpenAPI (springdoc) |
| Build | Maven (wrapper included, no install needed) |

---

## 2. How to run

### Step 1: What you need
- Java 17 or newer
- A MySQL 8 database (local MySQL or any cloud MySQL such as Aiven)

### Step 2: Create a `.env` file
Copy `.env.example` to `.env` in the project root and fill in your values:

```properties
DB_URL=jdbc:mysql://<host>:<port>/<database>?sslMode=REQUIRED
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<base64-encoded-secret-at-least-32-bytes>
```

- For a local MySQL without SSL, use `?sslMode=DISABLED` instead.
- To create a `JWT_SECRET`, run one of these:
  ```bash
  openssl rand -base64 48
  ```
  ```powershell
  [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
  ```

`.env` is in `.gitignore`, so secrets are never committed.

### Step 3: Start the app
```bash
./mvnw spring-boot:run        # Linux / Mac / Git Bash
mvnw.cmd spring-boot:run      # Windows CMD
```
Or in STS / Eclipse / IntelliJ: run `LibraryLendingSystemApplication`.

The app starts on **port 8000**. Tables are created automatically on first start.

### Step 4: Log in
On first start a librarian account is created automatically:

| Role | Email | Password |
|---|---|---|
| Librarian | `librarian@library.com` | `Librarian@123` |

Members create their own accounts with **Register**.

You can change the default librarian with the `LIBRARIAN_EMAIL` and `LIBRARIAN_PASSWORD` variables in `.env`.

### Step 5: Use Swagger
1. Open http://localhost:8000/swagger-ui.html
2. Call `POST /api/auth/login` and copy the `token` from the response.
3. Click **Authorize** (top right), paste the token, click **Authorize**.
4. Now every request is sent with your token.

---

## 3. Roles

| Role | Can do |
|---|---|
| **LIBRARIAN** | Manage books and copies, find members, issue and return books, renew any loan, see all loans and reservations, run the overdue job, view reports |
| **MEMBER** | Search books, reserve books, cancel own reservations, renew own loans, see own loans, fines, reservations and notifications |

Register always creates a **MEMBER**. Nobody can make themselves a librarian.

---

## 4. API list

All list APIs support `page`, `size` and `sort`, for example `?page=0&size=10&sort=title,asc`.

### Auth (public)
| Method | URL | What it does |
|---|---|---|
| POST | `/api/auth/register` | Create a member account |
| POST | `/api/auth/login` | Log in and get a JWT token |
| GET | `/api/auth/me` | Who am I (needs token) |

### Books
| Method | URL | Who | What it does |
|---|---|---|---|
| GET | `/api/books?title=&author=&category=&available=` | Anyone logged in | Search books. Title and author are partial matches |
| GET | `/api/books/{id}` | Anyone logged in | One book with total and available copies |
| POST | `/api/books` | Librarian | Add a book |
| PUT | `/api/books/{id}` | Librarian | Edit a book |
| DELETE | `/api/books/{id}` | Librarian | Delete a book (only if it has no loan or reservation history) |
| POST | `/api/books/{id}/copies` | Librarian | Add copies, for example `{ "count": 3 }` |
| GET | `/api/books/{id}/copies?status=AVAILABLE` | Anyone logged in | List copies of a book |

### Members
| Method | URL | Who | What it does |
|---|---|---|---|
| GET | `/api/members?query=` | Librarian | Find a member by name or email, with open and overdue loan counts |

### Loans
| Method | URL | Who | What it does |
|---|---|---|---|
| POST | `/api/loans/issue` | Librarian | Lend a copy to a member, for example `{ "copyId": 12, "memberId": 3 }` |
| POST | `/api/loans/{id}/return` | Librarian | Return a book, calculate and save the fine |
| POST | `/api/loans/{id}/renew` | Librarian, or member for own loan | Extend the due date by 7 days (once) |
| GET | `/api/loans?status=` | Librarian | All loans |
| GET | `/api/loans/{id}` | Librarian | One loan |

### Reservations
| Method | URL | Who | What it does |
|---|---|---|---|
| POST | `/api/reservations` | Member | Reserve a book that has no free copy, for example `{ "bookId": 2 }` |
| POST | `/api/reservations/{id}/cancel` | Member (own) or librarian | Cancel a reservation |
| GET | `/api/reservations?status=&bookId=` | Librarian | See who is waiting and which copies are held |

### My account (member)
| Method | URL | What it does |
|---|---|---|
| GET | `/api/me/loans?status=` | My loans with due dates and fines |
| GET | `/api/me/fines` | My total fines |
| GET | `/api/me/reservations?status=` | My reservations with queue position |
| GET | `/api/me/notifications?unreadOnly=` | My notifications |
| POST | `/api/me/notifications/{id}/read` | Mark a notification as read |

### Reports and jobs (librarian)
| Method | URL | What it does |
|---|---|---|
| GET | `/api/reports/top-borrowed?from=2026-10-01&to=2026-10-31` | Top 5 most borrowed books between two dates |
| GET | `/api/reports/overdue-loans` | Overdue loans with member name, book title, due date and days overdue |
| POST | `/api/admin/jobs/overdue` | Run the daily overdue job now |

---

## 5. Business rules

| Rule | Value | How it is done |
|---|---|---|
| Loan period | 14 days | Due date = issue date + 14 |
| Max books per member | 5 active loans | Active and overdue loans are counted when issuing |
| Renewal | Once, 7 days, only if no reservation and not overdue | A `renewed` flag on the loan, plus checks before renewing |
| Borrowing blocked | Member has any overdue book not yet returned | Checked by date when issuing, so it works even before the daily job runs |
| Fine | Rs 5 per late day, capped at the book price | `min(5 x days late, price)`, calculated and saved when the book is returned |
| Same copy issued twice | Must never happen | The copy row is locked while issuing (see section 7) |

Fine examples: returned 4 days late = Rs 20. Returned 100 days late on a Rs 300 book = Rs 300 (not Rs 500).

---

## 6. How the main flows work

### Issue a book
1. Librarian finds the member (`GET /api/members`) and the copy (`GET /api/books/{id}/copies?status=AVAILABLE`).
2. `POST /api/loans/issue` checks: the member has no overdue book, has fewer than 5 loans, and the copy is free (or is held for this member).
3. The copy becomes `ISSUED` and a loan is created, due in 14 days.

### Reserve a book
1. All copies are out, so the member calls `POST /api/reservations`. They join the queue as `WAITING`.
2. When a copy is returned (or a new copy is added), the first member in line gets it:
   the copy becomes `RESERVED`, the reservation becomes `READY`, and a notification is saved.
3. The librarian issues that held copy to that member. The reservation becomes `FULFILLED`.
   Nobody else can borrow a held copy.

### Return a book
1. `POST /api/loans/{id}/return` saves the return date and the fine.
2. If someone is waiting, the copy is held for them. Otherwise it becomes `AVAILABLE` again.

### Daily overdue job
Runs every day at 1 AM (`app.overdue-job.cron` in `application.properties`).
It marks every open loan past its due date as `OVERDUE` and saves **one notification per member** listing their overdue books.
Running it again does not send duplicate notifications.

### Status values
| Thing | Statuses |
|---|---|
| Copy | `AVAILABLE` → `ISSUED` → `AVAILABLE` or `RESERVED` |
| Loan | `ACTIVE` → `OVERDUE` → `RETURNED` |
| Reservation | `WAITING` → `READY` → `FULFILLED`, or `CANCELLED` |

---

## 7. Design decisions

**Layered structure.** Controller (HTTP) → Service (business rules) → Repository (database). Controllers never return entities, only DTOs, so passwords and internal fields are never exposed.

**Same copy issued twice.** When issuing, the copy row is read with `SELECT ... FOR UPDATE`. If two librarians click at the same time, the second request waits until the first finishes, then sees the copy is `ISSUED` and gets an error. A `@Version` column on the copy is a second safety net. The member row is also locked, so the 5-loan limit cannot be passed by two parallel requests. Returns, renewals and reservations use the same locking idea. Locks are always taken in the same order (member → loan → copy → reservation → book) to avoid deadlocks.

**First come, first served.** The queue is ordered by reservation time, then by id.

**Stateless login.** The JWT token carries the user's email and role and is signed with `JWT_SECRET`. The server stores no session. A member's identity always comes from the token, never from the URL, so a member can only see their own data.

**Fast list queries.** Lists load related data in one query (`@EntityGraph`), and copy counts or loan counts for a whole page come from one grouped query, so there is no "N+1 queries" problem.

**One clock.** "Today" comes from a single `Clock` bean, so all date rules agree.

**Clear errors.** Every error returns the same JSON shape:
```json
{ "timestamp": "...", "status": 409, "error": "Conflict", "message": "Member already has 5 active loans" }
```
| Status | Meaning |
|---|---|
| 400 | Bad input (validation errors are listed per field) |
| 401 | Not logged in or bad token |
| 403 | Logged in but not allowed |
| 404 | Not found |
| 409 | A business rule was broken |

---

## 8. Assumptions

The assignment does not say these things, so I chose:

1. **Who can renew:** the librarian (any loan) and the member (own loans only).
2. **Reserve only when no copy is free.** If a copy is available, the member just borrows it.
3. **One notification per member** for the overdue job, listing all their newly overdue books. Each loan is notified only once.
4. **"Top borrowed between two dates"** counts loans whose issue date is in the range (both dates included). A renewal is not a new borrow.
5. **A member cannot borrow a different copy** of a book while a copy of the same book is already held for them. The held copy must be issued instead.
6. **Deleting a book** is blocked if it has any loan or reservation history, so old fines and history stay correct.
7. **Category search** is an exact match (not case sensitive). Title and author are partial matches.
8. **Notifications** are saved as rows in a `notifications` table (and written to the log). No email is sent.

---

## 9. Project structure

```
src/main/java/com/library
├── config         Security, Swagger, scheduling, clock, default librarian
├── controller     REST endpoints
├── dto            Request and response objects
├── entity         Database tables (User, Book, BookCopy, Loan, Reservation, Notification)
├── enums          Role and status values
├── exception      Custom errors and the global error handler
├── repository     Database queries (Spring Data JPA)
├── scheduler      Daily overdue job
├── security       JWT, login user loading, current user
├── service        Business rules
└── specification  Dynamic book search filters
```

## 10. Database tables

| Table | Main columns |
|---|---|
| `users` | name, email (unique), password (BCrypt), role |
| `books` | title, isbn (unique), author, category, price |
| `book_copies` | book_id, status, version |
| `loans` | copy_id, member_id, issue_date, due_date, return_date, fine, status, renewed |
| `reservations` | book_id, member_id, status, created_at, held_copy_id |
| `notifications` | user_id, message, created_at, is_read |
