# --- !Ups

INSERT INTO user_status (id, name) VALUES (1, 'VERIFIED');
INSERT INTO user_status (id, name) VALUES (2, 'NOT_VERIFIED');

INSERT INTO status (id, name) VALUES (1, 'VERIFIED');
INSERT INTO status (id, name) VALUES (2, 'NOT_VERIFIED');

INSERT INTO user_account (email, name, pw, status) VALUES ('gata@4989.com', 'gata', 'gata', 1);
INSERT INTO user_account (email, name, pw, status) VALUES ('admin@4989.com', 'yunn', 'yunn', 1);

INSERT INTO publisher (id, name, description) VALUES (1, 'Penguin', 'Penguin Publisher');
INSERT INTO publisher (id, name, description) VALUES (2, 'Galaxy', 'Galaxy Publisher');
INSERT INTO publisher (id, name, description) VALUES (3, '문학사', '문학출판사');
INSERT INTO publisher (id, name, description) VALUES (4, '정신과학사', '정신과학출판');
INSERT INTO publisher (id, name, description) VALUES (5, 'Companhia da letra', 'Brazilian Publisher');
INSERT INTO publisher (id, name, description) VALUES (6, 'Penguin', 'One of biggest');
INSERT INTO publisher (id, name, description) VALUES (7, 'Demon', 'Demon from Hell');


INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (1, 'C completo e total', 'Herbert Schildt', 'Old C book written in portuguese.', 5, 'hnymokbdqogcpigfwn_c', 1, 5, 0, 2, 1);

INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (2, 'Pro ASP.NET MVC 4', 'Adam Freeman', 'Book has some torn marks in cover page', 5, 'nneeelw_lmykqikbccmx', 1, 5, 0, 2, 1);

INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (3, 'Visual C# 2005 How to Program (2nd Edition)', 'Paul Deitel', 'Good state', 5, 'bjmlflwymqjqyxkywfaw', 1, 5, 0, 1, 1);

INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (4, 'Demon cat recipe', 'Demon', 'Demon Possessed wild cat recipe. If interested contact me by kakaotalk @demonkat', 5, 'jhohqjxbngyhbeknxgia|yppjxex_xoqdkvkwic_v', 1, 5, 0, 1, 1);

INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (5, '파인다이닝', '작가 7인', '식탁 위의 소설, 눈으로 즐기고 혀로 맛보다', 5, 'dhwmdoi_hifkxxbqlqlc', 1, 5, 0, 1, 1);

INSERT INTO book (id, title, author, description, price, imgs, status, up_count, down_count, user_account_id, publisher_id)
VALUES
  (6, 'The shape of water', ' Guillermo del Toro', 'The Shape of Water is a 2017 American romantic fantasy drama film directed by Guillermo del Toro and written by del Toro and Vanessa Taylor', 5, 'nblwoofnddgidvadpoko|bokwpgboxlcbgwqpyoqw', 1, 5, 0, 1, 1);

INSERT INTO comment (content, user_account_id, status, book_id) VALUES ('comment number one', 1, 1, 1);
INSERT INTO comment (content, user_account_id, status, book_id) VALUES ('comment number two', 1, 1, 1);
INSERT INTO comment (content, user_account_id, status, book_id) VALUES ('comment number three', 1, 1, 1);
INSERT INTO comment (content, user_account_id, status, book_id) VALUES ('comment number four', 1, 1, 1);

