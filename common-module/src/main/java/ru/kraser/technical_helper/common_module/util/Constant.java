package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    //public static final String FRONT_URL = "http://192.168.0.101:12345";
    public static final String FRONT_URL = "http://localhost:12345";
    //public static final String MAIN_SERVER_URL = "http://main-server:12333";
    public static final String MAIN_SERVER_URL = "http://localhost:12333";
    //public static final String BREAKAGE_SERVER_URL = "http://breakage-server:12301";
    public static final String BREAKAGE_SERVER_URL = "http://localhost:12301";
    //public static final String GATEWAY_URL = "http://gateway:12344";
    //public static final String GATEWAY_URL = "http://localhost:12344";

    public static final String BASE_URL = "/api/v1";
    public static final String AUTH_URL = "/auth";
    public static final String DEFAULT_URL = "/default";
    public static final String ADMIN_URL = "/admin";
    public static final String TECHNICIAN_URL = "/technician";
    public static final String EMPLOYEE_URL = "/employee";
    public static final String DEPARTMENT_URL = "/department";
    public static final String USER_URL = "/user";
    public static final String BREAKAGE_URL = "/breakage";
    public static final String BREAKAGE_COMMENT_URL = "/comment";
    public static final String CURRENT_URL = "/current";
    public static final String ALL_URL = "/all";
    public static final String PASSWORD_URL = "/password";
    public static final String DELETE_URL = "/delete";

    public static final String STATUS_URL = "/status";
    public static final String PRIORITY_URL = "/priority";
    public static final String EXECUTOR_URL = "/executor";

    public static final String AUTH_HEADER = "Authorization";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String CURRENT_USER_ID_HEADER = "X-CurrentUser-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String DEPARTMENT_ID_HEADER = "X-Department-Id";
    public static final String USER_DEPARTMENT_ID_HEADER = "X-UserDepartment-Id";
    // public static final String DEPARTMENT_NAME_HEADER = "X-Department-Name";
    public static final String BREAKAGE_ID_HEADER = "X-Breakage-Id";
    public static final String BREAKAGE_COMMENT_ID_HEADER = "X-BreakageComment-Id";

    public static final String SERVER_ERROR = "Ошибка на сервере. Попробуйте позже !!!";
    public static final String DEPARTMENT_NOT_EXIST = "Данного отдела не существует !!!";
    public static final String USER_NOT_EXIST = "Данный пользователь не существует !!!";
    public static final String BREAKAGE_NOT_EXIST = "Данная заявка на неисправность не существует !!!";
    public static final String BREAKAGE_COMMENT_NOT_EXIST = "Данный комментарий к заявке на неисправность не существует !!!";
}
