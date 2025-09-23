package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.enums.Role;

@UtilityClass
public class AppPageMapper {

    public AppPage toAppPage(Page<?> page, Role currentUserRole) {

        boolean isForEmployee = currentUserRole == Role.EMPLOYEE;

        return AppPage.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .offset(page.getPageable().getOffset())
                .first(page.isFirst())
                .last(page.isLast())
                .isForEmployee(isForEmployee)
                .build();
    }
}
