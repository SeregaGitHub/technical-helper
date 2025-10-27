import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Department } from '../../model/department/department';
import { DATE_FORMAT } from '../../util/constant';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-view-department-form',
  imports: [
    CommonModule
  ],
  templateUrl: './view-department-form.component.html',
  styleUrl: './view-department-form.component.css'
})
export class ViewDepartmentFormComponent {

  department!: Department
  dateFormat = DATE_FORMAT;

  constructor(private _dialogRef: MatDialogRef<ViewDepartmentFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {}

  closeDialog() {
    this._dialogRef.close();
  };
}
