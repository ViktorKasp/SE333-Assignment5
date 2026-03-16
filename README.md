# Assignment 5 — SE333

![Build Status](https://github.com/ViktorKasp/SE333-Assignment5/actions/workflows/SE333_CI.yml/badge.svg)

## Project Overview

This repository contains the complete solution for Assignment 5 for SE333 Software Testing at DePaul University. The assignment covers two main parts:

- **Part 1**: Integration and unit testing of an Amazon shopping-cart simulation, with Checkstyle static analysis and JaCoCo code coverage, all automated via GitHub Actions CI.
- **Part 2**: UI end-to-end testing of the DePaul University Bookstore purchase flow using Playwright (manual Java tests + AI-assisted MCP-generated tests).

---

## Repository Structure
```
src/
  main/java/org/example/Amazon/         ← production source code
  test/java/org/example/
    AmazonIntegrationTest.java           ← Part 1: integration tests (real DB)
    AmazonUnitTest.java                  ← Part 1: unit tests (Mockito mocks)
  test/java/playwrightTraditional/
    BookstoreTest.java                   ← Part 2: manual Playwright tests
  test/java/playwrightLLM/
    BookstoreTestLLM.java                ← Part 2: AI-generated Playwright tests
.github/workflows/SE333_CI.yml          ← CI workflow
videos/                                  ← Playwright video recording (local)
```

---

## Part 1 — Integration & Unit Tests

### Integration Tests (AmazonIntegrationTest.java)
Tests how multiple components work together using a real in-memory HSQLDB database. Each test resets the database using `@BeforeEach` to ensure isolation. Tests cover:
- Empty cart returning zero total
- Single and multiple item price calculations
- Electronics surcharge applied correctly
- Delivery price tiers (0, 1-3, 4-10, 11+ items)
- Database persistence and reset behavior

### Unit Tests (AmazonUnitTest.java)
Tests each class in isolation using Mockito mocks for all external dependencies. Tests cover:
- `Amazon.calculate()` delegating to price rules correctly
- `Amazon.addToCart()` delegating to the cart
- `RegularCost`, `DeliveryPrice`, and `ExtraCostForElectronics` boundary conditions
- Item getter methods returning constructor values

Both test files include `@DisplayName("specification-based")` and `@DisplayName("structural-based")` tests as required.

---

## Part 2 — GitHub Actions Workflow

The CI workflow (`.github/workflows/SE333_CI.yml`) triggers on every push to `main` and:

1. Sets up Java 23 on Ubuntu
2. Runs **Checkstyle** static analysis at the validate phase — violations are reported but do not fail the build
3. Uploads `checkstyle-result.xml` as a downloadable artifact
4. Installs Playwright browsers (`chromium`) on the CI runner
5. Runs all **JUnit 5 tests** via Maven Surefire including Playwright UI tests in headless mode
6. Generates a **JaCoCo** XML coverage report
7. Uploads `jacoco.xml` and Playwright video recordings as artifacts

✅ **All GitHub Actions steps passed successfully.**

---

## Part 2 — Playwright UI Tests

### Traditional Tests (BookstoreTestLLM.java)
Manually written Java + Playwright tests that automate the full DePaul bookstore purchase flow across 7 ordered test cases:

1. Search for earbuds, apply Brand/Color/Price filters, navigate to JBL Quantum product, assert product details, add to cart
2. Verify shopping cart contents, enter invalid promo code, assert rejection, proceed to checkout
3. Assert Create Account page, proceed as guest
4. Fill in contact information (name, email, phone)
5. Confirm pickup information at DePaul Loop Campus
6. Reach payment page and go back to cart
7. Delete item from cart and assert cart is empty

A video recording of the full test run is saved to the `videos/` folder.

**Difficulties encountered**: The bookstore website presented several challenges during test development. The search box selector was initially matching a hidden cookie consent input instead of the main search bar, requiring the use of `input[placeholder*='product title']` as a more specific selector. Filter buttons (Brand, Color, Price) were being matched by a "Filter by" dropdown at the top of the page instead of the left sidebar filters — this required using `getByRole(AriaRole.BUTTON)` with exact names from the Playwright inspector. The cart page required careful scrolling management since the promo code section and checkout button were not visible without scrolling, and the FAST In-Store Pickup option was already pre-selected so clicking it was accidentally deselecting it. Contact information fields required clicking before filling due to the page's JavaScript validation. The JaCoCo coverage agent conflicted with Playwright's internal class loading on Java 23, requiring exclusions in the pom.xml configuration.

---

## Part 2 — AI-Assisted Tests (BookstoreTestLLM.java)

Generated using the **Playwright MCP** agent in VS Code by prompting it with a natural language description of the bookstore purchase flow. The agent navigated the live bookstore website and generated Java JUnit 5 test code automatically.

---

## Reflection: Manual vs AI-Assisted UI Testing

### Manual UI Testing (Java + Playwright)

Writing the Playwright tests manually in Java required significant effort but produced precise, reliable tests. Each action had to be carefully crafted using the Playwright Inspector to identify exact selectors, and many iterations were needed to handle the bookstore's dynamic page loading, scrolling behavior, and JavaScript-driven UI. The process of debugging failing tests was straightforward since the exact line of code responsible was immediately visible. However, the time investment was substantial — getting all 7 test cases working correctly required extensive trial and error, particularly around locator specificity, scroll management, and timing issues with `Thread.sleep()` delays.

### AI-Assisted UI Testing (Playwright MCP Agent)

Using the Playwright MCP agent in VS Code dramatically reduced the time needed to produce a working test skeleton. By describing the workflow in natural language, the agent navigated the live bookstore and generated syntactically correct Java/JUnit 5 code within seconds. This eliminated the most tedious parts — boilerplate setup and initial selector discovery. However, the generated code was not perfect and required human review. The agent made a minor API error using `Page.GetByRoleOptions()` on a `Locator` object instead of `Locator.GetByRoleOptions()`, which caused a compilation error. Additionally, some selectors used by the agent may be fragile if the page structure changes, and the agent could not anticipate dynamic behavior like the cart count update delay or the need to scroll before interacting with elements.

### Comparison

| Aspect | Manual (Java + Playwright) | AI-Assisted (MCP Agent) |
|---|---|---|
| Ease of writing | Time-consuming, many iterations | Fast initial draft |
| Accuracy | High once selectors verified | Good but needs human review |
| Reliability | High | Medium — some fragile selectors |
| Maintenance | Update selectors on UI change | AI can regenerate quickly |
| Limitations | Steep learning curve, lots of debugging | Minor API mistakes, no dynamic behavior awareness |

The best workflow combines both approaches — use the MCP agent to generate a first-pass test quickly, then review and harden the selectors manually. This provides the speed of AI generation without sacrificing the reliability that comes from careful human review.

---
