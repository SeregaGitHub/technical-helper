import { Component, Inject, OnInit } from '@angular/core';
import { DepartmentDto } from '../../model/departmentDto';
import { DepartmentService } from '../../services/department.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { ApiResponse } from '../../model/apiResponse';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Action } from '../../enum/action';

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
export class DepartmentFormComponent implements OnInit {

  departmentDto: DepartmentDto = {
    name: ''
  }

  departmentForm: any;
  buttonName!: string;

  apiResponse: ApiResponse = {
    message: '',
    status: 0,
    httpStatus: '',
    timestamp: new Date
  }

  constructor(private _depService: DepartmentService, 
              private _dialogRef: MatDialogRef<DepartmentFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
      this.departmentForm = new FormGroup({
        name: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
      });
    }

  ngOnInit(): void {

    if (this.data.action === Action.Update) {
      this.departmentDto = {
        name: this.data.departmentName
      }

      this.buttonName = 'Изменить';
      this.departmentForm.get('name').setValue(this.departmentDto.name);
      
      } else {
        this.buttonName = 'Создать';
      }
  }

  clickButton() {
    this.buttonName == 'Создать' ? this.createDepartment() : this.updateDepartment();
  };

  createDepartment() {

    this.departmentDto = {
        name: this.departmentForm.value.name
    };

    this._depService.createDep(this.departmentDto).subscribe({
      next: response => {
        this.apiResponse = response;
        this.departmentDto = {
          name: ''
        }
        this.clearForm();
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    })
  };

  updateDepartment() {

    this.departmentDto = {
        name: this.departmentForm.value.name
    };

    this._depService.updateDep(this.departmentDto, this.data.departmentId).subscribe({
      next: response => {
        this.apiResponse = response;
        this.departmentDto = {
          name: ''
        }
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
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
    this.apiResponse = {
        message: '',
        status: 0,
        httpStatus: '',
        timestamp: new Date
      };
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get name() {
    return this.departmentForm.get('name')
  };
}
