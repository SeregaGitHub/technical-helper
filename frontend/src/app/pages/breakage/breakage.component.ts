import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';
import { Breakage } from '../../model/breakage/breakage';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSelectModule } from '@angular/material/select';
import { Executor } from '../../enum/executor.enum';
import { MatDialog } from '@angular/material/dialog';
import { BreakageFormComponent } from '../../components/breakage-form/breakage-form.component';
import { EnumViewFactory } from '../../generator/enum-view-factory';
import { CustomBreakagePaginatorIntl } from '../../util/custom-breakage-paginator-intl';
import { UserProfileDirective } from '../../directive/user-profile.directive';
import { Router } from '@angular/router';
import { LocalStorageUtil } from '../../util/local-storage-util';

@Component({
  selector: 'app-breakage',
  imports: [
    MatSidenavModule,
    MatIconModule,
    FormsModule,
    CommonModule,
    MatTableModule, 
    MatPaginatorModule,
    MatSort, 
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatSlideToggleModule,
    MatSelectModule,
    UserProfileDirective
  ],
  templateUrl: './breakage.component.html',
  styleUrl: './breakage.component.css',
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomBreakagePaginatorIntl }
  ]
})
export class BreakageComponent implements OnInit, OnDestroy{

  opened = false;

  executors = EnumViewFactory.getExecutorViews();
  statusMap = EnumViewFactory.getStatusViews();
  priorityMap = EnumViewFactory.getPriorityViews();

  number = '№';
  departmentName = 'Отдел';
  room = 'Помещение';
  breakageTopic = 'Тема';
  status = 'Статус';
  priority = 'Приоритет';
  breakageExecutor = 'Исполнитель';
  createdBy = 'Создана';
  createdDate = 'Создано';

  pageIndex: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  pageEvent!: PageEvent;

  sortBy = 'createdDate';
  direction = 'DESC';

  statusNew = true;
  statusSolved = false;
  statusInProgress = true; 
  statusPaused = true; 
  statusRedirected = true; 
  statusCancelled = false;

  priorityUrgently = true; 
  priorityHigh = true; 
  priorityMedium = true; 
  priorityLow = true;

  executor = Executor.All; 
  deadline = false;
  now!: Date | null;

  searchText = '';

  dateFormat = DATE_FORMAT;
  breakages: any;
  getAllBreakagesError: ApiResponse;

  searchTimer: any;
  inputTimer: any;
  inputError: boolean = false;

  intervalId: any;

