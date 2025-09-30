import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';  
  
@Injectable()  
export class CustomPaginatorIntl extends MatPaginatorIntl {  
    override itemsPerPageLabel = 'Элементов на странице:';
    
    override getRangeLabel = (page: number, pageSize: number, length: number) => {
        //const initialText = `Элементов на странице:`;

        if (length == 0 || pageSize == 0) {
            //return `${initialText} 0 из ${length}`;
            return `0 из ${length}`;
        }

        length = Math.max(length, 0);

        const startIndex = page * pageSize;
        const endIndex = startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;

        // console.log('CustomPaginatorIntl');
        // console.log('startIndex');
        // console.log(startIndex);
        // console.log('endIndex');
        // console.log(endIndex);
        // console.log('length');
        // console.log(length);

        //return `${initialText} ${startIndex + 1} - ${endIndex} из ${length}`;
        return `${startIndex + 1} - ${endIndex} из ${length}`;
    }
} 