import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { catchError } from 'rxjs';


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

export class AuthComponent implements OnInit{

  authForm: any;
  authError: any;

  constructor(private authService: AuthService, private router: Router) {
    this.authForm = new FormGroup({
    username: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
    password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)])
  });
  }

  ngOnInit(): void {
    localStorage.removeItem('thJwt');
    localStorage.removeItem('thUn');
    localStorage.removeItem('thUr');
  };

  onSubmit() {
    console.log(this.authForm.value);  // NEED DELETE !!!
    this.authService.auth(this.authForm.value).subscribe(
      (responce) => {
        console.log(responce);         // NEED DELETE !!!
        if (responce.thJwt != null) {
          // console.log('localStorage');
          // console.log(responce.username);
          // console.log(responce.role);
          // console.log('localStorage');

          localStorage.setItem("thJwt", responce.thJwt);
          //localStorage.setItem("thUn", responce.username);
          //localStorage.setItem("thUr", responce.role);

          //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile());
          this.authService.setUserProfile(responce.username, responce.role);
          //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().getUserName());
          //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isAdmin());
          //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isTechnician());
          //console.log('this.service.getUserProfile() - ' + this.service.getUserProfile().isEmployee());
          //this.service.setUserProfile(responce.username, responce.userRole);

          //this.service.setUsername$(responce.username);
          //this.service.setUserRole$(responce.role);

          this.router.navigateByUrl("/breakage")

          // console.log('HeaderComponent');
          // console.log(this.service.username$);
          // console.log('HeaderComponent');
        }
      }, (err) => {
        if (err.status <= 0) {
          this.authError = 'Отказано в подключении к серверу Gateway !!!';
        } else {
          console.log(err.error.message)
          this.authError = err.error.message;
        }
      }
    )
  }

  get username() {
    return this.authForm.get('username')
  }

  get password() {
    return this.authForm.get('password')
  }
}
