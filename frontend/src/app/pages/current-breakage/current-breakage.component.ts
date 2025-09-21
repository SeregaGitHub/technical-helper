import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
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
import { DATE_FORMAT, DEADLINE_DATE_FORMAT } from '../../util/constant';
import { MatIconModule } from '@angular/material/icon';
import { UpdateBreakagePriorityDto } from '../../model/breakage/update-breakage-priority-dto';
import { UpdateBreakageStatusDto } from '../../model/breakage/update-breakage-status-dto';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmFormComponent } from '../../components/confirm-form/confirm-form.component';
import { BreakageExecutor } from '../../model/user/breakage-executor';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-current-breakage',
  imports: [
    UserProfileDirective,
    CommonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    MatIconModule,
    MatDatepickerModule
  ],
  templateUrl: './current-breakage.component.html',
  styleUrl: './current-breakage.component.css',
  providers: [
    provideNativeDateAdapter(),
    DatePipe
  ],
  //changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CurrentBreakageComponent implements OnInit {

  statusMap = EnumViewFactory.getStatusViews();
  priorityMap = EnumViewFactory.getPriorityViews();

  currentBreakage: any;
  dateFormat = DATE_FORMAT;
  deadlineDateFormat = DEADLINE_DATE_FORMAT;
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
  deadline!: string | null;
  executors!: BreakageExecutor[];
  apiResponse: ApiResponse;

  private readonly _currentYear = new Date().getFullYear();
  private readonly _currentMonth = new Date().getMonth();
  private readonly _currentDay = new Date().getDate();
  readonly minDate = new Date(this._currentYear, this._currentMonth, this._currentDay);
  readonly maxDate = new Date(this._currentYear + 1, this._currentMonth, this._currentDay);
  
  //selectedDate = '2025-10-29';

  private readonly _adapter = inject<DateAdapter<unknown, unknown>>(DateAdapter);
  private readonly _locale = signal(inject<unknown>(MAT_DATE_LOCALE));

  constructor(private _location: Location, 
              private _activatedRoute: ActivatedRoute, 
              private _breakageService: BreakageService,
              private _datePipe: DatePipe,
              public dialog: MatDialog) {
    this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    this.executors = new Array;

    this._locale.set('ru');
    this._adapter.setLocale(this._locale());
  }
  
  ngOnInit(): void {

    this.sub = this._activatedRoute.params.subscribe(params => {
      this.breakageId = params['id'];
      
      this.currentBreakage = this._breakageService
        .getBreakageById(this.breakageId)
          .subscribe({
            next: data => {
              this.currentBreakage = data;

              console.log(this.currentBreakage)

              this.priority = this.currentBreakage.priority;
              this.status = this.currentBreakage.status;
              this.bufferStatus = this.status;
              this.employeeStatuses = EnumViewFactory.getEmployeeStatuses(this.status);
              this.breakageExecutorId = this.currentBreakage.breakageExecutorId;
              this.breakageExecutor = this.currentBreakage.breakageExecutor;
              this.executorAppointedBy = this.currentBreakage.executorAppointedBy;
              //this.deadline = this.currentBreakage.deadline
              this.deadline = this._datePipe.transform(this.currentBreakage.deadline, 'yyyy-MM-dd');
              console.log(this.deadline)

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
        this.deleteResponseMessage();
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

  cancelBreakage(status: Status) {
    if (status === Status.Cancelled) {

        const openDialog = this.dialog.open(ConfirmFormComponent, {data: { 
              status: this.statusMap.get(status) 
          }});

          openDialog.afterClosed().subscribe(
            (confirmResult ) => {
              if (confirmResult) {
                this.cancelBreakageToBackend(this.breakageId, this.currentBreakage.departmentId);
              } else {
                this.status = this.bufferStatus;
              };
          });
    }
  }

  cancelBreakageToBackend(id: string, departmentId: string) {
      this._breakageService.cancelBreakage(id, departmentId)
        .subscribe({
          next: response => {
            this.status = Status.Cancelled;
            this.apiResponse = response;
            this.deleteResponseMessage();
            this.bufferStatus = this.status;
            this.currentBreakage.lastUpdatedBy = response.data;
            this.currentBreakage.lastUpdatedDate = response.timestamp;
          },
          error: err => {
            this.apiResponse = err.error;
            this.deleteResponseMessage();
          }
        });
  }

  setStatusToBackend(status: Status) {
      const updateBreakageStatusDto = new UpdateBreakageStatusDto(status);

      this._breakageService.setStatus(this.breakageId, updateBreakageStatusDto)
        .subscribe({
          next: response => {
            this.status = status;
            this.apiResponse = response;
            this.deleteResponseMessage();
            this.bufferStatus = this.status;
            this.currentBreakage.lastUpdatedBy = response.data;
            this.currentBreakage.lastUpdatedDate = response.timestamp;
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
            this.deleteResponseMessage();
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

  onDateChange(event: MatDatepickerInputEvent<Date>) {
    this.deadline = this._datePipe.transform(event.value, 'yyyy-MM-dd');
    console.log(this.deadline)
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
