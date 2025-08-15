package ru.kraser.technical_helper.main_server.util.error_handler;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import static ru.kraser.technical_helper.common_module.util.Constant.SERVER_ERROR;

@UtilityClass
public class ThrowMainServerException {

    public void departmentHandler(String message, String name) {
        if (message.contains("uk_department_name")) {
            throw new AlreadyExistsException("Отдел: " + name + ", - уже существует." +
                    " Используйте другое имя !!!");
        } else if (message.contains("не существует")) {
            throw new NotFoundException(message);
        } else {
            throw new ServerException(SERVER_ERROR);
        }
    }

    public void userHandler(String message, String name) {
        if (message.contains("uk_users_username")) {
            throw new AlreadyExistsException("Сотрудник: " + name + ", - уже существует." +
                    " Используйте другое имя !!!");
        } else if (message.contains("fk_users_department") || message.contains("Unable to find")) {
            throw new NotFoundException("Отдел в котором находится сотрудник не существует !!!");
        } else if (message.contains("ch_users_role")) {
            throw new NotFoundException("Роль, присвоенная сотруднику - не существует !!!");
        } else if (message.contains("не существует")) {
            throw new NotFoundException(message);
        } else {
            throw new ServerException(SERVER_ERROR);
        }
    }

    public void isExist(int response, String entity) {
        if (response != 1) {
            throw new NotFoundException("Данный " + entity + " не существует !!!");
        }
    }
}
