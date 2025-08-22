import { Component, Inject, OnInit } from '@angular/core';
import { User } from '../../model/user/user';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DATE_FORMAT } from '../../util/constant';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-view-user-form',
  imports: [
    CommonModule
  ],
  templateUrl: './view-user-form.component.html',
  styleUrl: './view-user-form.component.css'
})
export class ViewUserFormComponent {

  user!: User
  dateFormat = DATE_FORMAT;

  constructor(private _dialogRef: MatDialogRef<ViewUserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {}

  closeDialog() {
    this._dialogRef.close();
  };
}