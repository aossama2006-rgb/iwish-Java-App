# i-Wish

A desktop app where you add friends, build your wish list, look at your friends' wish lists,
and chip in to buy them the gifts they want. Java 23+, JavaFX, JDBC (MariaDB/MySQL), sockets.

## Setup (NetBeans)
1. Start MySQL/MariaDB (XAMPP is fine) and prepare the database, in MySQL Workbench
   (**File > Open SQL Script**, then the lightning-bolt button). Pick ONE:
   - `sql/schema.sql`: empty database with the tables and a 30-item sample catalog.
   - `sql/iwish_backup.sql`: the same, plus demo data (users, friends, wish lists, contributions,
     notifications). Running it replaces any existing `iwish` tables.
2. NetBeans: **File > Open Project** and pick this folder (it is a Maven project).
   Maven downloads JavaFX and the JDBC driver.
3. The server signs in to the database as `root` with an empty password. If yours is different, edit
   `iwish/server/Database.java`, or pass the VM options `-Diwish.db.user=... -Diwish.db.password=...`.
4. Right-click the project > **Clean and Build**. This also copies all third-party jars into `libs/`.

## Run
1. Right-click `iwish.server.ServerMain` > **Run File**, then press **Start**
   (it checks the database first, then opens port 5555). **Stop** shuts the server down.
2. Right-click `iwish.client.ClientMain` > **Run File**.
   To test with several users at once, open more clients from a Command Prompt in the project folder
   (after Clean and Build, so `target/` and `libs/` exist):
   `java -cp "target\classes;libs\*" iwish.client.ClientMain`
   A client on another PC: add `-Diwish.host=<server-ip>` after `java`.

## Demo accounts (only if you loaded `sql/iwish_backup.sql`)
Password for all of them: `123456`

| Email | State |
|---|---|
| ahmed@test.com | 2 friends (Sara, Omar), a pending request from Dina, 3 wishes: the Smart Watch is partly funded, the Yoga Mat was bought (unread notification) |
| sara@test.com | friend of Ahmed, 2 wishes, a notification that she helped buy the Yoga Mat |
| omar@test.com | friend of Ahmed, 1 wish, same notification |
| dina@test.com | sent a friend request to Ahmed (not accepted yet) |

## Requirements -> where they live
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

## How client and server talk
`java.net` sockets with `ObjectOutputStream` / `ObjectInputStream` (`java.io`): the client sends a
`Request` object (an `Action` plus its arguments) and the server answers with a `Response` object.
The server remembers who signed in on each connection, so a client cannot act as someone else, and it
only accepts the project's own classes from clients.

## Rules the app enforces
- A gift is "bought" when the contributions add up to its price; nobody can pay more than what is missing,
  even if two friends pay at the same moment (the wish-list row is locked inside a transaction).
- The owner does not see partial progress (it stays a surprise), only that the gift was bought.
- A wish that friends have contributed to cannot be removed.
- Notifications are stored in the database, so users who were offline see them at their next sign-in.
- Passwords are stored as salted PBKDF2 hashes, never as plain text.

## Team roles
Fill in the names before submitting. Suggested split by module:

| Member | Role | Files |
|--------|------|-------|
|        | Database and DAOs | `sql/*.sql`, `Database`, `UserDAO`, `FriendDAO`, `ItemDAO`, `WishlistDAO`, `PasswordUtil` |
|        | Server networking and server window | `IWishServer`, `ClientHandler`, `ServerApp`, `ServerMain` |
|        | Client networking and sign-in screen | `ServerConnection`, `Request`, `Response`, `IWishApp`, `LoginView` |
|        | Friends and wish-list screens | `MainView`, `WishlistView`, `CatalogWindow` |
|        | Contributions, notifications and styling | `FriendWishlistWindow`, `ContributionDAO`, `NotificationDAO`, `style.css` |

## Delivery package
- NetBeans (Maven) project: this folder
- Database scheme and backup: `sql/schema.sql`, `sql/iwish_backup.sql`
- Third-party libraries: `libs/` (created by Clean and Build): JavaFX 25.0.1 and MariaDB JDBC 3.4.1
- Working demo: see the demo video / steps agreed with the instructor
