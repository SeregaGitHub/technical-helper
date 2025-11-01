import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, tap } from 'rxjs';
import { ADMIN_URL, ALL_URL, BASE_URL, CURRENT_URL, DELETE_URL, DEPARTMENT_ID, DEPARTMENT_URL } from '../util/constant';
import { DepartmentDto } from '../model/department/department-dto';
import { HttpHeadersFactory } from '../generator/headers-factory';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

    constructor(private _http: HttpClient) { }

    departmentSubject = new Subject<any>();

    createDep(departmentDto: DepartmentDto): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.post(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL, departmentDto, {headers})

            .pipe(
                tap((newDepartment) => {
                    const currentState = this.departmentSubject;
                    this.departmentSubject.next({...currentState, newDepartment});
                })
            );
    };

    updateDep(departmentDto: DepartmentDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_ID, id);

        return this._http.patch(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL, departmentDto, {headers})

            .pipe(
                tap((updatedDepartment) => {
                    const currentState = this.departmentSubject;

                    this.departmentSubject.next({...currentState, updatedDepartment});
                })
            );
    };

    getAllDep(): Observable<any> {
        
        const headers = HttpHeadersFactory.createPermanentHeaders();
    
        return this._http.get(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL, {headers})
            .pipe(
                tap(
                    (allDepartments) => {
                        const currentState = this.departmentSubject;
                        this.departmentSubject.next({...currentState, allDepartments})
                    }
                )
            );
    };

    getDepById(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_ID, id);

        return this._http.get(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL, {headers})
            .pipe(
                tap((departmentById) => {
                    const currentState = this.departmentSubject;
                    this.departmentSubject.next({...currentState, departmentById});
                })
            );
    };

    deleteDep(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(DEPARTMENT_ID, id);

        return this._http.patch(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL, null, {headers});
    };
}
