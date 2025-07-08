import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ADMIN_URL, ALL_URL, BASE_URL, CURRENT_URL, DELETE_URL, DEPARTMENT_ID, DEPARTMENT_NAME, DEPARTMENT_URL, GATEWAY_URL } from '../util/constant';
import { DepartmentDto } from '../model/department/department-dto';
import { HttpHeadersFactory } from '../generator/headers-factory';

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

    createDep(departmentDto: DepartmentDto): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.post(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL, departmentDto, {headers})
            .pipe(
                tap((newDep) => {
                    const currentState = this.departmentSubject.value;

                    this.departmentSubject.next({...currentState, 
                    departments:
                    [newDep, ...currentState.departments] });
                })
            );
    };

    updateDep(departmentDto: DepartmentDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_ID, id);

        return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL, departmentDto, {headers})
            .pipe(
                tap((dep) => {
                    const currentState = this.departmentSubject.value;

                    this.departmentSubject.next({...currentState, 
                    departments:
                    [dep, ...currentState.departments] 
                  });
                })
            );
    };

    getAllDep(): Observable<any> {
        
        const headers = HttpHeadersFactory.createPermanentHeaders();
    
        return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL, {headers})
            .pipe(
                tap(
                    (dep) => {
                        const currentState = this.departmentSubject.value;
                        this.departmentSubject.next({...currentState, dep})
                    }
                )
            );
    };

    getDep(name: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_NAME, name);

        return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL, {headers})
            .pipe(
                tap((dep) => {
                    const currentState = this.departmentSubject.value;

                    this.departmentSubject.next({...currentState, 
                    departments:
                    [dep, ...currentState.departments] 
                  });
                })
            );
    };

    deleteDep(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_ID, id);

        return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL, null, {headers});
    };
}
