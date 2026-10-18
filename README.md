# AMIRA

Online perfume store with real inventory tracking, built from the Week 1 business proposal. It follows the
architecture of the professor's `gmartapp` sample.

```
amiraserver/           Spring Boot 2.7.5, Java 17, MySQL 8 (Maven multi-module, like gmartserver/)
  amiradata/            JPA entities (*Data) and Spring Data repositories
  amirabiz/             model/, service/, serviceimpl/, transform/, exception/
  sbamirams/            controllers, auth, config, application.yml, seeder, tests
amira/                  Angular 16 (NgModule, like gmart/)
docker-compose.yml      MySQL + backend + Angular (nginx), started with one command
```

## Live demo

The Angular app also has a **demo build** that needs no backend, so it can be hosted as a static site (it is
deployed on Vercel from the `amira/` folder). Instead of calling the Spring Boot API it uses an in-browser copy of
the API (`amira/src/app/demo/demo-backend.ts`, switched on by `environment.demo`) that follows the same stock,
payment and order rules and keeps its data in the visitor's browser. Sign in as the owner with `owner@amira.ph` /
`owner123`, or as a customer with `customer@amira.ph` / `customer123`. The banner at the top has a "Reset demo"
button. Build it with `npm run build:demo` from `amira/`. The normal `npm start` and Docker builds are unchanged and
still talk to the real backend.

## How the proposal maps to the code

| Proposal | Code |
|---|---|
| Model classes: Product, ProductVariant, Customer, Order, OrderItem, Payment | `amirabiz/model`, stored by `amiradata/entity/*Data` |
| ProductService, InventoryService, OrderService, PaymentService, CustomerService, ReportingService | `amirabiz/service` (interfaces) and `serviceimpl` |
| Stock reserved when ordered, deducted when paid, restored on cancel | `OrderServiceImpl`, `PaymentServiceImpl`, `InventoryServiceImpl` |
| Batch, expiry, low-stock and slow-moving reports | `/api/variant/expiring`, `/api/variant/low-stock`, `/api/report/*` and the Owner area |

Changes from the proposal and from the professor's sample are listed at the bottom.

## Run it

### Quickest way: Docker

Install and start Docker Desktop, then from this folder (the one holding `docker-compose.yml`):

```bash
docker compose up --build
```

The first build downloads packages and takes a few minutes. When it finishes, open **http://localhost:4200**.
The shop comes with 19 starter perfumes. Owner login: `owner@amira.ph` / `owner123`. Customers register on the site.
Stop with `docker compose down`. Details are under "Run with Docker" below.

> **"We could not load the perfumes"** means the Angular page is running but the backend is not. `ng serve` /
> `npm start` start only the front end, so the backend (port 8080) and MySQL must be running too. Use Docker above,
> or follow the steps below in order: MySQL, then backend, then front end.

### Without Docker

You need JDK 17, Maven, MySQL 8 and Node 18 or newer.

1. Start MySQL. The database `amiradb` is created automatically.
2. Give the backend your MySQL login. Do not commit it.

   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your-mysql-password
   ```

   (PowerShell: `$env:DB_PASSWORD = "your-mysql-password"`)
3. Backend, from `amiraserver/`:

   ```bash
   mvn clean install
   java -jar sbamirams/target/sbamirams-1.0-SNAPSHOT.jar
   ```

   It listens on `http://localhost:8080`. On first start it creates the five order statuses and the owner account.
   If the product table is empty it also adds 19 starter perfumes, so the shop is not blank. It never touches an
   existing catalog. To start with no products, set `AMIRA_SEED_DEMO_PRODUCTS=false` before starting. Add or change
   perfumes, sizes, batches and stock in the Owner area.
4. Frontend, from `amira/`:

   ```bash
   npm install
   npm start
   ```

   It opens on `http://localhost:4200`. If that port is taken, use `npx ng serve --port 4300` and start the
   backend with `--amira.cors.origins=http://localhost:4300`.

### Owner account

The first start creates one owner login: `owner@amira.ph` with password `owner123`, or whatever
`OWNER_PASSWORD` is set to. Set `OWNER_PASSWORD` before the first start for anything beyond local testing.
Customers register themselves on the site.

### Database name

