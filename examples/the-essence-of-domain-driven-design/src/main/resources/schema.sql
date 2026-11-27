-- The Department aggregate: one root table, three child tables, natural keys everywhere.

CREATE TABLE department (
  code VARCHAR(20) PRIMARY KEY
);

CREATE TABLE instrument (
  department VARCHAR(20)  NOT NULL REFERENCES department (code),
  id         VARCHAR(13)  NOT NULL,
  name       VARCHAR(100) NOT NULL,
  supplier   VARCHAR(100),
  state      VARCHAR(30)  NOT NULL,
  PRIMARY KEY (department, id)
);

CREATE TABLE repair_box (
  department VARCHAR(20) NOT NULL REFERENCES department (code),
  box_number INT         NOT NULL,
  category   VARCHAR(20) NOT NULL,
  state      VARCHAR(20) NOT NULL,
  PRIMARY KEY (department, box_number)
);

CREATE TABLE repair_box_item (
  department VARCHAR(20)  NOT NULL,
  box_number INT          NOT NULL,
  position   INT          NOT NULL,
  instrument VARCHAR(13)  NOT NULL,
  damage     VARCHAR(10)  NOT NULL,
  note       VARCHAR(200) NOT NULL,
  PRIMARY KEY (department, box_number, position),
  FOREIGN KEY (department, box_number) REFERENCES repair_box (department, box_number)
);

-- The RepairJob aggregate: one row per instrument in the workshop. The CHECKs keep the outcome
-- columns in step with the state.

CREATE TABLE repair_job (
  id            VARCHAR(44)   PRIMARY KEY,
  instrument    VARCHAR(13)   NOT NULL,
  department    VARCHAR(20)   NOT NULL,
  damage        VARCHAR(10)   NOT NULL,
  note          VARCHAR(200)  NOT NULL,
  state         VARCHAR(20)   NOT NULL,
  cost_amount   NUMERIC(19,4),
  cost_currency CHAR(3),
  reason        VARCHAR(200),
  CHECK ((state = 'REPAIRED') = (cost_amount IS NOT NULL)),
  CHECK ((cost_amount IS NULL) = (cost_currency IS NULL)),
  CHECK ((state = 'WRITTEN_OFF') = (reason IS NOT NULL))
);

-- The Asset aggregate. The repair cost is not stored: the asset adds up its repairs.

CREATE TABLE asset (
  instrument     VARCHAR(13)   PRIMARY KEY,
  price_amount   NUMERIC(19,4) NOT NULL,
  price_currency CHAR(3)       NOT NULL,
  state          VARCHAR(20)   NOT NULL
);

CREATE TABLE asset_repair (
  asset         VARCHAR(13)   NOT NULL REFERENCES asset (instrument),
  job           VARCHAR(44)   NOT NULL,
  cost_amount   NUMERIC(19,4) NOT NULL,
  cost_currency CHAR(3)       NOT NULL,
  PRIMARY KEY (asset, job)
);
