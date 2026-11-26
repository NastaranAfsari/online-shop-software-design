# Online Shop — Software Design Principles (Phase 2)

A console-based online shop written in Java. Customers can browse, search and filter products, manage a cart, place orders, pay through a simulated payment gateway and review the products they bought. Admins manage categories, products, inventory and orders.

This repository contains **Phase 2** of the course project: UML models of the system, a refactoring of the Phase 1 code so that it follows the SOLID principles, and a Git/GitHub workflow.

- **Course:** Software Design Principles (اصول طراحی نرم‌افزار), Amirkabir University of Technology — Faculty of Mathematics and Computer Science
- **Semester:** first semester of 1405–1406 (autumn 2026)

## Team

| Name | Student ID | GitHub |
|---|---|---|
| Nastaran Afsari (نسترن افسری) | 40212004 | [@NastaranAfsari](https://github.com/NastaranAfsari) |

This is a single-member project. All work was done on the personal branch `member-1` through feature branches and pull requests. As agreed with the head TA, pull requests were merged without a review by a second member.

## Project structure

The code follows a layered architecture. Each layer is one Java package:

```
OnlineShop/
├── src/main/java/shop/
│   ├── Main.java          # composition root: creates and wires all objects, adds sample data
│   ├── cli/               # presentation layer: console menus, input/output
│   ├── service/           # business logic: services, session, payment rules, payment gateway port
│   ├── repository/        # data access: repository interfaces + in-memory implementations
│   └── model/             # domain model: entities, value objects, enums, validation
├── docs/
│   ├── diagrams/          # Phase 1 domain model and design diagrams
│   └── phase2/
│       ├── diagrams/      # Phase 2 UML diagrams (PNG) and their sources (src/)
│       └── OnlineShop-Phase2-Design-Document.pdf
├── run.sh                 # compile and run from a terminal (Linux, macOS, Git Bash)
└── README.md
```

Data is kept in memory only (no database), so every run starts with the sample data created in `Main`.

## How to run

Requirements: **JDK 17 or newer**.

**IntelliJ IDEA**
1. Open the `OnlineShop` folder as a project.
2. Open `src/main/java/shop/Main.java` and click the green ▶ button next to `public class Main`.
3. Use the menu in the Run window.

**Terminal (Linux, macOS or Git Bash on Windows)**
```bash
bash run.sh
```

**Windows PowerShell**
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
java -cp out shop.Main
```

**Demo accounts**

| Role | Email | Password |
|---|---|---|
| Admin | admin@shop.com | admin123 |
| Customer | ali@shop.com | 123456 |

The payment gateway is simulated: during payment the program asks whether the gateway should approve the payment, so both the success and the failure path can be tried.

## UML diagrams

All diagrams are in [`docs/phase2/diagrams`](docs/phase2/diagrams). Source files are in [`docs/phase2/diagrams/src`](docs/phase2/diagrams/src) (Graphviz `.dot`, Mermaid `.mmd`) and match the final, refactored code.

| Diagram | File |
|---|---|
| Class diagram — domain model | `class-diagram-model.png` |
| Class diagram — services (accounts and catalog) | `class-diagram-services-accounts.png` |
| Class diagram — services (orders and payment) | `class-diagram-services-orders.png` |
| Class diagram — repositories | `class-diagram-repositories.png` |
| Class diagram — presentation layer | `class-diagram-cli.png` |
| Object diagram — state after a paid order | `object-diagram.png` |
| Activity diagram — checkout and payment | `activity-diagram.png` |
| Component diagram | `component-diagram.png` |
| Deployment diagram | `deployment-diagram.png` |
| Sequence diagram — paying an order (bonus) | `sequence-diagram.png` |

To regenerate an image from its source: `dot -Tpng file.dot -o file.png` (Graphviz) or paste the `.mmd` file into [mermaid.live](https://mermaid.live).

## SOLID refactoring (summary)

| Principle | Change |
|---|---|
| SRP | `AuthService` split into `AuthService`, `UserSession`, `ProfileService` and `PasswordRecoveryService`; order-related stock changes moved into `InventoryService` |
| OCP | Payment verification is a list of `PaymentVerificationRule` classes; a new check is a new class |
| LSP | All implementations (`InMemory*Repository`, `SimulatedPaymentGateway`, `Pbkdf2PasswordHasher`, rules) can replace their interfaces without changing behaviour |
| ISP | Services depend on small interfaces (`ProductLookup`, `PurchaseHistory`, `OrderStockHandler`); menus depend on `UserSession` instead of the whole `AuthService` |
| DIP | `OrderMenu` depends on `GatewaySimulationControl`, services on the `PasswordHasher` and `PaymentGateway` abstractions; all objects are wired in `Main` |

Details, with the reason for each change, are in the design document (PDF).

## Git workflow

- `main` — stable, final version
- `develop` — integration branch
- `member-1` — personal branch, created from `develop`
- `feature/*` — one branch per task, created from `member-1` and merged back into it through a pull request

Feature branches are never merged directly into `develop` or `main`. Commit messages follow `<type>: <short summary>` with the types `init`, `feat`, `fix`, `style`, `refactor`, `docs` and `chore`.
