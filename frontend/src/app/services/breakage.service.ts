import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, tap } from 'rxjs';
import { HttpHeadersFactory } from '../generator/headers-factory';
import { ADMIN_URL, BASE_URL, BREAKAGE_COMMENT_ID_HEADER, BREAKAGE_COMMENT_URL, BREAKAGE_ID, BREAKAGE_URL, CURRENT_URL, 
         DELETE_URL, 
         DEPARTMENT_ID, EMPLOYEE_URL, EXECUTOR_URL, GATEWAY_URL, TECHNICIAN_URL, USER_URL 
        } from '../util/constant';
import { Executor } from '../enum/executor.enum';
import { CreateBreakageDto } from '../model/breakage/create-breakage-dto';
import { UpdateBreakagePriorityDto } from '../model/breakage/update-breakage-priority-dto';
import { UpdateBreakageStatusDto } from '../model/breakage/update-breakage-status-dto';
import { AppointBreakageExecutorDto } from '../model/breakage/appoint-breakage-executor-dto';
import { CreateBreakageCommentDto } from '../model/breakage/create-breakage-comment-dto';

@Injectable({
  providedIn: 'root'
})
export class BreakageService {

  constructor(private _http: HttpClient) { }

  // breakageSubject = new BehaviorSubject<any>({});

  breakageSubject = new Subject<any>();

  createBreakage(createBreakageDto: CreateBreakageDto): Observable<any> {
  
      const headers = HttpHeadersFactory.createPermanentHeaders();
  
      return this._http.post(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL, createBreakageDto, {headers})
          // .pipe(
          //     tap((newBreakage) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, newBreakage});
          //     })
          // );

          .pipe(
              tap((newBreakage) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, newBreakage});
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
      // .pipe(
      //     tap(
      //       (allBreakages) => {
      //         const currentState = this.breakageSubject.value;
      //           this.breakageSubject.next({...currentState, allBreakages})
      //       })
      // );

      .pipe(
          tap(
            (allBreakages) => {
              const currentState = this.breakageSubject;
                this.breakageSubject.next({...currentState, allBreakages})
            })
      );
  };

  getBreakageEmployeeById(id: string): Observable<any> {
      
      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);
      
      // return this._http.get(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL, {headers})
      //     .pipe(
      //         tap((breakage) => {
      //             const currentState = this.breakageSubject.value;
      //             this.breakageSubject.next({...currentState, breakage});
      //         })
      //     );

      return this._http.get(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL, {headers})
          .pipe(
              tap((breakage) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, breakage});
              })
          );
  };

  getBreakageById(id: string): Observable<any> {
      
      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.get(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + CURRENT_URL, {headers})
          // .pipe(
          //     tap((breakage) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, breakage});
          //     })
          // );

          .pipe(
              tap((breakage) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, breakage});
              })
          );
  };

  getAdminAndTechnicianList(): Observable<any> {
      const headers = HttpHeadersFactory.createPermanentHeaders();

      return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + BREAKAGE_URL, {headers})
      //   .pipe(
      //     tap(
      //       (adminAndTechnicianList) => {
      //         const currentState = this.breakageSubject.value;
      //         this.breakageSubject.next({...currentState, adminAndTechnicianList})
      //       })
      // );

      .pipe(
          tap(
            (adminAndTechnicianList) => {
              const currentState = this.breakageSubject;
              this.breakageSubject.next({...currentState, adminAndTechnicianList})
            })
      );
  };

  setPriority(id: string, updateBreakagePriorityDto: UpdateBreakagePriorityDto): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + '/priority', updateBreakagePriorityDto, {headers})
          // .pipe(
          //     tap((updatedBreakagePriority) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, updatedBreakagePriority});
          //     })
          // );

          .pipe(
              tap((updatedBreakagePriority) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, updatedBreakagePriority});
              })
          );
  }

  cancelBreakage(id: string, departmentId: string): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);
      headers = headers.append(DEPARTMENT_ID, departmentId);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL, null, {headers})
          // .pipe(
          //     tap((chancelledBreakage) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, chancelledBreakage});
          //     })
          // );

          .pipe(
              tap((chancelledBreakage) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, chancelledBreakage});
              })
          );
  }

  setStatus(id: string, updateBreakageStatusDto: UpdateBreakageStatusDto): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + '/status', updateBreakageStatusDto, {headers})
          // .pipe(
          //     tap((updatedBreakageStatus) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, updatedBreakageStatus});
          //     })
          // );

          .pipe(
              tap((updatedBreakageStatus) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, updatedBreakageStatus});
              })
          );
  }

  addBreakageExecutor(breakageId: string, appointBreakageExecutorDto: AppointBreakageExecutorDto): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, breakageId);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + EXECUTOR_URL, appointBreakageExecutorDto, {headers})
          // .pipe(
          //     tap((updatedBreakageExecutor) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, updatedBreakageExecutor});
          //     })
          // );

          .pipe(
              tap((updatedBreakageExecutor) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, updatedBreakageExecutor});
              })
          );
  }

  dropExecutor(id: string): Observable<any> {

      let headers = HttpHeadersFactory.createPermanentHeaders();
      headers = headers.append(BREAKAGE_ID, id);

      return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + EXECUTOR_URL + DELETE_URL, null, {headers})
          // .pipe(
          //     tap((dropBreakageExecutor) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, dropBreakageExecutor});
          //     })
          // );

          .pipe(
              tap((dropBreakageExecutor) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, dropBreakageExecutor});
              })
          );
  };

  createBreakageComment(breakageId: string, createBreakageCommentDto: CreateBreakageCommentDto): Observable<any> {

    let headers = HttpHeadersFactory.createPermanentHeaders();
    headers = headers.append(BREAKAGE_ID, breakageId);

    return this._http.post(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL, createBreakageCommentDto, {headers})
          // .pipe(
          //     tap((newBreakageComment) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, newBreakageComment});
          //     })
          // );

          .pipe(
              tap((newBreakageComment) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, newBreakageComment});
              })
          );
  };

  updateBreakageComment(breakageCommentId: string, createBreakageCommentDto: CreateBreakageCommentDto): Observable<any> {

    let headers = HttpHeadersFactory.createPermanentHeaders();
    headers = headers.append(BREAKAGE_COMMENT_ID_HEADER, breakageCommentId);

    return this._http.patch(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL, createBreakageCommentDto, {headers})
          // .pipe(
          //     tap((updatedBreakageComment) => {
          //         const currentState = this.breakageSubject.value;
          //         this.breakageSubject.next({...currentState, updatedBreakageComment});
          //     })
          // );

          .pipe(
              tap((updatedBreakageComment) => {
                  const currentState = this.breakageSubject;
                  this.breakageSubject.next({...currentState, updatedBreakageComment});
              })
          );
  };

  deleteBreakageComment(breakageCommentId: string): Observable<any> {

    let headers = HttpHeadersFactory.createPermanentHeaders();
    headers = headers.append(BREAKAGE_COMMENT_ID_HEADER, breakageCommentId);
    
    return this._http.delete(GATEWAY_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL, {headers});
  }

}
