-- Departments are not created by the application: they come from the hospital's records.

INSERT INTO department (code) VALUES ('SURGERY');

INSERT INTO instrument (department, id, name, supplier, state) VALUES
  ('SURGERY', 'SC-1042', 'Scalpel',   NULL,       'USABLE'),
  ('SURGERY', 'FO-2001', 'Forceps',   NULL,       'USABLE'),
  ('SURGERY', 'EN-7',    'Endoscope', 'MedLease', 'USABLE');

-- Assets come from the purchase records. The leased endoscope EN-7 is not ours, so it is not here.

INSERT INTO asset (instrument, price_amount, price_currency, state) VALUES
  ('SC-1042', 60.00, 'EUR', 'ACTIVE'),
  ('FO-2001', 85.00, 'EUR', 'ACTIVE');
