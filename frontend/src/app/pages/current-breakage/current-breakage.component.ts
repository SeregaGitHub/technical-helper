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
import { AppointBreakageExecutorDto } from '../../model/breakage/appoint-breakage-executor-dto';
import { BreakageComment } from '../../model/breakage/breakage-comment';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { BreakageCommentFormComponent } from '../../components/breakage-comment-form/breakage-comment-form.component';

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
    MatDatepickerModule,
    MatTableModule
  ],
  templateUrl: './current-breakage.component.html',
  styleUrl: './current-breakage.component.css',
  providers: [
    provideNativeDateAdapter(),
    DatePipe
  ],
})
export class CurrentBreakageComponent implements OnInit {

  statusMap = EnumViewFactory.getStatusViews();
  priorityMap = EnumViewFactory.getPriorityViews();

  sub: any;
  emplSub: any;
  isEmployee!: boolean;

  currentBreakage: any;
  dateFormat = DATE_FORMAT;
  deadlineDateFormat = DEADLINE_DATE_FORMAT;
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
  now!: string | null;
  isOverdue: boolean = false;
  executors!: BreakageExecutor[];
  apiResponse: ApiResponse;

  private readonly _currentYear = new Date().getFullYear();
  private readonly _currentMonth = new Date().getMonth();
  private readonly _currentDay = new Date().getDate();
  readonly minDate = new Date(this._currentYear, this._currentMonth, this._currentDay);
  readonly maxDate = new Date(this._currentYear + 1, this._currentMonth, this._currentDay);
  comments: any;
  commentsCount!: number;
  readyForAppoint: boolean = false;

  private readonly _adapter = inject<DateAdapter<unknown, unknown>>(DateAdapter);
  private readonly _locale = signal(inject<unknown>(MAT_DATE_LOCALE));

  number = '№';
  comment = 'Комментарий';
  creatorName = 'Создан';
  createdDate = 'Создано';
  lastUpdatedDate = 'Обновлено';
  actions = 'Действия';

  public displayedColumns: string[] = [ 'number', 'comment', 'creatorName', 'createdDate', 'lastUpdatedDate', 'actions' ];
  public dataSource!: MatTableDataSource<BreakageComment>;

  constructor(private _location: Location, 
              private _activatedRoute: ActivatedRoute, 
              private _breakageService: BreakageService,
              private _datePipe: DatePipe,
              public dialog: MatDialog) {
    this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    this.executors = new Array;

    this._locale.set('ru');
    this._adapter.setLocale(this._locale());

    this.sub = this._activatedRoute.params.subscribe(params => {
      this.breakageId = params['id'];
    });

    this.emplSub = this._activatedRoute.queryParams.subscribe(queryParams => {
      this.isEmployee = JSON.parse(queryParams['isEmployee'].toLowerCase());
    });
  }
  
  ngOnInit(): void {
    this.getCurrentBreakage();
  };

