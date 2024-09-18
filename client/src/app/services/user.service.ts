import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JWTTokenService } from './jwttoken-service.service';

const API_URL = 'http://localhost:8080/api/v1/';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient, private jwtService : JWTTokenService) {}

  getProfile(): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.jwtService.getToken()
    });
    return this.http.get(API_URL + 'accounts/profile', {headers: headers});
  }

  getCurrentUserId(): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.jwtService.getToken()
    });
    return this.http.get(API_URL + 'accounts/current', {headers: headers});
  }

  getStudentBoard(): Observable<any> {
    return this.http.get(API_URL + 'student', { responseType: 'text' });
  }

  getTeacherBoard(): Observable<any> {
    return this.http.get(API_URL + 'teacher', { responseType: 'text' });
  }

  getAdminBoard(): Observable<any> {
    return this.http.get(API_URL + 'admin', { responseType: 'text' });
  }

}
