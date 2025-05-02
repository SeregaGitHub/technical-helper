package ru.kraser.technical_helper.main_server.util.error_handler;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import static ru.kraser.technical_helper.common_module.util.Constant.SERVER_ERROR;

@UtilityClass
public class ThrowException {
    public void departmentUkHandler(String message, String name) {
        if (message.contains("uk_department_name")) {
            throw new AlreadyExistsException("Отдел: " + name + ", - уже существует." +
                    " Используйте другое имя !!!");
        } else {
            throw new ServerException(SERVER_ERROR);
        }
    }
}
