# i-Wish

A desktop app where you add friends, build your wish list, look at your friends' wish lists,
and chip in to buy them the gifts they want.

**Technologies:** Java 23+, JavaFX, JDBC, MariaDB/MySQL, Java I/O sockets, Maven, Git/GitHub.

---

## Main Features

1. **Register / Sign-in**
2. **Friends:** add / remove, accept / decline requests, view your friends list
3. **Wish list:** add, edit the note, remove items from a catalog
4. **View a friend's wish list**
5. **Contribute** a specific amount towards a friend's wish-list item; several friends can
   chip in for the same item
6. **Notifications:** contributors are told when an item is fully funded; the owner is told
   who bought it
7. **Friendly JavaFX GUI**, styled with `style.css`
8. **Server window:** Start / Stop, with a live log of connections and requests

---

## Setup (NetBeans)

### Prerequisites
- **JDK 23 or newer** (JavaFX 25 requires it)
- **MySQL or MariaDB** (XAMPP works) running locally
- **NetBeans IDE** (the project is a Maven project, so it opens directly)

### Database
1. Open **MySQL Workbench**: **File > Open SQL Script...**, then open `sql/schema.sql`
   and run it with the lightning-bolt (Execute) button. It creates the `iwish` database,
   the tables, and a 30-item sample catalog.
2. The server signs in as `root` with an empty password by default. If yours is different,
   either edit `iwish/server/Database.java`, or pass VM options when running the server:
   `-Diwish.db.user=root -Diwish.db.password=yourPassword`
3. New catalog items can be added any time with a plain SQL insert (requirement 12), e.g.:
   ```sql
   INSERT INTO items (name, category, price) VALUES ('Headphones', 'Electronics', 1500.00);
   ```

### Open and build
1. **File > Open Project** and pick this folder.
2. Maven downloads JavaFX and the MariaDB JDBC driver automatically (needs internet the first time).
3. Right-click the project > **Clean and Build**. This also copies every third-party jar into
   `libs/`, so they are part of the delivered project.

### Run
1. Right-click `iwish.server.ServerMain` (package `iwish.server`) > **Run File**.
   A window opens — click **Start**. It checks the database connection, then opens port 5555.
   Click **Stop** to shut it down; the log area shows every client connecting and every
   request it makes.
2. Right-click `iwish.client.ClientMain` (package `iwish.client`) > **Run File**.
   To test with several users at once, open more clients from a Command Prompt in the
   project folder (after Clean and Build, so `target/` and `libs/` exist):
   ```
   java -cp "target\classes;libs\*" iwish.client.ClientMain
   ```
   A client on another PC: add `-Diwish.host=<server-ip>` right after `java`.

---

## Project Structure

```text
iwish/
├── pom.xml
├── README.md
├── .gitignore
├── libs/                    
├── sql/
│   └── schema.sql           (database + catalog)
└── src/main/
    ├── java/iwish/
    │   ├── client/          JavaFX screens + server connection
    │   ├── server/          socket server, DAOs, server window
    │   └── common/          shared classes (Request, Response, Action, User, Item, ...)
    └── resources/iwish/client/
        └── style.css
```

---

## How client and server talk

`java.net` sockets with `ObjectOutputStream` / `ObjectInputStream` (`java.io`): the client
sends a `Request` object (an `Action` plus its arguments) and the server answers with a
`Response` object. The server remembers who signed in on each connection, so a client can
never act as somebody else, and it only accepts the project's own classes from clients.

```text
JavaFX Client → Request → ClientHandler → DAO (JDBC) → Database
JavaFX Client ← Response ← ClientHandler ← DAO (JDBC) ← Database
```

---

## Rules the app enforces

- A gift is "bought" once contributions add up to its price; nobody can pay more than what
  is missing, even if two friends pay at the same moment (the wish-list row is locked inside
  a transaction, so contributions can never overshoot the price).
- The owner does not see partial progress on their own list (it stays a surprise) — only
  that the gift was bought.
- A wish that friends have already contributed to cannot be removed.
- Notifications are stored in the database, so a user who was offline sees them at their
  next sign-in.
