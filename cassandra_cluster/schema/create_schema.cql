CREATE KEYSPACE IF NOT EXISTS Cassandromat
  WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };

USE Cassandromat;

DROP TABLE IF EXISTS PostBox;
DROP TABLE IF EXISTS PostBoxContent;
DROP INDEX IF EXISTS PostBoxContentClientIndex;
DROP TABLE IF EXISTS Courier;
DROP TABLE IF EXISTS PackageLog;
DROP TABLE IF EXISTS WarehouseContent;
DROP TABLE IF EXISTS Client;
DROP TABLE IF EXISTS District;

CREATE TABLE PostBox (
  postbox_id text,
  district text,
  capacity int,
  PRIMARY KEY (district, postbox_id)
);

CREATE TABLE Courier (
  courier_id text,
  capacity int,
  PRIMARY KEY (courier_id)
);

CREATE TABLE PostBoxContent (
  postbox_id text,
  package_id text,
  client_id text,
  is_ready_to_pickup boolean,
  was_picked_up boolean,
  PRIMARY KEY (postbox_id, package_id)
);

CREATE INDEX PostBoxContentClientIndex ON PostBoxContent(client_id);

CREATE TABLE PackageLog (
  package_id text,
  action_type int,
  action_time timestamp,
  actor_id text,
  postbox_id text,
  PRIMARY KEY (action_time, package_id, actor_id)
);

CREATE TABLE WarehouseContent (
  package_id text,
  courier_id text,
  district_dest text,
  client_id text,
  PRIMARY KEY (district_dest, package_id)
);

CREATE TABLE Client (
  client_id text,
  district text,
  PRIMARY KEY (client_id)
);

CREATE TABLE District (
  district text,
  PRIMARY KEY (district)
);
