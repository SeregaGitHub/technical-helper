import { HttpHeaders, HttpParams, HttpParamsOptions } from "@angular/common/http";

export class HttpHeadersFactory {
    
    public static createPermanentHeaders(): HttpHeaders {
        return new HttpHeaders({
            Authorization: `Bearer ${localStorage.getItem("thJwt")}`,
            'Content-Type': 'application/json'
        })
    };

    public static createBreakageRequestParams(
        pageIndex: number, pageSize: number, sortBy: String, direction: String, 
        statusNew: boolean, statusSolved: boolean, statusInProgress: boolean, 
        statusPaused: boolean, statusRedirected: boolean, statusCancelled: boolean,
        priorityUrgently: boolean, priorityHigh: boolean, priorityMedium: boolean, priorityLow: boolean,
        executor: String, deadline: boolean, searchText: string | null
    ): HttpParams {
        
            const myObject: any = {
              pageIndex: pageIndex.toString(), 
              pageSize: pageSize.toString(), 
              sortBy: sortBy,
              direction: direction,
              statusNew: statusNew.toString(),
              statusSolved: statusSolved.toString(),
              statusInProgress: statusInProgress.toString(),
              statusPaused: statusPaused.toString(),
              statusRedirected: statusRedirected.toString(),
              statusCancelled: statusCancelled.toString(),
              priorityUrgently: priorityUrgently.toString(),
              priorityHigh: priorityHigh.toString(),
              priorityMedium: priorityMedium.toString(),
              priorityLow: priorityLow.toString(),
              breakageExecutor: executor,
              deadline: deadline.toString(),
        };

        const params: HttpParamsOptions = { fromObject: myObject } as HttpParamsOptions;
        let httpParams = new HttpParams(params);

        if (searchText != null) {
            httpParams = httpParams.set('searchText', searchText)
        }

        return httpParams;
    };
}