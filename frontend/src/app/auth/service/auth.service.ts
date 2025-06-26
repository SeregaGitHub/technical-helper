import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AUTH_URL, BASE_URL, GATEWAY_URL } from '../../util/constant';
import { UserProfile } from '../../model/user-profile';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private userProfile: any;
  public isAuthenticated = false;

  getUserProfile() {
    return this.userProfile;
  }

  setUserProfile(name: String, role: String) {
    this.userProfile = new UserProfile();
    this.userProfile.name = name;
    this.userProfile.role = role;
    this.isAuthenticated = true;
  }
  // public username$ = new Subject<String>();
  // public userRole$ = new Subject<String>();

  // private username$: any;
  // private userRole$: any;

  constructor(private _http: HttpClient) { }

  auth(authRequest: any): Observable<any> {
    return this._http.post<any>(GATEWAY_URL + BASE_URL + AUTH_URL, authRequest, {
      headers: new HttpHeaders({
           'Content-Type':  'application/json',
         })
    });
  }

  // setUsername$(name: String) {
  //   this.username$ = name;
  // }

  // setUserRole$(role: String) {
  //   this.userRole$ = role;
  // }

	// public setUserProfile(username: String, userRole: String) {
  //  	this.username$.next(username);
  //   this.userRole$.next(userRole);  
  // }

  logout() {
    //localStorage.clear();
    localStorage.removeItem('thJwt');
    //localStorage.removeItem('thUn');
    //localStorage.removeItem('thUr');
    //this.username$.unsubscribe();
    //this.userRole$.unsubscribe();
    this.userProfile = null;
    this.isAuthenticated = false; 
  }
}
