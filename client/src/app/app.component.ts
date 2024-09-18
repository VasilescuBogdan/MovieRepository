import { Component } from '@angular/core';
import { StorageService } from './services/storage.service';
import { AuthService } from './services/auth.service';
import { JWTTokenService } from './services/jwttoken-service.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'learnersourcing-app';
  isLoggedIn = false;
  showAdminBoard = false;
  showTeacherBoard = false;
  showStudentBoard = false;
  username?: string;

  constructor(private storageService: StorageService, private authService: AuthService, private jwtService: JWTTokenService) { }

  ngOnInit(): void {
    this.isLoggedIn = this.storageService.isLoggedIn();

    if (this.isLoggedIn) {
      const user = this.storageService.getUserLoginInfo();
      this.jwtService.setToken(user.accessToken);
      const role = this.jwtService.getRole();
      this.showAdminBoard = 'ADMINISTRATOR' === role;
      this.showTeacherBoard = 'TEACHER' === role;
      this.showStudentBoard = 'STUDENT' === role;

      this.username = user.email;
    }
  }

  logout(): void {
    this.storageService.clean();

    window.location.reload();

  }
}
