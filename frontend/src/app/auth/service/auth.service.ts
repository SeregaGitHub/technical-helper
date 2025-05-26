import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { AUTH_URL, BASE_URL, GATEWAY_URL } from '../../util/constant';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // public username$ = new Subject<String>();
  // public userRole$ = new Subject<String>();

  constructor(private http: HttpClient) { }

  auth(authRequest: any): Observable<any> {
    return this.http.post<any>(GATEWAY_URL + BASE_URL + AUTH_URL, authRequest, {
      headers: new HttpHeaders({
           'Content-Type':  'application/json',
         })
    });
  }

	// public setUserProfile(username: String, userRole: String) {
  //  	this.username$.next(username);
  //   this.userRole$.next(userRole);  
  // }

  logout() {
    //localStorage.clear();
    localStorage.removeItem('thJwt');
    localStorage.removeItem('thUn');
    localStorage.removeItem('thUr');
    //this.username$.unsubscribe();
    //this.userRole$.unsubscribe(); 
  }
}
