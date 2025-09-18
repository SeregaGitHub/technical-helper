import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BreakageService } from '../../services/breakage.service';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { Priority } from '../../enum/priority.enum';
import { EnumViewFactory } from '../../generator/enum-view-factory';
import { UserProfileDirective } from '../../directive/user-profile.directive';
import { Status } from '../../enum/status.enum';
import { DATE_FORMAT } from '../../util/constant';
import { MatIconModule } from '@angular/material/icon';
import { UpdateBreakagePriorityDto } from '../../model/breakage/update-breakage-priority-dto';
import { UpdateBreakageStatusDto } from '../../model/breakage/update-breakage-status-dto';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmFormComponent } from '../../components/confirm-form/confirm-form.component';
import { BreakageExecutor } from '../../model/user/breakage-executor';

@Component({
  selector: 'app-current-breakage',
  imports: [
    UserProfileDirective,
    CommonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    MatIconModule
  ],
  templateUrl: './current-breakage.component.html',
  styleUrl: './current-breakage.component.css'
})
export class CurrentBreakageComponent implements OnInit {

  statusMap = EnumViewFactory.getStatusViews();
  priorityMap = EnumViewFactory.getPriorityViews();

  currentBreakage: any;
  dateFormat = DATE_FORMAT;
  sub: any;
  breakageId: string = '';
  priority!: Priority;
  priorities = EnumViewFactory.getPriorities();
  status!: Status;
  bufferStatus!: Status;
  statuses = EnumViewFactory.getStatuses();
  employeeStatuses: any;
  breakageExecutorId!: string;
  breakageExecutor!: string;
  executorAppointedBy!: string;
  executors!: BreakageExecutor[];
  apiResponse: ApiResponse;

  constructor(private _location: Location, 
              private _activatedRoute: ActivatedRoute, 
              private _breakageService: BreakageService,
              public dialog: MatDialog) {
    this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    this.executors = new Array;
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
              this.bufferStatus = this.status;
              this.employeeStatuses = EnumViewFactory.getEmployeeStatuses(this.status);
              this.breakageExecutorId = this.currentBreakage.breakageExecutorId;
              this.breakageExecutor = this.currentBreakage.breakageExecutor;
              this.executorAppointedBy = this.currentBreakage.executorAppointedBy;

              console.log(this.currentBreakage);  // NEED FOR DELETE !!!
              this.executors.push(new BreakageExecutor(this.breakageExecutorId, this.breakageExecutor));
              
              
              if (this.status != Status.New) {
                this.statuses.splice(0, 1);
              }
            },
            error: err => {
              this.apiResponse = err;
            }
          });
    })
  };

  setPriority(priority: Priority) {
    const updateBreakagePriorityDto = new UpdateBreakagePriorityDto(priority, this.status);

    this._breakageService.setPriority(this.breakageId, updateBreakagePriorityDto).subscribe({
      next: response => {
        this.priority = priority;
        this.apiResponse = response;
        this.currentBreakage.lastUpdatedBy = response.data;
        this.currentBreakage.lastUpdatedDate = response.timestamp;
        this.deleteResponseMessage();
      },
      error: err => {
        this.apiResponse = err.error;
      }
    });
  }

  setStatus(status: Status) {
    
    if (status != Status.New && status != this.bufferStatus) {

      if (status === Status.Solved || status === Status.Cancelled) {
          const openDialog = this.dialog.open(ConfirmFormComponent, {data: { 
              status: this.statusMap.get(status) 
          }});

          openDialog.afterClosed().subscribe(
            (confirmResult ) => {
              if (confirmResult) {
                this.setStatusToBackend(status);
              } else {
                this.status = this.bufferStatus;
              };
          });
      } else {
          this.setStatusToBackend(status);
          if (this.bufferStatus === Status.New) {
            this.statuses.splice(0, 1);
          }
        }
    }
  }

  setStatusToBackend(status: Status) {
      this.status = status;
      const updateBreakageStatusDto = new UpdateBreakageStatusDto(status);

      this._breakageService.setStatus(this.breakageId, updateBreakageStatusDto)
        .subscribe({
          next: response => {
            this.apiResponse = response;
            this.deleteResponseMessage();
            this.bufferStatus = this.status;
            if (status === Status.Paused || status === Status.Redirected) {
              this.breakageExecutorId = '';
              this.breakageExecutor = 'Не назначен';
              this.executorAppointedBy = 'Отсутствует';
              this.executors.splice(0);
              this.executors.push(new BreakageExecutor('', 'Не назначен'));
            }
          },
          error: err => {
            this.apiResponse = err.error;
          }
        });
  }

  // IN WRITE PROGRESS
  clickExecutor(): void {
    console.log('clickExecutor()');
    
    this._breakageService.getAdminAndTechnicianList()
      .subscribe({
        next: data => {
          if (this.executors.length === 1 && this.breakageExecutorId === '') {
            this.executors = [...this.executors, ...data];
          } else {
            this.executors.splice(0);
            this.executors.push(new BreakageExecutor('', 'Не назначен'));
            this.executors = [...this.executors, ...data];
          }
          console.log(this.executors);
        },
        error: err => {
          this.apiResponse = err;
            setTimeout(() => {
              this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
            }, 3000);
          }
      })
  }

  setExecutor(id: string, username: string) {
    console.log('setExecutor(): ');
    const executor = new BreakageExecutor(id, username);
    console.log(executor);
    this.breakageExecutorId = executor.id;
    this.breakageExecutor = executor.username;
    //this.executorAppointedBy = 

    if (this.breakageExecutorId != '') {
      console.log('ready for send request to backend');
    } else {
      console.log('resetExecutor');
    }

  }
  // IN WRITE PROGRESS

  deleteResponseMessage() {
    setTimeout(() => {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }, 3000);
  };

  backClicked() {
    this._location.back();
  }
}
