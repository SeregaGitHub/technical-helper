import { Directive } from '@angular/core';
import { AuthService } from '../auth/service/auth.service';

@Directive({
  selector: '[appUserProfile]',
  exportAs: 'userProfile'
})
export class UserProfileDirective {

  constructor(private _authService: AuthService) { }

  getUserProfile() {
    return this._authService.getUserProfile();
  }
}
