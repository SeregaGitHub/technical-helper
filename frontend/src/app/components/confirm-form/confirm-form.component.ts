import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-form',
  imports: [],
  templateUrl: './confirm-form.component.html',
  styleUrl: './confirm-form.component.css'
})
export class ConfirmFormComponent implements OnInit {

  content!: string;

  constructor(private _dialogRef: MatDialogRef<ConfirmFormComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit(): void {
    this.content = 'Вы подтверждаете удаление: ' + this.data.departmentName + ' ?';
  }

  onCancelClick(): void {
    this._dialogRef.close(false);
  }

  onConfirmClick(): void {
    this._dialogRef.close(true);
  }
}
