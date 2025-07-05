import { Component, Inject, OnInit } from '@angular/core';
import { ApiResponse } from '../../model/response/api-response';
import { Action } from '../../enum/action.enum';
import { UserService } from '../../services/user.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { UserDtoFactory } from '../../generator/user-dto-factory';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { matchPassword } from '../../validators/match-password.validator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { Department } from '../../model/department/department';
import { DepartmentService } from '../../services/department.service';
import { BUTTON_CREATE, BUTTON_UPDATE } from '../../util/constant';
import { ChangeUserPasswordDto } from '../../model/user/change-user-password-dto';

@Component({
  selector: 'app-user-form',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule, 
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    ReactiveFormsModule
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {

  userForm: any;
  buttonName!: string;
  apiResponse: ApiResponse;
  deps!: Department[];

  constructor(private _userService: UserService,
              private _depService: DepartmentService, 
              private _dialogRef: MatDialogRef<UserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {

      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
      this._depService.getAllDep().subscribe({
        next: data => {
          this.deps = data;
        },
        error: err => {
        this.apiResponse = err.error;
        }
      });
    }

  ngOnInit(): void {
    
    if (this.data.action === Action.Update) {

      this.userForm = new FormGroup({
        username: new FormControl(this.data.username, [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
        departmentId: new FormControl('', [Validators.required]),
        role: new FormControl(this.data.role, [Validators.required])
      });

      let departmentId = this._depService.getDep(this.data.department).subscribe({
        next: data => {
          departmentId = data.id;
          this.userForm.get('departmentId').setValue(departmentId);         
        },
        error: err => {
          this.apiResponse = err.error;
        }
      });
    
      this.buttonName = BUTTON_UPDATE;
          
      } else {

        this.userForm = new FormGroup({
          username: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
          password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
          confirmPassword: new FormControl(null),
          departmentId: new FormControl("", [Validators.required]),
          role: new FormControl("", [Validators.required])
          },
          {
            validators: matchPassword
          }
        );

        this.buttonName = BUTTON_CREATE;
      }
  };

  clickButton() {
    switch(this.data.action) {

      case Action.Create: this.createUser();
      break;

      case Action.Update : this.updateUser();
      break;

      case Action.ChangePassword: this.changePassword();
      break;
    }
    //this.buttonName == BUTTON_CREATE ? this.createUser() : this.updateUser();
  };

  createUser() {

    const newUserDto = UserDtoFactory.createUserDto(
        this.userForm.value.username,
        this.userForm.value.password,
        this.userForm.value.departmentId,
        this.userForm.value.role
      );
  
    this._userService.createUser(newUserDto).subscribe({
      next: response => {
        this.apiResponse = response;
        this.clearForm();
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    });
  };

  updateUser() {

    const userDto = UserDtoFactory.createUpdateUserDto(
      this.userForm.value.username,
      this.userForm.value.departmentId,
      this.userForm.value.role
    );
  
    this._userService.updateUser(userDto, this.data.userId).subscribe({
      next: response => {
        this.apiResponse = response;
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    });
  };

  changePasswordForm() {

    this.userForm = new FormGroup({
          password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
          confirmPassword: new FormControl(null),
          },
          {
            validators: matchPassword
          }
    );

    this.data.action = Action.ChangePassword;
  };

  changePassword() {
    
    const newPassword = new ChangeUserPasswordDto(this.userForm.value.password);

    this._userService.changeUserPassword(newPassword, this.data.userId).subscribe({
      next: response => {
        this.apiResponse = response;
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    });
  }

  deleteResponseMessage() {
    setTimeout(() => {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }, 3000);
  };

  clearForm() {
    this.userForm.reset();
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get username() {
    return this.userForm.get('username')
  };

  get password() {
    return this.userForm.get('password')
  };

  get confirmPassword() {
    return this.userForm.get('confirmPassword')
  };
}
