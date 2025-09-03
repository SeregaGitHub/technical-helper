package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.kraser.technical_helper.common_module.enums.Executor;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AppPageUtil {

    public List<Status> createStatusList(boolean statusNew, boolean statusSolved, boolean statusInProgress,
                                         boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
                                         boolean deadline, Role currentUserRole, String executor) {
        List<Status> statusList = new ArrayList<>();

        if (deadline && currentUserRole != Role.EMPLOYEE && !executor.equals(Executor.NO_APPOINTED.name())) {
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

    public PageRequest createPageRequest(Integer pageSize, Integer pageIndex, String sortBy, String direction) {
        Sort.Direction breakagesDirection = direction.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(breakagesDirection, sortBy);

        return PageRequest.of(pageIndex, pageSize, sort);
    }
}
