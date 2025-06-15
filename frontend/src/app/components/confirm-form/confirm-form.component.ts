import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-form',
  imports: [],
  templateUrl: './confirm-form.component.html',
  styleUrl: './confirm-form.component.css'
})
export class ConfirmFormComponent {

  constructor(private _dialogRef: MatDialogRef<ConfirmFormComponent>) {}

  onCancelClick(): void {
    this._dialogRef.close(false);
  }

  onConfirmClick(): void {
    this._dialogRef.close(true);
  }

}
