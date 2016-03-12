# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "user"(
 "id" BIGSERIAL NOT NULL PRIMARY KEY,
 "name" VARCHAR(255) NOT NULL,
 "email" VARCHAR(255) NOT NULL,
 "password" VARCHAR(255),
 "created_at" BIGINT NOT NULL,
 "updated_at" BIGINT
);

create table "post"(
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "title" TEXT NOT NULL,
    "subtitle" TEXT NOT NULL,
    "content" TEXT,
    "cover" TEXT,
    "author" VARCHAR(255) NOT NULL,
    "created_at" BIGINT NOT NULL,
    "updated_at" BIGINT
);

create table "message"(
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "first_name" VARCHAR(255) NOT NULL,
    "last_name" VARCHAR(255) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "phone" VARCHAR(255) NOT NULL,
    "message" TEXT,
    "created_at" BIGINT NOT NULL,
    "updated_at" BIGINT
);
# --- !Downs


drop table if exists "user" cascade;
drop table if exists "post" cascade;
drop table if exists "message" cascade;