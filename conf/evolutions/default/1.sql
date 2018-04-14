# --- First database schema

# --- !Ups

-- set ignorecase true;

-- enable psql extensions
CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE userx (
  user_id VARCHAR(40) NOT NULL,
  email CITEXT NOT NULL,
  password VARCHAR(64) NOT NULL,
  created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  verified_on TIMESTAMP WITH TIME ZONE NULL,
  -- constraints
  CONSTRAINT users_user_id_pk PRIMARY KEY (user_id),
  CONSTRAINT users_email_is_unique UNIQUE (email),
  CONSTRAINT users_email_is_not_empty CHECK (email <> ''),
  CONSTRAINT users_password_is_not_empty CHECK (password <> ''),
  CONSTRAINT verified_on_is_after_created_on CHECK (verified_on > created_on)
);
CREATE INDEX users_email_index ON userx USING BTREE (email);
CREATE INDEX users_created_on_index ON userx USING BTREE (created_on);


create sequence user_id_seq;

create table usuario (
  id                        bigint not null DEFAULT nextval('user_id_seq'),
  email                     CITEXT not null,
  name                      varchar(255) not null,
  pw                        varchar(255) not null,
  created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  verified_on TIMESTAMP WITH TIME ZONE NULL,

  CONSTRAINT pk_user primary key (id),
  CONSTRAINT usuario_email_is_unique UNIQUE (email),
  CONSTRAINT usuario_email_is_not_empty CHECK (email <> ''),
  CONSTRAINT usuario_password_is_not_empty CHECK (pw <> ''),
  CONSTRAINT verified_on_is_after_created_on CHECK (verified_on > created_on)
);

create table publisher (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_publisher primary key (id))
;

create table book (
  id                        bigint not null,
  name                      varchar(255) not null,
  price                     bigint not null,
  author                    varchar(255),
  description               varchar(1024),
  imgKey                    varchar(1024),
  reserved                  BOOLEAN,
  publisher_id              bigint,
  user_id                   bigint,
  constraint pk_book primary key (id))
;



create sequence publisher_seq start with 1000;

create sequence book_seq start with 1000;

alter table book add constraint fk_book_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
alter table book add constraint fk_book_user_1 foreign key (user_id) references usuario (id)  on delete restrict  on update restrict;
create index ix_book_publisher_1 on book (publisher_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists usuario;

drop table if exists publisher;

drop table if exists book;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists publisher_seq;

drop sequence if exists book_seq;

drop sequence if exists user_id_seq;

--- new added
drop table if EXISTS userx;