- Passwords are stored as salted PBKDF2 hashes, never as plain text.
- Server-side validation errors (bad email, short password, note too long, invalid amount,
  etc.) are reported with a clear message via `BusinessException`, instead of a raw database
  or network error.

---

## Requirements → where they live

| # | Requirement | Main code |
|---|---|---|
| 1 | Register / Sign-in | `LoginView`, `UserDAO`, `PasswordUtil` |
| 2, 3, 5 | Friends: add / remove, accept / decline, list | `MainView`, `FriendDAO` |
| 4 | Create / Update / Delete my wish list | `WishlistView`, `CatalogWindow`, `WishlistDAO` |
| 6 | View a friend's wish list | `FriendWishlistWindow`, `WishlistDAO` |
| 7 | Contribute an amount towards a gift | `FriendWishlistWindow`, `ContributionDAO` |
| 8, 9 | Notifications to the buyers and to the receiver | `ContributionDAO`, `NotificationDAO`, `MainView` |
| 10 | Friendly GUI | JavaFX + `style.css` |
| 11 | Server Start / Stop | `ServerApp`, `IWishServer` |
| 12 | Database: connection, queries, catalog items | `Database`, `*DAO`, `sql/schema.sql` |
| 13, 14 | Handle client connections and requests | `IWishServer`, `ClientHandler` |

---

## Team Responsibilities

The project is divided into five feature areas. Each member is responsible for the logic,
GUI part, and database work of their assigned feature, and commits their own part from
their own GitHub account.

### Member 1 (Nourhan Ahmed) — Authentication & User Management
**Requirements:** 1 (Register/Sign-in), part of 10 (GUI), user-related part of 12 (Database)
**Main files:** `LoginView`, `UserDAO`, `User`, `PasswordUtil`, `ServerConnection`, `IWishApp`, `ClientMain`

### Member 2 (Ahmed Ossama) — Friends Management
**Requirements:** 2 (Add/Remove Friend), 3 (Accept/Decline Request), 5 (View Friends List), part of 10, friends-related part of 12
**Main files:** `FriendDAO`, `FriendRequest`, friend-related parts of `MainView`, friend-related request handling in `ClientHandler`

### Member 3 (Aya Dekhail) — Wishlist & Catalog
**Requirements:** 4 (Create/Update/Delete Wish List), 6 (View Friends' Wish Lists), part of 10, wishlist/catalog part of 12
**Main files:** `WishlistView`, `WishlistDAO`, `CatalogWindow`, `ItemDAO`, `Item`, `WishItem`, `FriendWishlistWindow`

### Member 4 (Habiba Usamah) — Contributions, Notifications & Styling
**Requirements:** 7 (Contribute), 8 (Buyer notification), 9 (Receiver notification), part of 10, contribution/notification part of 12
**Main files:** `ContributionDAO`, `NotificationDAO`, `Notification`, contribution/notification parts of `MainView` and `FriendWishlistWindow`

### Member 5 (Wafaa Nabeh) — Server & Networking
**Requirements:** 11 (Start/Stop Server), 13 (Handle Client Connections), 14 (Handle Client Requests), server part of 12, integration part of 10
**Main files:** `IWishServer`, `ServerApp`, `ServerMain`, `ClientHandler`, `Database`, `Request`, `Response`, `Action`, `BusinessException`


---

## GitHub Collaboration

The project is developed collaboratively on GitHub. Each member commits from their own
account, on the feature listed under their name above, with clear commit messages
describing what changed.

Repository: **https://github.com/aossama2006-rgb/iwish-Java-App**

---

## Demo

A short video demonstrating registration, adding a friend, building a wish list, viewing a
friend's wish list, contributing to a gift, and receiving the resulting notifications:

**[link to be added]**

---

## Delivery Package

- NetBeans (Maven) project: this repository
- Database scheme: `sql/schema.sql`
- Third-party libraries: `libs/` (created by Clean and Build): JavaFX 25.0.1 and MariaDB JDBC 3.4.1
- Working demo: see the **Demo** section above
