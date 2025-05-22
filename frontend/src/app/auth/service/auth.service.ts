import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AUTH_URL, BASE_URL, GATEWAY_URL } from '../../util/constant';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  auth(authRequest: any): Observable<any> {
    return this.http.post<any>(GATEWAY_URL + BASE_URL + AUTH_URL, authRequest, {
      headers: new HttpHeaders({
           'Content-Type':  'application/json',
         })
    });
  }

}
