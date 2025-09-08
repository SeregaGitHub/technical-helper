import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Location} from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BreakageService } from '../../services/breakage.service';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';

@Component({
  selector: 'app-current-breakage',
  imports: [
    CommonModule
  ],
  templateUrl: './current-breakage.component.html',
  styleUrl: './current-breakage.component.css'
})
export class CurrentBreakageComponent implements OnInit {

  currentBreakage!: any;
  getBreakageError: ApiResponse;
  sub: any;
  breakageId: string = '';

  constructor(private _location: Location, private _activatedRoute: ActivatedRoute, private _breakageService: BreakageService) {
    this.getBreakageError = ApiResponseFactory.createEmptyApiResponse();
  }
  
  ngOnInit(): void {

    this.sub = this._activatedRoute.params.subscribe(params => {
      this.breakageId = params['id'];
      
      this.currentBreakage = this._breakageService
        .getBreakageById(this.breakageId)
          .subscribe({
            next: data => {
              this.currentBreakage = data;
            },
            error: err => {
              this.getBreakageError = err;
            }
          });
    })

  }

  backClicked() {
    this._location.back();
  }
}
