CREATE TABLE JOB_INSTANCE (
  JOBINSTANCEID   BIGSERIAL NOT NULL PRIMARY KEY,
  VERSION         INTEGER,
  JOBNAME         VARCHAR(512),
  APPLICATIONNAME VARCHAR(512)
)!!

CREATE TABLE JOB_EXECUTION (
  JOBEXECUTIONID  BIGSERIAL NOT NULL PRIMARY KEY,
  JOBINSTANCEID   BIGINT             NOT NULL,
  VERSION         INTEGER,
  CREATETIME      TIMESTAMP,
  STARTTIME       TIMESTAMP,
  ENDTIME         TIMESTAMP,
  LASTUPDATEDTIME TIMESTAMP,
  BATCHSTATUS     VARCHAR(30),
  EXITSTATUS      VARCHAR(512),
  JOBPARAMETERS   VARCHAR(3000),
  RESTARTPOSITION VARCHAR(255),
  CONSTRAINT FK_JOB_EXECUTION_JOB_INSTANCE FOREIGN KEY (JOBINSTANCEID) REFERENCES JOB_INSTANCE (JOBINSTANCEID) ON DELETE CASCADE
)!!

CREATE TABLE STEP_EXECUTION (
  STEPEXECUTIONID    BIGSERIAL NOT NULL PRIMARY KEY,
  JOBEXECUTIONID     BIGINT             NOT NULL,
  VERSION            INTEGER,
  STEPNAME           VARCHAR(255),
  STARTTIME          TIMESTAMP,
  ENDTIME            TIMESTAMP,
  BATCHSTATUS        VARCHAR(30),
  EXITSTATUS         VARCHAR(512),
  EXECUTIONEXCEPTION VARCHAR(2048),
  PERSISTENTUSERDATA BYTEA,
  READCOUNT          INTEGER,
  WRITECOUNT         INTEGER,
  COMMITCOUNT        INTEGER,
  ROLLBACKCOUNT      INTEGER,
  READSKIPCOUNT      INTEGER,
  PROCESSSKIPCOUNT   INTEGER,
  FILTERCOUNT        INTEGER,
  WRITESKIPCOUNT     INTEGER,
  READERCHECKPOINTINFO  BYTEA,
  WRITERCHECKPOINTINFO  BYTEA,
  CONSTRAINT FK_STEP_EXECUTION_JOB_EXECUTION FOREIGN KEY (JOBEXECUTIONID) REFERENCES JOB_EXECUTION (JOBEXECUTIONID) ON DELETE CASCADE
)!!

CREATE TABLE PARTITION_EXECUTION (
  PARTITIONEXECUTIONID  INTEGER NOT NULL,
  STEPEXECUTIONID       BIGINT  NOT NULL,
  VERSION               INTEGER,
  BATCHSTATUS           VARCHAR(30),
  EXITSTATUS            VARCHAR(512),
  EXECUTIONEXCEPTION    VARCHAR(2048),
  PERSISTENTUSERDATA    BYTEA,
  READERCHECKPOINTINFO  BYTEA,
  WRITERCHECKPOINTINFO  BYTEA,
  PRIMARY KEY (PARTITIONEXECUTIONID, STEPEXECUTIONID),
  CONSTRAINT FK_PARTITION_EXECUTION_STEP_EXECUTION FOREIGN KEY (STEPEXECUTIONID) REFERENCES STEP_EXECUTION (STEPEXECUTIONID) ON DELETE CASCADE
)!!
