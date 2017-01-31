CREATE TABLE currency (
  id SMALLINT PRIMARY KEY,
  code VARCHAR(3)
);

COMMENT ON TABLE currency IS
  'Currency code representations (ISO 4217). See latest: http://www.currency-iso.org';

CREATE TABLE osta_account (
  id BIGINT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  since DATE
);

CREATE TABLE osta_auction (
  id BIGINT PRIMARY KEY,
  private BOOLEAN,
  finished BOOLEAN,
  fk_seller_account BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  bid_cnt INTEGER,
  view_cnt INTEGER,
  begin_ts TIMESTAMP,
  end_ts TIMESTAMP,
  fk_main_category INTEGER,
  fk_sub_category INTEGER
);

CREATE SEQUENCE seq_pk_osta_bid INCREMENT BY 1 MINVALUE 1000;
CREATE TABLE osta_bid (
  id BIGINT PRIMARY KEY DEFAULT nextval('seq_pk_osta_bid'),
  price NUMERIC(14,2) NOT NULL,
  time TIMESTAMP NOT NULL,
  fk_currency SMALLINT NOT NULL,
  fk_osta_auction BIGINT NOT NULL,
  fk_osta_account BIGINT
);
ALTER SEQUENCE seq_pk_osta_bid OWNED BY osta_bid.id;

CREATE SEQUENCE seq_pk_osta_image INCREMENT BY 1 MINVALUE 1000;
CREATE TABLE osta_image (
  id BIGINT PRIMARY KEY DEFAULT nextval('seq_pk_osta_image'),
  fk_osta_auction BIGINT NOT NULL,
  thumb_url TEXT,
  full_url TEXT
);
ALTER SEQUENCE seq_pk_osta_image OWNED BY osta_image.id;

ALTER TABLE osta_image ADD CONSTRAINT c_uniq_fk_osta_auction_thumb_url_full_url UNIQUE (fk_osta_auction, thumb_url, full_url);

ALTER TABLE osta_image
  ADD CONSTRAINT c_fk_osta_image_2_osta_auction FOREIGN KEY (fk_osta_auction)
REFERENCES osta_auction(id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_currency FOREIGN KEY (fk_currency)
REFERENCES currency(id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_osta_auction FOREIGN KEY (fk_osta_auction)
REFERENCES osta_auction(id);

ALTER TABLE osta_bid
  ADD CONSTRAINT c_fk_osta_account FOREIGN KEY (fk_osta_account)
REFERENCES osta_account(id);

-- CLASSIFIER strikes againa
CREATE SEQUENCE seq_pk_classifier INCREMENT BY 10 MINVALUE 10000;
CREATE TABLE classifier (
  id INTEGER PRIMARY KEY,
  fk_parent INTEGER,
  name TEXT
);
ALTER SEQUENCE seq_pk_classifier OWNED BY classifier.id;

ALTER TABLE classifier
  ADD CONSTRAINT c_fk_classifier FOREIGN KEY (fk_parent)
REFERENCES classifier(id);

ALTER TABLE classifier ADD CONSTRAINT c_uniq_fk_parent_cl_name UNIQUE (fk_parent, name);
ALTER TABLE classifier ALTER COLUMN name SET NOT NULL;

ALTER TABLE osta_auction
  ADD CONSTRAINT c_fk_auction_2_maincategory FOREIGN KEY (fk_main_category)
REFERENCES classifier(id);

ALTER TABLE osta_auction
  ADD CONSTRAINT c_fk_auction_2_seller_account FOREIGN KEY (fk_seller_account)
REFERENCES osta_account(id);

-- guarantees that bids won't be inserted multiple times per auction
-- note that bids with same price for same auction are allowed to appear
-- sometimes -- this is due to the fact of automated Osta bids...
ALTER TABLE osta_bid ADD CONSTRAINT c_uniq_bid_price_for_auction UNIQUE (price, fk_currency, fk_osta_account, fk_osta_auction);

-- the hierarchical category storage, separate from classifier, good or bad
CREATE SEQUENCE seq_pk_osta_category INCREMENT BY 1 MINVALUE 10000;

CREATE TABLE osta_category (
  id INTEGER PRIMARY KEY DEFAULT nextval('seq_pk_osta_category'),
  category ltree NOT NULL,
  deepest TEXT
);
ALTER SEQUENCE seq_pk_osta_category OWNED BY osta_category.id;
ALTER TABLE osta_category
  ADD CONSTRAINT c_unique_category_oc UNIQUE (category);

COMMENT ON COLUMN osta_category.deepest IS 'The human-readable name of deepest category in hierarchy';

ALTER TABLE osta_auction
  ADD CONSTRAINT c_fk_auction_subcategories_to_osta_category FOREIGN KEY (fk_sub_category)
REFERENCES osta_category(id);