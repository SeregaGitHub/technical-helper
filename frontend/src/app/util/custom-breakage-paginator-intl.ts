import { Injectable } from "@angular/core";
import { MatPaginatorIntl } from "@angular/material/paginator";

@Injectable()  
export class CustomBreakagePaginatorIntl extends MatPaginatorIntl {
    override itemsPerPageLabel = 'Элементов на странице:';

    override getRangeLabel = (page: number, pageSize: number, length: number) => {

        if (length == 0 || pageSize == 0) {
            return `0 из ${length}`;
        }

        const start = page * pageSize + 1;
        const end = (page + 1) * pageSize > length ? length : (page + 1) * pageSize;

        return `${start} - ${end} из ${length}`;
    }
}





// this.paginator._intl.getRangeLabel = (page: number, pageSize: number, length: number) => {
//       const start = page * pageSize + 1;
//       const end = (page + 1) * pageSize > this.totalCount? this.totalCount: (page + 1) * pageSize;
//       return `${start} - ${end} of ${this.totalLength}`;
//     };