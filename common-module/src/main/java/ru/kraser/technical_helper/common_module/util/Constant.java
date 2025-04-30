package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    public static final String BASE_URL = "/api/v1";
    public static final String ADMIN_URL = "/admin";

    public static final String MESSAGE_DEPARTMENT_NOT_FOUND_EXCEPTION = "Отдел, в котором находится сотрудник - " +
            "не существует. Используйте другой отдел !!!";
}
