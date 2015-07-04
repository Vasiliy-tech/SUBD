drop database SMDB;
create database SMDB;

use SMDB;

create table `users` (
`id` mediumint unsigned auto_increment not null,
`email` char(128) not null unique,
`username` char(64) not null,
`name` char(64) not null,
`isAnonymous` tinyint unsigned not null default '0',
`about` TEXT,
primary key (`id`),
key `id__email` (`id`,`email`)
) engine=InnoDB default charset=cp1251;

create table `follow` (
`follower_id` mediumint unsigned not null,
`followee_id` mediumint unsigned not null,
 primary key (`follower_id`, `followee_id`),
 foreign key(`follower_id`) references `users`(id),
 foreign key(`followee_id`) references `users`(id)
) engine=InnoDB default charset=cp1251;

create table `forum` (
`id` mediumint unsigned auto_increment not null,
`founder_id` mediumint unsigned not null,
`name` varchar(255) not null unique ,
`short_name` varchar(255) not null unique,
`date_of_creating` TIMESTAMP default NOW(),
primary key (`id`),
key (`short_name`),
foreign key (`founder_id`) references `users`(id)
) engine=InnoDB default charset=cp1251;

create table `thread` (
`id` mediumint unsigned auto_increment not null,
`isDeleted` tinyint unsigned not null default '0',
`isClosed` tinyint unsigned not null default '0',
`founder_id` mediumint unsigned not null,
`forum_id` mediumint unsigned not null,
`message` LONGTEXT not null,
`title` char(255) not null,
`slug` char(255) not null,
`date_of_creating` TIMESTAMP default NOW(),
`likes` mediumint default 0,
`dislikes` mediumint default 0,
`amountOfPost` mediumint(8) unsigned NOT NULL DEFAULT '0',
primary key (`id`),
key `forum_id` (`forum_id`,`date_of_creating`),
key `founder_id` (`founder_id`),
foreign key(`founder_id`) references `users`(id),
foreign key(`forum_id`) references `forum`(id)
) engine=InnoDB default charset=cp1251;

create table `post` (
`id` mediumint unsigned auto_increment not null,
`isDeleted` tinyint unsigned not null default '0',
`isEdited` tinyint unsigned not null default '0',
`isApproved` tinyint unsigned not null default '0',
`isSpam` tinyint unsigned not null default '0',
`isHighlighted` tinyint unsigned not null default '0',
`author_id` mediumint unsigned not null,
`forum_id` mediumint unsigned not null,
`thread` mediumint unsigned not null,
`parent` varchar(250) not null,
`message` LONGTEXT not null,
`date_of_creating` TIMESTAMP default NOW(),
`likes` mediumint default 0,
`dislikes` mediumint default 0,
`name` char(64) not null,
primary key (`id`),
key `forum_id` (`forum_id`,`author_id`,`date_of_creating`),
key `thread` (`thread`,`date_of_creating`),
key `author_id` (`author_id`),
key `forum_id__name__author_id` (`forum_id`,`name`,`author_id`),
key `forum_id__data` (`forum_id`,`date_of_creating`),
foreign key(`forum_id`) references `forum`(id),
foreign key(`thread`) references `thread`(id)
) engine=InnoDB default charset=cp1251;

create table `subscribtion` (
`user_id` mediumint unsigned not null,
`thread_id` mediumint unsigned not null,
primary key (`user_id`, `thread_id`),
key (`user_id`, `thread_id`),
foreign key(`user_id`) references `users`(id)
) engine=InnoDB default charset=cp1251;