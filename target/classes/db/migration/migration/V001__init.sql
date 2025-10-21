CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(200) NOT NULL
);

CREATE TABLE users_roles (
                             user_id BIGINT NOT NULL,
                             role VARCHAR(20) NOT NULL,
                             CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE cards (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       card_number_enc TEXT NOT NULL,
                       holder_name VARCHAR(100) NOT NULL,
                       expiry DATE NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       balance DECIMAL(19,2) NOT NULL,
                       owner_id BIGINT NOT NULL,
                       CONSTRAINT uq_card_number UNIQUE (card_number_enc),
                       CONSTRAINT fk_cards_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_cards_owner ON cards(owner_id);
CREATE INDEX idx_cards_status ON cards(status);