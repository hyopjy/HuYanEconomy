truncate table FishRanking;
drop table FishRanking;
drop table FISH;


create table FISH
(
  ID            INTEGER not null
  primary key,
  DESCRIPTION   TEXT not null,
  DIFFICULTY    INTEGER not null,
  DIMENSIONS1   INTEGER not null,
  DIMENSIONS2   INTEGER not null,
  DIMENSIONS3   INTEGER not null,
  DIMENSIONS4   INTEGER not null,
  DIMENSIONSMAX INTEGER not null,
  DIMENSIONSMIN INTEGER not null,
  LEVEL         INTEGER not null,
  NAME          TEXT not null,
  PRICE         INTEGER not null,
  SPECIAL       BOOLEAN not null
);


create table FISHRANKING
(
  ID           INTEGER          not null
  primary key,
  DATE         TIMESTAMP,
  DIMENSIONS   INTEGER          not null,
  FISHRODLEVEL INTEGER          not null,
  MONEY        DOUBLE PRECISION not null,
  NAME         TEXT,
  QQ           BIGINT           not null,
  FISHID       INTEGER,
  FISHPONDID   INTEGER,
constraint FKC6M1DLMDLPDTOXES89MIELNK1
        foreign key (FISHID) references FISH,
constraint FKLHE6FKUXF851B5VDLL7R1V3R3
foreign key (FISHPONDID) references FISHPOND
);
