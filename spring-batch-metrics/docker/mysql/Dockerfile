FROM mysql:8.0.24
LABEL MAINTAINER='Maksim Kostromin <daggerok@gmail.com>'
EXPOSE 3306
ENV MYSQL_ROOT_PASSWORD='spring_batch_password'
ADD --chown=mysql my.cnf /etc/mysql/conf/my.cnf
ADD --chown=mysql schema.sql /docker-entrypoint-initdb.d/
