CREATE KEYSPACE IF NOT EXISTS Cassandromat
  WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 3 };

USE Cassandromat;

CREATE TABLE PostBox (
  postbox_id text,
  district text,
  capacity_S int,
  capacity_M int,
  capacity_L int,
  cap_left_S int,
  cap_left_M int,
  cap_left_L int,
  open_at time,
  close_at time,
  PRIMARY KEY (district, postbox_id)
);

CREATE TABLE Courier (
  courier_id text,
  workday_start time,
  workday_end time,
  PRIMARY KEY (courier_id)
);

CREATE TABLE Package (
  package_id text,
  district_dest text,
  size int,
  client_id text,
  planned_delivery_datetime timestamp,
  pickup_datetime timestamp,
  is_delivered boolean,
  is_picked_up boolean,
  PRIMARY KEY (package_id)
);

CREATE TABLE Client (
  client_id text,
  district text,
  PRIMARY KEY (client_id)
);
