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

  constructor(private service: AuthService, private router: Router) {}

  authForm = new FormGroup({
    username: new FormControl("", [Validators.required, Validators.min(4), Validators.max(64)]),
    password: new FormControl("", [Validators.required, Validators.min(4), Validators.max(128)])
  });

  onSubmit() {
    console.log(this.authForm.value);  // NEED DELETE !!!
    this.service.auth(this.authForm.value).subscribe(
      (responce) => {
        console.log(responce);         // NEED DELETE !!!
        if (responce.token != null) {
          localStorage.setItem("token", responce.token);
          this.router.navigateByUrl("/breakage")
        }
      }
    )
  }
}
