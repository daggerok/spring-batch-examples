CREATE SCHEMA IF NOT EXISTS my_database DEFAULT CHARACTER SET utf8
;
CREATE
USER IF NOT EXISTS 'my_user'@'%' IDENTIFIED BY 'my_password'
;
GRANT ALL
ON my_database.* TO 'my_user'@'%'
;
flush
privileges
;
