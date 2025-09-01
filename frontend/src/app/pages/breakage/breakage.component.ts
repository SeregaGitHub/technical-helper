import { Component, ElementRef, ViewChild } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';
import { Breakage } from '../../model/breakage/breakage';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { FormControl, FormsModule } from '@angular/forms';
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
import { debounceTime, distinctUntilChanged, filter, fromEvent, map, switchMap } from 'rxjs';
import { TimeInterval } from 'rxjs/internal/operators/timeInterval';

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
    MatSelectModule
  ],
  templateUrl: './breakage.component.html',
  styleUrl: './breakage.component.css',
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomBreakagePaginatorIntl }
  ]
})
export class BreakageComponent {

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

  sortBy = 'lastUpdatedDate';
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

  dateFormat = DATE_FORMAT;
  breakages: any;
  getAllBreakagesError: ApiResponse;

  // apiResponse: any;
  // isSearching: boolean;
  searchTimer: any;
  inputTimer: any;
  inputError: boolean = false;

  public displayedColumns: string[] = [
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
  public dataSource!: MatTableDataSource<Breakage>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  // @ViewChild('movieSearchInput', { static: true })
  // movieSearchInput!: ElementRef;

  constructor (private _breakageService: BreakageService, public dialog: MatDialog) { 
    this.getAllBreakagesError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllBreakages();

    // this.isSearching = false;
    // this.apiResponse = [];
   }

  //  searchText: string = '';
  //  timeout: any = null;
  //  control = new FormControl();

  
  // Need for updating
  applyFilter(event: Event) {

    clearTimeout(this.searchTimer);
    clearTimeout(this.inputTimer);
    this.inputError = false;

    if (event.type === 'keyup') {
      const value = (event.target as HTMLInputElement).value;
      const filterValue = value.trim().toLowerCase();
      console.log(filterValue)

      if (filterValue.length > 2) {
          this.searchTimer = setTimeout(() => {
            this._breakageService.getBreakagesByText(filterValue)
              .subscribe({
                next: data => {
                  this.breakages = data.content;
                  this.dataSource = new MatTableDataSource(this.breakages);
                  //this.dataSource.paginator = this.paginator;
                  this.dataSource.sort = this.sort;
                  this.totalElements = data.totalElements;
                },
                error: err => {
                  this.getAllBreakagesError = err.error;
                }
              })
          }, 2000);
      } else if (filterValue.length > 0) {
          this.inputTimer = setTimeout(() => {
              this.inputError = true;
          }, 2000);
      } else {
          clearTimeout(this.searchTimer);
          clearTimeout(this.inputTimer);
          this.inputError = false;
          this.getAllBreakages();
      }
      
    }
    //   timer = setTimeout(() => {
    //   const currentDate = new Date();  
    //   console.log(currentDate + 'finish');
    //   this._breakageService
    //     .getBreakagesByText(filterValue)
    //       .subscribe({
    //         next: data => {
    //           this.breakages = data.content;
    //           this.dataSource = new MatTableDataSource(this.breakages);
    //           this.dataSource.sort = this.sort;
    //           this.totalElements = data.totalElements;
    //         },
    //         error: err => {
    //           this.getAllBreakagesError = err.error;
    //         }
    //       });
    // }, 5000);
    //}
    


    

  // applyFilter() {
    // clearTimeout(this.timeout);
    // this.timeout = setTimeout(() => {
    //   this.onSearchChange(this.searchText);
    // }, 5000);



    // fromEvent(this.movieSearchInput.nativeElement, 'keyup')
    //   .pipe(
    //     map((event: any) => event.target.value),
    //     filter((res) => res.length > 2),
    //     debounceTime(5000),
    //     distinctUntilChanged(),
    //     switchMap((term: string) => {
    //       this.isSearching = true;
    //       return this.searchGetCall(term);
    //     })
    //   )



    // let timer;

    // const waitTime = 5000;

    
    

    // const currentDate = new Date();  
    // console.log(currentDate + 'start');
    
    // if (event.type === 'keyup') {
    //   const value = (event.target as HTMLInputElement).value;
    //   const filterValue = value.trim().toLowerCase();

    //   clearTimeout(timer);

    //   timer = setTimeout(() => {
    //     console.log(filterValue);
    //   }, waitTime)
      



  //   deleteResponseMessage() {
  //   setTimeout(() => {
  //     this.apiResponse = ApiResponseFactory.createEmptyApiResponse();
  //   }, 3000);
  //  };

    // this.dataSource.filter = filterValue.trim().toLowerCase();

    // if (this.dataSource.paginator) {
    //   this.dataSource.paginator.firstPage();
    // }
  }

  // searchGetCall(term: string) {
  //   console.log(term)
  // }

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

    this._breakageService
      .getAllBreakages(
        this.pageIndex, this.pageSize, this.sortBy, this.direction, 
        this.statusNew, this.statusSolved, this.statusInProgress, 
        this.statusPaused, this.statusRedirected, this.statusCancelled,
        this.priorityUrgently, this.priorityHigh, this.priorityMedium, this.priorityLow,
        this.executor, this.deadline
      )
        .subscribe({
          next: data => {
            this.breakages = data.content;
            this.dataSource = new MatTableDataSource(this.breakages);
            //this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
            this.totalElements = data.totalElements;
          },
          error: err => {
            this.getAllBreakagesError = err.error;
          }
        })
  };

  getBreakageById(id: string) {
    console.log('getBreakageById() - Id=' + id)
  }


}
