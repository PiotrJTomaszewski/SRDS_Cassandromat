CREATE KEYSPACE IF NOT EXISTS Cassandromat
  WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 3 };

USE Cassandromat;

DROP TABLE IF EXISTS PostBox;
DROP TABLE IF EXISTS PostBoxContent;
DROP TABLE IF EXISTS Courier;
DROP TABLE IF EXISTS PackageLog;
DROP TABLE IF EXISTS WarehouseContent;
DROP TABLE IF EXISTS CourierTrunkPackages;
DROP TABLE IF EXISTS Client;

CREATE TABLE PostBox (
  postbox_id text,
  district text,
  capacity_S int,
  capacity_M int,
  capacity_L int,
  PRIMARY KEY (district, postbox_id)
);

-- CREATE TABLE Courier (
--   courier_id text,
--   capacity_S int,
--   capacity_M int,
--   capacity_L int,
--   PRIMARY KEY (courier_id)
-- );

CREATE TABLE PostBoxContent (
  postbox_id text,
  package_id text,
  container_size int,
  client_id text,
  PRIMARY KEY (postbox_id, package_id)
);

CREATE TABLE PackageLog (
  package_id text,
  district_dest text,
  size int,
  client_id text,
  is_in_warehouse boolean,
  is_delivered boolean,
  is_picked_up boolean,
  PRIMARY KEY (package_id)
);
-- Split by month or sth

CREATE TABLE WarehouseContent (
  package_id text,
  courier_id text,
  district_dest text,
  size int,
  client_id text,
  PRIMARY KEY (package_id)
);

CREATE TABLE CourierTrunkPackages (
  courier_id text,
  package_id text,
  district_dest text,
  size int,
  client_id text,
  container_size int,
  PRIMARY KEY (courier_id, package_id)
);

CREATE TABLE Client (
  client_id text,
  district text,
  PRIMARY KEY (client_id)
);
