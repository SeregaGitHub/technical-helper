package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AppPageUtil {

    public List<Status> createStatusList(boolean statusNew, boolean statusSolved, boolean statusInProgress,
                                         boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
                                         boolean deadline) {
        List<Status> statusList = new ArrayList<>();

        if (deadline) {
            statusList.add(Status.NEW);
            statusList.add(Status.IN_PROGRESS);
        } else {
            if (statusNew) {statusList.add(Status.NEW);}
            if (statusSolved) {statusList.add(Status.SOLVED);}
            if (statusInProgress) {statusList.add(Status.IN_PROGRESS);}
            if (statusPaused) {statusList.add(Status.PAUSED);}
            if (statusRedirected) {statusList.add(Status.REDIRECTED);}
            if (statusCancelled) {statusList.add(Status.CANCELLED);}
        }

        return statusList.isEmpty() ? List.of(Status.values()) : statusList;
    }

    public List<Priority> createPriorityList(boolean priorityUrgently, boolean priorityHigh,
                                             boolean priorityMedium, boolean priorityLow) {
        List<Priority> priorityList = new ArrayList<>();

        if (priorityUrgently) {priorityList.add(Priority.URGENTLY);}
        if (priorityHigh) {priorityList.add(Priority.HIGH);}
        if (priorityMedium) {priorityList.add(Priority.MEDIUM);}
        if (priorityLow) {priorityList.add(Priority.LOW);}

        return priorityList.isEmpty() ? List.of(Priority.values()) : priorityList;
    }
}
