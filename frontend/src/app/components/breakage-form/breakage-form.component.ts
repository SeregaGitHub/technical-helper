import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiResponse } from '../../model/response/api-response';
import { BreakageService } from '../../services/breakage.service';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { CreateBreakageDto } from '../../model/breakage/create-breakage-dto';
import { BUTTON_CREATE } from '../../util/constant';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-breakage-form',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule, 
    MatInputModule,
    ReactiveFormsModule
  ],
  templateUrl: './breakage-form.component.html',
  styleUrl: './breakage-form.component.css'
})
export class BreakageFormComponent implements OnInit, OnDestroy {

  breakageForm: any;
  buttonName = BUTTON_CREATE;
  apiResponse: ApiResponse;

  private _unsubscribe: Subject<void> = new Subject();

  constructor(private _breakageService: BreakageService,
              private _dialogRef: MatDialogRef<BreakageFormComponent>) {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }

  ngOnInit(): void {

    this.breakageForm = new FormGroup({
        room: new FormControl("", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]),
        breakageTopic: new FormControl("", [Validators.required, Validators.minLength(1), Validators.maxLength(128)]),
        breakageText: new FormControl("", [Validators.required, Validators.minLength(1), Validators.maxLength(2048)])
      });
  };

  ngOnDestroy(): void {
    this._unsubscribe.next();
    this._unsubscribe.complete();
  };

  createBreakage() {
  
    const breakageDto = new CreateBreakageDto(
      this.breakageForm.value.room, this.breakageForm.value.breakageTopic, this.breakageForm.value.breakageText
    );
  
    this._breakageService.createBreakage(breakageDto)
      .pipe(takeUntil(this._unsubscribe))
        .subscribe({
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

  deleteResponseMessage() {
    setTimeout(() => {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }, 3000);
  };

  clearForm() {
    this.breakageForm.reset();
  };

  closeDialog() {
    this._dialogRef.close();
  };

  get room() {
    return this.breakageForm.get('room')
  };

  get breakageTopic() {
    return this.breakageForm.get('breakageTopic')
  };

  get breakageText() {
    return this.breakageForm.get('breakageText')
  };
}
