package ru.kraser.technical_helper.breakage_server.util.error_handler;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import static ru.kraser.technical_helper.common_module.util.Constant.SERVER_ERROR;

@UtilityClass
public class ThrowBreakageServerException {

    public void breakageHandler(String message) {
        if (message.contains("fk_breakage_department") || message.contains("Unable to find")) {
            throw new NotFoundException("Отдел в котором находится сотрудник не существует !!!");
        } else if (message.contains("не существует")) {
            throw new NotFoundException(message);
        } else {
            throw new ServerException(SERVER_ERROR);
        }
    }
}