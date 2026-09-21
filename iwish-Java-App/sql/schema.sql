-- i-Wish database schema (works on MariaDB 10.4+ / XAMPP and MySQL 8)
CREATE DATABASE IF NOT EXISTS iwish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE iwish;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    salt          VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Only PENDING requests live here. Accepting moves the pair to `friendships`,
-- declining just deletes the row.
CREATE TABLE IF NOT EXISTS friend_requests (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT       NOT NULL,
    receiver_id INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_request (sender_id, receiver_id),
    FOREIGN KEY (sender_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- One row per friendship, stored once with user_a < user_b (the server guarantees the order).
CREATE TABLE IF NOT EXISTS friendships (
    user_a INT       NOT NULL,
    user_b INT       NOT NULL,
    since  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_a, user_b),
    FOREIGN KEY (user_a) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_b) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Wish lists
-- ---------------------------------------------------------------------

-- The catalog users pick their gifts from. Admin adds items with plain INSERTs, e.g.
--   INSERT INTO items (name, category, price) VALUES ('Headphones', 'Electronics', 1500.00);
CREATE TABLE IF NOT EXISTS items (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100)  NOT NULL UNIQUE,
    category VARCHAR(50)   NOT NULL,
    price    DECIMAL(10,2) NOT NULL
);

-- One row per (user, catalog item) on somebody's wish list.
CREATE TABLE IF NOT EXISTS wishlist_items (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id  INT          NOT NULL,
    item_id  INT          NOT NULL,
    note     VARCHAR(200) NULL,
    added_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_wish (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Sample catalog (prices in EGP). INSERT IGNORE + UNIQUE(name) makes re-running this file safe.
INSERT IGNORE INTO items (name, category, price) VALUES
    ('Wireless Earbuds',           'Electronics',  1200.00),
    ('Bluetooth Speaker',          'Electronics',   950.00),
    ('Smart Watch',                'Electronics',  2500.00),
    ('Power Bank 20000mAh',        'Electronics',   650.00),
    ('Mechanical Keyboard',        'Electronics',  1400.00),
    ('Gaming Mouse',               'Electronics',   700.00),
    ('E-Reader',                   'Electronics',  4200.00),
    ('HD Webcam',                  'Electronics',   800.00),
    ('Atomic Habits (book)',       'Books',          350.00),
    ('The Alchemist (book)',       'Books',          200.00),
    ('Clean Code (book)',          'Books',          900.00),
    ('Notebook and Pen Set',       'Books',          250.00),
    ('Leather Wallet',             'Fashion',        600.00),
    ('Sunglasses',                 'Fashion',        750.00),
    ('Backpack',                   'Fashion',        900.00),
    ('Wool Scarf',                 'Fashion',        400.00),
    ('Casual Sneakers',            'Fashion',       1800.00),
    ('Scented Candle Set',         'Home',           300.00),
    ('Coffee Maker',               'Home',          1600.00),
    ('Desk Lamp',                  'Home',           450.00),
    ('Throw Blanket',              'Home',           500.00),
    ('Mug Set (4 pieces)',         'Home',           280.00),
    ('Yoga Mat',                   'Sports',         400.00),
    ('Water Bottle 1L',            'Sports',         250.00),
    ('Football',                   'Sports',         500.00),
    ('Swim Goggles',               'Sports',         350.00),
    ('Resistance Bands Set',       'Sports',         300.00),
    ('Board Game',                 'Games',          750.00),
    ('1000-Piece Jigsaw Puzzle',   'Games',          400.00),
    ('Building Blocks Set',        'Games',         1100.00);

-- ---------------------------------------------------------------------
-- Contributions (friends chip in for a wish-list item) and notifications
-- ---------------------------------------------------------------------

-- Every payment towards a wish-list entry. The item is "bought" once the sum reaches its price.
CREATE TABLE IF NOT EXISTS contributions (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    wishlist_item_id INT           NOT NULL,
    contributor_id   INT           NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wishlist_item_id) REFERENCES wishlist_items(id) ON DELETE CASCADE,
    FOREIGN KEY (contributor_id)   REFERENCES users(id)          ON DELETE CASCADE
);

-- Messages waiting for a user. Stored in the database so offline users get them at next sign-in.
CREATE TABLE IF NOT EXISTS notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT          NOT NULL,
    message    VARCHAR(400) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
