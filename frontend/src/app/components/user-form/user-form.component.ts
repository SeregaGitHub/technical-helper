import { Component, Inject, OnInit } from '@angular/core';
import { ApiResponse } from '../../model/response/api-response';
import { UpdateUserDto } from '../../model/user/update-user-dto';
import { Action } from '../../enum/action';
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
  //userDto: UpdateUserDto;
  deps!: Department[];

  constructor(private _userService: UserService,
              private _depService: DepartmentService, 
              private _dialogRef: MatDialogRef<UserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
      this.userForm = new FormGroup({
        username: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
        password: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
        confirmPassword: new FormControl(null),
        departmentId: new FormControl("", [Validators.required]),
        role: new FormControl("", [Validators.required])
      },
      {
        validators: matchPassword
      });
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
      //this.userDto = UserDtoFactory.createEmptyUserDto();
      this._depService.getAllDep().subscribe({
        next: data => {
          this.deps = data;
        }
      });
    }

  ngOnInit(): void {
    
    if (this.data.action === Action.Update) {
      //let userDto = UserDtoFactory.createUpdateUserDto(this.data.username, this.data.department, this.data.role);
      // this.userDto = UserDtoFactory.createUpdateUserDto(this.data.username, this.data.department, this.data.role);
      // this.departmentDto = {
      //   name: this.data.departmentName
      // }
    
      this.buttonName = 'Изменить';
      // ???????????????????????????? in dep to ???????????????????????????????????
      // this.userForm.get('username').setValue(userDto.username);
      // this.userForm.get('department').setValue(userDto.department);
      // this.userForm.get('role').setValue(userDto.role);
      this.userForm.get('username').setValue(this.data.username);
      this.userForm.get('departmentId').setValue(this.data.departmentId);
      this.userForm.get('role').setValue(this.data.role);
      // ???????????????????????????? in dep to ???????????????????????????????????
          
      } else {
        this.buttonName = 'Создать';
      }
  };

  clickButton() {
    this.buttonName == 'Создать' ? this.createUser() : this.updateUser();
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
        //this.departmentDto = DepartmentDtoFactory.createEmptyDepartmentDto();
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
  
      // this.departmentDto = {
      //     name: this.departmentForm.value.name
      // };
  
    this._userService.updateUser(userDto, this.data.userId).subscribe({
      next: response => {
        this.apiResponse = response;
        //this.userDto = UserDtoFactory.createEmptyUserDto();
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    });
  };

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
