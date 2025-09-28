import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BreakageService } from '../../services/breakage.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Action } from '../../enum/action.enum';
import { BUTTON_CREATE, BUTTON_UPDATE } from '../../util/constant';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { CreateBreakageCommentDto } from '../../model/breakage/create-breakage-comment-dto';

@Component({
  selector: 'app-breakage-comment-form',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule, 
    MatInputModule,
    ReactiveFormsModule
  ],
  templateUrl: './breakage-comment-form.component.html',
  styleUrl: './breakage-comment-form.component.css'
})
export class BreakageCommentFormComponent implements OnInit {

  breakageCommentForm: any;
  buttonName!: string;
  apiResponse: any;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any, 
              private _breakageService: BreakageService,
              private _dialogRef: MatDialogRef<BreakageCommentFormComponent>) {
                this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
              }

  ngOnInit(): void {

    if (this.data.action === Action.Create) {
      this.breakageCommentForm = new FormGroup({
        breakageCommentText: new FormControl("", [Validators.required, Validators.minLength(1)])
      });

      this.buttonName = BUTTON_CREATE;

    } else {
      this.breakageCommentForm = new FormGroup({
        breakageCommentText: new FormControl(this.data.comment, [Validators.required, Validators.minLength(1)])
      });

      this.buttonName = BUTTON_UPDATE;

    }
  };

  clickButton() {
    this.buttonName == BUTTON_CREATE ? this.createBreakageComment() : this.updateBreakageComment();
  };

  createBreakageComment() {

    const createBreakageCommentDto = new CreateBreakageCommentDto(
      this.breakageCommentForm.value.breakageCommentText, this.data.status
    );

    this._breakageService.createBreakageComment(this.data.breakageId, createBreakageCommentDto).subscribe({
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

  updateBreakageComment() {

    const createBreakageCommentDto = new CreateBreakageCommentDto(
      this.breakageCommentForm.value.breakageCommentText, this.data.status
    );

    this._breakageService.updateBreakageComment(this.data.breakageCommentId, createBreakageCommentDto).subscribe({
        next: response => {
          this.apiResponse = response;
          this.deleteResponseMessage();
        },
        error: err => {
          this.apiResponse = err.error;
        }
      });
  }

  deleteResponseMessage() {
      setTimeout(() => {
        this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
      }, 3000);
    };

  clearForm() {
    this.breakageCommentForm.reset();
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get breakageCommentText() {
    return this.breakageCommentForm.get('breakageCommentText')
  };

}
