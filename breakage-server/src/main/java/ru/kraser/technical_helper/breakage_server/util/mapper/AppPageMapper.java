package ru.kraser.technical_helper.breakage_server.util.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;

@UtilityClass
public class AppPageMapper {

    public AppPage toAppPage(Page<?> page) {
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
                .build();
    }
}
