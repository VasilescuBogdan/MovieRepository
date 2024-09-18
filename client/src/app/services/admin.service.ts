import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { JWTTokenService } from './jwttoken-service.service';

const API_URL = 'http://localhost:8080/api/v1/';


@Injectable({
  providedIn: 'root'
})
export class AdminService {
  
  
  constructor(private http: HttpClient, private jwtService : JWTTokenService) { }

}
