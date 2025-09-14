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
  buttonName!: string;

  constructor(private _dialogRef: MatDialogRef<ConfirmFormComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit(): void {
    if (this.data.name != null) {
        this.buttonName = 'Удалить';
        this.content = 'Вы подтверждаете удаление: ' + this.data.name + ' ?';
    } else {
        this.buttonName = 'Изменить';
        this.content = 'Вы подтверждаете изменение статуса заявки на : ' + this.data.status + ' ?';
    }
  }

  onCancelClick(): void {
    this._dialogRef.close(false);
  }

  onConfirmClick(): void {
    this._dialogRef.close(true);
  }
}
