-- 2020-05-29 09:20

CREATE INDEX iOpExecTimestampValue
  ON m_operation_execution (timestampValue);

UPDATE m_global_metadata SET value = '4.2' WHERE name = 'databaseSchemaVersion';
GO

-- 2020-06-25 11:35

sp_rename 'm_acc_cert_campaign.definitionRef_type', 'definitionRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_campaign.ownerRef_type', 'ownerRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_definition.ownerRef_type', 'ownerRef_targetType', 'COLUMN';
GO
sp_rename 'm_connector.connectorHostRef_type', 'connectorHostRef_targetType', 'COLUMN';
GO
sp_rename 'm_object.creatorRef_type', 'creatorRef_targetType', 'COLUMN';
GO
sp_rename 'm_object.modifierRef_type', 'modifierRef_targetType', 'COLUMN';
GO
sp_rename 'm_object.tenantRef_type', 'tenantRef_targetType', 'COLUMN';
GO
sp_rename 'm_report_output.reportRef_type', 'reportRef_targetType', 'COLUMN';
GO
sp_rename 'm_resource.connectorRef_type', 'connectorRef_targetType', 'COLUMN';
GO
sp_rename 'm_shadow.resourceRef_type', 'resourceRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_case.objectRef_type', 'objectRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_case.orgRef_type', 'orgRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_case.targetRef_type', 'targetRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_case.tenantRef_type', 'tenantRef_targetType', 'COLUMN';
GO
sp_rename 'm_acc_cert_wi.performerRef_type', 'performerRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.creatorRef_type', 'creatorRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.modifierRef_type', 'modifierRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.orgRef_type', 'orgRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.resourceRef_type', 'resourceRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.targetRef_type', 'targetRef_targetType', 'COLUMN';
GO
sp_rename 'm_assignment.tenantRef_type', 'tenantRef_targetType', 'COLUMN';
GO
sp_rename 'm_case_wi.originalAssigneeRef_type', 'originalAssigneeRef_targetType', 'COLUMN';
GO
sp_rename 'm_case_wi.performerRef_type', 'performerRef_targetType', 'COLUMN';
GO
sp_rename 'm_operation_execution.initiatorRef_type', 'initiatorRef_targetType', 'COLUMN';
GO
sp_rename 'm_operation_execution.taskRef_type', 'taskRef_targetType', 'COLUMN';
GO
sp_rename 'm_task.objectRef_type', 'objectRef_targetType', 'COLUMN';
GO
sp_rename 'm_task.ownerRef_type', 'ownerRef_targetType', 'COLUMN';
GO
sp_rename 'm_abstract_role.ownerRef_type', 'ownerRef_targetType', 'COLUMN';
GO
sp_rename 'm_case.objectRef_type', 'objectRef_targetType', 'COLUMN';
GO
sp_rename 'm_case.parentRef_type', 'parentRef_targetType', 'COLUMN';
GO
sp_rename 'm_case.requestorRef_type', 'requestorRef_targetType', 'COLUMN';
GO
sp_rename 'm_case.targetRef_type', 'targetRef_targetType', 'COLUMN';
GO
