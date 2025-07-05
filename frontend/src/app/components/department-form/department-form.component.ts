import { Component, Inject, OnInit } from '@angular/core';
import { DepartmentDto } from '../../model/department/department-dto';
import { DepartmentService } from '../../services/department.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { ApiResponse } from '../../model/response/api-response';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Action } from '../../enum/action.enum';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { BUTTON_CREATE, BUTTON_UPDATE } from '../../util/constant';

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

  departmentForm: any;
  buttonName!: string;
  apiResponse: ApiResponse;

  constructor(private _depService: DepartmentService, 
              private _dialogRef: MatDialogRef<DepartmentFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }

  ngOnInit(): void {

    if (this.data.action === Action.Update) {

      this.departmentForm = new FormGroup({
        name: new FormControl(this.data.departmentName, [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
      });

      this.buttonName = BUTTON_UPDATE;
      
      } else {

        this.departmentForm = new FormGroup({
        name: new FormControl("", [Validators.required, Validators.minLength(4), Validators.maxLength(64)]),
      });

        this.buttonName = BUTTON_CREATE;
      }
  }

  clickButton() {
    this.buttonName == BUTTON_CREATE ? this.createDepartment() : this.updateDepartment();
  };

  createDepartment() {

    const departmentDto = new DepartmentDto(this.departmentForm.value.name);

    this._depService.createDep(departmentDto).subscribe({
      next: response => {
        this.apiResponse = response;
        this.clearForm();
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    })
  };

  updateDepartment() {

    const departmentDto = new DepartmentDto(this.departmentForm.value.name);

    this._depService.updateDep(departmentDto, this.data.departmentId).subscribe({
      next: response => {
        this.apiResponse = response;
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
    this.departmentForm.reset();
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get name() {
    return this.departmentForm.get('name')
  };
}
