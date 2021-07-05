create table IF not Exists BOT_SESSION
(
    ID INT auto_increment,
    BOT_QQ VARCHAR(20),
    SESSION VARCHAR(255),
    constraint BOT_SESSION_PK
    primary key(ID)
);
create table IF not Exists QQ_GROUP
(
    ID INT auto_increment,
    GROUP_ID INT,
    RANDOM_TALK INT default 1,
    SERVER VARCHAR(255) default STRINGDECODE('\u68a6\u6c5f\u5357'),
    constraint QQ_GROUP_PK
    primary key(ID)
);