  public displayedColumns!: string[];
  public dataSource!: MatTableDataSource<Breakage>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _breakageService: BreakageService, public dialog: MatDialog, private _router: Router) { 
    this.getAllBreakagesError = ApiResponseFactory.createEmptyApiResponse();
   }

  ngOnInit(): void {

    if (localStorage.getItem('thOpened') != null) {
      this.opened = LocalStorageUtil.stringToBoolean(localStorage.getItem('thOpened'));

      this.pageIndex = LocalStorageUtil.stringToNumber(localStorage.getItem('thPageIndex'));
      this.pageSize = LocalStorageUtil.stringToNumber(localStorage.getItem('thpageSize'));

      this.sortBy = LocalStorageUtil.toParamString(localStorage.getItem('thSortBy'));
      this.direction = LocalStorageUtil.toParamString(localStorage.getItem('thDirection')); 

      this.statusNew = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusNew'));
      this.statusSolved = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusSolved'));
      this.statusInProgress = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusInProgress'));
      this.statusPaused = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusPaused'));
      this.statusRedirected = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusRedirected'));
      this.statusCancelled = LocalStorageUtil.stringToBoolean(localStorage.getItem('thStatusCancelled'));

      this.priorityUrgently = LocalStorageUtil.stringToBoolean(localStorage.getItem('thPriorityUrgently'));
      this.priorityHigh = LocalStorageUtil.stringToBoolean(localStorage.getItem('thPriorityHigh'));
      this.priorityMedium = LocalStorageUtil.stringToBoolean(localStorage.getItem('thPriorityMedium'));
      this.priorityLow = LocalStorageUtil.stringToBoolean(localStorage.getItem('thPriorityLow'));

      this.executor = LocalStorageUtil.stringToExecutor(localStorage.getItem('thExecutor'));
      this.deadline = LocalStorageUtil.stringToBoolean(localStorage.getItem('thDeadline'));

      this.searchText = LocalStorageUtil.toParamString(localStorage.getItem('thSearchText'));
    }

    this.getAllBreakages();

    this.intervalId = setInterval(() => {
      this.getAllBreakages();
    }, 30000);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalId);
    
    localStorage.setItem('thOpened', LocalStorageUtil.booleanToString(this.opened));

    localStorage.setItem('thPageIndex', LocalStorageUtil.numberToString(this.pageIndex));
    localStorage.setItem('thpageSize', LocalStorageUtil.numberToString(this.pageSize));

    localStorage.setItem('thSortBy', this.sortBy);
    localStorage.setItem('thDirection', this.direction);
    
    localStorage.setItem('thStatusNew', LocalStorageUtil.booleanToString(this.statusNew));
    localStorage.setItem('thStatusSolved', LocalStorageUtil.booleanToString(this.statusSolved));
    localStorage.setItem('thStatusInProgress', LocalStorageUtil.booleanToString(this.statusInProgress));
    localStorage.setItem('thStatusPaused', LocalStorageUtil.booleanToString(this.statusPaused));
    localStorage.setItem('thStatusRedirected', LocalStorageUtil.booleanToString(this.statusRedirected));
    localStorage.setItem('thStatusCancelled', LocalStorageUtil.booleanToString(this.statusCancelled));

    localStorage.setItem('thPriorityUrgently', LocalStorageUtil.booleanToString(this.priorityUrgently));
    localStorage.setItem('thPriorityHigh', LocalStorageUtil.booleanToString(this.priorityHigh));
    localStorage.setItem('thPriorityMedium', LocalStorageUtil.booleanToString(this.priorityMedium));
    localStorage.setItem('thPriorityLow', LocalStorageUtil.booleanToString(this.priorityLow));

    localStorage.setItem('thExecutor', this.executor.toString());
    localStorage.setItem('thDeadline', LocalStorageUtil.booleanToString(this.deadline));

    localStorage.setItem('thSearchText', this.searchText);
  }

  applyFilter(event: Event) {

    clearTimeout(this.searchTimer);
    clearTimeout(this.inputTimer);
    this.inputError = false;
    this.searchText = '';

    if (event.type === 'keyup') {
      const value = (event.target as HTMLInputElement).value;
      const filterValue = value.trim().toLowerCase();
      this.searchText = filterValue;

      if (this.searchText.length > 2) {
          this.searchTimer = setTimeout(() => {
            this.getAllBreakages();
          }, 2000);
      } else if (this.searchText.length > 0) {
          this.inputTimer = setTimeout(() => {
              this.inputError = true;
          }, 2000);
      } else {
          clearTimeout(this.searchTimer);
          clearTimeout(this.inputTimer);
          this.inputError = false;
          this.searchText = '';
          this.getAllBreakages();
      }
    }
  }

  sortData(event: Sort) {
    this.sortBy = event.active;
    this.direction = event.direction;

    this.getAllBreakages();
  }

  onPaginateChange(event: PageEvent) {

    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.totalElements = event.length;

    this.getAllBreakages();
  }

  createBreakage() {
    
    const openDialog = this.dialog.open(BreakageFormComponent);
    
    openDialog.afterClosed().subscribe(() => {
        this.getAllBreakages();
    });
  };


  getAllBreakages(): void {

    if (this.direction === '') {
      this.sortBy = 'createdDate';
    };

    if (this.sortBy === 'departmentName') {
      this.sortBy = 'department'
    };

    this._breakageService
      .getAllBreakages(
        this.pageIndex, this.pageSize, this.sortBy, this.direction.toUpperCase(), 
        this.statusNew, this.statusSolved, this.statusInProgress, 
        this.statusPaused, this.statusRedirected, this.statusCancelled,
        this.priorityUrgently, this.priorityHigh, this.priorityMedium, this.priorityLow,
        this.executor, this.deadline, this.searchText
      )
        .subscribe({
          next: data => {

            let displayedColumns: string[] = [
                                    'number', 
                                    'departmentName', 
                                    'room', 
                                    'breakageTopic',
                                    'status', 
                                    'priority', 
                                    'breakageExecutor', 
                                    'createdBy',
                                    'createdDate'
                                  ];

            if (data.isForEmployee) {
              displayedColumns.splice(5, 1);
            }

            this.displayedColumns = displayedColumns;
            this.breakages = data.content;
            this.stringToDate(this.breakages);
            this.dataSource = new MatTableDataSource(this.breakages);
            this.totalElements = data.totalElements;

            if (data.now != null) {
              const [nowYear, nowMonth, nowDay] = data.now.substring(0, 10).split('-');
              this.now = new Date(+nowYear, +nowMonth - 1, +nowDay, 23, 59, 59);
            } else {
              this.now = null;
            }
          },
          error: err => {
            this.getAllBreakagesError = err.error;
            setTimeout(() => {
              this.getAllBreakagesError = ApiResponseFactory.createEmptyApiResponse();
            }, 3000);
          }
        })
  };

  getBreakageById(id: string, isEmpl: boolean) {
    this._router.navigate(["/breakage/" + id], {
      queryParams: { isEmployee: isEmpl ? 'true' : 'false' }
    });
  };

  toDefaultPageIndex() {
    this.pageIndex = 0;
  }

  private stringToDate(dataContent: any) {
    dataContent.forEach((breakage: any) => { 
      breakage.deadline = this.convertToDate(breakage.deadline) }); 
  }

  private convertToDate(str: string): Date | null {
    if (str != null) {
      const [month, day, year] = str.substring(0, 10).split('-');
      return new Date(+year, +month - 1, +day, 23, 59, 59);
    } else {
      return null;
    }
  }
}
