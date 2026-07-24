# Money Tracker

Android app (Java, MVVM, Room) for tracking income and expenses across multiple
accounts with different currencies, matching the reference design you shared.

## Opening the project

1. Open **Android Studio** (Koala/2024.1 or newer recommended — the project
   targets Java 17 and Gradle 8.7).
2. Choose **File ▸ Open** and select this folder
   (`tracker android app`).
3. Let Gradle sync. On first sync Android Studio will:
   - fill in `local.properties` with your SDK path automatically, and
   - download the Gradle 8.7 distribution + generate `gradle/wrapper/gradle-wrapper.jar`
     if it's missing (this repo only ships the wrapper *scripts*, not the
     binary jar — Studio regenerates it on sync, or you can run `gradle wrapper`
     once yourself if you have a system Gradle install).
4. Run the `app` configuration on an emulator or device (min SDK 24).

No backend/API keys are needed — everything is stored locally in a Room
(SQLite) database on the device.

## What's included

- **Home** — balance card for the active account, quick actions (Send / Add
  funds / Request / More), recent transactions list. Tap the account name to
  switch accounts if you have more than one; tap the eye icon to hide/show
  the balance.
- **Add transaction** — opened from the quick actions; expense/income toggle,
  amount, account picker, category picker, date picker, description.
- **Wallet** — list of accounts with live balances; tap `+` to add an
  account, tap an account to edit or delete it.
- **Analytics** — income vs. expense totals and a pie chart of expenses by
  category, switchable between week/month/year.
- **Settings** — switch the app language between English and Czech
  independently of the phone's system language, and hide balances on Home.

## Architecture

- **MVVM**: `ui/<feature>/XxxFragment` + `XxxViewModel` (`AndroidViewModel`,
  so each ViewModel reaches its repositories via `MoneyTrackerApp`).
- **Room**: entities/DAOs/database in `data/`; `AppDatabase` seeds a default
  "Cash" account and a starter set of categories the first time the app runs.
- **Repositories** (`data/repository`) wrap the DAOs and move writes onto a
  background executor (`util/AppExecutors`).
- Seeded accounts/categories are stored under a stable key (e.g.
  `category_food`) instead of literal text so their label can be
  re-localized after a language switch — see `util/DisplayNameResolver`.
  Anything you create yourself (new account, new... well, categories aren't
  user-creatable yet) is stored as literal text and shown as-is.

## Known simplifications (good next steps)

- Multi-currency accounts are each shown in their own currency; there's no
  currency-conversion layer, so Analytics totals sum raw amounts across all
  accounts regardless of currency.
- Categories are seeded but not yet user-editable (no "add category" UI).
- "Request" quick action currently opens the same income entry form as
  "Add funds" — there's no real request/invite flow.
- "See all" on Home and "More" don't navigate anywhere yet.