The database is now `amiradb`, so the first start creates a new, empty one. A database called `silverscentdb`
from the earlier name is left alone; drop it if you no longer need it:
`mysql -u root -p -e "DROP DATABASE silverscentdb"`. To reset AMIRA itself, drop `amiradb` and restart.

## Run with Docker

Needs Docker Desktop (or Docker Engine with the compose plugin). From the project root:

```bash
docker compose up --build
```

| Container | What it runs | Address |
|---|---|---|
| `amira-mysql` | MySQL 8, data kept in the `amira-db` volume | internal only |
| `amira-backend` | Spring Boot jar, built inside the image | http://localhost:8080 |
| `amira-frontend` | Angular production build served by nginx | http://localhost:4200 |

The database starts with the order statuses, the owner login and 19 starter perfumes (remove
`AMIRA_SEED_DEMO_PRODUCTS` from `docker-compose.yml` to start with an empty shop). Sign in as the owner
(`owner@amira.ph`, password `owner123` unless you set `OWNER_PASSWORD`) to manage perfumes, stock and orders.
For a delivered order, the owner's Orders page has a **Refund payment** button.
To change the database or owner password, copy `.env.example` to `.env` and edit it.

Stop with `docker compose down`. Add `-v` to also delete the database volume.

The browser talks to the backend at `http://localhost:8080`, so the three containers are meant to run on the
machine you open the site from. To host it elsewhere, change `apiBaseUrl` in
`amira/src/environments/environment.ts` and the two `AMIRA_*` addresses in `docker-compose.yml`.

## Tests

`PurchaseFlowTest` checks the app starts with no products, orders or customers, then runs the proposal's
section 6 scenario against an in-memory database, creating its own catalog: order reserves stock,
payment deducts it, cancel restores it, no overselling, expired stock cannot be bought, role checks, and all reports.

```bash
cd amiraserver
mvn -pl sbamirams -am test
```

## Where this differs from the professor's sample (and why)

- Controllers return objects and one `ApiExceptionHandler` maps errors to 400/401/403/404 with `{"message": ...}`,
  instead of a try/catch returning 500 in every method.
- Entities still use plain `int` foreign keys (no JPA relationships). Parents assemble their children in the service
  (`Product.variants`, `Order.items`).
- Money is `BigDecimal`, dates are `LocalDate`/`LocalDateTime`.
- Services are `@Transactional`; stock rows are locked while they change so two buyers cannot take the last bottle.
- Added `reservedQuantity` to `ProductVariant`, which the proposal's `reserveStock()` needs.
- Added customer login (BCrypt passwords, bearer tokens, CUSTOMER and OWNER roles). The sample has no login.
- Fixed in the sample's pattern: valid parent POM, no hardcoded database password, CORS enabled, update returns the
  saved record, deleting a missing id returns 404.
- Frontend adds an HTTP interceptor and route guard, and keeps the cart in the browser until checkout.

## Forgot password

"Forgot password?" on the sign-in page asks for the email, then sends a one-time link to `/reset-password`.
The link expires after 30 minutes, works once, and ends the customer's open sessions when used. The page gives the
same answer whether or not the email is registered. Only a hash of the token is stored.

To send real emails, set these before starting the backend (any SMTP account works, for example Gmail with an
app password):

```bash
export SPRING_MAIL_HOST=smtp.gmail.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=you@gmail.com
export SPRING_MAIL_PASSWORD=your-app-password
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

Also set `amira.frontend-url` in `application.yml` to the address the site is served from (default
`http://localhost:4200`), since that is what the link points to. With no mail settings the backend writes the link
to its console log instead, which is only suitable for local development.

## Known limits

- Reset requests are not rate limited, so an attacker could make the site send many emails to one address.

- Login sessions are kept in server memory, so everyone signs in again after a restart (`AuthTokenStore`).
- GCash and card payments use a simulated gateway that approves immediately. Bank transfer and cash on delivery wait
  for the owner to confirm. A real gateway would replace `PaymentServiceImpl.processPayment`.
- Product photos come from each product's `imageUrl` (the starter perfumes use `amira/src/assets/products`). A product
  without a photo, or whose photo fails to load, shows a drawn bottle instead.
- Light theme only.
