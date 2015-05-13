use gameDB;

create table users(
    id bigint unsigned auto_increment not null,
    email varchar(255) not null unique,
    login varchar(255) not null,
    password varchar(255) not null,
    score int not null default 0,
    isSuperUser bit not null default 0,
    primary key (id)
) engine=InnoDB default charset=cp1251;
