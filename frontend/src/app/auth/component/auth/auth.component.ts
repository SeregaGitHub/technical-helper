import { Component } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';


@Component({
  selector: 'app-auth',
  imports: [
    MatCardModule,
    MatFormFieldModule,
    ReactiveFormsModule
  ],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.scss'
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
