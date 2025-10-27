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
import { UserService } from '../../../services/user.service';


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
  isCreateAdminForm: boolean = false;
  createAdminMessage: string = '';
  createDefaultAdminError: boolean = true;

  private _unsubscribe: Subject<void> = new Subject();

  constructor(private _authService: AuthService, private _router: Router, private _userService: UserService) {
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

  openCreateAdminForm(event: MouseEvent) {
    if (event.altKey) {
      this.isCreateAdminForm = true;
    }
  };

  closeCreateAdminForm() {
    this.isCreateAdminForm = false;
  };

  createDefaultAdmin() {
    this._userService.createDefaultAdmin()
      .pipe(takeUntil(this._unsubscribe))
        .subscribe({
          next: response => {
            if (response.status === 201) {
              this.createDefaultAdminError = false;
              this.createAdminMessage = response.message;
              this.deleteCreateAdminMessage();
            }
          },
          error: err => {
            if (err.status <= 0) {
              this.createAdminMessage = 'Отказано в подключении к серверу Gateway !!!';
              this.deleteCreateAdminMessage();
            } else {
              this.createAdminMessage = err.error.message;
              this.deleteCreateAdminMessage();
            }
          }
        });
  };

  private deleteCreateAdminMessage() {
      setTimeout(() => {
        this.createAdminMessage = '';
        this.createDefaultAdminError = true;
        this.closeCreateAdminForm();
      }, 3000);
  };

  get username() {
    return this.authForm.get('username')
  };

  get password() {
    return this.authForm.get('password')
  };
}
