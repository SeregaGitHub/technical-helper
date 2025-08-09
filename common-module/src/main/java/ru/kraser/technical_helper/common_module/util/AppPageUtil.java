package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AppPageUtil {

    public List<Status> createStatusList(boolean statusNew, boolean statusSolved, boolean statusInProgress,
                                         boolean statusPaused, boolean statusRedirected, boolean statusCancelled) {
        List<Status> statusList = new ArrayList<>();

        if (statusNew) {statusList.add(Status.NEW);}
        if (statusSolved) {statusList.add(Status.SOLVED);}
        if (statusInProgress) {statusList.add(Status.IN_PROGRESS);}
        if (statusPaused) {statusList.add(Status.PAUSED);}
        if (statusRedirected) {statusList.add(Status.REDIRECTED);}
        if (statusCancelled) {statusList.add(Status.CANCELLED);}

        return statusList.isEmpty() ? List.of(Status.values()) : statusList;
    }
}
