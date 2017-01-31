CREATE TABLE currency (
  id SMALLINT PRIMARY KEY,
  code VARCHAR(3)
);

CREATE TABLE osta_account (
  id BIGINT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  since DATE
);

CREATE TABLE osta_auction (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  bid_cnt INTEGER,
  view_cnt INTEGER,
  begin_ts TIMESTAMP,
  end_ts TIMESTAMP,
  fk_leading_bid BIGINT
);

CREATE TABLE osta_bid (
  id BIGINT PRIMARY KEY,
  price NUMERIC(14,2) NOT NULL,
  fk_currency SMALLINT NOT NULL,
  fk_osta_auction BIGINT NOT NULL,
  fk_osta_account BIGINT NOT NULL
);

ALTER TABLE osta_auction
  ADD CONSTRAINT c_fk_osta_bid_leading_bid FOREIGN KEY (fk_leading_bid)
REFERENCES osta_bid (id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_currency FOREIGN KEY (fk_currency)
REFERENCES currency(id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_osta_auction FOREIGN KEY (fk_osta_auction)
REFERENCES osta_auction(id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_osta_account FOREIGN KEY (fk_osta_account)
REFERENCES osta_account(id);da