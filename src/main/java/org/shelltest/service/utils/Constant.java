package org.shelltest.service.utils;

/**
 * 常量列表
 * */
public class Constant {
    public class ResultCode {
        public static final int
            SUCCESS = 200,
            INTERNAL_ERROR = 500,
            LOGIN_FAILED = 101,
            USER_LOGIN_FAILED = 102,
            DEVELOPING = 501,
            NOT_FOUND = 404,
            ARGS_ERROR = 412,
            FILE_EXCEED = 120,
            SHELL_ERROR = 1551;
    }
    public class PropertyType {
        public static final String
            IP = "SERVER_IP",
            DEPLOY_PATH = "DEPLOY_PATH",
            BACKUP_PATH = "BACKUP_PATH",
            RUN_PATH = "RUN_PATH",
            LOG_PATH = "LOG_PATH",
            USERNAME = "SERVER_USER",
            PASSWORD = "SERVER_PASS",
            JAR_RENAME = "RENAME";
    }
    public class PropertyKey {
        public static final String
            FRONTEND = "FRONTEND",
            SERVICE = "SERVICE",
            JAR_PREFIX = "PREFIX",
            JAR_SUFFIX = "SUFFIX";
    }
    public class RequestArg {
        public static final String
            Auth = "Authorization";
    }
}
