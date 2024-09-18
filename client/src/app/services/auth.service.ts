import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const AUTH_API = 'http://localhost:8080/api/v1/users/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'login',
      {
        email,
        password,
      },
      httpOptions
    );
  }

  register(firstName: string, 
    lastName: string, 
    dateOfBirth : Date,
    email: string, 
    password: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'create',
      {
        firstName,
        lastName,
        dateOfBirth,
        email,
        password,
      },
      {responseType: 'text'}
    );
  }


}