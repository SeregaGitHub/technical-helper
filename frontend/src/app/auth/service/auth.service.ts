import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AUTH_URL } from '../../util/constant';
import { UserProfile } from '../../model/profile/user-profile';
import { environment } from '../../../environments/environment';

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

  constructor(private _http: HttpClient) { }

  auth(authRequest: any): Observable<any> {
    return this._http.post<any>(environment.GATEWAY_URL + environment.BASE_URL + AUTH_URL, authRequest, {
      headers: new HttpHeaders({
           'Content-Type':  'application/json',
         })
    });
  }

  logout() {
    localStorage.removeItem('thJwt');
    this.userProfile = null;
    this.isAuthenticated = false; 
  }
}
