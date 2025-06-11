import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ADMIN_URL, ALL_URL, BASE_URL, DELETE_URL, DEPARTMENT_URL, GATEWAY_URL } from '../util/constant';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

  constructor(private _http: HttpClient) { }

  departmentSubject = new BehaviorSubject<any>({
        departments: [],
        loading: false,
        newDepartment: null
      });

    private getHeaders(): HttpHeaders {
    //const token = localStorage.getItem("thJwt");
      return new HttpHeaders({
        Authorization: `Bearer ${localStorage.getItem("thJwt")}`,
        'Content-Type': 'application/json'
        })
    };

    getAllDep(): Observable<any> {
      const headers = this.getHeaders();
    //const dep = this.http.get<Department[]>(BASE_URL + this.departmentUrl, {headers});
    
      return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL, {headers})
        .pipe(
          tap(
            (dep) => {
              const currentState = this.departmentSubject.value;
              this.departmentSubject.next({...currentState, dep})
        }
      )
        //tap(_ => this.log('fetched heroes')),
        //,catchError(this.handleError<Department[]>('getAllDepartments', []))
      );
  }

  deleteDep(id: string): Observable<any> {
      let headers = this.getHeaders();
      headers = headers.append('X-TH-Department-Id', id);

      return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL, null, {headers})
        .pipe(     
          tap(
            (str) => {
              console.log(str);
            }
          )
        );
  }
}
