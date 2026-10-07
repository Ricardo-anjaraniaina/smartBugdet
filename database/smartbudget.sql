-- =============================================================================
-- SmartBudget — Script de création de la base de données
-- =============================================================================
-- Usage :
--   mysql -u root -p < database/smartbudget.sql
-- ou via MySQL Workbench / phpMyAdmin : importer ce fichier.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS smartbudget
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartbudget;

-- =============================================================================
-- TABLE : users
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,          -- SHA-256 hash
    initial_balance DECIMAL(15,2) DEFAULT 0.00,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- TABLE : categories
-- =============================================================================
CREATE TABLE IF NOT EXISTS categories (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- TABLE : transactions
-- =============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    category_id      INT NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    type             ENUM('INCOME', 'EXPENSE') NOT NULL,
    description      VARCHAR(255),
    transaction_date DATE NOT NULL,
    payment_method   VARCHAR(50),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tx_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- TABLE : budgets
-- =============================================================================
CREATE TABLE IF NOT EXISTS budgets (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    category_id  INT NOT NULL,
    amount_limit DECIMAL(15,2) NOT NULL,
    month        INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year         INT NOT NULL,

    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- TABLE : savings_goals
-- =============================================================================
CREATE TABLE IF NOT EXISTS savings_goals (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    name           VARCHAR(150) NOT NULL,
    target_amount  DECIMAL(15,2) NOT NULL,
    current_amount DECIMAL(15,2) DEFAULT 0.00,
    deadline       DATE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_savings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- DONNÉES INITIALES : catégories
-- =============================================================================
INSERT IGNORE INTO categories (id, name, type) VALUES
    (1,  'Salaire',      'INCOME'),
    (2,  'Freelance',    'INCOME'),
    (3,  'Alimentation', 'EXPENSE'),
    (4,  'Transport',    'EXPENSE'),
    (5,  'Logement',     'EXPENSE'),
    (6,  'Internet',     'EXPENSE'),
    (7,  'Santé',        'EXPENSE'),
    (8,  'Études',       'EXPENSE'),
    (9,  'Loisirs',      'EXPENSE'),
    (10, 'Autre',        'EXPENSE');

-- =============================================================================
-- COMPTE DE DÉMONSTRATION
-- Identifiants : admin / admin123
-- Mot de passe hashé en SHA-256 :
--   SHA-256("admin123") =
--   240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- =============================================================================
INSERT IGNORE INTO users (id, username, password) VALUES
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- =============================================================================
-- DONNÉES DE DÉMONSTRATION (transactions du mois courant)
-- Ces données permettent de tester immédiatement toutes les fonctionnalités.
-- =============================================================================

-- Transactions de démonstration (mois courant)
INSERT IGNORE INTO transactions
    (id, user_id, category_id, amount, type, description, transaction_date, payment_method)
VALUES
    (1, 1, 1, 2000000.00, 'INCOME',  'Salaire octobre',          DATE_FORMAT(NOW(), '%Y-%m-01'), 'Virement'),
    (2, 1, 2,  500000.00, 'INCOME',  'Mission freelance',        DATE_FORMAT(NOW(), '%Y-%m-05'), 'Mobile Money'),
    (3, 1, 3,  180000.00, 'EXPENSE', 'Courses alimentaires',     DATE_FORMAT(NOW(), '%Y-%m-03'), 'Espèces'),
    (4, 1, 4,   45000.00, 'EXPENSE', 'Taxi et transport',        DATE_FORMAT(NOW(), '%Y-%m-04'), 'Mobile Money'),
    (5, 1, 5,  400000.00, 'EXPENSE', 'Loyer mensuel',            DATE_FORMAT(NOW(), '%Y-%m-01'), 'Virement'),
    (6, 1, 6,   15000.00, 'EXPENSE', 'Abonnement internet',      DATE_FORMAT(NOW(), '%Y-%m-02'), 'Mobile Money'),
    (7, 1, 9,   60000.00, 'EXPENSE', 'Sortie cinéma et resto',   DATE_FORMAT(NOW(), '%Y-%m-08'), 'Carte bancaire'),
    (8, 1, 3,   95000.00, 'EXPENSE', 'Restaurant',               DATE_FORMAT(NOW(), '%Y-%m-10'), 'Carte bancaire'),
    (9, 1, 7,   30000.00, 'EXPENSE', 'Consultation médecin',     DATE_FORMAT(NOW(), '%Y-%m-12'), 'Espèces');

-- Budget de démonstration (mois courant)
INSERT IGNORE INTO budgets (id, user_id, category_id, amount_limit, month, year)
VALUES
    (1, 1, 3, 300000.00, MONTH(NOW()), YEAR(NOW())),
    (2, 1, 4,  80000.00, MONTH(NOW()), YEAR(NOW())),
    (3, 1, 5, 400000.00, MONTH(NOW()), YEAR(NOW())),
    (4, 1, 9, 100000.00, MONTH(NOW()), YEAR(NOW()));

-- Objectif d'épargne de démonstration
INSERT IGNORE INTO savings_goals (id, user_id, name, target_amount, current_amount, deadline)
VALUES
    (1, 1, 'Nouveau PC',      3000000.00, 1200000.00, DATE_ADD(NOW(), INTERVAL 6 MONTH)),
    (2, 1, 'Vacances',        1500000.00,  450000.00, DATE_ADD(NOW(), INTERVAL 4 MONTH)),
    (3, 1, 'Fonds d''urgence', 2000000.00, 2000000.00, NULL);
