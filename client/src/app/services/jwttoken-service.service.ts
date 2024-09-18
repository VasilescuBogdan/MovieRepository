import { Injectable } from '@angular/core';
import jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class JWTTokenService {

    jwtToken: string = '';
    decodedToken: { [key: string]: string } = {};

    constructor() {
    }

    setToken(token: string) {
      if (token) {
        this.jwtToken = token;
      }
    }

    decodeToken() {
      if (this.jwtToken && this.jwtToken != '') {
         this.decodedToken = jwt_decode(this.jwtToken);
      }
    }

    getDecodeToken() {
      return jwt_decode(this.jwtToken);
    }

    getUserId() {
      this.decodeToken();
      return this.decodedToken ? this.decodedToken['userId'] : null;
    }

    getEmail() {
      this.decodeToken();
      return this.decodedToken ? this.decodedToken['email'] : null;
    }

    getToken() {
      return this.jwtToken;
    }

    getRole() {
      this.decodeToken();
      return this.decodedToken ? this.decodedToken['role'] : null;
    }

    getExpiryTime() {
      this.decodeToken();
      return this.decodedToken ? this.decodedToken['exp'] : null;
    }

    isTokenExpired(): boolean {
      return false;
      // const expiryTime: number = this.getExpiryTime();
      // if (expiryTime) {
      //   return ((1000 * expiryTime) - (new Date()).getTime()) < 5000;
      // } else {
      //   return false;
      // }
    }
}