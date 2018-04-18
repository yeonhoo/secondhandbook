# --- Sample dataset

# --- !Ups



insert into usuario (email, name, pw) values ('gata@4989.com.br', 'gata', 'gata');
insert into usuario (email, name, pw) values ('admin@4989.com.br', 'yunn', 'yunn');

insert into publisher (id,name) values (  1,'문학사');
insert into publisher (id,name) values (  2,'정신과학사');
insert into publisher (id,name) values (  3,'Companhia da letra');
insert into publisher (id,name) values (  4,'Penguin');

insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  3,'C completo e total', 5, 'Herbert Schildt','Old C book written in portuguese.', 'hnymokbdqogcpigfwn_c', true, NULL, 2);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  4,'Pro ASP.NET MVC 4', 10, 'Adam Freeman','Book has some torn marks in cover page', 'nneeelw_lmykqikbccmx', true, NULL, 2);
insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  5,'Visual C# 2005 How to Program (2nd Edition)', 10, 'Paul Deitel','Good state', 'bjmlflwymqjqyxkywfaw', true, NULL, 2);

insert into book (id,name, price, author, description, imgKey, reserved, publisher_id, user_id) values (  6,'Demon cat recipe', 1000, 'Demon','Demon Possessed wild cat recipe. If interested contact me by kakaotalk @demonkat', 'jhohqjxbngyhbeknxgia|yppjxex_xoqdkvkwic_v', true, NULL, 2);

# --- !Downs

drop table if exists usuario;

drop table if exists publisher;

drop table if exists book;