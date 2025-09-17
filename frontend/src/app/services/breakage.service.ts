import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { HttpHeadersFactory } from '../generator/headers-factory';
import { ADMIN_URL, BASE_URL, BREAKAGE_ID, BREAKAGE_URL, CURRENT_URL, EMPLOYEE_URL, GATEWAY_URL, TECHNICIAN_URL, USER_URL } from '../util/constant';
import { Executor } from '../enum/executor.enum';
import { CreateBreakageDto } from '../model/breakage/create-breakage-dto';
import { UpdateBreakagePriorityDto } from '../model/breakage/update-breakage-priority-dto';
import { UpdateBreakageStatusDto } from '../model/breakage/update-breakage-status-dto';

@Injectable({
  providedIn: 'root'
})
export class BreakageService {

  constructor(private _http: HttpClient) { }

  breakageSubject = new BehaviorSubject<any>({
          breakages: [],
          loading: false,
          newBreakage: null
      });

  createBreakage(createBreakageDto: CreateBreakageDto): Observable<any> {
  
      const headers = HttpHeadersFactory.createPermanentHeaders();
  
      return this._http.post(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL, createBreakageDto, {headers})
          .pipe(
              tap((newBreakage) => {
                  const currentState = this.breakageSubject.value;
  
                  this.breakageSubject.next({...currentState,
                  breakages:
                  [newBreakage, ...currentState.breakages]
                });
              })
          );
  };    

  getAllBreakages(
        pageIndex: number, pageSize: number, sortBy: String, direction: String, 
        statusNew: boolean, statusSolved: boolean, statusInProgress: boolean, 
        statusPaused: boolean, statusRedirected: boolean, statusCancelled: boolean,
        priorityUrgently: boolean, priorityHigh: boolean, priorityMedium: boolean, priorityLow: boolean,
        executor: Executor, deadline: boolean, searchText: string | null
  ): Observable<any> {
  
    const headers = HttpHeadersFactory.createPermanentHeaders();
    const httpParams = HttpHeadersFactory.createBreakageRequestParams(
        pageIndex, pageSize, sortBy, direction, 
        statusNew, statusSolved, statusInProgress, 
        statusPaused, statusRedirected, statusCancelled,
        priorityUrgently, priorityHigh, priorityMedium, priorityLow,
        executor.toString(), deadline, searchText
    );
    const httpOptions = { params: httpParams, headers: headers };
  
    return this._http.get(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL, httpOptions)
      .pipe(
          tap(
            (allBreakages) => {
              const currentState = this.breakageSubject.value;
                this.breakageSubject.next({...currentState, allBreakages})
            })
      );
  };

  getBreakageById(id: string): Observable<any> {
      
      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);
      
      return this._http.get(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL, {headers})
          .pipe(
              tap((breakage) => {
                  const currentState = this.breakageSubject.value;
      
                  this.breakageSubject.next({...currentState, 
                  breakages:
                  [breakage, ...currentState.breakages] 
                });
              })
          );
  };

  getAdminAndTechnicianList(): Observable<any> {
      const headers = HttpHeadersFactory.createPermanentHeaders();

      return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + BREAKAGE_URL, {headers})
        .pipe(
          tap(
            (list) => {
              const currentState = this.breakageSubject.value;
              this.breakageSubject.next({...currentState, list})
            })
      );
  };

  setPriority(id: string, updateBreakagePriorityDto: UpdateBreakagePriorityDto): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + '/priority', updateBreakagePriorityDto, {headers})
          .pipe(
              tap((updatedBreakage) => {
                  const currentState = this.breakageSubject.value;
  
                  this.breakageSubject.next({...currentState,
                  breakages:
                  [updatedBreakage, ...currentState.breakages]
                });
              })
          );
  }

  setStatus(id: string, updateBreakageStatusDto: UpdateBreakageStatusDto): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + '/status', updateBreakageStatusDto, {headers})
          .pipe(
              tap((updatedBreakage) => {
                  const currentState = this.breakageSubject.value;
  
                  this.breakageSubject.next({...currentState,
                  breakages:
                  [updatedBreakage, ...currentState.breakages]
                });
              })
          );
  }

}
