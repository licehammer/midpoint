-- 2020-05-29 09:20

CREATE INDEX iOpExecTimestampValue
  ON m_operation_execution (timestampValue);

UPDATE m_global_metadata SET value = '4.2' WHERE name = 'databaseSchemaVersion';

-- 2020-06-25 11:35

ALTER TABLE m_acc_cert_campaign CHANGE definitionRef_type definitionRef_targetType INTEGER;

COMMIT;
