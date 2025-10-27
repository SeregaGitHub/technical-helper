import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { UserProfileDirective } from '../../directive/user-profile.directive';

@Component({
  selector: 'app-header',
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatButtonToggleModule,
    RouterModule,
    UserProfileDirective
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
    title = 'Technical Helper';
    noAuth = '--------------------';

    constructor(public authService: AuthService, private _router: Router) {}

    logout() {
      this.authService.logout();
      this._router.navigate(["/auth"]);
    }
}
