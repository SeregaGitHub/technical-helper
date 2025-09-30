import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';


@Component({
  selector: 'app-auth',
  imports: [
    CommonModule,
    MatCardModule, 
    MatButtonModule,
    FormsModule,
    MatFormFieldModule, 
    MatInputModule,
    MatButtonToggleModule,
    MatIconModule,
    ReactiveFormsModule,
    MatRadioModule
  ],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})

export class AuthComponent implements OnInit, OnDestroy {

  authForm: any;
  authError: any;

  private _unsubscribe: Subject<void> = new Subject();

  constructor(private _authService: AuthService, private _router: Router) {
      this.authForm = new FormGroup({
      username: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
      password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)])
    });
  }

  ngOnInit(): void {
    localStorage.clear();
  };

  ngOnDestroy(): void {
    this._unsubscribe.next();
    this._unsubscribe.complete();
  };

  onSubmit() {

    this._authService.auth(this.authForm.value)
      .pipe(takeUntil(this._unsubscribe))
        .subscribe({
          next: response => {
            if (response.thJwt != null) {
              localStorage.setItem("thJwt", response.thJwt);
              this._authService.setUserProfile(response.username, response.role);
              this._router.navigateByUrl("/breakage");
            }
          },
          error: err => {
            if (err.status <= 0) {
              this.authError = 'Отказано в подключении к серверу Gateway !!!';
            } else {
              this.authError = err.error.message;
            }
          }
        });
  };

  // onSubmit() {
  //   console.log(this.authForm.value);  // NEED DELETE !!!
  //   this._authService.auth(this.authForm.value).subscribe(
  //     (responce) => {
  //       console.log(responce);         // NEED DELETE !!!
  //       if (responce.thJwt != null) {
  //         // console.log('localStorage');
  //         // console.log(responce.username);
  //         // console.log(responce.role);
  //         // console.log('localStorage');

  //         localStorage.setItem("thJwt", responce.thJwt);
  //         //localStorage.setItem("thUn", responce.username);
  //         //localStorage.setItem("thUr", responce.role);

  //         //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile());
  //         this._authService.setUserProfile(responce.username, responce.role);
  //         //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().getUserName());
  //         //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isAdmin());
  //         //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isTechnician());
  //         //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isEmployee());
  //         //this.service.setUserProfile(responce.username, responce.userRole);

  //         //this.service.setUsername$(responce.username);
  //         //this.service.setUserRole$(responce.role);

  //         this._router.navigateByUrl("/breakage")

  //         // console.log('HeaderComponent');
  //         // console.log(this.service.username$);
  //         // console.log('HeaderComponent');
  //       }
  //     }, (err) => {
  //       if (err.status <= 0) {
  //         this.authError = 'Отказано в подключении к серверу Gateway !!!';
  //       } else {
  //         this.authError = err.error.message;
  //       }
  //     }
  //   )
  // }

  get username() {
    return this.authForm.get('username')
  };

  get password() {
    return this.authForm.get('password')
  };
}
