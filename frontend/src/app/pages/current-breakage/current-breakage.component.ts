import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Location} from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BreakageService } from '../../services/breakage.service';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { BreakageFullDto } from '../../model/breakage/breakage-full-dto';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { Priority } from '../../enum/priority.enum';
import { EnumViewFactory } from '../../generator/enum-view-factory';
import { UserProfileDirective } from '../../directive/user-profile.directive';
import { Status } from '../../enum/status.enum';
import { DATE_FORMAT } from '../../util/constant';

@Component({
  selector: 'app-current-breakage',
  imports: [
    UserProfileDirective,
    CommonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule
  ],
  templateUrl: './current-breakage.component.html',
  styleUrl: './current-breakage.component.css'
})
export class CurrentBreakageComponent implements OnInit {

  currentBreakage: any;
  getBreakageError: ApiResponse;
  dateFormat = DATE_FORMAT;
  sub: any;
  breakageId: string = '';
  priority!: Priority;
  priorities = EnumViewFactory.getPriorities();
  status!: Status;
  statuses = EnumViewFactory.getStatuses();
  employeeStatuses: any;

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
              this.priority = this.currentBreakage.priority;
              this.status = this.currentBreakage.status;
              this.employeeStatuses = EnumViewFactory.getEmployeeStatuses(this.status);
            },
            error: err => {
              this.getBreakageError = err;
            }
          });
    })

  }

  setPriority(priority: Priority) {
    this.priority = priority;
  }

  setStatus(status: Status) {
    this.status = status;
  }

  backClicked() {
    this._location.back();
  }
}
