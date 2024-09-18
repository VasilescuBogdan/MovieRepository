import { Component, OnInit } from '@angular/core';
import { StorageService } from '../../services/storage.service';
import { UserService } from '../../services/user.service';
import { UserDto } from '../../dtos/user.dto';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserDto = new UserDto();
  shouldDisplayUniversity! : boolean;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.userService.getProfile().subscribe({
      next: data => {
        this.user = JSON.parse(JSON.stringify(data)) as UserDto;
        this.shouldDisplayUniversity = this.user.role === 'Role_Administrator';
      },
      error: err => {console.log(err)

      }
    });
  }
}
