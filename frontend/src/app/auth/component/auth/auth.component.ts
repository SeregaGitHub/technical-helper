import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
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
export class AuthComponent {

  authForm: any;
  authError: any;

  constructor(private service: AuthService, private router: Router) {
    this.authForm = new FormGroup({
    username: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
    password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)])
  });
  }

  onSubmit() {
    console.log(this.authForm.value);  // NEED DELETE !!!
    this.service.auth(this.authForm.value).subscribe(
      (responce) => {
        console.log(responce);         // NEED DELETE !!!
        if (responce.token != null) {
          localStorage.setItem("token", responce.token);
          this.router.navigateByUrl("/breakage")
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
