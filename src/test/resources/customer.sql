drop database if EXISTS ssmdemo;
create database ssmdemo default character set utf8 collate utf8_bin;
use ssmdemo;

drop table if exists customer;
create table customer
(
  name varchar(32) not null,
  password varchar(64) not null,
  primary key(name)
)engine=innodb default charset=utf8 auto_increment=1;


#insert into customer values('admin','d033e22ae348aeb5660fc2140aec35850c4da997','',1,'admin@admin.com','','2017-08-23 15:58:07',0);
