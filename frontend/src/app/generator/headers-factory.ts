import { HttpHeaders } from "@angular/common/http";

export class HttpHeadersFactory {
    
    public static createPermanentHeaders(): HttpHeaders {
        //const token = localStorage.getItem("thJwt");
        return new HttpHeaders({
            Authorization: `Bearer ${localStorage.getItem("thJwt")}`,
            'Content-Type': 'application/json'
        })
    };
}