package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    public static final String BASE_URL = "/api/v1";
    public static final String ADMIN_URL = "/admin";

    public static final String USER_HEADER = "X-TH-User-Id";
    public static final String DEPARTMENT_HEADER = "X-TH-Department-Id";

    public static final String SERVER_ERROR = "Ошибка на сервере. Попробуйте позже !!!";
    public static final String DEPARTMENT_NOT_EXIST = "Данного отдела не существует !!!";
    public static final String USER_NOT_EXIST = "Данный пользователь не существует !!!";
}
