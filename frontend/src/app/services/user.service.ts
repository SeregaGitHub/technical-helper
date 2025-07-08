import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { CreateUserDto } from '../model/user/create-user-dto';
import { HttpHeadersFactory } from '../generator/headers-factory';
import { ADMIN_URL, ALL_URL, BASE_URL, DELETE_URL, GATEWAY_URL, USER_ID, USER_URL } from '../util/constant';
import { UpdateUserDto } from '../model/user/update-user-dto';
import { ChangeUserPasswordDto } from '../model/user/change-user-password-dto';

@Injectable({
  providedIn: 'root'
})
export class UserService {

    constructor(private _http: HttpClient) { }

    userSubject = new BehaviorSubject<any>({
        users: [],
        loading: false,
        newUser: null
    });

    createUser(createUserDto: CreateUserDto): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.post(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL, createUserDto, {headers})
            .pipe(
                tap((newUser) => {
                    const currentState = this.userSubject.value;

                    this.userSubject.next({...currentState,
                    users:
                    [newUser, ...currentState.users]
                  });
                })
            );
    };

    updateUser(updateUserDto: UpdateUserDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL, updateUserDto, {headers})
            .pipe(
                tap((user) => {
                    const currentState = this.userSubject.value;

                    this.userSubject.next({...currentState,
                    users:
                    [user, ...currentState.users]
                  });
                })
            );
    };

    changeUserPassword(changeUserPasswordDto: ChangeUserPasswordDto, id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + '/password', changeUserPasswordDto, {headers});
    };

    getAllUsers(): Observable<any> {

        const headers = HttpHeadersFactory.createPermanentHeaders();

        return this._http.get(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + ALL_URL, {headers})
            .pipe(
                tap(
                    (users) => {
                        const currentState = this.userSubject.value;
                        this.userSubject.next({...currentState, users})
                    }
                )
            );

    };

    deleteUser(id: string): Observable<any> {

        let headers = HttpHeadersFactory.createPermanentHeaders();
        headers = headers.append(USER_ID, id);

        return this._http.patch(GATEWAY_URL + BASE_URL + ADMIN_URL + USER_URL + DELETE_URL, null, {headers});
    };
}
