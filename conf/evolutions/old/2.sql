# --- Sample dataset

# --- !Ups



insert into usuario (email, name, pw) values ('admin@4989.com.br', 'yun', 'yun');
insert into usuario (email, name, pw) values ('gata@4989.com.br', 'gata', 'gata');

insert into publisher (id,name) values (  1,'문학사');
insert into publisher (id,name) values (  2,'정신과학사');

insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  1,'인간의 길', 20,  '이연후', '상태 최상입니다','dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa', false, 1, 1);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  2,'신과나눈이야기', 32, '이연후','깨끗합니다','mwiekgjyn_yjmyegvbjx|mwiekgjyn_yjmyegvbjx|mwiekgjyn_yjmyegvbjx', true, 1, 1);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  3,'신과나눈이야기', 32, 'gata','좀지저분합니다', NULL, true, 1, 2);


# --- !Downs

drop table if exists usuario;

drop table if exists publisher;

drop table if exists book;