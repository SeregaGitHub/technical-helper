import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Department } from '../../model/department/department';

@Component({
  selector: 'app-view-department-form',
  imports: [],
  templateUrl: './view-department-form.component.html',
  styleUrl: './view-department-form.component.css'
})
export class ViewDepartmentFormComponent implements OnInit {

  department!: Department

  constructor(private _dialogRef: MatDialogRef<ViewDepartmentFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit(): void {
    console.log('ViewDepartmentFormComponent')
    console.log(this.data.departmentId)
    console.log(this.data.departmentName)
    console.log(this.data.createdBy)
    console.log(this.data.createdDate)
    console.log(this.data.lastUpdatedBy)
    console.log(this.data.lastUpdatedDate)
  };

  closeDialog() {
    this._dialogRef.close();
  };
}
