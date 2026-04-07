package com.ootd.fitme.infrastructure.storage.log;

import java.io.File;

public interface LogStorage {

    void archiveLogFile(File logFile);

    void sendLogEvent(String logJson);
}