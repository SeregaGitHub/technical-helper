import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, tap } from 'rxjs';
import { CreateUserDto } from '../model/user/create-user-dto';
import { HttpHeadersFactory } from '../generator/headers-factory';
import { ADMIN_URL, ALL_URL, BASE_URL, CURRENT_URL, DELETE_URL, USER_ID, USER_URL } from '../util/constant';
import { UpdateUserDto } from '../model/user/update-user-dto';
import { ChangeUserPasswordDto } from '../model/user/change-user-password-dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

    constructor(private _http: HttpClient) { }

    userSubject = new Subject<any>();

    createUser(createUserDto: CreateUserDto): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.post(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL, createUserDto, {headers})

            .pipe(
                tap((newUser) => {
                    const currentState = this.userSubject;
                    this.userSubject.next({...currentState, newUser});
                })
            );
    };

    updateUser(updateUserDto: UpdateUserDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL, updateUserDto, {headers})
            .pipe(
                tap((updatedUser) => {
                    const currentState = this.userSubject;
                    this.userSubject.next({...currentState, updatedUser});
                })
            );
    };

    changeUserPassword(userPasswordDto: ChangeUserPasswordDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + '/password', userPasswordDto, {headers});
    };

    getAllUsers(): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.get(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + ALL_URL, {headers})
            .pipe(
                tap(
                    (allUsers) => {
                        const currentState = this.userSubject;
                        this.userSubject.next({...currentState, allUsers})
                    }
                )
            );
    };

    getUserById(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.get(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + CURRENT_URL, {headers})
            .pipe(
                tap((user) => {
                    const currentState = this.userSubject;
                    this.userSubject.next({...currentState, user});
                })
            );
    };

    deleteUser(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(environment.GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + DELETE_URL, null, {headers});
    };

    createDefaultAdmin(): Observable<any> {

        return this._http.post(environment.GATEWAY_URL + BASE_URL + '/default', null, { 
            headers: new HttpHeaders({
           'Content-Type':  'application/json',
         }) 
            })
            .pipe(
                tap((defaultAdmin) => {
                    const currentState = this.userSubject;
                    this.userSubject.next({...currentState, defaultAdmin});
                })
            );
    }
}