  getCurrentBreakage() {
    if (this.isEmployee) {

      this.currentBreakage = this._breakageService
        .getBreakageEmployeeById(this.breakageId)
          .subscribe({
            next: data => {
              this.currentBreakage = data;
              this.status = this.currentBreakage.status;
              this.bufferStatus = this.status;
              this.breakageExecutor = this.currentBreakage.breakageExecutor;
              
              this.employeeStatuses = EnumViewFactory.getEmployeeStatuses(this.status);
            },
            error: err => {
              this.apiResponse = err;
            }
          });
    } else {

      this.currentBreakage = this._breakageService
        .getBreakageById(this.breakageId)
          .subscribe({
            next: response => {

              console.log(response);  // NEED FOR DELETE

              this.currentBreakage = response.data;
              this.status = this.currentBreakage.status;
              this.bufferStatus = this.status;
              this.breakageExecutor = this.currentBreakage.breakageExecutor;
              
              this.breakageExecutorId = this.currentBreakage.breakageExecutorId;
              this.priority = this.currentBreakage.priority;
              this.executorAppointedBy = this.currentBreakage.executorAppointedBy;
              this.comments = this.currentBreakage.comments;
              this.commentsCount = this.comments.length;
              this.dataSource = new MatTableDataSource(this.comments);
              this.deadline = this._datePipe.transform(this.currentBreakage.deadline, 'yyyy-MM-dd');
              this.now = this._datePipe.transform(response.timestamp, 'yyyy-MM-dd');
              this.executors.push(new BreakageExecutor(this.breakageExecutorId, this.breakageExecutor));
              
              if (this.status != Status.New) {
                this.statuses.splice(0, 1);
              }

              this.isBreakageOverdue();
            },
            error: err => {
              this.apiResponse = err;
            }
          });
    }
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
            this.isOverdue = false;
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
              this.isOverdue = false;
              this.deadline = null;
              this.breakageExecutorId = '';
              this.breakageExecutor = 'Не назначен';
              this.executorAppointedBy = 'Отсутствует';
              this.executors.splice(0);
              this.executors.push(new BreakageExecutor('', 'Не назначен'));
            } else if (status === Status.Solved || status === Status.Cancelled) {
                this.isOverdue = false;
            }
          },
          error: err => {
            this.apiResponse = err.error;
            this.deleteResponseMessage();
          }
        });
  }

  clickExecutor(): void {
    
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
    this.breakageExecutorId = id;
    this.breakageExecutor = username;
    this.setReadyForAppoint();
  }

  onDateChange(event: MatDatepickerInputEvent<Date>) {
    this.deadline = this._datePipe.transform(event.value, 'yyyy-MM-dd');
    this.setReadyForAppoint();
  }

  addBreakageExecutor() {

    if (this.breakageExecutorId != '' && this.deadline != null) {

      const [year, month, day] = this.deadline.split('-');
      const executor = new AppointBreakageExecutorDto(this.breakageExecutorId, new Date(+year, +month - 1, +day, 23, 59, 59), this.status);

      this._breakageService.addBreakageExecutor(this.breakageId, executor)
        .subscribe({
          next: response => {
            this.apiResponse = response;
            this.isOverdue = false;
            this.deleteResponseMessage();
            this.currentBreakage.lastUpdatedBy = response.data;
            this.currentBreakage.lastUpdatedDate = response.timestamp;
          },
          error: err => {
            this.apiResponse = err.error;
            this.deleteResponseMessage();
          }
        })
    }
  }

  dropBreakageExecutor() {

    this._breakageService.dropExecutor(this.breakageId)
      .subscribe({
        next: response => {
          this.dropExecutorVariables();
          this.apiResponse = response;
          this.deleteResponseMessage();
          this.currentBreakage.lastUpdatedBy = response.data;
          this.currentBreakage.lastUpdatedDate = response.timestamp;
        },
        error: err => {
          this.apiResponse = err.error;
          this.deleteResponseMessage();
        }
      });
  }

  private dropExecutorVariables() {
      this.currentBreakage.breakageExecutor = 'Не назначен';
      this.currentBreakage.breakageExecutorId = '';
      this.currentBreakage.executorAppointedBy = 'Отсутствует';
      this.currentBreakage.deadline = null;
      this.breakageExecutor = 'Не назначен';
      this.breakageExecutorId = '';
      this.executorAppointedBy = 'Отсутствует';
      this.deadline = null;
      this.readyForAppoint = false;
      this.isOverdue = false;
  }

  private setReadyForAppoint() {
    if (this.breakageExecutorId != '' && this.deadline != null) {
      this.readyForAppoint = true;
    } else {
      this.readyForAppoint = false;
    }
  };

  private isBreakageOverdue() {
    if (this.breakageExecutorId != '' && 
        this.deadline != null && 
        this.now != null && 
        (this.status === Status.New || this.status === Status.InProgress)) {

      const [nowYear, nowMonth, nowDay] = this.now.substring(0, 10).split('-');
      const now = new Date(+nowYear, +nowMonth - 1, +nowDay, 23, 59, 59);

      const [deadlineyear, deadlineMonth, deadlineDay] = this.deadline.substring(0, 10).split('-');
      const deadline = new Date(+deadlineyear, +deadlineMonth - 1, +deadlineDay, 23, 59, 59);

      if (deadline < now) {
        this.isOverdue = true;
      } else {
        this.isOverdue = false;
      }
    }
  }

  private deleteResponseMessage() {
    setTimeout(() => {
      this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
    }, 3000);
  };

  createComment() {
    console.log('createComment()');
    console.log(this.breakageId);
    console.log(this.status);
        
    const openDialog = this.dialog.open(BreakageCommentFormComponent, {
      data: {
        'breakageId': this.breakageId,
        'status': this.status
      }
    });
        
    openDialog.afterClosed().subscribe(() => {
        this.getCurrentBreakage();
    });
      
  };

  updateComment(id: string, comment: string, actionEnabled: boolean) {
    console.log('updateComment()');
    console.log(id);
    console.log(comment);
    console.log(actionEnabled);
  };

  deleteComment(id: string) {
    console.log('deleteComment()');
    console.log(id);
  };

  backClicked() {
    this._location.back();
  }
}
