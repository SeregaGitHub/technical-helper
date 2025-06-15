import { Component, Inject } from '@angular/core';
import { DepartmentDto } from '../../model/departmentDto';
import { DepartmentService } from '../../services/department.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { ApiResponse } from '../../model/apiResponse';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-department-form',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule, 
    MatInputModule,
    ReactiveFormsModule
  ],
  templateUrl: './department-form.component.html',
  styleUrl: './department-form.component.css'
})
export class DepartmentFormComponent {

  departmentForm: any;

  apiResponse: ApiResponse = {
    message: '',
    status: 0,
    httpStatus: '',
    timestamp: new Date
  }

  newDepartment?: DepartmentDto;

  

  constructor(private _depService: DepartmentService, 
              private _dialogRef: MatDialogRef<DepartmentFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
      this.departmentForm = new FormGroup({
      name: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
    });
  }

  createDepartment() {

    console.log(this.data);

    this.newDepartment = {
        name: this.departmentForm.value.name
    };

    this._depService.createDep(this.newDepartment).subscribe(response => {
        this.apiResponse = response;
        this.newDepartment = undefined;
        this.clearForm();
        this.deleteResponseMessage();
      });
  };

  deleteResponseMessage() {
    setTimeout(() => {
      this.apiResponse = {
        message: '',
        status: 0,
        httpStatus: '',
        timestamp: new Date
      }
    }, 3000);
  };

  clearForm() {
    this.departmentForm.reset();
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get name() {
    return this.departmentForm.get('name')
  };
}
