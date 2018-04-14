# --- Sample dataset

# --- !Ups



insert into usuario (email, name, pw) values ('gata@4989.com.br', 'gata', 'gata');
insert into usuario (email, name, pw) values ('admin@4989.com.br', 'yunn', 'yunn');

insert into publisher (id,name) values (  1,'문학사');
insert into publisher (id,name) values (  2,'정신과학사');
insert into publisher (id,name) values (  3,'Companhia da letra');


insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  1,'인간의 길', 20,  '이연후', '상태 최상입니다','dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa|dcpineenljbmiqw_hgxa', false, 1, 1);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  2,'신과나눈이야기', 32, '이연후','깨끗합니다','mwiekgjyn_yjmyegvbjx|mwiekgjyn_yjmyegvbjx|mwiekgjyn_yjmyegvbjx', true, 1, 1);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  3,'Small', 5, 'Yun','좀지저분합니다', NULL, true, NULL, 2);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  4,'Big brother', 13, 'Yun','좀지저분합니다', NULL, true, NULL, 2);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  5,'Lala', 16, 'Yun','좀지저분합니다', NULL, true, NULL, 2);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  6,'Teatro', 26, 'Yun','좀지저분합니다', NULL, true, NULL, 2);


# --- !Downs

drop table if exists usuario;

drop table if exists publisher;

drop table if exists book;