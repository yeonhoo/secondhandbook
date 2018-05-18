# --- !Ups


-- Created by Vertabelo (http://vertabelo.com)
-- Last modification date: 2018-04-20 15:51:24.788

-- enable psql extensions
CREATE EXTENSION IF NOT EXISTS CITEXT;;

-- sequences
-- Sequence: book_seq
CREATE SEQUENCE book_seq
  NO MINVALUE
  NO MAXVALUE
  START WITH 1000
  NO CYCLE
;

-- Sequence: publisher_seq
CREATE SEQUENCE publisher_seq
  NO MINVALUE
  NO MAXVALUE
  START WITH 1000
  NO CYCLE
;

-- Sequence: user_seq
CREATE SEQUENCE user_seq
  NO MINVALUE
  NO MAXVALUE
  NO CYCLE
;

-- Sequence: comment_seq
CREATE SEQUENCE comment_seq
  NO MINVALUE
  NO MAXVALUE
  NO CYCLE
;

-- tables
-- Table: book
CREATE TABLE book (
  id bigint  NOT NULL,
  title varchar(200)  NOT NULL,
  author varchar(200)  NOT NULL,
  description varchar(1000)  NOT NULL,
  price bigint  NOT NULL,
  imgs varchar(200)  NOT NULL,
  status bigint  NOT NULL, -- change to INT
  up_count int  NOT NULL,
  down_count int  NOT NULL,
  user_account_id bigint  NOT NULL,
  publisher_id bigint  NOT NULL, -- can be NULL
  created timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP, -- add DEFAULT
  CONSTRAINT pk_book PRIMARY KEY (id)
);

CREATE INDEX ix_book_publisher on book (publisher_id ASC);

-- Table: comment
CREATE TABLE comment (
  id bigint  NOT NULL DEFAULT nextval('comment_seq'), -- add DEFAULT
  content varchar(1000)  NOT NULL,
  user_account_id bigint  NOT NULL,
  status bigint  NOT NULL, -- change to INT
  book_id bigint  NOT NULL,
  created timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP, -- add DEFAULT
  CONSTRAINT pk_comment PRIMARY KEY (id)
);

-- Table: publisher
CREATE TABLE publisher (
  id bigint  NOT NULL,
  name varchar(200)  NOT NULL,
  description varchar(200)  NOT NULL,
  CONSTRAINT pk_publisher PRIMARY KEY (id)
);

-- Table: status
CREATE TABLE status (
  id bigint  NOT NULL,
  name varchar(100)  NOT NULL,
  CONSTRAINT pk_status PRIMARY KEY (id)
);

-- Table: user_account
CREATE TABLE user_account (
  id bigint  NOT NULL DEFAULT nextval('user_seq'),
  email citext  NOT NULL,
  name varchar(200)  NOT NULL,
  pw varchar(100)  NOT NULL,
  created timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  verified timestamp  NULL,
  status bigint  NOT NULL,
  CONSTRAINT user_email_is_unique UNIQUE (email) NOT DEFERRABLE  INITIALLY IMMEDIATE,
  CONSTRAINT user_email_is_not_empty CHECK (email <> '') NOT DEFERRABLE INITIALLY IMMEDIATE,
  CONSTRAINT user_password_is_not_empty CHECK (pw <> '' ) NOT DEFERRABLE INITIALLY IMMEDIATE,
  CONSTRAINT verified_is_after_created CHECK (verified > created) NOT DEFERRABLE INITIALLY IMMEDIATE,
  CONSTRAINT pk_user PRIMARY KEY (id)
);

-- Table: user_status
CREATE TABLE user_status (
  id bigint  NOT NULL,
  name varchar(100)  NOT NULL,
  CONSTRAINT pk_user_status PRIMARY KEY (id)
);

-- foreign keys
-- Reference: fk_book_publisher (table: book)
ALTER TABLE book ADD CONSTRAINT fk_book_publisher
FOREIGN KEY (publisher_id)
REFERENCES publisher (id)
ON DELETE  RESTRICT
ON UPDATE  RESTRICT
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_book_status (table: book)
ALTER TABLE book ADD CONSTRAINT fk_book_status
FOREIGN KEY (status)
REFERENCES status (id)
ON DELETE  RESTRICT
ON UPDATE  RESTRICT
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_book_user_account (table: book)
ALTER TABLE book ADD CONSTRAINT fk_book_user_account
FOREIGN KEY (user_account_id)
REFERENCES user_account (id)
ON DELETE  RESTRICT
ON UPDATE  RESTRICT
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_comment_book (table: comment)
ALTER TABLE comment ADD CONSTRAINT fk_comment_book
FOREIGN KEY (book_id)
REFERENCES book (id)
ON UPDATE  CASCADE
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_comment_status (table: comment)
ALTER TABLE comment ADD CONSTRAINT fk_comment_status
FOREIGN KEY (status)
REFERENCES status (id)
ON DELETE  RESTRICT
ON UPDATE  RESTRICT
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_comment_user (table: comment)
ALTER TABLE comment ADD CONSTRAINT fk_comment_user
FOREIGN KEY (user_account_id)
REFERENCES user_account (id)
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;

-- Reference: fk_user_account_status (table: user_account)
ALTER TABLE user_account ADD CONSTRAINT fk_user_account_status
FOREIGN KEY (status)
REFERENCES user_status (id)
ON DELETE  RESTRICT
ON UPDATE  RESTRICT
  NOT DEFERRABLE
  INITIALLY IMMEDIATE
;



# --- !Downs

-- Created by Vertabelo (http://vertabelo.com)
-- Last modification date: 2018-04-20 15:51:24.788

-- foreign keys
ALTER TABLE book
  DROP CONSTRAINT fk_book_publisher;

ALTER TABLE book
  DROP CONSTRAINT fk_book_status;

ALTER TABLE book
  DROP CONSTRAINT fk_book_user_account;

ALTER TABLE comment
  DROP CONSTRAINT fk_comment_book;

ALTER TABLE comment
  DROP CONSTRAINT fk_comment_status;

ALTER TABLE comment
  DROP CONSTRAINT fk_comment_user;

ALTER TABLE user_account
  DROP CONSTRAINT fk_user_account_status;

-- tables
DROP TABLE book;

DROP TABLE comment;

DROP TABLE publisher;

DROP TABLE status;

DROP TABLE user_account;

DROP TABLE user_status;

-- sequences
DROP SEQUENCE IF EXISTS book_seq;

DROP SEQUENCE IF EXISTS publisher_seq;

DROP SEQUENCE IF EXISTS user_seq;

DROP SEQUENCE IF EXISTS comment_seq;


-- End of file.

